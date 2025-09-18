package io.github.jspinak.brobot.test.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

/**
 * Utility class for proper test synchronization without Thread.sleep. Provides various
 * synchronization mechanisms to replace Thread.sleep in tests.
 */
public class TestSynchronization {

    private static final int DEFAULT_TIMEOUT_MS = 5000;
    private static final int POLL_INTERVAL_MS = 10;

    /**
     * Wait for a condition to become true with timeout. Replaces Thread.sleep with condition-based
     * waiting.
     *
     * @param condition The condition to wait for
     * @param timeoutMs Maximum time to wait in milliseconds
     * @return true if condition was met, false if timeout
     */
    public static boolean waitForCondition(BooleanSupplier condition, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (condition.getAsBoolean()) {
                return true;
            }
            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /** Wait for a condition with default timeout. */
    public static boolean waitForCondition(BooleanSupplier condition) {
        return waitForCondition(condition, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Wait for async operation using CountDownLatch. Better alternative to Thread.sleep for async
     * operations.
     */
    public static boolean awaitAsync(Runnable asyncOperation, long timeoutMs) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);

        Thread thread =
                new Thread(
                        () -> {
                            try {
                                asyncOperation.run();
                                success.set(true);
                            } finally {
                                latch.countDown();
                            }
                        });

        thread.start();

        try {
            boolean completed = latch.await(timeoutMs, TimeUnit.MILLISECONDS);
            return completed && success.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /** Wait for a value to be set with timeout. Useful for waiting for async callbacks. */
    public static <T> T waitForValue(AtomicReference<T> reference, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            T value = reference.get();
            if (value != null) {
                return value;
            }
            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    /**
     * Synchronization barrier for coordinating multiple threads. Replaces Thread.sleep when waiting
     * for multiple operations.
     */
    public static class SyncBarrier {
        private final CountDownLatch latch;
        private final long timeoutMs;

        public SyncBarrier(int parties, long timeoutMs) {
            this.latch = new CountDownLatch(parties);
            this.timeoutMs = timeoutMs;
        }

        public SyncBarrier(int parties) {
            this(parties, DEFAULT_TIMEOUT_MS);
        }

        public void arrive() {
            latch.countDown();
        }

        public boolean await() {
            try {
                return latch.await(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    /**
     * Retry mechanism with exponential backoff. Better than fixed Thread.sleep for flaky
     * operations.
     */
    public static boolean retryWithBackoff(Runnable operation, int maxRetries) {
        int delay = 10;
        for (int i = 0; i < maxRetries; i++) {
            try {
                operation.run();
                return true;
            } catch (Exception e) {
                if (i < maxRetries - 1) {
                    try {
                        Thread.sleep(delay);
                        delay *= 2; // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /** Wait for async flush operations. Specifically for output stream flushing. */
    public static void waitForFlush() {
        // Force flush and wait briefly for buffers
        System.out.flush();
        System.err.flush();
        try {
            // Very short wait just for OS buffer flush
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
