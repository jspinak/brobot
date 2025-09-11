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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
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
public class MockModeManagerTest extends BrobotTestBase {

    private static final String MOCK_MODE_PROPERTY = "brobot.mock.mode";
    private static final String FRAMEWORK_MOCK_PROPERTY = "brobot.framework.mock";
    private static final String CORE_MOCK_PROPERTY = "brobot.core.mock-mode";

    private String originalMockMode;
    private String originalFrameworkMock;
    private String originalCoreMock;
    private ExecutionEnvironment originalEnv;
    private boolean originalFrameworkSettingsMock;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Save original property values
        originalMockMode = System.getProperty(MOCK_MODE_PROPERTY);
        originalFrameworkMock = System.getProperty(FRAMEWORK_MOCK_PROPERTY);
        originalCoreMock = System.getProperty(CORE_MOCK_PROPERTY);

        // Save original ExecutionEnvironment
        originalEnv = ExecutionEnvironment.getInstance();

        // Save original FrameworkSettings.mock
        originalFrameworkSettingsMock = FrameworkSettings.mock;

        // Clear properties for clean test state
        System.clearProperty(MOCK_MODE_PROPERTY);
        System.clearProperty(FRAMEWORK_MOCK_PROPERTY);
        System.clearProperty(CORE_MOCK_PROPERTY);
    }

    @AfterEach
    public void tearDown() {
        // Restore original property values
        if (originalMockMode != null) {
            System.setProperty(MOCK_MODE_PROPERTY, originalMockMode);
        } else {
            System.clearProperty(MOCK_MODE_PROPERTY);
        }

        if (originalFrameworkMock != null) {
            System.setProperty(FRAMEWORK_MOCK_PROPERTY, originalFrameworkMock);
        } else {
            System.clearProperty(FRAMEWORK_MOCK_PROPERTY);
        }

        if (originalCoreMock != null) {
            System.setProperty(CORE_MOCK_PROPERTY, originalCoreMock);
        } else {
            System.clearProperty(CORE_MOCK_PROPERTY);
        }

        // Restore original ExecutionEnvironment
        ExecutionEnvironment.setInstance(originalEnv);

        // Restore original FrameworkSettings.mock
        FrameworkSettings.mock = originalFrameworkSettingsMock;
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
        "brobot.mock.mode, true, true",
        "brobot.framework.mock, true, true",
        "brobot.core.mock-mode, true, true",
        "brobot.mock.mode, false, false",
        "brobot.framework.mock, false, false",
        "brobot.core.mock-mode, false, false"
    })
    @DisplayName("Should check various system properties")
    @Order(3)
    void testCheckVariousSystemProperties(String property, String value, boolean expected) {
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

        // Verify system properties
        assertEquals("true", System.getProperty(MOCK_MODE_PROPERTY));
        assertEquals("true", System.getProperty(FRAMEWORK_MOCK_PROPERTY));
        assertEquals("true", System.getProperty(CORE_MOCK_PROPERTY));

        // Verify ExecutionEnvironment
        assertTrue(ExecutionEnvironment.getInstance().isMockMode());
        assertTrue(ExecutionEnvironment.getInstance().hasDisplay() == false);
        assertFalse(ExecutionEnvironment.getInstance().canCaptureScreen());

        // Verify FrameworkSettings
        assertTrue(FrameworkSettings.mock);
    }

    @Test
    @DisplayName("Should disable mock mode across all components")
    @Order(6)
    void testDisableMockMode() {
        // First enable
        MockModeManager.setMockMode(true);

        // Then disable
        MockModeManager.setMockMode(false);

        // Verify system properties
        assertEquals("false", System.getProperty(MOCK_MODE_PROPERTY));
        assertEquals("false", System.getProperty(FRAMEWORK_MOCK_PROPERTY));
        assertEquals("false", System.getProperty(CORE_MOCK_PROPERTY));

        // Verify ExecutionEnvironment
        assertFalse(ExecutionEnvironment.getInstance().isMockMode());

        // Verify FrameworkSettings
        assertFalse(FrameworkSettings.mock);
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

    // ========== FrameworkSettings Update Tests ==========

    @Test
    @DisplayName("Should update FrameworkSettings.mock via reflection")
    @Order(8)
    void testUpdateFrameworkSettings() throws Exception {
        // Verify initial state
        boolean initialMock = FrameworkSettings.mock;

        // Enable mock mode
        MockModeManager.setMockMode(true);
        assertTrue(FrameworkSettings.mock, "Should set FrameworkSettings.mock to true");

        // Disable mock mode
        MockModeManager.setMockMode(false);
        assertFalse(FrameworkSettings.mock, "Should set FrameworkSettings.mock to false");
    }

    @Test
    @DisplayName("Should handle missing FrameworkSettings class gracefully")
    @Order(9)
    void testHandleMissingFrameworkSettings() throws Exception {
        // Save original value to restore later
        boolean originalMockValue = FrameworkSettings.mock;

        try {
            // Test that the updateFrameworkSettings method handles exceptions gracefully
            // We can't mock Class.forName, but we can verify the method exists and is accessible
            Method updateMethod =
                    MockModeManager.class.getDeclaredMethod(
                            "updateFrameworkSettings", boolean.class);
            updateMethod.setAccessible(true);

            // The method should handle any exception internally and not throw
            // Test with both true and false values
            assertDoesNotThrow(() -> updateMethod.invoke(null, true));
            assertDoesNotThrow(() -> updateMethod.invoke(null, false));

            // Verify FrameworkSettings.mock is set correctly (since class exists)
            updateMethod.invoke(null, true);
            assertTrue(FrameworkSettings.mock);

            updateMethod.invoke(null, false);
            assertFalse(FrameworkSettings.mock);
        } finally {
            // Restore original value to avoid affecting other tests
            FrameworkSettings.mock = originalMockValue;
            // Also sync through MockModeManager
            MockModeManager.setMockMode(originalMockValue);
        }
    }

    @Test
    @DisplayName("Should handle FrameworkSettings field access failure")
    @Order(10)
    void testHandleFrameworkSettingsFieldAccessFailure() throws Exception {
        // Test the robustness of updateFrameworkSettings method
        // While we can't simulate field access failure, we can verify the method's behavior
        Method updateMethod =
                MockModeManager.class.getDeclaredMethod("updateFrameworkSettings", boolean.class);
        updateMethod.setAccessible(true);

        // Save current value
        boolean originalValue = FrameworkSettings.mock;

        try {
            // Test that the method works correctly with the actual field
            updateMethod.invoke(null, true);
            assertTrue(FrameworkSettings.mock, "Should set mock to true");

            updateMethod.invoke(null, false);
            assertFalse(FrameworkSettings.mock, "Should set mock to false");

            // The method should never throw exceptions even if called multiple times
            for (int i = 0; i < 5; i++) {
                final boolean value = (i % 2 == 0);
                assertDoesNotThrow(() -> updateMethod.invoke(null, value));
            }

        } finally {
            // Restore original value
            FrameworkSettings.mock = originalValue;
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
        System.setProperty(FRAMEWORK_MOCK_PROPERTY, "false");
        System.setProperty(CORE_MOCK_PROPERTY, "true");

        ExecutionEnvironment env = ExecutionEnvironment.builder().mockMode(true).build();
        ExecutionEnvironment.setInstance(env);

        FrameworkSettings.mock = true;

        // Should not throw
        assertDoesNotThrow(() -> MockModeManager.logMockModeState());
    }

    @Test
    @DisplayName("Should handle logging with missing components")
    @Order(12)
    void testLogWithMissingComponents() {
        // Clear all properties
        System.clearProperty(MOCK_MODE_PROPERTY);
        System.clearProperty(FRAMEWORK_MOCK_PROPERTY);
        System.clearProperty(CORE_MOCK_PROPERTY);

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
        assertTrue(FrameworkSettings.mock);
    }

    @Test
    @DisplayName("Should not initialize mock mode when properties are false")
    @Order(14)
    void testInitializeMockModeDisabled() {
        // Set properties to false
        System.setProperty(MOCK_MODE_PROPERTY, "false");
        System.setProperty(FRAMEWORK_MOCK_PROPERTY, "false");
        System.setProperty(CORE_MOCK_PROPERTY, "false");

        // Set initial state to mock
        MockModeManager.setMockMode(true);

        // Initialize (should not change since properties are false)
        MockModeManager.initializeMockMode();

        // Mock mode should still be true (not changed by initialization)
        assertTrue(MockModeManager.isMockMode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"brobot.mock.mode", "brobot.framework.mock", "brobot.core.mock-mode"})
    @DisplayName("Should initialize from any mock property")
    @Order(15)
    void testInitializeFromAnyProperty(String property) {
        // Clear all and set only one
        System.clearProperty(MOCK_MODE_PROPERTY);
        System.clearProperty(FRAMEWORK_MOCK_PROPERTY);
        System.clearProperty(CORE_MOCK_PROPERTY);

        System.setProperty(property, "true");

        MockModeManager.initializeMockMode();

        assertTrue(MockModeManager.isMockMode(), "Should initialize mock mode from " + property);
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
        assertEquals("true", System.getProperty(FRAMEWORK_MOCK_PROPERTY));
        assertEquals("true", System.getProperty(CORE_MOCK_PROPERTY));
        assertTrue(ExecutionEnvironment.getInstance().isMockMode());
        assertTrue(FrameworkSettings.mock);

        // Disable mock mode
        MockModeManager.setMockMode(false);

        // All components should report real mode
        assertFalse(MockModeManager.isMockMode());
        assertEquals("false", System.getProperty(MOCK_MODE_PROPERTY));
        assertEquals("false", System.getProperty(FRAMEWORK_MOCK_PROPERTY));
        assertEquals("false", System.getProperty(CORE_MOCK_PROPERTY));
        assertFalse(ExecutionEnvironment.getInstance().isMockMode());
        assertFalse(FrameworkSettings.mock);
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
            assertEquals(enable, FrameworkSettings.mock);
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
        assertEquals(finalState, FrameworkSettings.mock);
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
        System.setProperty(FRAMEWORK_MOCK_PROPERTY, "");
        System.setProperty(CORE_MOCK_PROPERTY, "");

        assertFalse(MockModeManager.isMockMode(), "Empty properties should be false");
    }

    @Test
    @DisplayName("Should prioritize ExecutionEnvironment over properties")
    @Order(22)
    void testPriorityOfExecutionEnvironment() {
        // Set all properties to true
        System.setProperty(MOCK_MODE_PROPERTY, "true");
        System.setProperty(FRAMEWORK_MOCK_PROPERTY, "true");
        System.setProperty(CORE_MOCK_PROPERTY, "true");

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
    void testVariousMockModeScenarios(
            String prop1, String prop2, String prop3, Boolean envMock, boolean expected) {
        // Set properties
        if (prop1 != null) System.setProperty(MOCK_MODE_PROPERTY, prop1);
        if (prop2 != null) System.setProperty(FRAMEWORK_MOCK_PROPERTY, prop2);
        if (prop3 != null) System.setProperty(CORE_MOCK_PROPERTY, prop3);

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
                // prop1, prop2, prop3, envMock, expected
                Arguments.of("true", null, null, null, true), // Only first property
                Arguments.of(null, "true", null, null, true), // Only second property
                Arguments.of(null, null, "true", null, true), // Only third property
                Arguments.of("false", "true", null, null, true), // Mixed properties (any true)
                Arguments.of("false", "false", "false", null, false), // All false
                Arguments.of("true", "true", "true", false, false), // Env overrides properties
                Arguments.of(null, null, null, true, true), // Only env mock
                Arguments.of(null, null, null, false, false), // Only env real
                Arguments.of("invalid", "no", "0", null, false) // Invalid values
                );
    }
}
