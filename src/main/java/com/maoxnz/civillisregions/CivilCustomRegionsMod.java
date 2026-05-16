package com.maoxnz.civillisregions;

import com.maoxnz.civillisregions.net.ModNetwork;
import net.minecraftforge.fml.common.Mod;

@Mod(CivilCustomRegionsMod.MOD_ID)
public final class CivilCustomRegionsMod {
    public static final String MOD_ID = "civil_custom_regions";

    public CivilCustomRegionsMod() {
        ModNetwork.register();
    }
}
