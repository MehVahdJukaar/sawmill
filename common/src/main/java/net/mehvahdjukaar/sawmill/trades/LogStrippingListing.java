package net.mehvahdjukaar.sawmill.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.trades.ModItemListing;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public record LogStrippingListing(ItemCost price, int amount, int maxTrades, int xp,
                                  float priceMult, int level,
                                  BiomeToWoodList biomeWoods) implements ModItemListing {

    public static final MapCodec<LogStrippingListing> CODEC =
            RecordCodecBuilder.mapCodec(i -> i.group(
                    ItemCost.CODEC.fieldOf("price").forGetter(LogStrippingListing::price),
                    Codec.INT.fieldOf("amount").forGetter(LogStrippingListing::amount),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_trades", 16).forGetter(LogStrippingListing::maxTrades),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("xp").forGetter(w -> Optional.of(w.xp)),
                    ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("price_multiplier", 0.05f).forGetter(LogStrippingListing::priceMult),
                    Codec.intRange(1, 5).optionalFieldOf("level", 1).forGetter(LogStrippingListing::level),
                    BiomeToWoodList.CODEC.optionalFieldOf("biome_woods", new BiomeToWoodList(Map.of())).forGetter(LogStrippingListing::biomeWoods)
            ).apply(i, LogStrippingListing::createDefault));

    private static LogStrippingListing createDefault(ItemCost price, int amount, int maxTrades,
                                                     Optional<Integer> xp, float priceMult, int level,
                                                     @Nullable BiomeToWoodList biomeWoods) {
        return new LogStrippingListing(price, amount, maxTrades,
                xp.orElse(ModItemListing.defaultXp(false, level)), priceMult, level, biomeWoods);
    }

    @Override
    public MapCodec<? extends ModItemListing> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        WoodType type = biomeWoods.getRandomWood(trader, random);
        if (type == null) return null;
        Item log = type.log.asItem();
        Item stripped = type.getItemOfThis("stripped_log");
        if (stripped != null) {
            return new MerchantOffer(new ItemCost(log, amount), Optional.of(price), new ItemStack(stripped, amount), maxTrades, xp, priceMult);
        }
        return null;
    }

    @Override
    public int getLevel() {
        return level;
    }
}
