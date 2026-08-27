/**
 * File: ColorApocalypseFabric.java
 * Author: Maxime66410
 * Created: 2026-08-27
 * Last Modified: 2026-08-27
 */
package org.furranystudio.colorapocalypse.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.furranystudio.colorapocalypse.Colorapocalypse;
import org.furranystudio.colorapocalypse.Config;
import org.furranystudio.colorapocalypse.Platform;
import org.furranystudio.colorapocalypse.color.DestructionQueue;
import org.furranystudio.colorapocalypse.command.ColorApocalypseCommand;
import org.furranystudio.colorapocalypse.network.ModNetworking;
import org.furranystudio.colorapocalypse.network.RouletteSpinPacket;
import org.furranystudio.colorapocalypse.timer.AutoTrigger;
import org.furranystudio.colorapocalypse.timer.RouletteSequence;

// Fabric entrypoint (declared as "main" in fabric.mod.json - runs on both client and dedicated
// server). See ColorApocalypseFabricClient for the client-only HUD/tick wiring.
public final class ColorApocalypseFabric implements ModInitializer {

    private static MinecraftServer runningServer;

    @Override
    public void onInitialize() {
        Platform.init(FabricLoader.getInstance().getGameDir());
        Config.registerSettings();

        PayloadTypeRegistry.clientboundPlay().register(RouletteSpinPacket.TYPE, RouletteSpinPacket.STREAM_CODEC);
        ModNetworking.init(packet -> {
            if (runningServer != null) {
                for (ServerPlayer player : PlayerLookup.all(runningServer)) {
                    ServerPlayNetworking.send(player, packet);
                }
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> runningServer = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> runningServer = null);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            AutoTrigger.tick(server);
            DestructionQueue.tick();
            RouletteSequence.tick(server);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
            ColorApocalypseCommand.register(dispatcher));

        Colorapocalypse.commonSetup();
    }
}
