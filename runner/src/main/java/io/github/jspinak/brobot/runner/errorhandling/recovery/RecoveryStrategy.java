package io.github.jspinak.brobot.runner.errorhandling.recovery;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Interface for implementing different recovery strategies.
 */
public interface RecoveryStrategy {
    
    /**
     * Attempt to recover from an error.
     *
     * @param error The error that occurred
     * @param context The error context
     * @param retryOperation The operation to retry
     * @param state The recovery state tracking
     * @return A future containing the recovery result
     */
    CompletableFuture<RecoveryResult> recover(
        Throwable error,
        ErrorContext context,
        Supplier<Object> retryOperation,
        RecoveryManager.RecoveryState state
    );
}