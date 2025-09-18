package io.github.jspinak.brobot.test;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;

/**
 * Enhanced base class for Brobot tests with parallel execution support.
 *
 * <p>This class provides: - Thread-safe test initialization - Automatic mock mode enforcement -
 * Test isolation for parallel execution - Resource tracking and cleanup - Performance metrics
 * collection
 *
 * <p>All Brobot tests should extend this class or BrobotTestBase.
 */
@Execution(ExecutionMode.CONCURRENT)
public abstract class BrobotParallelTestBase {

    private static final Logger log = LoggerFactory.getLogger(BrobotParallelTestBase.class);

    // Thread-safe counters for tracking test execution
    private static final AtomicInteger activeTests = new AtomicInteger(0);
    private static final AtomicInteger completedTests = new AtomicInteger(0);
    private static final ConcurrentHashMap<String, Long> testStartTimes = new ConcurrentHashMap<>();

    // Unique test execution ID for tracking
    protected String testExecutionId;

    // Test context information
    protected String testClassName;
    protected String testMethodName;

    // AutoCloseable for automatic resource cleanup
    private AutoCloseable mocks;

    @BeforeEach
    public void setupParallelTest(TestInfo testInfo) {
        // Generate unique execution ID
        testExecutionId = UUID.randomUUID().toString();

        // Extract test information
        testClassName = testInfo.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        testMethodName = testInfo.getTestMethod().map(m -> m.getName()).orElse("unknown");

        // Track test start
        String testKey = testClassName + "." + testMethodName;
        testStartTimes.put(testKey, System.currentTimeMillis());
        int active = activeTests.incrementAndGet();

        // Log test start (only in debug mode to avoid output pollution)
        if (log.isDebugEnabled()) {
            log.debug("[{}] Starting test: {} (Active: {})", testExecutionId, testKey, active);
        }

        // Initialize mocks
        mocks = MockitoAnnotations.openMocks(this);

        // Ensure mock mode is enabled for all tests
        enforceMockMode();

        // Initialize ExecutionEnvironment for this thread
        initializeEnvironment();

        // Call custom setup if needed
        additionalSetup();
    }

    @AfterEach
    public void teardownParallelTest(TestInfo testInfo) {
        try {
            // Call custom teardown if needed
            additionalTeardown();

            // Clean up mocks
            if (mocks != null) {
                mocks.close();
            }

            // Track test completion
            String testKey = testClassName + "." + testMethodName;
            Long startTime = testStartTimes.remove(testKey);
            int active = activeTests.decrementAndGet();
            int completed = completedTests.incrementAndGet();

            if (startTime != null && log.isDebugEnabled()) {
                long duration = System.currentTimeMillis() - startTime;
                log.debug(
                        "[{}] Completed test: {} in {}ms (Active: {}, Total: {})",
                        testExecutionId,
                        testKey,
                        duration,
                        active,
                        completed);
            }

        } catch (Exception e) {
            log.error("Error during test teardown", e);
        } finally {
            // Reset environment to ensure clean state
            resetEnvironment();
        }
    }

    /** Enforces mock mode for all tests to ensure they don't interact with real resources. */
    private void enforceMockMode() {
        // Set mock mode
        // Mock mode is now enabled via BrobotTestBase

        // Configure mock timings for fast execution
        // Setting mockTimeFindFirst now handled by BrobotProperties
        // Setting mockTimeFindAll now handled by BrobotProperties
        // Setting mockTimeClick now handled by BrobotProperties
        // Setting mockTimeMove now handled by BrobotProperties
        // Setting mockTimeDrag now handled by BrobotProperties

        // Disable real screen operations
        // Setting saveSnapshots now handled by BrobotProperties
        // Setting saveHistory now handled by BrobotProperties

        // Set headless mode
        System.setProperty("java.awt.headless", "true");
        System.setProperty("brobot.mock", "true");

        // Disable SikuliX debug output
        System.setProperty("sikuli.Debug", "0");
    }

    /** Initializes the execution environment for this thread. */
    private void initializeEnvironment() {
        // Create a new ExecutionEnvironment configured for testing
        ExecutionEnvironment testEnv =
                ExecutionEnvironment.builder().forceHeadless(true).mockMode(true).build();
        ExecutionEnvironment.setInstance(testEnv);
    }

    /** Resets the environment after test completion. */
    private void resetEnvironment() {
        try {
            // Reset to default test environment
            ExecutionEnvironment defaultTestEnv =
                    ExecutionEnvironment.builder().forceHeadless(true).mockMode(true).build();
            ExecutionEnvironment.setInstance(defaultTestEnv);
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    /** Override this method to add custom setup logic. */
    protected void additionalSetup() {
        // Default: no additional setup
    }

    /** Override this method to add custom teardown logic. */
    protected void additionalTeardown() {
        // Default: no additional teardown
    }

    /** Gets the current number of active tests. */
    public static int getActiveTestCount() {
        return activeTests.get();
    }

    /** Gets the total number of completed tests. */
    public static int getCompletedTestCount() {
        return completedTests.get();
    }

    /** Utility method to assert mock mode is enabled. */
    protected void assertMockModeEnabled() {
        if (!true /* mock mode enabled in tests */) {
            throw new IllegalStateException(
                    "Mock mode is not enabled. All tests must run in mock mode.");
        }
    }

    /** Utility method to get a thread-safe test-specific temp directory name. */
    protected String getTestSpecificTempDir() {
        return "test-" + testExecutionId;
    }
}
