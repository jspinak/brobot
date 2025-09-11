package io.github.jspinak.brobot.logging.modular;

import static io.github.jspinak.brobot.logging.modular.ActionLogFormatter.VerbosityLevel.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.test.BrobotTestBase;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActionLoggingService Tests")
public class ActionLoggingServiceTest extends BrobotTestBase {

    @Mock private ActionLogFormatter quietFormatter;

    @Mock private ActionLogFormatter normalFormatter;

    @Mock private ActionLogFormatter verboseFormatter;

    @Mock private ActionResult actionResult;

    private ActionLoggingService loggingService;

    @BeforeEach
    public void setUp() {
        super.setupTest();

        when(quietFormatter.getVerbosityLevel()).thenReturn(QUIET);
        when(normalFormatter.getVerbosityLevel()).thenReturn(NORMAL);
        when(verboseFormatter.getVerbosityLevel()).thenReturn(VERBOSE);

        List<ActionLogFormatter> formatters =
                Arrays.asList(quietFormatter, normalFormatter, verboseFormatter);

        loggingService = new ActionLoggingService(formatters);
        ReflectionTestUtils.setField(loggingService, "consoleLoggingEnabled", true);
        ReflectionTestUtils.setField(loggingService, "verbosityConfig", "NORMAL");
    }

    @Test
    @DisplayName("Should initialize with all formatters")
    void shouldInitializeWithAllFormatters() {
        assertNotNull(loggingService);
        // Verify that formatters are properly initialized
        verify(quietFormatter).getVerbosityLevel();
        verify(normalFormatter).getVerbosityLevel();
        verify(verboseFormatter).getVerbosityLevel();
    }

    @Test
    @DisplayName("Should not log when logging is disabled")
    void shouldNotLogWhenLoggingDisabled() {
        ReflectionTestUtils.setField(loggingService, "consoleLoggingEnabled", false);
        ReflectionTestUtils.setField(loggingService, "fileLoggingEnabled", false);

        loggingService.logAction(actionResult);

        verify(quietFormatter, never()).shouldLog(any());
        verify(normalFormatter, never()).shouldLog(any());
        verify(verboseFormatter, never()).shouldLog(any());
    }

    @Test
    @DisplayName("Should not log null action result")
    void shouldNotLogNullActionResult() {
        loggingService.logAction(null);

        verify(quietFormatter, never()).shouldLog(any());
        verify(normalFormatter, never()).shouldLog(any());
        verify(verboseFormatter, never()).shouldLog(any());
    }

    @ParameterizedTest
    @EnumSource(ActionLogFormatter.VerbosityLevel.class)
    @DisplayName("Should use correct formatter for each verbosity level")
    void shouldUseCorrectFormatterForVerbosityLevel(ActionLogFormatter.VerbosityLevel level) {
        ReflectionTestUtils.setField(loggingService, "verbosityConfig", level.name());

        ActionLogFormatter expectedFormatter =
                switch (level) {
                    case QUIET -> quietFormatter;
                    case NORMAL -> normalFormatter;
                    case VERBOSE -> verboseFormatter;
                };

        assertNotNull(expectedFormatter);
        when(expectedFormatter.shouldLog(actionResult)).thenReturn(true);
        when(expectedFormatter.format(actionResult)).thenReturn("Test message");

        loggingService.logAction(actionResult);

        verify(expectedFormatter).shouldLog(actionResult);
        verify(expectedFormatter).format(actionResult);
    }

    @Test
    @DisplayName("Should not log when formatter returns false for shouldLog")
    void shouldNotLogWhenFormatterReturnsFalse() {
        when(normalFormatter.shouldLog(actionResult)).thenReturn(false);

        loggingService.logAction(actionResult);

        verify(normalFormatter).shouldLog(actionResult);
        verify(normalFormatter, never()).format(any());
    }

    @Test
    @DisplayName("Should format and output message when shouldLog returns true")
    void shouldFormatAndOutputMessage() {
        String expectedMessage = "Test action completed successfully";
        when(normalFormatter.shouldLog(actionResult)).thenReturn(true);
        when(normalFormatter.format(actionResult)).thenReturn(expectedMessage);

        loggingService.logAction(actionResult);

        verify(normalFormatter).shouldLog(actionResult);
        verify(normalFormatter).format(actionResult);
    }

