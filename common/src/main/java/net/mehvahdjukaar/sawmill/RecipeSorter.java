package net.mehvahdjukaar.sawmill;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.trades.ItemListingManager;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RecipeSorter {

    private static final List<Item> ITEM_ORDER = new ArrayList<>();
    private static final List<Item> UNSORTED = new ArrayList<>();


    //called from server side by recipe stuff.
    public static void accept(List<RecipeHolder<WoodcuttingRecipe>> sawmillRecipes) {
        UNSORTED.clear();
        sawmillRecipes.forEach(r -> UNSORTED.add(r.value().getResultItem(RegistryAccess.EMPTY).getItem()));
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


    public static void sort(List<RecipeHolder<WoodcuttingRecipe>> recipes, Level level) {
        if (CommonConfigs.SORT_RECIPES.get()) {
            //Just runs once if needed. Needs to be the same from server and client
            refreshIfNeeded(level.registryAccess());

            recipes.sort(Comparator.comparingInt(r ->
                    ITEM_ORDER.indexOf(r.value().getResultItem(RegistryAccess.EMPTY).getItem())));
        }
    }

    public static void sendOrderToClient(@Nullable ServerPlayer player) {
        refreshIfNeeded(Utils.hackyGetRegistryAccess());
        IntList list = new IntArrayList();
        ITEM_ORDER.forEach(i -> list.add(BuiltInRegistries.ITEM.getId(i)));
        NetworkStuff.SyncRecipeOrder message = new NetworkStuff.SyncRecipeOrder(list);
        if (player != null) {
            NetworkHelper.sendToClientPlayer(player, message);
        } else {
            NetworkHelper.sendToAllClientPlayers(message);
        }
    }
}
