package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.test.extensions.RetryExtension;
import io.github.jspinak.brobot.test.utils.ConcurrentTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for tests that need to be thread-safe for parallel execution.
 * Provides utilities and safeguards for concurrent test execution.
 */
@ExtendWith(RetryExtension.class)
@Execution(ExecutionMode.CONCURRENT)
@Timeout(value = 30, unit = TimeUnit.SECONDS) // Default timeout for all tests
public abstract class ConcurrentTestBase extends BrobotTestBase {
    
    /**
     * Track active test instances to detect parallel execution issues
     */
    private static final Set<String> activeTests = 
        Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    /**
     * Counter for unique test instance IDs
     */
    private static final AtomicInteger testIdCounter = new AtomicInteger(0);
    
    /**
     * Unique ID for this test instance
     */
    protected final int testInstanceId = testIdCounter.incrementAndGet();
    
    /**
     * Thread-local storage for test-specific data
     */
    protected final ThreadLocal<TestContext> testContext = 
        ThreadLocal.withInitial(TestContext::new);
    
    /**
     * Executor for async operations in tests
     */
    protected ExecutorService testExecutor;
    
    /**
     * Current test information
     */
    protected TestInfo testInfo;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Track active tests
        String testName = getTestName();
        if (!activeTests.add(testName)) {
            System.err.println("[CONCURRENT] Warning: Test " + testName + 
                " is already running in parallel!");
        }
        
        // Initialize test executor
        testExecutor = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setName("test-" + testInstanceId + "-" + thread.getId());
            thread.setDaemon(true);
            return thread;
        });
        
        // Initialize test context
        TestContext ctx = testContext.get();
        ctx.reset();
        ctx.testName = testName;
        ctx.startTime = System.currentTimeMillis();
    }
    
    @AfterEach
    public void tearDownConcurrent() {
        try {
            // Clean up test executor
            if (testExecutor != null) {
                ConcurrentTestHelper.shutdownExecutor(testExecutor, Duration.ofSeconds(5));
            }
            
            // Clear thread-local context
            testContext.remove();
            
            // Remove from active tests
            activeTests.remove(getTestName());
            
            // Log test duration
            TestContext ctx = testContext.get();
            long duration = System.currentTimeMillis() - ctx.startTime;
            if (duration > 5000) {
                System.err.println("[CONCURRENT] Warning: Test " + getTestName() + 
                    " took " + duration + "ms to complete");
            }
        } finally {
            // Cleanup happens automatically via JUnit lifecycle
        }
    }
    
    /**
     * Get a unique test name for tracking
     */
    protected String getTestName() {
        if (testInfo != null) {
            return testInfo.getTestClass().map(Class::getSimpleName).orElse("Unknown") + 
                   "." + testInfo.getTestMethod().map(m -> m.getName()).orElse("unknown");
        }
        return "Test-" + testInstanceId;
    }
    
    /**
     * Wait for a condition with default timeout
     */
    protected boolean waitFor(java.util.function.BooleanSupplier condition) {
        return ConcurrentTestHelper.waitForCondition(condition, Duration.ofSeconds(5));
    }
    
    /**
     * Wait for a condition with custom timeout
     */
    protected boolean waitFor(java.util.function.BooleanSupplier condition, Duration timeout) {
        return ConcurrentTestHelper.waitForCondition(condition, timeout);
    }
    
    /**
     * Execute an async operation with timeout
     */
    protected <T> T executeAsync(java.util.concurrent.Callable<T> task, Duration timeout) 
            throws Exception {
        return testExecutor.submit(task).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }
    
    /**
     * Assert eventually - retry assertion until it passes or timeout
     */
    protected void assertEventually(Runnable assertion, Duration timeout) {
        long endTime = System.currentTimeMillis() + timeout.toMillis();
        AssertionError lastError = null;
        
        while (System.currentTimeMillis() < endTime) {
            try {
                assertion.run();
                return; // Assertion passed
            } catch (AssertionError e) {
                lastError = e;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError("Interrupted while waiting for assertion", ie);
                }
            }
        }
        
        throw new AssertionError("Assertion did not pass within " + timeout, lastError);
    }
    
    /**
     * Context for thread-local test data
     */
    protected static class TestContext {
        String testName;
        long startTime;
        java.util.Map<String, Object> data = new ConcurrentHashMap<>();
        
        void reset() {
            data.clear();
            testName = null;
            startTime = 0;
        }
        
        public void put(String key, Object value) {
            data.put(key, value);
        }
        
        @SuppressWarnings("unchecked")
        public <T> T get(String key) {
            return (T) data.get(key);
        }
    }
    
    /**
     * Resource locks for tests that need exclusive access to shared resources
     */
    public static class ResourceLocks {
        public static final String FILE_SYSTEM = "file-system";
        public static final String NETWORK = "network";
        public static final String DATABASE = "database";
        public static final String DISPLAY = "display";
        public static final String SIKULI = "sikuli";
    }
}