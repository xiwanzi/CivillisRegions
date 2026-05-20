package com.maoxnz.civillisregions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public final class CustomSubRegion {
    private final String parentId;
    private final String id;
    private final int minBlockX;
    private final int maxBlockX;
    private final int minBlockY;
    private final int maxBlockY;
    private final int minBlockZ;
    private final int maxBlockZ;
    private final String enterText;
    private final String leaveText;
    private final int enterColor;
    private final int leaveColor;

    public CustomSubRegion(
            String parentId,
            String id,
            int minBlockX,
            int maxBlockX,
            int minBlockY,
            int maxBlockY,
            int minBlockZ,
            int maxBlockZ,
            String enterText,
            String leaveText) {
        this(
                parentId,
                id,
                minBlockX,
                maxBlockX,
                minBlockY,
                maxBlockY,
                minBlockZ,
                maxBlockZ,
                enterText,
                leaveText,
                -1,
                -1);
    }

    public CustomSubRegion(
            String parentId,
            String id,
            int minBlockX,
            int maxBlockX,
            int minBlockY,
            int maxBlockY,
            int minBlockZ,
            int maxBlockZ,
            String enterText,
            String leaveText,
            int enterColor,
            int leaveColor) {
        this.parentId = parentId;
        this.id = id;
        this.minBlockX = Math.min(minBlockX, maxBlockX);
        this.maxBlockX = Math.max(minBlockX, maxBlockX);
        this.minBlockY = Math.min(minBlockY, maxBlockY);
        this.maxBlockY = Math.max(minBlockY, maxBlockY);
        this.minBlockZ = Math.min(minBlockZ, maxBlockZ);
        this.maxBlockZ = Math.max(minBlockZ, maxBlockZ);
        this.enterText = enterText == null ? "" : enterText;
        this.leaveText = leaveText == null ? "" : leaveText;
        this.enterColor = enterColor;
        this.leaveColor = leaveColor;
    }

    public String parentId() {
        return parentId;
    }

    public String id() {
        return id;
    }

    public String qualifiedId() {
        return parentId + "/" + id;
    }

    public int minBlockX() {
        return minBlockX;
    }

    public int maxBlockX() {
        return maxBlockX;
    }

    public int minBlockY() {
        return minBlockY;
    }

    public int maxBlockY() {
        return maxBlockY;
    }

    public int minBlockZ() {
        return minBlockZ;
    }

    public int maxBlockZ() {
        return maxBlockZ;
    }

    public String enterText() {
        return enterText;
    }

    public String leaveText() {
        return leaveText;
    }

    public int enterColor() {
        return enterColor;
    }

    public int leaveColor() {
        return leaveColor;
    }

    public boolean hasEnterText() {
        return !enterText.isBlank();
    }

    public boolean hasLeaveText() {
        return !leaveText.isBlank();
    }

    public boolean contains(ResourceLocation playerDimension, CustomRegion parent, int blockX, int blockY, int blockZ) {
        return parent != null
                && parent.id().equals(parentId)
                && parent.dimension().equals(playerDimension)
                && blockX >= minBlockX
                && blockX <= maxBlockX
                && blockY >= minBlockY
                && blockY <= maxBlockY
                && blockZ >= minBlockZ
                && blockZ <= maxBlockZ;
    }

    public boolean isInsideParent(CustomRegion parent) {
        if (parent == null || !parent.id().equals(parentId)) {
            return false;
        }
        int minParentBlockX = parent.minChunkX() * 16;
        int maxParentBlockX = parent.maxChunkX() * 16 + 15;
        int minParentBlockZ = parent.minChunkZ() * 16;
        int maxParentBlockZ = parent.maxChunkZ() * 16 + 15;
        return minBlockX >= minParentBlockX
                && maxBlockX <= maxParentBlockX
                && minBlockZ >= minParentBlockZ
                && maxBlockZ <= maxParentBlockZ;
    }

    public CustomSubRegion withMessages(
            String newEnterText,
            String newLeaveText,
            int newEnterColor,
            int newLeaveColor) {
        return new CustomSubRegion(
                parentId,
                id,
                minBlockX,
                maxBlockX,
                minBlockY,
                maxBlockY,
                minBlockZ,
                maxBlockZ,
                newEnterText,
                newLeaveText,
                newEnterColor,
                newLeaveColor);
    }

    public CustomSubRegion withBounds(
            int newMinBlockX,
            int newMaxBlockX,
            int newMinBlockY,
            int newMaxBlockY,
            int newMinBlockZ,
            int newMaxBlockZ) {
        return new CustomSubRegion(
                parentId,
                id,
                newMinBlockX,
                newMaxBlockX,
                newMinBlockY,
                newMaxBlockY,
                newMinBlockZ,
                newMaxBlockZ,
                enterText,
                leaveText,
                enterColor,
                leaveColor);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("parentId", parentId);
        tag.putString("id", id);
        tag.putInt("minBlockX", minBlockX);
        tag.putInt("maxBlockX", maxBlockX);
        tag.putInt("minBlockY", minBlockY);
        tag.putInt("maxBlockY", maxBlockY);
        tag.putInt("minBlockZ", minBlockZ);
        tag.putInt("maxBlockZ", maxBlockZ);
        tag.putString("enterText", enterText);
        tag.putString("leaveText", leaveText);
        tag.putInt("enterColor", enterColor);
        tag.putInt("leaveColor", leaveColor);
        return tag;
    }

    public static CustomSubRegion load(CompoundTag tag) {
        return new CustomSubRegion(
                tag.getString("parentId"),
                tag.getString("id"),
                tag.getInt("minBlockX"),
                tag.getInt("maxBlockX"),
                tag.getInt("minBlockY"),
                tag.getInt("maxBlockY"),
                tag.getInt("minBlockZ"),
                tag.getInt("maxBlockZ"),
                tag.getString("enterText"),
                tag.getString("leaveText"),
                tag.contains("enterColor") ? tag.getInt("enterColor") : -1,
                tag.contains("leaveColor") ? tag.getInt("leaveColor") : -1);
    }
}
