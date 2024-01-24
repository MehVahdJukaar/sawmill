package net.mehvahdjukaar.sawmill;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynServerResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicDataPack;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SawmillRecipeGenerator extends DynServerResourcesGenerator {
    protected SawmillRecipeGenerator(DynamicDataPack pack) {
        super(new DynamicDataPack(SawmillMod.res("sawmill_recipes"),
                Pack.Position.TOP, false, false));
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

    public static void process(Collection<Recipe<?>> recipes,
                               Map<RecipeType<?>, ImmutableMap.Builder<ResourceLocation, Recipe<?>>> map,
                               ImmutableMap.Builder<ResourceLocation, Recipe<?>> builder,
                               ProfilerFiller profiler) {
        profiler.push("swamill_recipes");

        List<WoodcuttingRecipe> sawmillRecipes = process(recipes);

        for (var r : sawmillRecipes) {
            builder.put(r.getId(), r);
            map.computeIfAbsent(r.getType(), (recipeType) -> ImmutableMap.builder())
                    .put(r.getId(), r);
        }
        profiler.pop();
    }

    public static List<WoodcuttingRecipe> process(Collection<Recipe<?>> recipes) {
        SawmillMod.LOGGER.info("Generating Sawmill Recipes");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<Item, Map<WoodType, LogCost>> costs = createIngredientList(recipes, true);
        int maxWoods = WoodTypeRegistry.getTypes().size();
        Ingredient anyPlanks = Ingredient.of(ItemTags.PLANKS);
        Ingredient anyWood = Ingredient.of(ItemTags.LOGS);

        List<WoodcuttingRecipe> sawmillRecipes = new ArrayList<>();
        Map<WoodType, Ingredient> logIngredients = new HashMap<>();
        Map<WoodType, Ingredient> plankIngredients = new HashMap<>();
        String group = "logs";
        String group2 = "planks";
        for (var entry : costs.entrySet()) {
            Item result = entry.getKey();
            String itemId = Utils.getID(result).toDebugFileName();
            int counter = 0;
            Map<WoodType, LogCost> logCosts = entry.getValue();
            if (CommonConfigs.ONLY_VARIANTS.get() && logCosts.size() != 1) continue;
            //if we can use any wood, we assume that log cost is the same for each and add recipe using tags
            if (logCosts.size() == maxWoods) {
                var m = logCosts.get(WoodTypeRegistry.OAK_TYPE);
                addNewRecipe(sawmillRecipes, anyWood, group, result, itemId, counter++, m.cost, false);
                addNewRecipe(sawmillRecipes, anyPlanks, group2, result, itemId, counter++, m.cost * 4, true);
            } else {
                //If not we create a new recipe for each cost as single costs might be different.
                // IDK if grouping here would be worth it
                for (var m : logCosts.values()) {
                    WoodType woodType = m.type;
                    Ingredient logInput = logIngredients.computeIfAbsent(woodType, SawmillRecipeGenerator::makeLogIngredient);
                    if (!logInput.test(result.getDefaultInstance())) {
                        //dont add logs to logs
                        addNewRecipe(sawmillRecipes, logInput, group, result, itemId, counter++, m.cost, false);
                    }
                    Ingredient plankInput = plankIngredients.computeIfAbsent(woodType, SawmillRecipeGenerator::makePlankIngredient);
                    addNewRecipe(sawmillRecipes, plankInput, group2, result, itemId, counter++, m.cost * 4, true);
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
        SawmillMod.clearCacheHacks();
        return sawmillRecipes;
    }

    private static void addLogRecipe(List<WoodcuttingRecipe> sawmillRecipes, WoodType type, int counter,
                                     String from, String to) {
        var fromLog = type.getItemOfThis(from);
        var toLog = type.getItemOfThis(to);
        if (fromLog != null && toLog != null) {
            addNewRecipe(sawmillRecipes, Ingredient.of(fromLog),
                    "log", toLog, type.getAppendableId() + "_log", counter, 1, true);
        }
    }

    private static void addNewRecipe(List<WoodcuttingRecipe> sawmillRecipes, Ingredient input, String group,
                                     Item result, String itemId, int counter, double cost, boolean only1on1) {
        int inputCount = 1;
        double value = (1 / cost) - 0.0001;
        int outputCount;
        if (value > 0.5)
            outputCount = Mth.ceil(value);
        else outputCount = Mth.floor(value);
        if (outputCount < 1) {
            outputCount = 1;
            //discount!
            inputCount = (int) cost;
        }
        if (only1on1 && inputCount != 1) return;
        if (outputCount <= result.getMaxStackSize()) {

            ResourceLocation res = SawmillMod.res(itemId + "_" + counter);

            WoodcuttingRecipe recipe = new WoodcuttingRecipe(res, group, input, new ItemStack(result, outputCount), inputCount);
            sawmillRecipes.add(recipe);

            //planks recipe
        }
    }

    private static Ingredient makePlankIngredient(WoodType type) {
        var children = getAllChildren(type, "planks", "quark:vertical_planks");
        return Ingredient.of(children.toArray(Item[]::new));
    }

    private static Ingredient makeLogIngredient(WoodType type) {
        var children = getAllChildren(type, "log", "wood", "stripped_log", "stripped_wood");
        return Ingredient.of(children.toArray(Item[]::new));
    }

    private static Map<Item, Map<WoodType, LogCost>> createIngredientList(Collection<Recipe<?>> recipes, boolean optim) {
        Map<Item, Map<WoodType, LogCost>> itemToPrimitiveCost = new HashMap<>();
        for (var type : WoodTypeRegistry.getTypes()) {
            Map<WoodType, LogCost> cost = Map.of(type, LogCost.of(type, 1d));
            var children = getAllChildren(type, "log", "wood", "stripped_log", "stripped_wood");
            children.forEach(item -> itemToPrimitiveCost.put(item, cost));
        }

        // remove stuff that has non-whitelisted primitives
        Set<Recipe<?>> validRecipes = new HashSet<>();
        Set<Item> craftableItems = new HashSet<>();
        boolean allowNonBlocks = CommonConfigs.NON_BLOCKS.get();
        for (var recipe : recipes) {
            if (SawmillMod.isWhitelisted(recipe.getType())) {
                try {
                    Item i = recipe.getResultItem(RegistryAccess.EMPTY).getItem();
                    if (!allowNonBlocks && !(i instanceof BlockItem)) continue;
                    if (!recipe.getIngredients().isEmpty()) {
                        craftableItems.add(i);
                        validRecipes.add(recipe);
                    } else {
                        //oh oh
                        int aa = 1;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        //remove all the ones we dont need for sure
        if (optim) removeUnneded(itemToPrimitiveCost, validRecipes, craftableItems);

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

    private static void removeUnneded(Map<Item, Map<WoodType, LogCost>> itemToPrimitiveCost, Set<Recipe<?>> validRecipes, Set<Item> craftableItems) {
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
        boolean isTag = false;
        for (var v : ing.values) {
            if (v instanceof Ingredient.TagValue tv) {
                isTag = true;
                var tag = tv.tag;
                stacks.addAll(SawmillMod.getTagElements(tag));
            }
        }
        if (!isTag) return ing.getItems();
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
        if (itemToUncraft == Items.OAK_SIGN) {
            int aa = 1;
        }
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
