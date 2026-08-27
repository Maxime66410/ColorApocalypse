/**
 * File: ModNetworking.java
 * Author: Maxime66410
 * Created: 2026-08-23
 * Last Modified: 2026-08-27
 */
package org.furranystudio.colorapocalypse.network;

// Loader-agnostic send point. Each loader's bootstrap sets the Sender once at startup, wiring
// it to its own networking API (Forge: SimpleChannel; Fabric: ServerPlayNetworking).
public final class ModNetworking {

    public interface Sender {
        void sendToAll(RouletteSpinPacket packet);
    }

    private static Sender sender;

    private ModNetworking() {
    }

    public static void init(Sender sender) {
        ModNetworking.sender = sender;
    }

    public static void sendToAll(RouletteSpinPacket packet) {
        sender.sendToAll(packet);
    }
}
