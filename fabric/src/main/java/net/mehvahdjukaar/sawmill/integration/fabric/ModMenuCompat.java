package net.mehvahdjukaar.sawmill.integration.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.fabric.mixin.recipe.ingredient.IngredientMixin;
import net.mehvahdjukaar.sawmill.CommonConfigs;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return CommonConfigs.SPEC::makeScreen;
    }
}