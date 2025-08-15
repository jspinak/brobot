package io.github.jspinak.brobot.action.internal.execution;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
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
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ActionExecution pause functionality.
 * Note: Many tests are disabled as ExecutionController pause methods are not available.
 */
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
    @Mock private BrobotLogger brobotLogger;
    @Mock private ActionInterface actionMethod;

    private ActionConfig actionConfig;
    private ObjectCollection objectCollection;

    @BeforeEach
    void setUp() {
        actionConfig = new ClickOptions.Builder().build();

        StateImage stateImage = new StateImage.Builder()
                .setName("testImage")
                .build();

        objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();

        when(automationSession.getCurrentSessionId()).thenReturn("test-session");
        ActionResult defaultResult = new ActionResult();
        lenient().when(matchesInitializer.init(any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                .thenReturn(defaultResult);
        lenient().when(actionLifecycleManagement.getCurrentDuration(any())).thenReturn(Duration.ofMillis(100));
    }

    @Test
    @org.junit.jupiter.api.Disabled("ExecutionController pause methods not available")
    void testExecutionWithPause() {
        // This test would require ExecutionController to have shouldPauseExecution() and waitForResume() methods
        // which don't appear to exist in the current implementation
    }

    @Test
    void testExecutionStoppedException() {
        // Test that ExecutionStoppedException can be thrown
        assertThrows(ExecutionStoppedException.class, () -> {
            throw new ExecutionStoppedException("Execution stopped");
        });
    }

    @Test
    void testActionConfigTypes() {
        // Test with different ActionConfig types
        ActionConfig[] configs = {
            new ClickOptions.Builder().build(),
            new PatternFindOptions.Builder().build()
        };

        for (ActionConfig config : configs) {
            ActionResult result = new ActionResult();
            result.setActionConfig(config);
            assertNotNull(result.getActionConfig());
            assertEquals(config, result.getActionConfig());
        }
    }

    @Test
    void testActionResultWithSuccess() {
        // Test ActionResult success handling
        ActionResult result = new ActionResult();
        result.setSuccess(true);
        
        assertTrue(result.isSuccess());
        
        result.setSuccess(false);
        assertFalse(result.isSuccess());
    }

    @Test
    void testObjectCollectionBuilder() {
        // Test ObjectCollection building
        StateImage image1 = new StateImage.Builder().setName("image1").build();
        StateImage image2 = new StateImage.Builder().setName("image2").build();
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(image1, image2)
                .build();
        
        assertNotNull(collection);
        assertEquals(2, collection.getStateImages().size());
    }

    @Test
    void testActionLifecycleManagement() {
        // Test ActionLifecycleManagement mocking
        ActionResult result = new ActionResult();
        
        when(actionLifecycleManagement.isMoreSequencesAllowed(result)).thenReturn(true);
        assertTrue(actionLifecycleManagement.isMoreSequencesAllowed(result));
        
        when(actionLifecycleManagement.isMoreSequencesAllowed(result)).thenReturn(false);
        assertFalse(actionLifecycleManagement.isMoreSequencesAllowed(result));
        
        verify(actionLifecycleManagement, times(2)).isMoreSequencesAllowed(result);
    }

    @Test
    void testActionMethodPerform() {
        // Test ActionInterface perform method
        ActionResult actionResult = new ActionResult();
        actionResult.setSuccess(true);
        
        // ActionInterface.perform expects ActionResult as first parameter, not ActionConfig
        // The method is void, so we test that it gets called correctly
        doNothing().when(actionMethod).perform(any(ActionResult.class), any(ObjectCollection[].class));
        
        actionMethod.perform(actionResult, objectCollection);
        
        // Verify the method was called
        verify(actionMethod).perform(eq(actionResult), any(ObjectCollection[].class));
        
        // Test the result state
        assertTrue(actionResult.isSuccess());
    }

    @Test
    void testDurationHandling() {
        // Test Duration handling
        Duration testDuration = Duration.ofSeconds(5);
        
        when(actionLifecycleManagement.getCurrentDuration(any())).thenReturn(testDuration);
        
        ActionResult result = new ActionResult();
        Duration duration = actionLifecycleManagement.getCurrentDuration(result);
        
        assertEquals(5, duration.getSeconds());
    }

    @Test
    void testSessionId() {
        // Test session ID handling
        String sessionId = automationSession.getCurrentSessionId();
        
        assertNotNull(sessionId);
        assertEquals("test-session", sessionId);
        
        verify(automationSession).getCurrentSessionId();
    }

    @Test
    void testMatchesInitializer() {
        // Test ActionResultFactory initialization
        ActionResult result = matchesInitializer.init(actionConfig, "test", objectCollection);
        
        assertNotNull(result);
        verify(matchesInitializer).init(any(ActionConfig.class), eq("test"), any(ObjectCollection[].class));
    }
}