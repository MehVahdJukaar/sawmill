package net.mehvahdjukaar.sawmill.mixins;

import net.mehvahdjukaar.sawmill.RecipeManagerHook;
import net.mehvahdjukaar.sawmill.SawmillRecipeGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin implements RecipeManagerHook {

    @Shadow
    private RecipeMap recipes;

    @Override
    public void sawmill$addGeneratedRecipes(HolderLookup.Provider registries) {
        this.recipes = SawmillRecipeGenerator.onRecipesLoaded(this.recipes, registries);
    }
}
