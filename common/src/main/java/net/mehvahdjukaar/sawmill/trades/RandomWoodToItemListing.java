package net.mehvahdjukaar.sawmill.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodTypes;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.trades.ModItemListing;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record RandomWoodToItemListing(boolean buys, String childKey, int woodPrice,
                                      ItemCost emeralds, int maxTrades, int xp,
                                      float priceMult, int level,
                                      List<WoodType> blacklist) implements ModItemListing {

    public static final MapCodec<RandomWoodToItemListing> CODEC =
            RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.BOOL.optionalFieldOf("buys", true).forGetter(RandomWoodToItemListing::buys),
                    Codec.STRING.fieldOf("wood_block").forGetter(RandomWoodToItemListing::childKey),
                    ExtraCodecs.POSITIVE_INT.fieldOf("wood_block_amount").forGetter(RandomWoodToItemListing::woodPrice),
                    ItemCost.CODEC.fieldOf("emeralds").forGetter(RandomWoodToItemListing::emeralds),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_trades", 16).forGetter(RandomWoodToItemListing::maxTrades),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("xp").forGetter(w -> Optional.of(w.xp)),
                    ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("price_multiplier", 0.05f).forGetter(RandomWoodToItemListing::priceMult),
                    Codec.intRange(1, 5).optionalFieldOf("level", 1).forGetter(RandomWoodToItemListing::level),
                    WoodTypeRegistry.INSTANCE.getCodec().listOf().optionalFieldOf("wood_blacklist", List.of()).forGetter(RandomWoodToItemListing::blacklist)
            ).apply(i, RandomWoodToItemListing::createDefault));


    private static RandomWoodToItemListing createDefault(boolean buys, String wood, int woodAmount, ItemCost emeralds, int maxTrades,
                                                         Optional<Integer> xp, float priceMult, int level,
                                                         List<WoodType> blacklist) {
        return new RandomWoodToItemListing(buys, wood, woodAmount, emeralds, maxTrades,
                xp.orElse(ModItemListing.defaultXp(buys, level)), priceMult, level, blacklist);
    }

    public boolean isValid() {
        return VanillaWoodTypes.OAK.getItemOfThis(childKey) != null;
    }

    @Override
    public MapCodec<? extends ModItemListing> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        var types = new ArrayList<>(WoodTypeRegistry.INSTANCE.getValues());
        while (!types.isEmpty()) {
            WoodType type = types.remove(random.nextInt(types.size()));
            if (blacklist.contains(type)) continue;
            Item woodItem = type.getItemOfThis(childKey);
            if (woodItem != null && woodItem != Items.AIR) {
                ItemCost itemCost = new ItemCost(woodItem, woodPrice);
                ItemCost emerald = emeralds;
                if (buys) {
                    return new MerchantOffer(itemCost, Optional.empty(), emerald.itemStack(), maxTrades, xp, priceMult);
                } else {
                    return new MerchantOffer(emerald, Optional.empty(), itemCost.itemStack(), maxTrades, xp, priceMult);
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
