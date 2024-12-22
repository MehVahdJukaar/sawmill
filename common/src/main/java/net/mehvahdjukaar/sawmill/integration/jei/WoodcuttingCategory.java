package net.mehvahdjukaar.sawmill.integration.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Constants;
import mezz.jei.library.plugins.vanilla.stonecutting.StoneCuttingRecipeCategory;
import mezz.jei.library.util.RecipeUtil;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.WoodcuttingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class WoodcuttingCategory extends AbstractRecipeCategory<WoodcuttingRecipe> {

    public WoodcuttingCategory(IGuiHelper guiHelper) {
        super(JEIPlugin.WOODCUTTING_RECIPE_TYPE,
                Component.translatable("sawmill.category.wood_cutting"),
                guiHelper.createDrawableItemLike(SawmillMod.SAWMILL_BLOCK.get()), 82, 34);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WoodcuttingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 9)
                .addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 9).addItemStack(RecipeUtil.getResultItem(recipe));
    }

    @Override
    public void draw(WoodcuttingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);

        guiGraphics.renderItemDecorations(Minecraft.getInstance().font,
                new ItemStack(Items.DIRT, recipe.getInputCount()), 1,9);
    }
}
