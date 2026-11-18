package net.mehvahdjukaar.sawmill;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicServerResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodTypes;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.sawmill.integration.CreateCompat;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.*;
import java.util.function.Consumer;

public class SawmillRecipeGenerator extends DynamicServerResourceProvider {

    public static final SawmillRecipeGenerator INSTANCE = new SawmillRecipeGenerator();
    private boolean willRegenThisReload = false;

    protected SawmillRecipeGenerator() {
        super(SawmillMod.res("sawmill_recipes"), CommonConfigs.GEN_MODE.get().getStrategy());
    }

    public boolean willGeneratingRecipesThisReload() {
        return this.willRegenThisReload;
    }

    public static void init() {
        RegHelper.registerDynamicResourceProvider(INSTANCE);
    }

    @Override
    protected Collection<String> gatherSupportedNamespaces() {
        return List.of("c");
    }

    @Override
    public void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
        executor.accept((a, b) -> {
            SawmillMod.LOGGER.info("Scheduling Sawmill recipe generation");
            //so log shuts up about no tasks
        });
        this.willRegenThisReload = true;
    }

    private void saveRecipesToPack(List<RecipeHolder<WoodcuttingRecipe>> sawmillRecipes) {
        SawmillMod.LOGGER.info("Saving {} Sawmill Recipes to resource pack", sawmillRecipes.size());
        ResourceSink sink = new ResourceSink("dummy", "dummy");
        for (var r : sawmillRecipes) {
            sink.addRecipe(r);
        }
        ResourceSink.acceptSinks(this.packResources, List.of(sink));

        this.packResources.commitChanges();
    }

    public void process(Collection<RecipeHolder<?>> recipes,
                        HolderLookup.Provider registries,
                        ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> byName,
                        ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType) {

        SawmillMod.LOGGER.info("Processing Sawmill Recipes into Recipe Manager");
        List<RecipeHolder<WoodcuttingRecipe>> sawmillRecipes = process(recipes);

        for (var r : sawmillRecipes) {
            try {
                byName.put(r.id(), r);
                byType.put(r.value().getType(), r);
            } catch (Exception e) {
                throw new RuntimeException("Failed to add sawmill recipe " + r.id(), e);
            }
        }

        CreateCompat.addCuttingRecipes(recipes, sawmillRecipes, registries, byName, byType);
    }

    public List<RecipeHolder<WoodcuttingRecipe>> process(Collection<RecipeHolder<?>> recipes) {
        if (this.willRegenThisReload) {
            this.willRegenThisReload = false;

        } else {
            SawmillMod.LOGGER.info("Skipping Sawmill recipe generation as packs didn't change");
            //sort recipes anyways
            List<RecipeHolder<WoodcuttingRecipe>> existing = new ArrayList<>();
            for (var r : recipes) {
                if (r.value() instanceof WoodcuttingRecipe) {
                    existing.add((RecipeHolder<WoodcuttingRecipe>) r);
                }
            }
            RecipeSorter.accept(existing);
            return List.of();
        }


        SawmillMod.waitForTags();
        SawmillMod.bindItemTagsEarly();

        SawmillMod.LOGGER.info("Generating Sawmill Recipes");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<Item, Map<WoodType, Double>> costs = LogCostCalculator.calculate(recipes);
        int maxWoods = WoodTypeRegistry.INSTANCE.size();
        Ingredient anyPlanks = Ingredient.of(ItemTags.PLANKS);
        Ingredient anyWood = Ingredient.of(ItemTags.LOGS);

        List<RecipeHolder<WoodcuttingRecipe>> sawmillRecipes = new ArrayList<>();
        Map<WoodType, Ingredient> logIngredients = new HashMap<>();
        Map<WoodType, Ingredient> plankIngredients = new HashMap<>();

        for (var entry : costs.entrySet()) {
            Item result = entry.getKey();
            String itemId = Utils.getID(result).toDebugFileName();
            int counter = 0;
            Map<WoodType, Double> logCosts = entry.getValue();
            if (!CommonConfigs.ALLOW_NON_VARIANTS.get() && logCosts.size() != 1) continue;
            //if we can use any wood, we assume that log cost is the same for each and add recipe using tags
            if (logCosts.size() == maxWoods) {
                double cost = logCosts.get(VanillaWoodTypes.OAK);
                addNewRecipe(sawmillRecipes, anyWood, "logs", result, itemId, counter++, cost, false);

                addNewRecipe(sawmillRecipes, anyPlanks, "planks", result, itemId, counter++, getPlanksCost(VanillaWoodTypes.OAK, cost), true);
            } else {
                //If not we create a new recipe for each cost as single costs might be different.
                // IDK if grouping here would be worth it
                for (var c : logCosts.entrySet()) {
                    WoodType woodType = c.getKey();
                    double cost = c.getValue();
                    Ingredient logInput = getOrCreateLogIngredient(logIngredients, woodType);
                    if (!containsItem(logInput, result)) {
                        //dont add logs to logs
                        addNewRecipe(sawmillRecipes, logInput, "logs", result, itemId, counter++, cost, false);
                    }
                    Ingredient plankInput = getOrCreatePlankIngredient(plankIngredients, woodType);
                    if (!containsItem(plankInput, result)) {
                        addNewRecipe(sawmillRecipes, plankInput, "planks", result, itemId, counter++, getPlanksCost(woodType, cost), true);
                    }
                }
            }
        }
        for (WoodType type : WoodTypeRegistry.INSTANCE) {
            int counter = 0;
            //adds logs recipes
            addLogRecipe(sawmillRecipes, type, counter++, "log", "stripped_log");
            addLogRecipe(sawmillRecipes, type, counter++, "log", "stripped_wood");
            addLogRecipe(sawmillRecipes, type, counter++, "log", "wood");
            addLogRecipe(sawmillRecipes, type, counter++, "wood", "log");
            addLogRecipe(sawmillRecipes, type, counter++, "wood", "stripped_wood");
            addLogRecipe(sawmillRecipes, type, counter++, "wood", "stripped_log");
            addLogRecipe(sawmillRecipes, type, counter++, "stripped_wood", "stripped_log");
            addLogRecipe(sawmillRecipes, type, counter++, "stripped_log", "stripped_wood");
        }


        long millis = stopwatch.elapsed().toMillis();
        SawmillMod.LOGGER.info("Generated Sawmill recipes in {} milliseconds (cac mode {})", millis, this.generationStrategy);

        SawmillMod.clearTagHacks();

        this.saveRecipesToPack(sawmillRecipes);

        RecipeSorter.accept(sawmillRecipes);
        return sawmillRecipes;
    }

    private static double getPlanksCost(WoodType type, double logCost) {
        if (type.getTypeName().equals("bamboo")) return logCost * 2;
        return logCost * 4;
    }

    private static void addLogRecipe(List<RecipeHolder<WoodcuttingRecipe>> sawmillRecipes, WoodType type, int counter,
                                     String from, String to) {
        var fromLog = type.getItemOfThis(from);
        var toLog = type.getItemOfThis(to);
        if (fromLog != null && toLog != null) {
            addNewRecipe(sawmillRecipes, Ingredient.of(fromLog),
                    "log", toLog, type.getAppendableId() + "_log", counter, 1, true);
        }
    }

    private static void addNewRecipe(List<RecipeHolder<WoodcuttingRecipe>> sawmillRecipes, Ingredient input, String group,
                                     Item result, String itemId, int counter, double cost, boolean only1on1) {
        if (input.isEmpty()) return; //empty ingredient crashes codec when serializing
        int maxStackSize = result.components().getOrDefault(DataComponents.MAX_STACK_SIZE, 1);

        InputOutputCost resCost = getInputOutputCost(cost, maxStackSize);
        int inputCount = resCost.inputCount();
        int outputCount = resCost.outputCount();
        if (only1on1 && inputCount != 1 && CommonConfigs.PLANKS_ONLY_ONE.get()) return;
        if (outputCount <= 0 || inputCount > 64) {
            return;
        }

        WoodcuttingRecipe recipe = new WoodcuttingRecipe(group, input, new ItemStack(result, outputCount), inputCount);
        sawmillRecipes.add(new RecipeHolder<>(SawmillMod.res(itemId + "_" + counter), recipe));
    }

    //TODO: finish
    private static InputOutputCost getInputOutputCost(double cost, int maxOutputCount) {
        int inputCount = 1;
        int outputCount = 0;
        double maxDiscount = CommonConfigs.MAX_DISCOUNT.get(); //gives at most 0.25 log free

        if (cost > (1 + maxDiscount)) {
            return new InputOutputCost((int) cost, 1);
        }
        // all of this is totally made up
        double preciseOutputCount = (1 / cost);
        cost /= (1 + maxDiscount); // 0.4 cost : 1.25 = 0.3 discounted
        double discountedOutput = (1 / cost);
        double considerDiscountThreshold = 0.25;
        //this used to be floor. might be more forgiving like this but also more op
        outputCount += (int) Math.round(preciseOutputCount % 1 > considerDiscountThreshold ?
                (preciseOutputCount + discountedOutput) / 2f : preciseOutputCount);

        if (outputCount > maxOutputCount) {
            double ratio = (double) maxOutputCount / outputCount;
            outputCount = maxOutputCount;
            inputCount = Mth.ceil(inputCount * ratio);
        }
        return new InputOutputCost(inputCount, outputCount);
    }

    private record InputOutputCost(int inputCount, int outputCount) {
    }


    private static Ingredient getOrCreatePlankIngredient(Map<WoodType, Ingredient> cache, WoodType type) {
        return cache.computeIfAbsent(type, t -> {
            var children = LogCostCalculator.getAllChildren(type, "planks", "quark:vertical_planks");
            return warnIfEmpty(Ingredient.of(children.toArray(Item[]::new)), type, "plank");
        });
    }

    private static Ingredient getOrCreateLogIngredient(Map<WoodType, Ingredient> cache, WoodType type) {
        return cache.computeIfAbsent(type, t -> {
            // I hate this wood type very much
            if (t.getTypeName().equals("archwood")) {
                return Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:logs/archwood")));
            }
            var children = LogCostCalculator.getAllChildren(type, "log", "wood", "stripped_log", "stripped_wood");
            return warnIfEmpty(Ingredient.of(children.toArray(Item[]::new)), type, "log");
        });
    }

    private static Ingredient warnIfEmpty(Ingredient ingredient, WoodType type, String kind) {
        if (ingredient.isEmpty()) {
            SawmillMod.LOGGER.warn("Wood type {} has no {} items, skipping its {} recipes", type.getTypeName(), kind, kind);
        }
        return ingredient;
    }

    //cant use Ingredient.test here. on tag ingredients (archwood) it would cache an empty item list since tags aren t bound yet
    private static boolean containsItem(Ingredient ing, Item item) {
        for (var stack : SawmillMod.getIngItems(ing)) {
            if (stack.is(item)) return true;
        }
        return false;
    }

}
