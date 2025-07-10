package io.github.jspinak.brobot.runner.ui.log.services;

import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Repository service for managing log data.
 * Provides thread-safe storage and querying of log entries.
 */
@Slf4j
@Service
public class LogDataRepository {
    
    private static final int DEFAULT_MAX_ENTRIES = 10000;
    
    private final ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();
    private final Map<String, LogEntry> entryIndex = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> typeIndex = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> sourceIndex = new ConcurrentHashMap<>();
    private final Map<LogEntry.LogLevel, Set<String>> levelIndex = new ConcurrentHashMap<>();
    
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private int maxEntries = DEFAULT_MAX_ENTRIES;
    
    /**
     * Query criteria for searching logs.
     */
    public static class LogQuery {
        private Predicate<LogEntry> predicate;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Set<LogEntry.LogLevel> levels;
        private Set<String> types;
        private Set<String> sources;
        private String searchText;
        private int limit = Integer.MAX_VALUE;
        private SortOrder sortOrder = SortOrder.DESCENDING;
        
        public enum SortOrder {
            ASCENDING,
            DESCENDING
        }
        
        public static LogQueryBuilder builder() {
            return new LogQueryBuilder();
        }
        
        public static class LogQueryBuilder {
            private LogQuery query = new LogQuery();
            
            public LogQueryBuilder predicate(Predicate<LogEntry> predicate) {
                query.predicate = predicate;
                return this;
            }
            
            public LogQueryBuilder startTime(LocalDateTime startTime) {
                query.startTime = startTime;
                return this;
            }
            
            public LogQueryBuilder endTime(LocalDateTime endTime) {
                query.endTime = endTime;
                return this;
            }
            
            public LogQueryBuilder levels(LogEntry.LogLevel... levels) {
                query.levels = new HashSet<>(Arrays.asList(levels));
                return this;
            }
            
            public LogQueryBuilder types(String... types) {
                query.types = new HashSet<>(Arrays.asList(types));
                return this;
            }
            
            public LogQueryBuilder sources(String... sources) {
                query.sources = new HashSet<>(Arrays.asList(sources));
                return this;
            }
            
            public LogQueryBuilder searchText(String searchText) {
                query.searchText = searchText;
                return this;
            }
            
            public LogQueryBuilder limit(int limit) {
                query.limit = limit;
                return this;
            }
            
            public LogQueryBuilder sortOrder(SortOrder order) {
                query.sortOrder = order;
                return this;
            }
            
            public LogQuery build() {
                return query;
            }
        }
    }
    
    /**
     * Repository statistics.
     */
    public static class RepositoryStats {
        private final int totalEntries;
        private final Map<LogEntry.LogLevel, Integer> entriesByLevel;
        private final Map<String, Integer> entriesByType;
        private final LocalDateTime oldestEntry;
        private final LocalDateTime newestEntry;
        private final long totalSize;
        
        public RepositoryStats(int totalEntries, Map<LogEntry.LogLevel, Integer> entriesByLevel,
                             Map<String, Integer> entriesByType, LocalDateTime oldestEntry,
                             LocalDateTime newestEntry, long totalSize) {
            this.totalEntries = totalEntries;
            this.entriesByLevel = entriesByLevel;
            this.entriesByType = entriesByType;
            this.oldestEntry = oldestEntry;
            this.newestEntry = newestEntry;
            this.totalSize = totalSize;
        }
        
        // Getters
        public int getTotalEntries() { return totalEntries; }
        public Map<LogEntry.LogLevel, Integer> getEntriesByLevel() { return entriesByLevel; }
        public Map<String, Integer> getEntriesByType() { return entriesByType; }
        public LocalDateTime getOldestEntry() { return oldestEntry; }
        public LocalDateTime getNewestEntry() { return newestEntry; }
        public long getTotalSize() { return totalSize; }
    }
    
    public LogDataRepository() {
        // Initialize level index
        for (LogEntry.LogLevel level : LogEntry.LogLevel.values()) {
            levelIndex.put(level, ConcurrentHashMap.newKeySet());
        }
    }
    
    /**
     * Gets the observable list of log entries.
     */
    public ObservableList<LogEntry> getLogEntries() {
        return logEntries;
    }
    
