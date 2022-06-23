package net.fill1890.fabsit.error;

public class PoseException extends Exception {
    // trying to pose in midair
    public static class MidairException extends PoseException {}
    // trying to pose as a spectator
    public static class SpectatorException extends PoseException {}
    // trying to pose when underwater/flying/etc
    public static class StateException extends PoseException {}
    // pose disabled
    public static class PoseDisabled extends PoseException {}
    // block already occupied
    public static class BlockOccupied extends PoseException {}
}
