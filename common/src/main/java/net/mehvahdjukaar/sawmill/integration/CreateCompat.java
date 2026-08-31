package net.mehvahdjukaar.sawmill.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.sawmill.CommonConfigs;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.WoodcuttingRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;

public class CreateCompat {

    private static final Identifier CUTTING = Identifier.fromNamespaceAndPath("create", "cutting");
    private static final int PROCESSING_TIME = 50;
    private static final int MAX_OUTPUT_COUNT = 99;

    public static List<RecipeHolder<?>> makeCuttingRecipes(List<RecipeHolder<WoodcuttingRecipe>> woodcutting,
                                                           HolderLookup.Provider registries) {
        if (!CommonConfigs.CREATE_SAW_COMPAT.get() || !PlatHelper.isModLoaded("create")) return List.of();

        RecipeSerializer<?> serializer = BuiltInRegistries.RECIPE_SERIALIZER.getValue(CUTTING);
        if (serializer == null) {
            SawmillMod.LOGGER.warn("Create is installed but has no {} serializer. Skipping mechanical saw compat", CUTTING);
            return List.of();
        }
        Codec<Recipe<?>> codec = recipeCodec(serializer);
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registries);

        List<RecipeHolder<?>> added = new ArrayList<>();
        for (var holder : woodcutting) {
            WoodcuttingRecipe recipe = holder.value();
            if (recipe.getInputCount() != 1) continue;
            ItemStack result = recipe.resultTemplate().create();
            if (result.isEmpty() || result.getCount() > MAX_OUTPUT_COUNT) continue;

            JsonElement inputJson = Ingredient.CODEC.encodeStart(ops, recipe.input()).result().orElse(null);
            if (inputJson == null) continue;

            JsonObject json = new JsonObject();
            json.add("ingredients", singleton(inputJson));
            json.add("results", singleton(encodeOutput(result)));
            json.addProperty("processing_time", PROCESSING_TIME);

            Recipe<?> cutting = codec.parse(ops, json).result().orElse(null);
            if (cutting == null) {
                SawmillMod.LOGGER.warn("Create didn't accept cutting recipe json {}. Skipping mechanical saw compat", json);
                return List.of();
            }

            Identifier sourceId = holder.id().identifier();
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE,
                    SawmillMod.res("create_cutting/" + sourceId.getNamespace() + "/" + sourceId.getPath()));
            added.add(new RecipeHolder<>(key, cutting));
        }
        SawmillMod.LOGGER.info("Added {} Create cutting recipes for the Mechanical Saw", added.size());
        return added;
    }

    private static JsonObject encodeOutput(ItemStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        json.addProperty("count", stack.getCount());
        return json;
    }

    private static JsonArray singleton(JsonElement element) {
        JsonArray array = new JsonArray();
        array.add(element);
        return array;
    }

    @SuppressWarnings("unchecked")
    private static Codec<Recipe<?>> recipeCodec(RecipeSerializer<?> serializer) {
        return (Codec<Recipe<?>>) (Codec<?>) serializer.codec().codec();
    }
}
