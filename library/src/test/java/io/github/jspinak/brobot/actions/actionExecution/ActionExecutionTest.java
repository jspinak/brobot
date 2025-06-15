package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionConfigurations.Success;
import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.actionExecution.manageTrainingData.DatasetManager;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.SelectRegions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.illustratedHistory.IllustrateScreenshot;
import io.github.jspinak.brobot.imageUtils.CaptureScreenshot;
import io.github.jspinak.brobot.report.log.ActionLogger;
import io.github.jspinak.brobot.report.log.AutomationSession;
import io.github.jspinak.brobot.report.log.model.LogData;
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
    private Time time;
    @Mock
    private IllustrateScreenshot illustrateScreenshot;
    @Mock
    private SelectRegions selectRegions;
    @Mock
    private ActionLifecycleManagement actionLifecycleManagement;
    @Mock
    private DatasetManager datasetManager;
    @Mock
    private Success success;
    @Mock
    private MatchesInitializer matchesInitializer;
    @Mock
    private ActionLogger actionLogger;
    @Mock
    private CaptureScreenshot captureScreenshot;
    @Mock
    private AutomationSession automationSession;
    @Mock
    private ActionInterface actionInterface;
    @Mock
    private Matches matches;
    @Mock
    private LogData logData;

    private ActionOptions actionOptions;
    private ObjectCollection objectCollection;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        actionOptions = new ActionOptions.Builder().build();
        objectCollection = new ObjectCollection.Builder().build();

        when(automationSession.getCurrentSessionId()).thenReturn("testSession");
        // FIX: Use a more specific matcher for the varargs parameter to avoid ambiguity.
        // This will now correctly stub calls with both empty and non-empty varargs.
        when(matchesInitializer.init(any(ActionOptions.class), anyString(), any(ObjectCollection[].class)))
                .thenReturn(matches);
        when(actionLifecycleManagement.getCurrentDuration(matches)).thenReturn(Duration.ofSeconds(1));
        when(actionLogger.logAction(anyString(), any(), any())).thenReturn(logData);
    }

    @Test
    void perform_shouldExecuteActionOnceWhenMoreSequencesNotAllowedAfterFirstRun() {
        // Setup with distinct pause values
        actionOptions = new ActionOptions.Builder()
                .setPauseBeforeBegin(0.1)
                .setPauseAfterEnd(0.2)
                .build();
        when(actionLifecycleManagement.isMoreSequencesAllowed(matches)).thenReturn(true, false);

        Matches result = actionExecution.perform(actionInterface, "test action", actionOptions, objectCollection);

        // Verification
        verify(matchesInitializer).init(actionOptions, "test action", new ObjectCollection[]{objectCollection});
        verify(time).wait(0.1); // Verify specific pause before
        verify(actionInterface).perform(matches, new ObjectCollection[]{objectCollection});
        verify(actionLifecycleManagement).incrementCompletedSequences(matches);
        verify(success).set(actionOptions, matches);
        verify(illustrateScreenshot).illustrateWhenAllowed(eq(matches), any(), eq(actionOptions), any());
        verify(time).wait(0.2); // Verify specific pause after
        verify(matches).setDuration(Duration.ofSeconds(1));
        verify(actionLogger).logAction("testSession", matches, objectCollection);
        assertEquals(matches, result);
    }

    @Test
    void perform_shouldExecuteActionMultipleTimes() {
        // Setup
        when(actionLifecycleManagement.isMoreSequencesAllowed(matches)).thenReturn(true, true, true, false);

        actionExecution.perform(actionInterface, "test action", actionOptions, objectCollection);

        // Verification
        verify(actionInterface, times(3)).perform(matches, new ObjectCollection[]{objectCollection});
        verify(actionLifecycleManagement, times(3)).incrementCompletedSequences(matches);
    }

    @Test
    void perform_shouldNotAddDataToDatasetWhenBuildDatasetIsFalse() {
        // Setup
        BrobotSettings.buildDataset = false;
        when(actionLifecycleManagement.isMoreSequencesAllowed(matches)).thenReturn(true, false);

        actionExecution.perform(actionInterface, "test action", actionOptions, objectCollection);

        // Verification
        verify(datasetManager, never()).addSetOfData(any());
    }

    @Test
    void perform_shouldAddDataToDatasetWhenBuildDatasetIsTrue() {
        // Setup
        BrobotSettings.buildDataset = true;
        when(actionLifecycleManagement.isMoreSequencesAllowed(matches)).thenReturn(true, false);

        actionExecution.perform(actionInterface, "test action", actionOptions, objectCollection);

        // Verification
        verify(datasetManager).addSetOfData(matches);
    }

    @Test
    void perform_shouldNotThrowExceptionWhenLoggingWithEmptyObjectCollection() {
        // Setup
        when(actionLifecycleManagement.isMoreSequencesAllowed(matches)).thenReturn(true, false);
        // The redundant stubbing here is no longer necessary due to the fix in setUp().

        // Execute with no ObjectCollections
        actionExecution.perform(actionInterface, "test action", actionOptions);

        // Verify logger is not called, preventing the exception
        verify(actionLogger, never()).logAction(anyString(), any(), any());
        // Verify other methods were still called on the non-null matches object
        verify(matches).setDuration(any(Duration.class));
    }
}
