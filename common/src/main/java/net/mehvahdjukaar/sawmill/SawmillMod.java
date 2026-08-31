package net.mehvahdjukaar.sawmill;

import com.google.common.collect.ImmutableSet;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.sawmill.trades.CarpenterTrades;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.function.Supplier;

public class SawmillMod {
    public static final String MOD_ID = "sawmill";

    public static final Logger LOGGER = LogManager.getLogger("Sawmill");

    public static final Supplier<Block> SAWMILL_BLOCK = RegHelper.registerBlockWithItem(
            res("sawmill"), SawmillBlock::new, BlockBehaviour.Properties.of()
                    .destroyTime(2.5f)
                    .explosionResistance(2.5f)
                    .sound(SoundType.WOOD)
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS));

    public static final Supplier<MenuType<SawmillMenu>> SAWMILL_MENU = RegHelper.registerMenuType(
            res("sawmill"), SawmillMenu::new);

    public static final Supplier<SoundEvent> SAWMILL_TAKE = RegHelper.registerSound(res("ui.sawmill.take_result"));
    public static final Supplier<SoundEvent> SAWMILL_SELECT = RegHelper.registerSound(res("ui.sawmill.select_recipe"));
    public static final Supplier<SoundEvent> CARPENTER_WORK = RegHelper.registerSound(res("entity.villager.work_carpenter"));

    public static final Supplier<RecipeSerializer<WoodcuttingRecipe>> WOODCUTTING_RECIPE_SERIALIZER = RegHelper.registerRecipeSerializer(
            res("woodcutting"), () -> new RecipeSerializer<>(WoodcuttingRecipe.MAP_CODEC, WoodcuttingRecipe.STREAM_CODEC));

    public static final Supplier<RecipeType<WoodcuttingRecipe>> WOODCUTTING_RECIPE = RegHelper.registerRecipeType(
            res("woodcutting"));

    public static final ResourceKey<PoiType> CARPENTER_POI_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,
            res("carpenter"));

    public static final Supplier<PoiType> CARPENTER_POI = RegHelper.registerPOI(res("carpenter"),
            () -> new PoiType(new HashSet<>(SAWMILL_BLOCK.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final Supplier<VillagerProfession> CARPENTER = RegHelper.registerVillagerProfession(
            res("carpenter"),
            holder -> holder.is(CARPENTER_POI_KEY), holder -> holder.is(CARPENTER_POI_KEY),
            ImmutableSet.of(), ImmutableSet.of(),
            CARPENTER_WORK, CarpenterTrades.tradeSetsByLevel());

    public static final TagKey<Item> BLACKLIST = TagKey.create(Registries.ITEM, res("blacklist"));

    public static final TagKey<RecipeType<?>> RECIPE_WHITELIST = TagKey.create(Registries.RECIPE_TYPE, res("whitelist"));

    public static void init() {
        if (PlatHelper.getPhysicalSide().isClient()) {
            SawmillClient.init();
        }
        NetworkStuff.init();
        CommonConfigs.init();
        RegHelper.registerSimpleRecipeCondition(res("flag"), flag -> {
            if (flag.equals("rs_compat")) return CommonConfigs.RS_COMPAT.get();
            return false;
        });
        RegHelper.addItemsToTabsRegistration(event ->
                event.addAfter(CreativeModeTabs.FUNCTIONAL_BLOCKS,
                        stack -> stack.is(Items.STONECUTTER),
                        SAWMILL_BLOCK.get().asItem()));

        SawmillRecipeGenerator.init();
        CarpenterTrades.init();
    }

    public static Identifier res(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    public static ResourceKey<TradeSet> tradeSet(String path) {
        return ResourceKey.create(Registries.TRADE_SET, res(path));
    }
}
