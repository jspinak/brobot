package io.github.jspinak.brobot.runner.ui.log.services;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.ui.log.models.LogEntryViewModel;
import io.github.jspinak.brobot.tools.logging.model.LogData;

import lombok.extern.slf4j.Slf4j;

/**
 * Factory service for creating and managing LogEntryViewModel instances. Provides caching and batch
 * creation capabilities.
 */
@Slf4j
@Service
public class LogEntryViewModelFactory {

    // Cache for view models to avoid recreation
    private final Map<String, LogEntryViewModel> viewModelCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 1000;

    /** Configuration for view model creation. */
    public static class ViewModelConfiguration {
        private DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        private boolean includeMilliseconds = true;
        private boolean cacheEnabled = false;
        private int maxCacheSize = MAX_CACHE_SIZE;

        public static ViewModelConfigurationBuilder builder() {
            return new ViewModelConfigurationBuilder();
        }

        public static class ViewModelConfigurationBuilder {
            private ViewModelConfiguration config = new ViewModelConfiguration();

            public ViewModelConfigurationBuilder dateFormatter(DateTimeFormatter formatter) {
                config.dateFormatter = formatter;
                return this;
            }

            public ViewModelConfigurationBuilder includeMilliseconds(boolean include) {
                config.includeMilliseconds = include;
                return this;
            }

            public ViewModelConfigurationBuilder cacheEnabled(boolean enabled) {
                config.cacheEnabled = enabled;
                return this;
            }

            public ViewModelConfigurationBuilder maxCacheSize(int size) {
                config.maxCacheSize = size;
                return this;
            }

            public ViewModelConfiguration build() {
                return config;
            }
        }
    }

    private ViewModelConfiguration configuration = new ViewModelConfiguration();

    /** Sets the configuration for view model creation. */
    public void setConfiguration(ViewModelConfiguration configuration) {
        this.configuration = configuration;
        if (!configuration.cacheEnabled) {
            clearCache();
        }
    }

    /** Creates a view model from LogData. */
    public LogEntryViewModel createFromLogData(LogData logData) {
        if (logData == null) {
            log.warn("Attempted to create view model from null LogData");
            return new LogEntryViewModel();
        }

        // Check cache if enabled
        if (configuration.cacheEnabled && logData.getTimestamp() != null) {
            String cacheKey = createCacheKey(logData);
            LogEntryViewModel cached = viewModelCache.get(cacheKey);
            if (cached != null) {
                log.trace("Returning cached view model for key: {}", cacheKey);
                return cached;
            }
        }

        // Create new view model
        LogEntryViewModel viewModel = new LogEntryViewModel(logData);

        // Cache if enabled
        if (configuration.cacheEnabled) {
            cacheViewModel(createCacheKey(logData), viewModel);
        }

        return viewModel;
    }

    /** Creates a view model from LogEvent. */
    public LogEntryViewModel createFromLogEvent(LogEvent logEvent) {
        if (logEvent == null) {
            log.warn("Attempted to create view model from null LogEvent");
            return new LogEntryViewModel();
        }

        // Check cache if enabled
        if (configuration.cacheEnabled && logEvent.getTimestamp() != null) {
            String cacheKey = createCacheKey(logEvent);
            LogEntryViewModel cached = viewModelCache.get(cacheKey);
            if (cached != null) {
                log.trace("Returning cached view model for key: {}", cacheKey);
                return cached;
            }
        }

        // Create new view model
        LogEntryViewModel viewModel = new LogEntryViewModel(logEvent);

        // Cache if enabled
        if (configuration.cacheEnabled) {
            cacheViewModel(createCacheKey(logEvent), viewModel);
        }

        return viewModel;
    }

    /** Creates view models from a batch of LogData. */
    public List<LogEntryViewModel> createBatchFromLogData(List<LogData> logDataList) {
        if (logDataList == null || logDataList.isEmpty()) {
            return new ArrayList<>();
        }

        List<LogEntryViewModel> viewModels = new ArrayList<>(logDataList.size());

        for (LogData logData : logDataList) {
            viewModels.add(createFromLogData(logData));
        }

        log.debug("Created {} view models from LogData batch", viewModels.size());
        return viewModels;
    }

    /** Creates view models from a batch of LogEvents. */
    public List<LogEntryViewModel> createBatchFromLogEvents(List<LogEvent> logEvents) {
        if (logEvents == null || logEvents.isEmpty()) {
            return new ArrayList<>();
        }

        List<LogEntryViewModel> viewModels = new ArrayList<>(logEvents.size());

        for (LogEvent logEvent : logEvents) {
            viewModels.add(createFromLogEvent(logEvent));
        }

        log.debug("Created {} view models from LogEvent batch", viewModels.size());
        return viewModels;
    }

