package com.maoxnz.civillisregions.client;

import civil.compat.bandsync.ChunkBandClientCache;
import civil.registry.DimensionPolicyRegistry;
import com.maoxnz.civillisregions.CustomRegion;
import com.maoxnz.civillisregions.RegionColors;
import com.maoxnz.civillisregions.config.ClientOverlayConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ftb.mods.ftbchunks.FTBChunksWorldConfig;
import dev.ftb.mods.ftbchunks.client.FTBChunksClient;
import dev.ftb.mods.ftbchunks.client.FTBChunksClientConfig;
import dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen;
import dev.ftb.mods.ftbchunks.client.map.MapChunk;
import dev.ftb.mods.ftbchunks.client.map.MapDimension;
import dev.ftb.mods.ftbchunks.client.map.MapRegion;
import dev.ftb.mods.ftblibrary.math.XZ;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.List;

public final class FtbChunkOverlayRenderer {
    private static final int REGION_SIZE_CHUNKS = 32;
    private static final byte CIVILIZED_HIGH = 3;
    private static final byte CIVILIZED_MONSTER = 4;

    private FtbChunkOverlayRenderer() {}

    public static void drawLargeMapTile(GuiGraphics graphics, MapRegion mapRegion, int x, int y, int width, int height) {
        if (!ClientOverlayConfig.LARGE_MAP_ENABLED.get() || mapRegion == null || width <= 0 || height <= 0) {
            return;
        }

        ResourceLocation dimension = mapRegion.dimension.dimension.location();
        if (!isOverlayDimensionEnabled(dimension)) {
            return;
        }

        String dimensionId = dimension.toString();
        int baseChunkX = mapRegion.pos.x() * REGION_SIZE_CHUNKS;
        int baseChunkZ = mapRegion.pos.z() * REGION_SIZE_CHUNKS;

        setupOverlayDrawing();
        drawCivilizedChunks(graphics, mapRegion, dimensionId, baseChunkX, baseChunkZ, x, y, width, height);
        drawCustomRegionsOnTile(graphics, mapRegion, dimension, baseChunkX, baseChunkZ, x, y, width, height);
    }

    public static void addLargeMapMouseOverText(
            TooltipList tooltip,
            LargeMapScreen largeMap,
            int blockX,
            int blockZ) {
        if (!ClientOverlayConfig.LARGE_MAP_ENABLED.get()
                || largeMap == null
                || largeMap.currentDimension() == null) {
            return;
        }

        ResourceLocation dimension = largeMap.currentDimension().location();
        if (!isOverlayDimensionEnabled(dimension)) {
            return;
        }

        int chunkX = Math.floorDiv(blockX, 16);
        int chunkZ = Math.floorDiv(blockZ, 16);
        if (!tooltip.getLines().isEmpty() || isFtbClaimed(currentMapDimension(dimension), chunkX, chunkZ)) {
            return;
        }

        List<CustomRegion> regions = CustomRegionClientCache.regionsForDimension(dimension);
        for (int i = regions.size() - 1; i >= 0; i--) {
            CustomRegion region = regions.get(i);
            String label = region.displayName();
            if (!label.isBlank() && region.contains(dimension, chunkX, chunkZ)) {
                tooltip.add(Component.literal(label));
                return;
            }
        }

        byte band = ChunkBandClientCache.getBand(dimension.toString(), chunkX, chunkZ);
        if (isCivilizedOverlayBand(band)) {
            tooltip.add(Component.literal(band == CIVILIZED_MONSTER ? "Civillis MONSTER" : "Civillis HIGH"));
        }
    }

