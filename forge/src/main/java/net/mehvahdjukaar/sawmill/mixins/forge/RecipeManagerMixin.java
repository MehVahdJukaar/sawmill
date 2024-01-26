package net.mehvahdjukaar.sawmill.mixins.forge;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.serialization.DynamicOps;
import net.mehvahdjukaar.sawmill.SawmillRecipeGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {


    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;",
                    ordinal = 1,
                    shift = At.Shift.BEFORE), remap = false)
    public void addSawmillRecipesHack(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci,
                                      @Local(ordinal = 1) Map<RecipeType<?>, ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>>> map,
                                      @Local ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> builder,
                                      @Share("parsed") LocalRef<List<Recipe<?>>> parsed) {

        SawmillRecipeGenerator.process(parsed.get(), map, builder, profiler);
    }

    @ModifyExpressionValue(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager;fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;Lcom/mojang/serialization/DynamicOps;)Ljava/util/Optional;"))
    private static Optional<RecipeHolder<?>> interceptRecipe(Optional<RecipeHolder<?>> original,
                                                             @Share("parsed") LocalRef<List<Recipe<?>>> parsed) {
        if (parsed.get() == null) {
            parsed.set(new ArrayList<>());
        }
        original.ifPresent(holder -> parsed.get().add(holder.value()));
        return original;
    }
}
