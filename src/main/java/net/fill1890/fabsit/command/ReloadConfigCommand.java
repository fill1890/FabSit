package net.fill1890.fabsit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fill1890.fabsit.config.ConfigManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class ReloadConfigCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(literal("fabsit")
                .then(literal("reload")
                .requires(Permissions.require("fabsit.reload", 2))
                .executes(ReloadConfigCommand::run)));
    }

    public static int run(CommandContext<ServerCommandSource> context) {
        if(ConfigManager.loadConfig()) {
            context.getSource().sendFeedback(Text.translatable("chat.fabsit.reload_success"), false);
            return 0;
        } else {
            context.getSource().sendError(Text.translatable("chat.fabsit.reload_error").formatted(Formatting.RED));
            return -1;
        }
    }
}
