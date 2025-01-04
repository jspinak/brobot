package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.automationScripts.ContinuousAutomation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class TimedAutomationService {
    private static final Logger logger = LoggerFactory.getLogger(TimedAutomationService.class);

    private final ContinuousAutomation continuousAutomation;
    private ScheduledFuture<?> timerFuture;
    private final ScheduledExecutorService timerExecutor;

    public TimedAutomationService(ContinuousAutomation continuousAutomation) {
        this.continuousAutomation = continuousAutomation;
        this.timerExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public void startTimedAutomation(int seconds) {
        if (continuousAutomation.isRunning()) {
            logger.warn("Automation is already running");
            return;
        }

        logger.info("Starting timed automation for {} seconds", seconds);
        continuousAutomation.start();

        // Schedule the stop
        timerFuture = timerExecutor.schedule(() -> {
            logger.info("Stopping automation after {} seconds", seconds);
            continuousAutomation.stop();
        }, seconds, TimeUnit.SECONDS);
    }

    public void stopTimedAutomation() {
        if (timerFuture != null && !timerFuture.isDone()) {
            timerFuture.cancel(false);
        }
        if (continuousAutomation.isRunning()) {
            continuousAutomation.stop();
        }
        logger.info("Automation stopped manually");
    }

    public boolean isRunning() {
        return continuousAutomation.isRunning();
    }

    // Cleanup method to be called when the application shuts down
    public void shutdown() {
        stopTimedAutomation();
        timerExecutor.shutdownNow();
    }
}
