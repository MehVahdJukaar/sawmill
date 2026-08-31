package net.mehvahdjukaar.sawmill.integration.rei;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class WoodcuttingCategory implements DisplayCategory<WoodcuttingDisplay> {

    @Override
    public CategoryIdentifier<? extends WoodcuttingDisplay> getCategoryIdentifier() {
        return REIPlugin.WOODCUTTING_DISPLAY;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(SawmillMod.SAWMILL_BLOCK.get());
    }

    @Override
    public Component getTitle() {
        return Component.translatable("sawmill.category.wood_cutting");
    }

    @Override
    public int getDisplayHeight() {
        return 36;
    }

    @Override
    public List<Widget> setupDisplay(WoodcuttingDisplay display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 13);
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createArrow(new Point(startPoint.x + 27, startPoint.y + 4)));
        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 61, startPoint.y + 5)));
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 61, startPoint.y + 5))
                .entries(display.getOutputEntries().getFirst())
                .disableBackground()
                .markOutput());
        Point point = new Point(startPoint.x + 4, startPoint.y + 5);
        widgets.add(Widgets.createSlot(point)
                .entries(display.getInputEntries().getFirst()).markInput());
        widgets.add(new Widget() {
            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                graphics.renderItemDecorations(Minecraft.getInstance().font,
                        new ItemStack(Items.DIRT, display.getInputCount()), point.x, point.y);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of();
            }
        });
        return widgets;
    }
}
