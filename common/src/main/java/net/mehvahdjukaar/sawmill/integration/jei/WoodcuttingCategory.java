package net.mehvahdjukaar.sawmill.integration.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.library.util.RecipeUtil;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.WoodcuttingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class WoodcuttingCategory extends AbstractRecipeCategory<WoodcuttingRecipe> {

    public WoodcuttingCategory(IGuiHelper guiHelper) {
        super(JEIPlugin.WOODCUTTING_RECIPE_TYPE,
                Component.translatable("sawmill.category.wood_cutting"),
                guiHelper.createDrawableItemLike(SawmillMod.SAWMILL_BLOCK.get()), 82, 34);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WoodcuttingRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 9)
                .setStandardSlotBackground()
                .addIngredients(recipe.getIngredients().get(0));
        builder.addOutputSlot(61, 9)
                .setOutputSlotBackground()
                .addItemStack(RecipeUtil.getResultItem(recipe));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, WoodcuttingRecipe recipe, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);
        builder.addRecipeArrow().setPosition(26, 9);
    }

    @Override
    public void draw(WoodcuttingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);

        guiGraphics.renderItemDecorations(Minecraft.getInstance().font,
                new ItemStack(Items.DIRT, recipe.getInputCount()), 1, 9);
    }
}
