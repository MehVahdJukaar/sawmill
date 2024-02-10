package net.mehvahdjukaar.sawmill;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

public record FilterableRecipe(WoodcuttingRecipe recipe, ResourceLocation id) {

    public static FilterableRecipe of(WoodcuttingRecipe recipe) {
        return new FilterableRecipe(recipe, Utils.getID(recipe.getResultItem().getItem()));
    }

    public boolean matchFilter(String filter) {
        return id.getPath().contains(filter);
    }
}
