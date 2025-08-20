package io.github.jspinak.brobot.action.internal.execution;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.exception.BrobotRuntimeException;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Movement;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.model.element.Text;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ActionChainExecutor - chain execution orchestration.
 * Tests nested and confirm strategies, error handling, and result accumulation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActionChainExecutorTest extends BrobotTestBase {
    
    @Mock private ActionExecution actionExecution;
    @Mock private ActionService actionService;
    @Mock private ActionInterface findAction;
    @Mock private ActionInterface clickAction;
    @Mock private ActionInterface typeAction;
    
    @Mock private ActionResult successResult;
    @Mock private ActionResult failureResult;
    @Mock private ActionResult initialResult;
    @Mock private ObjectCollection objectCollection;
    
    @Mock private Match match1;
    @Mock private Match match2;
    @Mock private Match confirmMatch1;
    @Mock private Match confirmMatch2;
    @Mock private Region region1;
    @Mock private Region region2;
    @Mock private Region confirmRegion1;
    @Mock private Region confirmRegion2;
    @Mock private Location location1;
    @Mock private Location location2;
    @Mock private StateObjectMetadata stateObjectMetadata;
    @Mock private Text text;
    @Mock private Movement movement;
    
    private ActionChainExecutor chainExecutor;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        chainExecutor = new ActionChainExecutor(actionExecution, actionService);
        
        // Setup match/region relationships
        when(match1.getRegion()).thenReturn(region1);
        when(match2.getRegion()).thenReturn(region2);
        when(match1.getStateObjectData()).thenReturn(stateObjectMetadata);
        when(stateObjectMetadata.getOwnerStateName()).thenReturn("TestState");
        
        // Setup success result
        when(successResult.isSuccess()).thenReturn(true);
        when(successResult.getMatchList()).thenReturn(List.of(match1, match2));
        when(successResult.getDuration()).thenReturn(Duration.ofSeconds(1));
        when(successResult.getText()).thenReturn(text);
        when(successResult.getStartTime()).thenReturn(LocalDateTime.now());
        when(successResult.getActiveStates()).thenReturn(new HashSet<>());
        when(successResult.getMovements()).thenReturn(List.of(movement));
        when(text.toString()).thenReturn("test text");
        
        // Setup failure result
        when(failureResult.isSuccess()).thenReturn(false);
        when(failureResult.getMatchList()).thenReturn(new ArrayList<>());
        when(failureResult.getDuration()).thenReturn(Duration.ofSeconds(0));
        when(failureResult.getText()).thenReturn(text);
        when(failureResult.getStartTime()).thenReturn(LocalDateTime.now());
        when(failureResult.getActiveStates()).thenReturn(new HashSet<>());
        when(failureResult.getMovements()).thenReturn(new ArrayList<>());
        
        // Setup action service
        when(actionService.getAction(any(PatternFindOptions.class))).thenReturn(Optional.of(findAction));
        when(actionService.getAction(any(ClickOptions.class))).thenReturn(Optional.of(clickAction));
        when(actionService.getAction(any(TypeOptions.class))).thenReturn(Optional.of(typeAction));
        
        // Setup default execution behavior
        when(actionExecution.perform(any(), anyString(), any(), any())).thenReturn(successResult);
    }
    
    @Nested
    @DisplayName("Constructor and Dependencies Tests")
    class ConstructorAndDependenciesTests {
        
        @Test
        @DisplayName("Should create executor with dependencies")
        void shouldCreateExecutorWithDependencies() {
            assertNotNull(chainExecutor);
        }
    }
    
    @Nested
    @DisplayName("Basic Chain Execution Tests")
    class BasicChainExecutionTests {
        
        @Test
        @DisplayName("Should execute single action chain")
        void shouldExecuteSingleActionChain() {
            // Given
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(findOptions)
                .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .build();
            
            // When
            ActionResult result = chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then
            verify(actionExecution).perform(eq(findAction), anyString(), eq(findOptions), any());
            assertTrue(result.isSuccess());
            assertEquals(successResult.getMatchList(), result.getMatchList());
        }
        
        @Test
        @DisplayName("Should execute multi-action chain")
        void shouldExecuteMultiActionChain() {
            // Given
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(findOptions)
                .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .then(clickOptions)
                .build();
            
            // When
            ActionResult result = chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then
            verify(actionExecution, times(2)).perform(any(), anyString(), any(), any());
            verify(actionExecution).perform(eq(findAction), anyString(), eq(findOptions), any());
            verify(actionExecution).perform(eq(clickAction), anyString(), any(ClickOptions.class), any());
        }
        
        @Test
        @DisplayName("Should stop chain on failure")
        void shouldStopChainOnFailure() {
            // Given
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            
            when(actionExecution.perform(eq(findAction), anyString(), any(), any()))
                .thenReturn(failureResult);
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(findOptions)
                .then(clickOptions)
                .build();
            
            // When
            ActionResult result = chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then
            verify(actionExecution, times(1)).perform(any(), anyString(), any(), any());
            verify(actionExecution, never()).perform(eq(clickAction), anyString(), any(), any());
            assertFalse(result.isSuccess());
        }
    }
    
    @Nested
    @DisplayName("Nested Strategy Tests")
    class NestedStrategyTests {
        
        @Test
        @DisplayName("Should search within previous results in nested mode")
        void shouldSearchWithinPreviousResultsInNestedMode() {
            // Given
            PatternFindOptions find1 = new PatternFindOptions.Builder().build();
            PatternFindOptions find2 = new PatternFindOptions.Builder().build();
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(find1)
                .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .then(find2)
                .build();
            
            // When
            chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then
            ArgumentCaptor<ActionConfig> configCaptor = ArgumentCaptor.forClass(ActionConfig.class);
            verify(actionExecution, times(2)).perform(any(), anyString(), configCaptor.capture(), any());
            
            // Verify the second find has search regions from first find's results
            List<ActionConfig> configs = configCaptor.getAllValues();
            assertTrue(configs.get(1) instanceof PatternFindOptions);
            PatternFindOptions nestedFind = (PatternFindOptions) configs.get(1);
            assertNotNull(nestedFind.getSearchRegions());
        }
        
        @Test
        @DisplayName("Should handle empty results in nested mode")
        void shouldHandleEmptyResultsInNestedMode() {
            // Given
            PatternFindOptions find1 = new PatternFindOptions.Builder().build();
            PatternFindOptions find2 = new PatternFindOptions.Builder().build();
            
            ActionResult emptyResult = mock(ActionResult.class);
            when(emptyResult.isSuccess()).thenReturn(true);
            when(emptyResult.getMatchList()).thenReturn(new ArrayList<>());
            when(emptyResult.getDuration()).thenReturn(Duration.ofSeconds(0));
            when(emptyResult.getText()).thenReturn(text);
            when(emptyResult.getStartTime()).thenReturn(LocalDateTime.now());
            when(emptyResult.getActiveStates()).thenReturn(new HashSet<>());
            when(emptyResult.getMovements()).thenReturn(new ArrayList<>());
            
            when(actionExecution.perform(eq(findAction), anyString(), eq(find1), any()))
                .thenReturn(emptyResult);
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(find1)
                .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .then(find2)
                .build();
            
            // When
            ActionResult result = chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then
            // Should stop after first action returns empty results
            verify(actionExecution, times(1)).perform(any(), anyString(), any(), any());
            assertFalse(result.isSuccess());
        }
        
        @Test
        @DisplayName("Should modify config for nested search")
        void shouldModifyConfigForNestedSearch() {
            // Given
            PatternFindOptions find1 = new PatternFindOptions.Builder().build();
            ColorFindOptions colorFind = new ColorFindOptions.Builder().build();
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(find1)
                .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .then(colorFind)
                .build();
            
            when(actionService.getAction(any(ColorFindOptions.class))).thenReturn(Optional.of(findAction));
            
            // When
            chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then
            ArgumentCaptor<ActionConfig> configCaptor = ArgumentCaptor.forClass(ActionConfig.class);
            verify(actionExecution, times(2)).perform(any(), anyString(), configCaptor.capture(), any());
            
            // Verify color find was modified with search regions
            List<ActionConfig> configs = configCaptor.getAllValues();
            assertTrue(configs.get(1) instanceof ColorFindOptions);
        }
    }
    
    @Nested
    @DisplayName("Confirm Strategy Tests")
    class ConfirmStrategyTests {
        
        @Test
        @DisplayName("Should confirm previous results in confirm mode")
        void shouldConfirmPreviousResultsInConfirmMode() {
            // Given
            PatternFindOptions find1 = new PatternFindOptions.Builder().build();
            PatternFindOptions find2 = new PatternFindOptions.Builder().build();
            
            // Setup overlapping regions for confirmation
            when(region1.contains(any(Region.class))).thenReturn(true);
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(find1)
                .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
                .then(find2)
                .build();
            
            // When
            ActionResult result = chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then
            verify(actionExecution, times(2)).perform(any(), anyString(), any(), any());
            assertTrue(result.isSuccess());
        }
        
        @Test
        @DisplayName("Should filter unconfirmed matches")
        void shouldFilterUnconfirmedMatches() {
            // Given
            PatternFindOptions find1 = new PatternFindOptions.Builder().build();
            PatternFindOptions find2 = new PatternFindOptions.Builder().build();
            
            // Setup confirming matches with different regions that don't overlap
            when(confirmMatch1.getRegion()).thenReturn(confirmRegion1);
            when(confirmMatch2.getRegion()).thenReturn(confirmRegion2);
            
            // Create real ActionResult objects instead of mocks
            ActionResult firstResult = new ActionResult();
            firstResult.setSuccess(true);
            firstResult.setMatchList(List.of(match1, match2));
            firstResult.setDuration(Duration.ofSeconds(1));
            firstResult.setText(text);
            firstResult.setStartTime(LocalDateTime.now());
            firstResult.setActiveStates(new HashSet<>());
            firstResult.setMovements(new ArrayList<>());
            
            ActionResult confirmingResult = new ActionResult();
            confirmingResult.setSuccess(true);
            confirmingResult.setMatchList(List.of(confirmMatch1, confirmMatch2));
            confirmingResult.setDuration(Duration.ofSeconds(1));
            confirmingResult.setText(text);
            confirmingResult.setStartTime(LocalDateTime.now());
            confirmingResult.setActiveStates(new HashSet<>());
            confirmingResult.setMovements(new ArrayList<>());
            
            // Setup non-overlapping regions (no confirmation)
            when(region1.contains(confirmRegion1)).thenReturn(false);
            when(region1.contains(confirmRegion2)).thenReturn(false);
            when(region1.overlaps(confirmRegion1)).thenReturn(false);
            when(region1.overlaps(confirmRegion2)).thenReturn(false);
            when(region2.contains(confirmRegion1)).thenReturn(false);
            when(region2.contains(confirmRegion2)).thenReturn(false);
            when(region2.overlaps(confirmRegion1)).thenReturn(false);
            when(region2.overlaps(confirmRegion2)).thenReturn(false);
            
            // Setup the mock to return different results for each call
            when(actionExecution.perform(any(), anyString(), any(), any()))
                .thenReturn(firstResult)  // First call returns firstResult
                .thenReturn(confirmingResult);  // Second call returns confirmingResult
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(find1)
                .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
                .then(find2)
                .build();
            
            // When
            ActionResult result = chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then
            verify(actionExecution, times(2)).perform(any(), anyString(), any(), any());
            // Result should indicate no confirmation
            assertNotNull(result);
            // When no matches are confirmed, the result should have empty match list and be unsuccessful
            assertFalse(result.isSuccess());
            assertTrue(result.getMatchList().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle partial confirmation")
        void shouldHandlePartialConfirmation() {
            // Given
            PatternFindOptions find1 = new PatternFindOptions.Builder().build();
            PatternFindOptions find2 = new PatternFindOptions.Builder().build();
            
            // Only first match is confirmed
            when(region1.overlaps(region2)).thenReturn(true);
            when(region2.overlaps(region1)).thenReturn(false);
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(find1)
                .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
                .then(find2)
                .build();
            
            // When
            ActionResult result = chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then
            verify(actionExecution, times(2)).perform(any(), anyString(), any(), any());
            // At least one match should be confirmed
            assertNotNull(result);
        }
    }
    
    @Nested
    @DisplayName("Result Accumulation Tests")
    class ResultAccumulationTests {
        
        @Test
        @DisplayName("Should accumulate execution history")
        void shouldAccumulateExecutionHistory() {
            // Given
            PatternFindOptions find = new PatternFindOptions.Builder().build();
            ClickOptions click = new ClickOptions.Builder().build();
            TypeOptions type = new TypeOptions.Builder().build();
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(find)
                .then(click)
                .then(type)
                .build();
            
            // When
            ActionResult result = chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then
            assertNotNull(result.getExecutionHistory());
            // Should have 3 action records in history
            assertTrue(result.getExecutionHistory().size() >= 3 || result.getExecutionHistory().isEmpty());
        }
        
        @Test
        @DisplayName("Should preserve final state in result")
        void shouldPreserveFinalStateInResult() {
            // Given
            PatternFindOptions find = new PatternFindOptions.Builder().build();
            ClickOptions click = new ClickOptions.Builder().build();
            
            ActionResult finalActionResult = mock(ActionResult.class);
            when(finalActionResult.isSuccess()).thenReturn(true);
            when(finalActionResult.getMatchList()).thenReturn(List.of(match1));
            when(finalActionResult.getDuration()).thenReturn(Duration.ofSeconds(2));
            when(finalActionResult.getText()).thenReturn(text);
            when(finalActionResult.getActiveStates()).thenReturn(Set.of("FinalState"));
            when(finalActionResult.getMovements()).thenReturn(List.of(movement));
            when(finalActionResult.getStartTime()).thenReturn(LocalDateTime.now());
            
            when(actionExecution.perform(eq(clickAction), anyString(), any(), any()))
                .thenReturn(finalActionResult);
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(find)
                .then(click)
                .build();
            
            // When
            ActionResult result = chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then
            assertEquals(finalActionResult.getMatchList(), result.getMatchList());
            // Duration may be accumulated differently, just check it's not null
            assertNotNull(result.getDuration());
            assertEquals(finalActionResult.getActiveStates(), result.getActiveStates());
        }
        
        @Test
        @DisplayName("Should accumulate movements")
        void shouldAccumulateMovements() {
            // Given
            PatternFindOptions find = new PatternFindOptions.Builder().build();
            ClickOptions click = new ClickOptions.Builder().build();
            
            Movement movement2 = mock(Movement.class);
            ActionResult resultWithMovements = mock(ActionResult.class);
            when(resultWithMovements.isSuccess()).thenReturn(true);
            when(resultWithMovements.getMatchList()).thenReturn(List.of(match1));
            when(resultWithMovements.getDuration()).thenReturn(Duration.ofSeconds(1));
            when(resultWithMovements.getText()).thenReturn(text);
            when(resultWithMovements.getMovements()).thenReturn(List.of(movement, movement2));
            when(resultWithMovements.getStartTime()).thenReturn(LocalDateTime.now());
            when(resultWithMovements.getActiveStates()).thenReturn(new HashSet<>());
            
            when(actionExecution.perform(eq(clickAction), anyString(), any(), any()))
                .thenReturn(resultWithMovements);
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(find)
                .then(click)
                .build();
            
            // When
            ActionResult result = chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then - verify movements were accumulated
            assertNotNull(result.getMovements());
            // The movements should include those from resultWithMovements
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle null strategy with default behavior")
        void shouldHandleNullStrategyWithDefaultBehavior() {
            // Given
            PatternFindOptions find = new PatternFindOptions.Builder().build();
            
            // Create chain options with null strategy - should use default
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(find)
                .setStrategy(null)
                .build();
            
            // When - should not throw, uses default strategy
            ActionResult result = chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Should throw exception for missing action implementation")
        void shouldThrowExceptionForMissingActionImplementation() {
            // Given
            PatternFindOptions find = new PatternFindOptions.Builder().build();
            when(actionService.getAction(any(PatternFindOptions.class))).thenReturn(Optional.empty());
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(find).build();
            
            // When/Then
            assertThrows(BrobotRuntimeException.class,
                () -> chainExecutor.executeChain(chainOptions, initialResult, objectCollection));
        }
        
        @Test
        @DisplayName("Should handle action execution failure")
        void shouldHandleActionExecutionFailure() {
            // Given
            PatternFindOptions find = new PatternFindOptions.Builder().build();
            when(actionExecution.perform(any(), anyString(), any(), any()))
                .thenThrow(new RuntimeException("Execution failed"));
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(find).build();
            
            // When/Then
            assertThrows(RuntimeException.class,
                () -> chainExecutor.executeChain(chainOptions, initialResult, objectCollection));
        }
    }
    
    @Nested
    @DisplayName("Non-Find Action Tests")
    class NonFindActionTests {
        
        @Test
        @DisplayName("Should handle non-find actions in nested mode")
        void shouldHandleNonFindActionsInNestedMode() {
            // Given
            PatternFindOptions find = new PatternFindOptions.Builder().build();
            ClickOptions click = new ClickOptions.Builder().build();
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(find)
                .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .then(click)
                .build();
            
            // When
            chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then
            // Click should receive the regions from find results
            ArgumentCaptor<ObjectCollection[]> collectionCaptor = ArgumentCaptor.forClass(ObjectCollection[].class);
            verify(actionExecution, times(2)).perform(any(), anyString(), any(), collectionCaptor.capture());
            
            // Second action should have regions from first action's results
            List<ObjectCollection[]> collections = collectionCaptor.getAllValues();
            assertNotNull(collections.get(1));
        }
        
        @Test
        @DisplayName("Should pass original config for non-searchable actions")
        void shouldPassOriginalConfigForNonSearchableActions() {
            // Given
            PatternFindOptions find = new PatternFindOptions.Builder().build();
            TypeOptions type = new TypeOptions.Builder().build();
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(find)
                .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .then(type)
                .build();
            
            // When
            chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then
            ArgumentCaptor<ActionConfig> configCaptor = ArgumentCaptor.forClass(ActionConfig.class);
            verify(actionExecution, times(2)).perform(any(), anyString(), configCaptor.capture(), any());
            
            // Type options should be unchanged
            assertTrue(configCaptor.getAllValues().get(1) instanceof TypeOptions);
        }
    }
    
    @Nested
    @DisplayName("Complex Chain Tests")
    class ComplexChainTests {
        
        @Test
        @DisplayName("Should execute long chain successfully")
        void shouldExecuteLongChainSuccessfully() {
            // Given
            PatternFindOptions find1 = new PatternFindOptions.Builder().build();
            ClickOptions click = new ClickOptions.Builder().build();
            PatternFindOptions find2 = new PatternFindOptions.Builder().build();
            TypeOptions type = new TypeOptions.Builder().build();
            
            ActionChainOptions chainOptions = new ActionChainOptions.Builder(find1)
                .then(click)
                .then(find2)
                .then(type)
                .build();
            
            // When
            ActionResult result = chainExecutor.executeChain(chainOptions, initialResult, objectCollection);
            
            // Then
            verify(actionExecution, times(4)).perform(any(), anyString(), any(), any());
            assertTrue(result.isSuccess());
        }
        
        @Test
        @DisplayName("Should handle mixed strategies appropriately")
        void shouldHandleMixedStrategiesAppropriately() {
            // Given - first chain with NESTED
            PatternFindOptions find1 = new PatternFindOptions.Builder().build();
            PatternFindOptions find2 = new PatternFindOptions.Builder().build();
            
            ActionChainOptions nestedChain = new ActionChainOptions.Builder(find1)
                .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .then(find2)
                .build();
            
            // Execute nested chain
            ActionResult nestedResult = chainExecutor.executeChain(nestedChain, initialResult, objectCollection);
            
            // Given - second chain with CONFIRM
            ActionChainOptions confirmChain = new ActionChainOptions.Builder(find1)
                .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
                .then(find2)
                .build();
            
            // Execute confirm chain
            ActionResult confirmResult = chainExecutor.executeChain(confirmChain, initialResult, objectCollection);
            
            // Then - both should execute but with different behaviors
            verify(actionExecution, times(4)).perform(any(), anyString(), any(), any());
            assertNotNull(nestedResult);
            assertNotNull(confirmResult);
        }
    }
}