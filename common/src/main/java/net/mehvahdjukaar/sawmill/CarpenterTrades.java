package net.mehvahdjukaar.sawmill;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.mehvahdjukaar.moonlight.api.misc.ModItemListing;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.npc.VillagerTrades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CarpenterTrades extends SimpleJsonResourceReloadListener {

    public static final CarpenterTrades INSTANCE = new CarpenterTrades(new Gson(), "carpenter_trades");

    public CarpenterTrades(Gson gson, String name) {
        super(gson, name);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<VillagerTrades.ItemListing> trades = new ArrayList<>();
        for (var e : jsons.entrySet()) {
            var j = e.getValue();
            var id = e.getKey();
            VillagerTrades.ItemListing trade = ModItemListing.CODEC.decode(JsonOps.INSTANCE, j)
                    .getOrThrow(false, errorMsg -> SawmillMod.LOGGER.warn("Failed to parse red merchant trade with id {} - error: {}",
                            id, errorMsg)).getFirst();

            trades.add(trade);
        }

        Collections.shuffle(trades);

        var map = new Int2ObjectArrayMap<VillagerTrades.ItemListing[]>();
        map.put(1, trades.toArray(VillagerTrades.ItemListing[]::new));
        VillagerTrades.TRADES.put(SawmillMod.CARPENTER.get(), map);
    }
}
