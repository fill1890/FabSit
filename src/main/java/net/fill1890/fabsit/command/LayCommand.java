package net.fill1890.fabsit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.entity.Pose;
import net.fill1890.fabsit.entity.PoseManagerEntity;
import net.fill1890.fabsit.error.PoseException;
import net.fill1890.fabsit.util.Messages;
import net.fill1890.fabsit.util.PoseTest;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * /lay command implementation
 * <br>
 * Requires <code>fabsit.commands.lay</code> permission node, granted to all players by default
 * <br>
 * Implementation details taken from <a href="https://github.com/Gecolay/GSit">GSit</a>
 */
// TODO: implement generic pose class?
public class LayCommand {
    protected static final Pose POSE = Pose.LAYING;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
       dispatcher.register(literal("lay")
               .requires(Permissions.require("fabsit.commands.lay", true))
               .executes(LayCommand::run));
    }

    public static int run(CommandContext<ServerCommandSource> context) {
        final ServerCommandSource source = context.getSource();
        ServerPlayerEntity player;

        try {
            player = source.getPlayerOrThrow();
        } catch (CommandSyntaxException e) {
            source.sendError(Text.of("You must be a player to run this command!"));
            return -1;
        }

        // check the pose is config-enabled
        try {
            PoseTest.confirmEnabled(POSE);
        } catch(PoseException e) {
            if(ConfigManager.getConfig().enable_messages.pose_errors)
                Messages.sendByException(player, POSE, e);
            return -1;
        }


        // toggle sitting if the player was sat down
        if(player.hasVehicle()) {
            player.dismountVehicle();
            // TODO: should be able to add to manager?
            player.teleport(player.getX(), player.getY() + 0.6, player.getZ());
            return 1;
        }

        // confirm player can pose right now
        try {
            PoseTest.confirmPosable(player);
        } catch (PoseException e) {
            if(ConfigManager.getConfig().enable_messages.pose_errors)
                Messages.sendByException(player, POSE, e);
            return -1;
        }

        // create a new pose manager for laying and sit the player down
        // (player is then invisible and an npc lays down)
        PoseManagerEntity chair = new PoseManagerEntity(player.getEntityWorld(), player.getPos(), POSE, player);
        player.getEntityWorld().spawnEntity(chair);
        player.startRiding(chair, true);

        return 1;

    }
}
