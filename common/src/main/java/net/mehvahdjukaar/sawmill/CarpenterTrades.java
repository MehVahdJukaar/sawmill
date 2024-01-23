package net.mehvahdjukaar.sawmill;

import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.mehvahdjukaar.moonlight.api.misc.StrOpt;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.trades.ItemListingRegistry;
import net.mehvahdjukaar.moonlight.api.trades.ModItemListing;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class CarpenterTrades {

    public static void init() {
        ItemListingRegistry.registerSerializer(SawmillMod.res("wood_item_to_emerald"), WoodToItemListing.CODEC);
    }


    public record WoodToItemListing(boolean buys, String childKey, int woodPrice,
                                    int emeralds, int maxTrades, int xp,
                                    float priceMult, int level,
                                    boolean typeDependant) implements ModItemListing {

        public static final Codec<WoodToItemListing> CODEC =
                RecordCodecBuilder.create(i -> i.group(
                        StrOpt.of(Codec.BOOL, "buys", true).forGetter(WoodToItemListing::buys),
                        Codec.STRING.fieldOf("wood_block").forGetter(WoodToItemListing::childKey),
                        Codec.INT.fieldOf("wood_block_amount").forGetter(WoodToItemListing::woodPrice),
                        Codec.INT.fieldOf("emeralds").forGetter(WoodToItemListing::emeralds),
                        StrOpt.of(ExtraCodecs.POSITIVE_INT, "max_trades", 16).forGetter(WoodToItemListing::maxTrades),
                        StrOpt.of(ExtraCodecs.POSITIVE_INT, "xp").forGetter(w -> Optional.of(w.xp)),
                        StrOpt.of(ExtraCodecs.POSITIVE_FLOAT, "price_multiplier", 0.05f).forGetter(WoodToItemListing::priceMult),
                        StrOpt.of(Codec.intRange(1, 5), "level", 1).forGetter(WoodToItemListing::level),
                        StrOpt.of(Codec.BOOL, "type_dependant", false).forGetter(WoodToItemListing::typeDependant)
                ).apply(i, WoodToItemListing::createDefault));

        //lazy
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

        private static void maybeAddWood(List<WoodType> list, String... names) {
            for (var t : WoodTypeRegistry.getTypes()) {
                for (var name : names) {
                    if (t.id.getPath().contains(name)) {
                        list.add(t);
                    }
                }
            }
        }

        private static WoodType wood(String name) {
            return WoodTypeRegistry.getValue(new ResourceLocation(name));
        }

        private static WoodToItemListing createDefault(boolean buys, String wood, int woodAmount, int emeralds, int maxTrades,
                                                       Optional<Integer> xp, float priceMult, int level, boolean typeDependant) {
            return new WoodToItemListing(buys, wood, woodAmount, emeralds, maxTrades,
                    xp.orElse(ModItemListing.defaultXp(buys, level)), priceMult, level, typeDependant);
        }

        public boolean isValid() {
            return WoodTypeRegistry.OAK_TYPE.getItemOfThis(childKey) != null;
        }

        @Override
        public Codec<? extends ModItemListing> getCodec() {
            return CODEC;
        }

        @Nullable
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            WoodType type = null;
            if (typeDependant) {
                if (trader instanceof VillagerDataHolder d) {
                    List<WoodType> list = TYPE_MAP.get().getOrDefault(d.getVillagerData().getType(), List.of(WoodTypeRegistry.OAK_TYPE));
                    type = list.get(random.nextInt(list.size()));
                } else return null;
            }
            var types = new ArrayList<>(WoodTypeRegistry.getTypes());
            int tries = 0;
            while (tries < 100) {
                tries++;
                if (type == null) {
                    type = types.get(random.nextInt(types.size()));
                }
                types.remove(type);
                Item w = type.getItemOfThis(childKey);
                if (w != null) {
                    ItemStack wood = new ItemStack(w, woodPrice);
                    ItemStack emerald = new ItemStack(Items.EMERALD, emeralds);
                    if (buys) {
                        return new MerchantOffer(wood, ItemStack.EMPTY, emerald, maxTrades, xp, priceMult);
                    } else {
                        return new MerchantOffer(emerald, ItemStack.EMPTY, wood, maxTrades, xp, priceMult);
                    }
                }
            }
            return null;
        }

        @Override
        public int getLevel() {
            return level;
        }
    }
}
