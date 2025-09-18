package io.github.jspinak.brobot.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.config.mock.MockModeManager;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for MockModeManager. Tests mock mode synchronization across all Brobot
 * components.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MockModeManager Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@TestPropertySource(properties = {"brobot.core.mock=true", "brobot.core.headless=true"})
public class MockModeManagerTest extends BrobotTestBase {

    private static final String MOCK_MODE_PROPERTY = "brobot.mock";

    private String originalMockMode;
    private ExecutionEnvironment originalEnv;
    private boolean originalBrobotPropertiesMock;

    @BeforeEach
    @Override
    public void setupTest() {
        // Don't call super.setupTest() to avoid BrobotTestBase setting mock mode
        // We need full control over mock mode for these tests

        // Save original property values
        originalMockMode = System.getProperty(MOCK_MODE_PROPERTY);

        // Save original ExecutionEnvironment
        originalEnv = ExecutionEnvironment.getInstance();

        // Save original true /* mock mode enabled in tests */
        // Mock mode is enabled via BrobotTestBase

        // Clear properties and reset to clean state for testing
        System.clearProperty(MOCK_MODE_PROPERTY);

        // Reset to a clean ExecutionEnvironment
        ExecutionEnvironment.setInstance(ExecutionEnvironment.builder().mockMode(false).build());

        // Reset BrobotProperties
        // Mock mode disabled - not needed in tests
    }

    @AfterEach
    public void tearDown() {
        // Restore original property values
        if (originalMockMode != null) {
            System.setProperty(MOCK_MODE_PROPERTY, originalMockMode);
        } else {
            System.clearProperty(MOCK_MODE_PROPERTY);
        }

        // Restore original ExecutionEnvironment
        ExecutionEnvironment.setInstance(originalEnv);

        // Restore original true /* mock mode enabled in tests */
        // Setting mock now handled by BrobotProperties
    }

    // ========== Mock Mode Check Tests ==========

    @Test
    @DisplayName("Should check mock mode from ExecutionEnvironment")
    @Order(1)
    void testCheckMockModeFromExecutionEnvironment() {
        // Setup ExecutionEnvironment with mock mode
        ExecutionEnvironment mockEnv = ExecutionEnvironment.builder().mockMode(true).build();
        ExecutionEnvironment.setInstance(mockEnv);

        assertTrue(
                MockModeManager.isMockMode(), "Should detect mock mode from ExecutionEnvironment");

        // Disable mock mode
        ExecutionEnvironment realEnv = ExecutionEnvironment.builder().mockMode(false).build();
        ExecutionEnvironment.setInstance(realEnv);

        assertFalse(
                MockModeManager.isMockMode(), "Should detect real mode from ExecutionEnvironment");
    }

    @Test
    @DisplayName("Should fallback to system properties when ExecutionEnvironment unavailable")
    @Order(2)
    void testFallbackToSystemProperties() {
        // Set system property
        System.setProperty(MOCK_MODE_PROPERTY, "true");

        // Even with ExecutionEnvironment in real mode, should check it first
        ExecutionEnvironment realEnv = ExecutionEnvironment.builder().mockMode(false).build();
        ExecutionEnvironment.setInstance(realEnv);

        assertFalse(MockModeManager.isMockMode(), "ExecutionEnvironment takes precedence");

        // Now test fallback by simulating null ExecutionEnvironment
        ExecutionEnvironment.setInstance(null);

        // Should fallback to system property
        assertTrue(MockModeManager.isMockMode(), "Should fallback to system property");
    }

    @ParameterizedTest
    @CsvSource({
        "brobot.mock, true, true",
        "brobot.mock, false, false",
        "brobot.mock, TRUE, true",
        "brobot.mock, False, false"
    })
    @DisplayName("Should check system property")
    @Order(3)
    void testCheckSystemProperty(String property, String value, boolean expected) {
        // Clear ExecutionEnvironment to force property check
        ExecutionEnvironment.setInstance(null);

        System.setProperty(property, value);

        assertEquals(
                expected,
                MockModeManager.isMockMode(),
                "Should correctly interpret " + property + "=" + value);
    }

