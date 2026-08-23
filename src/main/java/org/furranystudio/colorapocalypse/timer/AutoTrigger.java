package org.furranystudio.colorapocalypse.timer;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.event.TickEvent;
import org.furranystudio.colorapocalypse.Config;
import org.furranystudio.colorapocalypse.color.ColorPoolData;
import org.furranystudio.colorapocalypse.color.DestructionQueue;

import java.util.concurrent.ThreadLocalRandom;

/** Draws a color (manually or on a difficulty-based timer) and kicks off its destruction. */
public final class AutoTrigger {

    private static final int TICKS_PER_MINUTE = 20 * 60;

    private static long nextDrawTick = -1;

    private AutoTrigger() {
    }

    public static void register() {
        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> tick(event.server()));
    }

    /** Draws a color right now and broadcasts the result. Returns null if the pool is empty. */
    public static DyeColor draw(MinecraftServer server) {
        ColorPoolData pool = server.overworld().getDataStorage().computeIfAbsent(ColorPoolData.TYPE);
        DyeColor color = pool.draw(ThreadLocalRandom.current());

        if (color == null) {
            broadcast(server, "[ColorApocalypse] No colors left in the pool. Use /colorapocalypse reset to refill it.");
            return null;
        }

        int chunkCount = DestructionQueue.start(color, server);
        broadcast(server, "[ColorApocalypse] " + color.getName() + " eliminated! Destroying its blocks across "
            + chunkCount + " chunk(s)...");

        scheduleNext(server);
        return color;
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
            draw(server);
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

    private static void broadcast(MinecraftServer server, String message) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }
}
