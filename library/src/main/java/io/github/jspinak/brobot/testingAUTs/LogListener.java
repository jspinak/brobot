package io.github.jspinak.brobot.testingAUTs;

import org.springframework.stereotype.Component;

@Component
public class LogListener {

    private static LogEventListener logEventListener;

    // the listener can be registered from the log module
    public static void registerLogEventListener(LogEventListener listener) {
        logEventListener = listener;
    }

    // Notify the registered LogEventListener
    public void logEvent(ActionLog actionLog) {
        if (logEventListener != null) {
            logEventListener.handleLogEvent(actionLog);
        }
    }
}
