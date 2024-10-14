package net.mehvahdjukaar.sawmill.neoforge;

import net.mehvahdjukaar.moonlight.api.platform.neoforge.RegHelperImpl;
import net.mehvahdjukaar.sawmill.RecipeSorter;
import net.mehvahdjukaar.sawmill.SawmillClient;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.VillageStructureModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
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
        RegHelperImpl.startRegisteringFor(bus);
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

}
