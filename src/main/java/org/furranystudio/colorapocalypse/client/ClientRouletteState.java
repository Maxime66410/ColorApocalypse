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

        // SpinTiming is the same schedule the server uses for the spin-tick sound - sharing
        // it is what keeps color changes and sound ticks from drifting apart. Once the next
        // scheduled change would overshoot durationTicks, that change IS the real result -
        // shown right when the natural slowdown gets there, no separate cutoff needed.
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
