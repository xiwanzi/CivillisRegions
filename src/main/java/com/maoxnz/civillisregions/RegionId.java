package com.maoxnz.civillisregions;

import java.util.regex.Pattern;

public final class RegionId {
    private static final Pattern PATTERN = Pattern.compile("[a-z0-9_-]+");

    private RegionId() {}

    public static boolean isValid(String id) {
        return id != null && !id.isBlank() && PATTERN.matcher(id).matches();
    }
}
