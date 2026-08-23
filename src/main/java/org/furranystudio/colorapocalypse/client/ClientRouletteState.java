package org.furranystudio.colorapocalypse.client;

import net.minecraft.world.item.DyeColor;
import net.minecraftforge.event.TickEvent;

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
        currentColor = randomColor();
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

        if (ticksElapsed >= durationTicks) {
            // landed - just hold the final color until HOLD_TICKS runs out, no more changes
            currentColor = targetColor;
            return;
        }

        // same growing-interval pacing as the server's spin-tick sound, so they line up
        if (ticksElapsed >= nextChangeAt) {
            float progress = (float) ticksElapsed / durationTicks;
            nextChangeAt = ticksElapsed + 2 + Math.round(progress * 13);
            currentColor = randomColor();
        }
    }

    private static DyeColor randomColor() {
        DyeColor[] colors = DyeColor.values();
        return colors[RANDOM.nextInt(colors.length)];
    }
}
