package net.mehvahdjukaar.sawmill.mixins;

import net.mehvahdjukaar.sawmill.SawmillMod;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(TagManager.class)
public class TagManagerHackMixin {

    @Shadow private List<TagManager.LoadResult<?>> results;

    @Inject(method = "method_40098",at = @At(value = "TAIL" ))
    private void joinHack(List list, Void void_, CallbackInfo ci){

        SawmillMod.setTagManagerResults(this.results);
    }
}
