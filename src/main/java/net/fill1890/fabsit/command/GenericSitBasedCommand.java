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
    // run from a command
    public static int run(CommandContext<ServerCommandSource> context, Pose pose) {
        // confirm command was run by player and not console, command block, etc
        final ServerCommandSource source = context.getSource();
        ServerPlayerEntity player;

        try {
            player = source.getPlayerOrThrow();
        } catch (CommandSyntaxException e) {
            source.sendError(Text.of("You must be a player to run this command!"));
            return -1;
        }

        // run as normal
        return run(player, pose);
    }

    // run either from command or from packet request
    public static int run(ServerPlayerEntity player, Pose pose) { return run(player, pose, null, ChairPosition.ON_BLOCK); }

    // run with a specific sit position and block-relative location
    // extra parameters for sitting on stairs and slabs
    // position to sit at, and whether sitting on or in (slab/stair) a block
    public static int run(ServerPlayerEntity player, Pose pose, @Nullable Vec3d pos, ChairPosition chairPosition) {
        // check the pose is config-enabled
        try {
            PoseTest.confirmEnabled(pose);
        } catch(PoseException e) {
            if(ConfigManager.getConfig().enable_messages.pose_errors)
                Messages.sendByException(player, pose, e);
            return -1;
        }

        // force a 500ms delay between running commands
        long currentTime = Calendar.getInstance().getTimeInMillis();
        Long lastUse = ConfigManager.lastUses.get(player);
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
            // use the current player position otherwise
            sitPos = player.getPos();
        }

        // set up the chair and register the block as occupied if config-enabled or using stair/slab
        PoseManagerEntity chair = new PoseManagerEntity(sitPos, pose, player, chairPosition);

        player.getEntityWorld().spawnEntity(chair);
        player.startRiding(chair, true);

        return 1;
    }
}
