package io.github.jspinak.brobot.runner.session.services;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for automatic periodic saving of sessions. Provides configurable autosave
 * intervals and pause/resume functionality.
 */
@Slf4j
@Service("sessionAutosaveService")
public class SessionAutosaveService {

    private static final long DEFAULT_AUTOSAVE_INTERVAL_MINUTES = 5;
    private static final long MIN_AUTOSAVE_INTERVAL_MINUTES = 1;
    private static final long MAX_AUTOSAVE_INTERVAL_MINUTES = 60;

    private final EventBus eventBus;
    private final SessionLifecycleService lifecycleService;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> autosaveTask;
    private final AtomicBoolean autosaveEnabled = new AtomicBoolean(true);

    @Getter private long autosaveIntervalMinutes = DEFAULT_AUTOSAVE_INTERVAL_MINUTES;

    @Getter private LocalDateTime lastAutosaveTime;

    @Getter private long autosaveCount = 0;

    @Autowired
    public SessionAutosaveService(EventBus eventBus, SessionLifecycleService lifecycleService) {

        this.eventBus = eventBus;
        this.lifecycleService = lifecycleService;
    }

    @PostConstruct
    public void initialize() {
        // Create daemon thread executor
        scheduler =
                Executors.newSingleThreadScheduledExecutor(
                        r -> {
                            Thread t = new Thread(r, "Session-Autosave");
                            t.setDaemon(true);
                            return t;
                        });

        // Start autosave
        startAutosave();

        log.info(
                "Session autosave service initialized with interval: {} minutes",
                autosaveIntervalMinutes);
    }

    @PreDestroy
    public void shutdown() {
        stopAutosave();

        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        log.info("Session autosave service shut down");
    }

    /** Starts or restarts the autosave task. */
    public void startAutosave() {
        // Cancel existing task if any
        if (autosaveTask != null && !autosaveTask.isDone()) {
            autosaveTask.cancel(false);
        }

        if (!autosaveEnabled.get()) {
            log.debug("Autosave is disabled, not starting task");
            return;
        }

        // Schedule new autosave task
        autosaveTask =
                scheduler.scheduleAtFixedRate(
                        this::performAutosave,
                        autosaveIntervalMinutes,
                        autosaveIntervalMinutes,
                        TimeUnit.MINUTES);

        log.info("Autosave started with interval: {} minutes", autosaveIntervalMinutes);
        eventBus.publish(
                LogEvent.info(
                        this,
                        "Session autosave started: " + autosaveIntervalMinutes + " minutes",
                        "Session"));
    }

    /** Stops the autosave task. */
    public void stopAutosave() {
        if (autosaveTask != null) {
            autosaveTask.cancel(false);
            log.info("Autosave stopped");
            eventBus.publish(LogEvent.info(this, "Session autosave stopped", "Session"));
        }
    }

    /** Performs the autosave operation. */
    private void performAutosave() {
        if (!autosaveEnabled.get()) {
            return;
        }

        try {
            if (lifecycleService.isSessionActive()) {
                lifecycleService.saveCurrentSession();

                lastAutosaveTime = LocalDateTime.now();
                autosaveCount++;

                log.debug("Session autosaved (count: {})", autosaveCount);
                eventBus.publish(
                        LogEvent.debug(
                                this,
                                "Session autosaved: "
                                        + lifecycleService.getCurrentSession().getId(),
                                "Session"));
            }
        } catch (Exception e) {
            log.error("Failed to autosave session", e);
            eventBus.publish(
                    LogEvent.error(
                            this, "Failed to autosave session: " + e.getMessage(), "Session", e));
        }
    }

    /** Triggers an immediate autosave. */
    public void triggerAutosave() {
        if (!autosaveEnabled.get()) {
            log.debug("Autosave is disabled, ignoring trigger");
            return;
        }

        log.debug("Triggering immediate autosave");
        scheduler.execute(this::performAutosave);
    }

    /** Sets the autosave interval in minutes. */
    public void setAutosaveInterval(long minutes) {
        if (minutes < MIN_AUTOSAVE_INTERVAL_MINUTES || minutes > MAX_AUTOSAVE_INTERVAL_MINUTES) {
            throw new IllegalArgumentException(
                    String.format(
                            "Autosave interval must be between %d and %d minutes",
                            MIN_AUTOSAVE_INTERVAL_MINUTES, MAX_AUTOSAVE_INTERVAL_MINUTES));
        }

        long oldInterval = this.autosaveIntervalMinutes;
        this.autosaveIntervalMinutes = minutes;

        // Restart autosave with new interval
        if (autosaveEnabled.get()) {
            startAutosave();
        }

        log.info("Autosave interval changed from {} to {} minutes", oldInterval, minutes);
        eventBus.publish(
                LogEvent.info(
                        this, "Autosave interval changed to: " + minutes + " minutes", "Session"));
    }

    /** Enables autosave functionality. */
    public void enableAutosave() {
        if (autosaveEnabled.compareAndSet(false, true)) {
            startAutosave();
            log.info("Autosave enabled");
            eventBus.publish(LogEvent.info(this, "Session autosave enabled", "Session"));
        }
    }

    /** Disables autosave functionality. */
    public void disableAutosave() {
        if (autosaveEnabled.compareAndSet(true, false)) {
            stopAutosave();
            log.info("Autosave disabled");
            eventBus.publish(LogEvent.info(this, "Session autosave disabled", "Session"));
        }
    }

    /** Checks if autosave is enabled. */
    public boolean isAutosaveEnabled() {
        return autosaveEnabled.get();
    }

    /** Gets the time until the next autosave. */
    public long getTimeUntilNextAutosave() {
        if (autosaveTask == null || autosaveTask.isDone() || !autosaveEnabled.get()) {
            return -1;
        }

        return autosaveTask.getDelay(TimeUnit.SECONDS);
    }

    /** Resets autosave statistics. */
    public void resetStatistics() {
        autosaveCount = 0;
        lastAutosaveTime = null;
        log.debug("Autosave statistics reset");
    }
}
