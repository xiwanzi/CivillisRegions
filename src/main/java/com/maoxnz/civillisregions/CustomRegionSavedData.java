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

public final class CustomRegionSavedData extends SavedData {
    private static final String DATA_NAME = CivilCustomRegionsMod.MOD_ID;

    private final Map<String, CustomRegion> regions = new LinkedHashMap<>();
    private long revision;

    public static CustomRegionSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                CustomRegionSavedData::load,
                CustomRegionSavedData::new,
                DATA_NAME);
    }

    public static CustomRegionSavedData load(CompoundTag tag) {
        CustomRegionSavedData data = new CustomRegionSavedData();
        data.revision = tag.getLong("revision");
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
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("revision", revision);
        ListTag entries = new ListTag();
        for (CustomRegion region : sortedRegions()) {
            entries.add(region.save());
        }
        tag.put("regions", entries);
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
        regions.put(region.id(), region);
        markChanged();
    }

    public boolean removeRegion(String id) {
        if (regions.remove(id) == null) {
            return false;
        }
        markChanged();
        return true;
    }

    public long revision() {
        return revision;
    }

    private void markChanged() {
        revision++;
        setDirty();
    }
}