    /**
     * Adds a new log entry.
     */
    public void addLogEntry(LogEntry entry) {
        if (entry == null) {
            return;
        }
        
        lock.writeLock().lock();
        try {
            // Add to main list (at beginning for newest first)
            logEntries.add(0, entry);
            
            // Update indices
            entryIndex.put(entry.getId(), entry);
            typeIndex.computeIfAbsent(entry.getType(), k -> ConcurrentHashMap.newKeySet())
                     .add(entry.getId());
            sourceIndex.computeIfAbsent(entry.getSource(), k -> ConcurrentHashMap.newKeySet())
                       .add(entry.getId());
            levelIndex.get(entry.getLevel()).add(entry.getId());
            
            // Remove oldest if exceeding max
            if (logEntries.size() > maxEntries) {
                LogEntry removed = logEntries.remove(logEntries.size() - 1);
                removeFromIndices(removed);
            }
            
        } finally {
            lock.writeLock().unlock();
        }
        
        log.trace("Added log entry: {}", entry.getId());
    }
    
    /**
     * Adds multiple log entries.
     */
    public void addLogEntries(Collection<LogEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        
        lock.writeLock().lock();
        try {
            for (LogEntry entry : entries) {
                if (entry != null) {
                    // Add without triggering individual events
                    logEntries.add(0, entry);
                    
                    // Update indices
                    entryIndex.put(entry.getId(), entry);
                    typeIndex.computeIfAbsent(entry.getType(), k -> ConcurrentHashMap.newKeySet())
                             .add(entry.getId());
                    sourceIndex.computeIfAbsent(entry.getSource(), k -> ConcurrentHashMap.newKeySet())
                               .add(entry.getId());
                    levelIndex.get(entry.getLevel()).add(entry.getId());
                }
            }
            
            // Trim to max size
            while (logEntries.size() > maxEntries) {
                LogEntry removed = logEntries.remove(logEntries.size() - 1);
                removeFromIndices(removed);
            }
            
        } finally {
            lock.writeLock().unlock();
        }
        
        log.debug("Added {} log entries", entries.size());
    }
    
    /**
     * Removes entry from indices.
     */
    private void removeFromIndices(LogEntry entry) {
        entryIndex.remove(entry.getId());
        
        Set<String> typeSet = typeIndex.get(entry.getType());
        if (typeSet != null) {
            typeSet.remove(entry.getId());
        }
        
        Set<String> sourceSet = sourceIndex.get(entry.getSource());
        if (sourceSet != null) {
            sourceSet.remove(entry.getId());
        }
        
        levelIndex.get(entry.getLevel()).remove(entry.getId());
    }
    
    /**
     * Clears all logs.
     */
    public void clearLogs() {
        lock.writeLock().lock();
        try {
            logEntries.clear();
            entryIndex.clear();
            typeIndex.clear();
            sourceIndex.clear();
            levelIndex.values().forEach(Set::clear);
        } finally {
            lock.writeLock().unlock();
        }
        
        log.info("Cleared all log entries");
    }
    
