package io.github.jspinak.brobot.aspects.annotations;

import java.lang.annotation.*;

/**
 * Marks a method or class for enhanced performance monitoring.
 *
 * <p>Methods annotated with @Monitored will have detailed performance metrics collected by the
 * PerformanceMonitoringAspect, including custom thresholds and alerting.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Monitored(threshold = 5000, trackMemory = true)
 * public void performExpensiveOperation() {
 *     // Operation that should complete within 5 seconds
 * }
 *
 * @Monitored(name = "UserLogin", tags = {"authentication", "critical"})
 * public User loginUser(String username, String password) {
 *     // Custom name and tags for categorization
 * }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Monitored {

    /** Custom name for the monitored operation. Default is the method signature. */
    String name() default "";

    /**
     * Performance threshold in milliseconds. Operations exceeding this will trigger alerts. Default
     * is -1 (use global threshold).
     */
    long threshold() default -1;

    /** Whether to track memory usage for this operation. Default is false. */
    boolean trackMemory() default false;

    /** Whether to include method parameters in logs. Default is false for security. */
    boolean logParameters() default false;

    /** Whether to include return value in logs. Default is false for security. */
    boolean logResult() default false;

    /** Tags for categorizing the operation. Useful for filtering and reporting. */
    String[] tags() default {};

    /**
     * Sampling rate for this operation (0.0 to 1.0). 1.0 means monitor every call, 0.1 means
     * monitor 10% of calls. Default is 1.0.
     */
    double samplingRate() default 1.0;

    /** Whether to create a trace span for distributed tracing. Default is false. */
    boolean createSpan() default false;

    /** Custom metrics to capture. */
    String[] customMetrics() default {};
}
