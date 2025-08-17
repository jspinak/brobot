package io.github.jspinak.brobot.action.internal.execution;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.internal.execution.ActionExecution;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycleManagement;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.action.internal.utility.ActionSuccessCriteria;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.ExecutionSession;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.ml.dataset.DatasetManager;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import io.github.jspinak.brobot.util.image.capture.ScreenshotCapture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ActionExecutionTest {

    @InjectMocks
    private ActionExecution actionExecution;

    @Mock
    private TimeProvider time;
    @Mock
    private IllustrationController illustrateScreenshot;
    @Mock
    private SearchRegionResolver selectRegions;
    @Mock
    private ActionLifecycleManagement actionLifecycleManagement;
    @Mock
    private DatasetManager datasetManager;
    @Mock
    private ActionSuccessCriteria success;
    @Mock
    private ActionResultFactory matchesInitializer;
    @Mock
    private ActionLogger actionLogger;
    @Mock
    private ScreenshotCapture captureScreenshot;
    @Mock
    private ExecutionSession automationSession;
    @Mock
    private ActionInterface actionInterface;
    @Mock
    private ActionResult matches;
    @Mock
    private LogData logData;

    private ActionConfig actionConfig;
    private ObjectCollection objectCollection;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        actionConfig = new PatternFindOptions.Builder().build();
        objectCollection = new ObjectCollection.Builder().build();

        when(automationSession.getCurrentSessionId()).thenReturn("testSession");
        // Stub for ActionConfig
        when(matchesInitializer.init(any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                .thenReturn(matches);
        when(actionLifecycleManagement.getCurrentDuration(matches)).thenReturn(Duration.ofSeconds(1));
        when(actionLogger.logAction(anyString(), any(), any())).thenReturn(logData);
    }

    @Test
    void perform_shouldExecuteActionOnceWhenMoreSequencesNotAllowedAfterFirstRun() {
        // Setup with PatternFindOptions
        actionConfig = new PatternFindOptions.Builder()
                .setPauseBeforeBegin(0.1)
                .setPauseAfterEnd(0.2)
                .build();
        when(actionLifecycleManagement.isMoreSequencesAllowed(matches)).thenReturn(true, false);

        ActionResult result = actionExecution.perform(actionInterface, "test action", actionConfig, objectCollection);

        // Verification
        verify(matchesInitializer).init(actionConfig, "test action", new ObjectCollection[]{objectCollection});
        verify(time).wait(0.1); // Verify specific pause before
        verify(actionInterface).perform(matches, new ObjectCollection[]{objectCollection});
        verify(actionLifecycleManagement).incrementCompletedSequences(matches);
        verify(success).set(actionConfig, matches);
        verify(illustrateScreenshot).illustrateWhenAllowed(eq(matches), any(), eq(actionConfig), any());
        verify(time).wait(0.2); // Verify specific pause after
        verify(matches).setDuration(Duration.ofSeconds(1));
        verify(actionLogger).logAction("testSession", matches, objectCollection);
        assertEquals(matches, result);
    }

    @Test
    void perform_shouldExecuteActionMultipleTimes() {
        // Setup
        when(actionLifecycleManagement.isMoreSequencesAllowed(matches)).thenReturn(true, true, true, false);

        actionExecution.perform(actionInterface, "test action", actionConfig, objectCollection);

        // Verification
        verify(actionInterface, times(3)).perform(matches, new ObjectCollection[]{objectCollection});
        verify(actionLifecycleManagement, times(3)).incrementCompletedSequences(matches);
    }

    @Test
    void perform_shouldNotAddDataToDatasetWhenBuildDatasetIsFalse() {
        // Setup
        FrameworkSettings.buildDataset = false;
        when(actionLifecycleManagement.isMoreSequencesAllowed(matches)).thenReturn(true, false);

        actionExecution.perform(actionInterface, "test action", actionConfig, objectCollection);

        // Verification
        verify(datasetManager, never()).addSetOfData(any());
    }

    @Test
    void perform_shouldAddDataToDatasetWhenBuildDatasetIsTrue() {
        // Setup
        FrameworkSettings.buildDataset = true;
        when(actionLifecycleManagement.isMoreSequencesAllowed(matches)).thenReturn(true, false);

        actionExecution.perform(actionInterface, "test action", actionConfig, objectCollection);

        // Verification
        verify(datasetManager).addSetOfData(matches);
    }

    @Test
    void perform_shouldNotThrowExceptionWhenLoggingWithEmptyObjectCollection() {
        // Setup
        when(actionLifecycleManagement.isMoreSequencesAllowed(matches)).thenReturn(true, false);
        // The redundant stubbing here is no longer necessary due to the fix in setUp().

        // Execute with no ObjectCollections
        actionExecution.perform(actionInterface, "test action", actionConfig);

        // Verify logger is not called, preventing the exception
        verify(actionLogger, never()).logAction(anyString(), any(), any());
        // Verify other methods were still called on the non-null matches object
        verify(matches).setDuration(any(Duration.class));
    }
    
    @Test
    void perform_shouldExecuteActionWithActionConfig() {
        // Setup using the new ActionConfig API
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setPauseBeforeBegin(0.1)
            .setPauseAfterEnd(0.2)
            .setSimilarity(0.9)
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
        
        when(actionLifecycleManagement.isMoreSequencesAllowed(matches)).thenReturn(true, false);
        
        // Execute using ActionConfig
        ActionResult result = actionExecution.perform(actionInterface, "find best match", findOptions, objectCollection);
        
        // Verification
        verify(matchesInitializer).init(findOptions, "find best match", new ObjectCollection[]{objectCollection});
        verify(time).wait(0.1); // Verify pause before
        verify(actionInterface).perform(matches, new ObjectCollection[]{objectCollection});
        verify(actionLifecycleManagement).incrementCompletedSequences(matches);
        verify(success).set(any(ActionConfig.class), eq(matches));
        verify(illustrateScreenshot).illustrateWhenAllowed(eq(matches), any(), eq(findOptions), any());
        verify(time).wait(0.2); // Verify pause after
        verify(matches).setDuration(Duration.ofSeconds(1));
        verify(actionLogger).logAction("testSession", matches, objectCollection);
        assertEquals(matches, result);
    }
    
    @Test
    void perform_shouldExecuteActionMultipleTimesWithActionConfig() {
        // Test multiple executions with the new API
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        when(actionLifecycleManagement.isMoreSequencesAllowed(matches)).thenReturn(true, true, true, false);
        
        actionExecution.perform(actionInterface, "test action", findOptions, objectCollection);
        
        // Verification
        verify(actionInterface, times(3)).perform(matches, new ObjectCollection[]{objectCollection});
        verify(actionLifecycleManagement, times(3)).incrementCompletedSequences(matches);
    }
    
    @Test
    void perform_shouldAddDataToDatasetWhenBuildDatasetIsTrueWithActionConfig() {
        // Test dataset building with the new API
        FrameworkSettings.buildDataset = true;
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        when(actionLifecycleManagement.isMoreSequencesAllowed(matches)).thenReturn(true, false);
        
        actionExecution.perform(actionInterface, "test action", findOptions, objectCollection);
        
        // Verification
        verify(datasetManager).addSetOfData(matches);
        
        // Cleanup
        FrameworkSettings.buildDataset = false;
    }
}
