package net.mehvahdjukaar.sawmill.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.VillageStructureModifier;

public class SawmillFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        SawmillMod.init();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> VillageStructureModifier.setup(server.registryAccess()));
    }

}
