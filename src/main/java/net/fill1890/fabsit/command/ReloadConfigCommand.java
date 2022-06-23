package net.fill1890.fabsit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.error.LoadConfigException;
import net.fill1890.fabsit.util.Messages;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.literal;

public abstract class ReloadConfigCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(literal("fabsit")
                .then(literal("reload")
                .requires(Permissions.require("fabsit.reload", 2))
                .executes(ReloadConfigCommand::run)));
    }

    public static int run(CommandContext<ServerCommandSource> context) {
        try {
            ConfigManager.loadConfig();
            context.getSource().sendFeedback(Messages.configLoadSuccess(context.getSource().getPlayer()), false);
            return 0;
        } catch (LoadConfigException ignored) {
            context.getSource().sendError(Messages.configLoadError(context.getSource().getPlayer()));
            return -1;
        }
    }
}
