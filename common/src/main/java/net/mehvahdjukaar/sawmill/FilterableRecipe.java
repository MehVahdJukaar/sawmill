package net.mehvahdjukaar.sawmill;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public record FilterableRecipe(RecipeHolder<WoodcuttingRecipe> recipe, ResourceLocation id) {

    public static FilterableRecipe of(RecipeHolder<WoodcuttingRecipe> recipe) {
        return new FilterableRecipe(recipe,
                Utils.getID(recipe.value().getResultItem(RegistryAccess.EMPTY).getItem()));
    }

    public boolean matchFilter(String filter) {
        return id.getPath().contains(filter);
    }

    public WoodcuttingRecipe getRecipe() {
        return recipe.value();
    }
}
