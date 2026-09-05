package net.mehvahdjukaar.sawmill.mixins;

import net.mehvahdjukaar.sawmill.RecipeManagerHook;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {

    @Shadow
    public abstract ReloadableServerRegistries.Holder fullRegistries();

    @Shadow
    @Final
    private RecipeManager recipes;

    //cant do this in RecipeManager.apply anymore
    @Inject(method = "updateComponentsAndStaticRegistryTags", at = @At("TAIL"))
    private void sawmill$addGeneratedRecipes(CallbackInfo ci) {
        ((RecipeManagerHook) this.recipes).sawmill$addGeneratedRecipes(this.fullRegistries().lookup());
    }
}
