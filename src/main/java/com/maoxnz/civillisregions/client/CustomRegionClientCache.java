package com.maoxnz.civillisregions.client;

import com.maoxnz.civillisregions.CustomRegion;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CustomRegionClientCache {
    private static final Map<ResourceLocation, List<CustomRegion>> BY_DIMENSION = new LinkedHashMap<>();

    private CustomRegionClientCache() {}

    public static synchronized void replace(List<CustomRegion> regions) {
        BY_DIMENSION.clear();
        for (CustomRegion region : regions) {
            BY_DIMENSION.computeIfAbsent(region.dimension(), ignored -> new ArrayList<>()).add(region);
        }
        for (Map.Entry<ResourceLocation, List<CustomRegion>> entry : BY_DIMENSION.entrySet()) {
            entry.setValue(List.copyOf(entry.getValue()));
        }
    }

    public static synchronized List<CustomRegion> regionsForDimension(ResourceLocation dimension) {
        List<CustomRegion> regions = BY_DIMENSION.get(dimension);
        return regions == null ? Collections.emptyList() : regions;
    }

    public static synchronized void clear() {
        BY_DIMENSION.clear();
    }
}
