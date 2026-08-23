package org.furranystudio.colorapocalypse.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.event.RegisterCommandsEvent;
import org.furranystudio.colorapocalypse.color.ColorPoolData;
import org.furranystudio.colorapocalypse.color.DestructionQueue;
import org.furranystudio.colorapocalypse.settings.SettingsRegistry;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class ColorApocalypseCommand {

    private ColorApocalypseCommand() {
    }

    public static void register() {
        RegisterCommandsEvent.BUS.addListener(ColorApocalypseCommand::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("colorapocalypse")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("start")
                    .executes(ColorApocalypseCommand::runStart))
                .then(Commands.literal("status")
                    .executes(ColorApocalypseCommand::runStatus))
                .then(Commands.literal("reset")
                    .executes(ColorApocalypseCommand::runReset))
                .then(Commands.literal("settings")
                    .executes(ColorApocalypseCommand::runSettingsList)
                    .then(Commands.argument("parameter", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(SettingsRegistry.names(), builder))
                        .then(Commands.argument("value", StringArgumentType.word())
                            .executes(ColorApocalypseCommand::runSettings))))
        );
    }

    private static int runStart(CommandContext<CommandSourceStack> context) {
        ColorPoolData pool = getPool(context);
        DyeColor color = pool.draw(ThreadLocalRandom.current());

        if (color == null) {
            context.getSource().sendFailure(Component.literal(
                "[ColorApocalypse] No colors left in the pool. Use /colorapocalypse reset to refill it."));
            return 0;
        }

        int chunkCount = DestructionQueue.start(color, context.getSource().getServer());

        context.getSource().sendSuccess(() -> Component.literal(
            "[ColorApocalypse] " + color.getName() + " eliminated! Destroying its blocks across "
                + chunkCount + " chunk(s)..."), true);
        return 1;
    }

    private static int runStatus(CommandContext<CommandSourceStack> context) {
        ColorPoolData pool = getPool(context);
        Set<DyeColor> remaining = pool.getRemaining();

        StringBuilder message = new StringBuilder(
            "[ColorApocalypse] " + remaining.size() + "/" + DyeColor.values().length + " colors remaining:");
        for (DyeColor color : DyeColor.values()) {
            if (remaining.contains(color)) {
                message.append(' ').append(color.getName());
            }
        }
        // TODO: report time remaining until the next automatic draw once the timer exists
        context.getSource().sendSuccess(() -> Component.literal(message.toString()), true);
        return 1;
    }

    private static int runReset(CommandContext<CommandSourceStack> context) {
        getPool(context).reset();
        context.getSource().sendSuccess(() -> Component.literal("[ColorApocalypse] Color pool reset."), true);
        return 1;
    }

    private static ColorPoolData getPool(CommandContext<CommandSourceStack> context) {
        return context.getSource().getServer().overworld().getDataStorage().computeIfAbsent(ColorPoolData.TYPE);
    }

    private static int runSettingsList(CommandContext<CommandSourceStack> context) {
        StringBuilder message = new StringBuilder("[ColorApocalypse] Settings:");
        for (String name : SettingsRegistry.names()) {
            message.append("\n - ").append(name).append(" = ").append(SettingsRegistry.get(name).get());
        }
        context.getSource().sendSuccess(() -> Component.literal(message.toString()), true);
        return 1;
    }

    private static int runSettings(CommandContext<CommandSourceStack> context) {
        String parameter = StringArgumentType.getString(context, "parameter");
        String value = StringArgumentType.getString(context, "value");

        SettingsRegistry.Setting setting = SettingsRegistry.get(parameter);
        if (setting == null) {
            context.getSource().sendFailure(Component.literal("[ColorApocalypse] Unknown setting: " + parameter));
            return 0;
        }

        String error = setting.trySet(value);
        if (error != null) {
            context.getSource().sendFailure(Component.literal("[ColorApocalypse] " + error));
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.literal(
            "[ColorApocalypse] " + parameter + " set to " + setting.get() + "."), true);
        return 1;
    }
}
