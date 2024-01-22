package net.mehvahdjukaar.sawmill;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;

import java.util.function.Supplier;

public class CommonConfigs {

    public static final Supplier<Boolean> NON_BLOCKS;

    static {

        ConfigBuilder builder = ConfigBuilder.create(Sawmill.MOD_ID, ConfigType.COMMON);

        builder.push("general");
        NON_BLOCKS = builder.comment("Allow crafting non-block items")
                .define("allow_non_blocks", false);

        builder.pop();

        builder.buildAndRegister();
    }

    public static void init() {
    }
}
