package net.fill1890.fabsit.util;

import net.fill1890.fabsit.config.Config;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.entity.Pose;
import net.fill1890.fabsit.error.PoseException;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

// maybe rename this later
public interface PoseTest {
    /**
     * Check if a player can currently perform a given pose
     *
     * On a successful return, pose is valid
     * If pose is invalid, will send the relevant message to the player and throw an exception
     *
     * @param player player to check posing for
     * @param pose pose player is attempting
     * @throws PoseException if pose is not valid
     */
    static void confirmPosable(ServerPlayerEntity player, Pose pose) throws PoseException {
        // check if spectating
        if(player.isSpectator()) {
            player.sendMessage(Messages.getSpectatorError(pose, player));
            throw new PoseException.SpectatorException();
        }

        // check if underwater
        if(player.isInsideWaterOrBubbleColumn() && !ConfigManager.getConfig().allow_posing_underwater) {
            player.sendMessage(Messages.getStateError(pose, player));
            throw new PoseException.StateException();
        }

        // check if flying, swimming, sleeping, or underwater
        if(
                player.isFallFlying()
                || player.isSwimming()
                || player.isSleeping())
        {
            player.sendMessage(Messages.getStateError(pose, player));
            throw new PoseException.StateException();
        }

        BlockState below = player.getEntityWorld().getBlockState(new BlockPos(player.getPos()).down());

        // check if in midair
        if(below.isAir() && !ConfigManager.getConfig().allow_posing_midair) {
            player.sendMessage(Messages.getMidairError(pose, player));
            throw new PoseException.MidairException();
        }
    }

    static boolean confirmEnabled(ServerPlayerEntity player, Pose pose) {
        Config.Poses poses = ConfigManager.getConfig().allow_poses;
        boolean allowed = switch (pose) {
            case LAYING -> poses.lay;
            case SPINNING -> poses.spin;
            case SITTING -> poses.sit;
        };

        if(!allowed) player.sendMessage(Messages.poseDisabledError(pose, player));

        return allowed;
    }
}
