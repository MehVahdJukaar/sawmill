package net.mehvahdjukaar.sawmill;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class WoodcuttingRecipe extends SingleItemRecipe {
    private final int inputCount;

    public WoodcuttingRecipe(String group, Ingredient ingredient, ItemStack itemStack, int inputCount) {
        super(SawmillMod.WOODCUTTING_RECIPE.get(), SawmillMod.WOODCUTTING_RECIPE_SERIALIZER.get(),
                group, ingredient, itemStack);
        this.inputCount = inputCount;
    }

    public int getInputCount() {
        return inputCount;
    }

    @Override
    public boolean matches(SingleRecipeInput container, Level level) {
        ItemStack item = container.getItem(0);
        return this.ingredient.test(item) && item.getCount() >= inputCount;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(SawmillMod.SAWMILL_BLOCK.get());
    }

    @Override
    public boolean isSpecial() {
        return true; //for recipe book
    }

    public static class Serializer implements RecipeSerializer<WoodcuttingRecipe> {

        private final MapCodec<WoodcuttingRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, WoodcuttingRecipe> streamCodec;

        public Serializer() {

            this.codec = RecordCodecBuilder.mapCodec(
                    instance -> instance.group(
                                    Codec.STRING.optionalFieldOf("group", "").forGetter(arg -> arg.group),
                                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(arg -> arg.ingredient),
                                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter(arg -> arg.result),
                                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("ingredient_count", 1).forGetter(arg -> arg.inputCount)
                            )
                            .apply(instance, WoodcuttingRecipe::new)
            );
            this.streamCodec = StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, arg -> arg.group,
                    Ingredient.CONTENTS_STREAM_CODEC, arg -> arg.ingredient,
                    ItemStack.STREAM_CODEC, arg -> arg.result,
                    ByteBufCodecs.VAR_INT, arg -> arg.inputCount,
                    WoodcuttingRecipe::new
            );
        }

        @Override
        public MapCodec<WoodcuttingRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, WoodcuttingRecipe> streamCodec() {
            return streamCodec;
        }
    }
}

