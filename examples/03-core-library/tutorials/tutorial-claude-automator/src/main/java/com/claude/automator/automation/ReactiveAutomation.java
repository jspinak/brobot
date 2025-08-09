package com.claude.automator.automation;

import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Reactive Approach example from documentation.
 * From: /docs/03-core-library/tutorials/tutorial-claude-automator/automation.md
 * 
 * This demonstrates reactive monitoring using Project Reactor.
 * Note: This is conditional on Reactor being available in the classpath.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnClass(Flux.class)
public class ReactiveAutomation {
    
    private final Action action;
    private final WorkingState workingState;
    
    /**
     * Monitor Claude icon using reactive streams.
     * Returns a Flux that emits true when the icon is missing.
     */
    public Flux<Boolean> monitorClaudeIcon() {
        log.info("Starting reactive Claude icon monitoring");
        
        return Flux.interval(Duration.ofSeconds(2))
            .doOnNext(tick -> log.debug("Checking Claude icon status (tick: {})", tick))
            .map(tick -> checkIconStatus())
            .filter(iconMissing -> {
                if (iconMissing) {
                    log.info("Claude icon missing detected via reactive stream");
                }
                return iconMissing;
            })
            .doOnNext(missing -> reopenWorkingState())
            .doOnError(error -> log.error("Error in reactive monitoring", error))
            .retry(); // Automatically retry on errors
    }
    
    /**
     * Check if the Claude icon is missing.
     * Returns true if icon is NOT found (i.e., missing).
     */
    private boolean checkIconStatus() {
        try {
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                    .setPauseBeforeBegin(0.1)
                    .build();
                    
            boolean iconFound = action.perform(findOptions, workingState.getClaudeIcon()).isSuccess();
            
            log.debug("Claude icon status: {}", iconFound ? "FOUND" : "MISSING");
            
            // Return true if icon is MISSING (inverted logic for filtering)
            return !iconFound;
            
        } catch (Exception e) {
            log.error("Error checking icon status", e);
            return false; // Assume icon is present on error
        }
    }
    
    private void reopenWorkingState() {
        log.info("Reopening Working state via reactive automation");
        
        try {
            // Simulate working state reopening
            Thread.sleep(500);
            log.info("Working state reopened successfully");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while reopening working state", e);
        } catch (Exception e) {
            log.error("Failed to reopen working state", e);
        }
    }
    
    /**
     * Advanced reactive monitoring with backpressure handling.
     */
    public Flux<String> advancedMonitoring() {
        return Flux.interval(Duration.ofSeconds(1))
            .onBackpressureDrop(tick -> log.warn("Dropped monitoring tick: {}", tick))
            .map(tick -> {
                boolean iconMissing = checkIconStatus();
                return iconMissing ? "ICON_MISSING" : "ICON_PRESENT";
            })
            .distinctUntilChanged() // Only emit when status changes
            .doOnNext(status -> log.info("Claude icon status changed to: {}", status))
            .filter(status -> "ICON_MISSING".equals(status))
            .doOnNext(status -> reopenWorkingState())
            .map(status -> "Working state reopened at " + System.currentTimeMillis());
    }
    
    /**
     * Reactive monitoring with custom error handling and recovery.
     */
    public Flux<Boolean> monitorWithErrorRecovery() {
        return Flux.interval(Duration.ofSeconds(2))
            .map(tick -> checkIconStatus())
            .onErrorContinue((throwable, obj) -> {
                log.warn("Error during monitoring tick, continuing: {}", throwable.getMessage());
            })
            .filter(iconMissing -> iconMissing)
            .doOnNext(missing -> {
                try {
                    reopenWorkingState();
                } catch (Exception e) {
                    log.error("Failed to reopen working state", e);
                }
            })
            .retryWhen(reactor.util.retry.Retry.backoff(3, Duration.ofSeconds(5))
                .doBeforeRetry(retrySignal -> 
                    log.info("Retrying reactive monitoring, attempt: {}", 
                        retrySignal.totalRetries() + 1)
                )
            );
    }
    
    /**
     * Demonstration method that shows reactive monitoring in action.
     */
    public void demonstrateReactiveMonitoring() {
        log.info("=== Reactive Monitoring Demonstration ===");
        
        // Start monitoring and take only first 10 emissions for demo
        monitorClaudeIcon()
            .take(10) // Limit to 10 checks for demonstration
            .doOnComplete(() -> log.info("Reactive monitoring demonstration complete"))
            .subscribe(
                iconMissing -> log.info("Icon missing event: {}", iconMissing),
                error -> log.error("Error in reactive monitoring", error),
                () -> log.info("Reactive monitoring stream completed")
            );
    }
    
    /**
     * Start advanced monitoring that logs status changes.
     */
    public void startAdvancedMonitoring() {
        log.info("Starting advanced reactive monitoring...");
        
        advancedMonitoring()
            .take(Duration.ofMinutes(1)) // Run for 1 minute for demo
            .subscribe(
                message -> log.info("Advanced monitoring: {}", message),
                error -> log.error("Advanced monitoring error", error),
                () -> log.info("Advanced monitoring completed")
            );
    }
}