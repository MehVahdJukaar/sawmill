package net.mehvahdjukaar.sawmill;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecipeSorter {

    public static List<WoodcuttingEntry> sorted(List<WoodcuttingEntry> entries, RegistryAccess registryAccess) {
        if (!CommonConfigs.SORT_RECIPES.get()) return entries;

        Set<Item> wanted = new HashSet<>();
        entries.forEach(e -> wanted.add(e.result().getItem()));

        Map<Item, Integer> orderByItem = buildTabOrder(wanted, registryAccess);
        List<WoodcuttingEntry> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparingInt(e -> orderByItem.getOrDefault(e.result().getItem(), -1)));
        return sorted;
    }

    private static Map<Item, Integer> buildTabOrder(Set<Item> wanted, RegistryAccess registryAccess) {
        if (!CreativeModeTabs.getDefaultTab().hasAnyItems()) {
            // this is NOT a client only method. Calling on server thread is valid.
            CreativeModeTabs.tryRebuildTabContents(FeatureFlags.VANILLA_SET, false, registryAccess);
        }
        Map<Item, Integer> order = new HashMap<>();
        for (var tab : CreativeModeTabs.tabs()) {
            for (ItemStack stack : tab.getDisplayItems()) {
                Item item = stack.getItem();
                if (wanted.remove(item)) {
                    order.put(item, order.size());
                }
            }
        }
        return order;
    }
}
