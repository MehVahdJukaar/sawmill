package net.mehvahdjukaar.sawmill.integration.jei;


import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.sawmill.SawmillMenu;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.WoodcuttingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    private static final boolean EMI = PlatHelper.isModLoaded("emi");
    private static final boolean REI = PlatHelper.isModLoaded("roughlyenoughitems");

    public static final RecipeType<WoodcuttingRecipe> WOODCUTTING_RECIPE_TYPE = RecipeType.create(SawmillMod.MOD_ID, "woodcutting", WoodcuttingRecipe.class);

    private static final ResourceLocation ID = SawmillMod.res("jei_plugin");

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        if (EMI || REI) return;
        registry.addRecipeCategories(new WoodcuttingCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (EMI || REI) return;
        var recipeManager = Minecraft.getInstance().level.getRecipeManager();
        var list = recipeManager.getAllRecipesFor(SawmillMod.WOODCUTTING_RECIPE.get()).stream()
                .map(RecipeHolder::value).toList();
        registration.addRecipes(WOODCUTTING_RECIPE_TYPE, list);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (EMI || REI) return;
        registration.addRecipeCatalyst(new ItemStack(SawmillMod.SAWMILL_BLOCK.get()), WOODCUTTING_RECIPE_TYPE);
    }


    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        if (EMI || REI) return;
        //registration.addRecipeTransferHandler(SawmillMenu.class, SawmillMod.SAWMILL_MENU.get(),
        //        WOODCUTTING_RECIPE_TYPE, 0, 1, 9, 36);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }
}