    /** Formats detailed text for a view model with custom formatting. */
    public String formatDetailedText(LogEntryViewModel viewModel, DetailFormat format) {
        if (viewModel == null || viewModel.getRawLogData() == null) {
            return "No data available.";
        }

        switch (format) {
            case COMPACT:
                return formatCompactText(viewModel);
            case DETAILED:
                return viewModel.getDetailedText();
            case JSON:
                return formatJsonText(viewModel);
            case XML:
                return formatXmlText(viewModel);
            default:
                return viewModel.getDetailedText();
        }
    }

    /** Detail format options. */
    public enum DetailFormat {
        COMPACT,
        DETAILED,
        JSON,
        XML
    }

    /** Formats compact text representation. */
    private String formatCompactText(LogEntryViewModel viewModel) {
        return String.format(
                "[%s] [%s] %s", viewModel.getTime(), viewModel.getLevel(), viewModel.getMessage());
    }

    /** Formats JSON representation. */
    private String formatJsonText(LogEntryViewModel viewModel) {
        StringBuilder json = new StringBuilder("{\n");
        json.append("  \"time\": \"").append(viewModel.getTime()).append("\",\n");
        json.append("  \"level\": \"").append(viewModel.getLevel()).append("\",\n");
        json.append("  \"type\": \"").append(viewModel.getType()).append("\",\n");
        json.append("  \"success\": ").append(viewModel.isSuccess()).append(",\n");
        json.append("  \"message\": \"").append(escapeJson(viewModel.getMessage())).append("\"");

        LogData data = viewModel.getRawLogData();
        if (data != null) {
            if (data.getErrorMessage() != null) {
                json.append(",\n  \"error\": \"")
                        .append(escapeJson(data.getErrorMessage()))
                        .append("\"");
            }
            if (data.getCurrentStateName() != null) {
                json.append(",\n  \"state\": \"")
                        .append(escapeJson(data.getCurrentStateName()))
                        .append("\"");
            }
            if (data.getPerformance() != null && data.getPerformance().getActionDuration() > 0) {
                json.append(",\n  \"duration\": ")
                        .append(data.getPerformance().getActionDuration());
            }
        }

        json.append("\n}");
        return json.toString();
    }

    /** Formats XML representation. */
    private String formatXmlText(LogEntryViewModel viewModel) {
        StringBuilder xml = new StringBuilder("<logEntry>\n");
        xml.append("  <time>").append(viewModel.getTime()).append("</time>\n");
        xml.append("  <level>").append(viewModel.getLevel()).append("</level>\n");
        xml.append("  <type>").append(viewModel.getType()).append("</type>\n");
        xml.append("  <success>").append(viewModel.isSuccess()).append("</success>\n");
        xml.append("  <message>").append(escapeXml(viewModel.getMessage())).append("</message>\n");

        LogData data = viewModel.getRawLogData();
        if (data != null) {
            if (data.getErrorMessage() != null) {
                xml.append("  <error>")
                        .append(escapeXml(data.getErrorMessage()))
                        .append("</error>\n");
            }
            if (data.getCurrentStateName() != null) {
                xml.append("  <state>")
                        .append(escapeXml(data.getCurrentStateName()))
                        .append("</state>\n");
            }
            if (data.getPerformance() != null && data.getPerformance().getActionDuration() > 0) {
                xml.append("  <duration>")
                        .append(data.getPerformance().getActionDuration())
                        .append("</duration>\n");
            }
        }

        xml.append("</logEntry>");
        return xml.toString();
    }

    /** Creates a cache key for LogData. */
    private String createCacheKey(LogData logData) {
        return logData.getTimestamp()
                + "_"
                + (logData.getType() != null ? logData.getType() : "UNKNOWN");
    }

    /** Creates a cache key for LogEvent. */
    private String createCacheKey(LogEvent logEvent) {
        return logEvent.getTimestamp()
                + "_"
                + logEvent.getLevel()
                + "_"
                + (logEvent.getCategory() != null ? logEvent.getCategory() : "UNKNOWN");
    }

    /** Caches a view model. */
    private void cacheViewModel(String key, LogEntryViewModel viewModel) {
        // Check cache size
        if (viewModelCache.size() >= configuration.maxCacheSize) {
            // Simple eviction - remove first entry
            String firstKey = viewModelCache.keySet().iterator().next();
            viewModelCache.remove(firstKey);
            log.trace("Evicted cache entry: {}", firstKey);
        }

        viewModelCache.put(key, viewModel);
    }

    /** Clears the view model cache. */
    public void clearCache() {
        int size = viewModelCache.size();
        viewModelCache.clear();
        log.debug("Cleared view model cache ({} entries)", size);
    }

    /** Gets current cache size. */
    public int getCacheSize() {
        return viewModelCache.size();
    }

    /** Escapes JSON special characters. */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /** Escapes XML special characters. */
    private String escapeXml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
