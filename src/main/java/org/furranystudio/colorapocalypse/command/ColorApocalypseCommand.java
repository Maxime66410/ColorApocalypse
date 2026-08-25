package org.furranystudio.colorapocalypse.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
        return AutoTrigger.trigger(context.getSource().getServer()) ? 1 : 0;
    }

    private static int runStatus(CommandContext<CommandSourceStack> context) {
        var server = context.getSource().getServer();
        Set<DyeColor> remaining = getPool(context).getRemaining();

        MutableComponent message = Component.translatable(
            "colorapocalypse.status.header", remaining.size(), DyeColor.values().length);
        for (DyeColor color : DyeColor.values()) {
            if (remaining.contains(color)) {
                message.append(" ").append(colorName(color));
            }
        }

        long ticksRemaining = AutoTrigger.getTicksRemaining(server);
        if (ticksRemaining < 0) {
            message.append(Component.translatable("colorapocalypse.status.timer_off"));
        } else {
            message.append(Component.translatable("colorapocalypse.status.next_draw", ticksRemaining / 20));
        }

        context.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    private static int runReset(CommandContext<CommandSourceStack> context) {
        getPool(context).reset();
        context.getSource().sendSuccess(() -> Component.translatable("colorapocalypse.reset.success"), true);
        return 1;
    }

    private static int runPause(CommandContext<CommandSourceStack> context) {
        boolean enabled = !Config.AUTO_TIMER_ENABLED.get();
        Config.AUTO_TIMER_ENABLED.set(enabled);
        context.getSource().sendSuccess(() -> Component.translatable(
            enabled ? "colorapocalypse.pause.resumed" : "colorapocalypse.pause.paused"), true);
        return 1;
    }

    private static ColorPoolData getPool(CommandContext<CommandSourceStack> context) {
        return context.getSource().getServer().overworld().getDataStorage().computeIfAbsent(ColorPoolData.TYPE);
    }

    private static int runSettingsList(CommandContext<CommandSourceStack> context) {
        MutableComponent message = Component.translatable("colorapocalypse.settings.header");
        for (String name : SettingsRegistry.names()) {
            message.append(Component.translatable("colorapocalypse.settings.entry", name, SettingsRegistry.get(name).get()));
        }
        context.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    private static int runSettings(CommandContext<CommandSourceStack> context) {
        String parameter = StringArgumentType.getString(context, "parameter");
        String value = StringArgumentType.getString(context, "value");

        SettingsRegistry.Setting setting = SettingsRegistry.get(parameter);
        if (setting == null) {
            context.getSource().sendFailure(Component.translatable("colorapocalypse.settings.unknown", parameter));
            return 0;
        }

        String error = setting.trySet(value);
        if (error != null) {
            context.getSource().sendFailure(Component.literal("[ColorApocalypse] " + error));
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.translatable(
            "colorapocalypse.settings.set", parameter, setting.get()), true);
        return 1;
    }

    private static Component colorName(DyeColor color) {
        return Component.translatable("color.minecraft." + color.getName());
    }
}
