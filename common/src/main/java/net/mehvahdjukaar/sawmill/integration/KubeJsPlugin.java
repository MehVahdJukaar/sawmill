package net.mehvahdjukaar.sawmill.integration;

import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.SawmillRecipeGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Map;


public class KubeJsPlugin implements KubeJSPlugin {

    /*
    RecipeKey<OutputItem> RESULT = ItemComponents.OUTPUT_ID_WITH_COUNT.key("result");
    RecipeKey<InputItem> INGREDIENT = ItemComponents.INPUT.key("ingredient");


    RecipeSchema WOODCUTTING_SCHEMA = new RecipeSchema(RESULT, INGREDIENT) {

        @Override
        public int inputCount() {
            return super.inputCount();
        }
    }.uniqueOutputId(RESULT);
*/

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(SawmillMod.res("woodcutting"), WOODCUTTING_SCHEMA);
    }

    @Override
    public void injectRuntimeRecipes(RecipesKubeEvent event, RecipeManagerKJS manager,
                                     Map<ResourceLocation, RecipeHolder<?>> recipesByName) {
        var newRecipes = SawmillRecipeGenerator.process(recipesByName.values());
        newRecipes.forEach(r -> recipesByName.put(r.id(), r));
    }

}