    /**
     * Queries logs based on criteria.
     */
    public List<LogEntry> queryLogs(LogQuery query) {
        lock.readLock().lock();
        try {
            List<LogEntry> results = new ArrayList<>();
            
            // Start with all entries or use index optimization
            Collection<LogEntry> candidates = getQueryCandidates(query);
            
            // Apply filters
            for (LogEntry entry : candidates) {
                if (matchesQuery(entry, query)) {
                    results.add(entry);
                    if (results.size() >= query.limit) {
                        break;
                    }
                }
            }
            
            // Sort results
            if (query.sortOrder == LogQuery.SortOrder.ASCENDING) {
                results.sort(Comparator.comparing(LogEntry::getTimestamp));
            } else {
                results.sort(Comparator.comparing(LogEntry::getTimestamp).reversed());
            }
            
            return results;
            
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Gets query candidates using index optimization.
     */
    private Collection<LogEntry> getQueryCandidates(LogQuery query) {
        // Try to use indices for optimization
        Set<String> candidateIds = null;
        
        // Level index
        if (query.levels != null && !query.levels.isEmpty()) {
            Set<String> levelIds = new HashSet<>();
            for (LogEntry.LogLevel level : query.levels) {
                levelIds.addAll(levelIndex.get(level));
            }
            candidateIds = levelIds;
        }
        
        // Type index
        if (query.types != null && !query.types.isEmpty()) {
            Set<String> typeIds = new HashSet<>();
            for (String type : query.types) {
                Set<String> ids = typeIndex.get(type);
                if (ids != null) {
                    typeIds.addAll(ids);
                }
            }
            
            if (candidateIds == null) {
                candidateIds = typeIds;
            } else {
                candidateIds.retainAll(typeIds); // Intersection
            }
        }
        
        // Source index
        if (query.sources != null && !query.sources.isEmpty()) {
            Set<String> sourceIds = new HashSet<>();
            for (String source : query.sources) {
                Set<String> ids = sourceIndex.get(source);
                if (ids != null) {
                    sourceIds.addAll(ids);
                }
            }
            
            if (candidateIds == null) {
                candidateIds = sourceIds;
            } else {
                candidateIds.retainAll(sourceIds); // Intersection
            }
        }
        
        // Convert IDs to entries
        if (candidateIds != null) {
            return candidateIds.stream()
                .map(entryIndex::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
        
        // No index optimization possible, use all entries
        return new ArrayList<>(logEntries);
    }
    
    /**
     * Checks if entry matches query criteria.
     */
    private boolean matchesQuery(LogEntry entry, LogQuery query) {
        // Custom predicate
        if (query.predicate != null && !query.predicate.test(entry)) {
            return false;
        }
        
        // Time range
        if (query.startTime != null && entry.getTimestamp().isBefore(query.startTime)) {
            return false;
        }
        if (query.endTime != null && entry.getTimestamp().isAfter(query.endTime)) {
            return false;
        }
        
        // Search text
        if (query.searchText != null && !query.searchText.isEmpty()) {
            String searchLower = query.searchText.toLowerCase();
            boolean matches = entry.getMessage().toLowerCase().contains(searchLower) ||
                            entry.getSource().toLowerCase().contains(searchLower) ||
                            (entry.getDetails() != null && entry.getDetails().toLowerCase().contains(searchLower));
            if (!matches) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Gets a specific log entry by ID.
     */
    public Optional<LogEntry> getLogEntry(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(entryIndex.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Sets the maximum number of entries.
     */
    public void setMaxEntries(int max) {
        if (max < 1) {
            throw new IllegalArgumentException("Max entries must be positive");
        }
        
        lock.writeLock().lock();
        try {
            this.maxEntries = max;
            
            // Trim if necessary
            while (logEntries.size() > maxEntries) {
                LogEntry removed = logEntries.remove(logEntries.size() - 1);
                removeFromIndices(removed);
            }
        } finally {
            lock.writeLock().unlock();
        }
        
        log.info("Set max entries to: {}", max);
    }
    
    /**
     * Gets repository statistics.
     */
    public RepositoryStats getStatistics() {
        lock.readLock().lock();
        try {
            Map<LogEntry.LogLevel, Integer> levelCounts = new HashMap<>();
            Map<String, Integer> typeCounts = new HashMap<>();
            LocalDateTime oldest = null;
            LocalDateTime newest = null;
            
            for (LogEntry entry : logEntries) {
                // Level counts
                levelCounts.merge(entry.getLevel(), 1, Integer::sum);
                
                // Type counts
                typeCounts.merge(entry.getType(), 1, Integer::sum);
                
                // Time range
                if (oldest == null || entry.getTimestamp().isBefore(oldest)) {
                    oldest = entry.getTimestamp();
                }
                if (newest == null || entry.getTimestamp().isAfter(newest)) {
                    newest = entry.getTimestamp();
                }
            }
            
            // Estimate size (rough)
            long totalSize = logEntries.size() * 500L; // Assume 500 bytes per entry
            
            return new RepositoryStats(
                logEntries.size(),
                levelCounts,
                typeCounts,
                oldest,
                newest,
                totalSize
            );
            
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Gets all unique types.
     */
    public Set<String> getUniqueTypes() {
        lock.readLock().lock();
        try {
            return new HashSet<>(typeIndex.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Gets all unique sources.
     */
    public Set<String> getUniqueSources() {
        lock.readLock().lock();
        try {
            return new HashSet<>(sourceIndex.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Gets the current size.
     */
    public int size() {
        lock.readLock().lock();
        try {
            return logEntries.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}