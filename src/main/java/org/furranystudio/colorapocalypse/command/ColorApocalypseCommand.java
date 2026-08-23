package org.furranystudio.colorapocalypse.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.event.RegisterCommandsEvent;
import org.furranystudio.colorapocalypse.Config;
import org.furranystudio.colorapocalypse.color.ColorPoolData;
import org.furranystudio.colorapocalypse.settings.SettingsRegistry;
import org.furranystudio.colorapocalypse.timer.AutoTrigger;

import java.util.Set;

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
                .then(Commands.literal("pause")
                    .executes(ColorApocalypseCommand::runPause))
                .then(Commands.literal("settings")
                    .executes(ColorApocalypseCommand::runSettingsList)
                    .then(Commands.argument("parameter", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(SettingsRegistry.names(), builder))
                        .then(Commands.argument("value", StringArgumentType.word())
                            .executes(ColorApocalypseCommand::runSettings))))
        );
    }

    private static int runStart(CommandContext<CommandSourceStack> context) {
        return AutoTrigger.draw(context.getSource().getServer()) != null ? 1 : 0;
    }

    private static int runStatus(CommandContext<CommandSourceStack> context) {
        var server = context.getSource().getServer();
        Set<DyeColor> remaining = getPool(context).getRemaining();

        StringBuilder message = new StringBuilder(
            "[ColorApocalypse] " + remaining.size() + "/" + DyeColor.values().length + " colors remaining:");
        for (DyeColor color : DyeColor.values()) {
            if (remaining.contains(color)) {
                message.append(' ').append(color.getName());
            }
        }

        long ticksRemaining = AutoTrigger.getTicksRemaining(server);
        if (ticksRemaining < 0) {
            message.append("\nAutomatic timer: off.");
        } else {
            message.append("\nNext automatic draw in ").append(ticksRemaining / 20).append("s.");
        }

        context.getSource().sendSuccess(() -> Component.literal(message.toString()), true);
        return 1;
    }

    private static int runReset(CommandContext<CommandSourceStack> context) {
        getPool(context).reset();
        context.getSource().sendSuccess(() -> Component.literal("[ColorApocalypse] Color pool reset."), true);
        return 1;
    }

    private static int runPause(CommandContext<CommandSourceStack> context) {
        boolean enabled = !Config.AUTO_TIMER_ENABLED.get();
        Config.AUTO_TIMER_ENABLED.set(enabled);
        context.getSource().sendSuccess(() -> Component.literal(
            "[ColorApocalypse] Automatic timer " + (enabled ? "resumed." : "paused.")), true);
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
