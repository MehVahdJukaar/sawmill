package net.mehvahdjukaar.sawmill;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RecipeSorter {

    private static final List<Item> ITEM_ORDER = new ArrayList<>();
    private static final Set<Item> UNSORTED = new HashSet<>();


    //called from server side by recipe stuff.
    public static void accept(List<WoodcuttingRecipe> sawmillRecipes) {
        UNSORTED.clear();
        sawmillRecipes.forEach(r -> UNSORTED.add(r.getResultItem(RegistryAccess.EMPTY).getItem()));
    }

    public static void acceptOrder(IntList list) {
        UNSORTED.clear();
        ITEM_ORDER.clear();
        list.forEach(i -> ITEM_ORDER.add(BuiltInRegistries.ITEM.byId(i)));
    }

    // don't think we can repopulate off-thread
    public static void refreshIfNeeded(RegistryAccess reg) {
        if (UNSORTED.isEmpty()) return;
        ITEM_ORDER.clear();
        if (!CreativeModeTabs.getDefaultTab().hasAnyItems()) {
            // this is NOT a client only method. Calling on server thread is valid.
            CreativeModeTabs.tryRebuildTabContents(FeatureFlags.VANILLA_SET, false, reg);
        }
        for (var t : CreativeModeTabs.tabs()) {
            List<Item> found = new ArrayList<>();
            var list = t.getDisplayItems().stream().map(ItemStack::getItem).toList();
            for (Item tabItem : list) {
                if (UNSORTED.contains(tabItem)) {
                    // if the item is in the tab, we can use its index to sort it
                    found.add(tabItem);
                    UNSORTED.remove(tabItem);
                }
            }
            ITEM_ORDER.addAll(found);
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
        refreshIfNeeded(Utils.hackyGetRegistryAccess());
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
