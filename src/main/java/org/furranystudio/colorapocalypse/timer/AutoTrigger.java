package org.furranystudio.colorapocalypse.timer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraftforge.event.TickEvent;
import org.furranystudio.colorapocalypse.Config;

/** Kicks off {@link RouletteSequence} manually or on a difficulty-based timer. */
public final class AutoTrigger {

    private static final int TICKS_PER_MINUTE = 20 * 60;

    private static long nextDrawTick = -1;

    private AutoTrigger() {
    }

    public static void register() {
        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> tick(event.server()));
    }

    /** Starts the roulette sequence right now. Returns false if it couldn't start (already running, empty pool). */
    public static boolean trigger(MinecraftServer server) {
        boolean started = RouletteSequence.start(server);
        if (started) {
            scheduleNext(server);
        }
        return started;
    }

    /** Ticks remaining until the next automatic draw, or -1 if the timer is off right now. */
    public static long getTicksRemaining(MinecraftServer server) {
        int intervalMinutes = intervalMinutesFor(server.overworld().getDifficulty());
        if (!Config.AUTO_TIMER_ENABLED.get() || intervalMinutes <= 0 || nextDrawTick < 0) {
            return -1;
        }
        return Math.max(0, nextDrawTick - server.getTickCount());
    }

    private static void tick(MinecraftServer server) {
        int intervalMinutes = intervalMinutesFor(server.overworld().getDifficulty());
        if (!Config.AUTO_TIMER_ENABLED.get() || intervalMinutes <= 0) {
            nextDrawTick = -1;
            return;
        }

        if (nextDrawTick < 0) {
            scheduleNext(server);
            return;
        }

        if (server.getTickCount() >= nextDrawTick) {
            trigger(server);
        }
    }

    private static void scheduleNext(MinecraftServer server) {
        int intervalMinutes = intervalMinutesFor(server.overworld().getDifficulty());
        nextDrawTick = intervalMinutes <= 0 ? -1 : server.getTickCount() + (long) intervalMinutes * TICKS_PER_MINUTE;
    }

    private static int intervalMinutesFor(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> 0;
            case EASY -> Config.EASY_INTERVAL_MINUTES.get();
            case NORMAL -> Config.NORMAL_INTERVAL_MINUTES.get();
            case HARD -> Config.HARD_INTERVAL_MINUTES.get();
        };
    }
}
