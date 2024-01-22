package net.mehvahdjukaar.sawmill.integration;


import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.SawmillRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    public static final RecipeType<SawmillRecipe> SAWMILL_RECIPE_TYPE = RecipeType.create(SawmillMod.MOD_ID, "sawmill", SawmillRecipe.class);

    private static final ResourceLocation ID = SawmillMod.res("jei_plugin");

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new SawmillRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var recipeManager = Minecraft.getInstance().level.getRecipeManager();
        var list = recipeManager.getAllRecipesFor(SawmillMod.SAWMILL_RECIPE.get()).stream().toList();
        registration.addRecipes(SAWMILL_RECIPE_TYPE, list);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(SawmillMod.SAWMILL_BLOCK.get()), SAWMILL_RECIPE_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        //registration.addRecipeClickArea(CookingPotScreen.class, 89, 25, 24, 17, FDRecipeTypes.COOKING);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        //registration.addRecipeTransferHandler(CookingPotMenu.class, ModMenuTypes.COOKING_POT.get(),
        //        FDRecipeTypes.COOKING, 0, 6, 9, 36);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }
}
