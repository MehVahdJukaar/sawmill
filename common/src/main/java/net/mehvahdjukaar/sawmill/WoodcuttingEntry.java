package net.mehvahdjukaar.sawmill;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Locale;
import java.util.Optional;

public record WoodcuttingEntry(Ingredient input, ItemStack result, int inputCount,
                               Optional<RecipeHolder<WoodcuttingRecipe>> recipe) {

    public static final StreamCodec<RegistryFriendlyByteBuf, WoodcuttingEntry> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, WoodcuttingEntry::input,
            ItemStack.STREAM_CODEC, WoodcuttingEntry::result,
            ByteBufCodecs.VAR_INT, WoodcuttingEntry::inputCount,
            (input, result, count) -> new WoodcuttingEntry(input, result, count, Optional.empty()));

    public static WoodcuttingEntry of(RecipeHolder<WoodcuttingRecipe> holder) {
        WoodcuttingRecipe recipe = holder.value();
        return new WoodcuttingEntry(recipe.input(), recipe.resultTemplate().create(),
                recipe.getInputCount(), Optional.of(holder));
    }

    public boolean matches(ItemStack stack) {
        return input.test(stack) && stack.getCount() >= inputCount;
    }

    public boolean matchFilter(String filter) {
        return result.getDisplayName().getString().toLowerCase(Locale.ROOT).contains(filter);
    }
}
