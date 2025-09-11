package io.github.jspinak.brobot.tools.testing.exploration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.util.image.capture.ScreenshotCapture;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExplorationSessionRunnerTest extends BrobotTestBase {

    @Mock private StateNavigator stateNavigator;

    @Mock private StateMemory stateMemory;

    @Mock private StateService stateService;

    @Mock private ActionLogger actionLogger;

    @Mock private ScreenshotCapture screenshotCapture;

    @Mock private Action action;

    private ExplorationSessionRunner sessionRunner;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        sessionRunner =
                new ExplorationSessionRunner(
                        stateNavigator,
                        stateMemory,
                        stateService,
                        actionLogger,
                        screenshotCapture,
                        action);
    }

    @Nested
    @DisplayName("Test Session Execution")
    class TestSessionExecutionTests {

        @Test
        @DisplayName("Should execute successful test with all logging steps")
        void shouldExecuteSuccessfulTest() throws IOException, AWTException {
            // Arrange
            String destination = "LoginState";
            Long destinationId = 42L;

            State loginState = mock(State.class);
            when(loginState.getId()).thenReturn(destinationId);
            when(loginState.getName()).thenReturn(destination);

            when(stateService.getState(destination)).thenReturn(Optional.of(loginState));

            Set<Long> initialStates = Set.of(1L, 2L);
            Set<Long> finalStates = Set.of(destinationId);
            when(stateMemory.getActiveStates()).thenReturn(initialStates).thenReturn(finalStates);

            when(stateNavigator.openState(destinationId)).thenReturn(true);
            when(stateService.findSetById(anySet())).thenReturn(new HashSet<>());

            // Act
            sessionRunner.runTest(destination);

            // Assert - Verify logging sequence
            verify(actionLogger).startVideoRecording(anyString());
            verify(actionLogger)
                    .logObservation(
                            anyString(), eq("TEST_START"), contains("Test started"), eq("INFO"));
            verify(actionLogger)
                    .logObservation(
                            anyString(),
                            eq("INITIAL_STATE"),
                            contains("Initial states"),
                            eq("INFO"));
            verify(actionLogger)
                    .logStateTransition(
                            anyString(), isNull(), anySet(), anySet(), eq(true), anyLong());
            verify(actionLogger)
                    .logObservation(
                            anyString(), eq("FINAL_STATE"), contains("Final states"), eq("INFO"));
            verify(actionLogger)
                    .logPerformanceMetrics(anyString(), anyLong(), anyLong(), anyLong());
            verify(actionLogger).stopVideoRecording(anyString());
            verify(actionLogger)
                    .logObservation(
                            anyString(), eq("TEST_END"), contains("Test ended"), eq("INFO"));

            // Should not capture screenshot on success
            verify(screenshotCapture, never()).captureScreenshot(anyString());
            verify(actionLogger, never()).logError(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should handle transition failure with screenshot capture")
        void shouldHandleTransitionFailure() throws IOException, AWTException {
            // Arrange
            String destination = "UnreachableState";
            Long destinationId = 99L;

            State unreachableState = mock(State.class);
            when(unreachableState.getId()).thenReturn(destinationId);
            when(unreachableState.getName()).thenReturn(destination);

            when(stateService.getState(destination)).thenReturn(Optional.of(unreachableState));

            when(stateMemory.getActiveStates()).thenReturn(Set.of(1L));
            when(stateNavigator.openState(destinationId)).thenReturn(false);
            when(stateService.findSetById(anySet())).thenReturn(new HashSet<>());

            String screenshotPath = "/path/to/screenshot.png";
            when(screenshotCapture.captureScreenshot(contains("transition_failed")))
                    .thenReturn(screenshotPath);

            // Act
            sessionRunner.runTest(destination);

            // Assert
            verify(stateNavigator).openState(destinationId);
            verify(screenshotCapture).captureScreenshot(contains("transition_failed"));
            verify(actionLogger)
                    .logError(anyString(), contains("Failed to transition"), eq(screenshotPath));
            verify(actionLogger)
                    .logStateTransition(
                            anyString(), isNull(), anySet(), anySet(), eq(false), anyLong());

            // Should still complete the test
            verify(actionLogger).stopVideoRecording(anyString());
            verify(actionLogger)
                    .logObservation(anyString(), eq("TEST_END"), anyString(), eq("INFO"));
        }

        @Test
        @DisplayName("Should handle destination state not found")
        void shouldHandleDestinationNotFound() throws IOException, AWTException {
            // Arrange
            String destination = "NonExistentState";
            when(stateService.getState(destination)).thenReturn(Optional.empty());

            String screenshotPath = "/path/to/error.png";
            when(screenshotCapture.captureScreenshot(contains("error"))).thenReturn(screenshotPath);

            // Act
            sessionRunner.runTest(destination);

            // Assert
            verify(actionLogger).startVideoRecording(anyString());
            verify(actionLogger)
                    .logError(
                            anyString(),
                            contains("Destination state not found"),
                            eq(screenshotPath));
            verify(screenshotCapture).captureScreenshot(contains("error"));

            // Should not attempt transition
            verify(stateNavigator, never()).openState(anyLong());

            // Should still stop recording
            verify(actionLogger).stopVideoRecording(anyString());
        }
    }

    @Nested
    @DisplayName("Performance Metrics Tests")
    class PerformanceMetricsTests {

        @Test
        @DisplayName("Should accurately measure transition duration")
        void shouldMeasureTransitionDuration() {
            // Arrange
            String destination = "SlowState";
            Long destinationId = 10L;

            State slowState = mock(State.class);
            when(slowState.getId()).thenReturn(destinationId);
            when(slowState.getName()).thenReturn(destination);

            when(stateService.getState(destination)).thenReturn(Optional.of(slowState));

            when(stateMemory.getActiveStates()).thenReturn(Set.of(1L));
            when(stateService.findSetById(anySet())).thenReturn(new HashSet<>());

            // Simulate slow transition
            when(stateNavigator.openState(destinationId))
                    .thenAnswer(
                            invocation -> {
                                Thread.sleep(100); // 100ms delay
                                return true;
                            });

            // Act
            sessionRunner.runTest(destination);

            // Assert
            ArgumentCaptor<Long> transitionDurationCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<Long> totalDurationCaptor = ArgumentCaptor.forClass(Long.class);

            verify(actionLogger)
                    .logPerformanceMetrics(
                            anyString(),
                            transitionDurationCaptor.capture(),
                            anyLong(),
                            totalDurationCaptor.capture());

            // Transition duration should be at least 100ms
            assertTrue(transitionDurationCaptor.getValue() >= 100);

            // Total duration should be greater than transition duration
            assertTrue(totalDurationCaptor.getValue() >= transitionDurationCaptor.getValue());
        }

        @Test
        @DisplayName("Should log performance metrics even on failure")
        void shouldLogMetricsOnFailure() {
            // Arrange
            String destination = "FailState";
            Long destinationId = 20L;

            State failState = mock(State.class);
            when(failState.getId()).thenReturn(destinationId);
            when(failState.getName()).thenReturn(destination);

            when(stateService.getState(destination)).thenReturn(Optional.of(failState));

            when(stateMemory.getActiveStates()).thenReturn(Set.of(1L));
            when(stateNavigator.openState(destinationId)).thenReturn(false);
            when(stateService.findSetById(anySet())).thenReturn(new HashSet<>());
            when(screenshotCapture.captureScreenshot(anyString())).thenReturn("/screenshot.png");

            // Act
            sessionRunner.runTest(destination);

            // Assert
            verify(actionLogger)
                    .logPerformanceMetrics(anyString(), anyLong(), anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("Session Management Tests")
    class SessionManagementTests {

        @Test
        @DisplayName("Should generate unique session IDs")
        void shouldGenerateUniqueSessionIds() throws IOException, AWTException {
            // Arrange
            String destination = "TestState";
            Long destinationId = 5L;

            State testState = mock(State.class);
            when(testState.getId()).thenReturn(destinationId);
            when(testState.getName()).thenReturn(destination);

            when(stateService.getState(destination)).thenReturn(Optional.of(testState));

            when(stateMemory.getActiveStates()).thenReturn(Set.of(1L));
            when(stateNavigator.openState(destinationId)).thenReturn(true);
            when(stateService.findSetById(anySet())).thenReturn(new HashSet<>());

            // Act - Run test twice
            sessionRunner.runTest(destination);
            sessionRunner.runTest(destination);

            // Assert - Capture session IDs
            ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);
            verify(actionLogger, times(2)).startVideoRecording(sessionIdCaptor.capture());

            String sessionId1 = sessionIdCaptor.getAllValues().get(0);
            String sessionId2 = sessionIdCaptor.getAllValues().get(1);

            assertNotNull(sessionId1);
            assertNotNull(sessionId2);
            assertNotEquals(sessionId1, sessionId2);
        }

        @Test
        @DisplayName("Should maintain session consistency throughout test")
        void shouldMaintainSessionConsistency() throws IOException, AWTException {
            // Arrange
            String destination = "ConsistentState";
            Long destinationId = 7L;

            State state = mock(State.class);
            when(state.getId()).thenReturn(destinationId);
            when(state.getName()).thenReturn(destination);

            when(stateService.getState(destination)).thenReturn(Optional.of(state));

            when(stateMemory.getActiveStates()).thenReturn(Set.of(1L));
            when(stateNavigator.openState(destinationId)).thenReturn(true);
            when(stateService.findSetById(anySet())).thenReturn(new HashSet<>());

            // Act
            sessionRunner.runTest(destination);

            // Assert - All logging should use the same session ID
            ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);

            verify(actionLogger).startVideoRecording(sessionIdCaptor.capture());
            String sessionId = sessionIdCaptor.getValue();

            verify(actionLogger)
                    .logObservation(eq(sessionId), eq("TEST_START"), anyString(), anyString());
            verify(actionLogger)
                    .logObservation(eq(sessionId), eq("INITIAL_STATE"), anyString(), anyString());
            verify(actionLogger)
                    .logStateTransition(
                            eq(sessionId), any(), anySet(), anySet(), anyBoolean(), anyLong());
            verify(actionLogger)
                    .logObservation(eq(sessionId), eq("FINAL_STATE"), anyString(), anyString());
            verify(actionLogger)
                    .logPerformanceMetrics(eq(sessionId), anyLong(), anyLong(), anyLong());
            verify(actionLogger).stopVideoRecording(eq(sessionId));
            verify(actionLogger)
                    .logObservation(eq(sessionId), eq("TEST_END"), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle video recording start failure gracefully")
        void shouldHandleVideoStartFailure() throws IOException, AWTException {
            // Arrange
            String destination = "TestState";
            Long destinationId = 8L;

            State state = mock(State.class);
            when(state.getId()).thenReturn(destinationId);
            when(state.getName()).thenReturn(destination);

            when(stateService.getState(destination)).thenReturn(Optional.of(state));

            doThrow(new RuntimeException("Video service unavailable"))
                    .when(actionLogger)
                    .startVideoRecording(anyString());

            when(stateMemory.getActiveStates()).thenReturn(Set.of(1L));
            when(stateNavigator.openState(destinationId)).thenReturn(true);
            when(stateService.findSetById(anySet())).thenReturn(new HashSet<>());
            when(screenshotCapture.captureScreenshot(anyString())).thenReturn("/error.png");

            // Act
            sessionRunner.runTest(destination);

            // Assert - Should log error and continue
            verify(actionLogger)
                    .logError(anyString(), contains("Video service unavailable"), anyString());
            verify(actionLogger).stopVideoRecording(anyString());
        }

        @Test
        @DisplayName("Should handle video recording stop failure")
        void shouldHandleVideoStopFailure() throws IOException, AWTException {
            // Arrange
            String destination = "TestState";
            Long destinationId = 9L;

            State state = mock(State.class);
            when(state.getId()).thenReturn(destinationId);
            when(state.getName()).thenReturn(destination);

            when(stateService.getState(destination)).thenReturn(Optional.of(state));

            when(stateMemory.getActiveStates()).thenReturn(Set.of(1L));
            when(stateNavigator.openState(destinationId)).thenReturn(true);
            when(stateService.findSetById(anySet())).thenReturn(new HashSet<>());

            doThrow(new RuntimeException("Cannot stop recording"))
                    .when(actionLogger)
                    .stopVideoRecording(anyString());

            // Act - Should not throw exception
            assertDoesNotThrow(() -> sessionRunner.runTest(destination));

            // Assert - Should complete test despite stop failure
            verify(actionLogger)
                    .logObservation(anyString(), eq("TEST_END"), anyString(), eq("INFO"));
        }

        @Test
        @DisplayName("Should handle exception during state transition")
        void shouldHandleTransitionException() throws IOException, AWTException {
            // Arrange
            String destination = "ExceptionState";
            Long destinationId = 11L;

            State state = mock(State.class);
            when(state.getId()).thenReturn(destinationId);
            when(state.getName()).thenReturn(destination);

            when(stateService.getState(destination)).thenReturn(Optional.of(state));

            when(stateMemory.getActiveStates()).thenReturn(Set.of(1L));
            when(stateNavigator.openState(destinationId))
                    .thenThrow(new RuntimeException("Navigation error"));

            when(screenshotCapture.captureScreenshot(contains("error")))
                    .thenReturn("/error_screenshot.png");

            // Act
            sessionRunner.runTest(destination);

            // Assert
            verify(screenshotCapture).captureScreenshot(contains("error"));
            verify(actionLogger)
                    .logError(
                            anyString(), contains("Navigation error"), eq("/error_screenshot.png"));
            verify(actionLogger).stopVideoRecording(anyString());
            verify(actionLogger)
                    .logObservation(anyString(), eq("TEST_END"), anyString(), eq("INFO"));
        }
    }

    @Nested
    @DisplayName("State Service Integration Tests")
    class StateServiceIntegrationTests {

        @Test
        @DisplayName("Should correctly retrieve and navigate to state")
        void shouldRetrieveAndNavigateToState() {
            // Arrange
            String destination = "TargetState";
            Long destinationId = 15L;

            State targetState = mock(State.class);
            when(targetState.getId()).thenReturn(destinationId);
            when(targetState.getName()).thenReturn(destination);

            State activeState1 = mock(State.class);
            when(activeState1.getId()).thenReturn(1L);

            State activeState2 = mock(State.class);
            when(activeState2.getId()).thenReturn(2L);

            Set<State> activeStates = Set.of(activeState1, activeState2);
            Set<State> finalStates = Set.of(targetState);

            when(stateService.getState(destination)).thenReturn(Optional.of(targetState));

            when(stateMemory.getActiveStates())
                    .thenReturn(Set.of(1L, 2L))
                    .thenReturn(Set.of(destinationId));

            when(stateService.findSetById(Set.of(1L, 2L))).thenReturn(activeStates);

            when(stateService.findSetById(Set.of(destinationId))).thenReturn(finalStates);

            when(stateNavigator.openState(destinationId)).thenReturn(true);

            // Act
            sessionRunner.runTest(destination);

            // Assert
            verify(stateService, times(2)).getState(destination); // Called twice in runTest method
            verify(stateNavigator).openState(destinationId);
            verify(actionLogger)
                    .logStateTransition(
                            anyString(), isNull(), anySet(), anySet(), eq(true), anyLong());
        }

        @Test
        @DisplayName("Should handle state service lookup failure")
        void shouldHandleStateLookupFailure() throws IOException, AWTException {
            // Arrange
            String destination = "InvalidState";

            when(stateService.getState(destination))
                    .thenThrow(new RuntimeException("Database error"));

            when(screenshotCapture.captureScreenshot(anyString())).thenReturn("/error.png");

            // Act
            sessionRunner.runTest(destination);

            // Assert
            verify(actionLogger)
                    .logError(anyString(), contains("Database error"), eq("/error.png"));
            verify(stateNavigator, never()).openState(anyLong());
            verify(actionLogger).stopVideoRecording(anyString());
        }
    }
}
