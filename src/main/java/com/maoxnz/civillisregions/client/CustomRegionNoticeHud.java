package com.maoxnz.civillisregions.client;

import com.maoxnz.civillisregions.NoticeKind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.ArrayDeque;

public final class CustomRegionNoticeHud {
    private static final int FADE_IN_TICKS = 16;
    private static final int HOLD_TICKS = 28;
    private static final int FADE_OUT_TICKS = 16;
    private static final int ENTER_RGB = 0xEAF4FF;
    private static final int LEAVE_RGB = 0x8ECFFF;
    private static final int BAR_WIDTH = 2;
    private static final int BAR_HEIGHT = 12;
    private static final int BAR_GAP = 8;
    private static final int BAR_TRAVEL = 16;
    private static final float BAR_CENTER_Y_BIAS_RATIO = -0.15f;
    private static final int DEFAULT_OFFSET_X_PERCENT = 0;
    private static final int DEFAULT_OFFSET_Y_PERCENT = 30;

    private static final ArrayDeque<Notice> QUEUE = new ArrayDeque<>();
    private static Component currentText = Component.empty();
    private static NoticeKind currentKind = NoticeKind.ENTER;
    private static int currentColor = -1;
    private static int ticksRemaining;

    private static Field civillisTicksRemainingField;
    private static boolean triedCivillisTicksField;
    private static Field offsetXField;
    private static Field offsetYField;
    private static boolean triedConfigFields;

    private CustomRegionNoticeHud() {}

    public static void enqueue(NoticeKind kind, String text) {
        enqueue(kind, text, -1);
    }

    public static void enqueue(NoticeKind kind, String text, int color) {
        if (text == null || text.isBlank()) {
            return;
        }
        QUEUE.addLast(new Notice(kind, Component.literal(text), color));
    }

