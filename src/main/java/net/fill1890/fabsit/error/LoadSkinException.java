package net.fill1890.fabsit.error;

/**
 * Error fetching a skin from Mojang
 */
public class LoadSkinException extends Exception {
    /**
     * URL malformed - error in UUID
     */
    public static class UrlException extends LoadSkinException {}

    /**
     * IOException reading skin data
     */
    public static class SkinIOException extends LoadSkinException {}

    /**
     * Server provided no response or an empty reponse
     */
    public static class NoResponseException extends LoadSkinException {}

    /**
     * Server response was an error
     */
    public static class ErrorResponseException extends LoadSkinException {}

    /**
     * Response was malformed or couldn't be read correctly
     */
    public static class InvalidResponseException extends LoadSkinException {}
}
