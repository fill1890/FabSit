package net.fill1890.fabsit.util;

import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.entity.Pose;
import net.fill1890.fabsit.error.PoseException;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.fill1890.fabsit.error.PoseException.StateException;
import static net.fill1890.fabsit.error.PoseException.MidairException;
import static net.fill1890.fabsit.error.PoseException.SpectatorException;
import static net.fill1890.fabsit.error.PoseException.PoseDisabled;

// this may not be the best way of doing this kind of function
// but it works for now

/**
 * Message lookup functions
 *
 * Supports both server-side and client-side translation
 * Given a player, will check if the player has the mod loaded locally for local translation support
 * If so, will return a translatable key for local translation
 * If not, will return a static response based on the server locale
 */
public class Messages {
    private static final String ACTION = "action.fabsit.";
    private static final String CHAT = "chat.fabsit.";

    // stop posing action message
    public static Text getPoseStopMessage(Pose pose, ServerPlayerEntity player) {
        if(ConfigManager.loadedPlayers.contains(player)) {
            return Text.translatable(ACTION + "stop_" + pose, Text.keybind("key.sneak"));
        } else {
            return Text.of(ConfigManager.LANG.get(ACTION + "stop_" + pose).formatted("the sneak key"));
        }
    }

    // get either a server or client translated string based on whether the player has the mod
    private static Text getChatMessageByKey(ServerPlayerEntity player, String key_base) {
        if(ConfigManager.loadedPlayers.contains(player)) {
            return Text.translatable(CHAT + key_base);
        } else {
            return Text.of(ConfigManager.LANG.get(CHAT + key_base));
        }
    }

    // trying to pose in midair
    public static Text getMidairError(Pose pose, ServerPlayerEntity player) {
        return getChatMessageByKey(player, switch(pose) {
            case SITTING -> "sit_air_error";
            default -> "pose_air_error";
        });
    }

    // trying to pose while a spectator
    public static Text getSpectatorError(Pose pose, ServerPlayerEntity player) {
        return getChatMessageByKey(player, switch(pose) {
            case SITTING -> "sit_spectator_error";
            default -> "pose_spectator_error";
        });
    }

    // trying to pose while swimming/sleeping/flying/etc
    public static Text getStateError(Pose pose, ServerPlayerEntity player) {
        return getChatMessageByKey(player, switch (pose) {
            case SITTING -> "sit_state_error";
            default -> "pose_state_error";
        });
    }

    // pose disabled
    public static Text poseDisabledError(Pose pose, ServerPlayerEntity player) {
        return getChatMessageByKey(player, switch (pose) {
            case SITTING -> "sit_disabled";
            default -> "pose_disabled";
        });
    }

    public static void sendByException(ServerPlayerEntity player, Pose pose, PoseException e) {
        if(e instanceof MidairException) {
            player.sendMessage(getMidairError(pose, player));
        } else if(e instanceof SpectatorException) {
            player.sendMessage(getSpectatorError(pose, player));
        } else if(e instanceof StateException) {
            player.sendMessage(getStateError(pose, player));
        } else if(e instanceof PoseDisabled) {
            player.sendMessage(poseDisabledError(pose, player));
        }
    }
}
