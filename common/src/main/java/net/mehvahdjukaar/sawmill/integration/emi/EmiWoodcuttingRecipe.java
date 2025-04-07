package net.mehvahdjukaar.sawmill.integration.emi;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.WoodcuttingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class EmiWoodcuttingRecipe implements EmiRecipe {
    private final ResourceLocation id;
    private final EmiIngredient input;
    private final List<EmiIngredient> inputs;
    private final EmiStack output;
    private final int inputCount;

    public EmiWoodcuttingRecipe(WoodcuttingRecipe recipe) {
        this.id = EmiPort.getId(recipe);
        this.input = EmiIngredient.of(recipe.getIngredients().get(0));
        this.output = EmiStack.of(EmiPort.getOutput(recipe));
        this.inputs = new ArrayList<>();
        this.inputCount = recipe.getInputCount();
        if (inputCount > 100) {
            throw new IllegalArgumentException("Input count for wood cutting recipe is too high: " + inputCount + ". Ingredient: " + input + ", Result: " + output);
        }
        for (int i = 0; i < inputCount; i++) {
            this.inputs.add(input);
        }

    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EMIPlugin.WOODCUTTING_CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return this.inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(this.output);
    }

    @Override
    public int getDisplayWidth() {
        return 76;
    }

    @Override
    public int getDisplayHeight() {
        return 18;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 26, 1);
        widgets.addSlot(this.input, 0, 0);
        widgets.add(new WidgetWithNumber(0, 0, this.inputCount));
        widgets.addSlot(this.output, 58, 0).recipeContext(this);
    }

    public static class WidgetWithNumber extends Widget {
        private final Bounds bounds;
        private final int number;

        public WidgetWithNumber(int x, int y, int number) {
            this.bounds = new Bounds(x, y, 18, 18);
            this.number = number;
        }

        @Override
        public Bounds getBounds() {
            return bounds;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int i, int i1, float v) {
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font,
                    new ItemStack(Items.DIRT, number), 0, 0);
        }
    }
}
