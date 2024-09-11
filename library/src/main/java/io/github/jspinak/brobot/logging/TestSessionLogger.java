package io.github.jspinak.brobot.logging;

/**
 * This interface manages the logging session.
 */
public interface TestSessionLogger {
    String startSession(String applicationUnderTest);
    void endSession(String sessionId);
    void setCurrentState(String sessionId, String stateName, String stateDescription);
}


