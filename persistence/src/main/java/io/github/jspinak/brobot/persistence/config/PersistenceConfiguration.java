package io.github.jspinak.brobot.persistence.config;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for persistence module.
 * Supports different persistence types and their settings.
 */
@Data
@NoArgsConstructor
public class PersistenceConfiguration {
    
    /**
     * Type of persistence backend to use.
     */
    public enum PersistenceType {
        FILE,       // File-based persistence (JSON/CSV)
        DATABASE,   // Database persistence (JPA)
        MEMORY,     // In-memory persistence (for testing)
        CUSTOM      // Custom implementation
    }
    
    /**
     * File format for file-based persistence.
     */
    public enum FileFormat {
        JSON,
        CSV,
        XML
    }
    
    private PersistenceType type = PersistenceType.FILE;
    private String name = "Default";
    
    // File-based persistence settings
    private FileSettings file = new FileSettings();
    
    // Database persistence settings
    private DatabaseSettings database = new DatabaseSettings();
    
    // Memory persistence settings
    private MemorySettings memory = new MemorySettings();
    
    // Performance settings
    private PerformanceSettings performance = new PerformanceSettings();
    
    @Data
    @NoArgsConstructor
    public static class FileSettings {
        private String basePath = "./brobot-history";
        private FileFormat format = FileFormat.JSON;
        private boolean compressExports = false;
        private boolean prettyPrint = true;
        private int maxFileSizeMb = 100;
        private boolean autoRotate = true;
        private String encoding = "UTF-8";
    }
    
    @Data
    @NoArgsConstructor
    public static class DatabaseSettings {
        private String url = "jdbc:h2:file:./data/brobot-persistence";
        private String username = "sa";
        private String password = "";
        private String driverClassName = "org.h2.Driver";
        private boolean autoCreateTables = true;
        private int connectionPoolSize = 5;
        private int batchSize = 100;
    }
    
    @Data
    @NoArgsConstructor
    public static class MemorySettings {
        private int maxSessions = 10;
        private int maxRecordsPerSession = 1000;
        private boolean persistOnShutdown = false;
        private String shutdownExportPath = "./brobot-history/shutdown-export";
    }
    
    @Data
    @NoArgsConstructor
    public static class PerformanceSettings {
        private boolean asyncRecording = true;
        private int bufferSize = 100;
        private int flushIntervalSeconds = 60;
        private int threadPoolSize = 3;
        private int queueCapacity = 1000;
        private boolean batchOperations = true;
    }
    
    /**
     * Create default configuration for file-based persistence.
     */
    public static PersistenceConfiguration fileDefault() {
        return new PersistenceConfiguration();
    }
    
    /**
     * Create default configuration for database persistence.
     */
    public static PersistenceConfiguration databaseDefault() {
        PersistenceConfiguration config = new PersistenceConfiguration();
        config.setType(PersistenceType.DATABASE);
        return config;
    }
    
    /**
     * Create default configuration for in-memory persistence.
     */
    public static PersistenceConfiguration memoryDefault() {
        PersistenceConfiguration config = new PersistenceConfiguration();
        config.setType(PersistenceType.MEMORY);
        config.getPerformance().setAsyncRecording(false); // Sync for memory
        return config;
    }
}