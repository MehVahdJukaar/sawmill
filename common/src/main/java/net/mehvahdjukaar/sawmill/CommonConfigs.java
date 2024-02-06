package net.mehvahdjukaar.sawmill;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;

import java.util.List;
import java.util.function.Supplier;

public class CommonConfigs {

    public static final Supplier<Boolean> ALLOW_NON_BLOCKS;
    public static final Supplier<Boolean> ALLOW_NON_VARIANTS;
    public static final Supplier<List<String>> MOD_BLACKLIST;
    public static final Supplier<Double> STICK_COST;
    public static final Supplier<Double> STAIRS_COST;
    public static final Supplier<SearchMode> SEARCH_MODE;
    public static final Supplier<Integer> SEARCH_BAR_THREASHOLD;

    static {

        ConfigBuilder builder = ConfigBuilder.create(SawmillMod.MOD_ID, ConfigType.COMMON);

        builder.push("general");
        ALLOW_NON_BLOCKS = builder.comment("Allow crafting non-block items")
                .define("allow_non_blocks", true);
        ALLOW_NON_VARIANTS = builder.comment("Allows crafting non wood variant items (crafting table for example)")
                .define("allow_non_variant", true);
        MOD_BLACKLIST = builder.comment("List of Mod ids you want to completely exclude from sawmill recipes. All recipes from these mods will be ignored." +
                        "For more fine control use the sawmill:blacklist item tag")
                .define("mod_blacklist", List.of());
        SEARCH_MODE = builder
                .comment("Determines if GUI will have search bar or not. Automatic only enables the bar statically when you have enough mods that add wood recipes. Dynamic changes the gui dynamically depending on how many recipe its displaying")
                .define("search_bar_mode", SearchMode.AUTOMATIC);
        SEARCH_BAR_THREASHOLD = builder.comment("At how many recipes the search bar should appear")
                .define("search_bar_threshold", 32, 0, 200);


        builder.push("special_costs")
                .comment("If you would need more of these contact me and I'll make them data driven");
        STICK_COST = builder.comment("Cost of a stick in planks. Set to -1 to disable this override")
                .define("sticks_cost", 0.4, -1, 10);
        STAIRS_COST = builder.comment("Cost of a stair block in planks. Set to -1 to disable this override")
                .define("stairs_cost", 1d, -1, 10);
        builder.pop();

        builder.setSynced();
        builder.buildAndRegister();
    }

    public static void init() {
    }


    public static double getThreshold() {
        return 0.5;
    }

    public static boolean hasSearchBar(int recipeCount) {
        var s = SEARCH_MODE.get();
        return switch (s) {
            case ON -> true;
            case OFF -> false;
            case AUTOMATIC -> SawmillClient.hasManyRecipes();
            case DYNAMIC -> recipeCount > SEARCH_BAR_THREASHOLD.get();
        };
    }

    public enum SearchMode {
        OFF, ON, AUTOMATIC, DYNAMIC
    }
}
