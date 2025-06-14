package io.github.jspinak.brobot.runner.events;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.report.log.ActionLogger;
import io.github.jspinak.brobot.report.log.TestSessionLogger;
import io.github.jspinak.brobot.report.log.model.LogData;
import io.github.jspinak.brobot.report.log.model.LogType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublishingActionLoggerTest {

    @Mock
    private ActionLogger actionLoggerDelegate;

    @Mock
    private TestSessionLogger sessionLoggerDelegate;

    @Mock
    private EventBus eventBus;

    private EventPublishingActionLogger logger;

    @BeforeEach
    void setUp() {
        logger = new EventPublishingActionLogger(actionLoggerDelegate, sessionLoggerDelegate, eventBus);
    }

    //
    // ActionLogger tests
    //

    @Test
    void logAction_ShouldDelegateAndPublishEvent() {
        // Arrange
        String sessionId = "test-session";
        Matches matches = mock(Matches.class);
        ObjectCollection objectCollection = mock(ObjectCollection.class);

        LogData logData = new LogData();
        logData.setType(LogType.ACTION);
        when(actionLoggerDelegate.logAction(anyString(), any(), any())).thenReturn(logData);
        when(matches.isSuccess()).thenReturn(true);

        // Act
        LogData result = logger.logAction(sessionId, matches, objectCollection);

        // Assert
        verify(actionLoggerDelegate).logAction(sessionId, matches, objectCollection);
        assertEquals(logData, result);

        // Verify events published
        verify(eventBus, times(2)).publish(any(BrobotEvent.class));
    }

    @Test
    void logAction_WithFailedMatch_ShouldPublishErrorEvent() {
        // Arrange
        String sessionId = "test-session";
        Matches matches = mock(Matches.class);
        ObjectCollection objectCollection = mock(ObjectCollection.class);
        ActionOptions actionOptions = mock(ActionOptions.class);

        LogData logData = new LogData();
        logData.setType(LogType.ACTION);
        when(actionLoggerDelegate.logAction(anyString(), any(), any())).thenReturn(logData);
        when(matches.isSuccess()).thenReturn(false);
        when(matches.getActionOptions()).thenReturn(actionOptions);
        when(actionOptions.getAction()).thenReturn(ActionOptions.Action.CLICK); // Use real enum value

        // Act
        logger.logAction(sessionId, matches, objectCollection);

        // Assert
        ArgumentCaptor<BrobotEvent> eventCaptor = ArgumentCaptor.forClass(BrobotEvent.class);
        verify(eventBus, atLeast(1)).publish(eventCaptor.capture());

        boolean hasErrorEvent = eventCaptor.getAllValues().stream()
                .anyMatch(event -> event instanceof ErrorEvent);
        assertTrue(hasErrorEvent, "Should publish an error event for failed match");
    }

    @Test
    void logStateTransition_ShouldDelegateAndPublishEvent() {
        // Arrange
        String sessionId = "test-session";
        Set<State> fromStates = new HashSet<>();
        Set<State> toStates = new HashSet<>();
        Set<State> beforeStates = new HashSet<>();
        boolean success = true;
        long transitionTime = 1000L;

        LogData logData = new LogData();
        logData.setType(LogType.TRANSITION);
        when(actionLoggerDelegate.logStateTransition(anyString(), any(), any(), any(), anyBoolean(), anyLong()))
                .thenReturn(logData);

        // Act
        LogData result = logger.logStateTransition(sessionId, fromStates, toStates, beforeStates, success, transitionTime);

        // Assert
        verify(actionLoggerDelegate).logStateTransition(sessionId, fromStates, toStates, beforeStates, success, transitionTime);
        assertEquals(logData, result);

        // Verify events published
        verify(eventBus, times(2)).publish(any(BrobotEvent.class));
    }

    @Test
    void logError_ShouldDelegateAndPublishMultipleEvents() {
        // Arrange
        String sessionId = "test-session";
        String errorMessage = "Test error";
        String screenshotPath = "path/to/screenshot.png";

        LogData logData = new LogData();
        logData.setType(LogType.ERROR);
        when(actionLoggerDelegate.logError(anyString(), anyString(), anyString())).thenReturn(logData);

        // Act
        LogData result = logger.logError(sessionId, errorMessage, screenshotPath);

        // Assert
        verify(actionLoggerDelegate).logError(sessionId, errorMessage, screenshotPath);
        assertEquals(logData, result);

        // Verify events published - should be a LogEntryEvent and an ErrorEvent
        verify(eventBus, times(3)).publish(any(BrobotEvent.class));
    }

    //
    // TestSessionLogger tests
    //

    @Test
    void startSession_ShouldDelegateAndPublishEvent() {
        // Arrange
        String applicationUnderTest = "TestApp";
        String sessionId = "test-session";

        when(sessionLoggerDelegate.startSession(anyString())).thenReturn(sessionId);

        // Act
        String result = logger.startSession(applicationUnderTest);

        // Assert
        verify(sessionLoggerDelegate).startSession(applicationUnderTest);
        assertEquals(sessionId, result);

        // Verify events published
        verify(eventBus, times(1)).publish(any(LogEvent.class));
    }

    @Test
    void endSession_ShouldDelegateAndPublishEvent() {
        // Arrange
        String sessionId = "test-session";

        // Act
        logger.endSession(sessionId);

        // Assert
        verify(sessionLoggerDelegate).endSession(sessionId);

        // Verify events published
        verify(eventBus, times(1)).publish(any(LogEvent.class));
    }

    @Test
    void setCurrentState_ShouldDelegateAndPublishEvent() {
        // Arrange
        String sessionId = "test-session";
        String stateName = "TestState";
        String stateDescription = "Test state description";

        // Act
        logger.setCurrentState(sessionId, stateName, stateDescription);

        // Assert
        verify(sessionLoggerDelegate).setCurrentState(sessionId, stateName, stateDescription);

        // Verify events published
        verify(eventBus, times(1)).publish(any(LogEvent.class));
    }

    @Test
    void handleNullLogEntry_ShouldNotThrowException() {
        // Arrange
        String sessionId = "test-session";
        Matches matches = mock(Matches.class);
        ObjectCollection objectCollection = mock(ObjectCollection.class);

        // Return null from delegate to simulate no-op implementation
        when(actionLoggerDelegate.logAction(anyString(), any(), any())).thenReturn(null);
        when(matches.isSuccess()).thenReturn(true);

        // Act & Assert - should not throw
        logger.logAction(sessionId, matches, objectCollection);

        // Just verify no events were published for null log entry
        verify(eventBus, never()).publish(any(LogEntryEvent.class));
    }
}