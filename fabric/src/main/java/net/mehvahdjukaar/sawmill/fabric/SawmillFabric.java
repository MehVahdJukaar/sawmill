package net.mehvahdjukaar.sawmill.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.fabric.RegHelperImpl;
import net.mehvahdjukaar.sawmill.Sawmill;

public class SawmillFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Sawmill.init();
    }

}
