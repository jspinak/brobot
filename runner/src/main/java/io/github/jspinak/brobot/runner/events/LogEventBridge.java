package io.github.jspinak.brobot.runner.events;

import lombok.Data;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Captures log messages from SLF4J/Logback and converts them to events.
 * This appender attaches to the logging system and forwards messages to the event bus.
 */
@Component
@Data
public class LogEventBridge {
    private final EventBus eventBus;
    private LoggingAppender appender;

    public LogEventBridge(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void initialize() {
        // Create and attach appender
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        appender = new LoggingAppender();
        appender.setContext(loggerContext);
        appender.start();

        // Attach to root logger to capture all logs
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(appender);
    }

    @PreDestroy
    public void cleanup() {
        if (appender != null) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.detachAppender(appender);
            appender.stop();
        }
    }

    /**
     * Custom Logback appender that converts log events to BrobotEvents
     */
    private class LoggingAppender extends AppenderBase<ILoggingEvent> {
        @Override
        protected void append(ILoggingEvent event) {
            // Convert Logback level to our log level
            LogEvent.LogLevel level = convertLevel(event.getLevel());

            // Extract exception if present
            Exception exception = null;
            if (event.getThrowableProxy() != null && event.getThrowableProxy() instanceof ThrowableProxy) {
                ThrowableProxy throwableProxy = (ThrowableProxy) event.getThrowableProxy();
                exception = (Exception) throwableProxy.getThrowable();
            }

            // Get the logger name as category
            String category = event.getLoggerName();

            // Create and publish log event
            LogEvent logEvent;
            switch (level) {
                case DEBUG:
                    logEvent = LogEvent.debug(this, event.getFormattedMessage(), category);
                    break;
                case INFO:
                    logEvent = LogEvent.info(this, event.getFormattedMessage(), category);
                    break;
                case WARNING:
                    logEvent = LogEvent.warning(this, event.getFormattedMessage(), category);
                    break;
                case ERROR:
                case CRITICAL:
                    logEvent = LogEvent.error(this, event.getFormattedMessage(), category, exception);
                    break;
                default:
                    logEvent = LogEvent.info(this, event.getFormattedMessage(), category);
            }

            eventBus.publish(logEvent);

            // For ERROR and above, also publish an error event
            if (level == LogEvent.LogLevel.ERROR || level == LogEvent.LogLevel.CRITICAL) {
                ErrorEvent.ErrorSeverity severity = level == LogEvent.LogLevel.CRITICAL ?
                        ErrorEvent.ErrorSeverity.HIGH : ErrorEvent.ErrorSeverity.MEDIUM;

                ErrorEvent errorEvent = new ErrorEvent(
                        this,
                        event.getFormattedMessage(),
                        exception,
                        severity,
                        category
                );

                eventBus.publish(errorEvent);
            }
        }

        private LogEvent.LogLevel convertLevel(Level level) {
            if (level.equals(Level.TRACE) || level.equals(Level.DEBUG)) {
                return LogEvent.LogLevel.DEBUG;
            } else if (level.equals(Level.INFO)) {
                return LogEvent.LogLevel.INFO;
            } else if (level.equals(Level.WARN)) {
                return LogEvent.LogLevel.WARNING;
            } else if (level.equals(Level.ERROR)) {
                return LogEvent.LogLevel.ERROR;
            } else if (level.equals(Level.OFF)) {
                return LogEvent.LogLevel.CRITICAL;
            } else {
                return LogEvent.LogLevel.INFO;
            }
        }
    }
}