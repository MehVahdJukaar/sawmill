package net.mehvahdjukaar.sawmill.trades;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.trades.ItemListingManager;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerType;

import java.util.*;
import java.util.function.Supplier;

public final class CarpenterTrades {

    public static void init() {
        ItemListingManager.registerSerializer(SawmillMod.res("random_wood_item_to_emerald"), RandomWoodToItemListing.CODEC);
        ItemListingManager.registerSerializer(SawmillMod.res("wood_item_to_emerald"), BiomeWoodToItemListing.CODEC);
        ItemListingManager.registerSerializer(SawmillMod.res("log_stripping"), LogStrippingListing.CODEC);
    }


    //lazy

    private static WoodType wood(String name) {
        return WoodTypeRegistry.INSTANCE.get(ResourceLocation.parse(name));
    }

    private static void maybeAddWood(List<WoodType> list, String... names) {
        for (var t : WoodTypeRegistry.INSTANCE) {
            for (var name : names) {
                if (t.id.getPath().contains(name)) {
                    list.add(t);
                }
            }
        }


    }


    private static final Supplier<Map<VillagerType, List<WoodType>>> TYPE_MAP = Suppliers.memoize(() -> {
        Map<VillagerType, List<WoodType>> map = new HashMap<>();
        map.put(VillagerType.PLAINS, List.of(wood("birch"), wood("oak")));
        map.put(VillagerType.JUNGLE, List.of(wood("jungle"), wood("bamboo")));
        map.put(VillagerType.SNOW, List.of(wood("spruce")));
        map.put(VillagerType.TAIGA, List.of(wood("spruce")));
        map.put(VillagerType.SAVANNA, List.of(wood("acacia")));
        var desertList = new ArrayList<WoodType>();
        maybeAddWood(desertList, "cactus");
        if (desertList.isEmpty()) desertList.add(wood("jungle"));
        map.put(VillagerType.DESERT, desertList);
        var swampList = new ArrayList<WoodType>();
        maybeAddWood(swampList, "swamp", "cypress");
        if (swampList.isEmpty()) swampList.add(wood("dark_oak"));
        map.put(VillagerType.SWAMP, swampList);

        return map;
    });

}
