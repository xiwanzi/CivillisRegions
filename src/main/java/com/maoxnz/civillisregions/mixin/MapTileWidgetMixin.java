package com.maoxnz.civillisregions.mixin;

import com.maoxnz.civillisregions.client.FtbChunkOverlayRenderer;
import dev.ftb.mods.ftbchunks.client.gui.MapTileWidget;
import dev.ftb.mods.ftbchunks.client.map.MapRegion;
import dev.ftb.mods.ftblibrary.ui.Theme;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MapTileWidget.class, remap = false)
public abstract class MapTileWidgetMixin {
    @Shadow
    @Final
    public MapRegion region;

    @Inject(method = "draw", at = @At("TAIL"), remap = false)
    private void civilCustomRegions$drawRegionOverlay(
            GuiGraphics graphics,
            Theme theme,
            int x,
            int y,
            int width,
            int height,
            CallbackInfo ci) {
        FtbChunkOverlayRenderer.drawLargeMapTile(graphics, region, x, y, width, height);
    }
}
