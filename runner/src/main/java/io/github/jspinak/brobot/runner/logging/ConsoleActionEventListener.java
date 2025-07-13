package io.github.jspinak.brobot.runner.logging;

import io.github.jspinak.brobot.runner.events.LogEntryEvent;
import io.github.jspinak.brobot.tools.logging.console.ConsoleActionReporter;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener that bridges the runner's event system with the console action reporter.
 * Listens for LogEntryEvent instances and forwards them to the console reporter
 * for real-time console output.
 * 
 * <p>This component enables console output in the desktop runner application,
 * providing immediate feedback about action execution.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsoleActionEventListener {
    
    private final ConsoleActionReporter consoleReporter;
    
    /**
     * Handles log entry events by forwarding them to the console reporter.
     * 
     * @param event The log entry event
     */
    @EventListener
    public void handleLogEntryEvent(LogEntryEvent event) {
        try {
            LogData logData = event.getLogData();
            if (logData != null) {
                consoleReporter.reportLogEntry(logData);
            }
        } catch (Exception e) {
            log.warn("Error reporting log entry to console: {}", e.getMessage());
        }
    }
}