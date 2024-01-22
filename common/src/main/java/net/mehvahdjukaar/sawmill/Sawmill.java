package net.mehvahdjukaar.sawmill;

import com.google.common.collect.ImmutableSet;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Supplier;

public class Sawmill {
    public static final String MOD_ID = "sawmill";

    public static final Logger LOGGER = LogManager.getLogger("Sawmill");

    public static final Supplier<Block> SAWMILL_BLOCK = RegHelper.registerBlockWithItem(
            res("sawmill"), SawmillBlock::new);
    public static final Supplier<MenuType<SawmillMenu>> SAWMILL_MENU = RegHelper.registerMenuType(
            res("sawmill"), SawmillMenu::new);
    public static final Supplier<RecipeSerializer<SawmillRecipe>> SAWMILL_RECIPE_SERIALIZER = RegHelper.registerRecipeSerializer(
            res("woodcutting"), SawmillRecipe.Serializer::new);
    public static final Supplier<RecipeType<SawmillRecipe>> SAWMILL_RECIPE = RegHelper.registerRecipeType(
            res("woodcutting"));
    public static final ResourceKey<PoiType> LUMBERJACK_POI_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,
            res("carpenter"));
    public static final Supplier<PoiType> LUMBERJACK_POI = RegHelper.registerPOI(res("carpenter"),
            () -> new PoiType(new HashSet<>(SAWMILL_BLOCK.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final Supplier<VillagerProfession> CARPENTER = registerVillager(
            "carpenter", LUMBERJACK_POI_KEY, SoundEvents.VILLAGER_WORK_WEAPONSMITH);

    private static Supplier<VillagerProfession> registerVillager(String name, ResourceKey<PoiType> jobSite, @Nullable SoundEvent workSound) {
        return RegHelper.register(res(name), () -> new VillagerProfession(name,
                        (holder) -> holder.is(jobSite),
                        (holder) -> holder.is(jobSite),
                        ImmutableSet.of(), ImmutableSet.of(), workSound),
                Registries.VILLAGER_PROFESSION);
    }

    public static void init() {
        if (PlatHelper.getPhysicalSide().isClient()) {
            SawmillClient.init();
        }
        RegHelper.addItemsToTabsRegistration(event ->
                event.addAfter(CreativeModeTabs.FUNCTIONAL_BLOCKS,
                        stack -> stack.is(Items.STONECUTTER),
                        SAWMILL_BLOCK.get().asItem()));

        PlatHelper.addServerReloadListener(CarpenterTrades.INSTANCE, res("carpenter_trades"));
    }

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    @Nullable
    private static WeakReference<TagManager> tagManager = null;
    private static WeakReference<Map<ResourceLocation, Collection<Holder>>> tags = null;
    private static Map<TagKey<Item>, List<ItemStack>> cachedTags = new HashMap<>();

    public static void setTagManager(TagManager t) {
        tagManager = new WeakReference<>(t);
    }

    public static Collection<ItemStack> getTagElements(TagKey<Item> tag) {
        if (tags == null) {
            TagManager manager = Sawmill.tagManager.get();
            if (manager != null) {
                for (var r : manager.getResult()) {
                    if (r.key() == Registries.ITEM) {
                        tags = new WeakReference<>((Map<ResourceLocation, Collection<Holder>>) (Object) r.tags());
                    }
                }
            }
        }

        if (tags == null) {
            return List.of();
        }
        return cachedTags.computeIfAbsent(tag, t -> {
            var tagList = tags.get().get(t.location());
            if (tagList == null) {
                return List.of();
            }
            return tagList.stream().map(h -> ((Item) h.value()).getDefaultInstance())
                    .toList();
        });
    }

    public static void clearCacheHacks() {
        tagManager = null;
        tags = null;
        cachedTags.clear();
    }
}
