package net.mehvahdjukaar.sawmill;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SawmillClient {

    public static void init() {
        ClientHelper.addClientSetup(SawmillClient::setup);
    }

    private static void setup() {
        ClientHelper.registerRenderType(SawmillMod.SAWMILL_BLOCK.get(), RenderType.cutout());
        MenuScreens.register(SawmillMod.SAWMILL_MENU.get(), SawmillScreen::new);
    }

    private static boolean hasManyRecipes = false;

    public static void refreshRecipeSize() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            CraftingContainer dummy = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
                public ItemStack quickMoveStack(Player player, int index) {
                    return ItemStack.EMPTY;
                }

                public boolean stillValid(Player player) {
                    return false;
                }
            }, 1, 1);
            dummy.setItem(0, Items.OAK_LOG.getDefaultInstance());
            var recipes = level.getRecipeManager().getRecipesFor(SawmillMod.WOODCUTTING_RECIPE.get(), dummy, level);

            hasManyRecipes = recipes.size() > CommonConfigs.SEARCH_BAR_THREASHOLD.get();
        }
    }

    public static boolean hasManyRecipes() {
        return hasManyRecipes;
    }
}
