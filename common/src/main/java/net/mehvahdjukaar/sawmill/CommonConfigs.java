package net.mehvahdjukaar.sawmill;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CommonConfigs {

    public static final Supplier<Boolean> ALLOW_NON_BLOCKS;
    public static final Supplier<Boolean> ALLOW_NON_VARIANTS;
    public static final Supplier<Boolean> PLANKS_ONLY_ONE;
    public static final Supplier<Boolean> WIDE_GUI;
    public static final Supplier<GenMode> GEN_MODE;
    public static final Supplier<List<String>> MOD_BLACKLIST;
    public static final Supplier<Map<String, Double>> SPECIAL_COSTS;
    public static final Supplier<SearchMode> SEARCH_MODE;
    public static final Supplier<Integer> SEARCH_BAR_THRESHOLD;
    public static final Supplier<Double> MAX_DISCOUNT;
    public static final Supplier<Boolean> SORT_RECIPES;
    public static final Supplier<Boolean> RS_COMPAT;

    public static final ModConfigHolder CONFIG;

    static {

        ConfigBuilder builder = ConfigBuilder.create(SawmillMod.MOD_ID, ConfigType.COMMON_SYNCED);

        builder.push("general");
        SORT_RECIPES = builder.comment("Sort recipes following cretive tab order. " +
                        "Could cause issue in the case when, for whatever reason, creative other would differ from server to client." +
                        "Additionally Neoforge has a bug where this stuff won't work on servers. Use Forge instead!")
                .define("sort_recipes", true);
        GEN_MODE = builder.comment("""
                        \nHow dynamic assets are generated. If cached the cache will regenerate once any mod or pack changes
                        - NEVER: This mod will never attempt to generate the cache folder. The assets will be put in memory
                        - CACHED: create a CACHE folder via .minecraft/dynamic-resource-pack-cache
                        - CACHED_ZIPPED: create a ZIP folder via .minecraft/dynamic-resource-pack-cache
                        - ALWAYS: Will always generate the assets & will be stored in memory. There will be no cache folder""")
                .define("dynamic_assets_generation_mode", GenMode.CACHED_ZIPPED);

        ALLOW_NON_BLOCKS = builder.comment("Allow crafting non-block items")
                .define("allow_non_blocks", true);
        ALLOW_NON_VARIANTS = builder.comment("Allows crafting non wood variant items (crafting table for example)")
                .define("allow_non_variant", true);
        MOD_BLACKLIST = builder.comment("List of Mod ids you want to completely exclude from sawmill recipes. " +
                        "All recipes from these mods will be ignored. Use to remove mods with many recipes as sawmill can display at most 255 at once" +
                        "For more fine control use the sawmill:blacklist item tag")
                .define("mods_blacklist", List.of("framedblocks"));
        PLANKS_ONLY_ONE = builder.comment("Makes so planks recipes can only craft items with 1 input plank. Disable to allow more recipes")
                .define("limit_planks_input_to_one", true);
        WIDE_GUI = builder.comment("Makes Sawmill GUI slightly wider")
                .define("wider_gui", false);
        SEARCH_MODE = builder
                .comment("Determines if GUI will have search bar or not. Automatic only enables the bar statically when you have enough mods that add wood recipes. Dynamic changes the gui dynamically depending on how many recipe its displaying")
                .define("search_bar_mode", SearchMode.AUTOMATIC);
        SEARCH_BAR_THRESHOLD = builder.comment("At how many recipes the search bar should appear")
                .define("search_bar_threshold", 32, 0, 200);

        SPECIAL_COSTS = builder.comment("This is a map of wood object type to its cost in planks. Its used to add some discount to some special blocks." +
                        "Change this if say you want all fences to cost 1 plank. Not all keys will work here bt you can try modded ones if you hae Every Compat")
                .defineObject("special_recipe_costs", () -> new HashMap<>(Map.of("stairs", 1d,
                                "boat", 4d)),
                        Codec.unboundedMap(Codec.STRING, Codec.DOUBLE));
        MAX_DISCOUNT = builder.comment("Maximum discount that sawmill will given when converting recipes. Unit is percentage of input item")
                .define("max_discount", 0.35, 0, 1);

        RS_COMPAT = builder.comment("Enables and disables compat structures for Repurposed Structures mod")
                .define("repurposed_structures_compat", true);
        builder.pop();

        CONFIG = builder.build();
        CONFIG.forceLoad();
    }

    public static void init() {
    }

    public static boolean hasSearchBar(int recipeCount) {
        var s = SEARCH_MODE.get();
        return switch (s) {
            case ON -> true;
            case OFF -> false;
            case AUTOMATIC -> SawmillClient.hasManyRecipes();
            case DYNAMIC -> recipeCount > SEARCH_BAR_THRESHOLD.get();
        };
    }

    public enum SearchMode {
        OFF, ON, AUTOMATIC, DYNAMIC
    }

    public enum GenMode {
        NEVER, CACHED, CACHED_ZIPPED, ALWAYS;

        public PackGenerationStrategy getStrategy() {
            return switch (this) {
                case NEVER -> PackGenerationStrategy.NO_OP;
                case CACHED -> PackGenerationStrategy.CACHED;
                case CACHED_ZIPPED -> PackGenerationStrategy.CACHED_ZIPPED;
                case ALWAYS -> PackGenerationStrategy.REGEN_ON_EVERY_RELOAD;
            };
        }
    }
}
