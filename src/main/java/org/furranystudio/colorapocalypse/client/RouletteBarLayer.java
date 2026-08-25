/**
 * File: RouletteBarLayer.java
 * Author: Maxime66410
 * Created: 2026-08-23
 * Last Modified: 2026-08-24
 */
package org.furranystudio.colorapocalypse.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import org.furranystudio.colorapocalypse.Colorapocalypse;

// Recolors the vanilla XP bar (same sprites, tinted) to show the roulette while it's active. Only ever registered client-side
public final class RouletteBarLayer {

    private static final Identifier BAR_BACKGROUND = Identifier.withDefaultNamespace("hud/experience_bar_background");
    private static final Identifier BAR_PROGRESS = Identifier.withDefaultNamespace("hud/experience_bar_progress");
    private static final int WIDTH = 182;
    private static final int HEIGHT = 5;
    private static final int MARGIN_BOTTOM = 24;

    private RouletteBarLayer() {
    }

    public static void register() {
        AddGuiOverlayLayersEvent.BUS.addListener(RouletteBarLayer::onAddLayers);
    }

    private static void onAddLayers(AddGuiOverlayLayersEvent event) {
        ForgeLayeredDraw layers = event.getLayeredDraw();
        layers.addConditionTo(ForgeLayeredDraw.HOTBAR_AND_DECOS, ForgeLayeredDraw.BACKGROUND,
            () -> !ClientRouletteState.isSpinning());
        layers.addWithCondition(
            ForgeLayeredDraw.HOTBAR_AND_DECOS,
            Identifier.fromNamespaceAndPath(Colorapocalypse.MODID, "roulette_bar"),
            RouletteBarLayer::draw,
            ClientRouletteState::isSpinning
        );
    }

    private static void draw(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Window window = Minecraft.getInstance().getWindow();
        int left = (window.getGuiScaledWidth() - WIDTH) / 2;
        int top = window.getGuiScaledHeight() - MARGIN_BOTTOM - HEIGHT;
        DyeColor current = ClientRouletteState.getCurrentColor();
        int color = current.getTextColor() | 0xFF000000;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_BACKGROUND, left, top, WIDTH, HEIGHT);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_PROGRESS, WIDTH, HEIGHT, 0, 0, left, top, WIDTH, HEIGHT, color);

        Font font = Minecraft.getInstance().font;
        graphics.centeredText(font, Component.translatable("color.minecraft." + current.getName()),
            window.getGuiScaledWidth() / 2, top - 12, color);
    }
}
