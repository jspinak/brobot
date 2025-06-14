package io.github.jspinak.brobot.mock;

import io.github.jspinak.brobot.report.log.model.LogData;
import io.github.jspinak.brobot.report.log.LogUpdateSender;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class MockLogUpdateSender implements LogUpdateSender {

    @Override
    public void sendLogUpdate(List<LogData> logEntries) {
        System.out.println("Mock sendLogUpdate: Sending " + logEntries.size() + " log entries");
        for (LogData entry : logEntries) {
            System.out.println("  - Log Entry: SessionId=" + entry.getSessionId() +
                    ", Type=" + entry.getType() +
                    ", Description=" + entry.getDescription());
        }
    }
}