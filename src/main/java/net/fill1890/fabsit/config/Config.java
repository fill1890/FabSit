package net.fill1890.fabsit.config;

public class Config {
    public String locale = "en_us";

    public boolean allow_posing_underwater = false;
    public boolean allow_posing_midair = false;

    public Poses allow_poses = new Poses();

    public static class Poses {
        public boolean sit = true;
        public boolean lay = true;
        public boolean spin = true;
    }
}
