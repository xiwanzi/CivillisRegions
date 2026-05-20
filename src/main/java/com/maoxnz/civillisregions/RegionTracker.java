package com.maoxnz.civillisregions;

import com.maoxnz.civillisregions.net.CustomRegionNoticePacket;
import com.maoxnz.civillisregions.net.CustomRegionSyncPacket;
import com.maoxnz.civillisregions.net.ModNetwork;
import com.maoxnz.civillisregions.config.ServerNoticeConfig;
import net.minecraft.core.BlockPos;
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
        long mapRevision = data.mapRevision();
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            tickPlayer(player, data, revision, mapRevision);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PLAYER_STATES.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendSnapshot(player, CustomRegionSavedData.get(player.getServer()));
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendSnapshot(player, CustomRegionSavedData.get(player.getServer()));
        }
    }

    private static void tickPlayer(
            ServerPlayer player,
            CustomRegionSavedData data,
            long revision,
            long mapRevision) {
        UUID playerId = player.getUUID();
        ResourceLocation dimension = player.level().dimension().location();
        BlockPos pos = player.blockPosition();
        int blockX = pos.getX();
        int blockY = pos.getY();
        int blockZ = pos.getZ();
        PlayerRegionState old = PLAYER_STATES.get(playerId);
        boolean sameDimension = old != null && old.dimension.equals(dimension);

        if (old == null || !sameDimension || old.mapRevision != mapRevision) {
            sendSnapshot(player, data);
        }

        if (old != null
                && sameDimension
                && old.dataRevision == revision
                && old.mapRevision == mapRevision
                && old.blockX == blockX
                && old.blockY == blockY
                && old.blockZ == blockZ) {
            return;
        }

        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        Set<String> activeRegionsNow = matchingRegionIds(data, dimension, chunkX, chunkZ);
        Set<String> activeSubRegionsNow = matchingSubRegionIds(data, dimension, blockX, blockY, blockZ);

        if (old == null
                || !sameDimension
                || old.dataRevision != revision
                || (old.blockX == blockX && old.blockY == blockY && old.blockZ == blockZ)) {
            PLAYER_STATES.put(playerId, new PlayerRegionState(
                    dimension,
                    blockX,
                    blockY,
                    blockZ,
                    activeRegionsNow,
                    activeSubRegionsNow,
                    revision,
                    mapRevision));
            return;
        }

        sendNotices(
                player,
                data,
                old.activeRegionIds,
                activeRegionsNow,
                old.activeSubRegionIds,
                activeSubRegionsNow);
        PLAYER_STATES.put(playerId, new PlayerRegionState(
                dimension,
                blockX,
                blockY,
                blockZ,
                activeRegionsNow,
                activeSubRegionsNow,
                revision,
                mapRevision));
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

    private static Set<String> matchingSubRegionIds(
            CustomRegionSavedData data,
            ResourceLocation dimension,
            int blockX,
            int blockY,
            int blockZ) {
        HashSet<String> ids = new HashSet<>();
        data.forEachSubRegion(subRegion -> {
            CustomRegion parent = data.getRegion(subRegion.parentId());
            if (subRegion.contains(dimension, parent, blockX, blockY, blockZ)) {
                ids.add(subRegion.qualifiedId());
            }
        });
        return ids;
    }

    private static void sendNotices(
            ServerPlayer player,
            CustomRegionSavedData data,
            Set<String> previous,
            Set<String> current,
            Set<String> previousSubRegions,
            Set<String> currentSubRegions) {
        ArrayList<String> exitedSubRegions = new ArrayList<>(previousSubRegions);
        exitedSubRegions.removeAll(currentSubRegions);
        exitedSubRegions.sort(String::compareTo);

        ArrayList<String> exitedRegions = new ArrayList<>(previous);
        exitedRegions.removeAll(current);
        exitedRegions.sort(String::compareTo);

        ArrayList<String> enteredRegions = new ArrayList<>(current);
        enteredRegions.removeAll(previous);
        enteredRegions.sort(String::compareTo);

        ArrayList<String> enteredSubRegions = new ArrayList<>(currentSubRegions);
        enteredSubRegions.removeAll(previousSubRegions);
        enteredSubRegions.sort(String::compareTo);

        for (String id : exitedSubRegions) {
            CustomSubRegion subRegion = getSubRegion(data, id);
            if (subRegion != null && subRegion.hasLeaveText()) {
                send(player, NoticeKind.LEAVE, subRegion.leaveText(), subRegion.leaveColor());
            }
        }
        for (String id : exitedRegions) {
            CustomRegion region = data.getRegion(id);
            if (region != null && region.hasLeaveText()) {
                send(player, NoticeKind.LEAVE, region.leaveText(), region.leaveColor());
            }
        }
        for (String id : enteredRegions) {
            CustomRegion region = data.getRegion(id);
            if (region != null && region.hasEnterText()) {
                send(player, NoticeKind.ENTER, region.enterText(), region.enterColor());
            }
        }
        for (String id : enteredSubRegions) {
            CustomSubRegion subRegion = getSubRegion(data, id);
            if (subRegion != null && subRegion.hasEnterText()) {
                send(player, NoticeKind.ENTER, subRegion.enterText(), subRegion.enterColor());
            }
        }
    }

    private static CustomSubRegion getSubRegion(CustomRegionSavedData data, String qualifiedId) {
        int separator = qualifiedId.indexOf('/');
        if (separator <= 0 || separator >= qualifiedId.length() - 1) {
            return null;
        }
        return data.getSubRegion(qualifiedId.substring(0, separator), qualifiedId.substring(separator + 1));
    }

    private static void send(ServerPlayer player, NoticeKind kind, String text, int color) {
        try {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new CustomRegionNoticePacket(kind, text, color, ServerNoticeConfig.noticeScale()));
        } catch (LinkageError | RuntimeException ignored) {
            // Keep the region state machine alive even if a client has a mismatched addon jar.
        }
    }

    private static void sendSnapshot(ServerPlayer player, CustomRegionSavedData data) {
        try {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new CustomRegionSyncPacket(data.sortedRegions()));
        } catch (LinkageError | RuntimeException ignored) {
            // Snapshot sync is optional for map overlays and must not block enter/leave notices.
        }
    }

    private record PlayerRegionState(
            ResourceLocation dimension,
            int blockX,
            int blockY,
            int blockZ,
            Set<String> activeRegionIds,
            Set<String> activeSubRegionIds,
            long dataRevision,
            long mapRevision) {
        private PlayerRegionState {
            activeRegionIds = Set.copyOf(activeRegionIds);
            activeSubRegionIds = Set.copyOf(activeSubRegionIds);
        }
    }
}
