package io.github.jspinak.brobot.report.log;

import io.github.jspinak.brobot.report.log.model.LogData;

import java.util.List;

public interface LogUpdateSender {
    default void sendLogUpdate(List<LogData> logEntries) {
        // No-op implementation
    }
}
