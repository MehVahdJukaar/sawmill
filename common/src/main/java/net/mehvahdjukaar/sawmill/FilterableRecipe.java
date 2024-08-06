package net.mehvahdjukaar.sawmill;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Locale;

public record FilterableRecipe(RecipeHolder<WoodcuttingRecipe> recipe, ItemStack output) {

    public static FilterableRecipe of(RecipeHolder<WoodcuttingRecipe> recipe) {
        return new FilterableRecipe(recipe, recipe.value().getResultItem(RegistryAccess.EMPTY));
    }

    public boolean matchFilter(String filter) {
        return output.getDisplayName().getString().toLowerCase(Locale.ROOT).contains(filter);
    }


    // we could use this instead...
    /*
        Minecraft.getInstance().searchRegistry.register(SearchRegistry.CREATIVE_NAMES, (list) ->
                new FullTextSearchTree<>((itemStack) ->
                        itemStack.getTooltipLines(null, TooltipFlag.Default.NORMAL.asCreative())
                                .stream().map((component) -> ChatFormatting.stripFormatting(component.getString())
                                        .trim()).filter((string) -> !string.isEmpty()),
                        (itemStack) -> Stream.of(BuiltInRegistries.ITEM.getKey(itemStack.getItem())),
                        list));*/
}
