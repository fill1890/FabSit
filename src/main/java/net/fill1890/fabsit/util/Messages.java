package net.fill1890.fabsit.util;

import net.fill1890.fabsit.entity.Pose;
import net.minecraft.text.Text;

// this may not be the best way of doing this kind of function
// but it works for now
public interface Messages {

    // stop posing action message
    static Text getPoseStopMessage(Pose pose) {
        return Text.translatable("action.fabsit.stop_" + pose, Text.keybind("key.sneak"));
    }

    // trying to pose in midair
    static Text getMidairError(Pose pose) {
        if(pose == Pose.SITTING) {
            return Text.translatable("chat.fabsit.sit_air_error");
        } else {
            return Text.translatable("chat.fabsit.pose_air_error");
        }
    }

    // trying to pose while a spectator
    static Text getSpectatorError(Pose pose) {
        if(pose == Pose.SITTING) {
            return Text.translatable("chat.fabsit.sit_spectator_error");
        } else {
            return Text.translatable("chat.fabsit.pose_spectator_error");
        }
    }

    // trying to pose while swimming/sleeping/flying/etc
    static Text getStateError(Pose pose) {
        if(pose == Pose.SITTING) {
            return Text.translatable("chat.fabsit.sit_state_error");
        } else {
            return Text.translatable("chat.fabsit.pose_state_error");
        }
    }

    static Text poseDisabledError(Pose pose) {
        if(pose == Pose.SITTING) {
            return Text.translatable("chat.fabsit.sit_disabled");
        } else {
            return Text.translatable("chat.fabsit.pose_disabled");
        }
    }
}
