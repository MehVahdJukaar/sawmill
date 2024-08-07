package net.mehvahdjukaar.sawmill.neoforge;

import net.mehvahdjukaar.sawmill.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/**
 * Author: MehVahdJukaar
 */
@Mod(SawmillMod.MOD_ID)
public class SawmillModImpl {

    public SawmillModImpl() {
        SawmillMod.init();
        NeoForge.EVENT_BUS.register(this);
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
