package net.mehvahdjukaar.sawmill;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.List;

public class SawmillMenu extends AbstractContainerMenu {
    public static final int MAX_RECIPES = 255;

    private final ContainerLevelAccess access;
    private final DataSlot selectedRecipeIndex;
    private final Level level;
    public final Container container;
    private final Slot inputSlot;
    private final Slot resultSlot;

    private List<WoodcuttingEntry> recipes = List.of();
    private ItemStack input;
    private long lastSoundTime;
    private final ResultContainer resultContainer;
    private Runnable slotUpdateListener;
    private WoodcuttingEntry lastSelectedRecipe = null;

    public boolean isWide = CommonConfigs.WIDE_GUI.get();

    public SawmillMenu(int i, Inventory inventory, FriendlyByteBuf buf) {
        this(i, inventory, ContainerLevelAccess.NULL);
    }

    public SawmillMenu(int i, Inventory inventory, final ContainerLevelAccess containerLevelAccess) {
        super(SawmillMod.SAWMILL_MENU.get(), i);
        this.selectedRecipeIndex = DataSlot.standalone();
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
        this.inputSlot = this.addSlot(new Slot(this.container, 0, isWide ? 17 : 21, 33));
        this.resultSlot = this.addSlot(new Slot(this.resultContainer, 1, isWide ? 146 : 143, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                stack.onCraftedBy(player, stack.getCount());
                resultContainer.awardUsedRecipes(player, List.of(inputSlot.getItem()));
                // guard against a desynced/stale selection (index can be -1) so we don't crash with IndexOutOfBounds.
                // This should be impossible (a filled result slot always implies a valid selection), so if it ever
                // trips, log it: it means the result-slot/index invariant got broken by some interleaving and we
                // want to know about it instead of silently swallowing it.
                if (isValidRecipeIndex(selectedRecipeIndex.get())) {
                    ItemStack itemStack = inputSlot.remove(recipes.get(selectedRecipeIndex.get()).inputCount());
                    if (!itemStack.isEmpty()) {
                        setupResultSlot();
                    }
                } else {
                    SawmillMod.LOGGER.warn("Took a sawmill result with no valid recipe selected (index={}, recipes={}). " +
                                    "This indicates a result-slot/selection desync; skipping input consumption.",
                            selectedRecipeIndex.get(), recipes.size());
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

    public List<WoodcuttingEntry> getRecipes() {
        return this.recipes;
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
        // hack since the freaking packet sends a byte not an int
        id = Byte.toUnsignedInt((byte) id);
        if (this.isValidRecipeIndex(id) || id == 255) {
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
        int maxItemsThatCanBeConsumed = 5; //I made it the f up
        if (!sameStack || itemStack.getCount() < maxItemsThatCanBeConsumed || old.getCount() < maxItemsThatCanBeConsumed) {
            this.input = itemStack.copy();
            this.setupRecipeList(itemStack);
        }
    }

    private void setupRecipeList(ItemStack stack) {
        this.selectedRecipeIndex.set(-1);
        this.resultSlot.set(ItemStack.EMPTY);

        List<WoodcuttingEntry> matching = List.of();
        if (!stack.isEmpty()) {
            matching = WoodcuttingRecipes.selectByInput(stack).stream()
                    .filter(e -> !e.result().is(SawmillMod.BLACKLIST))
                    .limit(MAX_RECIPES)
                    .toList();
        }
        this.recipes = matching;

        //preserve last clicked recipe on recipe change
        if (this.lastSelectedRecipe != null) {
            int newInd = this.recipes.indexOf(this.lastSelectedRecipe);
            if (newInd != -1) {
                this.selectedRecipeIndex.set(newInd);
            }
        }
        this.lastSelectedRecipe = null;
    }

    void setupResultSlot() {
        if (this.isValidRecipeIndex(this.selectedRecipeIndex.get())) {
            WoodcuttingEntry selected = this.recipes.get(this.selectedRecipeIndex.get());
            this.lastSelectedRecipe = selected;
            var holder = selected.recipe().orElse(null);
            if (holder != null) {
                ItemStack result = holder.value().assemble(new SingleRecipeInput(this.container.getItem(0)));
                if (result.isItemEnabled(this.level.enabledFeatures())) {
                    this.resultContainer.setRecipeUsed(holder);
                    this.resultSlot.set(result);
                } else {
                    this.resultSlot.set(ItemStack.EMPTY);
                }
            } else {
                this.resultSlot.set(ItemStack.EMPTY);
            }
        } else {
            this.resultContainer.setRecipeUsed(null);
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
                item.onCraftedBy(itemStack2, player);
                if (!this.moveItemStackTo(itemStack2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemStack2, itemStack);
            } else if (index == 0) {
                if (!this.moveItemStackTo(itemStack2, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (WoodcuttingRecipes.acceptsInput(itemStack2)) {
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
        this.access.execute((level, blockPos) -> this.clearContainer(player, this.container));
    }
}
