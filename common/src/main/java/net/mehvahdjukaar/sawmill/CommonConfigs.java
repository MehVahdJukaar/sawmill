package net.mehvahdjukaar.sawmill;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;

import java.util.function.Supplier;

public class CommonConfigs {

    public static final Supplier<Boolean> NON_BLOCKS;

    public static final Supplier<Boolean> ONLY_VARIANTS;

    static {

        ConfigBuilder builder = ConfigBuilder.create(SawmillMod.MOD_ID, ConfigType.COMMON);

        builder.push("general");
        NON_BLOCKS = builder.comment("Allow crafting non-block items")
                .define("allow_non_blocks", true);
        ONLY_VARIANTS = builder.comment("Allows crafting non wood variant items (crafting table for example)")
                        .define("allow_non_variant", true);
        builder.pop();

        builder.buildAndRegister();
    }

    public static void init() {
    }
}
