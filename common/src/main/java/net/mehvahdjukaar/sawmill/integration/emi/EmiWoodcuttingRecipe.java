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
import dev.emi.emi.api.widget.WidgetHolder;
import net.mehvahdjukaar.sawmill.WoodcuttingRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class EmiWoodcuttingRecipe implements EmiRecipe {
    private final ResourceLocation id;
    private final EmiIngredient input;
    private final EmiStack output;

    public EmiWoodcuttingRecipe(WoodcuttingRecipe recipe) {
        this.id = EmiPort.getId(recipe);
        this.input = EmiIngredient.of(recipe.getIngredients().get(0));
        this.output = EmiStack.of(EmiPort.getOutput(recipe));
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
        return List.of(this.input);
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
        widgets.addSlot(this.output, 58, 0).recipeContext(this);
    }
}
