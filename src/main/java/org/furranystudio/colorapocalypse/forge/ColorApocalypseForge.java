/**
 * File: ColorApocalypseForge.java
 * Author: Maxime66410
 * Created: 2026-08-27
 * Last Modified: 2026-08-27
 */
package org.furranystudio.colorapocalypse.forge;

import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
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

// Forge entrypoint - wires the shared/common code to Forge's event bus, config-free (Config
// is a hand-rolled JSON file now, see org.furranystudio.colorapocalypse.Config) and networking.
@Mod(Colorapocalypse.MODID)
public class ColorApocalypseForge {

    private static final SimpleChannel CHANNEL = ChannelBuilder
        .named(Identifier.fromNamespaceAndPath(Colorapocalypse.MODID, "main"))
        .networkProtocolVersion(1)
        .simpleChannel();

    public ColorApocalypseForge(FMLJavaModLoadingContext context) {
        Platform.init(FMLPaths.GAMEDIR.get());
        Config.registerSettings();

        CHANNEL.messageBuilder(RouletteSpinPacket.class, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(RouletteSpinPacket::encode)
            .decoder(RouletteSpinPacket::decode)
            .consumerMainThread((packet, ctx) -> {
                packet.apply();
                ctx.setPacketHandled(true);
            })
            .add();
        ModNetworking.init(packet -> CHANNEL.send(packet, PacketDistributor.ALL.noArg()));

        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> {
            AutoTrigger.tick(event.server());
            DestructionQueue.tick();
            RouletteSequence.tick(event.server());
        });

        RegisterCommandsEvent.BUS.addListener(event -> ColorApocalypseCommand.register(event.getDispatcher()));

        if (FMLEnvironment.dist == Dist.CLIENT) {
            TickEvent.ClientTickEvent.Post.BUS.addListener(event -> ClientRouletteState.tick());

            AddGuiOverlayLayersEvent.BUS.addListener(event -> {
                ForgeLayeredDraw layers = event.getLayeredDraw();
                layers.addConditionTo(ForgeLayeredDraw.HOTBAR_AND_DECOS, ForgeLayeredDraw.BACKGROUND,
                    () -> !ClientRouletteState.isSpinning());
                layers.addWithCondition(
                    ForgeLayeredDraw.HOTBAR_AND_DECOS,
                    Identifier.fromNamespaceAndPath(Colorapocalypse.MODID, "roulette_bar"),
                    RouletteBarLayer::draw,
                    ClientRouletteState::isSpinning
                );
            });
        }

        FMLCommonSetupEvent.getBus(context.getModBusGroup()).addListener(event -> Colorapocalypse.commonSetup());
    }
}
