package net.mehvahdjukaar.sawmill.forge;

import net.mehvahdjukaar.sawmill.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Author: MehVahdJukaar
 */
@Mod(SawmillMod.MOD_ID)
public class SawmillModImpl {

    public SawmillModImpl() {
        SawmillMod.init();
        MinecraftForge.EVENT_BUS.register(this);
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
        return ing.isVanilla();
    }

}
