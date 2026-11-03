package net.mehvahdjukaar.sawmill;

import com.google.common.base.Stopwatch;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicServerResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodTypes;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.sawmill.integration.CreateCompat;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SawmillRecipeGenerator extends DynamicServerResourceProvider {

    public static final SawmillRecipeGenerator INSTANCE = new SawmillRecipeGenerator();
    private boolean willRegenThisReload = false;

    protected SawmillRecipeGenerator() {
        super(SawmillMod.res("sawmill_recipes"), CommonConfigs.GEN_MODE.get().getStrategy());
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

    private void saveRecipesToPack(List<RecipeHolder<WoodcuttingRecipe>> sawmillRecipes, HolderLookup.Provider registries) {
        SawmillMod.LOGGER.info("Saving {} Sawmill Recipes to resource pack", sawmillRecipes.size());
        RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
        ResourceSink sink = new ResourceSink("dummy", "dummy");
        for (var r : sawmillRecipes) {
            JsonElement json = Recipe.CODEC.encodeStart(ops, r.value()).getOrThrow();
            sink.addJson(r.id().identifier(), json, ResType.RECIPES);
        }
        ResourceSink.acceptSinks(this.packResources, List.of(sink));

        this.packResources.commitChanges();
    }

    public static RecipeMap onRecipesLoaded(RecipeMap loaded, HolderLookup.Provider registries) {
        List<RecipeHolder<WoodcuttingRecipe>> generated = INSTANCE.process(loaded.values(), registries);

        List<RecipeHolder<WoodcuttingRecipe>> allWoodcutting = new ArrayList<>(generated);
        allWoodcutting.addAll(loaded.byType(SawmillMod.WOODCUTTING_RECIPE.get()));

        List<RecipeHolder<?>> extra = new ArrayList<>(generated);
        extra.addAll(CreateCompat.makeCuttingRecipes(allWoodcutting, registries));

        RecipeMap result = loaded;
        if (!extra.isEmpty()) {
            List<RecipeHolder<?>> all = new ArrayList<>(loaded.values());
            all.addAll(extra);
            result = RecipeMap.create(all);
        }

        List<WoodcuttingEntry> entries = result.byType(SawmillMod.WOODCUTTING_RECIPE.get()).stream()
                .map(WoodcuttingEntry::of)
                .toList();
        WoodcuttingRecipes.setUnsorted(entries);
        return result;
    }

    public List<RecipeHolder<WoodcuttingRecipe>> process(Collection<RecipeHolder<?>> recipes, HolderLookup.Provider registries) {
        if (!this.willRegenThisReload) {
            SawmillMod.LOGGER.info("Skipping Sawmill recipe generation as packs didn't change");
            return List.of();
        }
        this.willRegenThisReload = false;

        SawmillMod.LOGGER.info("Generating Sawmill Recipes");
        Stopwatch stopwatch = Stopwatch.createStarted();

        Context context = new Context(registries);
        Map<Item, Map<WoodType, Double>> costs = LogCostCalculator.calculate(recipes, context);
        int maxWoods = WoodTypeRegistry.INSTANCE.size();
        Ingredient anyPlanks = Ingredient.of(context.itemTag(ItemTags.PLANKS));
        Ingredient anyWood = Ingredient.of(context.itemTag(ItemTags.LOGS));

        List<RecipeHolder<WoodcuttingRecipe>> sawmillRecipes = new ArrayList<>();
        Map<WoodType, Optional<Ingredient>> logIngredients = new HashMap<>();
        Map<WoodType, Optional<Ingredient>> plankIngredients = new HashMap<>();

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
                    Ingredient logInput = getOrCreateLogIngredient(logIngredients, woodType, context).orElse(null);
                    //dont add logs to logs
                    if (logInput != null && !logInput.test(result.getDefaultInstance())) {
                        addNewRecipe(sawmillRecipes, logInput, "logs", result, itemId, counter++, cost, false);
                    }
                    Ingredient plankInput = getOrCreatePlankIngredient(plankIngredients, woodType).orElse(null);
                    if (plankInput != null && !plankInput.test(result.getDefaultInstance())) {
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
        SawmillMod.LOGGER.info("Generated Sawmill recipes in {} milliseconds (cache mode {})", millis, this.generationStrategy);

        this.saveRecipesToPack(sawmillRecipes, registries);

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
        int maxStackSize = result.getDefaultMaxStackSize();

        InputOutputCost resCost = getInputOutputCost(cost, maxStackSize);
        int inputCount = resCost.inputCount();
        int outputCount = resCost.outputCount();
        if (only1on1 && inputCount != 1 && CommonConfigs.PLANKS_ONLY_ONE.get()) return;
        if (outputCount > 0 && inputCount <= 64) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SawmillMod.res(itemId + "_" + counter));
            WoodcuttingRecipe recipe = new WoodcuttingRecipe(new Recipe.CommonInfo(true), input,
                    new ItemStackTemplate(result, outputCount), inputCount);
            sawmillRecipes.add(new RecipeHolder<>(key, recipe));
        }
    }

    //TODO: finish
    @NotNull
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

    private static Optional<Ingredient> getOrCreatePlankIngredient(Map<WoodType, Optional<Ingredient>> cache, WoodType type) {
        return cache.computeIfAbsent(type, t -> {
            var children = LogCostCalculator.getAllChildren(type, "planks", "quark:vertical_planks");
            return warnIfEmpty(ingredientOf(children), type, "plank");
        });
    }

    private static Optional<Ingredient> getOrCreateLogIngredient(Map<WoodType, Optional<Ingredient>> cache, WoodType type,
                                                                 Context context) {
        return cache.computeIfAbsent(type, t -> {
            // I hate this wood type very much
            if (t.getTypeName().equals("archwood")) {
                HolderSet<Item> tag = context.itemTagOrNull(
                        TagKey.create(Registries.ITEM, Identifier.parse("c:logs/archwood")));
                if (tag != null) return Optional.of(Ingredient.of(tag));
            }
            var children = LogCostCalculator.getAllChildren(type, "log", "wood", "stripped_log", "stripped_wood");
            return warnIfEmpty(ingredientOf(children), type, "log");
        });
    }

    private static Optional<Ingredient> ingredientOf(List<Item> items) {
        return items.isEmpty() ? Optional.empty() : Optional.of(Ingredient.of(items.toArray(Item[]::new)));
    }

    private static Optional<Ingredient> warnIfEmpty(Optional<Ingredient> ingredient, WoodType type, String kind) {
        if (ingredient.isEmpty()) {
            SawmillMod.LOGGER.warn("Wood type {} has no {} items, skipping its {} recipes", type.getTypeName(), kind, kind);
        }
        return ingredient;
    }

    public static class Context {
        private final HolderLookup.Provider registries;
        private final ContextMap displayContext;
        private final Set<RecipeType<?>> whitelist;
        private final Map<Recipe<?>, ItemStack> resultCache = new HashMap<>();

        public Context(HolderLookup.Provider registries) {
            this.registries = registries;
            this.displayContext = new ContextMap.Builder()
                    .withParameter(SlotDisplayContext.REGISTRIES, registries)
                    .create(SlotDisplayContext.CONTEXT);
            this.whitelist = registries.lookupOrThrow(Registries.RECIPE_TYPE)
                    .get(SawmillMod.RECIPE_WHITELIST)
                    .map(set -> set.stream().map(Holder::value).collect(Collectors.<RecipeType<?>>toSet()))
                    .orElseGet(Set::of);
        }

        public HolderSet<Item> itemTag(TagKey<Item> tag) {
            return registries.lookupOrThrow(Registries.ITEM).getOrThrow(tag);
        }

        @Nullable
        public HolderSet<Item> itemTagOrNull(TagKey<Item> tag) {
            return registries.lookupOrThrow(Registries.ITEM).get(tag).orElse(null);
        }

        public boolean isWhitelisted(RecipeHolder<?> recipe) {
            if (!whitelist.contains(recipe.value().getType())) return false;
            return !CommonConfigs.MOD_BLACKLIST.get().contains(recipe.id().identifier().getNamespace());
        }

        public ItemStack resultOf(Recipe<?> recipe) {
            return resultCache.computeIfAbsent(recipe, r -> {
                var displays = r.display();
                if (displays.isEmpty()) return ItemStack.EMPTY;
                return displays.getFirst().result().resolveForFirstStack(displayContext);
            });
        }
    }
}
