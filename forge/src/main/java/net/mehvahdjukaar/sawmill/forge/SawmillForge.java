package net.mehvahdjukaar.sawmill.forge;

import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.forge.RegHelperImpl;
import net.mehvahdjukaar.sawmill.Sawmill;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Author: MehVahdJukaar
 */
@Mod(Sawmill.MOD_ID)
public class SawmillForge {

    public SawmillForge() {
        Sawmill.init();
    }

    @SubscribeEvent
    public void registerCustomResolver(RegisterColorHandlersEvent.ColorResolvers event) {
    }


}
