package net.mehvahdjukaar.sawmill;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RecipeSorter {

    private static final List<Item> ITEM_ORDER = new ArrayList<>();
    private static final List<Item> UNSORTED = new ArrayList<>();


    //called from server side by recipe stuff.
    public static void accept(List<WoodcuttingRecipe> sawmillRecipes) {
        sawmillRecipes.forEach(r -> UNSORTED.add(r.getResultItem(RegistryAccess.EMPTY).getItem()));
    }

    public static void acceptOrder(IntList list) {
        UNSORTED.clear();
        list.forEach(i -> ITEM_ORDER.add(BuiltInRegistries.ITEM.byId(i)));
    }

    // dont think we can repopulate offthread
    public static void refreshIfNeeded(RegistryAccess reg) {
        if (UNSORTED.isEmpty()) return;
        if (!CreativeModeTabs.getDefaultTab().hasAnyItems()) {
            CreativeModeTabs.tryRebuildTabContents(FeatureFlags.VANILLA_SET, false, reg);
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
        if (CommonConfigs.SORT_RECIPES.get()) {
            //Just runs once if needed. Needs to be the same from server and client
            refreshIfNeeded(level.registryAccess());

            recipes.sort(Comparator.comparingInt(value ->
                    ITEM_ORDER.indexOf(value.getResultItem(RegistryAccess.EMPTY).getItem())));
        }
    }

    public static void sendOrderToClient(@Nullable ServerPlayer player) {
        IntList list = new IntArrayList();
        ITEM_ORDER.forEach(i -> list.add(BuiltInRegistries.ITEM.getId(i)));
        NetworkStuff.SyncRecipeOrder message = new NetworkStuff.SyncRecipeOrder(list);
        if (player != null) {
            NetworkStuff.CHANNEL.sendToClientPlayer(player, message);
        } else {
            NetworkStuff.CHANNEL.sendToAllClientPlayers(message);
        }
    }
}
