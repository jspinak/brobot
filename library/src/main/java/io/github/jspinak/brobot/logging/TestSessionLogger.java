package io.github.jspinak.brobot.logging;

/**
 * This interface manages the logging session.
 */
public interface TestSessionLogger {
    default String startSession(String applicationUnderTest) {
        // No-op implementation, returning null
        return null;
    }

    default void endSession(String sessionId) {
        // No-op implementation
    }

    default void setCurrentState(String sessionId, String stateName, String stateDescription) {
        // No-op implementation
    }
}


