package net.mehvahdjukaar.sawmill.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.WoodcuttingEntry;
import net.mehvahdjukaar.sawmill.WoodcuttingRecipes;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    private static final boolean REI = PlatHelper.isModLoaded("roughlyenoughitems");

    public static final IRecipeType<WoodcuttingEntry> WOODCUTTING_RECIPE_TYPE =
            IRecipeType.create(SawmillMod.MOD_ID, "woodcutting", WoodcuttingEntry.class);

    private static final Identifier ID = SawmillMod.res("jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        if (REI) return;
        registry.addRecipeCategories(new WoodcuttingCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (REI) return;
        registration.addRecipes(WOODCUTTING_RECIPE_TYPE, WoodcuttingRecipes.all());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (REI) return;
        registration.addCraftingStation(WOODCUTTING_RECIPE_TYPE, SawmillMod.SAWMILL_BLOCK.get());
    }
}
