package com.maoxnz.civillisregions.net;

import com.maoxnz.civillisregions.CustomRegion;
import com.maoxnz.civillisregions.client.CustomRegionClientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class CustomRegionSyncPacket {
    private static final int MAX_REGIONS = 4096;

    private final List<CustomRegion> regions;

    public CustomRegionSyncPacket(List<CustomRegion> regions) {
        this.regions = List.copyOf(regions);
    }

    public List<CustomRegion> regions() {
        return regions;
    }

    public static void encode(CustomRegionSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.regions.size());
        for (CustomRegion region : packet.regions) {
            buf.writeUtf(region.id(), 128);
            buf.writeResourceLocation(region.dimension());
            buf.writeInt(region.minChunkX());
            buf.writeInt(region.maxChunkX());
            buf.writeInt(region.minChunkZ());
            buf.writeInt(region.maxChunkZ());
            buf.writeUtf(region.enterText(), 32767);
            buf.writeUtf(region.leaveText(), 32767);
            buf.writeUtf(region.displayName(), 32767);
            buf.writeInt(region.overlayColor());
            buf.writeInt(region.enterColor());
            buf.writeInt(region.leaveColor());
        }
    }

    public static CustomRegionSyncPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        if (size > MAX_REGIONS) {
            throw new IllegalArgumentException("Too many custom regions in sync packet: " + size);
        }
        ArrayList<CustomRegion> regions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String id = buf.readUtf(128);
            ResourceLocation dimension = buf.readResourceLocation();
            int minChunkX = buf.readInt();
            int maxChunkX = buf.readInt();
            int minChunkZ = buf.readInt();
            int maxChunkZ = buf.readInt();
            String enterText = buf.readUtf(32767);
            String leaveText = buf.readUtf(32767);
            String displayName = buf.readUtf(32767);
            int overlayColor = buf.readInt();
            int enterColor = buf.readInt();
            int leaveColor = buf.readInt();
            regions.add(new CustomRegion(
                    id,
                    dimension,
                    minChunkX,
                    maxChunkX,
                    minChunkZ,
                    maxChunkZ,
                    enterText,
                    leaveText,
                    displayName,
                    overlayColor,
                    enterColor,
                    leaveColor));
        }
        return new CustomRegionSyncPacket(regions);
    }

    public static void handle(CustomRegionSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> CustomRegionClientCache.replace(packet.regions)));
        context.setPacketHandled(true);
    }
}
