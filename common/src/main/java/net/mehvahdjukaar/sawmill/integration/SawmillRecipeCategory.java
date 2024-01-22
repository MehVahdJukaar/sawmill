package net.mehvahdjukaar.sawmill.integration;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Constants;
import mezz.jei.library.util.RecipeUtil;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.SawmillRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SawmillRecipeCategory implements IRecipeCategory<SawmillRecipe> {
    public static final int width = 82;
    public static final int height = 34;
    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public SawmillRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = Constants.RECIPE_GUI_VANILLA;
        this.background = guiHelper.createDrawable(location, 0, 220, 82, 34);
        this.icon = guiHelper.createDrawableItemStack(SawmillMod.SAWMILL_BLOCK.get().asItem().getDefaultInstance());
        this.localizedName = Component.translatable("jei.sawmill.category.sawmill");
    }

    @Override
    public RecipeType<SawmillRecipe> getRecipeType() {
        return JEIPlugin.SAWMILL_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return this.localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SawmillRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 9)
                .addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 9).addItemStack(RecipeUtil.getResultItem(recipe));
    }

    @Override
    public void draw(SawmillRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);

        guiGraphics.renderItemDecorations(Minecraft.getInstance().font,
                new ItemStack(Items.DIRT, recipe.getInputCount()), 1,9);
    }
}
