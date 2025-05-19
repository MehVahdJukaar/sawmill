package net.mehvahdjukaar.sawmill.cache;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.HashMap;
import java.util.Map;

public class CacheResource {

    private int getModStateHash(ResourceManager manager) {
        var mods = PlatHelper.getInstalledMods();
        Map<String, String> modVersions = new HashMap<>();
        for (var mod : mods) {
            modVersions.put(mod, PlatHelper.getModVersion(mod));
        }
        return modVersions.hashCode();
    }

    public static void aa() {
     //   new PathPackResources();
    }
}