    public static void drawMinimapOverlay(GuiGraphics graphics, float partialTick) {
        if (!ClientOverlayConfig.MINIMAP_ENABLED.get()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || minecraft.level == null
                || minecraft.options.hideGui
                || !FTBChunksClientConfig.MINIMAP_ENABLED.get()
                || FTBChunksClientConfig.MINIMAP_VISIBILITY.get() == 0
                || !FTBChunksWorldConfig.shouldShowMinimap(minecraft.player)) {
            return;
        }

        ResourceLocation dimension = minecraft.level.dimension().location();
        if (!isOverlayDimensionEnabled(dimension)) {
            return;
        }

        String dimensionId = dimension.toString();
        MapDimension mapDimension = currentMapDimension(dimension);
        float zoom = Math.max(0.1F, FTBChunksClient.INSTANCE.getZoom());
        int minimapSize = minimapSize(minecraft);
        if (minimapSize <= 0) {
            return;
        }
        MinimapClip clip = new MinimapClip(minimapSize / 2, FTBChunksClientConfig.SQUARE_MINIMAP.get());

        double playerX = Mth.lerp(partialTick, minecraft.player.xOld, minecraft.player.getX());
        double playerZ = Mth.lerp(partialTick, minecraft.player.zOld, minecraft.player.getZ());
        double playerChunkX = playerX / 16.0D;
        double playerChunkZ = playerZ / 16.0D;
        float visibleChunks = 14.0F / zoom;
        float pixelsPerChunk = minimapSize / visibleChunks;
        int radius = Mth.ceil(visibleChunks / 2.0F) + 2;
        int centerChunkX = Mth.floor(playerChunkX);
        int centerChunkZ = Mth.floor(playerChunkZ);

        setupOverlayDrawing();
        for (int chunkZ = centerChunkZ - radius; chunkZ <= centerChunkZ + radius; chunkZ++) {
            for (int chunkX = centerChunkX - radius; chunkX <= centerChunkX + radius; chunkX++) {
                byte band = ChunkBandClientCache.getBand(dimensionId, chunkX, chunkZ);
                if (!isCivilizedOverlayBand(band) || isFtbClaimed(mapDimension, chunkX, chunkZ)) {
                    continue;
                }
                drawChunkLocal(graphics, chunkX, chunkZ, playerChunkX, playerChunkZ, pixelsPerChunk, colorForBand(band), clip);
                drawCivilChunkLocalBorder(
                        graphics,
                        mapDimension,
                        dimensionId,
                        chunkX,
                        chunkZ,
                        band,
                        playerChunkX,
                        playerChunkZ,
                        pixelsPerChunk,
                        clip);
            }
        }

        drawCustomRegionsOnMinimap(
                graphics,
                CustomRegionClientCache.regionsForDimension(dimension),
                centerChunkX - radius,
                centerChunkX + radius,
                centerChunkZ - radius,
                centerChunkZ + radius,
                playerChunkX,
                playerChunkZ,
                pixelsPerChunk,
                mapDimension,
                dimension,
                clip);
    }

    private static void drawCivilizedChunks(
            GuiGraphics graphics,
            MapRegion mapRegion,
            String dimensionId,
            int baseChunkX,
            int baseChunkZ,
            int x,
            int y,
            int width,
            int height) {
        for (int localZ = 0; localZ < REGION_SIZE_CHUNKS; localZ++) {
            for (int localX = 0; localX < REGION_SIZE_CHUNKS; localX++) {
                int chunkX = baseChunkX + localX;
                int chunkZ = baseChunkZ + localZ;
                byte band = ChunkBandClientCache.getBand(dimensionId, chunkX, chunkZ);
                if (!isCivilizedOverlayBand(band) || isFtbClaimed(mapRegion, chunkX, chunkZ)) {
                    continue;
                }
                drawChunk(graphics, x, y, width, height, localX, localZ, colorForBand(band));
            }
        }

        for (int localZ = 0; localZ < REGION_SIZE_CHUNKS; localZ++) {
            for (int localX = 0; localX < REGION_SIZE_CHUNKS; localX++) {
                int chunkX = baseChunkX + localX;
                int chunkZ = baseChunkZ + localZ;
                byte band = ChunkBandClientCache.getBand(dimensionId, chunkX, chunkZ);
                if (!isCivilizedOverlayBand(band) || isFtbClaimed(mapRegion, chunkX, chunkZ)) {
                    continue;
                }
                drawCivilChunkBorder(graphics, mapRegion, dimensionId, chunkX, chunkZ, band, x, y, width, height, localX, localZ);
            }
        }
    }

