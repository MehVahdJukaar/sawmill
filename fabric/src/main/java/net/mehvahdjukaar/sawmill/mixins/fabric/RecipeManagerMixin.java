package net.mehvahdjukaar.sawmill.mixins.fabric;

import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.fabricmc.fabric.impl.resource.conditions.conditions.AllModsLoadedResourceCondition;
import net.mehvahdjukaar.sawmill.SawmillRecipeGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
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
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;build()Lcom/google/common/collect/ImmutableMultimap;",
                    shift = At.Shift.BEFORE))
    public void addSawmillRecipesHack(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci,
                                      @Local com.google.common.collect.ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> byName,
                                      @Local ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType,
                                      @Share("parsed") LocalRef<List<RecipeHolder<?>>> parsed) {

        SawmillRecipeGenerator.process(parsed.get(), byName, byType);
    }


    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMap$Builder;",
                    ordinal = 0))
    public void interceptRecipe(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager,
                                ProfilerFiller profiler, CallbackInfo ci,
                                @Local RecipeHolder<?> recipe, @Share("parsed") LocalRef<List<RecipeHolder<?>>> parsed) {
        if (parsed.get() == null) {
            parsed.set(new ArrayList<>());
        }
        parsed.get().add(recipe);
    }
}
