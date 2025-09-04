package io.github.jspinak.brobot.test.utils;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Utility class to help with timing and concurrency issues in tests.
 * Provides methods to wait for conditions with timeout and retry logic.
 */
public class ConcurrentTestHelper {
    
    /**
     * Default timeout for waiting operations
     */
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    
    /**
     * Default polling interval
     */
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofMillis(100);
    
    /**
     * Waits for a condition to become true with timeout.
     * 
     * @param condition the condition to check
     * @param timeout maximum time to wait
     * @return true if condition became true within timeout, false otherwise
     */
    public static boolean waitForCondition(BooleanSupplier condition, Duration timeout) {
        return waitForCondition(condition, timeout, DEFAULT_POLL_INTERVAL);
    }
    
    /**
     * Waits for a condition to become true with timeout and custom polling interval.
     * 
     * @param condition the condition to check
     * @param timeout maximum time to wait
     * @param pollInterval how often to check the condition
     * @return true if condition became true within timeout, false otherwise
     */
    public static boolean waitForCondition(BooleanSupplier condition, Duration timeout, Duration pollInterval) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeout.toMillis();
        long pollMillis = pollInterval.toMillis();
        
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (condition.getAsBoolean()) {
                return true;
            }
            try {
                Thread.sleep(pollMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
    
    /**
     * Waits for a value to be produced with timeout.
     * 
     * @param supplier the supplier that may eventually produce a non-null value
     * @param timeout maximum time to wait
     * @param <T> the type of value
     * @return the value if produced within timeout, null otherwise
     */
    public static <T> T waitForValue(Supplier<T> supplier, Duration timeout) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeout.toMillis();
        
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            T value = supplier.get();
            if (value != null) {
                return value;
            }
            try {
                Thread.sleep(DEFAULT_POLL_INTERVAL.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }
    
    /**
     * Executes a task with timeout and returns whether it completed successfully.
     * 
     * @param task the task to execute
     * @param timeout maximum time to wait for completion
     * @return true if task completed within timeout, false otherwise
     */
    public static boolean executeWithTimeout(Runnable task, Duration timeout) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(task);
        
        try {
            future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return true;
        } catch (TimeoutException e) {
            future.cancel(true);
            return false;
        } catch (InterruptedException | ExecutionException e) {
            return false;
        } finally {
            executor.shutdownNow();
        }
    }
    
    /**
     * Waits for a CountDownLatch with better error reporting.
     * 
     * @param latch the latch to wait for
     * @param timeout maximum time to wait
     * @param description description of what we're waiting for
     * @return true if latch reached zero within timeout
     */
    public static boolean awaitLatch(CountDownLatch latch, Duration timeout, String description) {
        try {
            boolean success = latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!success) {
                long remaining = latch.getCount();
                throw new AssertionError(String.format(
                    "Timeout waiting for %s. Latch count remaining: %d", 
                    description, remaining));
            }
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for " + description, e);
        }
    }
    
    /**
     * Retries an operation that may fail due to timing issues.
     * 
     * @param operation the operation to retry
     * @param maxAttempts maximum number of attempts
     * @param delayBetweenAttempts delay between retry attempts
     * @return true if operation succeeded within max attempts
     */
    public static boolean retryOperation(BooleanSupplier operation, int maxAttempts, Duration delayBetweenAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (operation.getAsBoolean()) {
                return true;
            }
            
            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(delayBetweenAttempts.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }
    
    /**
     * Creates a CountDownLatch-based synchronization helper for async operations.
     * 
     * @param expectedCount expected number of events
     * @return a latch initialized with the expected count
     */
    public static CountDownLatch createLatch(int expectedCount) {
        return new CountDownLatch(expectedCount);
    }
    
    /**
     * Waits for an async operation to produce a result.
     * 
     * @param future the future to wait for
     * @param timeout maximum time to wait
     * @param <T> the type of result
     * @return the result if available within timeout
     * @throws TimeoutException if timeout exceeded
     * @throws ExecutionException if computation threw an exception
     */
    public static <T> T getWithTimeout(Future<T> future, Duration timeout) 
            throws TimeoutException, ExecutionException {
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExecutionException("Interrupted while waiting for result", e);
        }
    }
    
    /**
     * Ensures an executor service shuts down cleanly within timeout.
     * 
     * @param executor the executor to shut down
     * @param timeout maximum time to wait for termination
     * @return true if executor terminated within timeout
     */
    public static boolean shutdownExecutor(ExecutorService executor, Duration timeout) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
                return executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
            }
            return true;
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            return false;
        }
    }
}