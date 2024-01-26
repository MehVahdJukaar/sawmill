package net.mehvahdjukaar.sawmill;

import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;

public class SawmillClient {

    public static void init() {
        ClientPlatformHelper.addClientSetup(SawmillClient::setup);
    }

    private static void setup() {
        ClientPlatformHelper.registerRenderType(SawmillMod.SAWMILL_BLOCK.get(), RenderType.cutout());
        MenuScreens.register(SawmillMod.SAWMILL_MENU.get(), SawmillScreen::new);
    }
}
