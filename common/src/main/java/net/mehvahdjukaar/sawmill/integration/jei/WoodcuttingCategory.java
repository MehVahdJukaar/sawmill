package net.mehvahdjukaar.sawmill.integration.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.WoodcuttingEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class WoodcuttingCategory extends AbstractRecipeCategory<WoodcuttingEntry> {

    public WoodcuttingCategory(IGuiHelper guiHelper) {
        super(JEIPlugin.WOODCUTTING_RECIPE_TYPE,
                Component.translatable("sawmill.category.wood_cutting"),
                guiHelper.createDrawableItemLike(SawmillMod.SAWMILL_BLOCK.get()), 82, 34);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WoodcuttingEntry recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 9)
                .setStandardSlotBackground()
                .addIngredients(recipe.input());
        builder.addOutputSlot(61, 9)
                .setOutputSlotBackground()
                .addItemStack(recipe.result());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, WoodcuttingEntry recipe, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);
        builder.addRecipeArrow().setPosition(26, 9);
    }

    @Override
    public void draw(WoodcuttingEntry recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics,
                     double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);
        graphics.itemDecorations(Minecraft.getInstance().font,
                new ItemStack(Items.DIRT, recipe.inputCount()), 1, 9);
    }
}
