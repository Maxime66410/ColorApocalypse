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
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.event.TickEvent;
import org.furranystudio.colorapocalypse.color.ColorPoolData;
import org.furranystudio.colorapocalypse.color.DestructionQueue;

import java.util.concurrent.ThreadLocalRandom;

/** 10s title countdown, then reveals the drawn color (in its own color) and starts destruction. */
public final class RouletteSequence {

    private static final int COUNTDOWN_SECONDS = 10;
    private static final int TICKS_PER_SECOND = 20;

    private static MinecraftServer activeServer;
    private static int secondsLeft = -1;
    private static int tickCounter;

    private RouletteSequence() {
    }

    public static void register() {
        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> tick(event.server()));
    }

    public static boolean isActive() {
        return secondsLeft >= 0;
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
        secondsLeft = COUNTDOWN_SECONDS;
        tickCounter = 0;
        showCountdown(server, secondsLeft);
        return true;
    }

    private static void tick(MinecraftServer server) {
        if (!isActive() || server != activeServer) {
            return;
        }

        if (++tickCounter < TICKS_PER_SECOND) {
            return;
        }
        tickCounter = 0;
        secondsLeft--;

        if (secondsLeft > 0) {
            showCountdown(server, secondsLeft);
        } else {
            reveal(server);
            secondsLeft = -1;
            activeServer = null;
        }
    }

    private static void showCountdown(MinecraftServer server, int seconds) {
        PlayerList players = server.getPlayerList();
        players.broadcastAll(new ClientboundSetTitlesAnimationPacket(0, 25, 5));
        players.broadcastAll(new ClientboundSetTitleTextPacket(
            Component.literal(String.valueOf(seconds)).withStyle(ChatFormatting.YELLOW)));
        players.broadcastAll(new ClientboundSetSubtitleTextPacket(
            Component.literal("The wheel is about to spin...")));
    }

    private static void reveal(MinecraftServer server) {
        ColorPoolData pool = server.overworld().getDataStorage().computeIfAbsent(ColorPoolData.TYPE);
        DyeColor color = pool.draw(ThreadLocalRandom.current());
        if (color == null) {
            broadcastMessage(server, "[ColorApocalypse] No colors left in the pool. Use /colorapocalypse reset to refill it.");
            return;
        }

        int chunkCount = DestructionQueue.start(color, server);

        PlayerList players = server.getPlayerList();
        players.broadcastAll(new ClientboundSetTitlesAnimationPacket(5, 60, 20));
        players.broadcastAll(new ClientboundSetTitleTextPacket(
            Component.literal(color.getName().toUpperCase())
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color.getTextColor())))));
        players.broadcastAll(new ClientboundSetSubtitleTextPacket(
            Component.literal("has been eliminated!")));

        broadcastMessage(server, "[ColorApocalypse] " + color.getName() + " eliminated! Destroying its blocks across "
            + chunkCount + " chunk(s)...");
    }

    private static void broadcastMessage(MinecraftServer server, String message) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }
}
