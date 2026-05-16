package com.maoxnz.civillisregions.net;

import com.maoxnz.civillisregions.NoticeKind;
import com.maoxnz.civillisregions.client.CustomRegionNoticeHud;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class CustomRegionNoticePacket {
    private final NoticeKind kind;
    private final String text;
    private final int color;

    public CustomRegionNoticePacket(NoticeKind kind, String text) {
        this(kind, text, -1);
    }

    public CustomRegionNoticePacket(NoticeKind kind, String text, int color) {
        this.kind = kind;
        this.text = text == null ? "" : text;
        this.color = color;
    }

    public NoticeKind kind() {
        return kind;
    }

    public String text() {
        return text;
    }

    public int color() {
        return color;
    }

    public static void encode(CustomRegionNoticePacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.kind);
        buf.writeUtf(packet.text, 32767);
        buf.writeInt(packet.color);
    }

    public static CustomRegionNoticePacket decode(FriendlyByteBuf buf) {
        return new CustomRegionNoticePacket(buf.readEnum(NoticeKind.class), buf.readUtf(32767), buf.readInt());
    }

    public static void handle(CustomRegionNoticePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> CustomRegionNoticeHud.enqueue(packet.kind, packet.text, packet.color)));
        context.setPacketHandled(true);
    }
}
