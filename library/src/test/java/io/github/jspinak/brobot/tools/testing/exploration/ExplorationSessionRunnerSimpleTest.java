package io.github.jspinak.brobot.tools.testing.exploration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

/** Simplified test suite for ExplorationSessionRunner focusing on core functionality. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExplorationSessionRunnerSimpleTest extends BrobotTestBase {

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

    @Test
    @DisplayName("Should execute successful test session")
    void shouldExecuteSuccessfulTest() throws IOException, AWTException {
        // Arrange
        String destination = "LoginState";
        Long destinationId = 42L;

        State loginState = mock(State.class);
        when(loginState.getId()).thenReturn(destinationId);
        when(loginState.getName()).thenReturn(destination);

        when(stateService.getState(destination)).thenReturn(Optional.of(loginState));

        when(stateMemory.getActiveStates()).thenReturn(Set.of(1L));
        when(stateNavigator.openState(destinationId)).thenReturn(true);
        when(stateService.findSetById(anySet())).thenReturn(new HashSet<>());

        // Act
        sessionRunner.runTest(destination);

        // Assert - Just verify key methods were called
        verify(actionLogger).startVideoRecording(anyString());
        verify(stateNavigator).openState(destinationId);
        verify(actionLogger).stopVideoRecording(anyString());
    }

    @Test
    @DisplayName("Should handle transition failure")
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
        when(screenshotCapture.captureScreenshot(anyString())).thenReturn("/screenshot.png");

        // Act
        sessionRunner.runTest(destination);

        // Assert
        verify(stateNavigator).openState(destinationId);
        verify(screenshotCapture).captureScreenshot(contains("transition_failed"));
        verify(actionLogger).stopVideoRecording(anyString());
    }

    @Test
    @DisplayName("Should handle state not found")
    void shouldHandleStateNotFound() throws IOException, AWTException {
        // Arrange
        String destination = "NonExistentState";
        when(stateService.getState(destination)).thenReturn(Optional.empty());
        when(screenshotCapture.captureScreenshot(anyString())).thenReturn("/error.png");

        // Act
        sessionRunner.runTest(destination);

        // Assert
        verify(stateNavigator, never()).openState(anyLong());
        verify(screenshotCapture).captureScreenshot(contains("error"));
        verify(actionLogger).stopVideoRecording(anyString());
    }

    @Test
    @DisplayName("Should log performance metrics")
    void shouldLogPerformanceMetrics() throws IOException, AWTException {
        // Arrange
        String destination = "TestState";
        Long destinationId = 10L;

        State testState = mock(State.class);
        when(testState.getId()).thenReturn(destinationId);
        when(testState.getName()).thenReturn(destination);

        when(stateService.getState(destination)).thenReturn(Optional.of(testState));

        when(stateMemory.getActiveStates()).thenReturn(Set.of(1L));
        when(stateNavigator.openState(destinationId)).thenReturn(true);
        when(stateService.findSetById(anySet())).thenReturn(new HashSet<>());

        // Act
        sessionRunner.runTest(destination);

        // Assert
        verify(actionLogger).logPerformanceMetrics(anyString(), anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("Should handle exception during execution")
    void shouldHandleExecutionException() throws IOException, AWTException {
        // Arrange
        String destination = "ExceptionState";

        when(stateService.getState(destination)).thenThrow(new RuntimeException("Test exception"));
        when(screenshotCapture.captureScreenshot(anyString())).thenReturn("/error.png");

        // Act
        sessionRunner.runTest(destination);

        // Assert
        verify(screenshotCapture).captureScreenshot(contains("error"));
        verify(actionLogger).logError(anyString(), contains("Test exception"), anyString());
        verify(actionLogger).stopVideoRecording(anyString());
    }
}
