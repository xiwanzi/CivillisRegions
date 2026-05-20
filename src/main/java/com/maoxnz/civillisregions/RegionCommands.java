package com.maoxnz.civillisregions;

import com.maoxnz.civillisregions.config.ServerNoticeConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class RegionCommands {
    private RegionCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("civil")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(createCommand())
                .then(resizeCommand())
                .then(editCommand())
                .then(overlayCommand())
                .then(subCommand())
                .then(hudScaleCommand())
                .then(listCommand())
                .then(deleteCommand()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return literal("create")
                .then(argument("id", StringArgumentType.word())
                        .then(blockBoxArguments((ctx, pos1, pos2) -> create(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "id"),
                                pos1,
                                pos2))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> resizeCommand() {
        return literal("resize")
                .then(regionIdArgument("id")
                        .then(blockBoxArguments((ctx, pos1, pos2) -> resize(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "id"),
                                pos1,
                                pos2))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> editCommand() {
        return literal("edit")
                .then(regionIdArgument("id")
                        .then(noticeArguments((ctx, enterText, leaveText, enterColor, leaveColor) -> edit(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "id"),
                                enterText,
                                leaveText,
                                enterColor,
                                leaveColor))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> overlayCommand() {
        return literal("overlay")
                .then(regionIdArgument("id")
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
                                                StringArgumentType.getString(ctx, "color"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> subCommand() {
        return literal("sub")
                .then(subCreateCommand())
                .then(subEditCommand())
                .then(subResizeCommand())
                .then(subListCommand())
                .then(subDeleteCommand());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> subCreateCommand() {
        return literal("create")
                .then(regionIdArgument("parentId")
                        .then(argument("id", StringArgumentType.word())
                                .then(blockBoxArguments((ctx, pos1, pos2) -> subCreate(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "parentId"),
                                        StringArgumentType.getString(ctx, "id"),
                                        pos1,
                                        pos2)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> subEditCommand() {
        return literal("edit")
                .then(regionIdArgument("parentId")
                        .then(subRegionIdArgument("id")
                                .then(noticeArguments((ctx, enterText, leaveText, enterColor, leaveColor) -> subEdit(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "parentId"),
                                        StringArgumentType.getString(ctx, "id"),
                                        enterText,
                                        leaveText,
                                        enterColor,
                                        leaveColor)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> subResizeCommand() {
        return literal("resize")
                .then(regionIdArgument("parentId")
                        .then(subRegionIdArgument("id")
                                .then(blockBoxArguments((ctx, pos1, pos2) -> subResize(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "parentId"),
                                        StringArgumentType.getString(ctx, "id"),
                                        pos1,
                                        pos2)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> subListCommand() {
        return literal("list")
                .executes(ctx -> subList(ctx.getSource(), null))
                .then(regionIdArgument("parentId")
                        .executes(ctx -> subList(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "parentId"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> subDeleteCommand() {
        return literal("delete")
                .then(regionIdArgument("parentId")
                        .then(subRegionIdArgument("id")
                                .executes(ctx -> subDelete(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "parentId"),
                                        StringArgumentType.getString(ctx, "id")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> hudScaleCommand() {
        return literal("hudscale")
                .executes(ctx -> hudScale(ctx.getSource()))
                .then(argument("scale", DoubleArgumentType.doubleArg(
                                ServerNoticeConfig.minNoticeScale(),
                                ServerNoticeConfig.maxNoticeScale()))
                        .executes(ctx -> hudScale(
                                ctx.getSource(),
                                DoubleArgumentType.getDouble(ctx, "scale"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> listCommand() {
        return literal("list").executes(ctx -> list(ctx.getSource()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> deleteCommand() {
        return literal("delete")
                .then(regionIdArgument("id")
                        .executes(ctx -> delete(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "id"))));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ?> blockBoxArguments(BlockBoxExecutor executor) {
        return argument("pos1", BlockPosArgument.blockPos())
                .then(argument("pos2", BlockPosArgument.blockPos())
                        .executes(ctx -> executor.run(
                                ctx,
                                BlockPosArgument.getBlockPos(ctx, "pos1"),
                                BlockPosArgument.getBlockPos(ctx, "pos2"))));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> noticeArguments(NoticeExecutor executor) {
        return argument("enterText", StringArgumentType.string())
                .then(argument("leaveText", StringArgumentType.string())
                        .executes(ctx -> executor.run(
                                ctx,
                                StringArgumentType.getString(ctx, "enterText"),
                                StringArgumentType.getString(ctx, "leaveText"),
                                null,
                                null))
                        .then(argument("enterColor", StringArgumentType.word())
                                .executes(ctx -> executor.run(
                                        ctx,
                                        StringArgumentType.getString(ctx, "enterText"),
                                        StringArgumentType.getString(ctx, "leaveText"),
                                        StringArgumentType.getString(ctx, "enterColor"),
                                        StringArgumentType.getString(ctx, "enterColor")))
                                .then(argument("leaveColor", StringArgumentType.word())
                                        .executes(ctx -> executor.run(
                                                ctx,
                                                StringArgumentType.getString(ctx, "enterText"),
                                                StringArgumentType.getString(ctx, "leaveText"),
                                                StringArgumentType.getString(ctx, "enterColor"),
                                                StringArgumentType.getString(ctx, "leaveColor"))))));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> regionIdArgument(String name) {
        return argument(name, StringArgumentType.word())
                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                        CustomRegionSavedData.get(ctx.getSource().getServer()).sortedIds(),
                        builder));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> subRegionIdArgument(String name) {
        return argument(name, StringArgumentType.word())
                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                        CustomRegionSavedData.get(ctx.getSource().getServer())
                                .sortedSubRegionIds(StringArgumentType.getString(ctx, "parentId")),
                        builder));
    }

    @FunctionalInterface
    private interface BlockBoxExecutor {
        int run(CommandContext<CommandSourceStack> context, BlockPos pos1, BlockPos pos2);
    }

    @FunctionalInterface
    private interface NoticeExecutor {
        int run(
                CommandContext<CommandSourceStack> context,
                String enterText,
                String leaveText,
                String enterColorText,
                String leaveColorText);
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

        CustomRegion region = new CustomRegion(
                id,
                source.getLevel().dimension().location(),
                chunkFromBlock(pos1.getX()),
                chunkFromBlock(pos2.getX()),
                chunkFromBlock(pos1.getZ()),
                chunkFromBlock(pos2.getZ()),
                "",
                "");
        data.putRegion(region);
        source.sendSuccess(
                () -> Component.literal("[Civil] Created custom region '" + id + "' in " + region.dimension()
                        + " chunks x=" + region.minChunkX() + ".." + region.maxChunkX()
                        + ", z=" + region.minChunkZ() + ".." + region.maxChunkZ()
                        + ". Use /civil edit to enable notices."),
                true);
        return 1;
    }

    private static int resize(CommandSourceStack source, String id, BlockPos pos1, BlockPos pos2) {
        CustomRegionSavedData data = CustomRegionSavedData.get(source.getServer());
        CustomRegion existing = data.getRegion(id);
        if (existing == null) {
            source.sendFailure(Component.literal("[Civil] Custom region '" + id + "' does not exist."));
            return 0;
        }
        if (!ensureCurrentDimension(source, existing, "Custom region '" + id + "'")) {
            return 0;
        }

        CustomRegion resized = existing.withBounds(
                chunkFromBlock(pos1.getX()),
                chunkFromBlock(pos2.getX()),
                chunkFromBlock(pos1.getZ()),
                chunkFromBlock(pos2.getZ()));
        List<CustomSubRegion> outside = data.subRegionsOutsideParent(resized);
        if (!outside.isEmpty()) {
            source.sendFailure(Component.literal("[Civil] Resize would move sub-region '"
                    + outside.get(0).qualifiedId() + "' outside its parent. Resize rejected."));
            return 0;
        }

        data.putRegion(resized);
        source.sendSuccess(
                () -> Component.literal("[Civil] Resized custom region '" + id + "' to chunks x="
                        + resized.minChunkX() + ".." + resized.maxChunkX()
                        + ", z=" + resized.minChunkZ() + ".." + resized.maxChunkZ()
                        + ". Notices and overlay settings were kept."),
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

        data.putRegion(existing.withMessages(enterText, leaveText, enterColor, leaveColor), false);
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

        CustomRegion updated = existing.withOverlay(displayName, overlayColor);
        data.putRegion(updated);
        source.sendSuccess(
                () -> Component.literal("[Civil] Updated custom region '" + id + "' overlay label '"
                        + updated.displayName()
                        + "' color " + RegionColors.formatHexRgb(overlayColor) + "."),
                true);
        return 1;
    }

    private static int subCreate(CommandSourceStack source, String parentId, String id, BlockPos pos1, BlockPos pos2) {
        if (!RegionId.isValid(id)) {
            source.sendFailure(Component.literal("[Civil] Sub-region id must match [a-z0-9_-]+."));
            return 0;
        }

        CustomRegionSavedData data = CustomRegionSavedData.get(source.getServer());
        CustomRegion parent = data.getRegion(parentId);
        if (parent == null) {
            source.sendFailure(Component.literal("[Civil] Parent region '" + parentId + "' does not exist."));
            return 0;
        }
        if (!ensureCurrentDimension(source, parent, "Parent region '" + parentId + "'")) {
            return 0;
        }
        if (data.containsSubRegion(parentId, id)) {
            source.sendFailure(Component.literal("[Civil] Sub-region '" + parentId + "/" + id + "' already exists."));
            return 0;
        }

        CustomSubRegion subRegion = newSubRegion(parentId, id, pos1, pos2, "", "");
        if (!subRegion.isInsideParent(parent)) {
            source.sendFailure(Component.literal("[Civil] Sub-region '" + parentId + "/" + id
                    + "' must be fully inside the parent region's X/Z chunk bounds."));
            return 0;
        }

        data.putSubRegion(subRegion);
        source.sendSuccess(
                () -> Component.literal("[Civil] Created sub-region '" + subRegion.qualifiedId() + "' blocks x="
                        + subRegion.minBlockX() + ".." + subRegion.maxBlockX()
                        + ", y=" + subRegion.minBlockY() + ".." + subRegion.maxBlockY()
                        + ", z=" + subRegion.minBlockZ() + ".." + subRegion.maxBlockZ()
                        + ". Use /civil sub edit to enable notices."),
                true);
        return 1;
    }

    private static int subEdit(
            CommandSourceStack source,
            String parentId,
            String id,
            String enterText,
            String leaveText,
            String enterColorText,
            String leaveColorText) {
        CustomRegionSavedData data = CustomRegionSavedData.get(source.getServer());
        CustomSubRegion existing = data.getSubRegion(parentId, id);
        if (existing == null) {
            source.sendFailure(Component.literal("[Civil] Sub-region '" + parentId + "/" + id + "' does not exist."));
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

        data.putSubRegion(existing.withMessages(enterText, leaveText, enterColor, leaveColor));
        source.sendSuccess(
                () -> Component.literal("[Civil] Updated sub-region '" + parentId + "/" + id + "' notices."),
                true);
        return 1;
    }

    private static int subResize(CommandSourceStack source, String parentId, String id, BlockPos pos1, BlockPos pos2) {
        CustomRegionSavedData data = CustomRegionSavedData.get(source.getServer());
        CustomRegion parent = data.getRegion(parentId);
        CustomSubRegion existing = data.getSubRegion(parentId, id);
        if (parent == null || existing == null) {
            source.sendFailure(Component.literal("[Civil] Sub-region '" + parentId + "/" + id + "' does not exist."));
            return 0;
        }
        if (!ensureCurrentDimension(source, parent, "Parent region '" + parentId + "'")) {
            return 0;
        }

        CustomSubRegion resized = existing.withBounds(
                Math.min(pos1.getX(), pos2.getX()),
                Math.max(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()),
                Math.max(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ()),
                Math.max(pos1.getZ(), pos2.getZ()));
        if (!resized.isInsideParent(parent)) {
            source.sendFailure(Component.literal("[Civil] Sub-region '" + resized.qualifiedId()
                    + "' must be fully inside the parent region's X/Z chunk bounds."));
            return 0;
        }

        data.putSubRegion(resized);
        source.sendSuccess(
                () -> Component.literal("[Civil] Resized sub-region '" + resized.qualifiedId() + "' to blocks x="
                        + resized.minBlockX() + ".." + resized.maxBlockX()
                        + ", y=" + resized.minBlockY() + ".." + resized.maxBlockY()
                        + ", z=" + resized.minBlockZ() + ".." + resized.maxBlockZ()
                        + ". Notices were kept."),
                true);
        return 1;
    }

    private static int subList(CommandSourceStack source, String parentId) {
        CustomRegionSavedData data = CustomRegionSavedData.get(source.getServer());
        List<CustomSubRegion> regions = parentId == null ? data.sortedSubRegions() : data.sortedSubRegions(parentId);
        if (parentId != null && data.getRegion(parentId) == null) {
            source.sendFailure(Component.literal("[Civil] Parent region '" + parentId + "' does not exist."));
            return 0;
        }
        if (regions.isEmpty()) {
            source.sendSuccess(() -> Component.literal(parentId == null
                    ? "[Civil] No sub-regions."
                    : "[Civil] No sub-regions under '" + parentId + "'."), false);
            return 1;
        }

        source.sendSuccess(
                () -> Component.literal("[Civil] Sub-regions (" + regions.size() + "):"),
                false);
        for (CustomSubRegion region : regions) {
            String status = region.hasEnterText() || region.hasLeaveText() ? "notices set" : "no notices";
            source.sendSuccess(
                    () -> Component.literal(" - " + region.qualifiedId()
                            + " x=" + region.minBlockX() + ".." + region.maxBlockX()
                            + ", y=" + region.minBlockY() + ".." + region.maxBlockY()
                            + ", z=" + region.minBlockZ() + ".." + region.maxBlockZ()
                            + ", enter=" + RegionColors.formatHexRgb(region.enterColor())
                            + ", leave=" + RegionColors.formatHexRgb(region.leaveColor())
                            + " (" + status + ")"),
                    false);
        }
        return regions.size();
    }

    private static int subDelete(CommandSourceStack source, String parentId, String id) {
        CustomRegionSavedData data = CustomRegionSavedData.get(source.getServer());
        if (!data.removeSubRegion(parentId, id)) {
            source.sendFailure(Component.literal("[Civil] Sub-region '" + parentId + "/" + id + "' does not exist."));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("[Civil] Deleted sub-region '" + parentId + "/" + id + "'."),
                true);
        return 1;
    }

    private static int hudScale(CommandSourceStack source) {
        source.sendSuccess(
                () -> Component.literal("[Civil] HUD notice scale is "
                        + formatScale(ServerNoticeConfig.noticeScale())
                        + ". Old default size was 1.00. Use /civil hudscale <0.5..3.0> to change it."),
                false);
        return 1;
    }

    private static int hudScale(CommandSourceStack source, double scale) {
        if (!ServerNoticeConfig.setNoticeScale(scale)) {
            source.sendFailure(Component.literal("[Civil] HUD notice scale must be between "
                    + formatScale(ServerNoticeConfig.minNoticeScale())
                    + " and " + formatScale(ServerNoticeConfig.maxNoticeScale()) + "."));
            return 0;
        }
        source.sendSuccess(
                () -> Component.literal("[Civil] HUD notice scale set to " + formatScale(scale)
                        + ". This server value is sent with future region notices."),
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
                            + ", subRegions=" + data.sortedSubRegions(region.id()).size()
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
        int subRegionCount = data.sortedSubRegions(id).size();
        if (!data.removeRegion(id)) {
            source.sendFailure(Component.literal("[Civil] Custom region '" + id + "' does not exist."));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("[Civil] Deleted custom region '" + id + "' and "
                        + subRegionCount + " sub-region(s)."),
                true);
        return 1;
    }

    private static boolean ensureCurrentDimension(CommandSourceStack source, CustomRegion region, String label) {
        ResourceLocation currentDimension = source.getLevel().dimension().location();
        if (region.dimension().equals(currentDimension)) {
            return true;
        }
        source.sendFailure(Component.literal("[Civil] " + label + " is in " + region.dimension()
                + ", but this command is running in " + currentDimension + "."));
        return false;
    }

    private static CustomSubRegion newSubRegion(
            String parentId,
            String id,
            BlockPos pos1,
            BlockPos pos2,
            String enterText,
            String leaveText) {
        return new CustomSubRegion(
                parentId,
                id,
                pos1.getX(),
                pos2.getX(),
                pos1.getY(),
                pos2.getY(),
                pos1.getZ(),
                pos2.getZ(),
                enterText,
                leaveText);
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

    private static String formatScale(double scale) {
        return String.format(Locale.ROOT, "%.2f", scale);
    }
}
