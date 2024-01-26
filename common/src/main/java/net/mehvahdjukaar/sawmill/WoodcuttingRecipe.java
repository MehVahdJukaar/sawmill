package net.mehvahdjukaar.sawmill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.misc.StrOpt;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.level.Level;

public class WoodcuttingRecipe extends SingleItemRecipe {
    private final int inputCount;

    public WoodcuttingRecipe(String group, Ingredient ingredient, ItemStack result, int inputCount) {
        super(SawmillMod.WOODCUTTING_RECIPE.get(), SawmillMod.WOODCUTTING_RECIPE_SERIALIZER.get(), group, ingredient, result);
        this.inputCount = inputCount;
    }

    public int getInputCount() {
        return inputCount;
    }

    @Override
    public boolean matches(Container container, Level level) {
        ItemStack item = container.getItem(0);
        return this.ingredient.test(item) &&
                item.getCount() >= inputCount;
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
        private final Codec<WoodcuttingRecipe> codec;

        protected Serializer() {
            this.codec = RecordCodecBuilder.create((instance) -> instance.group(
                            ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter((singleItemRecipe) -> singleItemRecipe.group),
                            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter((singleItemRecipe) -> singleItemRecipe.ingredient),
                            ItemStack.RESULT_CODEC.forGetter((singleItemRecipe) -> singleItemRecipe.result),
                            StrOpt.of(ExtraCodecs.POSITIVE_INT, "ingredient_count", 1).forGetter(r -> r.inputCount)
                    )
                    .apply(instance, WoodcuttingRecipe::new));
        }

        @Override
        public Codec<WoodcuttingRecipe> codec() {
            return this.codec;
        }

        @Override
        public WoodcuttingRecipe fromNetwork(FriendlyByteBuf buffer) {
            String string = buffer.readUtf();
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack itemStack = buffer.readItem();
            int intCount = buffer.readVarInt();
            return new WoodcuttingRecipe(string, ingredient, itemStack, intCount);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, WoodcuttingRecipe recipe) {
            buffer.writeUtf(recipe.group);
            recipe.ingredient.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.inputCount);
        }

    }
}

