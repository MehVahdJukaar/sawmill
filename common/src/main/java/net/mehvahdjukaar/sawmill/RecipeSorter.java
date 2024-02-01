package net.mehvahdjukaar.sawmill;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;

public class RecipeSorter {

    private static final List<Item> ITEM_ORDER = new ArrayList<>();
    private static final List<Item> UNSORTED = new ArrayList<>();
    private static boolean needsRefresh = true;

    public static void accept(List<WoodcuttingRecipe> sawmillRecipes) {
        if (needsRefresh) {
            needsRefresh = false;

            sawmillRecipes.forEach(r -> UNSORTED.add(r.getResultItem(RegistryAccess.EMPTY).getItem()));
        }
    }

    // dont think we can repopulate offthread
    private static void refreshIfNeeded(Level level) {
        if (UNSORTED.isEmpty()) return;
        if (!CreativeModeTabs.getDefaultTab().hasAnyItems()) {
            CreativeModeTabs.tryRebuildTabContents(level.enabledFeatures(), false, level.registryAccess());
        }
        Map<CreativeModeTab, List<Item>> tabContent = new HashMap<>();

        for (var t : CreativeModeTabs.tabs()) {
            List<Pair<Item, Integer>> weights = new ArrayList<>();
            var list = tabContent.computeIfAbsent(t,
                    creativeModeTabs -> t.getDisplayItems().stream().map(ItemStack::getItem).toList());
            var iterator = UNSORTED.iterator();
            while (iterator.hasNext()) {
                var i = iterator.next();
                int index = list.indexOf(i);
                if (index != -1) {
                    weights.add(Pair.of(i, index));
                    iterator.remove();
                }
            }
            weights.sort(Comparator.comparingInt(Pair::getSecond));
            ITEM_ORDER.addAll(weights.stream().map(Pair::getFirst).toList());
        }

        UNSORTED.clear();
    }


    public static void sort(List<WoodcuttingRecipe> recipes, Level level) {
        //just runs once if needed
        refreshIfNeeded(level);

        recipes.sort(Comparator.comparingInt(value ->
                ITEM_ORDER.indexOf(value.getResultItem(RegistryAccess.EMPTY).getItem())));

    }
}
