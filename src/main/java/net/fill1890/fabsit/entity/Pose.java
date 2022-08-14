package net.fill1890.fabsit.entity;

/*
Possible poses
 */
public enum Pose {
    SITTING("sitting"),
    LAYING("laying"),
    SPINNING("spinning");

    public final String pose;

    Pose(String pose) {
        this.pose = pose;
    }

    @Override
    public String toString() {
        return this.pose;
    }
}
