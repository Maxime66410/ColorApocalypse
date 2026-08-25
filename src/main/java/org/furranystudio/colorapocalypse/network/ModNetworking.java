/**
 * File: ModNetworking.java
 * Author: Maxime66410
 * Created: 2026-08-23
 * Last Modified: 2026-08-24
 */
package org.furranystudio.colorapocalypse.network;

import net.minecraft.resources.Identifier;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import org.furranystudio.colorapocalypse.Colorapocalypse;

public final class ModNetworking {

    private static final SimpleChannel CHANNEL = ChannelBuilder
        .named(Identifier.fromNamespaceAndPath(Colorapocalypse.MODID, "main"))
        .networkProtocolVersion(1)
        .simpleChannel();

    private ModNetworking() {
    }

    public static void register() {
        CHANNEL.messageBuilder(RouletteSpinPacket.class, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(RouletteSpinPacket::encode)
            .decoder(RouletteSpinPacket::decode)
            .consumerMainThread(RouletteSpinPacket::handle)
            .add();
    }

    public static void sendToAll(Object message) {
        CHANNEL.send(message, PacketDistributor.ALL.noArg());
    }
}
