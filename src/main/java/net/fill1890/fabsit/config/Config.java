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
    public final String locale = "en_us";

    public final boolean allow_posing_underwater = false;
    public final boolean allow_posing_midair = false;

    // permitted poses
    // if LuckPerms is installed it may be better to use it for more specific permissions
    public final Poses allow_poses = new Poses();

    public static class Poses {
        public final boolean sit = true;
        public final boolean lay = true;
        public final boolean spin = true;
    }
}
