package io.github.jspinak.brobot.test.benchmark;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.extension.*;

/**
 * JUnit 5 extension for automatic performance benchmarking of tests.
 *
 * <p>Usage: - Add @Benchmark to individual test methods - Add @ExtendWith(BenchmarkExtension.class)
 * to test class - Or use @Benchmark at class level to benchmark all tests
 */
public class BenchmarkExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final String START_TIME = "start_time";

    /** Annotation to mark tests for benchmarking. */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Benchmark {
        /** Warning threshold in milliseconds. */
        long warningMs() default -1;

        /** Error threshold in milliseconds. */
        long errorMs() default -1;

        /** Whether to include in reports. */
        boolean report() default true;

        /** Custom name for the benchmark. */
        String name() default "";
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        if (shouldBenchmark(context)) {
            context.getStore(ExtensionContext.Namespace.create(getClass()))
                    .put(START_TIME, Instant.now());
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (shouldBenchmark(context)) {
            Instant startTime =
                    context.getStore(ExtensionContext.Namespace.create(getClass()))
                            .remove(START_TIME, Instant.class);

            if (startTime != null) {
                long duration = Duration.between(startTime, Instant.now()).toNanos();
                recordBenchmark(context, duration);
            }
        }
    }

    private boolean shouldBenchmark(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        Class<?> testClass = context.getRequiredTestClass();

        // Check if method or class has @Benchmark annotation
        return method.isAnnotationPresent(Benchmark.class)
                || testClass.isAnnotationPresent(Benchmark.class);
    }

    private void recordBenchmark(ExtensionContext context, long durationNanos) {
        Method method = context.getRequiredTestMethod();
        Class<?> testClass = context.getRequiredTestClass();

        // Get benchmark annotation (from method or class)
        Benchmark benchmark = method.getAnnotation(Benchmark.class);
        if (benchmark == null) {
            benchmark = testClass.getAnnotation(Benchmark.class);
        }

        // Determine benchmark name
        String benchmarkName =
                benchmark.name().isEmpty()
                        ? testClass.getSimpleName() + "." + method.getName()
                        : benchmark.name();

        // Set thresholds if specified
        if (benchmark.warningMs() > 0 || benchmark.errorMs() > 0) {
            long warning = benchmark.warningMs() > 0 ? benchmark.warningMs() : Long.MAX_VALUE;
            long error = benchmark.errorMs() > 0 ? benchmark.errorMs() : Long.MAX_VALUE;
            PerformanceBenchmark.setThreshold(benchmarkName, warning, error);
        }

        // Record the result
        PerformanceBenchmark.BenchmarkResult result =
                new PerformanceBenchmark.BenchmarkResult(benchmarkName, durationNanos);

        // Add metadata
        result.addMetadata("testClass", testClass.getName());
        result.addMetadata("testMethod", method.getName());
        result.addMetadata("displayName", context.getDisplayName());

        // Log if reporting is enabled
        if (benchmark.report()) {
            long millis = durationNanos / 1_000_000;
            System.out.printf("Benchmark: %s completed in %d ms%n", benchmarkName, millis);
        }
    }
}
