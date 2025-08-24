package io.github.jspinak.brobot.runner.performance;

/**
 * Interface for components that can release memory when requested.
 * Used by the MemoryOptimizer to manage memory pressure.
 */
public interface IMemoryReleasable {
    
    /**
     * Releases memory by clearing caches, buffers, or other memory-intensive resources.
     * 
     * @return The estimated amount of memory released in bytes, or -1 if unknown
     */
    long releaseMemory();
    
    /**
     * Gets the priority of this releasable component.
     * Lower values indicate higher priority (released first).
     * 
     * @return The priority value
     */
    default int getPriority() {
        return 50; // Default middle priority
    }
    
    /**
     * Gets a descriptive name for this releasable component.
     * 
     * @return The component name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}