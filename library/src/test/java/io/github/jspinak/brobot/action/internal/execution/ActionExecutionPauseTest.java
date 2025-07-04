package io.github.jspinak.brobot.action.internal.execution;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.action.internal.utility.ActionSuccessCriteria;
import io.github.jspinak.brobot.control.ExecutionController;
import io.github.jspinak.brobot.control.ExecutionStoppedException;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.ExecutionSession;
import io.github.jspinak.brobot.tools.ml.dataset.DatasetManager;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import io.github.jspinak.brobot.util.image.capture.ScreenshotCapture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionExecutionPauseTest {

    @Mock private TimeProvider time;
    @Mock private IllustrationController illustrateScreenshot;
    @Mock private SearchRegionResolver selectRegions;
    @Mock private ActionLifecycleManagement actionLifecycleManagement;
    @Mock private DatasetManager datasetManager;
    @Mock private ActionSuccessCriteria success;
    @Mock private ActionResultFactory matchesInitializer;
    @Mock private ActionLogger actionLogger;
    @Mock private ScreenshotCapture captureScreenshot;
    @Mock private ExecutionSession automationSession;
    @Mock private ExecutionController executionController;
    @Mock private ActionInterface actionMethod;

    private ActionExecution actionExecution;
    private ActionOptions actionOptions;
    private ObjectCollection objectCollection;

    @BeforeEach
    void setUp() {
        actionExecution = new ActionExecution(
                time, illustrateScreenshot, selectRegions,
                actionLifecycleManagement, datasetManager, success,
                matchesInitializer, actionLogger, captureScreenshot,
                automationSession, executionController
        );

        actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .build();

        StateImage stateImage = new StateImage.Builder()
                .setName("testImage")
                .build();

        objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();

        when(automationSession.getCurrentSessionId()).thenReturn("test-session");
        ActionResult defaultResult = new ActionResult();
        lenient().when(matchesInitializer.init(any(ActionOptions.class), anyString(), any(ObjectCollection[].class)))
                .thenReturn(defaultResult);
        lenient().when(matchesInitializer.init(any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                .thenReturn(defaultResult);
        lenient().when(actionLifecycleManagement.getCurrentDuration(any())).thenReturn(Duration.ofMillis(100));
    }

    @Test
    void testActionExecutionWithoutExecutionController() {
        // Create ActionExecution without ExecutionController
        ActionExecution actionExecutionNoController = new ActionExecution(
                time, illustrateScreenshot, selectRegions,
                actionLifecycleManagement, datasetManager, success,
                matchesInitializer, actionLogger, captureScreenshot,
                automationSession, null
        );

        when(actionLifecycleManagement.isMoreSequencesAllowed(any())).thenReturn(true, false);

        // Should work normally without pause checks
        ActionResult result = actionExecutionNoController.perform(
                actionMethod, "test action", actionOptions, objectCollection);

        assertNotNull(result);
        verify(actionMethod, times(1)).perform(any(), any());
    }

    @Test
    void testCheckPausePointBeforeExecution() throws Exception {
        when(actionLifecycleManagement.isMoreSequencesAllowed(any())).thenReturn(false);

        actionExecution.perform(actionMethod, "test action", actionOptions, objectCollection);

        // Should check pause point once before starting
        verify(executionController, times(1)).checkPausePoint();
    }

    @Test
    void testCheckPausePointDuringSequences() throws Exception {
        // Simulate 3 sequences
        when(actionLifecycleManagement.isMoreSequencesAllowed(any()))
                .thenReturn(true, true, true, false);

        actionExecution.perform(actionMethod, "test action", actionOptions, objectCollection);

        // Should check: 1 before start + 3 before each sequence = 4 total
        verify(executionController, times(4)).checkPausePoint();
        verify(actionMethod, times(3)).perform(any(), any());
    }

    @Test
    void testExecutionStoppedBeforeStart() throws Exception {
        doThrow(new ExecutionStoppedException("Stopped before start"))
                .when(executionController).checkPausePoint();

        assertThrows(ExecutionStoppedException.class, () ->
                actionExecution.perform(actionMethod, "test action", actionOptions, objectCollection)
        );

        // Should not execute the action
        verify(actionMethod, never()).perform(any(), any());
        verify(time, never()).wait(anyDouble());
    }

    @Test
    void testExecutionStoppedDuringSequences() throws Exception {
        when(actionLifecycleManagement.isMoreSequencesAllowed(any()))
                .thenReturn(true, true, false);

        // Stop after first checkPausePoint (which happens before first sequence)
        doNothing()
                .doNothing()
                .doThrow(new ExecutionStoppedException("Stopped during execution"))
                .when(executionController).checkPausePoint();

        assertThrows(ExecutionStoppedException.class, () ->
                actionExecution.perform(actionMethod, "test action", actionOptions, objectCollection)
        );

        // Should execute only once before stopping
        verify(actionMethod, times(1)).perform(any(), any());
    }

    @Test
    void testExecutionInterrupted() throws Exception {
        doThrow(new InterruptedException("Thread interrupted"))
                .when(executionController).checkPausePoint();

        assertThrows(ExecutionStoppedException.class, () ->
                actionExecution.perform(actionMethod, "test action", actionOptions, objectCollection)
        );

        // Verify the interrupted exception was wrapped
        ArgumentCaptor<ExecutionStoppedException> captor = 
                ArgumentCaptor.forClass(ExecutionStoppedException.class);
        try {
            actionExecution.perform(actionMethod, "test action", actionOptions, objectCollection);
        } catch (ExecutionStoppedException e) {
            assertTrue(e.getCause() instanceof InterruptedException);
            assertTrue(Thread.currentThread().isInterrupted());
        }
    }

    @Test
    void testActionResultMarkedAsFailureOnStop() throws Exception {
        lenient().when(actionLifecycleManagement.isMoreSequencesAllowed(any())).thenReturn(true);
        doThrow(new ExecutionStoppedException("Stopped"))
                .when(executionController).checkPausePoint();

        ActionResult mockResult = new ActionResult();
        mockResult.setSuccess(true);
        lenient().when(matchesInitializer.init(any(ActionOptions.class), anyString(), any(ObjectCollection[].class)))
                .thenReturn(mockResult);

        try {
            actionExecution.perform(actionMethod, "test action", actionOptions, objectCollection);
        } catch (ExecutionStoppedException e) {
            // Expected
        }

        assertFalse(mockResult.isSuccess());
    }

    @Test
    void testActionConfigPerformWithPausePoints() throws Exception {
        ActionConfig actionConfig = new ClickOptions.Builder().build();
        when(actionLifecycleManagement.isMoreSequencesAllowed(any()))
                .thenReturn(true, true, false);
        
        // Mock the result from matchesInitializer for ActionConfig
        ActionResult mockResult = new ActionResult();
        lenient().when(matchesInitializer.init(any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                .thenReturn(mockResult);

        actionExecution.perform(actionMethod, "test action", actionConfig, objectCollection);

        // Should check pause points
        verify(executionController, times(3)).checkPausePoint();
        verify(actionMethod, times(2)).perform(any(), any());
    }

    @Test
    void testPausePointsWithMultipleObjectCollections() throws Exception {
        ObjectCollection collection2 = new ObjectCollection.Builder().build();
        when(actionLifecycleManagement.isMoreSequencesAllowed(any()))
                .thenReturn(true, false);

        actionExecution.perform(actionMethod, "test action", actionOptions, 
                objectCollection, collection2);

        verify(executionController, times(2)).checkPausePoint();
    }

    @Test
    void testConcurrentPauseChecks() throws Exception {
        int numThreads = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);

        when(actionLifecycleManagement.isMoreSequencesAllowed(any())).thenReturn(false);

        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    actionExecution.perform(actionMethod, "concurrent action", 
                            actionOptions, objectCollection);
                } catch (Exception e) {
                    // Expected
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(endLatch.await(5, TimeUnit.SECONDS));

        // Each thread should check pause point
        verify(executionController, atLeast(numThreads)).checkPausePoint();
    }

    @Test
    void testPauseBeforeAndAfterWaits() throws Exception {
        actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setPauseBeforeBegin(1.0)
                .setPauseAfterEnd(1.0)
                .build();

        when(actionLifecycleManagement.isMoreSequencesAllowed(any())).thenReturn(false);

        actionExecution.perform(actionMethod, "test action", actionOptions, objectCollection);

        // Verify pause checks happen around waits
        verify(executionController, times(1)).checkPausePoint();
        // Both pauses happen, so verify wait is called twice with 1.0
        verify(time, times(2)).wait(1.0);
    }
}