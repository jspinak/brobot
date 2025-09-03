package io.github.jspinak.brobot.aspects.core;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.exception.ActionFailedException;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.sikuli.script.FindFailed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Aspect that intercepts all Sikuli method calls to provide:
 * - Centralized error handling and translation
 * - Mock mode support without wrapper modifications
 * - Automatic screenshot capture on failures
 * - Comprehensive operation logging
 * - Performance metrics collection
 * 
 * This aspect eliminates the need for repetitive error handling in wrapper
 * classes
 * and provides a single point of control for all Sikuli operations.
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(prefix = "brobot.aspects.sikuli", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SikuliInterceptionAspect {

    @Autowired
    private BrobotLogger brobotLogger;

    // Screenshot capture will be implemented later
    // @Autowired(required = false)
    // private ScreenCapture screenCapture;

    // Metrics collection
    private final ConcurrentHashMap<String, OperationMetrics> metricsMap = new ConcurrentHashMap<>();

    // Mock implementations
    private final SikuliMockProvider mockProvider = new SikuliMockProvider();

    /**
     * Pointcut for all Sikuli public methods
     */
    @Pointcut("execution(public * org.sikuli.script..*(..))")
    public void sikuliOperation() {
    }

    /**
     * Pointcut for Sikuli find operations that might throw FindFailed
     * Matches methods like find, findAll, wait, exists, etc.
     */
    @Pointcut("execution(* org.sikuli.script..find*(..)) || " +
            "execution(* org.sikuli.script..wait*(..)) || " +
            "execution(* org.sikuli.script..exists(..))")
    public void sikuliFindOperation() {
    }

    /**
     * Main interception for all Sikuli operations
     */
    @Around("sikuliOperation()")
    public Object interceptSikuliCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String operation = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        // Check mock mode
        if (FrameworkSettings.mock) {
            log.debug("Mock mode: intercepting {}", operation);
            return handleMockMode(joinPoint);
        }

        // Log operation start
        logOperationStart(joinPoint);

        try {
            // Execute the actual Sikuli operation
            Object result = joinPoint.proceed();

            // Log success
            long duration = System.currentTimeMillis() - startTime;
            logOperationSuccess(joinPoint, result, duration);
            updateMetrics(operation, true, duration);

            return result;

        } catch (FindFailed e) {
            // Handle FindFailed specifically
            long duration = System.currentTimeMillis() - startTime;
            handleFindFailed(joinPoint, e, duration);
            updateMetrics(operation, false, duration);
            throw new ActionFailedException(ActionInterface.Type.FIND,
                    "Sikuli find operation failed: " + e.getMessage(), e);

        } catch (Exception e) {
            // Handle other exceptions
            long duration = System.currentTimeMillis() - startTime;
            handleGeneralException(joinPoint, e, duration);
            updateMetrics(operation, false, duration);
            throw new ActionFailedException(ActionInterface.Type.FIND, "Sikuli operation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Handle mock mode execution
     */
    private Object handleMockMode(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Log mock operation
        brobotLogger.log()
                .type(LogEvent.Type.ACTION)
                .level(LogEvent.Level.DEBUG)
                .action("MOCK_" + methodName.toUpperCase())
                .metadata("method", methodName)
                .metadata("args", Arrays.toString(args))
                .metadata("mock", true)
                .log();

        // Return appropriate mock result
        return mockProvider.getMockResult(methodName, args);
    }

    /**
     * Log operation start
     */
    private void logOperationStart(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        brobotLogger.log()
                .type(LogEvent.Type.ACTION)
                .level(LogEvent.Level.DEBUG)
                .action("SIKULI_" + methodName.toUpperCase())
                .metadata("method", methodName)
                .metadata("args", sanitizeArgs(args))
                .metadata("thread", Thread.currentThread().getName())
                .log();
    }

    /**
     * Log operation success
     */
    private void logOperationSuccess(JoinPoint joinPoint, Object result, long duration) {
        String methodName = joinPoint.getSignature().getName();

        brobotLogger.log()
                .type(LogEvent.Type.ACTION)
                .level(LogEvent.Level.DEBUG)
                .action("SIKULI_" + methodName.toUpperCase() + "_SUCCESS")
                .success(true)
                .duration(duration)
                .metadata("method", methodName)
                .metadata("resultType", result != null ? result.getClass().getSimpleName() : "null")
                .log();
    }

    /**
     * Handle FindFailed exceptions with screenshot capture
     */
    private void handleFindFailed(JoinPoint joinPoint, FindFailed e, long duration) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Screenshot capture will be implemented later
        String screenshotPath = null;
        // TODO: Implement screenshot capture
        // if (screenCapture != null) {
        // try {
        // screenshotPath = screenCapture.captureError("FindFailed_" + methodName);
        // } catch (Exception screenshotError) {
        // log.error("Failed to capture screenshot on FindFailed", screenshotError);
        // }
        // }

        // Log the failure with details
        brobotLogger.log()
                .type(LogEvent.Type.ERROR)
                .level(LogEvent.Level.ERROR)
                .action("SIKULI_" + methodName.toUpperCase() + "_FAILED")
                .success(false)
                .duration(duration)
                .error(e)
                .metadata("method", methodName)
                .metadata("args", sanitizeArgs(args))
                .metadata("errorType", "FindFailed")
                .metadata("screenshot", screenshotPath)
                .metadata("searchImage", extractImagePath(args))
                .log();
    }

    /**
     * Handle general exceptions
     */
    private void handleGeneralException(JoinPoint joinPoint, Exception e, long duration) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        brobotLogger.log()
                .type(LogEvent.Type.ERROR)
                .level(LogEvent.Level.ERROR)
                .action("SIKULI_" + methodName.toUpperCase() + "_ERROR")
                .success(false)
                .duration(duration)
                .error(e)
                .metadata("method", methodName)
                .metadata("args", sanitizeArgs(args))
                .metadata("errorType", e.getClass().getSimpleName())
                .log();
    }

    /**
     * Update operation metrics
     */
    private void updateMetrics(String operation, boolean success, long duration) {
        metricsMap.compute(operation, (k, v) -> {
            if (v == null) {
                v = new OperationMetrics(operation);
            }
            v.recordOperation(success, duration);
            return v;
        });
    }

    /**
     * Sanitize arguments for logging (remove sensitive data, large objects)
     */
    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(sanitizeArg(args[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Sanitize individual argument
     */
    private String sanitizeArg(Object arg) {
        if (arg == null) {
            return "null";
        }

        // For image paths, just show the filename
        if (arg instanceof String && ((String) arg).contains("/")) {
            String path = (String) arg;
            int lastSlash = path.lastIndexOf('/');
            return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
        }

        // For other objects, use simple string representation
        return arg.toString();
    }

    /**
     * Extract image path from arguments if present
     */
    private String extractImagePath(Object[] args) {
        if (args == null)
            return null;

        for (Object arg : args) {
            if (arg instanceof String) {
                String str = (String) arg;
                if (str.endsWith(".png") || str.endsWith(".jpg") || str.endsWith(".jpeg")) {
                    return str;
                }
            }
        }
        return null;
    }

    /**
     * Get metrics for reporting
     */
    public ConcurrentHashMap<String, OperationMetrics> getMetrics() {
        return new ConcurrentHashMap<>(metricsMap);
    }

    /**
     * Reset metrics
     */
    public void resetMetrics() {
        metricsMap.clear();
        log.info("Sikuli operation metrics reset");
    }

    /**
     * Inner class for tracking operation metrics
     */
    public static class OperationMetrics {
        private final String operation;
        private final AtomicInteger totalCalls = new AtomicInteger();
        private final AtomicInteger successfulCalls = new AtomicInteger();
        private final AtomicInteger failedCalls = new AtomicInteger();
        private final AtomicLong totalDuration = new AtomicLong();
        private volatile long minDuration = Long.MAX_VALUE;
        private volatile long maxDuration = 0;

        public OperationMetrics(String operation) {
            this.operation = operation;
        }

        public synchronized void recordOperation(boolean success, long duration) {
            totalCalls.incrementAndGet();
            if (success) {
                successfulCalls.incrementAndGet();
            } else {
                failedCalls.incrementAndGet();
            }
            totalDuration.addAndGet(duration);
            minDuration = Math.min(minDuration, duration);
            maxDuration = Math.max(maxDuration, duration);
        }

        public double getSuccessRate() {
            int total = totalCalls.get();
            return total > 0 ? (double) successfulCalls.get() / total * 100 : 0;
        }

        public double getAverageDuration() {
            int total = totalCalls.get();
            return total > 0 ? (double) totalDuration.get() / total : 0;
        }

        // Getters
        public String getOperation() {
            return operation;
        }

        public int getTotalCalls() {
            return totalCalls.get();
        }

        public int getSuccessfulCalls() {
            return successfulCalls.get();
        }

        public int getFailedCalls() {
            return failedCalls.get();
        }

        public long getTotalDuration() {
            return totalDuration.get();
        }

        public long getMinDuration() {
            return minDuration == Long.MAX_VALUE ? 0 : minDuration;
        }

        public long getMaxDuration() {
            return maxDuration;
        }
    }

    /**
     * Inner class for providing mock results
     */
    private static class SikuliMockProvider {

        public Object getMockResult(String methodName, Object[] args) {
            // Return appropriate mock objects based on method name
            switch (methodName) {
                case "find":
                case "findAll":
                case "wait":
                case "waitVanish":
                    return MockObjectFactory.createMockMatch();

                case "exists":
                    return MockObjectFactory.createMockMatch();

                case "click":
                case "doubleClick":
                case "rightClick":
                    return 1; // Success code

                case "type":
                case "paste":
                    return 1; // Success code

                case "hover":
                case "mouseMove":
                    return 1; // Success code

                case "dragDrop":
                    return 1; // Success code

                case "getScreen":
                    return MockObjectFactory.createMockScreen();

                default:
                    log.warn("No mock implementation for Sikuli method: {}", methodName);
                    return null;
            }
        }
    }

    /**
     * Factory for creating mock Sikuli objects
     */
    private static class MockObjectFactory {

        public static Object createMockMatch() {
            // Return a mock Match object
            // In real implementation, this would return a proper mock
            return new Object() {
                public double getScore() {
                    return 0.95;
                }

                public int getX() {
                    return 100;
                }

                public int getY() {
                    return 100;
                }

                public int getW() {
                    return 50;
                }

                public int getH() {
                    return 50;
                }

                @Override
                public String toString() {
                    return "MockMatch[100,100,50,50]@0.95";
                }
            };
        }

        public static Object createMockScreen() {
            // Return a mock Screen object
            return new Object() {
                public int getID() {
                    return 0;
                }

                public int getW() {
                    return 1920;
                }

                public int getH() {
                    return 1080;
                }

                @Override
                public String toString() {
                    return "MockScreen[0,1920x1080]";
                }
            };
        }
    }
}