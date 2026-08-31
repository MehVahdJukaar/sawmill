package net.mehvahdjukaar.sawmill.trades;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicServerResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeSet;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class CarpenterTrades extends DynamicServerResourceProvider {

    public static final CarpenterTrades INSTANCE = new CarpenterTrades();

    private static final int MAX_LEVEL = 5;
    private static final float REPUTATION_DISCOUNT = 0.05f;
    private static final int MAX_USES = 16;

    private static final int[] BUY_XP = {2, 10, 20, 30, 40};
    private static final int[] SELL_XP = {1, 5, 10, 15, 30};

    private CarpenterTrades() {
        super(SawmillMod.res("carpenter_trades"), PackGenerationStrategy.REGEN_ON_EVERY_RELOAD);
    }

    public static void init() {
        RegHelper.registerDynamicResourceProvider(INSTANCE);
    }

    public static Int2ObjectMap<ResourceKey<TradeSet>> tradeSetsByLevel() {
        Int2ObjectMap<ResourceKey<TradeSet>> map = new Int2ObjectOpenHashMap<>();
        for (int level = 1; level <= MAX_LEVEL; level++) {
            map.put(level, SawmillMod.tradeSet("carpenter/level_" + level));
        }
        return map;
    }

    @Override
    protected Collection<String> gatherSupportedNamespaces() {
        return List.of();
    }

    @Override
    protected void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
        executor.accept((manager, sink) -> {
            SimpleTagBuilder[] levelTags = new SimpleTagBuilder[MAX_LEVEL + 1];
            for (int level = 1; level <= MAX_LEVEL; level++) {
                levelTags[level] = SimpleTagBuilder.of(SawmillMod.res("carpenter/level_" + level));
            }

            for (WoodType wood : WoodTypeRegistry.INSTANCE) {
                addWoodTrades(sink, levelTags, wood);
            }

            for (int level = 1; level <= MAX_LEVEL; level++) {
                sink.addTag(levelTags[level], Registries.VILLAGER_TRADE);
            }
        });
    }

    private static void addWoodTrades(ResourceSink sink, SimpleTagBuilder[] levelTags, WoodType wood) {
        Item log = wood.getItemOfThis("log");
        Item strippedLog = wood.getItemOfThis("stripped_log");
        Item planks = wood.getItemOfThis("planks");

        buys(sink, levelTags, wood, "log", 1, log, 4);
        buys(sink, levelTags, wood, "wood", 2, wood.getItemOfThis("wood"), 4);
        buys(sink, levelTags, wood, "sapling", 3, wood.getItemOfThis("sapling"), 8);
        sells(sink, levelTags, wood, "planks", 3, planks, 12);
        sells(sink, levelTags, wood, "chest_boat", 4, wood.getItemOfThis("chest_boat"), 1);
        sells(sink, levelTags, wood, "hanging_sign", 5, wood.getItemOfThis("hanging_sign"), 1);

        if (log != null && strippedLog != null) {
            JsonObject trade = trade(itemJson(log, 4), itemJson(strippedLog, 4), SELL_XP[2]);
            trade.add("additional_wants", itemJson(Items.EMERALD, 1));
            add(sink, levelTags, wood, "stripping", 3, trade);
        }
    }

    private static void buys(ResourceSink sink, SimpleTagBuilder[] levelTags, WoodType wood, String name, int level,
                             Item woodItem, int count) {
        if (woodItem == null) return;
        add(sink, levelTags, wood, name, level, trade(itemJson(woodItem, count), itemJson(Items.EMERALD, 1), BUY_XP[level - 1]));
    }

    private static void sells(ResourceSink sink, SimpleTagBuilder[] levelTags, WoodType wood, String name, int level,
                              Item woodItem, int count) {
        if (woodItem == null) return;
        add(sink, levelTags, wood, name, level, trade(itemJson(Items.EMERALD, 1), itemJson(woodItem, count), SELL_XP[level - 1]));
    }

    private static void add(ResourceSink sink, SimpleTagBuilder[] levelTags, WoodType wood, String name, int level,
                            JsonObject trade) {
        Identifier id = SawmillMod.res("carpenter/" + name + "/" + wood.getAppendableId());
        sink.addJson(id.withPrefix("villager_trade/"), trade, ResType.JSON);
        levelTags[level].add(id);
    }

    private static JsonObject trade(JsonObject wants, JsonObject gives, int xp) {
        JsonObject json = new JsonObject();
        json.add("wants", wants);
        json.add("gives", gives);
        json.addProperty("max_uses", MAX_USES);
        json.addProperty("xp", xp);
        json.addProperty("reputation_discount", REPUTATION_DISCOUNT);
        return json;
    }

    private static JsonObject itemJson(Item item, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("id", Utils.getID(item).toString());
        json.addProperty("count", count);
        return json;
    }
}
