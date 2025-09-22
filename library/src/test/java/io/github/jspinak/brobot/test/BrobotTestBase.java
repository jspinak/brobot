package io.github.jspinak.brobot.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.jspinak.brobot.config.mock.MockModeManager;
import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogBuilder;
import io.github.jspinak.brobot.logging.LogCategory;
import io.github.jspinak.brobot.logging.LogLevel;
import io.github.jspinak.brobot.test.jackson.BrobotJacksonMixins;
import io.github.jspinak.brobot.test.jackson.BrobotJacksonTestConfig;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Base test class for all Brobot tests. Provides common setup and configuration for consistent test
 * execution.
 *
 * <p>This base class provides a foundation for testing:
 *
 * <ul>
 *   <li>Common test setup and configuration
 *   <li>Ensures tests work in Docker, CI/CD pipelines, and headless servers
 *   <li>Provides utility methods for test scenarios
 *   <li>Ensures proper mock mode initialization using MockModeManager
 * </ul>
 */
public abstract class BrobotTestBase {

    /**
     * Shared ObjectMapper configured for Brobot serialization tests. This mapper handles all
     * problematic types (Mat, BufferedImage, SikuliX objects).
     */
    protected ObjectMapper testObjectMapper;

    /**
     * Mock BrobotLogger for testing logging functionality.
     * Pre-configured with fluent API mock responses.
     */
    protected BrobotLogger mockLogger;

    /**
     * Mock LogBuilder for building log entries in tests.
     * Pre-configured to support fluent API chaining.
     */
    protected LogBuilder mockLogBuilder;

    /**
     * Global setup for all tests in the class. Sets system properties to ensure headless operation
     * and enables mock mode.
     */
    @BeforeAll
    public static void setUpBrobotEnvironment() {
        // Set test profile to use test-specific configuration
        System.setProperty("spring.profiles.active", "test");

        // Set test mode FIRST to prevent any blocking initialization
        System.setProperty("brobot.test.mode", "true");
        System.setProperty("brobot.test.type", "unit");

        // Disable blocking @PostConstruct operations during tests
        System.setProperty("brobot.diagnostics.image-loading.enabled", "false");
        System.setProperty("brobot.logging.capture.enabled", "false");
        System.setProperty("brobot.startup.verification.enabled", "false");

        // Disable all startup delays
        System.setProperty("brobot.startup.delay", "0");
        System.setProperty("brobot.startup.initial.delay", "0");
        System.setProperty("brobot.startup.ui.stabilization.delay", "0");

        // Ensure headless mode for all tests
        System.setProperty("java.awt.headless", "true");
        System.setProperty("sikuli.Debug", "0");

        // Disable SikuliX splash screen and popups
        System.setProperty("sikuli.console", "false");
        System.setProperty("sikuli.splashscreen", "false");

        // Set mock timings for fast test execution
        System.setProperty("brobot.mock.time.find.first", "0.01");
        System.setProperty("brobot.mock.time.find.all", "0.04");
        System.setProperty("brobot.mock.time.click", "0.01");
        System.setProperty("brobot.mock.time.type", "0.02");
        System.setProperty("brobot.mock.time.move", "0.01");
        System.setProperty("brobot.mock.time.drag", "0.03");
        System.setProperty("brobot.mock.time.vanish", "0.05");
        System.setProperty("brobot.mock.time.wait", "0.01");

        // Enable mock mode using MockModeManager for proper synchronization
        MockModeManager.setMockMode(true);
    }

    /**
     * Setup method that runs before each test. Configures test environment to ensure tests work in
     * all environments. Subclasses should override and call super.setupTest() if they need
     * additional setup.
     */
    @BeforeEach
    public void setupTest() {
        // Enable mock mode by default for testing using MockModeManager
        MockModeManager.setMockMode(true);

        // Initialize properly configured ObjectMapper for serialization tests
        testObjectMapper = BrobotJacksonTestConfig.createTestObjectMapper();
        BrobotJacksonMixins.registerMixins(testObjectMapper);

        // Initialize logging mocks
        mockLogger = createMockLogger();
        mockLogBuilder = createMockLogBuilder();

        // Reset any static state that might interfere between tests
        resetStaticState();
    }

    /**
     * Hook for subclasses to reset any static state between tests. Override this method if your
     * tests use static fields or singletons.
     */
    protected void resetStaticState() {
        // Subclasses can override to reset static state
    }

    /**
     * Utility method to temporarily disable mock mode for specific test scenarios. Remember to
     * re-enable it in a finally block or @AfterEach method.
     */
    protected void disableMockMode() {
        MockModeManager.setMockMode(false);
    }

    /** Re-enables mock mode after it has been disabled. */
    protected void enableMockMode() {
        MockModeManager.setMockMode(true);
    }

