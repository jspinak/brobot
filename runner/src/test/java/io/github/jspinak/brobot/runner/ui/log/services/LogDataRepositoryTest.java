package io.github.jspinak.brobot.runner.ui.log.services;

import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LogDataRepository.
 */
class LogDataRepositoryTest {
    
    private LogDataRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new LogDataRepository();
    }
    
    @Test
    @DisplayName("Should add log entry successfully")
    void testAddLogEntry() {
        // Given
        LogEntry entry = createTestEntry("1", LogEntry.LogLevel.INFO);
        
        // When
        repository.addLogEntry(entry);
        
        // Then
        assertEquals(1, repository.size());
        Optional<LogEntry> retrieved = repository.getLogEntry("1");
        assertTrue(retrieved.isPresent());
        assertEquals(entry, retrieved.get());
    }
    
    @Test
    @DisplayName("Should maintain max entries limit")
    void testMaxEntriesLimit() {
        // Given
        repository.setMaxEntries(5);
        
        // When
        for (int i = 0; i < 10; i++) {
            repository.addLogEntry(createTestEntry(String.valueOf(i), LogEntry.LogLevel.INFO));
        }
        
        // Then
        assertEquals(5, repository.size());
        // Should keep newest entries (5-9)
        assertTrue(repository.getLogEntry("9").isPresent());
        assertFalse(repository.getLogEntry("0").isPresent());
    }
    
    @Test
    @DisplayName("Should query logs by level")
    void testQueryByLevel() {
        // Given
        repository.addLogEntry(createTestEntry("1", LogEntry.LogLevel.ERROR));
        repository.addLogEntry(createTestEntry("2", LogEntry.LogLevel.INFO));
        repository.addLogEntry(createTestEntry("3", LogEntry.LogLevel.ERROR));
        
        // When
        LogDataRepository.LogQuery query = LogDataRepository.LogQuery.builder()
            .levels(LogEntry.LogLevel.ERROR)
            .build();
        List<LogEntry> results = repository.queryLogs(query);
        
        // Then
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(e -> e.getLevel() == LogEntry.LogLevel.ERROR));
    }
    
    @Test
    @DisplayName("Should query logs by type")
    void testQueryByType() {
        // Given
        repository.addLogEntry(createTestEntry("1", LogEntry.LogLevel.INFO, "ACTION"));
        repository.addLogEntry(createTestEntry("2", LogEntry.LogLevel.INFO, "STATE"));
        repository.addLogEntry(createTestEntry("3", LogEntry.LogLevel.INFO, "ACTION"));
        
        // When
        LogDataRepository.LogQuery query = LogDataRepository.LogQuery.builder()
            .types("ACTION")
            .build();
        List<LogEntry> results = repository.queryLogs(query);
        
        // Then
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(e -> "ACTION".equals(e.getType())));
    }
    
    @Test
    @DisplayName("Should query logs by time range")
    void testQueryByTimeRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        repository.addLogEntry(createTestEntryWithTime("1", now.minusHours(2)));
        repository.addLogEntry(createTestEntryWithTime("2", now.minusHours(1)));
        repository.addLogEntry(createTestEntryWithTime("3", now));
        
        // When
        LogDataRepository.LogQuery query = LogDataRepository.LogQuery.builder()
            .startTime(now.minusHours(1).minusMinutes(30))
            .endTime(now.minusMinutes(30))
            .build();
        List<LogEntry> results = repository.queryLogs(query);
        
        // Then
        assertEquals(1, results.size());
        assertEquals("2", results.get(0).getId());
    }
    
    @Test
    @DisplayName("Should query with search text")
    void testQueryWithSearchText() {
        // Given
        repository.addLogEntry(createTestEntry("1", "Contains keyword here"));
        repository.addLogEntry(createTestEntry("2", "Different message"));
        repository.addLogEntry(createTestEntry("3", "Also has keyword"));
        
        // When
        LogDataRepository.LogQuery query = LogDataRepository.LogQuery.builder()
            .searchText("keyword")
            .build();
        List<LogEntry> results = repository.queryLogs(query);
        
        // Then
        assertEquals(2, results.size());
    }
    
    @Test
    @DisplayName("Should limit query results")
    void testQueryLimit() {
        // Given
        for (int i = 0; i < 10; i++) {
            repository.addLogEntry(createTestEntry(String.valueOf(i), LogEntry.LogLevel.INFO));
        }
        
        // When
        LogDataRepository.LogQuery query = LogDataRepository.LogQuery.builder()
            .limit(3)
            .build();
        List<LogEntry> results = repository.queryLogs(query);
        
        // Then
        assertEquals(3, results.size());
    }
    
    @Test
    @DisplayName("Should clear all logs")
    void testClearLogs() {
        // Given
        repository.addLogEntry(createTestEntry("1", LogEntry.LogLevel.INFO));
        repository.addLogEntry(createTestEntry("2", LogEntry.LogLevel.ERROR));
        assertEquals(2, repository.size());
        
        // When
        repository.clearLogs();
        
        // Then
        assertEquals(0, repository.size());
        assertFalse(repository.getLogEntry("1").isPresent());
    }
    
    @Test
    @DisplayName("Should get repository statistics")
    void testGetStatistics() {
        // Given
        repository.addLogEntry(createTestEntry("1", LogEntry.LogLevel.ERROR, "ACTION"));
        repository.addLogEntry(createTestEntry("2", LogEntry.LogLevel.INFO, "STATE"));
        repository.addLogEntry(createTestEntry("3", LogEntry.LogLevel.ERROR, "ACTION"));
        
        // When
        LogDataRepository.RepositoryStats stats = repository.getStatistics();
        
        // Then
        assertEquals(3, stats.getTotalEntries());
        assertEquals(2, stats.getEntriesByLevel().get(LogEntry.LogLevel.ERROR));
        assertEquals(1, stats.getEntriesByLevel().get(LogEntry.LogLevel.INFO));
        assertEquals(2, stats.getEntriesByType().get("ACTION"));
        assertEquals(1, stats.getEntriesByType().get("STATE"));
    }
    
    @Test
    @DisplayName("Should return observable list")
    void testObservableList() {
        // Given
        ObservableList<LogEntry> entries = repository.getLogEntries();
        assertTrue(entries.isEmpty());
        
        // When
        repository.addLogEntry(createTestEntry("1", LogEntry.LogLevel.INFO));
        
        // Then
        assertEquals(1, entries.size());
        assertEquals("1", entries.get(0).getId());
    }
    
    @Test
    @DisplayName("Should handle concurrent access")
    void testConcurrentAccess() throws InterruptedException {
        // Given
        int threadCount = 10;
        int entriesPerThread = 100;
        
        // When
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < entriesPerThread; j++) {
                    repository.addLogEntry(createTestEntry(
                        threadId + "-" + j, 
                        LogEntry.LogLevel.INFO
                    ));
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then
        assertEquals(threadCount * entriesPerThread, repository.size());
    }
    
    private LogEntry createTestEntry(String id, LogEntry.LogLevel level) {
        return createTestEntry(id, level, "TEST");
    }
    
    private LogEntry createTestEntry(String id, LogEntry.LogLevel level, String type) {
        return LogEntry.builder()
            .id(id)
            .timestamp(LocalDateTime.now())
            .level(level)
            .type(type)
            .source("TestSource")
            .message("Test message " + id)
            .build();
    }
    
    private LogEntry createTestEntry(String id, String message) {
        return LogEntry.builder()
            .id(id)
            .timestamp(LocalDateTime.now())
            .level(LogEntry.LogLevel.INFO)
            .type("TEST")
            .source("TestSource")
            .message(message)
            .build();
    }
    
    private LogEntry createTestEntryWithTime(String id, LocalDateTime time) {
        return LogEntry.builder()
            .id(id)
            .timestamp(time)
            .level(LogEntry.LogLevel.INFO)
            .type("TEST")
            .source("TestSource")
            .message("Test message " + id)
            .build();
    }
}