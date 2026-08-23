package org.furranystudio.colorapocalypse.timer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.event.TickEvent;
import org.furranystudio.colorapocalypse.Config;
import org.furranystudio.colorapocalypse.color.ColorPoolData;
import org.furranystudio.colorapocalypse.color.DestructionQueue;
import org.furranystudio.colorapocalypse.color.MobDestroyer;
import org.furranystudio.colorapocalypse.network.ModNetworking;
import org.furranystudio.colorapocalypse.network.RouletteSpinPacket;
import org.furranystudio.colorapocalypse.sound.ModSounds;
import org.furranystudio.colorapocalypse.sound.SoundBroadcaster;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The full roulette sequence: 10s title countdown ("it's coming") -> the roulette itself
 * appears and spins for 5s -> it stops and reveals the drawn color (in its own color),
 * which starts destruction.
 */
public final class RouletteSequence {

    private static final int COUNTDOWN_SECONDS = 10;
    private static final int SPIN_SECONDS = 5;
    private static final int TICKS_PER_SECOND = 20;
    private static final int SPIN_TICKS = SPIN_SECONDS * TICKS_PER_SECOND;

    private enum Phase { COUNTDOWN, SPINNING }

    private static MinecraftServer activeServer;
    private static Phase phase;
    private static int ticksInPhase;
    private static int secondsLeft;
    private static int nextSpinTickAt;
    private static DyeColor drawnColor;

    private RouletteSequence() {
    }

    public static void register() {
        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> tick(event.server()));
    }

    public static boolean isActive() {
        return activeServer != null;
    }

    /** Starts the countdown. Returns false if already running, or the pool is empty. */
    public static boolean start(MinecraftServer server) {
        if (isActive()) {
            return false;
        }

        ColorPoolData pool = server.overworld().getDataStorage().computeIfAbsent(ColorPoolData.TYPE);
        if (pool.getRemaining().isEmpty()) {
            broadcastMessage(server, "[ColorApocalypse] No colors left in the pool. Use /colorapocalypse reset to refill it.");
            return false;
        }

        activeServer = server;
        phase = Phase.COUNTDOWN;
        ticksInPhase = 0;
        secondsLeft = COUNTDOWN_SECONDS;
        showCountdown(server, secondsLeft);
        return true;
    }

    private static void tick(MinecraftServer server) {
        if (!isActive() || server != activeServer) {
            return;
        }
        ticksInPhase++;

        if (phase == Phase.COUNTDOWN) {
            tickCountdown(server);
        } else {
            tickSpin(server);
        }
    }

    private static void tickCountdown(MinecraftServer server) {
        if (ticksInPhase < TICKS_PER_SECOND) {
            return;
        }
        ticksInPhase = 0;
        secondsLeft--;

        if (secondsLeft > 0) {
            showCountdown(server, secondsLeft);
        } else {
            beginSpin(server);
        }
    }

    private static void showCountdown(MinecraftServer server, int seconds) {
        PlayerList players = server.getPlayerList();
        players.broadcastAll(new ClientboundSetTitlesAnimationPacket(0, 25, 5));
        players.broadcastAll(new ClientboundSetTitleTextPacket(
            Component.literal(String.valueOf(seconds)).withStyle(ChatFormatting.YELLOW)));
        players.broadcastAll(new ClientboundSetSubtitleTextPacket(
            Component.literal("The wheel is about to spin...")));
        SoundBroadcaster.playToAll(server, ModSounds.COUNTDOWN_TICK, SoundSource.MASTER, 1f, 1f);
    }

    private static void beginSpin(MinecraftServer server) {
        ColorPoolData pool = server.overworld().getDataStorage().computeIfAbsent(ColorPoolData.TYPE);
        drawnColor = pool.draw(ThreadLocalRandom.current());
        if (drawnColor == null) {
            broadcastMessage(server, "[ColorApocalypse] No colors left in the pool. Use /colorapocalypse reset to refill it.");
            activeServer = null;
            return;
        }

        phase = Phase.SPINNING;
        ticksInPhase = 0;
        nextSpinTickAt = 0;

        PlayerList players = server.getPlayerList();
        players.broadcastAll(new ClientboundSetTitlesAnimationPacket(0, SPIN_TICKS, 5));
        players.broadcastAll(new ClientboundSetTitleTextPacket(Component.empty()));
        players.broadcastAll(new ClientboundSetSubtitleTextPacket(Component.literal("The wheel is spinning...")));
        SoundBroadcaster.playToAll(server, ModSounds.ROULETTE_APPEAR, SoundSource.MASTER, 1f, 1f);
        ModNetworking.sendToAll(new RouletteSpinPacket(drawnColor, SPIN_TICKS));
    }

    private static void tickSpin(MinecraftServer server) {
        if (ticksInPhase >= nextSpinTickAt) {
            nextSpinTickAt = SpinTiming.nextChangeAt(ticksInPhase, SPIN_TICKS);
            float pitch = 0.8f + ThreadLocalRandom.current().nextFloat() * 0.7f;
            SoundBroadcaster.playToAll(server, ModSounds.ROULETTE_SPIN_TICK, SoundSource.MASTER, 1f, pitch);
        }

        if (ticksInPhase >= SPIN_TICKS) {
            reveal(server);
            activeServer = null;
        }
    }

    private static void reveal(MinecraftServer server) {
        DyeColor color = drawnColor;
        int chunkCount = DestructionQueue.start(color, server);
        int mobsKilled = Config.MOB_KILL_ENABLED.get() ? MobDestroyer.kill(color, server) : 0;

        PlayerList players = server.getPlayerList();
        players.broadcastAll(new ClientboundSetTitlesAnimationPacket(5, 60, 20));
        players.broadcastAll(new ClientboundSetTitleTextPacket(
            Component.literal(color.getName().toUpperCase())
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color.getTextColor())))));
        players.broadcastAll(new ClientboundSetSubtitleTextPacket(
            Component.literal("has been eliminated!")));
        SoundBroadcaster.playToAll(server, ModSounds.ROULETTE_REVEAL, SoundSource.MASTER, 1f, 1f);

        String mobSuffix = Config.MOB_KILL_ENABLED.get() ? " and killing " + mobsKilled + " mob(s)" : "";
        broadcastMessage(server, "[ColorApocalypse] " + color.getName() + " eliminated! Destroying its blocks across "
            + chunkCount + " chunk(s)" + mobSuffix + "...");
    }

    private static void broadcastMessage(MinecraftServer server, String message) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }
}
