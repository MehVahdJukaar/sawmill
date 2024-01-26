package net.mehvahdjukaar.sawmill.mixins.forge;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.SawmillRecipeGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {


    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;",
                    ordinal = 1,
                    shift = At.Shift.BEFORE))
    public void addSawmillRecipesHack(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci,
                                      @Local(ordinal = 1) Map<RecipeType<?>, ImmutableMap.Builder<ResourceLocation, Recipe<?>>> map,
                                      @Local ImmutableMap.Builder<ResourceLocation, Recipe<?>> builder,
                                      @Share("parsed") LocalRef<List<Recipe<?>>> parsed) {

        SawmillRecipeGenerator.process(parsed.get(), map, builder, profiler);
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMap$Builder;",
                    ordinal = 1))
    public void interceptRecipe(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager,
                                ProfilerFiller profiler, CallbackInfo ci,
                                @Local Recipe<?> recipe,
                                @Local ImmutableMap.Builder<ResourceLocation, Recipe<?>> b,
                                @Share("parsed") LocalRef<List<Recipe<?>>> parsed) {
        if (parsed.get() == null) {
            parsed.set(new ArrayList<>());
        }
        parsed.get().add(recipe);
    }
}
