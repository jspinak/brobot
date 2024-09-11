package io.github.jspinak.brobot.logging;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AutomationSession {
    private static final ThreadLocal<String> currentSessionId = new ThreadLocal<>();

    public String startNewSession() {
        String newId = UUID.randomUUID().toString();
        currentSessionId.set(newId);
        return newId;
    }

    public String getCurrentSessionId() {
        return currentSessionId.get();
    }

    public void endSession() {
        currentSessionId.remove();
    }
}
