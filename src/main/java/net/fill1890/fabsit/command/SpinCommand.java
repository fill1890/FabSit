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
 * /spin command implementation
 * <br>
 * Requires <code>fabsit.commands.spin</code> permission node, granted to all players by default
 */
public abstract class SpinCommand {
    protected static final Pose POSE = Pose.SPINNING;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(literal("spin")
                .requires(Permissions.require("fabsit.commands.spin", true))
                .executes((CommandContext<ServerCommandSource> context) -> GenericSitBasedCommand.run(context, POSE)));
    }
}