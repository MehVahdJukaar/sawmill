package net.mehvahdjukaar.sawmill;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynServerResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicDataPack;
import net.mehvahdjukaar.moonlight.api.resources.recipe.BlockTypeSwapIngredient;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SawmillRecipeGenerator extends DynServerResourcesGenerator {

    public static void init() {
        PlatHelper.addServerReloadListener(r -> SawmillRecipeGenerator.INSTANCE, SawmillMod.res("recipe_generator"));
    }

    public static final SawmillRecipeGenerator INSTANCE = new SawmillRecipeGenerator(new DynamicDataPack(SawmillMod.res("sawmill_recipes"),
            Pack.Position.TOP, true, true));

    protected SawmillRecipeGenerator(DynamicDataPack pack) {
        super(pack);
        pack.setGenerateDebugResources(CommonConfigs.SAVE_RECIPES.get());
    }

    @Override
    public Collection<String> additionalNamespaces() {
        return List.of("c");
    }

    @Override
    public Logger getLogger() {
        return SawmillMod.LOGGER;
    }

    @Override
    public boolean dependsOnLoadedPacks() {
        return true;
    }

    //UNUSED. implement if mixin in recipe manager causes issues
    @Override
    public void regenerateDynamicAssets(ResourceManager resourceManager) {
        //gather and parse all recipes. then call process
    }

    public static void process(Collection<RecipeHolder<?>> recipes,
                               com.google.common.collect.ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> byName,
                               ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType) {

        List<RecipeHolder<WoodcuttingRecipe>> sawmillRecipes = process(recipes);

        for (var r : sawmillRecipes) {
            byName.put(r.id(), r);
            byType.put(r.value().getType(), r);
        }
    }

    public static List<RecipeHolder<WoodcuttingRecipe>> process(Collection<RecipeHolder<?>> recipes) {
        if (!CommonConfigs.DYNAMIC_RECIPES.get() && !CommonConfigs.SAVE_RECIPES.get()) return List.of();


        SawmillMod.waitForTags();

        SawmillMod.LOGGER.info("Generating Sawmill Recipes");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<Item, Map<WoodType, LogCost>> costs = createIngredientList(recipes, true);
        int maxWoods = WoodTypeRegistry.getTypes().size();
        Ingredient anyPlanks = Ingredient.of(ItemTags.PLANKS);
        Ingredient anyWood = Ingredient.of(ItemTags.LOGS);

        List<RecipeHolder<WoodcuttingRecipe>> sawmillRecipes = new ArrayList<>();
        Map<WoodType, Ingredient> logIngredients = new HashMap<>();
        Map<WoodType, Ingredient> plankIngredients = new HashMap<>();
        String group = "logs";
        String group2 = "planks";

        for (var entry : costs.entrySet()) {
            Item result = entry.getKey();
            String itemId = Utils.getID(result).toDebugFileName();
            int counter = 0;
            Map<WoodType, LogCost> logCosts = entry.getValue();
            if (!CommonConfigs.ALLOW_NON_VARIANTS.get() && logCosts.size() != 1) continue;
            //if we can use any wood, we assume that log cost is the same for each and add recipe using tags
            if (logCosts.size() == maxWoods) {
                var m = logCosts.get(WoodTypeRegistry.OAK_TYPE);
                addNewRecipe(sawmillRecipes, anyWood, group, result, itemId, counter++, m.cost, false);
                addNewRecipe(sawmillRecipes, anyPlanks, group2, result, itemId, counter++, getPlanksCost(WoodTypeRegistry.OAK_TYPE, m), true);
            } else {
                //If not we create a new recipe for each cost as single costs might be different.
                // IDK if grouping here would be worth it
                for (var m : logCosts.values()) {
                    WoodType woodType = m.type;
                    Ingredient logInput = getOrCreateLogIngredient(logIngredients, woodType);
                    if (!logInput.test(result.getDefaultInstance())) {
                        //dont add logs to logs
                        addNewRecipe(sawmillRecipes, logInput, group, result, itemId, counter++, m.cost, false);
                    }
                    Ingredient plankInput = getOrCreatePlankIngredient(plankIngredients, woodType);
                    if (!plankInput.test(result.getDefaultInstance())) {
                        addNewRecipe(sawmillRecipes, plankInput, group2, result, itemId, counter++, getPlanksCost(woodType, m), true);
                    }
                }
            }
        }
        for (WoodType type : WoodTypeRegistry.getTypes()) {
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
        SawmillMod.LOGGER.info("Generated Sawmill recipes in {} milliseconds", millis);
        if (millis > 2000) {
            SawmillMod.LOGGER.warn("Generating Sawmill recipes took a long time. Consider disabling dynamic recipes in the configs and adding them statically via datapack. You can turn on save_recipe configs to help you with that");
        }
        if (millis > 7000) {
            SawmillMod.LOGGER.error("You might really want to consider above advice...");
        }

        SawmillMod.clearTagHacks();

        if (CommonConfigs.SAVE_RECIPES.get()) {
            for (var r : sawmillRecipes) {
                INSTANCE.dynamicPack.addRecipe(r);
            }
        }

        if (!CommonConfigs.DYNAMIC_RECIPES.get()) return List.of();

        RecipeSorter.accept(sawmillRecipes);
        return sawmillRecipes;
    }

    private static double getPlanksCost(WoodType type, LogCost m) {
        if (type.getTypeName().equals("bamboo")) return m.cost * 2;
        return m.cost * 4;
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
        int maxStackSize = result.components().getOrDefault(DataComponents.MAX_STACK_SIZE, 1);

        InputOutputCost resCost = getInputOutputCost(cost, maxStackSize);
        int inputCount = resCost.inputCount();
        int outputCount = resCost.outputCount();
        if (only1on1 && inputCount != 1 && CommonConfigs.PLANKS_ONLY_ONE.get()) return;
        if (outputCount > 0) {

            // we know that we are going to add cost with 1 too,
            // so we check what cost with that would be to match it if needed
            if (!only1on1 && false) {
                var costWith1 = getInputOutputCost(cost / 4, maxStackSize);
                if (costWith1.inputCount == 1 && costWith1.outputCount / cost > outputCount) {
                    outputCount = (int) (costWith1.outputCount / cost);
                }
            }
            ResourceLocation res = SawmillMod.res(itemId + "_" + counter);
            if (inputCount > 64) {
                SawmillMod.LOGGER.error("Sawmill tried to generate a recipe with too high input count: {}. Ingredient: {}, Result: {},ID: {}", inputCount, input, result, res);
            } else {
                WoodcuttingRecipe recipe = new WoodcuttingRecipe(group, input, new ItemStack(result, outputCount), inputCount);
                sawmillRecipes.add(new RecipeHolder<>(res, recipe));
            }
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
        outputCount += Math.round(preciseOutputCount % 1 > considerDiscountThreshold ?
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
            var children = getAllChildren(type, "planks", "quark:vertical_planks");
            return Ingredient.of(children.toArray(Item[]::new));
        });
    }

    private static Ingredient getOrCreateLogIngredient(Map<WoodType, Ingredient> cache, WoodType type) {
        return cache.computeIfAbsent(type, t -> {
            // I hate this wood type very much
            if (t.getTypeName().equals("archwood")) {
                return Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:logs/archwood")));
            }
            var children = getAllChildren(type, "log", "wood", "stripped_log", "stripped_wood");
            return Ingredient.of(children.toArray(Item[]::new));
        });
    }

    private static Map<Item, Map<WoodType, LogCost>> createIngredientList(Collection<RecipeHolder<?>> recipes, boolean optim) {
        Map<Item, Map<WoodType, LogCost>> itemToPrimitiveCost = new HashMap<>();
        //primitive costs
        for (var type : WoodTypeRegistry.getTypes()) {
            Map<WoodType, LogCost> logCostInLog = Map.of(type, LogCost.of(type, 1d));
            var children = getAllChildren(type, "log", "wood", "stripped_log", "stripped_wood");
            children.forEach(item -> itemToPrimitiveCost.put(item, logCostInLog));

            //shamelessly hardcodes sticks
            itemToPrimitiveCost.computeIfAbsent(Items.STICK, s -> new HashMap<>()).put(type, LogCost.of(type, 0.125));
        }

        addHardcodedCosts(itemToPrimitiveCost);

        // remove stuff that has non-whitelisted primitives
        Set<Recipe<?>> validRecipes = new HashSet<>();
        Set<Item> craftableItems = new HashSet<>();
        boolean allowNonBlocks = CommonConfigs.ALLOW_NON_BLOCKS.get();
        for (var recipe : recipes) {
            if (SawmillMod.isWhitelisted(recipe)) {
                try {
                    Recipe<?> value = recipe.value();
                    Item i = value.getResultItem(RegistryAccess.EMPTY).getItem();

                    if (!allowNonBlocks && !(i instanceof BlockItem)) continue;
                    if (!value.getIngredients().isEmpty()) {
                        craftableItems.add(i);
                        validRecipes.add(value);
                    } else {
                        //oh oh
                        int aa = 1;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        //remove all the ones we dont need for sure
        if (optim) removeUnNeded(itemToPrimitiveCost, validRecipes, craftableItems);

        craftableItems.clear();
        Multimap<Item, Recipe<?>> itemsToRecipe = HashMultimap.create();
        for (var r : validRecipes) {
            Item res = r.getResultItem(RegistryAccess.EMPTY).getItem();
            itemsToRecipe.put(res, r);
            craftableItems.add(res);
        }


        //magic
        for (var item : craftableItems) {
            getPrimitiveCostRecursive(item, itemsToRecipe, itemToPrimitiveCost, new HashSet<>());
        }
        itemToPrimitiveCost.values().removeIf(Objects::isNull);
        return itemToPrimitiveCost;
    }

    private static void addHardcodedCosts(Map<Item, Map<WoodType, LogCost>> itemToPrimitiveCost) {
        Map<String, Double> specialCosts = new HashMap<>(CommonConfigs.SPECIAL_COSTS.get());
        var iter = specialCosts.entrySet().iterator();
        while (iter.hasNext()) {
            var c = iter.next();
            String id = c.getKey();
            double costInLogs = c.getValue() / 4d;
            boolean hasWood = false;
            for (var type : WoodTypeRegistry.getTypes()) {
                Item woodItem = type.getItemOfThis(id);
                if (woodItem != null) {
                    Map<WoodType, LogCost> stairsCostInLog = Map.of(type, LogCost.of(type, costInLogs));
                    itemToPrimitiveCost.put(woodItem, stairsCostInLog);
                    hasWood = true;
                }
            }
            if (!hasWood) {
                var opt = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id));
                if (opt.isPresent()) {
                    var cost = WoodTypeRegistry.getTypes().stream().collect(Collectors.toMap(Function.identity(),
                            type -> LogCost.of(type, costInLogs)));
                    itemToPrimitiveCost.put(opt.get(), cost);
                }
            }
            iter.remove();
        }
    }

    private static void removeUnNeded(Map<Item, Map<WoodType, LogCost>> itemToPrimitiveCost, Set<Recipe<?>> validRecipes, Set<Item> craftableItems) {
        Iterator<Recipe<?>> iterator = validRecipes.iterator();
        outer:
        while (iterator.hasNext()) {
            Recipe<?> recipe = iterator.next();

            for (var ing : recipe.getIngredients()) {
                //don't consider air
                if (!ing.isEmpty()) {
                    // it all alternatives of an ingredient contain a primitive we remove
                    boolean atLeastOneCorrect = false;

                    for (var alternative : getIngItems(ing)) {
                        Item a = alternative.getItem();

                        // if we don't have a recipe for this it means it's a primitive. if it's not whitelisted we remove
                        if (itemToPrimitiveCost.containsKey(a) || craftableItems.contains(a)) {
                            // it is not primitive or its primitive of the right type.
                            atLeastOneCorrect = true;
                        }
                    }
                    //if an entire ing group is made up of invalid primitives then entire recipe is invalid
                    if (!atLeastOneCorrect) {
                        iterator.remove();
                        continue outer;
                    }
                }
            }
        }
    }

    @NotNull
    private static ItemStack[] getIngItems(Ingredient ing) {
        List<ItemStack> stacks = new ArrayList<>();
        boolean isVanilla = SawmillMod.isVanillaIngredient(ing);
        if (!isVanilla && SawmillMod.getCustomIngredient(ing) instanceof BlockTypeSwapIngredient<?> bts) {
            ItemStack[] innerConverted = getIngItems(bts.getInner());
            stacks.addAll(bts.convertItems(Arrays.stream(innerConverted).toList()));
        }

        if (!isVanilla) {
            return stacks.toArray(ItemStack[]::new);
        }
        boolean isTag = false;
        if (isVanilla) {
            for (var v : ing.values) {
                if (v instanceof Ingredient.TagValue tv) {
                    isTag = true;
                    var tag = tv.tag;
                    stacks.addAll(SawmillMod.getTagElements(tag));
                }
            }

        }

        //TODO: add support for forge custom ingredients with tags
        if (!isTag) {
            // very, very bad
            stacks.addAll(List.of(ing.getItems()));
            // get item is unsafe to call here. we must reset it
            // this is still not enough for compount ingredients...
            ing.itemStacks = null;
        }

        return stacks.toArray(ItemStack[]::new);
    }


    private static List<Item> getAllChildren(WoodType type, String... keys) {
        List<Item> children = new ArrayList<>();
        for (var k : keys) {
            var child = type.getChild(k);
            if (child instanceof ItemLike il) {
                children.add(il.asItem());
            }
        }
        return children;
    }

    @Nullable
    public static Map<WoodType, LogCost> getPrimitiveCostRecursive(Item itemToUncraft, Multimap<Item, Recipe<?>> allRecipes,
                                                                   Map<Item, Map<WoodType, LogCost>> cache, Set<Recipe<?>> visitedRecipes) {
        var cached = cache.get(itemToUncraft);
        if (cached != null) return cached;
        //try to uncraft looping through all its recipes
        List<Map<WoodType, LogCost>> possibleCosts = new ArrayList<>();
        Collection<Recipe<?>> possibleRecipes = allRecipes.get(itemToUncraft);

        outer:
        for (var recipe : possibleRecipes) {
            if (visitedRecipes.contains(recipe)) {
                continue;
            }
            visitedRecipes.add(recipe);
            Map<WoodType, LogCost> recipeCostPerWood = new HashMap<>();
            for (var ingredient : recipe.getIngredients()) {
                if (ingredient.isEmpty()) {
                    continue;
                }
                //This is an EITHER. Each entry are equivalent to create this specific ingredient
                Map<WoodType, LogCost> ingredientPossibleCosts = new HashMap<>();
                //get log cost for each item in the ingredient
                for (ItemStack ing : getIngItems(ingredient)) {
                    Map<WoodType, LogCost> itemCost = getPrimitiveCostRecursive(ing.getItem(), allRecipes, cache, visitedRecipes);
                    if (itemCost != null) {
                        itemCost.forEach((woodType, logCost) -> {
                            ingredientPossibleCosts.merge(woodType, logCost, LogCost::min);
                        });
                    }
                }
                //this ingredient had no log cost for any of its items. this means its invalid.
                //this also means the entire recipe is invalid
                if (ingredientPossibleCosts.isEmpty()) {
                    continue outer;
                }
                if (recipeCostPerWood.isEmpty()) {
                    //first ing
                    recipeCostPerWood.putAll(ingredientPossibleCosts);
                } else {
                    //remove non common ones
                    recipeCostPerWood.keySet().retainAll(ingredientPossibleCosts.keySet());
                    // invalidate if its empty
                    if (recipeCostPerWood.isEmpty()) {
                        continue outer;
                    }
                    //merge
                    recipeCostPerWood.forEach((key, val) ->
                            recipeCostPerWood.merge(key, ingredientPossibleCosts.get(key), LogCost::sum)
                    );
                }
            }

            int outputCount = recipe.getResultItem(RegistryAccess.EMPTY).getCount();
            recipeCostPerWood.replaceAll((woodType, logCost) -> logCost.divide(outputCount));
            possibleCosts.add(recipeCostPerWood);
        }
        Map<WoodType, LogCost> ret = null;
        if (!possibleCosts.isEmpty()) {
            //choose min costs
            ret = chooseMinCost(possibleCosts);
        }
        // cache it whether null or not
        cache.put(itemToUncraft, ret);

        return ret;
    }

    public static Map<WoodType, LogCost> chooseMinCost(List<Map<WoodType, LogCost>> possibleRecipeCosts) {
        Map<WoodType, LogCost> result = new HashMap<>();

        for (Map<WoodType, LogCost> map : possibleRecipeCosts) {
            for (Map.Entry<WoodType, LogCost> entry : map.entrySet()) {
                WoodType key = entry.getKey();
                LogCost value = entry.getValue();

                result.merge(key, value, LogCost::min);
            }
        }
        return result;
    }


    private record LogCost(WoodType type, Double cost) {
        static LogCost of(WoodType type, Double amount) {
            return new LogCost(type, amount);
        }

        public LogCost sum(LogCost logCost) {
            return new LogCost(this.type, logCost.cost + this.cost);
        }

        public LogCost divide(double outputCount) {
            return new LogCost(this.type, this.cost / outputCount);
        }

        public static LogCost min(LogCost first, LogCost second) {
            return first.cost < second.cost ? first : second;
        }
    }

}
