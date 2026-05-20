package com.maoxnz.civillisregions.config;

import com.maoxnz.civillisregions.CivilCustomRegionsMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = CivilCustomRegionsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ServerNoticeConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue NOTICE_SCALE;

    private static final double MIN_NOTICE_SCALE = 0.5D;
    private static final double MAX_NOTICE_SCALE = 3.0D;
    private static ModConfig activeConfig;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder
                .translation("configuration.civil_custom_regions.hud")
                .comment("Server-controlled HUD notice settings.")
                .push("hud");
        NOTICE_SCALE = builder
                .translation("configuration.civil_custom_regions.hud.noticeScale")
                .comment("Scale for custom region enter/leave HUD notices. 1.0 is the old size.")
                .defineInRange("noticeScale", 1.25D, MIN_NOTICE_SCALE, MAX_NOTICE_SCALE);
        builder.pop();
        SPEC = builder.build();
    }

    private ServerNoticeConfig() {}

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getType() == ModConfig.Type.SERVER && CivilCustomRegionsMod.MOD_ID.equals(config.getModId())) {
            activeConfig = config;
        }
    }

    public static double noticeScale() {
        return NOTICE_SCALE.get();
    }

    public static boolean setNoticeScale(double scale) {
        if (scale < MIN_NOTICE_SCALE || scale > MAX_NOTICE_SCALE) {
            return false;
        }
        NOTICE_SCALE.set(scale);
        if (activeConfig != null) {
            activeConfig.save();
        }
        return true;
    }

    public static double minNoticeScale() {
        return MIN_NOTICE_SCALE;
    }

    public static double maxNoticeScale() {
        return MAX_NOTICE_SCALE;
    }
}
