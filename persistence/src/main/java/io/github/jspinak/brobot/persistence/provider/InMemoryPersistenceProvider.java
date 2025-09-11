package io.github.jspinak.brobot.persistence.provider;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;

import lombok.extern.slf4j.Slf4j;

/**
 * In-memory persistence provider. Stores all data in memory - useful for testing and temporary
 * sessions. Optionally can export to file on shutdown.
 */
@Slf4j
public class InMemoryPersistenceProvider extends AbstractPersistenceProvider {

    private final Map<String, InMemorySession> sessions = new ConcurrentHashMap<>();
    private final LinkedList<String> sessionOrder = new LinkedList<>();
    private final Object sessionLock = new Object();

    public InMemoryPersistenceProvider(PersistenceConfiguration configuration) {
        super(configuration);

        // Register shutdown hook if persist on shutdown is enabled
        if (configuration.getMemory().isPersistOnShutdown()) {
            Runtime.getRuntime().addShutdownHook(new Thread(this::persistOnShutdown));
        }
    }

    @Override
    protected void doStartSession(String sessionId, SessionMetadata metadata) {
        InMemorySession session = new InMemorySession();
        session.metadata = metadata;
        session.records = Collections.synchronizedList(new ArrayList<>());
        session.startTime = LocalDateTime.now();

        sessions.put(sessionId, session);

        synchronized (sessionLock) {
            sessionOrder.add(sessionId);

            // Check if we need to evict old sessions
            enforceSessionLimit();
        }

        log.debug("Started in-memory session: {} ({})", metadata.getName(), sessionId);
    }

    @Override
    protected void doStopSession(String sessionId, SessionMetadata metadata) {
        InMemorySession session = sessions.get(sessionId);

        if (session != null) {
            session.endTime = LocalDateTime.now();
            session.metadata = metadata;
            log.debug(
                    "Stopped in-memory session: {} with {} records",
                    sessionId,
                    session.records.size());
        }
    }

    @Override
    protected void doRecordAction(String sessionId, ActionRecord record, StateObject stateObject) {
        InMemorySession session = sessions.get(sessionId);

        if (session != null) {
            // Store record with state context
            RecordWithContext recordContext = new RecordWithContext();
            recordContext.record = record;
            recordContext.stateObject = stateObject;
            recordContext.timestamp = LocalDateTime.now();

            session.records.add(recordContext);

            // Check record limit
            enforceRecordLimit(session);

            log.trace(
                    "Recorded action to session {} (total: {})", sessionId, session.records.size());
        }
    }

    @Override
    protected SessionMetadata doGetSessionMetadata(String sessionId) {
        InMemorySession session = sessions.get(sessionId);
        return session != null ? session.metadata : null;
    }

    @Override
    public ActionHistory exportSession(String sessionId) {
        InMemorySession session = sessions.get(sessionId);

        if (session == null) {
            log.warn("Session not found for export: {}", sessionId);
            return null;
        }

        ActionHistory history = new ActionHistory();

        for (RecordWithContext recordContext : session.records) {
            history.addSnapshot(recordContext.record);
        }

        log.info(
                "Exported in-memory session {} with {} records", sessionId, session.records.size());
        return history;
    }

    @Override
    public String importSession(ActionHistory history, String sessionName) {
        String sessionId = UUID.randomUUID().toString();

        SessionMetadata metadata = new SessionMetadata(sessionId, sessionName, "Imported");
        metadata.setStartTime(LocalDateTime.now());
        metadata.setEndTime(LocalDateTime.now());
        metadata.setTotalActions(history.getSnapshots().size());
        metadata.setSuccessfulActions(
                (int)
                        history.getSnapshots().stream()
                                .filter(ActionRecord::isActionSuccess)
                                .count());

        InMemorySession session = new InMemorySession();
        session.metadata = metadata;
        session.records = Collections.synchronizedList(new ArrayList<>());
        session.startTime = metadata.getStartTime();
        session.endTime = metadata.getEndTime();

        // Import all records
        for (ActionRecord record : history.getSnapshots()) {
            RecordWithContext recordContext = new RecordWithContext();
            recordContext.record = record;
            recordContext.timestamp = LocalDateTime.now();
            session.records.add(recordContext);
        }

        sessions.put(sessionId, session);

        synchronized (sessionLock) {
            sessionOrder.add(sessionId);
            enforceSessionLimit();
        }

        log.info("Imported session {} with {} records", sessionName, history.getSnapshots().size());
        return sessionId;
    }