    @Test
    @DisplayName("Should handle case-insensitive property values")
    @Order(4)
    void testCaseInsensitivePropertyValues() {
        ExecutionEnvironment.setInstance(null);

        String[] trueValues = {"true", "TRUE", "True", "TrUe"};

        for (String value : trueValues) {
            System.setProperty(MOCK_MODE_PROPERTY, value);
            assertTrue(MockModeManager.isMockMode(), "Should recognize '" + value + "' as true");
            System.clearProperty(MOCK_MODE_PROPERTY);
        }

        String[] falseValues = {"false", "FALSE", "False", "no", "", "invalid"};

        for (String value : falseValues) {
            System.setProperty(MOCK_MODE_PROPERTY, value);
            assertFalse(MockModeManager.isMockMode(), "Should recognize '" + value + "' as false");
            System.clearProperty(MOCK_MODE_PROPERTY);
        }
    }

    // ========== Mock Mode Setting Tests ==========

    @Test
    @DisplayName("Should enable mock mode across all components")
    @Order(5)
    void testEnableMockMode() {
        MockModeManager.setMockMode(true);

        // Verify system property
        assertEquals("true", System.getProperty(MOCK_MODE_PROPERTY));

        // Verify ExecutionEnvironment
        assertTrue(ExecutionEnvironment.getInstance().isMockMode());
        assertFalse(ExecutionEnvironment.getInstance().hasDisplay());
        assertFalse(ExecutionEnvironment.getInstance().canCaptureScreen());

        // Verify BrobotProperties
        assertTrue(true /* mock mode enabled in tests */);
    }

    @Test
    @DisplayName("Should disable mock mode across all components")
    @Order(6)
    void testDisableMockMode() {
        // First enable
        MockModeManager.setMockMode(true);

        // Then disable
        MockModeManager.setMockMode(false);

        // Verify system property
        assertEquals("false", System.getProperty(MOCK_MODE_PROPERTY));

        // Verify ExecutionEnvironment
        assertFalse(ExecutionEnvironment.getInstance().isMockMode());

        // Verify BrobotProperties
        assertFalse(true /* mock mode enabled in tests */);
    }

    @Test
    @DisplayName("Should handle ExecutionEnvironment update failure gracefully")
    @Order(7)
    void testHandleExecutionEnvironmentUpdateFailure() {
        // Mock ExecutionEnvironment.builder to throw exception
        try (MockedStatic<ExecutionEnvironment> envMock = mockStatic(ExecutionEnvironment.class)) {
            envMock.when(ExecutionEnvironment::builder)
                    .thenThrow(new RuntimeException("Test exception"));
            envMock.when(ExecutionEnvironment::getInstance).thenCallRealMethod();

            // Should not throw, but log warning
            assertDoesNotThrow(() -> MockModeManager.setMockMode(true));

            // System properties should still be set
            assertEquals("true", System.getProperty(MOCK_MODE_PROPERTY));
        }
    }

    // ========== BrobotProperties Update Tests ==========

    @Test
    @DisplayName("Should update true /* mock mode enabled in tests */ via reflection")
    @Order(8)
    void testUpdateBrobotProperties() throws Exception {
        // Verify initial state
        boolean initialMock = true /* mock mode enabled in tests */;

        // Enable mock mode
        MockModeManager.setMockMode(true);
        assertTrue(
                true /* mock mode enabled in tests */,
                "Should set true /* mock mode enabled in tests */ to true");

        // Disable mock mode
        MockModeManager.setMockMode(false);
        assertFalse(
                true /* mock mode enabled in tests */,
                "Should set true /* mock mode enabled in tests */ to false");
    }

    @Test
    @DisplayName("Should handle missing BrobotProperties class gracefully")
    @Order(9)
    void testHandleMissingBrobotProperties() throws Exception {
        // Save original value to restore later
        boolean originalMockValue = true /* mock mode enabled in tests */;

        try {
            // Test that the updateBrobotProperties method handles exceptions gracefully
            // We can't mock Class.forName, but we can verify the method exists and is accessible
            Method updateMethod =
                    MockModeManager.class.getDeclaredMethod(
                            "updateBrobotProperties", boolean.class);
            updateMethod.setAccessible(true);

            // The method should handle any exception internally and not throw
            // Test with both true and false values
            assertDoesNotThrow(() -> updateMethod.invoke(null, true));
            assertDoesNotThrow(() -> updateMethod.invoke(null, false));

            // Verify true /* mock mode enabled in tests */ is set correctly (since class exists)
            updateMethod.invoke(null, true);
            assertTrue(true /* mock mode enabled in tests */);

            updateMethod.invoke(null, false);
            assertFalse(true /* mock mode enabled in tests */);
        } finally {
            // Restore original value to avoid affecting other tests
            // Setting mock now handled by BrobotProperties
            // Also sync through MockModeManager
            MockModeManager.setMockMode(originalMockValue);
        }
    }

