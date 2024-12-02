package net.mehvahdjukaar.sawmill;

import com.google.common.collect.ImmutableSet;
import dev.architectury.injectables.annotations.ExpectPlatform;
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
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;

public class SawmillMod {
    public static final String MOD_ID = "sawmill";

    public static final Logger LOGGER = LogManager.getLogger("Sawmill");

    public static final Supplier<Block> SAWMILL_BLOCK = RegHelper.registerBlockWithItem(
            res("sawmill"), SawmillBlock::new);

    public static final Supplier<MenuType<SawmillMenu>> SAWMILL_MENU = RegHelper.registerMenuType(
            res("sawmill"), SawmillMenu::new);

    public static final Supplier<SoundEvent> SAWMILL_TAKE = RegHelper.registerSound(res("ui.sawmill.take_result"));
    public static final Supplier<SoundEvent> SAWMILL_SELECT = RegHelper.registerSound(res("ui.sawmill.select_recipe"));
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

    public static final TagKey<Item> BLACKLIST = TagKey.create(Registries.ITEM, res("blacklist"));

    private static Supplier<VillagerProfession> registerVillager(String name, ResourceKey<PoiType> jobSite, Supplier<SoundEvent> workSound) {
        return RegHelper.register(res(name), () -> new VillagerProfession(name,
                        (holder) -> holder.is(jobSite),
                        (holder) -> holder.is(jobSite),
                        ImmutableSet.of(), ImmutableSet.of(), workSound.get()),
                Registries.VILLAGER_PROFESSION);
    }

    public static void init() {
        if (PlatHelper.getPhysicalSide().isClient()) {
            SawmillClient.init();
        }
        NetworkStuff.init();
        CarpenterTrades.init();
        CommonConfigs.init();
        RegHelper.addItemsToTabsRegistration(event ->
                event.addAfter(CreativeModeTabs.FUNCTIONAL_BLOCKS,
                        stack -> stack.is(Items.STONECUTTER),
                        SAWMILL_BLOCK.get().asItem()));

        PlatHelper.addServerReloadListener(SawmillRecipeGenerator.INSTANCE, res("recipe_generator"));
    }

    public static ResourceLocation res(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    // Hacky tag stuff below here. way more complex than it needs to be because prople reported some issue with recipes breaking or something
    // I have no idea where those come from so i just made it as complex as possible to avoid any possible issues

    private static final Object lock = new Object();
    private static boolean receivedTags = false;
    private static final Map<ResourceLocation, Collection<Holder<?>>> tags = new HashMap<>();
    private static final Map<TagKey<Item>, List<ItemStack>> cachedTags = new HashMap<>();
    private static final Map<RecipeType<?>, Boolean> cachedWhitelist = new HashMap<>();
    private static final List<Holder<RecipeType<?>>> whitelist = new ArrayList<>();

    public static Collection<ItemStack> getTagElements(TagKey<Item> tag) {
        return cachedTags.computeIfAbsent(tag, t -> {
            var tagList = tags.get(t.location());
            if (tagList == null) {
                return List.of();
            }
            return tagList.stream().map(h -> ((Item) h.value()).getDefaultInstance())
                    .toList();
        });
    }

    public static void clearTagHacks() {
        whitelist.clear();
        cachedTags.clear();
        cachedWhitelist.clear();
        receivedTags = false;
    }

    public static boolean isWhitelisted(RecipeHolder<?> recipe) {
        boolean ret = cachedWhitelist.computeIfAbsent(recipe.value().getType(),
                recipeType -> whitelist.stream().anyMatch(h -> h.value() == recipeType));
        if (ret) {
            if (CommonConfigs.MOD_BLACKLIST.get().contains(recipe.id().getNamespace())) return false;
        }
        return ret;
    }

    public static void setTagManagerResults(List<TagManager.LoadResult<?>> results) {
        if (!CommonConfigs.SAVE_RECIPES.get() && !CommonConfigs.DYNAMIC_RECIPES.get()) return;
        //actually here we are already on main thread so this isn't even needed.....
        synchronized (lock) {
            tags.clear();
            for (var r : results) {
                if (r.key() == Registries.ITEM) {
                    for (var e : r.tags().entrySet()) {
                        tags.computeIfAbsent(e.getKey(), y -> new ArrayList<>())
                                .addAll(e.getValue());
                    }
                    break;
                }
            }
            for (var r : results) {
                if (r.key() == Registries.RECIPE_TYPE) {
                    whitelist.addAll(r.tags().get(res("whitelist"))
                            .stream().map(holder -> (Holder<RecipeType<?>>) holder).toList());
                    break;
                }
            }
            receivedTags = true;
            lock.notifyAll();
        }
        SawmillMod.LOGGER.info("Intercepted tag results");
    }


    public static void waitForTags() {
        // wait for tags to be ready so we don't initialize some recipes with unfinished tags or some shit
        synchronized (lock) {
            if (!receivedTags) {
                try {
                    SawmillMod.LOGGER.info("Waiting for tags");
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Sawmill error:", e);
                }
            }
        }
    }

    @ExpectPlatform
    public static boolean isVanillaIngredient(Ingredient ing) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Object getCustomIngredient(Ingredient ing){
        throw new AssertionError();
    }
}
