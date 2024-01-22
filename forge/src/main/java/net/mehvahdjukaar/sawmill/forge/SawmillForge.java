package net.mehvahdjukaar.sawmill.forge;

import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.forge.RegHelperImpl;
import net.mehvahdjukaar.sawmill.Sawmill;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Author: MehVahdJukaar
 */
@Mod(Sawmill.MOD_ID)
public class SawmillForge {

    public SawmillForge() {
        Sawmill.init();
        MinecraftForge.EVENT_BUS.register(this);
    }



}
