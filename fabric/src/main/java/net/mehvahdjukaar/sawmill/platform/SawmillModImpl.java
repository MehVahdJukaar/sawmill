package net.mehvahdjukaar.sawmill.platform;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.mehvahdjukaar.sawmill.NetworkStuff;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.VillageStructureModifier;
import net.mehvahdjukaar.sawmill.WoodcuttingRecipes;

public class SawmillModImpl implements ModInitializer {

    @Override
    public void onInitialize() {
        SawmillMod.init();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> VillageStructureModifier.setup(server.registryAccess()));
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            WoodcuttingRecipes.sortIfNeeded(player.level().registryAccess());
            NetworkStuff.sendRecipesToClient(player);
        });
    }
}
