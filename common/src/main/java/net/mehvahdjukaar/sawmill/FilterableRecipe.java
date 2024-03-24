package net.mehvahdjukaar.sawmill;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;

public record FilterableRecipe(WoodcuttingRecipe recipe, ItemStack output) {

    public static FilterableRecipe of(WoodcuttingRecipe recipe) {
        return new FilterableRecipe(recipe, recipe.getResultItem(RegistryAccess.EMPTY));
    }

    public boolean matchFilter(String filter) {
        return output.getDisplayName().getString().contains(filter);
    }
}
