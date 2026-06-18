package net.mehvahdjukaar.sawmill.platform;

import net.mehvahdjukaar.sawmill.RecipeSorter;
import net.mehvahdjukaar.sawmill.SawmillClient;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.VillageStructureModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.function.Supplier;

/**
 * Author: MehVahdJukaar
 */
@Mod(SawmillMod.MOD_ID)
public class SawmillModImpl {

    public SawmillModImpl(IEventBus bus) {
        SawmillMod.init();
        NeoForge.EVENT_BUS.register(this);
    }

    public static Supplier<ItemStack> getRecipeCategoryDefaultItem() {
        return () -> new ItemStack(Items.OAK_PLANKS);
    }

    @SubscribeEvent
    public void onServerStart(ServerAboutToStartEvent event) {
        VillageStructureModifier.setup(event.getServer().registryAccess());
    }

    @SubscribeEvent
    public void onTagReload(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
            SawmillClient.onTagsUpdated();
        } else {
        }
    }

    @SubscribeEvent
    public void dataSync(OnDatapackSyncEvent event) {
        RecipeSorter.sendOrderToClient(event.getPlayer());
    }

    public static boolean isVanillaIngredient(Ingredient ing) {
        return !ing.isCustom();
    }

    public static Object getCustomIngredient(Ingredient ing) {
        return ing.getCustomIngredient();
    }

    public static java.util.List<Ingredient> decomposeCustomIngredient(Ingredient ing) {
        ICustomIngredient custom = ing.getCustomIngredient();
        // Return the inner ingredients only; the caller resolves them safely. We never call
        // getItems()/test() here, so no nested lazy cache gets poisoned.
        if (custom instanceof CompoundIngredient compound) {
            return compound.children();
        }
        if (custom instanceof IntersectionIngredient intersection) {
            // Over-approximate AND as the union of its children (fine for a cost heuristic).
            return intersection.children();
        }
        if (custom instanceof DifferenceIngredient difference) {
            // base minus subtracted: approximate with base. Including the subtracted items
            // would only ever make a recipe look cheaper, never break it.
            return java.util.List.of(difference.base());
        }
        return java.util.List.of();
    }

}
