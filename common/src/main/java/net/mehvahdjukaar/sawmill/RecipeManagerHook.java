package net.mehvahdjukaar.sawmill;

import net.minecraft.core.HolderLookup;

public interface RecipeManagerHook {

    void sawmill$addGeneratedRecipes(HolderLookup.Provider registries);
}
