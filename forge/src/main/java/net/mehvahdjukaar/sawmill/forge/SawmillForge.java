package net.mehvahdjukaar.sawmill.forge;

import net.mehvahdjukaar.moonlight.core.mixins.forge.ConditionHackMixin;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.VillageStructureModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/**
 * Author: MehVahdJukaar
 */
@Mod(SawmillMod.MOD_ID)
public class SawmillForge {

    public SawmillForge() {
        SawmillMod.init();
        NeoForge.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void onServerStart(ServerAboutToStartEvent event){
        VillageStructureModifier.setup(event.getServer().registryAccess());
    }


}
