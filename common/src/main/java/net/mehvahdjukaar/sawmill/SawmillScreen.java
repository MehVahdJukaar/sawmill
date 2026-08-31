package net.mehvahdjukaar.sawmill;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SawmillScreen extends AbstractContainerScreen<SawmillMenu> {
    private static final Identifier BACKGROUND = SawmillMod.res("textures/gui/container/sawmill.png");
    private static final Identifier BACKGROUND_SEARCH = SawmillMod.res("textures/gui/container/sawmill_search.png");
    private static final Identifier BACKGROUND_WIDE = SawmillMod.res("textures/gui/container/sawmill_wide.png");
    private static final Identifier BACKGROUND_WIDE_SEARCH = SawmillMod.res("textures/gui/container/sawmill_search_wide.png");

    private static final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace("container/stonecutter/scroller");
    private static final Identifier SCROLLER_DISABLED_SPRITE = Identifier.withDefaultNamespace("container/stonecutter/scroller_disabled");
    private static final Identifier RECIPE_SELECTED_SPRITE = Identifier.withDefaultNamespace("container/stonecutter/recipe_selected");
    private static final Identifier RECIPE_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("container/stonecutter/recipe_highlighted");
    private static final Identifier RECIPE_SPRITE = Identifier.withDefaultNamespace("container/stonecutter/recipe");

    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;
    private boolean displayRecipes;

    private EditBox searchBox;

    private final List<WoodcuttingEntry> filteredRecipes = new ArrayList<>();
    private int filteredIndex = -1;

    public SawmillScreen(SawmillMenu sawmillMenu, Inventory inventory, Component component) {
        super(sawmillMenu, inventory, component);
        sawmillMenu.registerUpdateListener(this::containerChanged);
        --this.titleLabelY;
    }

    @Override
    protected void init() {
        super.init();

        int boxX = this.leftPos + (menu.isWide ? 41 : 53);
        int boxY = this.topPos + 15;
        this.searchBox = new EditBox(this.font, boxX, boxY, 69, 9, Component.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setFocused(false);
        this.searchBox.setEditable(false);
        this.searchBox.setTextColor(0xFFFFFFFF);
        this.searchBox.setResponder(s -> this.refreshSearchResults());
        this.addRenderableWidget(this.searchBox);

        updateSearchBarVisibility();
        refreshSearchResults();
    }

    private void updateSearchBarVisibility() {
        boolean hasSearch = CommonConfigs.hasSearchBar(menu.getRecipes().size());
        this.searchBox.setVisible(hasSearch);
        this.searchBox.active = hasSearch;
    }

    private void refreshSearchResults() {
        int oldSize = filteredRecipes.size();
        this.filteredRecipes.clear();
        String filter = searchBox.getValue().toLowerCase(Locale.ROOT);
        boolean isFiltered = searchBox.isVisible() && !filter.isEmpty();
        for (var r : this.menu.getRecipes()) {
            if (!isFiltered || r.matchFilter(filter)) {
                this.filteredRecipes.add(r);
            }
        }
        if (oldSize != filteredRecipes.size()) {
            //only reset if the filtered list changed
            this.scrollOffs = 0;
            this.startIndex = 0;
        }

        updateSelectedIndex();
        // this makes it so after we typed something, the current result is reset as we are unselecting all clicked stuff
        // only clear if we cant keep selecting the old one
        if (filteredIndex == -1 && this.menu.getSelectedRecipeIndex() != -1
                && this.menu.clickMenuButton(minecraft.player, -1)) {
            //also send a packet to servers to unselect
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, -1);
        }

        updateSearchBarVisibility();
    }

    private void updateSelectedIndex() {
        filteredIndex = -1;
        int selectedInd = this.menu.getSelectedRecipeIndex();
        var recipes = this.menu.getRecipes();
        if (selectedInd > -1 && selectedInd < recipes.size()) {
            filteredIndex = filteredRecipes.indexOf(recipes.get(selectedInd));
        }
    }

    @Override
    public void resize(int width, int height) {
        // same as creative tab one
        String string = this.searchBox.getValue();
        super.resize(width, height);
        this.searchBox.setValue(string);
        this.containerChanged();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        String string = this.searchBox.getValue();
        if (this.searchBox.isVisible() && this.searchBox.keyPressed(event)) {
            if (!Objects.equals(string, this.searchBox.getValue())) {
                this.refreshSearchResults();
            }
            return true;
        }
        boolean typingInBox = this.searchBox.isFocused() && this.searchBox.isVisible() && event.key() != 256;
        return typingInBox || super.keyPressed(event);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, getBgLocation(), this.leftPos, this.topPos,
                0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        // scrollbar
        int barH = scrollBarHeight();
        int scrollY = minScrollY();
        float barSpan = maxScrollY() - scrollY - barH;
        int barPos = (int) (barSpan * this.scrollOffs);

        Identifier scroller = this.isScrollBarActive() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, scroller, minScrollX(), scrollY + barPos, 12, barH);

        if (!displayRecipes) return;

        forEachButton((index, buttonX, buttonY) -> {
            Identifier buttonTexture;
            if (index == filteredIndex) {
                buttonTexture = RECIPE_SELECTED_SPRITE;
            } else if (mouseX >= buttonX && mouseY >= buttonY && mouseX < buttonX + 16 && mouseY < buttonY + 18) {
                buttonTexture = RECIPE_HIGHLIGHTED_SPRITE;
            } else {
                buttonTexture = RECIPE_SPRITE;
            }
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, buttonTexture, buttonX, buttonY, 16, 18);
        });

        forEachButton((index, buttonX, buttonY) -> {
            ItemStack item = filteredRecipes.get(index).result();
            graphics.fakeItem(item, buttonX, buttonY + 1);
            graphics.itemDecorations(font, item, buttonX, buttonY + 1);
        });
    }

    @NotNull
    private Identifier getBgLocation() {
        if (menu.isWide) {
            return searchBox.isVisible() ? BACKGROUND_WIDE_SEARCH : BACKGROUND_WIDE;
        }
        return searchBox.isVisible() ? BACKGROUND_SEARCH : BACKGROUND;
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (this.displayRecipes) {
            forEachButton((index, buttonX, buttonY) -> {
                if (mouseX >= buttonX && mouseX < buttonX + 16 && mouseY >= buttonY && mouseY < buttonY + 18) {
                    graphics.setTooltipForNextFrame(this.font, filteredRecipes.get(index).result(), mouseX, mouseY);
                }
            });
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        if (filteredIndex >= 0 && filteredIndex < filteredRecipes.size()) {
            int input = filteredRecipes.get(filteredIndex).inputCount();
            if (input != 1) {
                String multiplier = input + "x";
                int labelX = this.titleLabelX + (menu.isWide ? -4 : 0);
                graphics.text(this.font, multiplier, labelX, this.titleLabelY + 37, 0xFF404040, false);
            }
        }
    }

    private int getButtonCount() {
        return getRowCount() * getButtonsPerRow();
    }

    private int getButtonsPerRow() {
        return menu.isWide ? 5 : 4;
    }

    private int getRowCount() {
        return searchBox.isVisible() ? 2 : 3;
    }

    private int minScrollX() {
        return this.leftPos + (menu.isWide ? 123 : 119);
    }

    private int maxScrollX() {
        return minScrollX() + 12;
    }

    private int minScrollY() {
        return this.topPos + (searchBox.isVisible() ? 29 : 15);
    }

    private int maxScrollY() {
        return this.topPos + (searchBox.isVisible() ? 29 + 36 : 15 + 55);
    }

    private int scrollBarHeight() {
        return searchBox.isVisible() ? 11 : 15;
    }

    private void forEachButton(ButtonConsumer buttonConsumer) {
        int buttonBoxX = this.leftPos + (menu.isWide ? 40 : 52);
        int buttonBoxY = this.topPos + (searchBox.isVisible() ? 27 : 13);
        int lastVisibleElementIndex = this.startIndex + getButtonCount();
        int buttonsPerRow = getButtonsPerRow();
        for (int index = this.startIndex; index < lastVisibleElementIndex && index < filteredRecipes.size(); ++index) {
            int visualIndex = index - this.startIndex;
            int buttonX = buttonBoxX + (visualIndex % buttonsPerRow) * 16;
            int buttonY = buttonBoxY + (visualIndex / buttonsPerRow) * 18 + 2;
            buttonConsumer.accept(index, buttonX, buttonY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        this.scrolling = false;
        if (this.displayRecipes) {
            int clicked = buttonAt(event.x(), event.y());
            if (clicked != -1) {
                int actualIndex = menu.getRecipes().indexOf(filteredRecipes.get(clicked));
                if (this.menu.clickMenuButton(this.minecraft.player, actualIndex)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SawmillMod.SAWMILL_SELECT.get(), 1.0F));
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, actualIndex);
                    updateSelectedIndex();
                }
                return true;
            }

            if (event.x() >= minScrollX() && event.x() < maxScrollX() && event.y() >= minScrollY() && event.y() < maxScrollY()) {
                this.scrolling = true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private int buttonAt(double mouseX, double mouseY) {
        int[] found = {-1};
        forEachButton((index, buttonX, buttonY) -> {
            if (found[0] == -1 && mouseX >= buttonX && mouseX < buttonX + 16 && mouseY >= buttonY && mouseY < buttonY + 18) {
                found[0] = index;
            }
        });
        return found[0];
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.scrolling && this.isScrollBarActive()) {
            int min = minScrollY();
            int max = maxScrollY();
            this.scrollOffs = ((float) event.y() - min - 7.5F) / ((max - min) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) ((this.scrollOffs * this.getOffscreenRows()) + 0.5) * getButtonsPerRow();
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.scrolling = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.isScrollBarActive()) {
            int i = this.getOffscreenRows();
            float f = (float) scrollY / (float) i;
            this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
            this.startIndex = (int) ((this.scrollOffs * i) + 0.5) * getButtonsPerRow();
        }
        return true;
    }

    private boolean isScrollBarActive() {
        return this.displayRecipes && filteredRecipes.size() > getButtonCount();
    }

    protected int getOffscreenRows() {
        int buttonsPerRow = getButtonsPerRow();
        return (filteredRecipes.size() + buttonsPerRow - 1) / buttonsPerRow - getRowCount();
    }

    private void containerChanged() {
        this.displayRecipes = this.menu.hasInputItem();
        if (!this.displayRecipes) {
            this.scrollOffs = 0.0F;
            this.startIndex = 0;
            this.searchBox.setValue("");
        } else this.setFocused(searchBox);
        this.searchBox.setEditable(displayRecipes);
        this.searchBox.setFocused(displayRecipes);

        //recipes could have changed here so we need to refresh
        this.refreshSearchResults();
    }

    private interface ButtonConsumer {
        void accept(int index, int buttonX, int buttonY);
    }
}
