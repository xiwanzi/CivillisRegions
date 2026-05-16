package com.maoxnz.civillisregions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public final class CustomRegion {
    private final String id;
    private final ResourceLocation dimension;
    private final int minChunkX;
    private final int maxChunkX;
    private final int minChunkZ;
    private final int maxChunkZ;
    private final String enterText;
    private final String leaveText;

    public CustomRegion(
            String id,
            ResourceLocation dimension,
            int minChunkX,
            int maxChunkX,
            int minChunkZ,
            int maxChunkZ,
            String enterText,
            String leaveText) {
        this.id = id;
        this.dimension = dimension;
        this.minChunkX = Math.min(minChunkX, maxChunkX);
        this.maxChunkX = Math.max(minChunkX, maxChunkX);
        this.minChunkZ = Math.min(minChunkZ, maxChunkZ);
        this.maxChunkZ = Math.max(minChunkZ, maxChunkZ);
        this.enterText = enterText == null ? "" : enterText;
        this.leaveText = leaveText == null ? "" : leaveText;
    }

    public String id() {
        return id;
    }

    public ResourceLocation dimension() {
        return dimension;
    }

    public int minChunkX() {
        return minChunkX;
    }

    public int maxChunkX() {
        return maxChunkX;
    }

    public int minChunkZ() {
        return minChunkZ;
    }

    public int maxChunkZ() {
        return maxChunkZ;
    }

    public String enterText() {
        return enterText;
    }

    public String leaveText() {
        return leaveText;
    }

    public boolean hasEnterText() {
        return !enterText.isBlank();
    }

    public boolean hasLeaveText() {
        return !leaveText.isBlank();
    }

    public boolean contains(ResourceLocation playerDimension, int chunkX, int chunkZ) {
        return dimension.equals(playerDimension)
                && chunkX >= minChunkX
                && chunkX <= maxChunkX
                && chunkZ >= minChunkZ
                && chunkZ <= maxChunkZ;
    }

    public CustomRegion withMessages(String newEnterText, String newLeaveText) {
        return new CustomRegion(
                id,
                dimension,
                minChunkX,
                maxChunkX,
                minChunkZ,
                maxChunkZ,
                newEnterText,
                newLeaveText);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("dimension", dimension.toString());
        tag.putInt("minChunkX", minChunkX);
        tag.putInt("maxChunkX", maxChunkX);
        tag.putInt("minChunkZ", minChunkZ);
        tag.putInt("maxChunkZ", maxChunkZ);
        tag.putString("enterText", enterText);
        tag.putString("leaveText", leaveText);
        return tag;
    }

    public static CustomRegion load(CompoundTag tag) {
        String id = tag.getString("id");
        ResourceLocation dimension = new ResourceLocation(tag.getString("dimension"));
        return new CustomRegion(
                id,
                dimension,
                tag.getInt("minChunkX"),
                tag.getInt("maxChunkX"),
                tag.getInt("minChunkZ"),
                tag.getInt("maxChunkZ"),
                tag.getString("enterText"),
                tag.getString("leaveText"));
    }
}
