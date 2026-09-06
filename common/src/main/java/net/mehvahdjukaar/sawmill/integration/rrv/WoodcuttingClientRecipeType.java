package net.mehvahdjukaar.sawmill.integration.rrv;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WoodcuttingClientRecipeType implements ReliableClientRecipeType {

    public static final WoodcuttingClientRecipeType INSTANCE = new WoodcuttingClientRecipeType();
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("rrv", "textures/gui/type/stonecutter.png");

    @Override
    public Component getDisplayName() {
        return Component.translatable("sawmill.category.wood_cutting");
    }

    @Override
    public Identifier getId() {
        return SawmillMod.res("woodcutting");
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(SawmillMod.SAWMILL_BLOCK.get());
    }

    @Override
    public int getDisplayWidth() {
        return 80;
    }

    @Override
    public int getDisplayHeight() {
        return 24;
    }

    @Override
    public Identifier getGuiTexture() {
        return BACKGROUND;
    }

    @Override
    public int getSlotCount() {
        return 2;
    }

    @Override
    public void placeSlots(RecipeViewMenu.SlotDefinition slotDefinition) {
        slotDefinition.addItemSlot(0, 4, 4);
        slotDefinition.addItemSlot(1, 60, 4);
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(new ItemStack(SawmillMod.SAWMILL_BLOCK.get()));
    }
}
