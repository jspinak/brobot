package io.github.jspinak.brobot.report.log.spi;

import io.github.jspinak.brobot.report.log.model.LogData;

/**
 * An interface for components that wish to persist log data produced by the library.
 */
@FunctionalInterface
public interface LogSink {
    void save(LogData logData);
}
