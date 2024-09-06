package net.mehvahdjukaar.sawmill.mixins.neoforge;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.sawmill.SawmillRecipeGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {


    @ModifyExpressionValue(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;builder()Lcom/google/common/collect/ImmutableMap$Builder;"))
    public ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> sawmill$whyCantICaptureThatLocal(ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> original,
                                                                                                    @Share("byName") LocalRef<ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>>> byName) {
        byName.set(original);
        return original;
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;build()Lcom/google/common/collect/ImmutableMultimap;",
                    shift = At.Shift.BEFORE))
    public void sawmill$addRecipes(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci,
                                   @Share("byName") LocalRef<ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>>> byName,
                                   @Local ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType) {
        var oldRecipes = byName.get().build();

        ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> copy = ImmutableMap.builder();
        copy.putAll(oldRecipes);
        byName.set(copy);
        SawmillRecipeGenerator.process(oldRecipes.values(), byName.get(), byType);
    }

}
