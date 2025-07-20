package io.github.jspinak.brobot.runner.execution.context;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;

/**
 * Configuration options for task execution.
 * 
 * This class encapsulates all configurable parameters for an execution,
 * following the principle of explicit configuration over implicit defaults.
 * 
 * @since 1.0.0
 */
@Getter
@Builder
@ToString
public class ExecutionOptions {
    
    /**
     * Maximum time allowed for execution before timeout
     */
    @Builder.Default
    private final Duration timeout = Duration.ofMinutes(5);
    
    /**
     * Whether to run in safe mode (with additional safety checks)
     */
    @Builder.Default
    private final boolean safeMode = true;
    
    /**
     * Maximum number of retry attempts on failure
     */
    @Builder.Default
    private final int maxRetries = 0;
    
    /**
     * Whether to collect diagnostic information during execution
     */
    @Builder.Default
    private final boolean diagnosticMode = false;
    
    /**
     * Priority level for thread scheduling (1-10, where 10 is highest)
     */
    @Builder.Default
    private final int priority = Thread.NORM_PRIORITY;
    
    /**
     * Whether the execution can be interrupted
     */
    @Builder.Default
    private final boolean interruptible = true;
    
    /**
     * Delay before starting execution
     */
    @Builder.Default
    private final Duration startDelay = Duration.ZERO;
    
    /**
     * Creates default execution options
     */
    public static ExecutionOptions defaultOptions() {
        return ExecutionOptions.builder().build();
    }
    
    /**
     * Creates execution options for quick tasks
     */
    public static ExecutionOptions quickTask() {
        return ExecutionOptions.builder()
                .timeout(Duration.ofSeconds(30))
                .safeMode(false)
                .priority(Thread.MAX_PRIORITY)
                .build();
    }
    
    /**
     * Creates execution options for long-running tasks
     */
    public static ExecutionOptions longRunning() {
        return ExecutionOptions.builder()
                .timeout(Duration.ofHours(1))
                .safeMode(true)
                .priority(Thread.MIN_PRIORITY)
                .interruptible(false)
                .build();
    }
}