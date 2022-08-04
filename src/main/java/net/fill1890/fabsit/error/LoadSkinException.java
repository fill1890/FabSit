package net.fill1890.fabsit.error;

public class LoadSkinException extends Exception {
    public static class UrlException extends LoadSkinException {}
    public static class SkinIOException extends LoadSkinException {}
    public static class NoResponseException extends LoadSkinException {}
    public static class ErrorResponseException extends LoadSkinException {}
    public static class InvalidResponseException extends LoadSkinException {}
}