    private static void drawCustomRegionsOnTile(
            GuiGraphics graphics,
            MapRegion mapRegion,
            ResourceLocation dimension,
            int baseChunkX,
            int baseChunkZ,
            int x,
            int y,
            int width,
            int height) {
        List<CustomRegion> regions = CustomRegionClientCache.regionsForDimension(dimension);
        if (regions.isEmpty()) {
            return;
        }

        int tileMaxChunkX = baseChunkX + REGION_SIZE_CHUNKS - 1;
        int tileMaxChunkZ = baseChunkZ + REGION_SIZE_CHUNKS - 1;
        for (CustomRegion region : regions) {
            int minX = Math.max(region.minChunkX(), baseChunkX);
            int maxX = Math.min(region.maxChunkX(), tileMaxChunkX);
            int minZ = Math.max(region.minChunkZ(), baseChunkZ);
            int maxZ = Math.min(region.maxChunkZ(), tileMaxChunkZ);
            if (minX > maxX || minZ > maxZ) {
                continue;
            }
            int rgb = colorForCustom(region);
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                for (int chunkX = minX; chunkX <= maxX; chunkX++) {
                    if (isFtbClaimed(mapRegion, chunkX, chunkZ)) {
                        continue;
                    }
                    drawChunk(graphics, x, y, width, height, chunkX - baseChunkX, chunkZ - baseChunkZ, rgb);
                }
            }
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                for (int chunkX = minX; chunkX <= maxX; chunkX++) {
                    if (!isCustomChunkVisible(region, mapRegion, dimension, chunkX, chunkZ)) {
                        continue;
                    }
                    int x1 = chunkEdge(x, width, chunkX - baseChunkX);
                    int x2 = chunkEdge(x, width, chunkX - baseChunkX + 1);
                    int y1 = chunkEdge(y, height, chunkZ - baseChunkZ);
                    int y2 = chunkEdge(y, height, chunkZ - baseChunkZ + 1);
                    drawCustomEdges(graphics, region, mapRegion, dimension, chunkX, chunkZ, x1, y1, x2, y2, rgb);
                }
            }
        }
    }

    private static void drawCustomRegionsOnMinimap(
            GuiGraphics graphics,
            List<CustomRegion> regions,
            int visibleMinChunkX,
            int visibleMaxChunkX,
            int visibleMinChunkZ,
            int visibleMaxChunkZ,
            double playerChunkX,
            double playerChunkZ,
            float pixelsPerChunk,
            MapDimension mapDimension,
            ResourceLocation dimension,
            MinimapClip clip) {
        for (CustomRegion region : regions) {
            int minX = Math.max(region.minChunkX(), visibleMinChunkX);
            int maxX = Math.min(region.maxChunkX(), visibleMaxChunkX);
            int minZ = Math.max(region.minChunkZ(), visibleMinChunkZ);
            int maxZ = Math.min(region.maxChunkZ(), visibleMaxChunkZ);
            if (minX > maxX || minZ > maxZ) {
                continue;
            }
            int rgb = colorForCustom(region);
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                for (int chunkX = minX; chunkX <= maxX; chunkX++) {
                    if (isFtbClaimed(mapDimension, chunkX, chunkZ)) {
                        continue;
                    }
                    drawChunkLocal(graphics, chunkX, chunkZ, playerChunkX, playerChunkZ, pixelsPerChunk, rgb, clip);
                }
            }
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                for (int chunkX = minX; chunkX <= maxX; chunkX++) {
                    if (!isCustomChunkVisible(region, mapDimension, dimension, chunkX, chunkZ)) {
                        continue;
                    }
                    float x1 = (float) ((chunkX - playerChunkX) * pixelsPerChunk);
                    float x2 = (float) (((chunkX + 1) - playerChunkX) * pixelsPerChunk);
                    float y1 = (float) ((chunkZ - playerChunkZ) * pixelsPerChunk);
                    float y2 = (float) (((chunkZ + 1) - playerChunkZ) * pixelsPerChunk);
                    drawCustomEdges(graphics, region, mapDimension, dimension, chunkX, chunkZ, x1, y1, x2, y2, rgb, clip);
                }
            }
        }
    }

    private static void drawChunk(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int localX,
            int localZ,
            int rgb) {
        int x1 = chunkEdge(x, width, localX);
        int x2 = chunkEdge(x, width, localX + 1);
        int y1 = chunkEdge(y, height, localZ);
        int y2 = chunkEdge(y, height, localZ + 1);
        drawRect(graphics, x1, y1, x2, y2, rgb);
    }

    private static void drawChunkLocal(
            GuiGraphics graphics,
            int chunkX,
            int chunkZ,
            double playerChunkX,
            double playerChunkZ,
            float pixelsPerChunk,
            int rgb,
            MinimapClip clip) {
        float x1 = (float) ((chunkX - playerChunkX) * pixelsPerChunk);
        float x2 = (float) (((chunkX + 1) - playerChunkX) * pixelsPerChunk);
        float y1 = (float) ((chunkZ - playerChunkZ) * pixelsPerChunk);
        float y2 = (float) (((chunkZ + 1) - playerChunkZ) * pixelsPerChunk);
        drawMinimapRect(graphics, x1, y1, x2, y2, rgb, ClientOverlayConfig.FILL_ALPHA.get(), clip);
    }

    private static void drawCivilChunkBorder(
            GuiGraphics graphics,
            MapRegion mapRegion,
            String dimensionId,
            int chunkX,
            int chunkZ,
            byte band,
            int x,
            int y,
            int width,
            int height,
            int localX,
            int localZ) {
        int x1 = chunkEdge(x, width, localX);
        int x2 = chunkEdge(x, width, localX + 1);
        int y1 = chunkEdge(y, height, localZ);
        int y2 = chunkEdge(y, height, localZ + 1);
        drawCivilEdges(graphics, mapRegion, dimensionId, chunkX, chunkZ, band, x1, y1, x2, y2, colorForBand(band));
    }

    private static void drawCivilChunkLocalBorder(
            GuiGraphics graphics,
            MapDimension mapDimension,
            String dimensionId,
            int chunkX,
            int chunkZ,
            byte band,
            double playerChunkX,
            double playerChunkZ,
            float pixelsPerChunk,
            MinimapClip clip) {
        float x1 = (float) ((chunkX - playerChunkX) * pixelsPerChunk);
        float x2 = (float) (((chunkX + 1) - playerChunkX) * pixelsPerChunk);
        float y1 = (float) ((chunkZ - playerChunkZ) * pixelsPerChunk);
        float y2 = (float) (((chunkZ + 1) - playerChunkZ) * pixelsPerChunk);
        drawCivilEdges(graphics, mapDimension, dimensionId, chunkX, chunkZ, band, x1, y1, x2, y2, colorForBand(band), clip);
    }

    private static void drawCivilEdges(
            GuiGraphics graphics,
            MapRegion mapRegion,
            String dimensionId,
            int chunkX,
            int chunkZ,
            byte band,
            int x1,
            int y1,
            int x2,
            int y2,
            int rgb) {
        int border = RegionColors.withAlpha(rgb, ClientOverlayConfig.BORDER_ALPHA.get());
        if (ChunkBandClientCache.getBand(dimensionId, chunkX, chunkZ - 1) != band
                || isFtbClaimed(mapRegion, chunkX, chunkZ - 1)) {
            graphics.fill(x1, y1, Math.max(x1 + 1, x2), y1 + 1, border);
        }
        if (ChunkBandClientCache.getBand(dimensionId, chunkX, chunkZ + 1) != band
                || isFtbClaimed(mapRegion, chunkX, chunkZ + 1)) {
            graphics.fill(x1, y2 - 1, Math.max(x1 + 1, x2), y2, border);
        }
        if (ChunkBandClientCache.getBand(dimensionId, chunkX - 1, chunkZ) != band
                || isFtbClaimed(mapRegion, chunkX - 1, chunkZ)) {
            graphics.fill(x1, y1, x1 + 1, Math.max(y1 + 1, y2), border);
        }
        if (ChunkBandClientCache.getBand(dimensionId, chunkX + 1, chunkZ) != band
                || isFtbClaimed(mapRegion, chunkX + 1, chunkZ)) {
            graphics.fill(x2 - 1, y1, x2, Math.max(y1 + 1, y2), border);
        }
    }

    private static void drawCivilEdges(
            GuiGraphics graphics,
            MapDimension mapDimension,
            String dimensionId,
            int chunkX,
            int chunkZ,
            byte band,
            float x1,
            float y1,
            float x2,
            float y2,
            int rgb,
            MinimapClip clip) {
        int border = RegionColors.withAlpha(rgb, ClientOverlayConfig.BORDER_ALPHA.get());
        if (ChunkBandClientCache.getBand(dimensionId, chunkX, chunkZ - 1) != band
                || isFtbClaimed(mapDimension, chunkX, chunkZ - 1)) {
            drawMinimapRect(graphics, x1, y1, Math.max(x1 + 1.0F, x2), y1 + 1.0F, border, clip);
        }
        if (ChunkBandClientCache.getBand(dimensionId, chunkX, chunkZ + 1) != band
                || isFtbClaimed(mapDimension, chunkX, chunkZ + 1)) {
            drawMinimapRect(graphics, x1, y2 - 1.0F, Math.max(x1 + 1.0F, x2), y2, border, clip);
        }
        if (ChunkBandClientCache.getBand(dimensionId, chunkX - 1, chunkZ) != band
                || isFtbClaimed(mapDimension, chunkX - 1, chunkZ)) {
            drawMinimapRect(graphics, x1, y1, x1 + 1.0F, Math.max(y1 + 1.0F, y2), border, clip);
        }
        if (ChunkBandClientCache.getBand(dimensionId, chunkX + 1, chunkZ) != band
                || isFtbClaimed(mapDimension, chunkX + 1, chunkZ)) {
            drawMinimapRect(graphics, x2 - 1.0F, y1, x2, Math.max(y1 + 1.0F, y2), border, clip);
        }
    }

    private static void drawRect(GuiGraphics graphics, int x1, int y1, int x2, int y2, int rgb) {
        if (x2 <= x1) {
            x2 = x1 + 1;
        }
        if (y2 <= y1) {
            y2 = y1 + 1;
        }
        graphics.fill(x1, y1, x2, y2, RegionColors.withAlpha(rgb, ClientOverlayConfig.FILL_ALPHA.get()));
    }

    private static void drawMinimapRect(
            GuiGraphics graphics,
            float x1,
            float y1,
            float x2,
            float y2,
            int rgb,
            int alpha,
            MinimapClip clip) {
        drawMinimapRect(graphics, x1, y1, x2, y2, RegionColors.withAlpha(rgb, alpha), clip);
    }

    private static void drawMinimapRect(
            GuiGraphics graphics,
            float x1,
            float y1,
            float x2,
            float y2,
            int argb,
            MinimapClip clip) {
        if (x2 <= x1) {
            x2 = x1 + 1.0F;
        }
        if (y2 <= y1) {
            y2 = y1 + 1.0F;
        }

        float radius = clip.radius();
        x1 = Mth.clamp(x1, -radius, radius);
        x2 = Mth.clamp(x2, -radius, radius);
        y1 = Mth.clamp(y1, -radius, radius);
        y2 = Mth.clamp(y2, -radius, radius);
        if (x2 <= x1 || y2 <= y1) {
            return;
        }

        if (clip.square()) {
            drawFloatRect(graphics, x1, y1, x2, y2, argb);
            return;
        }

        float radiusSq = radius * radius;
        if (isRectInsideCircle(x1, y1, x2, y2, radiusSq)) {
            drawFloatRect(graphics, x1, y1, x2, y2, argb);
            return;
        }
        if (!rectIntersectsCircle(x1, y1, x2, y2, radiusSq)) {
            return;
        }

        int rowStart = Mth.floor(y1);
        int rowEnd = Mth.ceil(y2);
        for (int y = rowStart; y < rowEnd; y++) {
            float rowTop = Math.max(y, y1);
            float rowBottom = Math.min(y + 1.0F, y2);
            float sampleY = (rowTop + rowBottom) * 0.5F;
            float insideSq = radiusSq - sampleY * sampleY;
            if (insideSq <= 0.0F) {
                continue;
            }

            float halfWidth = (float) Math.sqrt(insideSq);
            float rowX1 = Math.max(x1, -halfWidth);
            float rowX2 = Math.min(x2, halfWidth);
            if (rowX2 > rowX1 && rowBottom > rowTop) {
                drawFloatRect(graphics, rowX1, rowTop, rowX2, rowBottom, argb);
            }
        }
    }

    private static void drawFloatRect(GuiGraphics graphics, float x1, float y1, float x2, float y2, int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha == 0 || x2 <= x1 || y2 <= y1) {
            return;
        }

        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;
        Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, x1, y2, 0.0F).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x2, y2, 0.0F).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x2, y1, 0.0F).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x1, y1, 0.0F).color(red, green, blue, alpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }

    private static boolean isRectInsideCircle(float x1, float y1, float x2, float y2, float radiusSq) {
        return x1 * x1 + y1 * y1 <= radiusSq
                && x2 * x2 + y1 * y1 <= radiusSq
                && x1 * x1 + y2 * y2 <= radiusSq
                && x2 * x2 + y2 * y2 <= radiusSq;
    }

    private static boolean rectIntersectsCircle(float x1, float y1, float x2, float y2, float radiusSq) {
        float closestX = Mth.clamp(0.0F, x1, x2);
        float closestY = Mth.clamp(0.0F, y1, y2);
        return closestX * closestX + closestY * closestY <= radiusSq;
    }

    private static void drawCustomEdges(
            GuiGraphics graphics,
            CustomRegion region,
            MapRegion mapRegion,
            ResourceLocation dimension,
            int chunkX,
            int chunkZ,
            int x1,
            int y1,
            int x2,
            int y2,
            int rgb) {
        int border = RegionColors.withAlpha(rgb, ClientOverlayConfig.BORDER_ALPHA.get());
        if (!isCustomChunkVisible(region, mapRegion, dimension, chunkX, chunkZ - 1)) {
            graphics.fill(x1, y1, Math.max(x1 + 1, x2), y1 + 1, border);
        }
        if (!isCustomChunkVisible(region, mapRegion, dimension, chunkX, chunkZ + 1)) {
            graphics.fill(x1, y2 - 1, Math.max(x1 + 1, x2), y2, border);
        }
        if (!isCustomChunkVisible(region, mapRegion, dimension, chunkX - 1, chunkZ)) {
            graphics.fill(x1, y1, x1 + 1, Math.max(y1 + 1, y2), border);
        }
        if (!isCustomChunkVisible(region, mapRegion, dimension, chunkX + 1, chunkZ)) {
            graphics.fill(x2 - 1, y1, x2, Math.max(y1 + 1, y2), border);
        }
    }

    private static void drawCustomEdges(
            GuiGraphics graphics,
            CustomRegion region,
            MapDimension mapDimension,
            ResourceLocation dimension,
            int chunkX,
            int chunkZ,
            float x1,
            float y1,
            float x2,
            float y2,
            int rgb,
            MinimapClip clip) {
        int border = RegionColors.withAlpha(rgb, ClientOverlayConfig.BORDER_ALPHA.get());
        if (!isCustomChunkVisible(region, mapDimension, dimension, chunkX, chunkZ - 1)) {
            drawMinimapRect(graphics, x1, y1, Math.max(x1 + 1.0F, x2), y1 + 1.0F, border, clip);
        }
        if (!isCustomChunkVisible(region, mapDimension, dimension, chunkX, chunkZ + 1)) {
            drawMinimapRect(graphics, x1, y2 - 1.0F, Math.max(x1 + 1.0F, x2), y2, border, clip);
        }
        if (!isCustomChunkVisible(region, mapDimension, dimension, chunkX - 1, chunkZ)) {
            drawMinimapRect(graphics, x1, y1, x1 + 1.0F, Math.max(y1 + 1.0F, y2), border, clip);
        }
        if (!isCustomChunkVisible(region, mapDimension, dimension, chunkX + 1, chunkZ)) {
            drawMinimapRect(graphics, x2 - 1.0F, y1, x2, Math.max(y1 + 1.0F, y2), border, clip);
        }
    }

    private static int chunkEdge(int origin, int size, int localChunkEdge) {
        return origin + Math.round(localChunkEdge * (size / (float) REGION_SIZE_CHUNKS));
    }

    private static boolean isCivilizedOverlayBand(byte band) {
        return band == CIVILIZED_HIGH || band == CIVILIZED_MONSTER;
    }

    private static int colorForBand(byte band) {
        return band == CIVILIZED_MONSTER
                ? ClientOverlayConfig.civilizedMonsterRgb()
                : ClientOverlayConfig.civilizedHighRgb();
    }

    private static int colorForCustom(CustomRegion region) {
        return region.overlayColor() >= 0 ? region.overlayColor() : ClientOverlayConfig.defaultCustomRgb();
    }

    private static boolean isCustomChunkVisible(
            CustomRegion region,
            MapRegion mapRegion,
            ResourceLocation dimension,
            int chunkX,
            int chunkZ) {
        return region.contains(dimension, chunkX, chunkZ) && !isFtbClaimed(mapRegion, chunkX, chunkZ);
    }

    private static boolean isCustomChunkVisible(
            CustomRegion region,
            MapDimension mapDimension,
            ResourceLocation dimension,
            int chunkX,
            int chunkZ) {
        return region.contains(dimension, chunkX, chunkZ) && !isFtbClaimed(mapDimension, chunkX, chunkZ);
    }

    private static boolean isFtbClaimed(MapRegion mapRegion, int chunkX, int chunkZ) {
        if (mapRegion == null) {
            return false;
        }
        return isFtbClaimed(mapRegion.dimension, chunkX, chunkZ);
    }

    private static boolean isFtbClaimed(MapDimension mapDimension, int chunkX, int chunkZ) {
        if (mapDimension == null) {
            return false;
        }
        MapRegion region = mapDimension.getRegions().get(XZ.regionFromChunk(chunkX, chunkZ));
        if (region == null) {
            return false;
        }
        MapChunk chunk = region.getMapChunk(XZ.of(chunkX & 31, chunkZ & 31));
        return chunk != null && chunk.getTeam().isPresent();
    }

    private static MapDimension currentMapDimension(ResourceLocation dimension) {
        return MapDimension.getCurrent()
                .filter(mapDimension -> mapDimension.dimension.location().equals(dimension))
                .orElse(null);
    }

    private static boolean isOverlayDimensionEnabled(ResourceLocation dimension) {
        try {
            return DimensionPolicyRegistry.policyFor(dimension.toString()).civilization();
        } catch (RuntimeException ignored) {
            return true;
        }
    }

    private static int minimapSize(Minecraft minecraft) {
        double guiScale = minecraft.getWindow().getGuiScale();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        double configScale = FTBChunksClientConfig.MINIMAP_SCALE.get();
        float scale;
        if (FTBChunksClientConfig.MINIMAP_PROPORTIONAL.get()) {
            scale = (float) ((screenWidth / 640.0D) * configScale);
        } else {
            scale = (float) (configScale * 4.0D / guiScale);
        }
        return Mth.floor(64.0F * scale);
    }

    private static void setupOverlayDrawing() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private record MinimapClip(int radius, boolean square) {}
}
