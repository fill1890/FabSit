package net.fill1890.fabsit.error;

public class PoseException extends Exception {
    public static class MidairException extends PoseException {}
    public static class SpectatorException extends PoseException {}
    public static class StateException extends PoseException {}
}
