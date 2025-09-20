package net.mehvahdjukaar.sawmill.mixins;

import net.mehvahdjukaar.sawmill.SawmillMod;
import net.mehvahdjukaar.sawmill.SawmillRecipeGenerator;
import net.minecraft.tags.TagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Mixin(TagManager.class)
public class TagManagerHackMixin {

    @Shadow
    private List<TagManager.LoadResult<?>> results;

    @Inject(method = "method_40098", at = @At(value = "TAIL"))
    private void joinHack(List<CompletableFuture<TagManager.LoadResult<?>>> list, Void void_, CallbackInfo ci) {
        if (!SawmillRecipeGenerator.INSTANCE.willGeneratingRecipesThisReload()) return;
        synchronized (this) { //i don't even know anymore. People keep reporting issues and i have no clue where that is
            SawmillMod.setTagManagerResults(list.stream()
                    .map(loadResultCompletableFuture -> {
                        try {
                            //blockingly finish all results. these must be returned before the barrier ends so that recipes can be added in between
                            return loadResultCompletableFuture.get();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toUnmodifiableList()));
        }
    }
}
