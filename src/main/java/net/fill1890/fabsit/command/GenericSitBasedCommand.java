package net.fill1890.fabsit.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.entity.Pose;
import net.fill1890.fabsit.entity.PoseManagerEntity;
import net.fill1890.fabsit.entity.ChairPosition;
import net.fill1890.fabsit.error.PoseException;
import net.fill1890.fabsit.util.Messages;
import net.fill1890.fabsit.util.PoseTest;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Calendar;

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

    public static int run(ServerPlayerEntity player, Pose pose) { return run(player, pose, null, ChairPosition.ON_BLOCK); }

    public static int run(ServerPlayerEntity player, Pose pose, @Nullable Vec3d pos, ChairPosition chairPosition) {
        // check the pose is config-enabled
        try {
            PoseTest.confirmEnabled(pose);
        } catch(PoseException e) {
            if(ConfigManager.getConfig().enable_messages.pose_errors)
                Messages.sendByException(player, pose, e);
            return -1;
        }

        long currentTime = Calendar.getInstance().getTimeInMillis();
        Long lastUse = ConfigManager.lastUses.get(player);
        // force a 500ms delay between running commands
        if(lastUse != null) {
            if(currentTime - lastUse < 500) return -1;
        }
        ConfigManager.lastUses.put(player, currentTime);

        // toggle sitting if the player was sat down
        if(player.hasVehicle()) {
            player.stopRiding();
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

        Vec3d sitPos = pos;
        if(sitPos == null && ConfigManager.getConfig().centre_on_blocks) {
            // centre on blocks if enabled in config
            BlockPos block = player.getBlockPos();
            sitPos = new Vec3d(block.getX() + 0.5d, block.getY(), block.getZ() + 0.5d);
        } else if(sitPos == null) {
            sitPos = player.getPos();
        }

        PoseManagerEntity chair = new PoseManagerEntity(sitPos, pose, player, chairPosition);
        if(ConfigManager.getConfig().centre_on_blocks || chairPosition == ChairPosition.IN_BLOCK)
            ConfigManager.occupiedBlocks.add(new BlockPos(sitPos));

        player.getEntityWorld().spawnEntity(chair);
        player.startRiding(chair, true);

        return 1;
    }
}
