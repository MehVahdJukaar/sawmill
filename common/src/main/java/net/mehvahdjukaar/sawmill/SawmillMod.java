package net.mehvahdjukaar.sawmill;

import com.google.common.collect.ImmutableSet;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Supplier;

public class SawmillMod {
    public static final String MOD_ID = "sawmill";

    public static final Logger LOGGER = LogManager.getLogger("Sawmill");

    public static final boolean KUBEJS = PlatHelper.isModLoaded("kubejs");

    public static final Supplier<Block> SAWMILL_BLOCK = RegHelper.registerBlockWithItem(
            res("sawmill"), SawmillBlock::new);
    public static final Supplier<MenuType<SawmillMenu>> SAWMILL_MENU = RegHelper.registerMenuType(
            res("sawmill"), SawmillMenu::new);
    public static final Supplier<SoundEvent> SAWMILL_TAKE = RegHelper.registerSound(res("ui.sawmill.take_result"));
    public static final Supplier<SoundEvent> SAWMILL_SELECT = RegHelper.registerSound(res("ui.sawmill.select_result"));
    public static final Supplier<SoundEvent> CARPENTER_WORK = RegHelper.registerSound(res("entity.villager.work_carpenter"));
    public static final Supplier<RecipeSerializer<WoodcuttingRecipe>> WOODCUTTING_RECIPE_SERIALIZER = RegHelper.registerRecipeSerializer(
            res("woodcutting"), WoodcuttingRecipe.Serializer::new);
    public static final Supplier<RecipeType<WoodcuttingRecipe>> WOODCUTTING_RECIPE = RegHelper.registerRecipeType(
            res("woodcutting"));
    public static final ResourceKey<PoiType> CARPENTER_POI_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,
            res("carpenter"));
    public static final Supplier<PoiType> CARPENTER_POI = RegHelper.registerPOI(res("carpenter"),
            () -> new PoiType(new HashSet<>(SAWMILL_BLOCK.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final Supplier<VillagerProfession> CARPENTER = registerVillager(
            "carpenter", CARPENTER_POI_KEY, CARPENTER_WORK);

    private static Supplier<VillagerProfession> registerVillager(String name, ResourceKey<PoiType> jobSite, Supplier<SoundEvent> workSound) {
        return RegHelper.register(res(name), () -> new VillagerProfession(name,
                        (holder) -> holder.is(jobSite),
                        (holder) -> holder.is(jobSite),
                        ImmutableSet.of(), ImmutableSet.of(), workSound.get()),
                Registries.VILLAGER_PROFESSION);
    }

    public static void init() {
        //TODO: add searchbar
        if (PlatHelper.getPhysicalSide().isClient()) {
            SawmillClient.init();
        }
        CarpenterTrades.init();
        CommonConfigs.init();
        RegHelper.addItemsToTabsRegistration(event ->
                event.addAfter(CreativeModeTabs.FUNCTIONAL_BLOCKS,
                        stack -> stack.is(Items.STONECUTTER),
                        SAWMILL_BLOCK.get().asItem()));
    }

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    //Hacky tag stuff below here

    @Nullable
    private static WeakReference<TagManager> tagManager = null;
    private static Map<ResourceLocation, List<Holder>> tags = null;
    private static final Map<TagKey<Item>, List<ItemStack>> cachedTags = new HashMap<>();
    private static final List<RecipeType<?>> whitelist = new ArrayList<>();

    public static void setTagManager(TagManager t) {
        tagManager = new WeakReference<>(t);
    }

    public static Collection<ItemStack> getTagElements(TagKey<Item> tag) {
        if (tags == null) {
            if (SawmillMod.tagManager != null) {
                TagManager manager = SawmillMod.tagManager.get();
                if(manager !=null) {
                    tags = new HashMap<>();
                    for (var r : manager.getResult()) {
                        if (r.key() == Registries.ITEM) {
                            for (var e : r.tags().entrySet()) {
                                tags.computeIfAbsent(e.getKey(),
                                                y -> new ArrayList<>())
                                        .addAll(e.getValue());
                            }
                            break;
                        }
                    }
                }
            }
            return List.of();
        }
        return cachedTags.computeIfAbsent(tag, t -> {
            var tagList = tags.get(t.location());
            if (tagList == null) {
                return List.of();
            }
            return tagList.stream().map(h -> ((Item) h.value()).getDefaultInstance())
                    .toList();
        });
    }

    public static void clearCacheHacks() {
        tagManager = null;
        tags.clear();
        whitelist.clear();
        cachedTags.clear();
    }

    public static boolean isWhitelisted(RecipeType<?> type) {
        if (whitelist.isEmpty()) {
            TagManager manager = SawmillMod.tagManager.get();
            if (manager != null) {
                for (var r : manager.getResult()) {
                    if (r.key() == Registries.RECIPE_TYPE) {
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