    /**
     * Checks if mock mode is currently enabled.
     *
     * @return true if mock mode is enabled, false otherwise
     */
    protected boolean isMockMode() {
        return MockModeManager.isMockMode();
    }

    /**
     * Utility method to log test execution for debugging.
     *
     * @param testInfo Information about the current test
     */
    protected void logTestExecution(TestInfo testInfo) {
        System.out.printf(
                "Running test: %s.%s%n",
                testInfo.getTestClass().map(Class::getSimpleName).orElse("Unknown"),
                testInfo.getTestMethod().map(m -> m.getName()).orElse("unknown"));
    }

    /**
     * Creates a mock BrobotLogger with pre-configured fluent API responses.
     *
     * @return A fully configured mock BrobotLogger
     */
    protected BrobotLogger createMockLogger() {
        BrobotLogger logger = mock(BrobotLogger.class);
        LogBuilder builder = createMockLogBuilder();
        lenient().when(logger.builder(any(LogCategory.class))).thenReturn(builder);
        return logger;
    }

    /**
     * Creates a mock LogBuilder with fluent API support.
     * All builder methods return the builder itself to support method chaining.
     *
     * @return A fully configured mock LogBuilder
     */
    protected LogBuilder createMockLogBuilder() {
        LogBuilder builder = mock(LogBuilder.class);
        // Configure fluent API with lenient stubbing to avoid UnnecessaryStubbingException
        lenient().when(builder.level(any(LogLevel.class))).thenReturn(builder);
        lenient().when(builder.message(anyString())).thenReturn(builder);
        lenient().when(builder.message(anyString(), any())).thenReturn(builder);
        lenient().when(builder.context(anyString(), any())).thenReturn(builder);
        lenient().when(builder.action(anyString(), anyString())).thenReturn(builder);
        lenient().when(builder.duration(any(Duration.class))).thenReturn(builder);
        lenient().when(builder.error(any(Throwable.class))).thenReturn(builder);
        lenient().when(builder.correlationId(anyString())).thenReturn(builder);
        lenient().when(builder.state(anyString())).thenReturn(builder);
        lenient().doNothing().when(builder).log();
        return builder;
    }

    // Logging Verification Utilities

    /**
     * Verify that a log was created with specific category and level.
     *
     * @param category The expected log category
     * @param level The expected log level
     */
    protected void verifyLogged(LogCategory category, LogLevel level) {
        verify(mockLogger).builder(category);
        verify(mockLogBuilder).level(level);
        verify(mockLogBuilder).log();
    }

    /**
     * Verify that an action was logged.
     *
     * @param actionType The type of action that should have been logged
     * @param target The target of the action
     */
    protected void verifyActionLogged(String actionType, String target) {
        verify(mockLogBuilder).action(actionType, target);
        verify(mockLogBuilder).log();
    }

    /**
     * Verify that an error was logged.
     *
     * @param error The error that should have been logged
     */
    protected void verifyErrorLogged(Throwable error) {
        verify(mockLogBuilder).error(error);
        verify(mockLogBuilder).log();
    }

    /**
     * Verify that a specific message was logged.
     *
     * @param message The message that should have been logged
     */
    protected void verifyMessageLogged(String message) {
        verify(mockLogBuilder).message(eq(message), any());
        verify(mockLogBuilder).log();
    }

    /**
     * Reset the mock logger and builder for fresh verification.
     * Useful when testing multiple logging scenarios in a single test.
     */
    protected void resetLoggerMocks() {
        Mockito.reset(mockLogger, mockLogBuilder);
        // Re-configure the fluent API after reset without lenient stubbing for proper invocation
        when(mockLogger.builder(any(LogCategory.class))).thenReturn(mockLogBuilder);
        when(mockLogBuilder.level(any(LogLevel.class))).thenReturn(mockLogBuilder);
        when(mockLogBuilder.message(anyString())).thenReturn(mockLogBuilder);
        when(mockLogBuilder.message(anyString(), any())).thenReturn(mockLogBuilder);
        when(mockLogBuilder.context(anyString(), any())).thenReturn(mockLogBuilder);
        when(mockLogBuilder.action(anyString(), anyString())).thenReturn(mockLogBuilder);
        when(mockLogBuilder.duration(any(Duration.class))).thenReturn(mockLogBuilder);
        when(mockLogBuilder.error(any(Throwable.class))).thenReturn(mockLogBuilder);
        when(mockLogBuilder.correlationId(anyString())).thenReturn(mockLogBuilder);
        when(mockLogBuilder.state(anyString())).thenReturn(mockLogBuilder);
        doNothing().when(mockLogBuilder).log();
    }
}
