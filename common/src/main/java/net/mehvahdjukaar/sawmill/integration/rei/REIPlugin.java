package net.mehvahdjukaar.sawmill.integration.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.minecraft.client.Minecraft;

@REIPluginClient
public class REIPlugin implements REIClientPlugin {

    public static final CategoryIdentifier<WoodcuttingDisplay> WOODCUTTING_DISPLAY = CategoryIdentifier.of(SawmillMod.res("effects"));

    public REIPlugin() {
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new WoodcuttingCategory());
        registry.addWorkstations(WOODCUTTING_DISPLAY, EntryStacks.of(SawmillMod.SAWMILL_BLOCK.get()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        var recipeManager = Minecraft.getInstance().level.getRecipeManager();
        recipeManager.getAllRecipesFor(SawmillMod.WOODCUTTING_RECIPE.get())
                .forEach(r -> registry.add(new WoodcuttingDisplay(r)));
    }

}

