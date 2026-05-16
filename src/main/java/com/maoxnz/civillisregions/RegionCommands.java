package com.maoxnz.civillisregions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class RegionCommands {
    private RegionCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("civil")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(literal("create")
                        .then(argument("id", StringArgumentType.word())
                                .then(argument("pos1", BlockPosArgument.blockPos())
                                        .then(argument("pos2", BlockPosArgument.blockPos())
                                                .executes(ctx -> create(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "id"),
                                                        BlockPosArgument.getBlockPos(ctx, "pos1"),
                                                        BlockPosArgument.getBlockPos(ctx, "pos2")))))))
                .then(literal("edit")
                        .then(argument("id", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        CustomRegionSavedData.get(ctx.getSource().getServer()).sortedIds(),
                                        builder))
                                .then(argument("enterText", StringArgumentType.string())
                                        .then(argument("leaveText", StringArgumentType.string())
                                                .executes(ctx -> edit(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "id"),
                                                        StringArgumentType.getString(ctx, "enterText"),
                                                        StringArgumentType.getString(ctx, "leaveText"),
                                                        null,
                                                        null))
                                                .then(argument("enterColor", StringArgumentType.word())
                                                        .executes(ctx -> edit(
                                                                ctx.getSource(),
                                                                StringArgumentType.getString(ctx, "id"),
                                                                StringArgumentType.getString(ctx, "enterText"),
                                                                StringArgumentType.getString(ctx, "leaveText"),
                                                                StringArgumentType.getString(ctx, "enterColor"),
                                                                StringArgumentType.getString(ctx, "enterColor")))
                                                        .then(argument("leaveColor", StringArgumentType.word())
                                                                .executes(ctx -> edit(
                                                                        ctx.getSource(),
                                                                        StringArgumentType.getString(ctx, "id"),
                                                                        StringArgumentType.getString(ctx, "enterText"),
                                                                        StringArgumentType.getString(ctx, "leaveText"),
                                                                        StringArgumentType.getString(ctx, "enterColor"),
                                                                        StringArgumentType.getString(ctx, "leaveColor")))))))))
                .then(literal("overlay")
                        .then(argument("id", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        CustomRegionSavedData.get(ctx.getSource().getServer()).sortedIds(),
                                        builder))
                                .then(argument("displayName", StringArgumentType.string())
                                        .executes(ctx -> overlay(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "id"),
                                                StringArgumentType.getString(ctx, "displayName"),
                                                null))
                                        .then(argument("color", StringArgumentType.word())
                                                .executes(ctx -> overlay(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "id"),
                                                        StringArgumentType.getString(ctx, "displayName"),
                                                        StringArgumentType.getString(ctx, "color")))))))
                .then(literal("list")
                        .executes(ctx -> list(ctx.getSource())))
                .then(literal("delete")
                        .then(argument("id", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        CustomRegionSavedData.get(ctx.getSource().getServer()).sortedIds(),
                                        builder))
                                .executes(ctx -> delete(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "id"))))));
    }

    private static int create(CommandSourceStack source, String id, BlockPos pos1, BlockPos pos2) {
        if (!RegionId.isValid(id)) {
            source.sendFailure(Component.literal("[Civil] Region id must match [a-z0-9_-]+."));
            return 0;
        }

        CustomRegionSavedData data = CustomRegionSavedData.get(source.getServer());
        if (data.contains(id)) {
            source.sendFailure(Component.literal("[Civil] Custom region '" + id + "' already exists."));
            return 0;
        }

        ResourceLocation dimension = source.getLevel().dimension().location();
        int c1x = chunkFromBlock(pos1.getX());
        int c2x = chunkFromBlock(pos2.getX());
        int c1z = chunkFromBlock(pos1.getZ());
        int c2z = chunkFromBlock(pos2.getZ());
        CustomRegion region = new CustomRegion(
                id,
                dimension,
                Math.min(c1x, c2x),
                Math.max(c1x, c2x),
                Math.min(c1z, c2z),
                Math.max(c1z, c2z),
                "",
                "");
        data.putRegion(region);
        source.sendSuccess(
                () -> Component.literal("[Civil] Created custom region '" + id + "' in " + dimension
                        + " chunks x=" + region.minChunkX() + ".." + region.maxChunkX()
                        + ", z=" + region.minChunkZ() + ".." + region.maxChunkZ()
                        + ". Use /civil edit to enable notices."),
                true);
        return 1;
    }

    private static int edit(
            CommandSourceStack source,
            String id,
            String enterText,
            String leaveText,
            String enterColorText,
            String leaveColorText) {
        CustomRegionSavedData data = CustomRegionSavedData.get(source.getServer());
        CustomRegion existing = data.getRegion(id);
        if (existing == null) {
            source.sendFailure(Component.literal("[Civil] Custom region '" + id + "' does not exist."));
            return 0;
        }

        int enterColor = parseOptionalColor(source, enterColorText);
        if (enterColorText != null && enterColor == RegionColors.UNSET) {
            return 0;
        }
        int leaveColor = parseOptionalColor(source, leaveColorText);
        if (leaveColorText != null && leaveColor == RegionColors.UNSET) {
            return 0;
        }

        data.putRegion(existing.withMessages(enterText, leaveText, enterColor, leaveColor));
        source.sendSuccess(
                () -> Component.literal("[Civil] Updated custom region '" + id + "' notices."),
                true);
        return 1;
    }

    private static int overlay(CommandSourceStack source, String id, String displayName, String colorText) {
        CustomRegionSavedData data = CustomRegionSavedData.get(source.getServer());
        CustomRegion existing = data.getRegion(id);
        if (existing == null) {
            source.sendFailure(Component.literal("[Civil] Custom region '" + id + "' does not exist."));
            return 0;
        }

        int overlayColor = parseOptionalColor(source, colorText);
        if (colorText != null && overlayColor == RegionColors.UNSET) {
            return 0;
        }

        data.putRegion(existing.withOverlay(displayName, overlayColor));
        source.sendSuccess(
                () -> Component.literal("[Civil] Updated custom region '" + id + "' overlay label '"
                        + existing.withOverlay(displayName, overlayColor).displayName()
                        + "' color " + RegionColors.formatHexRgb(overlayColor) + "."),
                true);
        return 1;
    }

    private static int list(CommandSourceStack source) {
        CustomRegionSavedData data = CustomRegionSavedData.get(source.getServer());
        if (data.regions().isEmpty()) {
            source.sendSuccess(() -> Component.literal("[Civil] No custom regions."), false);
            return 1;
        }

        source.sendSuccess(
                () -> Component.literal("[Civil] Custom regions (" + data.regions().size() + "):"),
                false);
        for (CustomRegion region : data.sortedRegions()) {
            String status = region.hasEnterText() || region.hasLeaveText() ? "notices set" : "no notices";
            source.sendSuccess(
                    () -> Component.literal(" - " + region.id()
                            + " label=\"" + region.displayName() + "\""
                            + " [" + region.dimension() + "]"
                            + " x=" + region.minChunkX() + ".." + region.maxChunkX()
                            + ", z=" + region.minChunkZ() + ".." + region.maxChunkZ()
                            + ", overlay=" + RegionColors.formatHexRgb(region.overlayColor())
                            + ", enter=" + RegionColors.formatHexRgb(region.enterColor())
                            + ", leave=" + RegionColors.formatHexRgb(region.leaveColor())
                            + " (" + status + ")"),
                    false);
        }
        return data.regions().size();
    }

    private static int delete(CommandSourceStack source, String id) {
        CustomRegionSavedData data = CustomRegionSavedData.get(source.getServer());
        if (!data.removeRegion(id)) {
            source.sendFailure(Component.literal("[Civil] Custom region '" + id + "' does not exist."));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("[Civil] Deleted custom region '" + id + "'."),
                true);
        return 1;
    }

    private static int chunkFromBlock(int blockCoord) {
        return Math.floorDiv(blockCoord, 16);
    }

    private static int parseOptionalColor(CommandSourceStack source, String text) {
        if (text == null) {
            return RegionColors.UNSET;
        }
        String normalized = RegionColors.normalizeHexRgb(text);
        if (normalized == null) {
            source.sendFailure(Component.literal("[Civil] Color must use RRGGBB or #RRGGBB, for example d9e6ff."));
            return RegionColors.UNSET;
        }
        return RegionColors.parseHexRgb(normalized);
    }
}
