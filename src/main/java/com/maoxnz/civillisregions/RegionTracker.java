package com.maoxnz.civillisregions;

import com.maoxnz.civillisregions.net.CustomRegionNoticePacket;
import com.maoxnz.civillisregions.net.ModNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CivilCustomRegionsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RegionTracker {
    private static final Map<UUID, PlayerRegionState> PLAYER_STATES = new HashMap<>();

    private RegionTracker() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        RegionCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer() == null) {
            return;
        }

        CustomRegionSavedData data = CustomRegionSavedData.get(event.getServer());
        long revision = data.revision();
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            tickPlayer(player, data, revision);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PLAYER_STATES.remove(event.getEntity().getUUID());
    }

    private static void tickPlayer(ServerPlayer player, CustomRegionSavedData data, long revision) {
        UUID playerId = player.getUUID();
        ResourceLocation dimension = player.level().dimension().location();
        int chunkX = player.blockPosition().getX() >> 4;
        int chunkZ = player.blockPosition().getZ() >> 4;
        Set<String> activeNow = matchingRegionIds(data, dimension, chunkX, chunkZ);

        PlayerRegionState old = PLAYER_STATES.get(playerId);
        if (old == null || old.dataRevision != revision) {
            PLAYER_STATES.put(playerId, new PlayerRegionState(dimension, chunkX, chunkZ, activeNow, revision));
            return;
        }

        if (old.dimension.equals(dimension) && old.chunkX == chunkX && old.chunkZ == chunkZ) {
            return;
        }

        sendNotices(player, data, old.activeRegionIds, activeNow);
        PLAYER_STATES.put(playerId, new PlayerRegionState(dimension, chunkX, chunkZ, activeNow, revision));
    }

    private static Set<String> matchingRegionIds(
            CustomRegionSavedData data,
            ResourceLocation dimension,
            int chunkX,
            int chunkZ) {
        HashSet<String> ids = new HashSet<>();
        for (CustomRegion region : data.regions()) {
            if (region.contains(dimension, chunkX, chunkZ)) {
                ids.add(region.id());
            }
        }
        return ids;
    }

    private static void sendNotices(
            ServerPlayer player,
            CustomRegionSavedData data,
            Set<String> previous,
            Set<String> current) {
        ArrayList<String> exited = new ArrayList<>(previous);
        exited.removeAll(current);
        exited.sort(String::compareTo);

        ArrayList<String> entered = new ArrayList<>(current);
        entered.removeAll(previous);
        entered.sort(String::compareTo);

        for (String id : exited) {
            CustomRegion region = data.getRegion(id);
            if (region != null && region.hasLeaveText()) {
                send(player, NoticeKind.LEAVE, region.leaveText());
            }
        }
        for (String id : entered) {
            CustomRegion region = data.getRegion(id);
            if (region != null && region.hasEnterText()) {
                send(player, NoticeKind.ENTER, region.enterText());
            }
        }
    }

    private static void send(ServerPlayer player, NoticeKind kind, String text) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new CustomRegionNoticePacket(kind, text));
    }

    private record PlayerRegionState(
            ResourceLocation dimension,
            int chunkX,
            int chunkZ,
            Set<String> activeRegionIds,
            long dataRevision) {
        private PlayerRegionState {
            activeRegionIds = Set.copyOf(activeRegionIds);
        }
    }
}
