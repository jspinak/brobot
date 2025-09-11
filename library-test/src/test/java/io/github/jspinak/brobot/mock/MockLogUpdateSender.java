package io.github.jspinak.brobot.mock;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.tools.logging.LogEventDispatcher;
import io.github.jspinak.brobot.tools.logging.model.LogData;

@Component
@Primary
public class MockLogUpdateSender implements LogEventDispatcher {

    @Override
    public void sendLogUpdate(List<LogData> logEntries) {
        System.out.println("Mock sendLogUpdate: Sending " + logEntries.size() + " log entries");
        for (LogData entry : logEntries) {
            System.out.println(
                    "  - Log Entry: SessionId="
                            + entry.getSessionId()
                            + ", Type="
                            + entry.getType()
                            + ", Description="
                            + entry.getDescription());
        }
    }
}
