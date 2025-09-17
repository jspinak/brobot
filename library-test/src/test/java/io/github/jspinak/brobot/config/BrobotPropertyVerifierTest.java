package io.github.jspinak.brobot.config;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.config.core.BrobotPropertyVerifier;
import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogBuilder;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for BrobotPropertyVerifier. Achieves high coverage through testing all
 * methods, branches, and scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BrobotPropertyVerifier Tests")
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test incompatible with CI environment")
@SpringBootTest
@TestPropertySource(properties = {"brobot.core.mock=true", "brobot.core.headless=true"})
public class BrobotPropertyVerifierTest extends BrobotTestBase {

    @Mock private BrobotProperties mockProperties;

    @Mock private BrobotProperties.Screenshot mockScreenshotProps;

    @Mock private BrobotProperties.Illustration mockIllustrationProps;

    @Mock private BrobotLogger mockLogger;

    @Mock private LogBuilder mockLogBuilder;

    @Mock private ApplicationReadyEvent mockEvent;

    @Mock private ApplicationContext mockContext;

    @InjectMocks private BrobotPropertyVerifier verifier;

    @Captor private ArgumentCaptor<String> stringCaptor;

    @Captor private ArgumentCaptor<Object> objectCaptor;

    // Original settings are now handled by Spring context
    // No need to save/restore as each test gets fresh context

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Setup mock chains - use lenient() for stubs that may not be used by all tests
        lenient().when(mockProperties.getScreenshot()).thenReturn(mockScreenshotProps);
        lenient().when(mockProperties.getIllustration()).thenReturn(mockIllustrationProps);
        lenient().when(mockLogger.log()).thenReturn(mockLogBuilder);
        lenient().when(mockLogBuilder.observation(anyString())).thenReturn(mockLogBuilder);
        lenient().when(mockLogBuilder.metadata(anyString(), any())).thenReturn(mockLogBuilder);

