package org.furranystudio.colorapocalypse.sound;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.concurrent.ThreadLocalRandom;

public final class SoundBroadcaster {

    private SoundBroadcaster() {
    }

    // Player.playSound() excludes that same player (it assumes their client already predicted
    // the sound) - sending the packet straight to each connection, centered on that player's
    // own position, is what actually reaches everyone regardless of where they are.
    public static void playToAll(MinecraftServer server, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundSoundPacket(
                sound, source, player.getX(), player.getY(), player.getZ(), volume, pitch, ThreadLocalRandom.current().nextLong()));
        }
    }
}
