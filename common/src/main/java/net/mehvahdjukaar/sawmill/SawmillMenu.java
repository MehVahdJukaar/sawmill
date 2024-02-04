package net.mehvahdjukaar.sawmill;

import com.google.common.collect.Lists;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.List;

public class SawmillMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final DataSlot selectedRecipeIndex;
    private final Level level;
    private List<RecipeHolder<WoodcuttingRecipe>> recipes;
    private ItemStack input;
    long lastSoundTime;
    final Slot inputSlot;
    final Slot resultSlot;
    Runnable slotUpdateListener;
    public final Container container;
    final ResultContainer resultContainer;

    private ResourceLocation lastSelectedRecipe = null;

    public SawmillMenu(int i, Inventory inventory, FriendlyByteBuf buf) {
        this(i, inventory, ContainerLevelAccess.NULL);
    }

    public SawmillMenu(int i, Inventory inventory, final ContainerLevelAccess containerLevelAccess) {
        super(SawmillMod.SAWMILL_MENU.get(), i);
        this.selectedRecipeIndex = DataSlot.standalone();
        this.recipes = Lists.newArrayList();
        this.input = ItemStack.EMPTY;
        this.slotUpdateListener = () -> {
        };
        this.container = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                slotsChanged(this);
                slotUpdateListener.run();
            }
        };
        this.resultContainer = new ResultContainer();
        this.access = containerLevelAccess;
        this.level = inventory.player.level();
        this.inputSlot = this.addSlot(new Slot(this.container, 0, 21, 33));
        this.resultSlot = this.addSlot(new Slot(this.resultContainer, 1, 143, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                stack.onCraftedBy(player.level(), player, stack.getCount());
                resultContainer.awardUsedRecipes(player, this.getRelevantItems());
                ItemStack itemStack = inputSlot.remove(recipes.get(selectedRecipeIndex.get()).value().getInputCount());
                if (!itemStack.isEmpty()) {
                    setupResultSlot();
                }

                containerLevelAccess.execute((level, blockPos) -> {
                    long l = level.getGameTime();
                    if (lastSoundTime != l) {
                        level.playSound(null, blockPos, SawmillMod.SAWMILL_TAKE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                        lastSoundTime = l;
                    }

                });
                super.onTake(player, stack);
            }

            private List<ItemStack> getRelevantItems() {
                return List.of(inputSlot.getItem());
            }
        });

        int j;
        for (j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(inventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
            }
        }

        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(inventory, j, 8 + j * 18, 142));
        }

        this.addDataSlot(this.selectedRecipeIndex);
    }

    public int getSelectedRecipeIndex() {
        return this.selectedRecipeIndex.get();
    }

    public List<RecipeHolder<WoodcuttingRecipe>> getRecipes() {
        return this.recipes;
    }

    public int getNumRecipes() {
        return this.recipes.size();
    }

    public boolean hasInputItem() {
        return this.inputSlot.hasItem() && !this.recipes.isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, SawmillMod.SAWMILL_BLOCK.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.isValidRecipeIndex(id)) {
            this.selectedRecipeIndex.set(id);
            this.setupResultSlot();
        }

        return true;
    }

    private boolean isValidRecipeIndex(int recipeIndex) {
        return recipeIndex >= 0 && recipeIndex < this.recipes.size();
    }

    @Override
    public void slotsChanged(Container container) {
        ItemStack itemStack = this.inputSlot.getItem();
        ItemStack old = this.input;
        boolean sameStack = itemStack.is(old.getItem());
        int maxConsumable = 5;
        if (!sameStack || itemStack.getCount() < maxConsumable || old.getCount() < maxConsumable) {
            this.input = itemStack.copy();
            this.setupRecipeList(container, itemStack);
        }

    }

    private void setupRecipeList(Container container, ItemStack stack) {
        this.recipes.clear();
        this.selectedRecipeIndex.set(-1);

        this.resultSlot.set(ItemStack.EMPTY);
        if (!stack.isEmpty()) {
            this.recipes = this.level.getRecipeManager()
                    .getRecipesFor(SawmillMod.WOODCUTTING_RECIPE.get(), container, this.level);

            //remove blacklisted
            this.recipes.removeIf(r -> r.value().getResultItem(RegistryAccess.EMPTY).is(SawmillMod.BLACKLIST));

            RecipeSorter.sort(this.recipes, this.level);

            //preserve last clicked recipe on recipe change
            if (lastSelectedRecipe != null) {
                this.recipes.stream().filter(h -> h.id().equals(lastSelectedRecipe)).findAny()
                        .ifPresent(r -> this.selectedRecipeIndex.set(this.recipes.indexOf(r)));
            }
        }

        lastSelectedRecipe = null;
    }

    void setupResultSlot() {
        if (!this.recipes.isEmpty() && this.isValidRecipeIndex(this.selectedRecipeIndex.get())) {
            var recipe = this.recipes.get(this.selectedRecipeIndex.get());
            this.lastSelectedRecipe = recipe.id();
            ItemStack itemStack = recipe.value().assemble(this.container, this.level.registryAccess());
            if (itemStack.isItemEnabled(this.level.enabledFeatures())) {
                this.resultContainer.setRecipeUsed(recipe);
                this.resultSlot.set(itemStack);
            } else {
                this.resultSlot.set(ItemStack.EMPTY);
            }
        } else {
            this.resultSlot.set(ItemStack.EMPTY);
        }

        this.broadcastChanges();
    }

    @Override
    public MenuType<?> getType() {
        return SawmillMod.SAWMILL_MENU.get();
    }

    public void registerUpdateListener(Runnable listener) {
        this.slotUpdateListener = listener;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.resultContainer && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            Item item = itemStack2.getItem();
            itemStack = itemStack2.copy();
            if (index == 1) {
                item.onCraftedBy(itemStack2, player.level(), player);
                if (!this.moveItemStackTo(itemStack2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemStack2, itemStack);
            } else if (index == 0) {
                if (!this.moveItemStackTo(itemStack2, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.level.getRecipeManager().getRecipeFor(SawmillMod.WOODCUTTING_RECIPE.get(), new SimpleContainer(itemStack2), this.level).isPresent()) {
                if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 2 && index < 29) {
                if (!this.moveItemStackTo(itemStack2, 29, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 29 && index < 38 && !this.moveItemStackTo(itemStack2, 2, 29, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }

            slot.setChanged();
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemStack2);
            this.broadcastChanges();
        }

        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.resultContainer.removeItemNoUpdate(1);
        this.access.execute((level, blockPos) -> {
            this.clearContainer(player, this.container);
        });
    }
}
