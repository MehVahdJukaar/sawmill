package net.mehvahdjukaar.sawmill.mixins;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeTab.class)
public class UnnededMixin {

    @Inject(method = "rebuildSearchTree", at = @At("HEAD"), cancellable = true)
    public void preventCallOnServerThreadBecauseSomeModsMightHaveIncorrectNonThreadSafeTooltipLines(CallbackInfo ci) {
        // check if server thread
        ItemStack s;
        s.getTooltipLines()
        MinecraftServer currentServer = PlatHelper.getCurrentServer();
        if (currentServer != null && currentServer.isSameThread()) {
            ci.cancel();
        }
    }
}
