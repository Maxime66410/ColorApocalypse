/**
 * File: SoundBroadcaster.java
 * Author: Maxime66410
 * Created: 2026-08-23
 * Last Modified: 2026-08-24
 */
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

    // Generic Function to play everyone the same Sound
    public static void playToAll(MinecraftServer server, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundSoundPacket(
                sound, source, player.getX(), player.getY(), player.getZ(), volume, pitch, ThreadLocalRandom.current().nextLong()));
        }
    }
}
