package net.mehvahdjukaar.sawmill;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SawmillScreen extends AbstractContainerScreen<SawmillMenu> {
    private static final ResourceLocation BG_LOCATION = SawmillMod.res("textures/gui/container/sawmill.png");
    private static final ResourceLocation BG_LOCATION_SEARCH = SawmillMod.res("textures/gui/container/sawmill_search.png");
    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;
    private boolean displayRecipes;

    private EditBox searchBox;

    private final List<WoodcuttingRecipe> filteredRecipes = new ArrayList<>();
    private int filteredIndex = -1;

    public SawmillScreen(SawmillMenu sawmillMenu, Inventory inventory, Component component) {
        super(sawmillMenu, inventory, component);
        sawmillMenu.registerUpdateListener(this::containerChanged);
        --this.titleLabelY;
    }

    @Override
    protected void init() {
        super.init();


        int boxX = this.leftPos + 53;
        int boxY = this.topPos + 15;
        this.searchBox = new EditBox(this.font, boxX, boxY, 69, 9, Component.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setFocused(false);
        this.searchBox.setEditable(false);
        this.searchBox.setTextColor(16777215);
        this.searchBox.setResponder(s -> this.refreshSearchResults());
        this.addRenderableWidget(this.searchBox);

        ClientConfigs.SearchMode searchMode = ClientConfigs.SEARCH_MODE.get();
        boolean hasSearch = searchMode == ClientConfigs.SearchMode.ON ||
                (searchMode == ClientConfigs.SearchMode.AUTOMATIC && SawmillClient.hasManyRecipes());
        hasSearch = true;
        this.searchBox.visible = hasSearch;
        this.searchBox.active = hasSearch;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (searchBox.visible) this.searchBox.tick();
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (slot != null && slot.container == getMenu().container) {
            highlightSearch();
        }
        super.slotClicked(slot, slotId, mouseButton, type);
    }

    private void highlightSearch() {
        this.searchBox.moveCursorToEnd();
        this.searchBox.setHighlightPos(0);
    }

    private void refreshSearchResults() {
        this.filteredRecipes.clear();
        boolean isFiltered = searchBox.visible && !searchBox.getValue().equals("");
        for (var r : this.menu.getRecipes()) {
            if (!isFiltered || Utils.getID(r.getResultItem(RegistryAccess.EMPTY).getItem())
                    .getPath().contains(searchBox.getValue())) {
                this.filteredRecipes.add(r);
            }
        }
        this.scrollOffs = 0;
        this.startIndex = 0;
        updateSelectedIndex();
    }

    private void updateSelectedIndex() {
        filteredIndex = -1;
        int selectedInd = this.menu.getSelectedRecipeIndex();
        List<WoodcuttingRecipe> recipes = this.menu.getRecipes();
        if (selectedInd > 0 && filteredIndex < recipes.size()) {
            filteredIndex = filteredRecipes.indexOf(recipes.get(selectedInd));
        }
        if (filteredIndex == -1 && selectedInd != -1) {
            //pretty sure we only need it client side
            this.menu.clearResult();
        }
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        // same as creative tab one
        String string = this.searchBox.getValue();
        this.init(minecraft, width, height);
        this.searchBox.setValue(string);
        this.containerChanged();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        String string = this.searchBox.getValue();
        if (this.searchBox.visible && this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(string, this.searchBox.getValue())) {
                this.refreshSearchResults();
            }

            return true;
        } else {
            return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != 256 ? true : super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        this.renderBackground(guiGraphics);

        ResourceLocation bgLocation = searchBox.visible ? BG_LOCATION_SEARCH : BG_LOCATION;
        guiGraphics.blit(bgLocation, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        int barH = scrollBarHeight();
        int scrollY = minScrollY();
        float barSpan = maxScrollY() - scrollY - barH;
        int barPos = (int) (barSpan * this.scrollOffs);

        guiGraphics.blit(bgLocation, this.leftPos + 119, scrollY + barPos, 176 + (this.isScrollBarActive() ? 0 : 12), 0, 12, barH);
        int buttonBoxX = this.leftPos + 52;
        int buttonBoxY = this.topPos + buttonBoxYOffset();
        int endIndex = this.startIndex + buttonCount();
        this.renderButtons(guiGraphics, mouseX, mouseY, buttonBoxX, buttonBoxY, endIndex);
        this.renderRecipes(guiGraphics, buttonBoxX, buttonBoxY, endIndex);

    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        if (filteredIndex >= 0 && filteredIndex < filteredRecipes.size()) {
            int input = filteredRecipes.get(filteredIndex).getInputCount();
            if (input != 1) {
                String multiplier = input + "x";

                guiGraphics.drawString(this.font, multiplier, this.titleLabelX, this.titleLabelY + 37, 4210752, false);
            }
        }

    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);
        if (this.displayRecipes) {
            int startX = this.leftPos + 52;
            int startY = this.topPos + 14;
            int endIndex = this.startIndex + buttonCount();

            for (int l = this.startIndex; l < endIndex && l < filteredRecipes.size(); ++l) {
                int m = l - this.startIndex;
                int n = startX + m % 4 * 16;
                int o = startY + m / 4 * 18 + 2;
                if (x >= n && x < n + 16 && y >= o && y < o + 18) {
                    guiGraphics.renderTooltip(this.font, (filteredRecipes.get(l)).getResultItem(this.minecraft.level.registryAccess()), x, y);
                }
            }
        }

    }

    private int buttonBoxYOffset() {
        return searchBox.visible ? 28 : 14;
    }

    private int buttonCount() {
        return getRows() * 4;
    }

    private int getRows() {
        return searchBox.visible ? 2 : 3;
    }

    private int minScrollY() {
        return this.topPos + (searchBox.visible ? 29 : 15);
    }

    private int maxScrollY() {
        return this.topPos + (searchBox.visible ? 29 + 36 : 15 + 55);
    }

    private int scrollBarHeight() {
        return searchBox.visible ? 11 : 15;
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, int lastVisibleElementIndex) {
        for (int i = this.startIndex; i < lastVisibleElementIndex && i < filteredRecipes.size(); ++i) {
            int j = i - this.startIndex;
            int k = x + j % 4 * 16;
            int l = j / 4;
            int m = y + l * 18 + 2;
            int n = this.imageHeight;
            if (i == filteredIndex) {
                n += 18;
            } else if (mouseX >= k && mouseY >= m && mouseX < k + 16 && mouseY < m + 18) {
                n += 36;
            }

            guiGraphics.blit(BG_LOCATION, k, m - 1, 0, n, 16, 18);
        }

    }

    private void renderRecipes(GuiGraphics guiGraphics, int x, int y, int startIndex) {

        for (int i = this.startIndex; i < startIndex && i < filteredRecipes.size(); ++i) {
            int j = i - this.startIndex;
            int k = x + j % 4 * 16;
            int l = j / 4;
            int m = y + l * 18 + 2;
            ItemStack item = filteredRecipes.get(i).getResultItem(this.minecraft.level.registryAccess());
            guiGraphics.renderFakeItem(item, k, m);
            guiGraphics.renderItemDecorations(font, item, k, m);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        if (this.displayRecipes) {
            int i = this.leftPos + 52;
            int j = this.topPos + buttonBoxYOffset();
            int endIndex = this.startIndex + buttonCount();

            for (int recipeIndex = this.startIndex; recipeIndex < endIndex && recipeIndex < filteredRecipes.size(); ++recipeIndex) {
                int m = recipeIndex - this.startIndex;
                double d = mouseX - (i + m % 4 * 16);
                double e = mouseY - (j + (m / 4) * 18);
                int actualIndex = menu.getRecipes().indexOf(filteredRecipes.get(recipeIndex));

                if (d >= 0.0 && e >= 0.0 && d < 16.0 && e < 18.0 && (this.menu).clickMenuButton(this.minecraft.player, actualIndex)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SawmillMod.SAWMILL_SELECT.get(), 1.0F));
                    this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, actualIndex);
                    updateSelectedIndex();
                    return true;
                }
            }

            i = this.leftPos + 119;
            if (mouseX >= i && mouseX < (i + 12) && mouseY >= minScrollY() && mouseY < maxScrollY()) {
                this.scrolling = true;
                highlightSearch();
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling && this.isScrollBarActive()) {
            int min = minScrollY();
            int max = maxScrollY();
            this.scrollOffs = ((float) mouseY - min - 7.5F) / ((max - min) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) ((this.scrollOffs * this.getOffscreenRows()) + 0.5) * 4;
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.isScrollBarActive()) {
            int offscreenRows = this.getOffscreenRows();
            float f = (float) delta / offscreenRows;
            this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
            this.startIndex = (int) ((this.scrollOffs * offscreenRows) + 0.5) * 4;
        }

        return true;
    }

    private boolean isScrollBarActive() {
        return this.displayRecipes && filteredRecipes.size() > buttonCount();
    }

    protected int getOffscreenRows() {
        return (filteredRecipes.size() + 4 - 1) / 4 - getRows();
    }

    private void containerChanged() {
        this.displayRecipes = (this.menu).hasInputItem();
        if (!this.displayRecipes) {
            this.scrollOffs = 0.0F;
            this.startIndex = 0;
            this.searchBox.setValue("");
        } else this.setFocused(searchBox);
        this.searchBox.setEditable(displayRecipes);
        this.searchBox.setFocused(displayRecipes);

        this.refreshSearchResults();
    }

}

