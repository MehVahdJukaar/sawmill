package net.mehvahdjukaar.sawmill;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RecipeSorter {

    private static final Object2IntMap<Item> ITEM_ORDER = new Object2IntLinkedOpenHashMap<>();
    private static final Set<Item> UNSORTED = new HashSet<>();


    //called from server side by recipe stuff.
    public static void accept(List<RecipeHolder<WoodcuttingRecipe>> sawmillRecipes) {
        UNSORTED.clear();
        sawmillRecipes.forEach(r -> UNSORTED.add(r.value().getResultItem(RegistryAccess.EMPTY).getItem()));
    }

    public static void acceptOrder(IntList list) {
        UNSORTED.clear();
        ITEM_ORDER.clear();
        list.forEach(i -> ITEM_ORDER.put(BuiltInRegistries.ITEM.byId(i), ITEM_ORDER.size()));
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
            for (ItemStack stack : t.getDisplayItems()) {
                Item tabItem = stack.getItem();
                // if the item is in the tab, we can use its index to sort it
                if (UNSORTED.remove(tabItem)) ITEM_ORDER.put(tabItem, ITEM_ORDER.size());
            }
        }

        UNSORTED.clear();
    }


    public static void sort(List<RecipeHolder<WoodcuttingRecipe>> recipes, Level level) {
        if (CommonConfigs.SORT_RECIPES.get()) {
            //Just runs once if needed. Needs to be the same from server and client
            refreshIfNeeded(level.registryAccess());

            //stuff in no creative tab goes last
            recipes.sort(Comparator.comparingInt(r ->
                    ITEM_ORDER.getOrDefault(r.value().getResultItem(RegistryAccess.EMPTY).getItem(), Integer.MAX_VALUE)));
        }
    }

    public static void sendOrderToClient(@Nullable ServerPlayer player) {
        refreshIfNeeded(Utils.hackyGetRegistryAccess());
        IntList list = new IntArrayList();
        ITEM_ORDER.keySet().forEach(i -> list.add(BuiltInRegistries.ITEM.getId(i)));
        NetworkStuff.SyncRecipeOrder message = new NetworkStuff.SyncRecipeOrder(list);
        if (player != null) {
            NetworkHelper.sendToClientPlayer(player, message);
        } else {
            NetworkHelper.sendToAllClientPlayers(message);
        }
    }
}
