package org.furranystudio.colorapocalypse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.furranystudio.colorapocalypse.client.ClientRouletteState;

/** Server -> client: the roulette started spinning, and will land on {@code color} in {@code durationTicks}. */
public record RouletteSpinPacket(DyeColor color, int durationTicks) {

    public static void encode(RouletteSpinPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.color());
        buf.writeVarInt(msg.durationTicks());
    }

    public static RouletteSpinPacket decode(FriendlyByteBuf buf) {
        return new RouletteSpinPacket(buf.readEnum(DyeColor.class), buf.readVarInt());
    }

    public static void handle(RouletteSpinPacket msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> ClientRouletteState.start(msg.color(), msg.durationTicks()));
        ctx.setPacketHandled(true);
    }
}