    @Override
    public List<String> getAllSessions() {
        synchronized (sessionLock) {
            return new ArrayList<>(sessionOrder);
        }
    }

    @Override
    public void deleteSession(String sessionId) {
        InMemorySession removed = sessions.remove(sessionId);

        if (removed != null) {
            synchronized (sessionLock) {
                sessionOrder.remove(sessionId);
            }
            log.info("Deleted in-memory session: {}", sessionId);
        }
    }

    /** Get statistics about memory usage. */
    public MemoryStatistics getMemoryStatistics() {
        MemoryStatistics stats = new MemoryStatistics();
        stats.totalSessions = sessions.size();
        stats.totalRecords = sessions.values().stream().mapToInt(s -> s.records.size()).sum();

        // Estimate memory usage (rough approximation)
        stats.estimatedMemoryBytes = stats.totalRecords * 1024; // ~1KB per record estimate

        return stats;
    }

    /** Clear all sessions from memory. */
    public void clearAll() {
        sessions.clear();
        synchronized (sessionLock) {
            sessionOrder.clear();
        }
        log.info("Cleared all in-memory sessions");
    }

    /** Get a copy of all session data (for debugging/testing). */
    public Map<String, List<ActionRecord>> getAllSessionData() {
        Map<String, List<ActionRecord>> data = new HashMap<>();

        for (Map.Entry<String, InMemorySession> entry : sessions.entrySet()) {
            List<ActionRecord> records =
                    entry.getValue().records.stream()
                            .map(r -> r.record)
                            .collect(Collectors.toList());
            data.put(entry.getKey(), records);
        }

        return data;
    }

    private void enforceSessionLimit() {
        int maxSessions = configuration.getMemory().getMaxSessions();

        while (sessionOrder.size() > maxSessions) {
            String oldestSession = sessionOrder.removeFirst();
            InMemorySession removed = sessions.remove(oldestSession);

            if (removed != null) {
                log.debug(
                        "Evicted old session {} to maintain limit of {} sessions",
                        oldestSession,
                        maxSessions);
            }
        }
    }

    private void enforceRecordLimit(InMemorySession session) {
        int maxRecords = configuration.getMemory().getMaxRecordsPerSession();

        while (session.records.size() > maxRecords) {
            session.records.remove(0);
            log.trace("Removed oldest record to maintain limit of {} records", maxRecords);
        }
    }

    private void persistOnShutdown() {
        if (sessions.isEmpty()) {
            return;
        }

        log.info("Persisting {} sessions on shutdown", sessions.size());

        try {
            // Create file-based provider for export
            PersistenceConfiguration fileConfig = PersistenceConfiguration.fileDefault();
            fileConfig.getFile().setBasePath(configuration.getMemory().getShutdownExportPath());

            FileBasedPersistenceProvider fileProvider =
                    new FileBasedPersistenceProvider(fileConfig);

            // Export each session
            for (Map.Entry<String, InMemorySession> entry : sessions.entrySet()) {
                ActionHistory history = exportSession(entry.getKey());
                if (history != null) {
                    String sessionName =
                            entry.getValue().metadata.getName()
                                    + "_shutdown_"
                                    + System.currentTimeMillis();
                    fileProvider.importSession(history, sessionName);
                }
            }

            fileProvider.shutdown();
            log.info("Successfully persisted sessions on shutdown");

        } catch (Exception e) {
            log.error("Failed to persist sessions on shutdown", e);
        }
    }

    /** Internal session representation. */
    private static class InMemorySession {
        SessionMetadata metadata;
        List<RecordWithContext> records;
        LocalDateTime startTime;
        LocalDateTime endTime;
    }

    /** Record with additional context. */
    private static class RecordWithContext {
        ActionRecord record;
        StateObject stateObject;
        LocalDateTime timestamp;
    }

    /** Memory usage statistics. */
    public static class MemoryStatistics {
        public int totalSessions;
        public int totalRecords;
        public long estimatedMemoryBytes;

        @Override
        public String toString() {
            return String.format(
                    "Sessions: %d, Records: %d, Est. Memory: %.2f MB",
                    totalSessions, totalRecords, estimatedMemoryBytes / (1024.0 * 1024.0));
        }
    }
}
