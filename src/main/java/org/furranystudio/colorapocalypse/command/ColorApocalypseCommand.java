package org.furranystudio.colorapocalypse.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.furranystudio.colorapocalypse.color.ColorBlockRegistry;
import org.furranystudio.colorapocalypse.settings.SettingsRegistry;

import java.util.List;
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
        // DEBUG ONLY: no color pool / destruction system yet, this just verifies the
        // MapColor -> DyeColor classification by picking a random color and listing the
        // blocks that would be destroyed. Nothing is actually destroyed here.
        DyeColor[] colors = DyeColor.values();
        DyeColor color = colors[ThreadLocalRandom.current().nextInt(colors.length)];
        List<Block> blocks = ColorBlockRegistry.getBlocksFor(color);

        StringBuilder message = new StringBuilder(
            "[ColorApocalypse] (debug) Drew " + color.getName() + " (" + blocks.size() + " blocks):");
        for (Block block : blocks) {
            message.append("\n - ").append(ForgeRegistries.BLOCKS.getKey(block));
        }
        context.getSource().sendSuccess(() -> Component.literal(message.toString()), true);
        return 1;
    }

    private static int runStatus(CommandContext<CommandSourceStack> context) {
        // TODO: report time remaining until the next automatic draw once the timer exists, later too
        context.getSource().sendSuccess(() -> Component.literal("[ColorApocalypse] status: not implemented yet."), true);
        return 1;
    }

    private static int runReset(CommandContext<CommandSourceStack> context) {
        // TODO: reset the color pool once it exists, wawa later ;)
        context.getSource().sendSuccess(() -> Component.literal("[ColorApocalypse] reset: not implemented yet."), true);
        return 1;
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
