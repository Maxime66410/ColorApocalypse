/**
 * File: ClientRouletteState.java
 * Author: Maxime66410
 * Created: 2026-08-23
 * Last Modified: 2026-08-24
 */
package org.furranystudio.colorapocalypse.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.event.TickEvent;
import org.furranystudio.colorapocalypse.timer.SpinTiming;

import java.util.Random;

/** Client-side spin animation state, driven by {@code RouletteSpinPacket}. Only ever registered client-side. */
public final class ClientRouletteState {

    private static final Random RANDOM = new Random();
    // how long to keep showing the landed color before handing back to the vanilla XP bar
    private static final int HOLD_TICKS = 60;

    private static DyeColor targetColor;
    private static int durationTicks;
    private static int ticksElapsed;
    private static int nextChangeAt;
    private static boolean landed;
    private static DyeColor currentColor = DyeColor.WHITE;

    private ClientRouletteState() {
    }

    public static void register() {
        TickEvent.ClientTickEvent.Post.BUS.addListener(event -> tick());
    }

    public static void start(DyeColor color, int duration) {
        targetColor = color;
        durationTicks = Math.max(duration, 1);
        ticksElapsed = 0;
        nextChangeAt = 0;
        landed = false;
        setCurrentColor(randomColor());
    }

    public static boolean isSpinning() {
        return targetColor != null && ticksElapsed < durationTicks + HOLD_TICKS;
    }

    public static DyeColor getCurrentColor() {
        return currentColor;
    }

    private static void tick() {
        if (!isSpinning()) {
            return;
        }
        ticksElapsed++;

        if (landed || ticksElapsed < nextChangeAt) {
            return;
        }

        // If we have reached the final change, set the current color to the target color and mark as landed. Otherwise, set the next change time and pick a random color.
        if (SpinTiming.isFinalChange(ticksElapsed, durationTicks)) {
            setCurrentColor(targetColor);
            landed = true;
        } else {
            nextChangeAt = SpinTiming.nextChangeAt(ticksElapsed, durationTicks);
            setCurrentColor(randomColor());
        }
    }

    private static DyeColor randomColor() {
        DyeColor[] colors = DyeColor.values();
        return colors[RANDOM.nextInt(colors.length)];
    }

    private static void setCurrentColor(DyeColor color) {
        currentColor = color;
        Minecraft.getInstance().gui.hud.setTitle(Component.translatable("color.minecraft." + color.getName())
            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color.getTextColor()))));
    }
}
