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
    private final String displayName;
    private final int overlayColor;
    private final int enterColor;
    private final int leaveColor;

    public CustomRegion(
            String id,
            ResourceLocation dimension,
            int minChunkX,
            int maxChunkX,
            int minChunkZ,
            int maxChunkZ,
            String enterText,
            String leaveText) {
        this(
                id,
                dimension,
                minChunkX,
                maxChunkX,
                minChunkZ,
                maxChunkZ,
                enterText,
                leaveText,
                id,
                -1,
                -1,
                -1);
    }

    public CustomRegion(
            String id,
            ResourceLocation dimension,
            int minChunkX,
            int maxChunkX,
            int minChunkZ,
            int maxChunkZ,
            String enterText,
            String leaveText,
            String displayName,
            int overlayColor,
            int enterColor,
            int leaveColor) {
        this.id = id;
        this.dimension = dimension;
        this.minChunkX = Math.min(minChunkX, maxChunkX);
        this.maxChunkX = Math.max(minChunkX, maxChunkX);
        this.minChunkZ = Math.min(minChunkZ, maxChunkZ);
        this.maxChunkZ = Math.max(minChunkZ, maxChunkZ);
        this.enterText = enterText == null ? "" : enterText;
        this.leaveText = leaveText == null ? "" : leaveText;
        this.displayName = displayName == null || displayName.isBlank() ? id : displayName;
        this.overlayColor = overlayColor;
        this.enterColor = enterColor;
        this.leaveColor = leaveColor;
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

    public String displayName() {
        return displayName;
    }

    public int overlayColor() {
        return overlayColor;
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

    public boolean contains(ResourceLocation playerDimension, int chunkX, int chunkZ) {
        return dimension.equals(playerDimension)
                && chunkX >= minChunkX
                && chunkX <= maxChunkX
                && chunkZ >= minChunkZ
                && chunkZ <= maxChunkZ;
    }

    public CustomRegion withMessages(String newEnterText, String newLeaveText) {
        return withMessages(newEnterText, newLeaveText, enterColor, leaveColor);
    }

    public CustomRegion withMessages(String newEnterText, String newLeaveText, int newEnterColor, int newLeaveColor) {
        return new CustomRegion(
                id,
                dimension,
                minChunkX,
                maxChunkX,
                minChunkZ,
                maxChunkZ,
                newEnterText,
                newLeaveText,
                displayName,
                overlayColor,
                newEnterColor,
                newLeaveColor);
    }

    public CustomRegion withOverlay(String newDisplayName, int newOverlayColor) {
        return new CustomRegion(
                id,
                dimension,
                minChunkX,
                maxChunkX,
                minChunkZ,
                maxChunkZ,
                enterText,
                leaveText,
                newDisplayName,
                newOverlayColor,
                enterColor,
                leaveColor);
    }

    public CustomRegion withBounds(int newMinChunkX, int newMaxChunkX, int newMinChunkZ, int newMaxChunkZ) {
        return new CustomRegion(
                id,
                dimension,
                newMinChunkX,
                newMaxChunkX,
                newMinChunkZ,
                newMaxChunkZ,
                enterText,
                leaveText,
                displayName,
                overlayColor,
                enterColor,
                leaveColor);
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
        tag.putString("displayName", displayName);
        tag.putInt("overlayColor", overlayColor);
        tag.putInt("enterColor", enterColor);
        tag.putInt("leaveColor", leaveColor);
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
                tag.getString("leaveText"),
                tag.contains("displayName") ? tag.getString("displayName") : id,
                tag.contains("overlayColor") ? tag.getInt("overlayColor") : -1,
                tag.contains("enterColor") ? tag.getInt("enterColor") : -1,
                tag.contains("leaveColor") ? tag.getInt("leaveColor") : -1);
    }
}
