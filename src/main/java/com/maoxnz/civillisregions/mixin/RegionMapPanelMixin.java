package com.maoxnz.civillisregions.mixin;

import com.maoxnz.civillisregions.client.FtbChunkOverlayRenderer;
import dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen;
import dev.ftb.mods.ftbchunks.client.gui.RegionMapPanel;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RegionMapPanel.class, remap = false)
public abstract class RegionMapPanelMixin {
    @Shadow
    @Final
    private LargeMapScreen largeMap;

    @Shadow
    private int blockX;

    @Shadow
    private int blockZ;

    @Inject(method = "addMouseOverText", at = @At("TAIL"), remap = false)
    private void civilCustomRegions$addRegionOverlayTooltip(TooltipList tooltip, CallbackInfo ci) {
        FtbChunkOverlayRenderer.addLargeMapMouseOverText(tooltip, largeMap, blockX, blockZ);
    }
}