    public static void tick() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
        }
        if (ticksRemaining <= 0 && !QUEUE.isEmpty() && !isCivillisZoneHudVisible()) {
            Notice next = QUEUE.removeFirst();
            currentKind = next.kind();
            currentText = next.text();
            currentColor = next.color();
            ticksRemaining = FADE_IN_TICKS + HOLD_TICKS + FADE_OUT_TICKS;
        }
    }

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        if (ticksRemaining <= 0 || currentText.getString().isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.font == null) {
            return;
        }

        int total = FADE_IN_TICKS + HOLD_TICKS + FADE_OUT_TICKS;
        float clampedPartial = clamp01(partialTick);
        float elapsed = (total - ticksRemaining) + clampedPartial;
        float inPhase = clamp01(elapsed / FADE_IN_TICKS);
        float outPhase = clamp01((elapsed - FADE_IN_TICKS - HOLD_TICKS) / FADE_OUT_TICKS);
        float alpha;
        if (elapsed < FADE_IN_TICKS) {
            alpha = easeOutCubic(inPhase);
        } else if (elapsed < (FADE_IN_TICKS + HOLD_TICKS)) {
            alpha = 1.0f;
        } else {
            alpha = 1.0f - easeInCubic(outPhase);
        }
        if (alpha <= 0.01f) {
            return;
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        int ox = clampInt(readOffsetXPercent(), -50, 50);
        int oy = clampInt(readOffsetYPercent(), -50, 50);
        int anchorX = Math.round(width * (0.5f + ox / 100.0f));
        int anchorY = Math.round(height * (0.5f - oy / 100.0f));

        int textWidth = mc.font.width(currentText);
        int textHeight = mc.font.lineHeight;
        int barCenterBias = Math.round(textHeight * BAR_CENTER_Y_BIAS_RATIO);
        barCenterBias = clampInt(barCenterBias, -4, -1);

        int x = anchorX - textWidth / 2;
        int y = alignBaselineToHudCenterY(anchorY, textHeight, barCenterBias);

        int rgb = currentColor >= 0 ? currentColor : (currentKind == NoticeKind.ENTER ? ENTER_RGB : LEAVE_RGB);
        int color = ((int) (alpha * 255.0f) << 24) | rgb;
        guiGraphics.drawString(mc.font, currentText, x, y, color, true);

        float inEased = easeOutCubic(inPhase);
        float outEased = easeInCubic(outPhase);
        float leftOffset;
        float rightOffset;
        if (elapsed < FADE_IN_TICKS) {
            leftOffset = -BAR_TRAVEL * (1.0f - inEased);
            rightOffset = BAR_TRAVEL * (1.0f - inEased);
        } else if (elapsed < (FADE_IN_TICKS + HOLD_TICKS)) {
            leftOffset = 0.0f;
            rightOffset = 0.0f;
        } else {
            leftOffset = BAR_TRAVEL * outEased;
            rightOffset = -BAR_TRAVEL * outEased;
        }

        int barColor = ((int) (alpha * 220.0f) << 24) | rgb;
        int barShadowRgb = currentKind == NoticeKind.ENTER ? 0x0C1526 : 0x0D1A2A;
        int barShadowColor = ((int) (alpha * 120.0f) << 24) | barShadowRgb;
        int baseBarY = y + (textHeight - BAR_HEIGHT) / 2 + barCenterBias;
        int leftX = x - BAR_GAP - BAR_WIDTH;
        int rightX = x + textWidth + BAR_GAP;
        int leftY1 = Math.round(baseBarY + leftOffset);
        int rightY1 = Math.round(baseBarY + rightOffset);
        guiGraphics.fill(leftX + 1, leftY1 + 1, leftX + BAR_WIDTH + 1, leftY1 + BAR_HEIGHT + 1, barShadowColor);
        guiGraphics.fill(rightX + 1, rightY1 + 1, rightX + BAR_WIDTH + 1, rightY1 + BAR_HEIGHT + 1, barShadowColor);
        guiGraphics.fill(leftX, leftY1, leftX + BAR_WIDTH, leftY1 + BAR_HEIGHT, barColor);
        guiGraphics.fill(rightX, rightY1, rightX + BAR_WIDTH, rightY1 + BAR_HEIGHT, barColor);
    }

    private static boolean isCivillisZoneHudVisible() {
        Field field = civillisTicksRemainingField();
        if (field == null) {
            return false;
        }
        try {
            return field.getInt(null) > 0;
        } catch (IllegalAccessException ignored) {
            return false;
        }
    }

    private static Field civillisTicksRemainingField() {
        if (triedCivillisTicksField) {
            return civillisTicksRemainingField;
        }
        triedCivillisTicksField = true;
        try {
            Class<?> hudClass = Class.forName("civil.civilization.ZoneTransitionHud");
            Field field = hudClass.getDeclaredField("ticksRemaining");
            field.setAccessible(true);
            civillisTicksRemainingField = field;
        } catch (ReflectiveOperationException ignored) {
            civillisTicksRemainingField = null;
        }
        return civillisTicksRemainingField;
    }

    private static int readOffsetXPercent() {
        Field field = offsetField(true);
        return readIntField(field, DEFAULT_OFFSET_X_PERCENT);
    }

    private static int readOffsetYPercent() {
        Field field = offsetField(false);
        return readIntField(field, DEFAULT_OFFSET_Y_PERCENT);
    }

    private static Field offsetField(boolean x) {
        if (!triedConfigFields) {
            triedConfigFields = true;
            try {
                Class<?> configClass = Class.forName("civil.config.CivilConfig");
                offsetXField = configClass.getDeclaredField("zoneTransitionHudAnchorOffsetXPercent");
                offsetYField = configClass.getDeclaredField("zoneTransitionHudAnchorOffsetYPercent");
                offsetXField.setAccessible(true);
                offsetYField.setAccessible(true);
            } catch (ReflectiveOperationException ignored) {
                offsetXField = null;
                offsetYField = null;
            }
        }
        return x ? offsetXField : offsetYField;
    }

    private static int readIntField(Field field, int fallback) {
        if (field == null) {
            return fallback;
        }
        try {
            return field.getInt(null);
        } catch (IllegalAccessException ignored) {
            return fallback;
        }
    }

    private static float clamp01(float value) {
        if (value < 0.0f) {
            return 0.0f;
        }
        return Math.min(value, 1.0f);
    }

    private static float easeOutCubic(float t) {
        float x = clamp01(t);
        float oneMinus = 1.0f - x;
        return 1.0f - oneMinus * oneMinus * oneMinus;
    }

    private static float easeInCubic(float t) {
        float x = clamp01(t);
        return x * x * x;
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int hudClusterCenterY(int baselineY, int textHeight, int barCenterBias) {
        int k = (textHeight - BAR_HEIGHT) / 2 + barCenterBias;
        int baseBarY = baselineY + k;
        int textTop = baselineY - textHeight;
        int blockTop = Math.min(textTop, baseBarY);
        int blockBottom = Math.max(baselineY, baseBarY + BAR_HEIGHT);
        return Math.round((blockTop + blockBottom) / 2.0f);
    }

    private static int alignBaselineToHudCenterY(int targetCenterY, int textHeight, int barCenterBias) {
        int baseline = targetCenterY + textHeight / 2;
        int actual = hudClusterCenterY(baseline, textHeight, barCenterBias);
        return baseline + (targetCenterY - actual);
    }

    private record Notice(NoticeKind kind, Component text, int color) {}
}
