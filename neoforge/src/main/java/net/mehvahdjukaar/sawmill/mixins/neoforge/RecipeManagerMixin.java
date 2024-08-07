package net.mehvahdjukaar.sawmill.mixins.neoforge;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.moonlight.api.platform.neoforge.RegHelperImpl;
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

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;build()Lcom/google/common/collect/ImmutableMultimap;",
                    shift = At.Shift.BEFORE))
    public void sawmill$addRecipes(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci,
                                      @Local LocalRef<ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>>> byName,
                                      @Local LocalRef<ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>>> byType) {
        var oldRecipes = byName.get().build();

        ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> copy = ImmutableMap.builder();
        copy.putAll(oldRecipes);
        byName.set(copy);
        SawmillRecipeGenerator.process(oldRecipes.values(), byName.get(), byType.get());
    }

}
