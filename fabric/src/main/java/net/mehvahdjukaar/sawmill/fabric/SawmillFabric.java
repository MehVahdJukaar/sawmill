package net.mehvahdjukaar.sawmill.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.fabric.RegHelperImpl;
import net.mehvahdjukaar.sawmill.Sawmill;

public class SawmillFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Sawmill.init();
    }

}
