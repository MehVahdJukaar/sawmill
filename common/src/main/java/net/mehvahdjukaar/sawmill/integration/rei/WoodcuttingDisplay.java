package net.mehvahdjukaar.sawmill.integration.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.mehvahdjukaar.sawmill.WoodcuttingEntry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class WoodcuttingDisplay implements Display {
    private final List<EntryIngredient> input;
    private final List<EntryIngredient> output;
    private final int inputCount;

    public WoodcuttingDisplay(WoodcuttingEntry entry) {
        this.input = List.of(EntryIngredients.ofIngredient(entry.input()));
        this.output = List.of(EntryIngredients.of(entry.result()));
        this.inputCount = entry.inputCount();
    }

    public int getInputCount() {
        return inputCount;
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return input;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return output;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return REIPlugin.WOODCUTTING_DISPLAY;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return Optional.empty();
    }

    @Nullable
    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return null;
    }
}
