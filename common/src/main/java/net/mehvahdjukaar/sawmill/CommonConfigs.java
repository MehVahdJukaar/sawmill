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

        builder.push("special_costs")
                .comment("If you would need more of these contact me and I'll make them data driven");
        STICK_COST = builder.comment("Cost of a stick in planks. Set to -1 to disable this override")
                .define("sticks_cost", 0.4, -1, 10);
        STAIRS_COST = builder.comment("Cost of a stair block in planks. Set to -1 to disable this override")
                .define("stairs_cost", 1d, -1, 10);
        builder.pop();

        builder.buildAndRegister();
    }

    public static void init() {
    }


    public static double getThreshold() {
        return 0.5;
    }
}
