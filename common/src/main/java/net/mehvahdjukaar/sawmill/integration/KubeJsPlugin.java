package net.mehvahdjukaar.sawmill.integration;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.SawmillRecipeGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Map;


public class KubeJsPlugin extends KubeJSPlugin {
    RecipeKey<OutputItem> RESULT = ItemComponents.OUTPUT_ID_WITH_COUNT.key("result");
    RecipeKey<InputItem> INGREDIENT = ItemComponents.INPUT.key("ingredient");


    RecipeSchema WOODCUTTING_SCHEMA = new RecipeSchema(RESULT, INGREDIENT){

        @Override
        public int inputCount() {
            return super.inputCount();
        }
    }.uniqueOutputId(RESULT);

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.register(SawmillMod.res("woodcutting"), WOODCUTTING_SCHEMA);
    }

    @Override
    public void injectRuntimeRecipes(RecipesEventJS event, RecipeManager manager, Map<ResourceLocation, Recipe<?>> recipesByName) {
        SawmillRecipeGenerator.process(recipesByName.values(), null, null, null);
    }

}