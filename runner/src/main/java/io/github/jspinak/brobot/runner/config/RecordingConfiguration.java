package io.github.jspinak.brobot.runner.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/** Configuration properties for action recording functionality. */
@Configuration
@ConfigurationProperties(prefix = "brobot.runner.recording")
@Data
public class RecordingConfiguration {

    /** Whether recording is enabled by default on startup */
    private boolean enabled = false;

    /** Auto-save interval in seconds (0 = disabled) */
    private int autoSaveInterval = 60;

    /** Maximum records to keep in memory before flushing */
    private int bufferSize = 100;

    /** Maximum records per session (0 = unlimited) */
    private int maxRecordsPerSession = 10000;

    /** Maximum number of sessions to keep (0 = unlimited) */
    private int maxSessions = 100;

    /** Default export format (JSON, CSV, JSON_COMPRESSED, CSV_COMPRESSED) */
    private String exportFormat = "JSON";

    /** Whether to compress exports by default */
    private boolean compressExports = false;

    /** Directory for auto-exports (null = disabled) */
    private String autoExportPath = null;

    /** Whether to store screenshots with matches */
    private boolean storeScreenshots = false;

    /** Maximum screenshot size in KB (0 = unlimited) */
    private int maxScreenshotSize = 500;

    /** Database configuration */
    private DatabaseConfig database = new DatabaseConfig();

    /** Performance configuration */
    private PerformanceConfig performance = new PerformanceConfig();

    /** Cleanup configuration */
    private CleanupConfig cleanup = new CleanupConfig();

    @Data
    public static class DatabaseConfig {
        /** Batch insert size for better performance */
        private int batchSize = 100;

        /** Whether to use async recording */
        private boolean asyncRecording = true;

        /** Connection pool size for recording operations */
        private int poolSize = 5;
    }

    @Data
    public static class PerformanceConfig {
        /** Maximum memory buffer for records (in MB) */
        private int maxMemoryBuffer = 50;

        /** Whether to flush on memory pressure */
        private boolean flushOnMemoryPressure = true;

        /** Thread pool size for async operations */
        private int threadPoolSize = 3;

        /** Queue capacity for async operations */
        private int queueCapacity = 1000;
    }

    @Data
    public static class CleanupConfig {
        /** How long to keep sessions (in days, 0 = forever) */
        private int retentionDays = 30;

        /** Whether cleanup is enabled */
        private boolean enabled = true;

        /** Cleanup schedule (cron expression) */
        private String schedule = "0 0 2 * * ?"; // 2 AM daily

        /** Whether to auto-archive old sessions */
        private boolean autoArchive = false;

        /** Archive path for old sessions */
        private String archivePath = null;
    }
}
