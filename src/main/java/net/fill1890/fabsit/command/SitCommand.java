package net.fill1890.fabsit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fill1890.fabsit.entity.Pose;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * /sit command implementation
 * <br>
 * Requires <code>fabsit.commands.sit</code> permission node; granted to all players by default
 */
public abstract class SitCommand {
    protected static final Pose POSE = Pose.SITTING;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(literal("sit")
                .requires(Permissions.require("fabsit.commands.sit", true))
                .executes((CommandContext<ServerCommandSource> context) -> GenericSitBasedCommand.run(context, POSE)));
    }
}