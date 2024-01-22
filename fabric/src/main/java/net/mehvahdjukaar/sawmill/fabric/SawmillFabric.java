package net.mehvahdjukaar.sawmill.fabric;

import net.fabricmc.api.ModInitializer;
import net.mehvahdjukaar.sawmill.SawmillMod;

public class SawmillFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        SawmillMod.init();
    }

}
