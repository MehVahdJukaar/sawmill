package net.mehvahdjukaar.sawmill.integration;

import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import net.mehvahdjukaar.sawmill.SawmillRecipeGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Map;


public class KubeJsPlugin implements KubeJSPlugin {


    @Override
    public void injectRuntimeRecipes(RecipesKubeEvent event, RecipeManagerKJS manager,
                                     Map<ResourceLocation, RecipeHolder<?>> recipesByName) {
        var newRecipes = SawmillRecipeGenerator.process(recipesByName.values());
        newRecipes.forEach(r -> recipesByName.put(r.id(), r));
    }

}