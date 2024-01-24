package net.mehvahdjukaar.sawmill.forge;

import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.VillageStructureModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Author: MehVahdJukaar
 */
@Mod(SawmillMod.MOD_ID)
public class SawmillForge {

    public SawmillForge() {
        SawmillMod.init();
        MinecraftForge.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void onServerStart(ServerAboutToStartEvent event){
        VillageStructureModifier.setup(event.getServer().registryAccess());
    }


}