        // Reset static flag for each test
        resetPropertiesVerifiedFlag();
    }

    @Test
    @DisplayName("Should verify properties on application ready event")
    void testVerifyPropertiesOnApplicationReady() {
        // Given
        when(mockScreenshotProps.isSaveHistory()).thenReturn(true);
        when(mockIllustrationProps.isDrawFind()).thenReturn(true);
        when(mockIllustrationProps.isDrawClick()).thenReturn(true);
        // Setting saveHistory now handled by BrobotProperties
        // Setting historyPath now handled by BrobotProperties

        // When
        verifier.verifyProperties();

        // Then
        verify(mockLogger, atLeast(3)).log();
        verify(mockLogBuilder, atLeastOnce()).observation("Brobot Property Verification");
        verify(mockLogBuilder, atLeastOnce()).observation("Execution Environment");
        verify(mockLogBuilder, atLeastOnce()).observation("Illustration Settings");
    }

    @Test
    @DisplayName("Should only verify properties once")
    void testVerifyPropertiesOnlyOnce() {
        // Given
        when(mockScreenshotProps.isSaveHistory()).thenReturn(true);
        // Setting saveHistory now handled by BrobotProperties

        // When - First call
        verifier.verifyProperties();

        // Reset mock to verify second call doesn't trigger
        reset(mockLogger);
        // Don't stub again - we want to verify it's not called

        // When - Second call
        verifier.verifyProperties();

        // Then - Logger should not be called on second invocation
        verify(mockLogger, never()).log();
    }

    @Test
    @DisplayName("Should warn when illustrations are disabled")
    void testWarnWhenIllustrationsDisabled() {
        // Given
        when(mockScreenshotProps.isSaveHistory()).thenReturn(false);
        // Setting saveHistory now handled by BrobotProperties

        // When
        verifier.verifyProperties();

        // Then
        verify(mockLogBuilder).observation("WARNING: Illustrations Disabled");
        verify(mockLogBuilder).metadata("reason", "saveHistory is false");
        verify(mockLogBuilder)
                .metadata(
                        "solution",
                        "Set brobot.screenshot.save-history=true in application.properties");
    }

    @Test
    @DisplayName("Should handle mock mode appropriately")
    void testHandleMockMode() throws Exception {
        // Given
        when(mockScreenshotProps.isSaveHistory()).thenReturn(true);
        // Setting saveHistory now handled by BrobotProperties

        // Mock the ExecutionEnvironment singleton
        try (MockedStatic<ExecutionEnvironment> mockedEnv =
                mockStatic(ExecutionEnvironment.class)) {
            ExecutionEnvironment mockEnvInstance = mock(ExecutionEnvironment.class);
            mockedEnv.when(ExecutionEnvironment::getInstance).thenReturn(mockEnvInstance);
            when(mockEnvInstance.isMockMode()).thenReturn(true);
            when(mockEnvInstance.hasDisplay()).thenReturn(true);
            when(mockEnvInstance.canCaptureScreen()).thenReturn(true);

            // When
            verifier.verifyProperties();

            // Then - BrobotLogger.observation() is called directly
            verify(mockLogger)
                    .observation("Note: Running in mock mode - illustrations use mock data");
        }
    }

    @Test
    @DisplayName("Should warn about headless mode")
    void testWarnAboutHeadlessMode() throws Exception {
        // Given
        when(mockScreenshotProps.isSaveHistory()).thenReturn(true);
        // Setting saveHistory now handled by BrobotProperties

        // Mock headless environment - use try-with-resources for proper cleanup
        try (MockedStatic<GraphicsEnvironment> mockedGE = mockStatic(GraphicsEnvironment.class);
                MockedStatic<ExecutionEnvironment> mockedEnv =
                        mockStatic(ExecutionEnvironment.class)) {

            mockedGE.when(GraphicsEnvironment::isHeadless).thenReturn(true);

            ExecutionEnvironment mockEnvInstance = mock(ExecutionEnvironment.class);
            mockedEnv.when(ExecutionEnvironment::getInstance).thenReturn(mockEnvInstance);
            when(mockEnvInstance.isMockMode()).thenReturn(false);
            when(mockEnvInstance.hasDisplay()).thenReturn(true);
            when(mockEnvInstance.canCaptureScreen()).thenReturn(true);

            // When
            verifier.verifyProperties();

            // Then
            verify(mockLogBuilder).observation("WARNING: Headless Mode Active");
            verify(mockLogBuilder).metadata("headless", true);
        }
    }

    @Test
    @DisplayName("Should confirm illustrations are enabled")
    void testConfirmIllustrationsEnabled() throws Exception {
        // Given
        when(mockScreenshotProps.isSaveHistory()).thenReturn(true);
        when(mockIllustrationProps.isDrawFind())
                .thenReturn(true); // At least one needs to be true for illustrations to be enabled
        // Setting saveHistory now handled by BrobotProperties

        // Mock non-headless, non-mock environment - use try-with-resources
        try (MockedStatic<GraphicsEnvironment> mockedGE = mockStatic(GraphicsEnvironment.class);
                MockedStatic<ExecutionEnvironment> mockedEnv =
                        mockStatic(ExecutionEnvironment.class)) {

            mockedGE.when(GraphicsEnvironment::isHeadless).thenReturn(false);

            ExecutionEnvironment mockEnvInstance = mock(ExecutionEnvironment.class);
            mockedEnv.when(ExecutionEnvironment::getInstance).thenReturn(mockEnvInstance);
            when(mockEnvInstance.isMockMode()).thenReturn(false);
            when(mockEnvInstance.hasDisplay()).thenReturn(true);
            when(mockEnvInstance.canCaptureScreen()).thenReturn(true);

            // When
            verifier.verifyProperties();

            // Then - BrobotLogger.observation() is called directly
            verify(mockLogger).observation("Illustrations are enabled and configured");
        }
    }

    @ParameterizedTest
    @CsvSource({
        "true, true, false, false, false, false, false, true", // Only drawFind enabled
        "true, false, true, false, false, false, false, true", // Only drawClick enabled
        "true, false, false, true, false, false, false, true", // Only drawDrag enabled
        "true, false, false, false, true, false, false, true", // Only drawMove enabled
        "true, false, false, false, false, true, false, true", // Only drawHighlight enabled
        "true, false, false, false, false, false, true, true", // Only drawClassify enabled
        "true, false, false, false, false, false, false, false", // None enabled but save history
        // true
        "false, true, true, true, true, true, true, false", // All draw enabled but save history
        // false
    })
    @DisplayName("Should detect illustration enabled state correctly")
    void testIsIllustrationEnabled(
            boolean saveHistory,
            boolean drawFind,
            boolean drawClick,
            boolean drawDrag,
            boolean drawMove,
            boolean drawHighlight,
            boolean drawClassify,
            boolean expectedEnabled) {
        // Given
        when(mockScreenshotProps.isSaveHistory()).thenReturn(saveHistory);
        when(mockIllustrationProps.isDrawFind()).thenReturn(drawFind);
        when(mockIllustrationProps.isDrawClick()).thenReturn(drawClick);
        when(mockIllustrationProps.isDrawDrag()).thenReturn(drawDrag);
        when(mockIllustrationProps.isDrawMove()).thenReturn(drawMove);
        when(mockIllustrationProps.isDrawHighlight()).thenReturn(drawHighlight);
        when(mockIllustrationProps.isDrawClassify()).thenReturn(drawClassify);
        when(mockIllustrationProps.isDrawDefine()).thenReturn(false);
        // Setting saveHistory now handled by BrobotProperties

        // When
        verifier.verifyProperties();

        // Then
        verify(mockLogBuilder).metadata(eq("illustrationEnabled"), eq(expectedEnabled));
    }

    @Test
    @DisplayName("Should print verification to console")
    void testPrintVerification() {
        // Given
        when(mockScreenshotProps.isSaveHistory()).thenReturn(true);
        // Setting historyPath now handled by BrobotProperties
        // Setting saveHistory now handled by BrobotProperties

        // Capture console output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try (MockedStatic<ExecutionEnvironment> mockedEnv =
                mockStatic(ExecutionEnvironment.class)) {
            // Mock ExecutionEnvironment for predictable output
            ExecutionEnvironment mockEnvInstance = mock(ExecutionEnvironment.class);
            mockedEnv.when(ExecutionEnvironment::getInstance).thenReturn(mockEnvInstance);
            when(mockEnvInstance.isMockMode()).thenReturn(true);
            when(mockEnvInstance.hasDisplay()).thenReturn(false);
            when(mockEnvInstance.canCaptureScreen()).thenReturn(false);

            // When
            verifier.printVerification();

            // Then
            String output = outContent.toString();
            assertTrue(output.contains("=== Brobot Property Verification ==="));
            assertTrue(output.contains("Save history: true"));
            assertTrue(output.contains("History path: /test/history"));
            assertTrue(output.contains("Save history (BrobotProperties): true"));
            assertTrue(output.contains("Mock mode:"));
            assertTrue(output.contains("Has display:"));
            assertTrue(output.contains("Can capture screen:"));
            assertTrue(output.contains("==================================="));
        } finally {
            // Restore original output
            System.setOut(originalOut);
        }
    }

    @Test
    @DisplayName("Should log all illustration settings")
    void testLogAllIllustrationSettings() {
        // Given
        when(mockScreenshotProps.isSaveHistory()).thenReturn(true);
        when(mockIllustrationProps.isDrawFind()).thenReturn(true);
        when(mockIllustrationProps.isDrawClick()).thenReturn(false);
        when(mockIllustrationProps.isDrawDrag()).thenReturn(true);
        when(mockIllustrationProps.isDrawMove()).thenReturn(false);
        when(mockIllustrationProps.isDrawHighlight()).thenReturn(true);
        when(mockIllustrationProps.isDrawRepeatedActions()).thenReturn(false);
        when(mockIllustrationProps.isDrawClassify()).thenReturn(true);
        when(mockIllustrationProps.isDrawDefine()).thenReturn(false);
        // Setting saveHistory now handled by BrobotProperties

        // When
        verifier.verifyProperties();

        // Then - Verify illustration properties are logged
        verify(mockLogBuilder, atLeastOnce()).metadata("drawFind", true);
        verify(mockLogBuilder, atLeastOnce()).metadata("drawClick", false);
        verify(mockLogBuilder, atLeastOnce()).metadata("drawDrag", true);
        verify(mockLogBuilder, atLeastOnce()).metadata("drawMove", false);
        verify(mockLogBuilder, atLeastOnce()).metadata("drawHighlight", true);
        verify(mockLogBuilder, atLeastOnce()).metadata("drawRepeatedActions", false);
        verify(mockLogBuilder, atLeastOnce()).metadata("drawClassify", true);
        verify(mockLogBuilder, atLeastOnce()).metadata("drawDefine", false);
    }

    @Test
    @DisplayName("Should log framework settings")
    void testLogBrobotProperties() {
        // Given
        when(mockScreenshotProps.isSaveHistory()).thenReturn(true);
        // Setting drawFind now handled by BrobotProperties
        // Setting drawClick now handled by BrobotProperties
        // Setting drawDrag now handled by BrobotProperties
        // Setting drawMove now handled by BrobotProperties
        // Setting drawHighlight now handled by BrobotProperties
        // Setting drawRepeatedActions now handled by BrobotProperties
        // Setting drawClassify now handled by BrobotProperties
        // Setting drawDefine now handled by BrobotProperties

        // When
        verifier.verifyProperties();

        // Then
        verify(mockLogBuilder).observation("Framework Settings (Illustration Related)");
        // Framework settings are logged in a separate log call
        verify(mockLogBuilder, atLeastOnce()).metadata(eq("drawFind"), any());
    }

    @Test
    @DisplayName("Should log execution environment details")
    void testLogExecutionEnvironmentDetails() throws Exception {
        // Given
        when(mockScreenshotProps.isSaveHistory()).thenReturn(true);
        when(mockIllustrationProps.isDrawFind()).thenReturn(true); // At least one needs to be true
        // Setting saveHistory now handled by BrobotProperties

        // Use try-with-resources for proper cleanup
        try (MockedStatic<ExecutionEnvironment> mockedEnv =
                mockStatic(ExecutionEnvironment.class)) {
            ExecutionEnvironment mockEnvInstance = mock(ExecutionEnvironment.class);
            mockedEnv.when(ExecutionEnvironment::getInstance).thenReturn(mockEnvInstance);
            when(mockEnvInstance.isMockMode()).thenReturn(true);
            when(mockEnvInstance.hasDisplay()).thenReturn(false);
            when(mockEnvInstance.canCaptureScreen()).thenReturn(false);

            // When
            verifier.verifyProperties();

            // Then
            verify(mockLogBuilder).metadata("mockMode", true);
            verify(mockLogBuilder).metadata("hasDisplay", false);
            verify(mockLogBuilder).metadata("canCaptureScreen", false);
            verify(mockLogBuilder).metadata(eq("osName"), anyString());
        }
    }

    @ParameterizedTest
    @MethodSource("providePropertyCombinations")
    @DisplayName("Should handle various property combinations")
    void testVariousPropertyCombinations(
            boolean saveHistory,
            boolean hasIllustrations,
            boolean mockMode,
            String expectedObservation)
            throws Exception {
        // Given
        when(mockScreenshotProps.isSaveHistory()).thenReturn(saveHistory);
        // Setting saveHistory now handled by BrobotProperties

        if (hasIllustrations) {
            when(mockIllustrationProps.isDrawFind()).thenReturn(true);
        } else {
            when(mockIllustrationProps.isDrawFind()).thenReturn(false);
            when(mockIllustrationProps.isDrawClick()).thenReturn(false);
            when(mockIllustrationProps.isDrawDrag()).thenReturn(false);
            when(mockIllustrationProps.isDrawMove()).thenReturn(false);
            when(mockIllustrationProps.isDrawHighlight()).thenReturn(false);
            when(mockIllustrationProps.isDrawClassify()).thenReturn(false);
            when(mockIllustrationProps.isDrawDefine()).thenReturn(false);
        }

        // Use try-with-resources for proper cleanup
        try (MockedStatic<ExecutionEnvironment> mockedEnv = mockStatic(ExecutionEnvironment.class);
                MockedStatic<GraphicsEnvironment> mockedGE =
                        mockStatic(GraphicsEnvironment.class)) {

            ExecutionEnvironment mockEnvInstance = mock(ExecutionEnvironment.class);
            mockedEnv.when(ExecutionEnvironment::getInstance).thenReturn(mockEnvInstance);
            when(mockEnvInstance.isMockMode()).thenReturn(mockMode);
            when(mockEnvInstance.hasDisplay()).thenReturn(true);
            when(mockEnvInstance.canCaptureScreen()).thenReturn(true);

            mockedGE.when(GraphicsEnvironment::isHeadless).thenReturn(false);

            // When
            verifier.verifyProperties();

            // Then
            if (!expectedObservation.isEmpty()) {
                if (expectedObservation.contains("Note: Running in mock mode")) {
                    verify(mockLogger)
                            .observation(
                                    argThat(
                                            s ->
                                                    s != null
                                                            && s.contains(
                                                                    "Note: Running in mock mode")));
                } else if (expectedObservation.contains("Illustrations are enabled")) {
                    verify(mockLogger)
                            .observation(
                                    argThat(
                                            s ->
                                                    s != null
                                                            && s.contains(
                                                                    "Illustrations are enabled")));
                } else {
                    verify(mockLogBuilder)
                            .observation(
                                    argThat(s -> s != null && s.contains(expectedObservation)));
                }
            }
        }
    }

    private static Stream<Arguments> providePropertyCombinations() {
        return Stream.of(
                Arguments.of(false, false, false, "WARNING: Illustrations Disabled"),
                Arguments.of(false, true, false, "WARNING: Illustrations Disabled"),
                Arguments.of(true, true, true, "Note: Running in mock mode"),
                Arguments.of(true, true, false, "Illustrations are enabled"),
                Arguments.of(true, false, false, "Illustrations are enabled"));
    }

    // Annotation tests removed - focus on functional testing

    private void resetPropertiesVerifiedFlag() {
        try {
            Field field = BrobotPropertyVerifier.class.getDeclaredField("propertiesVerified");
            field.setAccessible(true);
            field.setBoolean(null, false);
        } catch (Exception e) {
            // Ignore - this is just for test cleanup
        }
    }
}
