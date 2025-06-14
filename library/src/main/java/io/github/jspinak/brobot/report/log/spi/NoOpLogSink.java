package io.github.jspinak.brobot.report.log.spi;

import io.github.jspinak.brobot.report.log.model.LogData;

/**
 * A default, do-nothing implementation of LogSink.
 * This ensures the library is usable out-of-the-box without requiring
 * a specific logging implementation.
 */
public class NoOpLogSink implements LogSink {
    @Override
    public void save(LogData logData) {
        // This is a no-operation sink. It does not persist logs.
    }
}
