package net.mehvahdjukaar.sawmill.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.mehvahdjukaar.sawmill.RecipeSorter;
import net.mehvahdjukaar.sawmill.SawmillClient;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.VillageStructureModifier;

public class SawmillFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        SawmillMod.init();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> VillageStructureModifier.setup(server.registryAccess()));
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (client) SawmillClient.onTagsUpdated();
        });
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((p, manager) -> RecipeSorter.sendOrderToClient(p));
    }

}
