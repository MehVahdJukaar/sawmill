package net.mehvahdjukaar.sawmill.integration.emi;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeSorting;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiDrawContext;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

@EmiEntrypoint
public class EMIPlugin implements EmiPlugin {

    public static final EmiRecipeCategory WOODCUTTING_CATEGORY = new EmiRecipeCategory(
            SawmillMod.res("woodcutting"), EmiStack.of(SawmillMod.SAWMILL_BLOCK.get()),
            simplifiedRenderer(160, 240), EmiRecipeSorting.compareInputThenOutput()){
        @Override
        public Component getName() {
            return Component.translatable("sawmill.category.wood_cutting");
        }
    };

    private static EmiRenderable simplifiedRenderer(int u, int v) {
        return (raw, x, y, delta) -> {
            EmiDrawContext context = EmiDrawContext.wrap(raw);
            context.drawTexture(EmiRenderHelper.WIDGETS, x, y, u, v, 16, 16);
        };
    }

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(WOODCUTTING_CATEGORY);
        var recipeManager = Minecraft.getInstance().level.getRecipeManager();
        recipeManager.getAllRecipesFor(SawmillMod.WOODCUTTING_RECIPE.get())
                .forEach(r -> registry.addRecipe(new EmiWoodcuttingRecipe(r.value())));
    }

}
