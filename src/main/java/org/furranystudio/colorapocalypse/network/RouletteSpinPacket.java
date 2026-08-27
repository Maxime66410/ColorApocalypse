/**
 * File: RouletteSpinPacket.java
 * Author: Maxime66410
 * Created: 2026-08-23
 * Last Modified: 2026-08-27
 */
package org.furranystudio.colorapocalypse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import org.furranystudio.colorapocalypse.Colorapocalypse;
import org.furranystudio.colorapocalypse.client.ClientRouletteState;

/**
 * Server -> client: the roulette started spinning, and will land on {@code color} in
 * {@code durationTicks}. Implements vanilla {@link CustomPacketPayload} for Fabric's
 * networking API; Forge's SimpleChannel uses {@link #encode}/{@link #decode} directly and
 * doesn't care about the payload type.
 */
public record RouletteSpinPacket(DyeColor color, int durationTicks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RouletteSpinPacket> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Colorapocalypse.MODID, "roulette_spin"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RouletteSpinPacket> STREAM_CODEC =
        CustomPacketPayload.codec(RouletteSpinPacket::encode, RouletteSpinPacket::decode);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RouletteSpinPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.color());
        buf.writeVarInt(msg.durationTicks());
    }

    public static RouletteSpinPacket decode(FriendlyByteBuf buf) {
        return new RouletteSpinPacket(buf.readEnum(DyeColor.class), buf.readVarInt());
    }

    public void apply() {
        ClientRouletteState.start(color(), durationTicks());
    }
}
