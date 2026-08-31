package net.mehvahdjukaar.sawmill;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public class WoodcuttingRecipe extends SingleItemRecipe {

    public static final MapCodec<WoodcuttingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(SingleItemRecipe::input),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(WoodcuttingRecipe::resultTemplate),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("ingredient_count", 1).forGetter(WoodcuttingRecipe::getInputCount)
            ).apply(i, WoodcuttingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WoodcuttingRecipe> STREAM_CODEC = StreamCodec.composite(
            CommonInfo.STREAM_CODEC, r -> r.commonInfo,
            Ingredient.CONTENTS_STREAM_CODEC, SingleItemRecipe::input,
            ItemStackTemplate.STREAM_CODEC, WoodcuttingRecipe::resultTemplate,
            ByteBufCodecs.VAR_INT, WoodcuttingRecipe::getInputCount,
            WoodcuttingRecipe::new);

    private final int inputCount;

    public WoodcuttingRecipe(CommonInfo commonInfo, Ingredient ingredient, ItemStackTemplate result, int inputCount) {
        super(commonInfo, ingredient, result);
        if (inputCount > 64) {
            throw new IllegalArgumentException("Input count for wood cutting recipe is too high: " + inputCount +
                    ". Ingredient: " + ingredient + ", Result: " + result);
        }
        this.inputCount = inputCount;
    }

    public int getInputCount() {
        return inputCount;
    }

    public ItemStackTemplate resultTemplate() {
        return this.result();
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input().test(input.item()) && input.item().getCount() >= inputCount;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeType<WoodcuttingRecipe> getType() {
        return SawmillMod.WOODCUTTING_RECIPE.get();
    }

    @Override
    public RecipeSerializer<WoodcuttingRecipe> getSerializer() {
        return SawmillMod.WOODCUTTING_RECIPE_SERIALIZER.get();
    }

    @Override
    public boolean isSpecial() {
        return true; //for recipe book
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new StonecutterRecipeDisplay(this.input().display(), this.resultDisplay(),
                new SlotDisplay.ItemSlotDisplay(SawmillMod.SAWMILL_BLOCK.get().asItem().builtInRegistryHolder())));
    }

    public SlotDisplay resultDisplay() {
        return new SlotDisplay.ItemStackSlotDisplay(this.result());
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.STONECUTTER;
    }
}
