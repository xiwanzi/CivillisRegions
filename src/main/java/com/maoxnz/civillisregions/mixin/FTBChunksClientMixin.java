package com.maoxnz.civillisregions.mixin;

import com.maoxnz.civillisregions.client.FtbChunkOverlayRenderer;
import dev.ftb.mods.ftbchunks.client.FTBChunksClient;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FTBChunksClient.class, remap = false)
public abstract class FTBChunksClientMixin {
    @Inject(
            method = "renderHud",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableDepthTest()V",
                    ordinal = 0),
            remap = false)
    private void civilCustomRegions$drawMinimapOverlay(GuiGraphics graphics, float partialTick, CallbackInfo ci) {
        FtbChunkOverlayRenderer.drawMinimapOverlay(graphics, partialTick);
    }
}
