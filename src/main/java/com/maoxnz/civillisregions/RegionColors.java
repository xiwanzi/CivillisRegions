package com.maoxnz.civillisregions;

import java.util.Locale;
import java.util.regex.Pattern;

public final class RegionColors {
    public static final int UNSET = -1;
    private static final Pattern HEX_RGB = Pattern.compile("^#?[0-9a-fA-F]{6}$");

    private RegionColors() {}

    public static int parseHexRgb(String input) {
        if (input == null || input.isBlank()) {
            return UNSET;
        }
        String normalized = normalizeHexRgb(input);
        if (normalized == null) {
            return UNSET;
        }
        return Integer.parseInt(normalized.substring(1), 16);
    }

    public static String normalizeHexRgb(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        if (!HEX_RGB.matcher(trimmed).matches()) {
            return null;
        }
        String withoutPrefix = trimmed.charAt(0) == '#' ? trimmed.substring(1) : trimmed;
        return "#" + withoutPrefix.toLowerCase(Locale.ROOT);
    }

    public static String formatHexRgb(int rgb) {
        if (rgb < 0) {
            return "default";
        }
        return String.format(Locale.ROOT, "#%06x", rgb & 0xFFFFFF);
    }

    public static int withAlpha(int rgb, int alpha) {
        int clampedAlpha = Math.max(0, Math.min(255, alpha));
        return (clampedAlpha << 24) | (rgb & 0xFFFFFF);
    }
}
