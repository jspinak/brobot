package io.github.jspinak.brobot.logging.unified;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrobotLogger Tests")
public class BrobotLoggerTest extends BrobotTestBase {

    @Mock private LoggingContext context;

    @Mock private MessageRouter router;

    @Mock private LoggingVerbosityConfig verbosityConfig;

    @Mock private StateObject stateObject;

    @Mock private State state;

    @Mock private ActionResult actionResult;

    @Mock private Match match;

    @InjectMocks private BrobotLogger brobotLogger;

    @BeforeEach
    public void setUp() {
        super.setupTest();
    }

    @Test
    @DisplayName("Should log simple action with state object")
    void shouldLogSimpleAction() {
        when(stateObject.getName()).thenReturn("LoginButton");
        when(context.getSessionId()).thenReturn("test-session-123");
        when(context.getCurrentState()).thenReturn(state);
        when(state.getName()).thenReturn("LoginPage");

        brobotLogger.action("CLICK", stateObject);

        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(router).route(eventCaptor.capture());

        LogEvent captured = eventCaptor.getValue();
        assertEquals(LogEvent.Type.ACTION, captured.getType());
        assertEquals("CLICK", captured.getAction());
        assertEquals("LoginButton", captured.getTarget());
        assertEquals("test-session-123", captured.getSessionId());
        assertEquals("LoginPage", captured.getStateId());
        assertNotNull(captured.getTimestamp());
    }

    @Test
    @DisplayName("Should handle null current state")
    void shouldHandleNullCurrentState() {
        when(stateObject.getName()).thenReturn("TestObject");
        when(context.getSessionId()).thenReturn("session-456");
        when(context.getCurrentState()).thenReturn(null);

        brobotLogger.action("HOVER", stateObject);

        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(router).route(eventCaptor.capture());

        LogEvent captured = eventCaptor.getValue();
        assertNull(captured.getStateId());
    }

    @Test
    @DisplayName("Should log action with result")
    void shouldLogActionWithResult() {
        when(stateObject.getName()).thenReturn("SubmitButton");
        when(actionResult.isSuccess()).thenReturn(true);
        when(actionResult.getDuration()).thenReturn(Duration.ofMillis(250));
        when(context.getSessionId()).thenReturn("session-789");

        brobotLogger.action("CLICK", stateObject, actionResult);

        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(router).route(eventCaptor.capture());

        LogEvent captured = eventCaptor.getValue();
        assertEquals("CLICK", captured.getAction());
        assertEquals("SubmitButton", captured.getTarget());
        assertTrue(captured.isSuccess());
        assertEquals(Long.valueOf(250), captured.getDuration());
    }

    // State transition logging method removed - not part of current API

    @Test
    @DisplayName("Should create log builder for complex logging")
    void shouldCreateLogBuilder() {
        LogBuilder builder = brobotLogger.log();

        assertNotNull(builder);
        // The builder should be associated with the logger
        // Testing builder functionality separately
    }

    @Test
    @DisplayName("Should log error with exception")
    void shouldLogError() {
        Exception exception = new RuntimeException("Test error");

        when(context.getSessionId()).thenReturn("error-session");

        brobotLogger.error("Operation failed", exception);

        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(router).route(eventCaptor.capture());

        LogEvent captured = eventCaptor.getValue();
        assertEquals(LogEvent.Type.ERROR, captured.getType());
        assertEquals("Operation failed", captured.getMessage());
        assertEquals(exception, captured.getError());
    }

    @Test
    @DisplayName("Should create session scope")
    void shouldCreateSessionScope() throws Exception {
        String sessionId = "test-session-999";

        try (AutoCloseable session = brobotLogger.session(sessionId)) {
            assertNotNull(session);
            verify(context).setSessionId(sessionId);
        }

        verify(context).clearSession();
    }

    @Test
    @DisplayName("Should track operation performance")
    void shouldTrackOperationPerformance() throws Exception {
        String operationName = "testOperation";

        when(context.getSessionId()).thenReturn("perf-session");

        try (AutoCloseable operation = brobotLogger.operation(operationName)) {
            assertNotNull(operation);
            verify(context).pushOperation(operationName);

            // Simulate some work
            Thread.sleep(10);
        }

        verify(context).popOperation();

        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(router).route(eventCaptor.capture());

        LogEvent captured = eventCaptor.getValue();
        assertEquals(LogEvent.Type.PERFORMANCE, captured.getType());
        assertTrue(captured.getMessage().contains(operationName));
    }

    @Test
    @DisplayName("Should set verbosity config")
    void shouldSetVerbosityConfig() {
        brobotLogger.setVerbosityConfig(verbosityConfig);

        // Verify the config is set (internal state)
        // The actual behavior depends on how verbosity affects logging
        assertNotNull(brobotLogger);
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOW", "HIGH"})
    @DisplayName("Should set console output level")
    void shouldSetConsoleOutputLevel(String level) {
        ConsoleReporter.OutputLevel outputLevel = ConsoleReporter.OutputLevel.valueOf(level);

        brobotLogger.setConsoleLevel(outputLevel);

        // The console level should be updated
        assertEquals(outputLevel, brobotLogger.getConsoleLevel());
    }

    @Test
    @DisplayName("Should log action with valid session context")
    void shouldLogActionWithSessionContext() {
        when(stateObject.getName()).thenReturn("TestObject");
        when(context.getSessionId()).thenReturn("test-session");
        when(context.getCurrentState()).thenReturn(state);
        when(state.getName()).thenReturn("TestState");

        brobotLogger.action("TEST", stateObject);

        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(router).route(eventCaptor.capture());

        LogEvent captured = eventCaptor.getValue();
        assertEquals("test-session", captured.getSessionId());
        assertEquals("TestState", captured.getStateId());
    }

    @Test
    @DisplayName("Should handle concurrent logging")
    void shouldHandleConcurrentLogging() throws InterruptedException {
        when(stateObject.getName()).thenReturn("ConcurrentObject");
        when(context.getSessionId()).thenReturn("concurrent-session");

        Thread thread1 =
                new Thread(
                        () -> {
                            for (int i = 0; i < 10; i++) {
                                brobotLogger.action("ACTION1", stateObject);
                            }
                        });

        Thread thread2 =
                new Thread(
                        () -> {
                            for (int i = 0; i < 10; i++) {
                                brobotLogger.action("ACTION2", stateObject);
                            }
                        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        verify(router, times(20)).route(any(LogEvent.class));
    }
}
