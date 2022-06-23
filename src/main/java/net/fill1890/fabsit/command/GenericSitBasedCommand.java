package net.fill1890.fabsit.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.entity.Pose;
import net.fill1890.fabsit.entity.PoseManagerEntity;
import net.fill1890.fabsit.error.PoseException;
import net.fill1890.fabsit.util.Messages;
import net.fill1890.fabsit.util.PoseTest;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Generic sit-based command class
 * <br>
 * Implementation details taken from <a href="https://github.com/Gecolay/GSit">GSit</a>
 */
public abstract class GenericSitBasedCommand {
    public static int run(CommandContext<ServerCommandSource> context, Pose pose) {
        final ServerCommandSource source = context.getSource();
        ServerPlayerEntity player;

        try {
            player = source.getPlayerOrThrow();
        } catch (CommandSyntaxException e) {
            source.sendError(Text.of("You must be a player to run this command!"));
            return -1;
        }

        return run(player, pose);
    }

    public static int run(ServerPlayerEntity player, Pose pose) {
        // check the pose is config-enabled
        try {
            PoseTest.confirmEnabled(pose);
        } catch(PoseException e) {
            if(ConfigManager.getConfig().enable_messages.pose_errors)
                Messages.sendByException(player, pose, e);
            return -1;
        }


        // toggle sitting if the player was sat down
        if(player.hasVehicle()) {
            player.dismountVehicle();
            return 1;
        }

        // confirm player can pose right now
        try {
            PoseTest.confirmPosable(player);
        } catch (PoseException e) {
            if(ConfigManager.getConfig().enable_messages.pose_errors)
                Messages.sendByException(player, pose, e);
            return -1;
        }

        // create a new pose manager for laying and sit the player down
        // (player is then invisible and an npc lays down)
        PoseManagerEntity chair = new PoseManagerEntity(player.getPos(), pose, player);
        player.getEntityWorld().spawnEntity(chair);
        player.startRiding(chair, true);

        return 1;
    }
}
