package io.github.jspinak.brobot.persistence.provider;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for persistence providers. Provides common functionality for all
 * implementations.
 */
@Slf4j
public abstract class AbstractPersistenceProvider implements PersistenceProvider {

    @Getter protected final PersistenceConfiguration configuration;

    protected String currentSessionId;
    protected SessionMetadata currentSession;
    protected final AtomicBoolean isRecording = new AtomicBoolean(false);
    protected final AtomicBoolean isPaused = new AtomicBoolean(false);

    // Async support
    protected ExecutorService executorService;
    protected BlockingQueue<RecordTask> recordQueue;

    public AbstractPersistenceProvider(PersistenceConfiguration configuration) {
        this.configuration = configuration;
        initializeAsync();
    }

    private void initializeAsync() {
        if (configuration.getPerformance().isAsyncRecording()) {
            PersistenceConfiguration.PerformanceSettings perf = configuration.getPerformance();

            this.recordQueue = new LinkedBlockingQueue<>(perf.getQueueCapacity());
            this.executorService =
                    new ThreadPoolExecutor(
                            perf.getThreadPoolSize(),
                            perf.getThreadPoolSize(),
                            0L,
                            TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue<>(),
                            new ThreadFactory() {
                                private int counter = 0;

                                @Override
                                public Thread newThread(Runnable r) {
                                    Thread thread = new Thread(r);
                                    thread.setName("brobot-persistence-" + counter++);
                                    thread.setDaemon(true);
                                    return thread;
                                }
                            });

            // Start async processor
            startAsyncProcessor();
        }
    }

    private void startAsyncProcessor() {
        executorService.submit(
                () -> {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            RecordTask task = recordQueue.poll(1, TimeUnit.SECONDS);
                            if (task != null) {
                                processRecordTask(task);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        } catch (Exception e) {
                            log.error("Error processing record task", e);
                        }
                    }
                });
    }

    @Override
    public String startSession(String sessionName, String application, String metadata) {
        if (isRecording.get()) {
            log.warn("Already recording, stopping current session");
            stopSession();
        }

        currentSessionId = UUID.randomUUID().toString();
        currentSession = new SessionMetadata(currentSessionId, sessionName, application);
        currentSession.setMetadata(metadata);

        isRecording.set(true);
        isPaused.set(false);

        doStartSession(currentSessionId, currentSession);

        log.info("Started recording session: {} ({})", sessionName, currentSessionId);
        return currentSessionId;
    }

    @Override
    public String stopSession() {
        if (!isRecording.get()) {
            log.warn("Not currently recording");
            return null;
        }

        isRecording.set(false);
        isPaused.set(false);

        if (currentSession != null) {
            currentSession.setEndTime(LocalDateTime.now());
        }

        // Flush any pending records
        if (configuration.getPerformance().isAsyncRecording()) {
            flushPendingRecords();
        }

        doStopSession(currentSessionId, currentSession);

        String sessionId = currentSessionId;
        log.info(
                "Stopped recording session: {} ({} actions)",
                currentSessionId,
                currentSession != null ? currentSession.getTotalActions() : 0);

        currentSessionId = null;
        currentSession = null;

        return sessionId;
    }

    @Override
    public void pauseRecording() {
        if (isRecording.get() && !isPaused.get()) {
            isPaused.set(true);
            log.debug("Recording paused");
        }
    }

    @Override
    public void resumeRecording() {
        if (isRecording.get() && isPaused.get()) {
            isPaused.set(false);
            log.debug("Recording resumed");
        }
    }

    @Override
    public boolean isRecording() {
        return isRecording.get() && !isPaused.get();
    }

    @Override
    public void recordAction(ActionRecord record, StateObject stateObject) {
        if (!isRecording()) {
            return;
        }

        if (currentSession != null) {
            currentSession.setTotalActions(currentSession.getTotalActions() + 1);
            if (record.isActionSuccess()) {
                currentSession.setSuccessfulActions(currentSession.getSuccessfulActions() + 1);
            }
        }

        if (configuration.getPerformance().isAsyncRecording()) {
            recordAsync(record, stateObject);
        } else {
            doRecordAction(currentSessionId, record, stateObject);
        }
    }

    @Override
    public void recordBatch(java.util.List<ActionRecord> records) {
        if (!isRecording()) {
            return;
        }

        for (ActionRecord record : records) {
            recordAction(record, null);
        }
    }

    @Override
    public String getCurrentSessionId() {
        return currentSessionId;
    }

    @Override
    public SessionMetadata getSessionMetadata(String sessionId) {
        if (currentSessionId != null && currentSessionId.equals(sessionId)) {
            return currentSession;
        }
        return doGetSessionMetadata(sessionId);
    }

    private void recordAsync(ActionRecord record, StateObject stateObject) {
        try {
            if (!recordQueue.offer(
                    new RecordTask(currentSessionId, record, stateObject),
                    100,
                    TimeUnit.MILLISECONDS)) {
                log.warn("Record queue full, recording synchronously");
                doRecordAction(currentSessionId, record, stateObject);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while queuing record", e);
        }
    }

    private void processRecordTask(RecordTask task) {
        try {
            doRecordAction(task.sessionId, task.record, task.stateObject);
        } catch (Exception e) {
            log.error("Error recording action", e);
        }
    }

    private void flushPendingRecords() {
        if (recordQueue == null || recordQueue.isEmpty()) {
            return;
        }

        log.debug("Flushing {} pending records", recordQueue.size());

        RecordTask task;
        while ((task = recordQueue.poll()) != null) {
            processRecordTask(task);
        }
    }

    public void shutdown() {
        if (isRecording.get()) {
            stopSession();
        }

        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // Abstract methods for implementations
    protected abstract void doStartSession(String sessionId, SessionMetadata metadata);

    protected abstract void doStopSession(String sessionId, SessionMetadata metadata);

    protected abstract void doRecordAction(
            String sessionId, ActionRecord record, StateObject stateObject);

    protected abstract SessionMetadata doGetSessionMetadata(String sessionId);

    /** Internal class for async record tasks. */
    private static class RecordTask {
        final String sessionId;
        final ActionRecord record;
        final StateObject stateObject;

        RecordTask(String sessionId, ActionRecord record, StateObject stateObject) {
            this.sessionId = sessionId;
            this.record = record;
            this.stateObject = stateObject;
        }
    }
}
