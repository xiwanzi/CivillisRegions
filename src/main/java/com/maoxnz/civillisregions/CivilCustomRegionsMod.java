package com.maoxnz.civillisregions;

import com.maoxnz.civillisregions.net.ModNetwork;
import com.maoxnz.civillisregions.config.ClientOverlayConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@Mod(CivilCustomRegionsMod.MOD_ID)
public final class CivilCustomRegionsMod {
    public static final String MOD_ID = "civil_custom_regions";

    public CivilCustomRegionsMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientOverlayConfig.SPEC);
        ModNetwork.register();
    }
}
