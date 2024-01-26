package net.mehvahdjukaar.sawmill;

import com.google.common.collect.ImmutableSet;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.sawmill.trades.ItemListingRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Supplier;

public class SawmillMod {
    public static final String MOD_ID = "sawmill";

    public static final Logger LOGGER = LogManager.getLogger("Sawmill");

    public static final Supplier<Block> SAWMILL_BLOCK = RegHelper.registerBlockWithItem(
            res("sawmill"), SawmillBlock::new, CreativeModeTab.TAB_DECORATIONS);
    public static final Supplier<MenuType<SawmillMenu>> SAWMILL_MENU = RegHelper.registerMenuType(
            res("sawmill"), SawmillMenu::new);
    public static final Supplier<SoundEvent> SAWMILL_TAKE = RegHelper.registerSound(res("ui.sawmill.take_result"));
    public static final Supplier<SoundEvent> SAWMILL_SELECT = RegHelper.registerSound(res("ui.sawmill.select_result"));
    public static final Supplier<SoundEvent> CARPENTER_WORK = RegHelper.registerSound(res("entity.villager.work_carpenter"));
    public static final Supplier<RecipeSerializer<WoodcuttingRecipe>> WOODCUTTING_RECIPE_SERIALIZER = RegHelper.registerRecipeSerializer(
            res("woodcutting"), WoodcuttingRecipe.Serializer::new);
    public static final Supplier<RecipeType<WoodcuttingRecipe>> WOODCUTTING_RECIPE = RegHelper.registerRecipeType(
            res("woodcutting"));
    public static final ResourceKey<PoiType> CARPENTER_POI_KEY = ResourceKey.create(Registry.POINT_OF_INTEREST_TYPE_REGISTRY,
            res("carpenter"));
    public static final Supplier<PoiType> CARPENTER_POI = RegHelper.register(res("carpenter"),
            () -> new PoiType(new HashSet<>(SAWMILL_BLOCK.get().getStateDefinition().getPossibleStates()), 1, 1),
            Registry.POINT_OF_INTEREST_TYPE);
    public static final Supplier<VillagerProfession> CARPENTER = registerVillager(
            "carpenter", CARPENTER_POI_KEY, CARPENTER_WORK);

    private static Supplier<VillagerProfession> registerVillager(String name, ResourceKey<PoiType> jobSite, Supplier<SoundEvent> workSound) {
        return RegHelper.register(res(name), () -> new VillagerProfession(name,
                        (holder) -> holder.is(jobSite),
                        (holder) -> holder.is(jobSite),
                        ImmutableSet.of(), ImmutableSet.of(), workSound.get()),
                Registry.VILLAGER_PROFESSION);
    }

    public static void init() {
        if (PlatformHelper.getEnv().isClient()) {
            SawmillClient.init();
        }
        CarpenterTrades.init();
        CommonConfigs.init();
        PlatformHelper.addServerReloadListener(ItemListingRegistry.INSTANCE, res("carpenter_trades"));
        BlockSetAPI.addDynamicRegistration(SawmillMod::forceLoadWoodTypes, WoodType.class, Registry.BLOCK);
    }

    private static <E, T extends BlockType> void forceLoadWoodTypes(Registrator<E> eRegistrator, Collection<T> ts) {
    }

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    //Hacky tag stuff below here

    @Nullable
    private static WeakReference<TagManager> tagManager = null;
    private static WeakReference<Map<ResourceLocation, Collection<Holder>>> tags = null;
    private static final Map<TagKey<Item>, List<ItemStack>> cachedTags = new HashMap<>();
    private static final List<RecipeType<?>> whitelist = new ArrayList<>();

    public static void setTagManager(TagManager t) {
        tagManager = new WeakReference<>(t);
    }

    public static Collection<ItemStack> getTagElements(TagKey<Item> tag) {
        if (tags == null) {
            TagManager manager = SawmillMod.tagManager.get();
            if (manager != null) {
                for (var r : manager.getResult()) {
                    if (r.key() == Registry.ITEM_REGISTRY) {
                        tags = new WeakReference<>((Map<ResourceLocation, Collection<Holder>>) (Object) r.tags());
                        break;
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
        whitelist.clear();
        cachedTags.clear();
    }

    public static boolean isWhitelisted(RecipeType<?> type) {
        if (whitelist.isEmpty()) {
            TagManager manager = SawmillMod.tagManager.get();
            if (manager != null) {
                for (var r : manager.getResult()) {
                    if (r.key() == Registry.RECIPE_TYPE_REGISTRY) {
                        whitelist.addAll(r.tags().get(res("whitelist"))
                                .stream().map(holder -> (RecipeType<?>) holder.value()).toList());
                        break;
                    }
                }
            }
        }
        return whitelist.contains(type);

    }
}
