package com.maoxnz.civillisregions.config;

import com.maoxnz.civillisregions.RegionColors;
import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientOverlayConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue LARGE_MAP_ENABLED;
    public static final ForgeConfigSpec.BooleanValue MINIMAP_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<String> DEFAULT_CUSTOM_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> CIVILIZED_HIGH_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> CIVILIZED_MONSTER_COLOR;
    public static final ForgeConfigSpec.IntValue FILL_ALPHA;
    public static final ForgeConfigSpec.IntValue BORDER_ALPHA;

    private static final int FALLBACK_CUSTOM_RGB = 0xD8DCDD;
    private static final int FALLBACK_HIGH_RGB = 0xD2D7D9;
    private static final int FALLBACK_MONSTER_RGB = 0xC8CED1;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("ftb_chunks_overlay");
        LARGE_MAP_ENABLED = builder
                .comment("Draw Civillis/custom region overlays on the FTB Chunks large map.")
                .define("largeMapEnabled", false);
        MINIMAP_ENABLED = builder
                .comment("Draw Civillis/custom region overlays on the FTB Chunks minimap.")
                .define("minimapEnabled", false);
        DEFAULT_CUSTOM_COLOR = builder
                .comment("Default RRGGBB or #RRGGBB color for custom regions with no per-region overlay color.")
                .define("defaultCustomColor", "#d8dcdd");
        CIVILIZED_HIGH_COLOR = builder
                .comment("Default RRGGBB or #RRGGBB color for Civillis HIGH civilization chunks.")
                .define("civilizedHighColor", "#d2d7d9");
        CIVILIZED_MONSTER_COLOR = builder
                .comment("Default RRGGBB or #RRGGBB color for Civillis MONSTER chunks.")
                .define("civilizedMonsterColor", "#c8ced1");
        FILL_ALPHA = builder
                .comment("Overlay fill alpha, 0-255.")
                .defineInRange("fillAlpha", 56, 0, 255);
        BORDER_ALPHA = builder
                .comment("Overlay border alpha, 0-255.")
                .defineInRange("borderAlpha", 168, 0, 255);
        builder.pop();
        SPEC = builder.build();
    }

    private ClientOverlayConfig() {}

    public static boolean anyFtbOverlayEnabled() {
        return LARGE_MAP_ENABLED.get() || MINIMAP_ENABLED.get();
    }

    public static int defaultCustomRgb() {
        return parseColor(DEFAULT_CUSTOM_COLOR.get(), FALLBACK_CUSTOM_RGB);
    }

    public static int civilizedHighRgb() {
        return parseColor(CIVILIZED_HIGH_COLOR.get(), FALLBACK_HIGH_RGB);
    }

    public static int civilizedMonsterRgb() {
        return parseColor(CIVILIZED_MONSTER_COLOR.get(), FALLBACK_MONSTER_RGB);
    }

    private static int parseColor(String value, int fallback) {
        int parsed = RegionColors.parseHexRgb(value);
        return parsed >= 0 ? parsed : fallback;
    }
}
