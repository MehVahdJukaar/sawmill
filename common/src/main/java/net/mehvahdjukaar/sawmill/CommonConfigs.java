package net.mehvahdjukaar.sawmill;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CommonConfigs {

    public static final Supplier<Boolean> ALLOW_NON_BLOCKS;
    public static final Supplier<Boolean> ALLOW_NON_VARIANTS;
    public static final Supplier<Boolean> PLANKS_ONLY_ONE;
    public static final Supplier<Boolean> WIDE_GUI;
    public static final Supplier<Boolean> SAVE_RECIPES;
    public static final Supplier<Boolean> DYNAMIC_RECIPES;
    public static final Supplier<List<String>> MOD_BLACKLIST;
    public static final Supplier<Map<String, Double>> SPECIAL_COSTS;
    public static final Supplier<SearchMode> SEARCH_MODE;
    public static final Supplier<Integer> SEARCH_BAR_THRESHOLD;
    public static final Supplier<Double> MAX_DISCOUNT;
    public static final Supplier<Boolean> SORT_RECIPES;
    public static final Supplier<Boolean> IGNORE_CUSTOM_INGREDIENTS;

    public static final ConfigSpec SPEC;

    static {

        ConfigBuilder builder = ConfigBuilder.create(SawmillMod.MOD_ID, ConfigType.COMMON);

        builder.push("general");
        SORT_RECIPES = builder.comment("Sort recipes following cretive tab order. " +
                        "Could cause issue in the case when, for whatever reason, creative other would differ from server to client." +
                        "Additionally Neoforge has a bug where this stuff won't work on servers. Use Forge instead!")
                .define("sort_recipes", true);
        SAVE_RECIPES = builder.comment("Save sawmill recipes to disk. Enable this if you are the author of a modpack and want to disable dynamic recipe generation. To do so just turn of that config, turn this one on and after booting up the game just copy the generated recipes in the generated folder in your own datapack, then turn off this config")
                .define("save_recipes", false);
        DYNAMIC_RECIPES = builder.comment("Generates Sawmill recipes dynamically. Remove this if you plan to add all of them manually instead. Can speed up boot time slightly")
                .define("dynamic_recipes", true);
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
                .defineObject("special_recipe_costs", () -> Map.of("stairs", 1d,
                                "boat", 4d),
                        Codec.unboundedMap(Codec.STRING, Codec.DOUBLE));
        MAX_DISCOUNT = builder.comment("Maximum discount that sawmill will given when converting recipes. Unit is percentage of input item")
                .define("max_discount", 0.35, 0, 1);
        IGNORE_CUSTOM_INGREDIENTS = builder.comment("Ignore all custom ingredient types when scanning recipes." +
                        "Turn this on if some recipes are missing.")
                .define("ignore_custom_ingredients", true);
        builder.pop();

        builder.setSynced();
        SPEC = builder.buildAndRegister();
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
}
