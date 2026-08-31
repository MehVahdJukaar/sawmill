package net.mehvahdjukaar.sawmill;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WoodcuttingRecipes {

    private static List<WoodcuttingEntry> entries = List.of();
    private static boolean needsSorting = false;

    public static void setUnsorted(List<WoodcuttingEntry> newEntries) {
        entries = List.copyOf(newEntries);
        needsSorting = true;
    }

    public static void sortIfNeeded(RegistryAccess registryAccess) {
        if (!needsSorting) return;
        needsSorting = false;
        entries = RecipeSorter.sorted(entries, registryAccess);
    }

    public static void set(List<WoodcuttingEntry> newEntries) {
        entries = List.copyOf(newEntries);
        needsSorting = false;
    }

    public static void append(List<WoodcuttingEntry> more) {
        List<WoodcuttingEntry> merged = new ArrayList<>(entries);
        merged.addAll(more);
        entries = List.copyOf(merged);
    }

    public static List<WoodcuttingEntry> all() {
        return entries;
    }

    public static List<WoodcuttingEntry> selectByInput(ItemStack input) {
        return entries.stream().filter(e -> e.matches(input)).toList();
    }

    public static boolean acceptsInput(ItemStack input) {
        return entries.stream().anyMatch(e -> e.matches(input));
    }
}
