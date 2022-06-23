package net.fill1890.fabsit.config;

/**
 * Config structure
 *
 * Written and read using GSON
 *
 * Configuration for FabSit
 */
public class Config {
    // server-side locale for translation if clients don't have the mod
    public String locale = "en_us";

    public boolean allow_posing_underwater = false;
    public boolean allow_posing_midair = false;

    // centre players on the blocks they're posing on
    public boolean centre_on_blocks = false;

    // permitted poses
    // if LuckPerms is installed it may be better to use it for more specific permissions
    public Poses allow_poses = new Poses();

    public Messages enable_messages = new Messages();

    public static class Poses {
        public boolean sit = true;
        public boolean lay = true;
        public boolean spin = true;
    }

    public static class Messages {
        public boolean action_bar = true;
        public boolean pose_errors = true;
    }
}
