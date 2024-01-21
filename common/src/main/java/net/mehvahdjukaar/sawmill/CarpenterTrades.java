package net.mehvahdjukaar.sawmill;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class CarpenterTrades extends SimpleJsonResourceReloadListener {

    public static final CarpenterTrades INSTANCE = new CarpenterTrades(new Gson(), "carpenter_trades");

    public CarpenterTrades(Gson gson, String name) {
        super(gson, name);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {

    }
}
