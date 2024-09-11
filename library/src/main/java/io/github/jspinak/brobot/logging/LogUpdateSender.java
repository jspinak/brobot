package io.github.jspinak.brobot.logging;

import io.github.jspinak.brobot.log.entities.LogEntry;

import java.util.List;

public interface LogUpdateSender {
    void sendLogUpdate(List<LogEntry> logEntries);
}
