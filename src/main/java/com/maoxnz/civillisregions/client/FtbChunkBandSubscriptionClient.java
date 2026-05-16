package com.maoxnz.civillisregions.client;

import civil.compat.bandsync.ChunkBandSubscriptionHub;
import com.maoxnz.civillisregions.config.ClientOverlayConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;

public final class FtbChunkBandSubscriptionClient {
    private static final String CONSUMER_ID = "civil_custom_regions:ftb_chunks";
    private static boolean held;

    private FtbChunkBandSubscriptionClient() {}

    public static void tick() {
        if (!ClientOverlayConfig.anyFtbOverlayEnabled()) {
            releaseIfHeld();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        boolean shouldHold = minecraft.level != null && ModList.get().isLoaded("ftbchunks");
        if (!shouldHold) {
            releaseIfHeld();
            return;
        }

        try {
            if (!ChunkBandSubscriptionHub.isHeld(CONSUMER_ID)) {
                ChunkBandSubscriptionHub.acquire(CONSUMER_ID);
            }
            held = ChunkBandSubscriptionHub.isHeld(CONSUMER_ID);
        } catch (LinkageError | RuntimeException ignored) {
            held = false;
        }
    }

    private static void releaseIfHeld() {
        if (!held) {
            return;
        }
        try {
            if (ChunkBandSubscriptionHub.isHeld(CONSUMER_ID)) {
                ChunkBandSubscriptionHub.release(CONSUMER_ID);
            }
        } catch (LinkageError | RuntimeException ignored) {
            // Overlay support is optional; failing here must not affect HUD notices.
        } finally {
            held = false;
        }
    }
}
