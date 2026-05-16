package com.maoxnz.civillisregions.client;

import com.maoxnz.civillisregions.CivilCustomRegionsMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CivilCustomRegionsMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientHudEvents {
    private ClientHudEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            try {
                FtbChunkBandSubscriptionClient.tick();
            } catch (LinkageError | RuntimeException ignored) {
                // Optional map overlay support must not break the custom region HUD queue.
            }
            CustomRegionNoticeHud.tick();
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!VanillaGuiOverlay.CROSSHAIR.id().equals(event.getOverlay().id())) {
            return;
        }
        CustomRegionNoticeHud.render(event.getGuiGraphics(), event.getPartialTick());
    }
}
