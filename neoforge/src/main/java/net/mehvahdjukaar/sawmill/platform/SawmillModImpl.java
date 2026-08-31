package net.mehvahdjukaar.sawmill.platform;

import net.mehvahdjukaar.sawmill.NetworkStuff;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.VillageStructureModifier;
import net.mehvahdjukaar.sawmill.WoodcuttingRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/**
 * Author: MehVahdJukaar
 */
@Mod(SawmillMod.MOD_ID)
public class SawmillModImpl {

    public SawmillModImpl(IEventBus bus) {
        SawmillMod.init();
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStart(ServerAboutToStartEvent event) {
        VillageStructureModifier.setup(event.getServer().registryAccess());
    }

    @SubscribeEvent
    public void dataSync(OnDatapackSyncEvent event) {
        WoodcuttingRecipes.sortIfNeeded(event.getPlayerList().getServer().registryAccess());
        NetworkStuff.sendRecipesToClient(event.getPlayer());
    }
}
