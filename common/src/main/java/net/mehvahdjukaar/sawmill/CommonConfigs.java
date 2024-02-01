package net.mehvahdjukaar.sawmill;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;

import java.util.List;
import java.util.function.Supplier;

public class CommonConfigs {

    public static final Supplier<Boolean> ALLOW_NON_BLOCKS;

    public static final Supplier<Boolean> ALLOW_NON_VARIANTS;
    public static final Supplier<List<String>> MOD_BLACKLIST;

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
        builder.pop();

        builder.buildAndRegister();
    }

    public static void init() {
    }
}
