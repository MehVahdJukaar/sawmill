package net.mehvahdjukaar.sawmill.forge;

import net.mehvahdjukaar.sawmill.SawmillMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

/**
 * Author: MehVahdJukaar
 */
@Mod(SawmillMod.MOD_ID)
public class SawmillForge {

    public SawmillForge() {
        SawmillMod.init();
        MinecraftForge.EVENT_BUS.register(this);
    }




}
