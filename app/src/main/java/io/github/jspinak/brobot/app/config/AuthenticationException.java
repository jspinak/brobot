package io.github.jspinak.brobot.app.config;

// Custom exception for authentication failures
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
