package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Movement;
import io.github.jspinak.brobot.model.element.Text;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.action.result.*;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycle;
import io.github.jspinak.brobot.model.action.ActionRecord;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ActionResult's component architecture (Version 2.0).
 * Tests the delegation to specialized component classes.
 */
@DisplayName("ActionResult Component Tests")
public class ActionResultComponentTest extends BrobotTestBase {
    
    private ActionResult actionResult;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        actionResult = new ActionResult();
    }
    
    @Nested
    @DisplayName("MatchCollection Component")
    class MatchCollectionComponent {
        
        @Test
        @DisplayName("Should delegate match operations to MatchCollection")
        void shouldDelegateMatchOperationsToMatchCollection() {
            // Setup
            Match match1 = createMockMatch(0.9);
            Match match2 = createMockMatch(0.8);
            
            // Add matches
            actionResult.add(match1, match2);
            
            // Verify delegation to MatchCollection
            MatchCollection matchCollection = actionResult.getMatchCollection();
            assertNotNull(matchCollection);
            assertEquals(2, matchCollection.size());
            assertTrue(matchCollection.getMatches().contains(match1));
            assertTrue(matchCollection.getMatches().contains(match2));
        }
        
        @Test
        @DisplayName("Should maintain initial matches separately")
        void shouldMaintainInitialMatchesSeparately() {
            List<Match> initial = Arrays.asList(
                createMockMatch(0.9),
                createMockMatch(0.8)
            );
            
            actionResult.setInitialMatchList(initial);
            
            // Add more matches
            actionResult.add(createMockMatch(0.7));
            
            // Initial matches should be preserved
            assertEquals(2, actionResult.getInitialMatchList().size());
            assertEquals(1, actionResult.getMatchList().size()); // Only the added match
        }
    }
    
    @Nested
    @DisplayName("TimingData Component")
    class TimingDataComponent {
        
        @Test
        @DisplayName("Should track timing information")
        void shouldTrackTimingInformation() {
            TimingData timing = actionResult.getTimingData();
            assertNotNull(timing);
            
            // TimingData auto-starts on creation
            assertNotNull(timing.getStartTime());
            
            // Stop timing
            timing.stop();
            
            assertNotNull(timing.getEndTime());
            assertNotNull(timing.getTotalDuration());
        }
    }
    
    @Nested
    @DisplayName("TextExtractionResult Component")
    class TextExtractionResultComponent {
        
        @Test
        @DisplayName("Should have text extraction result component")
        void shouldHaveTextExtractionResultComponent() {
            TextExtractionResult textResult = actionResult.getTextResult();
            assertNotNull(textResult);
        }
    }
    
    @Nested
    @DisplayName("StateTracker Component")
    class StateTrackerComponent {
        
        @Test
        @DisplayName("Should track state changes")
        void shouldTrackStateChanges() {
            StateTracker stateTracker = actionResult.getStateTracker();
            assertNotNull(stateTracker);
            
            // Track state activations
            stateTracker.recordActiveState("State1");
            stateTracker.recordActiveState("State2");
            stateTracker.recordActiveState("State3");
            
            Set<String> activeStates = stateTracker.getActiveStates();
            assertEquals(3, activeStates.size());
            assertTrue(activeStates.contains("State1"));
            assertTrue(activeStates.contains("State2"));
            assertTrue(activeStates.contains("State3"));
        }
        
        @Test
        @DisplayName("Should process matches for state information")
        void shouldProcessMatchesForStateInformation() {
            StateTracker stateTracker = actionResult.getStateTracker();
            
            // Create match with state information
            Match match = mock(Match.class);
            StateImage stateImage = mock(StateImage.class);
            when(stateImage.getOwnerStateName()).thenReturn("TestState");
            StateObjectMetadata metadata = mock(StateObjectMetadata.class);
            when(metadata.getOwnerStateName()).thenReturn("TestState");
            when(match.getStateObjectData()).thenReturn(metadata);
            
            // Process match
            stateTracker.processMatch(match);
            
            // Should track the state from the match
            assertTrue(stateTracker.getActiveStates().contains("TestState"));
        }
    }
    
    @Nested
    @DisplayName("RegionManager Component")
    class RegionManagerComponent {
        
        @Test
        @DisplayName("Should have region manager component")
        void shouldHaveRegionManagerComponent() {
            RegionManager regionManager = actionResult.getRegionManager();
            assertNotNull(regionManager);
        }
    }
    
    @Nested
    @DisplayName("MovementTracker Component")
    class MovementTrackerComponent {
        
        @Test
        @DisplayName("Should have movement tracker component")
        void shouldHaveMovementTrackerComponent() {
            MovementTracker movementTracker = actionResult.getMovementTracker();
            assertNotNull(movementTracker);
        }
    }
    
    @Nested
    @DisplayName("ActionAnalysis Component")
    class ActionAnalysisComponent {
        
        @Test
        @DisplayName("Should have action analysis component")
        void shouldHaveActionAnalysisComponent() {
            ActionAnalysis analysis = actionResult.getActionAnalysis();
            assertNotNull(analysis);
        }
    }
    
    @Nested
    @DisplayName("ExecutionHistory Component")
    class ExecutionHistoryComponent {
        
        @Test
        @DisplayName("Should have execution history component")
        void shouldHaveExecutionHistoryComponent() {
            List<ActionRecord> history = actionResult.getExecutionHistory();
            assertNotNull(history);
        }
    }
    
    @Nested
    @DisplayName("ActionLifecycle Integration")
    class ActionLifecycleIntegration {
        
        @Test
        @DisplayName("Should integrate with ActionLifecycle")
        void shouldIntegrateWithActionLifecycle() {
            ActionLifecycle lifecycle = mock(ActionLifecycle.class);
            actionResult.setActionLifecycle(lifecycle);
            
            assertEquals(lifecycle, actionResult.getActionLifecycle());
        }
    }
    
    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("Should handle complete action workflow")
        void shouldHandleCompleteActionWorkflow() {
            // Setup action configuration
            ActionConfig config = mock(ActionConfig.class);
            actionResult.setActionConfig(config);
            
            // Add matches
            actionResult.add(
                createMockMatch(0.95),
                createMockMatch(0.85),
                createMockMatch(0.75)
            );
            
            // Track state
            actionResult.getStateTracker().recordActiveState("TestState");
            
            // Complete timing
            actionResult.getTimingData().stop();
            
            // Set success
            actionResult.setSuccess(true);
            actionResult.setActionDescription("Complex action completed");
            
            // Verify complete state
            assertTrue(actionResult.isSuccess());
            assertEquals(3, actionResult.size());
            assertFalse(actionResult.getStateTracker().getActiveStates().isEmpty());
            assertNotNull(actionResult.getTimingData().getEndTime());
        }
    }
    
    // Helper methods
    private Match createMockMatch(double score) {
        Match match = mock(Match.class);
        when(match.getScore()).thenReturn(score);
        when(match.getRegion()).thenReturn(new Region(0, 0, 50, 50));
        when(match.getTarget()).thenReturn(new Location(25, 25));
        return match;
    }
}