package com.maoxnz.civillisregions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class CustomRegionSavedData extends SavedData {
    private static final String DATA_NAME = CivilCustomRegionsMod.MOD_ID;

    private final Map<String, CustomRegion> regions = new LinkedHashMap<>();
    private final Map<String, Map<String, CustomSubRegion>> subRegions = new LinkedHashMap<>();
    private long revision;
    private long mapRevision;

    public static CustomRegionSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                CustomRegionSavedData::load,
                CustomRegionSavedData::new,
                DATA_NAME);
    }

    public static CustomRegionSavedData load(CompoundTag tag) {
        CustomRegionSavedData data = new CustomRegionSavedData();
        data.revision = tag.getLong("revision");
        data.mapRevision = tag.contains("mapRevision") ? tag.getLong("mapRevision") : data.revision;
        ListTag entries = tag.getList("regions", 10);
        for (int i = 0; i < entries.size(); i++) {
            try {
                CustomRegion region = CustomRegion.load(entries.getCompound(i));
                if (RegionId.isValid(region.id())) {
                    data.regions.put(region.id(), region);
                }
            } catch (RuntimeException ignored) {
                // Skip one bad entry without dropping the whole saved data file.
            }
        }
        ListTag subEntries = tag.getList("subRegions", 10);
        for (int i = 0; i < subEntries.size(); i++) {
            try {
                CustomSubRegion subRegion = CustomSubRegion.load(subEntries.getCompound(i));
                CustomRegion parent = data.regions.get(subRegion.parentId());
                if (parent != null
                        && RegionId.isValid(subRegion.id())
                        && subRegion.isInsideParent(parent)) {
                    data.subRegions
                            .computeIfAbsent(subRegion.parentId(), ignored -> new LinkedHashMap<>())
                            .put(subRegion.id(), subRegion);
                }
            } catch (RuntimeException ignored) {
                // Skip one bad sub-region without dropping the whole saved data file.
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("revision", revision);
        tag.putLong("mapRevision", mapRevision);
        ListTag entries = new ListTag();
        for (CustomRegion region : sortedRegions()) {
            entries.add(region.save());
        }
        tag.put("regions", entries);
        ListTag subEntries = new ListTag();
        for (CustomSubRegion subRegion : sortedSubRegions()) {
            subEntries.add(subRegion.save());
        }
        tag.put("subRegions", subEntries);
        return tag;
    }

    public Collection<CustomRegion> regions() {
        return Collections.unmodifiableCollection(regions.values());
    }

    public List<CustomRegion> sortedRegions() {
        ArrayList<CustomRegion> out = new ArrayList<>(regions.values());
        out.sort((a, b) -> a.id().compareTo(b.id()));
        return out;
    }

    public List<String> sortedIds() {
        ArrayList<String> out = new ArrayList<>(regions.keySet());
        Collections.sort(out);
        return out;
    }

    public CustomRegion getRegion(String id) {
        return regions.get(id);
    }

    public boolean contains(String id) {
        return regions.containsKey(id);
    }

    public void putRegion(CustomRegion region) {
        putRegion(region, true);
    }

    public void putRegion(CustomRegion region, boolean affectsMap) {
        regions.put(region.id(), region);
        markChanged(affectsMap);
    }

    public boolean removeRegion(String id) {
        if (regions.remove(id) == null) {
            return false;
        }
        subRegions.remove(id);
        markChanged(true);
        return true;
    }

    public void forEachSubRegion(Consumer<CustomSubRegion> consumer) {
        for (Map<String, CustomSubRegion> byId : subRegions.values()) {
            byId.values().forEach(consumer);
        }
    }

    public List<CustomSubRegion> sortedSubRegions() {
        ArrayList<CustomSubRegion> out = new ArrayList<>();
        for (Map<String, CustomSubRegion> byId : subRegions.values()) {
            out.addAll(byId.values());
        }
        out.sort((a, b) -> a.qualifiedId().compareTo(b.qualifiedId()));
        return out;
    }

    public List<CustomSubRegion> sortedSubRegions(String parentId) {
        ArrayList<CustomSubRegion> out = new ArrayList<>(subRegions
                .getOrDefault(parentId, Collections.emptyMap())
                .values());
        out.sort((a, b) -> a.id().compareTo(b.id()));
        return out;
    }

    public List<String> sortedSubRegionIds(String parentId) {
        ArrayList<String> out = new ArrayList<>(subRegions
                .getOrDefault(parentId, Collections.emptyMap())
                .keySet());
        Collections.sort(out);
        return out;
    }

    public CustomSubRegion getSubRegion(String parentId, String id) {
        return subRegions.getOrDefault(parentId, Collections.emptyMap()).get(id);
    }

    public boolean containsSubRegion(String parentId, String id) {
        return getSubRegion(parentId, id) != null;
    }

    public void putSubRegion(CustomSubRegion subRegion) {
        subRegions
                .computeIfAbsent(subRegion.parentId(), ignored -> new LinkedHashMap<>())
                .put(subRegion.id(), subRegion);
        markChanged(false);
    }

    public boolean removeSubRegion(String parentId, String id) {
        Map<String, CustomSubRegion> byId = subRegions.get(parentId);
        if (byId == null || byId.remove(id) == null) {
            return false;
        }
        if (byId.isEmpty()) {
            subRegions.remove(parentId);
        }
        markChanged(false);
        return true;
    }

    public List<CustomSubRegion> subRegionsOutsideParent(CustomRegion parent) {
        ArrayList<CustomSubRegion> out = new ArrayList<>();
        for (CustomSubRegion subRegion : sortedSubRegions(parent.id())) {
            if (!subRegion.isInsideParent(parent)) {
                out.add(subRegion);
            }
        }
        return out;
    }

    public long revision() {
        return revision;
    }

    public long mapRevision() {
        return mapRevision;
    }

    private void markChanged(boolean affectsMap) {
        revision++;
        if (affectsMap) {
            mapRevision++;
        }
        setDirty();
    }
}
