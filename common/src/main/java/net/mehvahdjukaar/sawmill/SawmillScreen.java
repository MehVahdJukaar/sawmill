package net.mehvahdjukaar.sawmill;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;

import java.awt.*;
import java.util.List;

public class SawmillScreen extends AbstractContainerScreen<SawmillMenu> {
    private static final ResourceLocation BG_LOCATION = SawmillMod.res("textures/gui/container/sawmill.png");
    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;
    private boolean displayRecipes;

    public SawmillScreen(SawmillMenu sawmillMenu, Inventory inventory, Component component) {
        super(sawmillMenu, inventory, component);
        sawmillMenu.registerUpdateListener(this::containerChanged);
        --this.titleLabelY;
    }

    @Override
    public void render(PoseStack guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        this.renderBackground(poseStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BG_LOCATION);
        int k = this.leftPos;
        int l = this.topPos;
        this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
        int m = (int)(41.0F * this.scrollOffs);
        this.blit(poseStack, k + 119, l + 15 + m, 176 + (this.isScrollBarActive() ? 0 : 12), 0, 12, 15);
        int n = this.leftPos + 52;
        int o = this.topPos + 14;
        int p = this.startIndex + 12;
        this.renderButtons(poseStack, i, j, n, o, p);
        this.renderRecipes(n, o, p);
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        super.renderLabels(pose, mouseX, mouseY);

        int selectedRecipeIndex = menu.getSelectedRecipeIndex();
        List<WoodcuttingRecipe> recipes = this.menu.getRecipes();
        if (selectedRecipeIndex >= 0 && selectedRecipeIndex < recipes.size()) {
            int input = recipes.get(selectedRecipeIndex).getInputCount();
            if (input != 1) {
                String multiplier = input+"x" ;

                Gui.drawString(pose, this.font, multiplier, this.titleLabelX, this.titleLabelY + 37, 4210752);
            }
        }

    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int i, int j) {
        super.renderTooltip(poseStack, i, j);
        if (this.displayRecipes) {
            int k = this.leftPos + 52;
            int l = this.topPos + 14;
            int m = this.startIndex + 12;
            List<WoodcuttingRecipe> list = (this.menu).getRecipes();

            for(int n = this.startIndex; n < m && n < (this.menu).getNumRecipes(); ++n) {
                int o = n - this.startIndex;
                int p = k + o % 4 * 16;
                int q = l + o / 4 * 18 + 2;
                if (i >= p && i < p + 16 && j >= q && j < q + 18) {
                    this.renderTooltip(poseStack, (list.get(n)).getResultItem(), i, j);
                }
            }
        }

    }


    private void renderButtons(PoseStack poseStack, int i, int j, int k, int l, int m) {
        for(int n = this.startIndex; n < m && n < (this.menu).getNumRecipes(); ++n) {
            int o = n - this.startIndex;
            int p = k + o % 4 * 16;
            int q = o / 4;
            int r = l + q * 18 + 2;
            int s = this.imageHeight;
            if (n == (this.menu).getSelectedRecipeIndex()) {
                s += 18;
            } else if (i >= p && j >= r && i < p + 16 && j < r + 18) {
                s += 36;
            }

            this.blit(poseStack, p, r - 1, 0, s, 16, 18);
        }

    }


    private void renderRecipes(int i, int j, int k) {
        List<WoodcuttingRecipe> list = (this.menu).getRecipes();

        for(int l = this.startIndex; l < k && l < (this.menu).getNumRecipes(); ++l) {
            int m = l - this.startIndex;
            int n = i + m % 4 * 16;
            int o = m / 4;
            int p = j + o * 18 + 2;
            this.minecraft.getItemRenderer().renderAndDecorateItem((list.get(l)).getResultItem(), n, p);
        }

    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        if (this.displayRecipes) {
            int i = this.leftPos + 52;
            int j = this.topPos + 14;
            int k = this.startIndex + 12;

            for (int l = this.startIndex; l < k; ++l) {
                int m = l - this.startIndex;
                double d = mouseX - (i + m % 4 * 16);
                double e = mouseY - (j + (m / 4) * 18);
                if (d >= 0.0 && e >= 0.0 && d < 16.0 && e < 18.0 && (this.menu).clickMenuButton(this.minecraft.player, l)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SawmillMod.SAWMILL_SELECT.get(), 1.0F));
                    this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, l);
                    return true;
                }
            }

            i = this.leftPos + 119;
            j = this.topPos + 9;
            if (mouseX >= i && mouseX < (i + 12) && mouseY >= j && mouseY < (j + 54)) {
                this.scrolling = true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling && this.isScrollBarActive()) {
            int i = this.topPos + 14;
            int j = i + 54;
            this.scrollOffs = ((float) mouseY - i - 7.5F) / ((j - i) - 15.0F);
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
            int i = this.getOffscreenRows();
            float f = (float) delta / i;
            this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
            this.startIndex = (int) ((this.scrollOffs * i) + 0.5) * 4;
        }

        return true;
    }

    private boolean isScrollBarActive() {
        return this.displayRecipes && (this.menu).getNumRecipes() > 12;
    }

    protected int getOffscreenRows() {
        return ((this.menu).getNumRecipes() + 4 - 1) / 4 - 3;
    }

    private void containerChanged() {
        this.displayRecipes = (this.menu).hasInputItem();
        if (!this.displayRecipes) {
            this.scrollOffs = 0.0F;
            this.startIndex = 0;
        }

    }
}

