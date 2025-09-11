package io.github.jspinak.brobot.runner.errorhandling.processing;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.IErrorProcessor;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for executing error processors in sequence.
 *
 * <p>This service manages the registration and execution of error processors, handling failures
 * gracefully to ensure all processors have a chance to run.
 *
 * <p>Thread Safety: This class is thread-safe.
 *
 * @since 1.0.0
 */
@Slf4j
@Service
public class ErrorProcessingService implements DiagnosticCapable {

    private final List<IErrorProcessor> processors = new CopyOnWriteArrayList<>();

    // Statistics
    private final AtomicLong totalProcessed = new AtomicLong(0);
    private final AtomicLong processingFailures = new AtomicLong(0);
    private final Map<String, AtomicLong> processorExecutions = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> processorFailures = new ConcurrentHashMap<>();

    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);

    /**
     * Process an error through all registered processors.
     *
     * @param error the error to process
     * @param context the error context
     */
    public void processError(Throwable error, ErrorContext context) {
        if (error == null || context == null) {
            log.warn("Attempted to process null error or context");
            return;
        }

        totalProcessed.incrementAndGet();

        if (diagnosticMode.get()) {
            log.info(
                    "[DIAGNOSTIC] Processing error {} with {} processors",
                    context.getErrorId(),
                    processors.size());
        }

        for (IErrorProcessor processor : processors) {
            String processorName = processor.getClass().getSimpleName();

            try {
                // Track execution
                processorExecutions
                        .computeIfAbsent(processorName, k -> new AtomicLong())
                        .incrementAndGet();

                // Execute processor
                processor.process(error, context);

                if (diagnosticMode.get()) {
                    log.debug("[DIAGNOSTIC] Successfully executed processor: {}", processorName);
                }

            } catch (Exception e) {
                // Track failure
                processingFailures.incrementAndGet();
                processorFailures
                        .computeIfAbsent(processorName, k -> new AtomicLong())
                        .incrementAndGet();

                log.error(
                        "Error processor {} failed for error {}",
                        processorName,
                        context.getErrorId(),
                        e);

                // Continue with next processor - don't let one failure stop others
            }
        }
    }

    /**
     * Register a new error processor.
     *
     * @param processor the processor to register
     * @throws IllegalArgumentException if processor is null
     */
    public void registerProcessor(IErrorProcessor processor) {
        if (processor == null) {
            throw new IllegalArgumentException("Processor cannot be null");
        }

        processors.add(processor);
        log.info("Registered error processor: {}", processor.getClass().getSimpleName());

        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Total registered processors: {}", processors.size());
        }
    }

    /**
     * Remove a processor from the pipeline.
     *
     * @param processor the processor to remove
     * @return true if removed, false if not found
     */
    public boolean removeProcessor(IErrorProcessor processor) {
        return processors.remove(processor);
    }

    /**
     * Get all registered processors.
     *
     * @return unmodifiable list of processors
     */
    public List<IErrorProcessor> getProcessors() {
        return List.copyOf(processors);
    }

    /** Clear all registered processors. */
    public void clearProcessors() {
        processors.clear();
        log.info("Cleared all error processors");
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        states.put("registeredProcessors", processors.size());
        states.put("totalProcessed", totalProcessed.get());
        states.put("processingFailures", processingFailures.get());
        states.put("failureRate", calculateFailureRate());

        // Per-processor statistics
        processors.forEach(
                processor -> {
                    String name = processor.getClass().getSimpleName();
                    Long executions =
                            processorExecutions.getOrDefault(name, new AtomicLong(0)).get();
                    Long failures = processorFailures.getOrDefault(name, new AtomicLong(0)).get();

                    states.put("processor." + name + ".executions", executions);
                    states.put("processor." + name + ".failures", failures);
                    states.put(
                            "processor." + name + ".successRate",
                            executions > 0
                                    ? ((executions - failures) * 100.0 / executions)
                                    : 100.0);
                });

        return DiagnosticInfo.builder().component("ErrorProcessingService").states(states).build();
    }

    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }

    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);
        log.info("Diagnostic mode {} for ErrorProcessingService", enabled ? "enabled" : "disabled");
    }

    /**
     * Get the number of registered processors.
     *
     * @return processor count
     */
    public int getProcessorCount() {
        return processors.size();
    }

    private double calculateFailureRate() {
        long total = totalProcessed.get();
        if (total == 0) {
            return 0.0;
        }
        return (processingFailures.get() * 100.0) / total;
    }
}
