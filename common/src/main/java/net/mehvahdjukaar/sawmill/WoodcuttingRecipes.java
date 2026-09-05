package net.mehvahdjukaar.sawmill;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class WoodcuttingRecipes {

    private static List<WoodcuttingEntry> serverEntries = List.of();
    private static List<WoodcuttingEntry> clientEntries = List.of();
    private static boolean needsSorting = false;

    public static void setUnsorted(List<WoodcuttingEntry> newEntries) {
        serverEntries = List.copyOf(newEntries);
        needsSorting = true;
    }

    public static void sortIfNeeded(RegistryAccess registryAccess) {
        if (!needsSorting) return;
        needsSorting = false;
        serverEntries = RecipeSorter.sorted(serverEntries, registryAccess);
    }

    public static void setClient(List<WoodcuttingEntry> newEntries) {
        clientEntries = List.copyOf(newEntries);
    }

    public static void appendClient(List<WoodcuttingEntry> more) {
        List<WoodcuttingEntry> merged = new ArrayList<>(clientEntries);
        merged.addAll(more);
        clientEntries = List.copyOf(merged);
    }

    public static List<WoodcuttingEntry> onServer() {
        return serverEntries;
    }

    public static List<WoodcuttingEntry> onClient() {
        return clientEntries;
    }

    public static List<WoodcuttingEntry> forSide(Level level) {
        return level.isClientSide() ? clientEntries : serverEntries;
    }

    public static List<WoodcuttingEntry> selectByInput(Level level, ItemStack input) {
        return forSide(level).stream().filter(e -> e.matches(input)).toList();
    }

    public static boolean acceptsInput(Level level, ItemStack input) {
        return forSide(level).stream().anyMatch(e -> e.matches(input));
    }
}
