package net.mehvahdjukaar.sawmill.integration.rrv;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import net.mehvahdjukaar.sawmill.SawmillScreen;
import net.mehvahdjukaar.sawmill.WoodcuttingEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;

import java.util.List;

public class WoodcuttingClientRecipe implements ReliableClientRecipe {

    private final SlotContent input, result;
    private final Identifier id;

    public WoodcuttingClientRecipe(WoodcuttingEntry entry) {
        this.input = SlotContent.of(entry.input());
        this.result = SlotContent.of(entry.result());
        this.id = entry.id();

        int count = entry.inputCount();
        if (count > 1) this.input.getValidContents().forEach(s -> s.setCount(count));
    }

    @Override
    public ReliableClientRecipeType getType() {
        return WoodcuttingClientRecipeType.INSTANCE;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext slotFillContext) {
        slotFillContext.bindSlot(0, this.input);
        slotFillContext.bindSlot(1, this.result);
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(this.input);
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(this.result);
    }

    @Override
    public boolean supportsItemTransfer() {
        return true;
    }

    @Override
    public List<Class<? extends AbstractContainerScreen<?>>> getTransferClasses() {
        return List.of(SawmillScreen.class);
    }

    @Override
    public void mapRecipeItems(RecipeTransferMap transferMap, AbstractContainerScreen<?> screen) {
        transferMap.linkSlots(0, 0);
    }
}
