package net.mehvahdjukaar.sawmill;

import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SawmillClient {

    public static void init() {
        ClientPlatformHelper.addClientSetup(SawmillClient::setup);
    }

    private static void setup() {
        ClientPlatformHelper.registerRenderType(SawmillMod.SAWMILL_BLOCK.get(), RenderType.cutout());
        MenuScreens.register(SawmillMod.SAWMILL_MENU.get(), SawmillScreen::new);
    }

    private static boolean hasManyRecipes = false;

    public static void onTagsUpdated() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            CraftingContainer dummy = new CraftingContainer(new AbstractContainerMenu(null, -1) {
                public ItemStack quickMoveStack(Player player, int index) {
                    return ItemStack.EMPTY;
                }

                public boolean stillValid(Player player) {
                    return false;
                }
            }, 1, 1);
            dummy.setItem(0, Items.OAK_LOG.getDefaultInstance());
            var recipes = level.getRecipeManager().getRecipesFor(SawmillMod.WOODCUTTING_RECIPE.get(), dummy, level);

            hasManyRecipes = recipes.size() > CommonConfigs.SEARCH_BAR_THRESHOLD.get();
        }

        //update sort on client
        //var rec = level.getRecipeManager().getAllRecipesFor(SawmillMod.WOODCUTTING_RECIPE.get());
        //RecipeSorter.accept(rec);
    }

    public static boolean hasManyRecipes() {
        return hasManyRecipes;
    }
}
