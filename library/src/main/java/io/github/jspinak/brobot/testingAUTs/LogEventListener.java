package io.github.jspinak.brobot.testingAUTs;

import org.springframework.stereotype.Component;

@Component
public interface LogEventListener {

    void handleLogEvent(ActionLog event);
}
