package net.mehvahdjukaar.sawmill;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;

import java.util.function.Supplier;

public class ClientConfigs {

    public static final Supplier<SearchMode> SEARCH_MODE;

    static {

        ConfigBuilder builder = ConfigBuilder.create(SawmillMod.MOD_ID, ConfigType.CLIENT);

        builder.push("general");
        SEARCH_MODE =   builder.define("search_mode", SearchMode.AUTOMATIC);
        builder.pop();

        builder.buildAndRegister();
    }

    public static void init() {
    }

    public enum SearchMode{
        OFF, ON, AUTOMATIC
    }
}
