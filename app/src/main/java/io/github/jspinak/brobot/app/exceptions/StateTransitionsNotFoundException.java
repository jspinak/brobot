package io.github.jspinak.brobot.app.exceptions;

public class StateTransitionsNotFoundException extends RuntimeException {
    public StateTransitionsNotFoundException(String message) {
        super(message);
    }
}
