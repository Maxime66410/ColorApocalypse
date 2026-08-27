/**
 * File: ColorApocalypseFabricClient.java
 * Author: Maxime66410
 * Created: 2026-08-27
 * Last Modified: 2026-08-27
 */
package org.furranystudio.colorapocalypse.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import org.furranystudio.colorapocalypse.client.ClientRouletteState;
import org.furranystudio.colorapocalypse.client.RouletteBarLayer;
import org.furranystudio.colorapocalypse.network.RouletteSpinPacket;

// Client-only Fabric entrypoint (declared as "client" in fabric.mod.json).
public final class ColorApocalypseFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientRouletteState.tick());

        ClientPlayNetworking.registerGlobalReceiver(RouletteSpinPacket.TYPE, (packet, context) -> packet.apply());

        HudElementRegistry.replaceElement(VanillaHudElements.INFO_BAR, original -> (graphics, deltaTracker) -> {
            if (ClientRouletteState.isSpinning()) {
                RouletteBarLayer.draw(graphics, deltaTracker);
            } else {
                original.extractRenderState(graphics, deltaTracker);
            }
        });
    }
}
