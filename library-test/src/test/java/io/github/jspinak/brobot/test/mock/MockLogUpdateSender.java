package io.github.jspinak.brobot.test.mock;

import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.logging.LogUpdateSender;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class MockLogUpdateSender implements LogUpdateSender {

    @Override
    public void sendLogUpdate(List<LogEntry> logEntries) {
        System.out.println("Mock sendLogUpdate: Sending " + logEntries.size() + " log entries");
        for (LogEntry entry : logEntries) {
            System.out.println("  - Log Entry: SessionId=" + entry.getSessionId() +
                    ", Type=" + entry.getType() +
                    ", Description=" + entry.getDescription());
        }
    }
}