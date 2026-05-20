package com.maoxnz.civillisregions.net;

import com.maoxnz.civillisregions.CivilCustomRegionsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "3";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(CivilCustomRegionsMod.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    private static int nextId;

    private ModNetwork() {}

    public static void register() {
        CHANNEL.messageBuilder(CustomRegionNoticePacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CustomRegionNoticePacket::encode)
                .decoder(CustomRegionNoticePacket::decode)
                .consumerMainThread(CustomRegionNoticePacket::handle)
                .add();
        CHANNEL.messageBuilder(CustomRegionSyncPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CustomRegionSyncPacket::encode)
                .decoder(CustomRegionSyncPacket::decode)
                .consumerMainThread(CustomRegionSyncPacket::handle)
                .add();
    }
}