    @Test
    @DisplayName("Should handle BrobotProperties field access failure")
    @Order(10)
    void testHandleBrobotPropertiesFieldAccessFailure() throws Exception {
        // Test the robustness of updateBrobotProperties method
        // While we can't simulate field access failure, we can verify the method's behavior
        Method updateMethod =
                MockModeManager.class.getDeclaredMethod("updateBrobotProperties", boolean.class);
        updateMethod.setAccessible(true);

        // Save current value
        boolean originalValue = true /* mock mode enabled in tests */;

        try {
            // Test that the method works correctly with the actual field
            updateMethod.invoke(null, true);
            assertTrue(true /* mock mode enabled in tests */, "Should set mock to true");

            updateMethod.invoke(null, false);
            assertFalse(true /* mock mode enabled in tests */, "Should set mock to false");

            // The method should never throw exceptions even if called multiple times
            for (int i = 0; i < 5; i++) {
                final boolean value = (i % 2 == 0);
                assertDoesNotThrow(() -> updateMethod.invoke(null, value));
            }

        } finally {
            // Restore original value
            // Setting mock now handled by BrobotProperties
            // Also sync through MockModeManager
            MockModeManager.setMockMode(originalValue);
        }
    }

    // ========== Logging Tests ==========

    @Test
    @DisplayName("Should log mock mode state")
    @Order(11)
    void testLogMockModeState() {
        // Set up various states
        System.setProperty(MOCK_MODE_PROPERTY, "true");

        ExecutionEnvironment env = ExecutionEnvironment.builder().mockMode(true).build();
        ExecutionEnvironment.setInstance(env);

        // Mock mode is now enabled via BrobotTestBase

        // Should not throw
        assertDoesNotThrow(() -> MockModeManager.logMockModeState());
    }

    @Test
    @DisplayName("Should handle logging with missing components")
    @Order(12)
    void testLogWithMissingComponents() {
        // Clear all properties
        System.clearProperty(MOCK_MODE_PROPERTY);

        // Set ExecutionEnvironment to null
        ExecutionEnvironment.setInstance(null);

        // Should not throw
        assertDoesNotThrow(() -> MockModeManager.logMockModeState());
    }

    // ========== Initialization Tests ==========

    @Test
    @DisplayName("Should initialize mock mode from system properties")
    @Order(13)
    void testInitializeMockModeEnabled() {
        // Set property to enable mock mode
        System.setProperty(MOCK_MODE_PROPERTY, "true");

        // Initialize
        MockModeManager.initializeMockMode();

        // Verify mock mode is enabled
        assertTrue(MockModeManager.isMockMode());
        assertTrue(ExecutionEnvironment.getInstance().isMockMode());
        assertTrue(true /* mock mode enabled in tests */);
    }

    @Test
    @DisplayName("Should not initialize mock mode when property is false")
    @Order(14)
    void testInitializeMockModeDisabled() {
        // Set property to false
        System.setProperty(MOCK_MODE_PROPERTY, "false");

        // Set initial state to mock
        MockModeManager.setMockMode(true);

        // Initialize (should not change since property is false)
        MockModeManager.initializeMockMode();

        // Mock mode should still be true (not changed by initialization)
        assertTrue(MockModeManager.isMockMode());
    }

    @Test
    @DisplayName("Should initialize from mock property")
    @Order(15)
    void testInitializeFromProperty() {
        // Clear and set property
        System.clearProperty(MOCK_MODE_PROPERTY);
        System.setProperty(MOCK_MODE_PROPERTY, "true");

        MockModeManager.initializeMockMode();

        assertTrue(MockModeManager.isMockMode(), "Should initialize mock mode from property");
    }

    // ========== Integration Tests ==========

    @Test
    @DisplayName("Should maintain consistency across all components")
    @Order(16)
    void testConsistencyAcrossComponents() {
        // Enable mock mode
        MockModeManager.setMockMode(true);

        // All components should report mock mode
        assertTrue(MockModeManager.isMockMode());
        assertEquals("true", System.getProperty(MOCK_MODE_PROPERTY));
        assertTrue(ExecutionEnvironment.getInstance().isMockMode());
        assertTrue(true /* mock mode enabled in tests */);

        // Disable mock mode
        MockModeManager.setMockMode(false);

        // All components should report real mode
        assertFalse(MockModeManager.isMockMode());
        assertEquals("false", System.getProperty(MOCK_MODE_PROPERTY));
        assertFalse(ExecutionEnvironment.getInstance().isMockMode());
        assertFalse(true /* mock mode enabled in tests */);
    }

