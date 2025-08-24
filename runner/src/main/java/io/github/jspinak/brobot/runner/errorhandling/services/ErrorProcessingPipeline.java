package io.github.jspinak.brobot.runner.errorhandling.services;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.IErrorProcessor;
import io.github.jspinak.brobot.runner.errorhandling.processors.EventPublishingProcessor;
import io.github.jspinak.brobot.runner.errorhandling.processors.LoggingErrorProcessor;
import io.github.jspinak.brobot.runner.events.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service responsible for managing the error processing pipeline.
 * Coordinates multiple error processors that handle errors in sequence.
 */
@Slf4j
@Service
public class ErrorProcessingPipeline {
    
    private final EventBus eventBus;
    private final List<IErrorProcessor> errorProcessors = new CopyOnWriteArrayList<>();
    
    @Autowired
    public ErrorProcessingPipeline(EventBus eventBus) {
        this.eventBus = eventBus;
    }
    
    @PostConstruct
    public void initialize() {
        // Register default processors
        registerProcessor(new LoggingErrorProcessor());
        registerProcessor(new EventPublishingProcessor(eventBus));
        
        log.info("Error processing pipeline initialized with {} processors", errorProcessors.size());
    }
    
    /**
     * Processes an error through all registered processors.
     */
    public void process(Throwable error, ErrorContext context) {
        log.trace("Processing error through {} processors", errorProcessors.size());
        
        for (IErrorProcessor processor : errorProcessors) {
            try {
                processor.process(error, context);
            } catch (Exception e) {
                log.error("Error processor {} failed while processing {}", 
                         processor.getClass().getSimpleName(), 
                         error.getClass().getSimpleName(), e);
            }
        }
    }
    
    /**
     * Registers a custom error processor.
     */
    public void registerProcessor(IErrorProcessor processor) {
        if (processor == null) {
            throw new IllegalArgumentException("Processor cannot be null");
        }
        
        errorProcessors.add(processor);
        log.debug("Registered error processor: {}", processor.getClass().getSimpleName());
    }
    
    /**
     * Removes a processor from the pipeline.
     */
    public boolean removeProcessor(IErrorProcessor processor) {
        boolean removed = errorProcessors.remove(processor);
        if (removed) {
            log.debug("Removed error processor: {}", processor.getClass().getSimpleName());
        }
        return removed;
    }
    
    /**
     * Removes all processors of a specific type.
     */
    public int removeProcessorsByType(Class<? extends IErrorProcessor> processorType) {
        List<IErrorProcessor> toRemove = errorProcessors.stream()
            .filter(processorType::isInstance)
            .toList();
        
        int removedCount = toRemove.size();
        errorProcessors.removeAll(toRemove);
        
        if (removedCount > 0) {
            log.debug("Removed {} processors of type {}", removedCount, processorType.getSimpleName());
        }
        
        return removedCount;
    }
    
    /**
     * Clears all processors except the default ones.
     */
    public void clearCustomProcessors() {
        errorProcessors.clear();
        initialize(); // Re-add default processors
        log.info("Cleared custom processors, restored defaults");
    }
    
    /**
     * Gets the count of registered processors.
     */
    public int getProcessorCount() {
        return errorProcessors.size();
    }
    
    /**
     * Checks if a specific processor type is registered.
     */
    public boolean hasProcessorType(Class<? extends IErrorProcessor> processorType) {
        return errorProcessors.stream().anyMatch(processorType::isInstance);
    }
    
    /**
     * Gets a list of processor class names (for debugging/monitoring).
     */
    public List<String> getProcessorNames() {
        return errorProcessors.stream()
            .map(processor -> processor.getClass().getSimpleName())
            .toList();
    }
    
    /**
     * Executes a specific processor by type (useful for testing).
     */
    public boolean executeProcessor(Class<? extends IErrorProcessor> processorType, 
                                   Throwable error, ErrorContext context) {
        for (IErrorProcessor processor : errorProcessors) {
            if (processorType.isInstance(processor)) {
                try {
                    processor.process(error, context);
                    return true;
                } catch (Exception e) {
                    log.error("Failed to execute processor {}", processorType.getSimpleName(), e);
                    return false;
                }
            }
        }
        
        log.warn("No processor found of type {}", processorType.getSimpleName());
        return false;
    }
}