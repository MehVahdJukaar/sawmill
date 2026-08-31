package net.mehvahdjukaar.sawmill;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;

public class SawmillClient {

    public static void init() {
        ClientHelper.addMenuScreensRegistration(event ->
                event.register(SawmillMod.SAWMILL_MENU.get(), SawmillScreen::new));
    }

    public static boolean hasManyRecipes() {
        return WoodcuttingRecipes.all().size() > CommonConfigs.SEARCH_BAR_THRESHOLD.get();
    }
}
