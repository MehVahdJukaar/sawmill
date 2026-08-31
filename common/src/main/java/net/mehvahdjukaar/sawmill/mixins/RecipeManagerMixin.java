package net.mehvahdjukaar.sawmill.mixins;

import net.mehvahdjukaar.sawmill.SawmillRecipeGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Shadow
    private RecipeMap recipes;

    @Shadow
    @Final
    private HolderLookup.Provider registries;

    @Inject(method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;" +
            "Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("TAIL"))
    private void sawmill$addGeneratedRecipes(RecipeMap loaded, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        this.recipes = SawmillRecipeGenerator.onRecipesLoaded(this.recipes, this.registries);
    }
}
