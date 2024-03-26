package net.mehvahdjukaar.sawmill;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;

public record FilterableRecipe(WoodcuttingRecipe recipe, ItemStack output) {

    public static FilterableRecipe of(WoodcuttingRecipe recipe) {
        return new FilterableRecipe(recipe, recipe.getResultItem(RegistryAccess.EMPTY));
    }

    public boolean matchFilter(String filter) {
        return output.getDisplayName().getString().contains(filter);
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
