package net.mehvahdjukaar.sawmill.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.mehvahdjukaar.sawmill.RecipeSorter;
import net.mehvahdjukaar.sawmill.SawmillClient;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.VillageStructureModifier;
import net.minecraft.world.item.crafting.Ingredient;

public class SawmillModImpl implements ModInitializer {

    @Override
    public void onInitialize() {
        SawmillMod.init();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> VillageStructureModifier.setup(server.registryAccess()));
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (client) SawmillClient.onTagsUpdated();
        });
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((p, manager) -> RecipeSorter.sendOrderToClient(p));
    }

    public static boolean isVanillaIngredient(Ingredient ing) {
        return ing.getCustomIngredient() == null;
    }

    public static Object getCustomIngredient(Ingredient ing) {
        return ing.getCustomIngredient();
    }

}
