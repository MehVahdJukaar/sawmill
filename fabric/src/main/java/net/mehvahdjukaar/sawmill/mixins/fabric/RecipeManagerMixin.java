package net.mehvahdjukaar.sawmill.mixins.fabric;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import net.mehvahdjukaar.sawmill.SawmillRecipeGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Shadow
    private Map<ResourceLocation, RecipeHolder<?>> byName;

    @Shadow
    private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE_ASSIGN",
                    target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;",
                    shift = At.Shift.AFTER))
    public void sawmill$addRecipes(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> byNameCopy = ImmutableMap.builder();
        ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byTypeCopy = ImmutableMultimap.builder();
        this.byName.values().forEach(r -> {
            byNameCopy.put(r.id(), r);
            byTypeCopy.put(r.value().getType(), r);
        });
        SawmillRecipeGenerator.INSTANCE.process(this.byName.values(), byNameCopy, byTypeCopy);
        this.byName = byNameCopy.build();
        this.byType = byTypeCopy.build();
    }
}
