package com.maoxnz.civillisregions.config;

import com.maoxnz.civillisregions.CivilCustomRegionsMod;
import com.maoxnz.civillisregions.RegionColors;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = CivilCustomRegionsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientOverlayConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue LARGE_MAP_ENABLED;
    public static final ForgeConfigSpec.BooleanValue MINIMAP_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<String> DEFAULT_CUSTOM_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> CIVILIZED_HIGH_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> CIVILIZED_HIGH_LABEL;
    public static final ForgeConfigSpec.IntValue FILL_ALPHA;
    public static final ForgeConfigSpec.IntValue BORDER_ALPHA;

    private static final String DEFAULT_LIGHT_GRAY = "#d8dcdd";
    private static final String DEFAULT_CIVILIZED_LABEL = "文明区域";
    private static final int FALLBACK_LIGHT_GRAY_RGB = 0xD8DCDD;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder
                .translation("configuration.civil_custom_regions.ftb_chunks_overlay")
                .comment("Optional FTB Chunks map overlays for Civillis civilization chunks and custom regions.")
                .push("ftb_chunks_overlay");
        LARGE_MAP_ENABLED = builder
                .translation("configuration.civil_custom_regions.ftb_chunks_overlay.largeMapEnabled")
                .comment("Draw Civillis/custom region overlays on the FTB Chunks large map.")
                .define("largeMapEnabled", false);
        MINIMAP_ENABLED = builder
                .translation("configuration.civil_custom_regions.ftb_chunks_overlay.minimapEnabled")
                .comment("Draw Civillis/custom region overlays on the FTB Chunks minimap.")
                .define("minimapEnabled", false);
        DEFAULT_CUSTOM_COLOR = builder
                .translation("configuration.civil_custom_regions.ftb_chunks_overlay.defaultCustomColor")
                .comment("Default RRGGBB or #RRGGBB color for custom regions with no per-region overlay color.")
                .define("defaultCustomColor", DEFAULT_LIGHT_GRAY);
        CIVILIZED_HIGH_COLOR = builder
                .translation("configuration.civil_custom_regions.ftb_chunks_overlay.civilizedHighColor")
                .comment("Default RRGGBB or #RRGGBB color for Civillis civilization chunks.")
                .define("civilizedHighColor", DEFAULT_LIGHT_GRAY);
        CIVILIZED_HIGH_LABEL = builder
                .translation("configuration.civil_custom_regions.ftb_chunks_overlay.civilizedHighLabel")
                .comment("Large map hover label for Civillis civilization chunks.")
                .define("civilizedHighLabel", DEFAULT_CIVILIZED_LABEL);
        FILL_ALPHA = builder
                .translation("configuration.civil_custom_regions.ftb_chunks_overlay.fillAlpha")
                .comment("Overlay fill alpha, 0-255.")
                .defineInRange("fillAlpha", 56, 0, 255);
        BORDER_ALPHA = builder
                .translation("configuration.civil_custom_regions.ftb_chunks_overlay.borderAlpha")
                .comment("Overlay border alpha, 0-255.")
                .defineInRange("borderAlpha", 168, 0, 255);
        builder.pop();
        SPEC = builder.build();
    }

    private ClientOverlayConfig() {}

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getType() == ModConfig.Type.CLIENT
                && CivilCustomRegionsMod.MOD_ID.equals(config.getModId())
                && migrateLegacyDefaultColors()) {
            config.save();
        }
    }

    public static boolean anyFtbOverlayEnabled() {
        return LARGE_MAP_ENABLED.get() || MINIMAP_ENABLED.get();
    }

    public static int defaultCustomRgb() {
        migrateLegacyDefaultColors();
        return parseColor(DEFAULT_CUSTOM_COLOR.get(), FALLBACK_LIGHT_GRAY_RGB);
    }

    public static int civilizedHighRgb() {
        migrateLegacyDefaultColors();
        return parseColor(CIVILIZED_HIGH_COLOR.get(), FALLBACK_LIGHT_GRAY_RGB);
    }

    public static String civilizedHighLabel() {
        return labelOrDefault(CIVILIZED_HIGH_LABEL.get());
    }

    private static int parseColor(String value, int fallback) {
        int parsed = RegionColors.parseHexRgb(value);
        return parsed >= 0 ? parsed : fallback;
    }

    private static boolean migrateLegacyDefaultColors() {
        boolean changed = false;
        changed |= migrateLegacyColor(DEFAULT_CUSTOM_COLOR, "#d978ff");
        changed |= migrateLegacyColor(CIVILIZED_HIGH_COLOR, "#f0d94d");
        return changed;
    }

    private static boolean migrateLegacyColor(ForgeConfigSpec.ConfigValue<String> value, String legacyDefault) {
        String normalized = RegionColors.normalizeHexRgb(value.get());
        if (!legacyDefault.equals(normalized)) {
            return false;
        }
        value.set(DEFAULT_LIGHT_GRAY);
        return true;
    }

    private static String labelOrDefault(String value) {
        if (value == null) {
            return DEFAULT_CIVILIZED_LABEL;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? DEFAULT_CIVILIZED_LABEL : trimmed;
    }
}