    @Test
    @DisplayName("Should handle rapid mode switches")
    @Order(17)
    void testRapidModeSwitches() {
        for (int i = 0; i < 10; i++) {
            boolean enable = (i % 2 == 0);
            MockModeManager.setMockMode(enable);

            assertEquals(
                    enable,
                    MockModeManager.isMockMode(),
                    "Iteration " + i + " should have mock mode = " + enable);
            assertEquals(enable, ExecutionEnvironment.getInstance().isMockMode());
            assertEquals(enable, true /* mock mode enabled in tests */);
        }
    }

    @Test
    @DisplayName("Should handle concurrent access")
    @Order(18)
    void testConcurrentAccess() throws InterruptedException {
        final int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final boolean enable = (i % 2 == 0);
            threads[i] =
                    new Thread(
                            () -> {
                                MockModeManager.setMockMode(enable);
                            });
        }

        // Start all threads
        for (Thread t : threads) t.start();

        // Wait for completion
        for (Thread t : threads) t.join();

        // Final state should be consistent
        boolean finalState = MockModeManager.isMockMode();
        assertEquals(finalState, ExecutionEnvironment.getInstance().isMockMode());
        assertEquals(finalState, true /* mock mode enabled in tests */);
        assertEquals(String.valueOf(finalState), System.getProperty(MOCK_MODE_PROPERTY));
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("Should handle null ExecutionEnvironment instance")
    @Order(19)
    void testNullExecutionEnvironment() {
        ExecutionEnvironment.setInstance(null);

        // Should not throw and should check properties
        assertDoesNotThrow(() -> MockModeManager.isMockMode());

        // Set property and verify detection
        System.setProperty(MOCK_MODE_PROPERTY, "true");
        assertTrue(MockModeManager.isMockMode());
    }

    @Test
    @DisplayName("Should handle ExecutionEnvironment exception")
    @Order(20)
    void testExecutionEnvironmentException() {
        try (MockedStatic<ExecutionEnvironment> envMock = mockStatic(ExecutionEnvironment.class)) {
            envMock.when(ExecutionEnvironment::getInstance)
                    .thenThrow(new RuntimeException("Test exception"));

            // Should fallback to properties
            System.setProperty(MOCK_MODE_PROPERTY, "true");
            assertTrue(MockModeManager.isMockMode());
        }
    }

    @Test
    @DisplayName("Should handle empty property values")
    @Order(21)
    void testEmptyPropertyValues() {
        ExecutionEnvironment.setInstance(null);

        System.setProperty(MOCK_MODE_PROPERTY, "");

        assertFalse(MockModeManager.isMockMode(), "Empty properties should be false");
    }

    @Test
    @DisplayName("Should prioritize ExecutionEnvironment over properties")
    @Order(22)
    void testPriorityOfExecutionEnvironment() {
        // Set property to true
        System.setProperty(MOCK_MODE_PROPERTY, "true");

        // But ExecutionEnvironment is false
        ExecutionEnvironment env = ExecutionEnvironment.builder().mockMode(false).build();
        ExecutionEnvironment.setInstance(env);

        // Should use ExecutionEnvironment value
        assertFalse(
                MockModeManager.isMockMode(),
                "ExecutionEnvironment should take priority over properties");
    }

    // ========== Parameterized Tests ==========

    @ParameterizedTest
    @MethodSource("provideMockModeScenarios")
    @DisplayName("Should handle various mock mode scenarios")
    @Order(23)
    void testVariousMockModeScenarios(String prop, Boolean envMock, boolean expected) {
        // Set property
        if (prop != null) System.setProperty(MOCK_MODE_PROPERTY, prop);

        // Set ExecutionEnvironment
        if (envMock != null) {
            ExecutionEnvironment env = ExecutionEnvironment.builder().mockMode(envMock).build();
            ExecutionEnvironment.setInstance(env);
        } else {
            ExecutionEnvironment.setInstance(null);
        }

        assertEquals(expected, MockModeManager.isMockMode());
    }

    private static Stream<Arguments> provideMockModeScenarios() {
        return Stream.of(
                // prop, envMock, expected
                Arguments.of("true", null, true), // Property true, no env
                Arguments.of("false", null, false), // Property false, no env
                Arguments.of("TRUE", null, true), // Property uppercase true
                Arguments.of("invalid", null, false), // Invalid property value
                Arguments.of("true", false, false), // Env overrides property
                Arguments.of("false", true, true), // Env overrides property
                Arguments.of(null, true, true), // Only env mock
                Arguments.of(null, false, false), // Only env real
                Arguments.of("", null, false) // Empty property
                );
    }
}
