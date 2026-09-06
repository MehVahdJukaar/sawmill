package net.mehvahdjukaar.sawmill;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Locale;

public sealed interface WoodcuttingEntry {

    StreamCodec<RegistryFriendlyByteBuf, WoodcuttingEntry> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, WoodcuttingEntry::id,
            Ingredient.CONTENTS_STREAM_CODEC, WoodcuttingEntry::input,
            ItemStack.STREAM_CODEC, WoodcuttingEntry::result,
            ByteBufCodecs.VAR_INT, WoodcuttingEntry::inputCount,
            ClientSide::new);

    static WoodcuttingEntry of(RecipeHolder<WoodcuttingRecipe> holder) {
        return new ServerSide(holder);
    }

    Identifier id();

    Ingredient input();

    ItemStack result();

    int inputCount();

    default boolean matches(ItemStack stack) {
        return input().test(stack) && stack.getCount() >= inputCount();
    }

    default boolean matchFilter(String filter) {
        return result().getDisplayName().getString().toLowerCase(Locale.ROOT).contains(filter);
    }

    record ServerSide(RecipeHolder<WoodcuttingRecipe> holder) implements WoodcuttingEntry {

        @Override
        public Identifier id() {
            return holder.id().identifier();
        }

        @Override
        public Ingredient input() {
            return holder.value().input();
        }

        @Override
        public ItemStack result() {
            return holder.value().resultTemplate().create();
        }

        @Override
        public int inputCount() {
            return holder.value().getInputCount();
        }
    }

    record ClientSide(Identifier id, Ingredient input, ItemStack result, int inputCount) implements WoodcuttingEntry {
    }
}