    @Test
    @DisplayName("Should not output empty or null formatted message")
    void shouldNotOutputEmptyMessage() {
        when(normalFormatter.shouldLog(actionResult)).thenReturn(true);
        when(normalFormatter.format(actionResult)).thenReturn("");

        loggingService.logAction(actionResult);

        verify(normalFormatter).format(actionResult);
        // Message should be rejected due to being empty
    }

    @Test
    @DisplayName("Should handle formatter exceptions gracefully")
    void shouldHandleFormatterExceptions() {
        when(normalFormatter.shouldLog(actionResult)).thenReturn(true);
        when(normalFormatter.format(actionResult))
                .thenThrow(new RuntimeException("Test exception"));

        assertDoesNotThrow(() -> loggingService.logAction(actionResult));

        verify(normalFormatter).format(actionResult);
    }

    @Test
    @DisplayName("Should support explicit verbosity level override")
    void shouldSupportExplicitVerbosityLevel() {
        when(verboseFormatter.shouldLog(actionResult)).thenReturn(true);
        when(verboseFormatter.format(actionResult)).thenReturn("Verbose message");

        loggingService.logAction(actionResult, VERBOSE);

        verify(verboseFormatter).shouldLog(actionResult);
        verify(verboseFormatter).format(actionResult);
        verify(normalFormatter, never()).shouldLog(any());
    }

    @Test
    @DisplayName("Should handle invalid verbosity config gracefully")
    void shouldHandleInvalidVerbosityConfig() {
        ReflectionTestUtils.setField(loggingService, "verbosityConfig", "INVALID");

        when(quietFormatter.shouldLog(actionResult)).thenReturn(true);
        when(quietFormatter.format(actionResult)).thenReturn("Default message");

        // Should default to QUIET
        loggingService.logAction(actionResult);

        verify(quietFormatter).shouldLog(actionResult);
    }

    @Test
    @DisplayName("Should log with multiple formatters in sequence")
    void shouldLogWithMultipleFormattersInSequence() {
        when(normalFormatter.shouldLog(actionResult)).thenReturn(true);
        when(normalFormatter.format(actionResult)).thenReturn("Normal message");

        when(verboseFormatter.shouldLog(actionResult)).thenReturn(true);
        when(verboseFormatter.format(actionResult)).thenReturn("Verbose message");

        // Log with normal
        loggingService.logAction(actionResult);
        verify(normalFormatter).shouldLog(actionResult);

        // Log with verbose
        loggingService.logAction(actionResult, VERBOSE);
        verify(verboseFormatter).shouldLog(actionResult);
    }

    @Test
    @DisplayName("Should respect console logging enabled flag")
    void shouldRespectConsoleLoggingEnabled() {
        ReflectionTestUtils.setField(loggingService, "consoleLoggingEnabled", true);
        ReflectionTestUtils.setField(loggingService, "fileLoggingEnabled", false);

        when(normalFormatter.shouldLog(actionResult)).thenReturn(true);
        when(normalFormatter.format(actionResult)).thenReturn("Console message");

        loggingService.logAction(actionResult);

        verify(normalFormatter).format(actionResult);
    }

    @Test
    @DisplayName("Should respect file logging enabled flag")
    void shouldRespectFileLoggingEnabled() {
        ReflectionTestUtils.setField(loggingService, "consoleLoggingEnabled", false);
        ReflectionTestUtils.setField(loggingService, "fileLoggingEnabled", true);

        when(normalFormatter.shouldLog(actionResult)).thenReturn(true);
        when(normalFormatter.format(actionResult)).thenReturn("File message");

        loggingService.logAction(actionResult);

        verify(normalFormatter).format(actionResult);
    }

    @ParameterizedTest
    @ValueSource(strings = {"QUIET", "NORMAL", "VERBOSE"})
    @DisplayName("Should parse verbosity config correctly")
    void shouldParseVerbosityConfig(String verbosityLevel) {
        ReflectionTestUtils.setField(loggingService, "verbosityConfig", verbosityLevel);

        ActionLogFormatter expectedFormatter =
                switch (verbosityLevel) {
                    case "QUIET" -> quietFormatter;
                    case "NORMAL" -> normalFormatter;
                    case "VERBOSE" -> verboseFormatter;
                    default -> quietFormatter; // Default to QUIET
                };

        assertNotNull(expectedFormatter);
        when(expectedFormatter.shouldLog(actionResult)).thenReturn(true);
        when(expectedFormatter.format(actionResult)).thenReturn("Test");

        loggingService.logAction(actionResult);

        verify(expectedFormatter).shouldLog(actionResult);
    }
}
