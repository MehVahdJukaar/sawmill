package net.mehvahdjukaar.sawmill;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.*;

public class LogCostCalculator {

    private final Map<Item, Map<WoodType, Double>> costs = new HashMap<>();
    private final Map<Item, Map<WoodType, Double>> incompleteCosts = new HashMap<>();
    private final Multimap<Item, Recipe<?>> recipesByResult = HashMultimap.create();
    private final Set<Item> inProgress = new HashSet<>();
    private final SawmillRecipeGenerator.Context context;
    //how many time current item gave up (loop or too deep)
    private int giveUps = 0;

    private LogCostCalculator(SawmillRecipeGenerator.Context context) {
        this.context = context;
    }

    public static Map<Item, Map<WoodType, Double>> calculate(Collection<RecipeHolder<?>> recipes, SawmillRecipeGenerator.Context context) {
        LogCostCalculator calc = new LogCostCalculator(context);
        calc.addPrimitiveCosts();
        calc.addSpecialCosts();
        calc.collectRecipes(recipes);
        calc.uncraftAll();
        return calc.costs;
    }

    private void addPrimitiveCosts() {
        for (var type : WoodTypeRegistry.INSTANCE) {
            Map<WoodType, Double> oneLog = Map.of(type, 1d);
            for (var item : getAllChildren(type, "log", "wood", "stripped_log", "stripped_wood")) {
                costs.put(item, oneLog);
            }

            //shamelessly hardcodes sticks
            costs.computeIfAbsent(Items.STICK, s -> new HashMap<>()).put(type, 0.125);
        }
    }

    private void addSpecialCosts() {
        for (var c : CommonConfigs.SPECIAL_COSTS.get().entrySet()) {
            String id = c.getKey();
            double costInLogs = c.getValue() / 4d;
            boolean hasWood = false;
            for (var type : WoodTypeRegistry.INSTANCE) {
                Item woodItem = type.getItemOfThis(id);
                if (woodItem != null) {
                    costs.put(woodItem, Map.of(type, costInLogs));
                    hasWood = true;
                }
            }
            if (!hasWood) {
                Item item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElse(null);
                if (item != null) {
                    Map<WoodType, Double> cost = new HashMap<>();
                    for (var type : WoodTypeRegistry.INSTANCE) cost.put(type, costInLogs);
                    costs.put(item, cost);
                }
            }
        }
    }

    private void collectRecipes(Collection<RecipeHolder<?>> recipes) {
        boolean allowNonBlocks = CommonConfigs.ALLOW_NON_BLOCKS.get();
        Set<Recipe<?>> validRecipes = new HashSet<>();
        Set<Item> craftableItems = new HashSet<>();
        for (var holder : recipes) {
            if (!context.isWhitelisted(holder)) continue;
            try {
                Recipe<?> recipe = holder.value();
                ItemStack resultStack = context.resultOf(recipe);
                if (resultStack.isEmpty()) continue;
                Item result = resultStack.getItem();
                if (!allowNonBlocks && !(result instanceof BlockItem)) continue;
                if (recipe.placementInfo().ingredients().isEmpty()) continue;
                validRecipes.add(recipe);
                craftableItems.add(result);
            } catch (Exception ignored) {
            }
        }

        //remove all the ones we dont need for sure
        validRecipes.removeIf(r -> !canBeMadeOfWood(r, craftableItems));
        for (var r : validRecipes) {
            recipesByResult.put(context.resultOf(r).getItem(), r);
        }
    }

    private boolean canBeMadeOfWood(Recipe<?> recipe, Set<Item> craftableItems) {
        for (var ing : recipe.placementInfo().ingredients()) {
            if (ing.isEmpty()) continue;
            boolean atLeastOneCorrect = false;
            for (var alternative : ing.items().toList()) {
                Item a = alternative.value();
                // if we don't have a recipe for this it means it's a primitive. if it's not whitelisted we remove
                if (costs.containsKey(a) || craftableItems.contains(a)) {
                    atLeastOneCorrect = true;
                    break;
                }
            }
            //if an entire ing group is made up of invalid primitives then entire recipe is invalid
            if (!atLeastOneCorrect) return false;
        }
        return true;
    }

    private void uncraftAll() {
        //magic
        for (var item : recipesByResult.keySet()) {
            incompleteCosts.clear();
            Map<WoodType, Double> cost = uncraft(item, 0);

            if (!cost.isEmpty()) costs.put(item, cost);
        }
        costs.values().removeIf(Map::isEmpty);
    }


    private Map<WoodType, Double> uncraft(Item item, int depth) {
        var cached = costs.get(item);
        if (cached != null) return cached;
        var incomplete = incompleteCosts.get(item);
        if (incomplete != null) {
            giveUps++;
            return incomplete;
        }
        //if its not made out of pure wood by now it probably never will be
        if (depth >= 10 || !inProgress.add(item)) {
            giveUps++;
            return Map.of();
        }
        int giveUpsBefore = giveUps;
        Map<WoodType, Double> best = new HashMap<>();

        //try to uncraft looping through all its recipes
        outer:
        for (var recipe : recipesByResult.get(item)) {
            Map<WoodType, Double> recipeCost = new HashMap<>();
            for (var ingredient : recipe.placementInfo().ingredients()) {
                if (ingredient.isEmpty()) continue;

                //This is an EITHER. Each entry are equivalent to create this specific ingredient
                Map<WoodType, Double> ingredientCost = new HashMap<>();
                for (Holder<Item> alternative : ingredient.items().toList()) {
                    uncraft(alternative.value(), depth + 1).forEach((w, c) -> ingredientCost.merge(w, c, Double::min));
                }
                //this ingredient had no log cost for any of its items. this means its invalid.
                //this also means the entire recipe is invalid
                if (ingredientCost.isEmpty()) continue outer;

                if (recipeCost.isEmpty()) {
                    recipeCost.putAll(ingredientCost);
                } else {
                    //remove non common ones
                    recipeCost.keySet().retainAll(ingredientCost.keySet());
                    if (recipeCost.isEmpty()) {
                        continue outer;
                    }
                    recipeCost.replaceAll((w, c) -> c + ingredientCost.get(w));
                }
            }

            int outputCount = context.resultOf(recipe).getCount();
            if (outputCount <= 0) continue;
            recipeCost.forEach((w, c) -> best.merge(w, c / outputCount, Double::min));
        }
        inProgress.remove(item);
        // something below got cut by a cycle or the depth limit so this is only good for this path. dont remember it
        if (giveUps == giveUpsBefore) costs.put(item, best);
        else incompleteCosts.put(item, best);
        return best;
    }

    public static List<Item> getAllChildren(WoodType type, String... keys) {
        List<Item> children = new ArrayList<>();
        for (var k : keys) {
            Item child = type.getItemOfThis(k);
            if (child != null) children.add(child);
        }
        return children;
    }
}
