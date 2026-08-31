package net.mehvahdjukaar.sawmill.integration;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
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
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreateCompat {

    private static final ResourceLocation CUTTING = ResourceLocation.fromNamespaceAndPath("create", "cutting");
    private static final int PROCESSING_TIME = 50;
    private static final int MAX_OUTPUT_COUNT = 99;

    public static void addCuttingRecipes(Collection<RecipeHolder<?>> allRecipes,
                                         List<RecipeHolder<WoodcuttingRecipe>> generated,
                                         HolderLookup.Provider registries,
                                         ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> byName,
                                         ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType) {
        if (!CommonConfigs.CREATE_SAW_COMPAT.get() || !PlatHelper.isModLoaded("create")) return;

        RecipeSerializer<?> serializer = BuiltInRegistries.RECIPE_SERIALIZER.get(CUTTING);
        if (serializer == null) {
            SawmillMod.LOGGER.warn("Create is installed but has no {} serializer. Skipping mechanical saw compat", CUTTING);
            return;
        }
        Codec<Recipe<?>> codec = recipeCodec(serializer);
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registries);

        // when the pack cache is fresh nothing gets generated, recipes come in already loaded instead
        List<RecipeHolder<WoodcuttingRecipe>> woodcutting = new ArrayList<>(generated);
        for (var r : allRecipes) {
            if (r.value() instanceof WoodcuttingRecipe w) {
                woodcutting.add(new RecipeHolder<>(r.id(), w));
            }
        }

        int added = 0;
        for (var holder : woodcutting) {
            WoodcuttingRecipe recipe = holder.value();
            if (recipe.getInputCount() != 1) continue;
            ItemStack result = recipe.getResultItem(registries);
            if (result.isEmpty() || result.getCount() > MAX_OUTPUT_COUNT) continue;

            Ingredient input = recipe.getIngredients().getFirst();
            JsonElement inputJson = Ingredient.CODEC_NONEMPTY.encodeStart(ops, input).result().orElse(null);
            if (inputJson == null) continue;

            JsonObject json = new JsonObject();
            json.add("ingredients", singleton(inputJson));
            json.add("results", singleton(encodeOutput(result)));
            json.addProperty("processing_time", PROCESSING_TIME);

            Recipe<?> cutting = codec.parse(ops, json).result().orElse(null);
            if (cutting == null) {
                SawmillMod.LOGGER.warn("Create didn't accept cutting recipe json {}. Skipping mechanical saw compat", json);
                return;
            }

            ResourceLocation id = SawmillMod.res("create_cutting/" + holder.id().getNamespace() + "/" + holder.id().getPath());
            RecipeHolder<Recipe<?>> cuttingHolder = new RecipeHolder<>(id, cutting);
            byName.put(id, cuttingHolder);
            byType.put(cutting.getType(), cuttingHolder);
            added++;
        }
        SawmillMod.LOGGER.info("Added {} Create cutting recipes for the Mechanical Saw", added);
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
