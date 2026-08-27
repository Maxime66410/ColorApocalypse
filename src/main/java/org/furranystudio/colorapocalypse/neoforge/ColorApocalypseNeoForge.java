/**
 * File: ColorApocalypseNeoForge.java
 * Author: Maxime66410
 * Created: 2026-08-27
 * Last Modified: 2026-08-27
 */
package org.furranystudio.colorapocalypse.neoforge;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.furranystudio.colorapocalypse.Colorapocalypse;
import org.furranystudio.colorapocalypse.Config;
import org.furranystudio.colorapocalypse.Platform;
import org.furranystudio.colorapocalypse.client.ClientRouletteState;
import org.furranystudio.colorapocalypse.client.RouletteBarLayer;
import org.furranystudio.colorapocalypse.color.DestructionQueue;
import org.furranystudio.colorapocalypse.command.ColorApocalypseCommand;
import org.furranystudio.colorapocalypse.network.ModNetworking;
import org.furranystudio.colorapocalypse.network.RouletteSpinPacket;
import org.furranystudio.colorapocalypse.timer.AutoTrigger;
import org.furranystudio.colorapocalypse.timer.RouletteSequence;

@Mod(Colorapocalypse.MODID)
public class ColorApocalypseNeoForge {

    public ColorApocalypseNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        Platform.init(FMLPaths.GAMEDIR.get());
        Config.registerSettings();

        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> {
            var registrar = event.registrar("1");
            registrar.playToClient(RouletteSpinPacket.TYPE, RouletteSpinPacket.STREAM_CODEC,
                (payload, context) -> payload.apply());
        });
        ModNetworking.init(PacketDistributor::sendToAllPlayers);

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            AutoTrigger.tick(event.getServer());
            DestructionQueue.tick();
            RouletteSequence.tick(event.getServer());
        });

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
            ColorApocalypseCommand.register(event.getDispatcher()));

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> ClientRouletteState.tick());

            NeoForge.EVENT_BUS.addListener((RenderGuiLayerEvent.Pre event) -> {
                if (event.getName().equals(VanillaGuiLayers.EXPERIENCE_LEVEL) && ClientRouletteState.isSpinning()) {
                    event.setCanceled(true);
                }
            });

            modEventBus.addListener((RegisterGuiLayersEvent event) ->
                event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL,
                    Identifier.fromNamespaceAndPath(Colorapocalypse.MODID, "roulette_bar"),
                    (graphics, deltaTracker) -> {
                        if (ClientRouletteState.isSpinning()) {
                            RouletteBarLayer.draw(graphics, deltaTracker);
                        }
                    }));
        }

        modEventBus.addListener((FMLCommonSetupEvent event) -> Colorapocalypse.commonSetup());
    }
}
