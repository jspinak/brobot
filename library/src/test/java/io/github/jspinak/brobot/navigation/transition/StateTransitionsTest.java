package io.github.jspinak.brobot.navigation.transition;

import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for StateTransitions class.
 * Tests container functionality for state transitions including
 * transition management, visibility control, and builder patterns.
 */
@DisplayName("StateTransitions Tests")
class StateTransitionsTest extends BrobotTestBase {
    
    private StateTransitions stateTransitions;
    
    @Mock
    private StateTransition mockTransitionFinish;
    
    @Mock
    private JavaStateTransition mockJavaTransition;
    
    @Mock
    private TaskSequenceStateTransition mockTaskSequenceTransition;
    
    @Mock
    private TaskSequence mockTaskSequence;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        stateTransitions = new StateTransitions();
    }
    
    @Nested
    @DisplayName("Basic Properties")
    class BasicProperties {
        
        @Test
        @DisplayName("Should initialize with default values")
        void testInitialization() {
            assertNull(stateTransitions.getStateName());
            assertNull(stateTransitions.getStateId());
            assertNull(stateTransitions.getTransitionFinish());
            assertNotNull(stateTransitions.getActionDefinitionTransitions());
            assertTrue(stateTransitions.getActionDefinitionTransitions().isEmpty());
            assertNotNull(stateTransitions.getTransitions());
            assertTrue(stateTransitions.getTransitions().isEmpty());
            assertFalse(stateTransitions.isStaysVisibleAfterTransition());
        }
        
        @Test
        @DisplayName("Should set and get state name")
        void testStateName() {
            stateTransitions.setStateName("TestState");
            assertEquals("TestState", stateTransitions.getStateName());
        }
        
        @Test
        @DisplayName("Should set and get state ID")
        void testStateId() {
            stateTransitions.setStateId(123L);
            assertEquals(123L, stateTransitions.getStateId());
        }
        
        @Test
        @DisplayName("Should set and get transition finish")
        void testTransitionFinish() {
            stateTransitions.setTransitionFinish(mockTransitionFinish);
            assertEquals(mockTransitionFinish, stateTransitions.getTransitionFinish());
        }
        
        @Test
        @DisplayName("Should set and get stays visible flag")
        void testStaysVisibleFlag() {
            stateTransitions.setStaysVisibleAfterTransition(true);
            assertTrue(stateTransitions.isStaysVisibleAfterTransition());
            
            stateTransitions.setStaysVisibleAfterTransition(false);
            assertFalse(stateTransitions.isStaysVisibleAfterTransition());
        }
    }
    
    @Nested
    @DisplayName("Transition Management")
    class TransitionManagement {
        
        @Test
        @DisplayName("Should add JavaStateTransition")
        void testAddJavaTransition() {
            stateTransitions.addTransition(mockJavaTransition);
            
            assertEquals(1, stateTransitions.getTransitions().size());
            assertTrue(stateTransitions.getTransitions().contains(mockJavaTransition));
        }
        
        @Test
        @DisplayName("Should add multiple JavaStateTransitions")
        void testAddMultipleJavaTransitions() {
            JavaStateTransition transition1 = mock(JavaStateTransition.class);
            JavaStateTransition transition2 = mock(JavaStateTransition.class);
            JavaStateTransition transition3 = mock(JavaStateTransition.class);
            
            stateTransitions.addTransition(transition1);
            stateTransitions.addTransition(transition2);
            stateTransitions.addTransition(transition3);
            
            assertEquals(3, stateTransitions.getTransitions().size());
            assertTrue(stateTransitions.getTransitions().contains(transition1));
            assertTrue(stateTransitions.getTransitions().contains(transition2));
            assertTrue(stateTransitions.getTransitions().contains(transition3));
        }
        
        @Test
        @DisplayName("Should add TaskSequenceStateTransition")
        void testAddTaskSequenceTransition() {
            Set<Long> activateStates = new HashSet<>(Arrays.asList(1L, 2L, 3L));
            when(mockTaskSequenceTransition.getActivate()).thenReturn(activateStates);
            
            stateTransitions.addTransition(mockTaskSequenceTransition);
            
            // Should add transition for each activated state
            assertEquals(3, stateTransitions.getTransitions().size());
            assertEquals(3, stateTransitions.getActionDefinitionTransitions().size());
            
            // Verify mapping
            assertEquals(mockTaskSequenceTransition, stateTransitions.getActionDefinitionTransitions().get(1L));
            assertEquals(mockTaskSequenceTransition, stateTransitions.getActionDefinitionTransitions().get(2L));
            assertEquals(mockTaskSequenceTransition, stateTransitions.getActionDefinitionTransitions().get(3L));
        }
        
        @Test
        @DisplayName("Should add transition with BooleanSupplier convenience method")
        void testAddTransitionWithBooleanSupplier() {
            BooleanSupplier supplier = () -> true;
            String[] toStates = {"State1", "State2"};
            
            stateTransitions.addTransition(supplier, toStates);
            
            assertEquals(1, stateTransitions.getTransitions().size());
            StateTransition added = stateTransitions.getTransitions().get(0);
            assertTrue(added instanceof JavaStateTransition);
            
            JavaStateTransition javaTransition = (JavaStateTransition) added;
            assertTrue(javaTransition.getActivateNames().contains("State1"));
            assertTrue(javaTransition.getActivateNames().contains("State2"));
        }
    }
    
    @Nested
    @DisplayName("Transition Retrieval")
    class TransitionRetrieval {
        
        @BeforeEach
        void setup() {
            stateTransitions.setStateId(100L);
        }
        
        @Test
        @DisplayName("Should find transition by activated state ID")
        void testGetTransitionByActivatedStateId() {
            Set<Long> activateStates = new HashSet<>(Arrays.asList(1L, 2L));
            when(mockJavaTransition.getActivate()).thenReturn(activateStates);
            
            stateTransitions.addTransition(mockJavaTransition);
            
            Optional<StateTransition> result = stateTransitions.getTransitionFunctionByActivatedStateId(1L);
            assertTrue(result.isPresent());
            assertEquals(mockJavaTransition, result.get());
            
            result = stateTransitions.getTransitionFunctionByActivatedStateId(2L);
            assertTrue(result.isPresent());
            assertEquals(mockJavaTransition, result.get());
        }
        
        @Test
        @DisplayName("Should return transition finish for own state ID")
        void testGetTransitionFinishForOwnStateId() {
            stateTransitions.setTransitionFinish(mockTransitionFinish);
            
            Optional<StateTransition> result = stateTransitions.getTransitionFunctionByActivatedStateId(100L);
            assertTrue(result.isPresent());
            assertEquals(mockTransitionFinish, result.get());
        }
        
        @Test
        @DisplayName("Should return empty for unknown state ID")
        void testGetTransitionForUnknownStateId() {
            Optional<StateTransition> result = stateTransitions.getTransitionFunctionByActivatedStateId(999L);
            assertFalse(result.isPresent());
        }
        
        @Test
        @DisplayName("Should return empty for null state ID")
        void testGetTransitionForNullStateId() {
            Optional<StateTransition> result = stateTransitions.getTransitionFunctionByActivatedStateId(null);
            assertFalse(result.isPresent());
        }
        
        @Test
        @DisplayName("Should find correct transition among multiple")
        void testGetTransitionAmongMultiple() {
            JavaStateTransition transition1 = mock(JavaStateTransition.class);
            when(transition1.getActivate()).thenReturn(new HashSet<>(Arrays.asList(1L, 2L)));
            
            JavaStateTransition transition2 = mock(JavaStateTransition.class);
            when(transition2.getActivate()).thenReturn(new HashSet<>(Arrays.asList(3L, 4L)));
            
            JavaStateTransition transition3 = mock(JavaStateTransition.class);
            when(transition3.getActivate()).thenReturn(new HashSet<>(Arrays.asList(5L, 6L)));
            
            stateTransitions.addTransition(transition1);
            stateTransitions.addTransition(transition2);
            stateTransitions.addTransition(transition3);
            
            assertEquals(transition2, stateTransitions.getTransitionFunctionByActivatedStateId(3L).get());
            assertEquals(transition3, stateTransitions.getTransitionFunctionByActivatedStateId(6L).get());
            assertEquals(transition1, stateTransitions.getTransitionFunctionByActivatedStateId(1L).get());
        }
    }
    
    @Nested
    @DisplayName("Visibility Management")
    class VisibilityManagement {
        
        @BeforeEach
        void setup() {
            stateTransitions.setStateId(100L);
        }
        
        @Test
        @DisplayName("Should use transition-specific visibility when set to TRUE")
        void testTransitionSpecificVisibilityTrue() {
            when(mockJavaTransition.getActivate()).thenReturn(new HashSet<>(Arrays.asList(1L)));
            when(mockJavaTransition.getStaysVisibleAfterTransition())
                .thenReturn(StateTransition.StaysVisible.TRUE);
            
            stateTransitions.addTransition(mockJavaTransition);
            stateTransitions.setStaysVisibleAfterTransition(false); // State default is false
            
            assertTrue(stateTransitions.stateStaysVisible(1L));
        }
        
        @Test
        @DisplayName("Should use transition-specific visibility when set to FALSE")
        void testTransitionSpecificVisibilityFalse() {
            when(mockJavaTransition.getActivate()).thenReturn(new HashSet<>(Arrays.asList(1L)));
            when(mockJavaTransition.getStaysVisibleAfterTransition())
                .thenReturn(StateTransition.StaysVisible.FALSE);
            
            stateTransitions.addTransition(mockJavaTransition);
            stateTransitions.setStaysVisibleAfterTransition(true); // State default is true
            
            assertFalse(stateTransitions.stateStaysVisible(1L));
        }
        
        @Test
        @DisplayName("Should use state default when transition visibility is NONE")
        void testStateDefaultVisibilityWhenNone() {
            when(mockJavaTransition.getActivate()).thenReturn(new HashSet<>(Arrays.asList(1L)));
            when(mockJavaTransition.getStaysVisibleAfterTransition())
                .thenReturn(StateTransition.StaysVisible.NONE);
            
            stateTransitions.addTransition(mockJavaTransition);
            
            // Test with state default true
            stateTransitions.setStaysVisibleAfterTransition(true);
            assertTrue(stateTransitions.stateStaysVisible(1L));
            
            // Test with state default false
            stateTransitions.setStaysVisibleAfterTransition(false);
            assertFalse(stateTransitions.stateStaysVisible(1L));
        }
        
        @Test
        @DisplayName("Should return false for unknown transition")
        void testVisibilityForUnknownTransition() {
            assertFalse(stateTransitions.stateStaysVisible(999L));
        }
    }
    
    @Nested
    @DisplayName("ActionDefinition Retrieval")
    class ActionDefinitionRetrieval {
        
        @Test
        @DisplayName("Should retrieve ActionDefinition for TaskSequenceStateTransition")
        void testGetActionDefinitionSuccess() {
            Set<Long> activateStates = new HashSet<>(Arrays.asList(1L));
            when(mockTaskSequenceTransition.getActivate()).thenReturn(activateStates);
            when(mockTaskSequenceTransition.getTaskSequenceOptional())
                .thenReturn(Optional.of(mockTaskSequence));
            
            stateTransitions.addTransition(mockTaskSequenceTransition);
            
            Optional<TaskSequence> result = stateTransitions.getActionDefinition(1L);
            assertTrue(result.isPresent());
            assertEquals(mockTaskSequence, result.get());
        }
        
        @Test
        @DisplayName("Should return empty for JavaStateTransition")
        void testGetActionDefinitionForJavaTransition() {
            when(mockJavaTransition.getActivate()).thenReturn(new HashSet<>(Arrays.asList(1L)));
            stateTransitions.addTransition(mockJavaTransition);
            
            Optional<TaskSequence> result = stateTransitions.getActionDefinition(1L);
            assertFalse(result.isPresent());
        }
        
        @Test
        @DisplayName("Should return empty for unknown state")
        void testGetActionDefinitionForUnknownState() {
            Optional<TaskSequence> result = stateTransitions.getActionDefinition(999L);
            assertFalse(result.isPresent());
        }
    }
    
    @Nested
    @DisplayName("Builder Pattern")
    class BuilderPattern {
        
        @Test
        @DisplayName("Should build with minimal configuration")
        void testBuilderMinimal() {
            StateTransitions transitions = new StateTransitions.Builder("TestState")
                .build();
            
            assertEquals("TestState", transitions.getStateName());
            assertNotNull(transitions.getTransitionFinish());
            assertTrue(transitions.getTransitions().isEmpty());
            assertFalse(transitions.isStaysVisibleAfterTransition());
        }
        
        @Test
        @DisplayName("Should build with transition finish BooleanSupplier")
        void testBuilderWithTransitionFinishSupplier() {
            BooleanSupplier finishSupplier = () -> true;
            
            StateTransitions transitions = new StateTransitions.Builder("TestState")
                .addTransitionFinish(finishSupplier)
                .build();
            
            assertNotNull(transitions.getTransitionFinish());
            assertTrue(transitions.getTransitionFinish() instanceof JavaStateTransition);
            
            JavaStateTransition finish = (JavaStateTransition) transitions.getTransitionFinish();
            assertTrue(finish.getTransitionFunction().getAsBoolean());
        }
        
        @Test
        @DisplayName("Should build with transition finish JavaStateTransition")
        void testBuilderWithTransitionFinishJava() {
            JavaStateTransition finish = new JavaStateTransition.Builder()
                .setFunction(() -> false)
                .build();
            
            StateTransitions transitions = new StateTransitions.Builder("TestState")
                .addTransitionFinish(finish)
                .build();
            
            assertEquals(finish, transitions.getTransitionFinish());
        }
        
        @Test
        @DisplayName("Should build with transitions using BooleanSupplier")
        void testBuilderWithTransitionSupplier() {
            BooleanSupplier supplier1 = () -> true;
            BooleanSupplier supplier2 = () -> false;
            
            StateTransitions transitions = new StateTransitions.Builder("TestState")
                .addTransition(supplier1, "State1", "State2")
                .addTransition(supplier2, "State3")
                .build();
            
            assertEquals(2, transitions.getTransitions().size());
            
            JavaStateTransition trans1 = (JavaStateTransition) transitions.getTransitions().get(0);
            assertTrue(trans1.getActivateNames().contains("State1"));
            assertTrue(trans1.getActivateNames().contains("State2"));
            
            JavaStateTransition trans2 = (JavaStateTransition) transitions.getTransitions().get(1);
            assertTrue(trans2.getActivateNames().contains("State3"));
        }
        
        @Test
        @DisplayName("Should build with JavaStateTransition directly")
        void testBuilderWithJavaTransition() {
            JavaStateTransition transition = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .addToActivate("State1")
                .build();
            
            StateTransitions transitions = new StateTransitions.Builder("TestState")
                .addTransition(transition)
                .build();
            
            assertEquals(1, transitions.getTransitions().size());
            assertEquals(transition, transitions.getTransitions().get(0));
        }
        
        @Test
        @DisplayName("Should build with stays visible configuration")
        void testBuilderWithStaysVisible() {
            StateTransitions transitions = new StateTransitions.Builder("TestState")
                .setStaysVisibleAfterTransition(true)
                .build();
            
            assertTrue(transitions.isStaysVisibleAfterTransition());
        }
        
        @Test
        @DisplayName("Should build with complete configuration")
        void testBuilderComplete() {
            JavaStateTransition finish = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
            
            JavaStateTransition transition1 = new JavaStateTransition.Builder()
                .setFunction(() -> false)
                .addToActivate("State1")
                .build();
            
            StateTransitions transitions = new StateTransitions.Builder("CompleteState")
                .addTransitionFinish(finish)
                .addTransition(() -> true, "State2", "State3")
                .addTransition(transition1)
                .setStaysVisibleAfterTransition(true)
                .build();
            
            assertEquals("CompleteState", transitions.getStateName());
            assertEquals(finish, transitions.getTransitionFinish());
            assertEquals(2, transitions.getTransitions().size());
            assertTrue(transitions.isStaysVisibleAfterTransition());
        }
        
        @Test
        @DisplayName("Should support method chaining")
        void testBuilderMethodChaining() {
            StateTransitions transitions = new StateTransitions.Builder("ChainState")
                .addTransitionFinish(() -> true)
                .addTransition(() -> false, "State1")
                .addTransition(() -> true, "State2", "State3")
                .setStaysVisibleAfterTransition(false)
                .addTransition(() -> false, "State4")
                .build();
            
            assertEquals("ChainState", transitions.getStateName());
            assertEquals(3, transitions.getTransitions().size());
            assertFalse(transitions.isStaysVisibleAfterTransition());
        }
    }
    
    @Nested
    @DisplayName("ToString Method")
    class ToStringMethod {
        
        @Test
        @DisplayName("Should format with state ID and name")
        void testToStringWithIdAndName() {
            stateTransitions.setStateId(123L);
            stateTransitions.setStateName("TestState");
            
            String result = stateTransitions.toString();
            assertTrue(result.contains("id=123"));
            assertTrue(result.contains("from=TestState"));
        }
        
        @Test
        @DisplayName("Should format without state ID")
        void testToStringWithoutId() {
            stateTransitions.setStateName("TestState");
            
            String result = stateTransitions.toString();
            assertFalse(result.contains("id="));
            assertTrue(result.contains("from=TestState"));
        }
        
        @Test
        @DisplayName("Should format without state name")
        void testToStringWithoutName() {
            stateTransitions.setStateId(123L);
            
            String result = stateTransitions.toString();
            assertTrue(result.contains("id=123"));
            assertFalse(result.contains("from="));
        }
        
        @Test
        @DisplayName("Should include TaskSequenceStateTransition activate states")
        void testToStringWithTaskSequenceTransition() {
            Set<Long> activateStates = new HashSet<>(Arrays.asList(1L, 2L));
            when(mockTaskSequenceTransition.getActivate()).thenReturn(activateStates);
            
            stateTransitions.setStateName("TestState");
            stateTransitions.addTransition(mockTaskSequenceTransition);
            
            String result = stateTransitions.toString();
            assertTrue(result.contains("from=TestState"));
            assertTrue(result.contains("to="));
            // Should contain the activate state IDs
            assertTrue(result.contains("1") || result.contains("2"));
        }
        
        @Test
        @DisplayName("Should include JavaStateTransition activate names")
        void testToStringWithJavaTransition() {
            Set<String> activateNames = new HashSet<>(Arrays.asList("State1", "State2"));
            when(mockJavaTransition.getActivateNames()).thenReturn(activateNames);
            
            stateTransitions.setStateName("TestState");
            stateTransitions.addTransition(mockJavaTransition);
            
            String result = stateTransitions.toString();
            assertTrue(result.contains("from=TestState"));
            assertTrue(result.contains("to="));
            // The implementation adds names and a comma
            assertTrue(result.contains("State1") || result.contains("State2"));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle empty activate set in TaskSequenceStateTransition")
        void testEmptyActivateSet() {
            when(mockTaskSequenceTransition.getActivate()).thenReturn(new HashSet<>());
            
            stateTransitions.addTransition(mockTaskSequenceTransition);
            
            assertTrue(stateTransitions.getTransitions().isEmpty());
            assertTrue(stateTransitions.getActionDefinitionTransitions().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle null transition finish in visibility check")
        void testNullTransitionFinishInVisibility() {
            stateTransitions.setStateId(100L);
            // Don't set transition finish - it's null
            
            // Should throw NPE when checking visibility for own state with null transitionFinish
            assertThrows(NullPointerException.class, () -> stateTransitions.stateStaysVisible(100L));
        }
        
        @Test
        @DisplayName("Should handle duplicate state IDs in different transitions")
        void testDuplicateStateIds() {
            JavaStateTransition transition1 = mock(JavaStateTransition.class);
            when(transition1.getActivate()).thenReturn(new HashSet<>(Arrays.asList(1L)));
            
            JavaStateTransition transition2 = mock(JavaStateTransition.class);
            when(transition2.getActivate()).thenReturn(new HashSet<>(Arrays.asList(1L))); // Same ID
            
            stateTransitions.addTransition(transition1);
            stateTransitions.addTransition(transition2);
            
            // Should find first matching transition
            Optional<StateTransition> result = stateTransitions.getTransitionFunctionByActivatedStateId(1L);
            assertTrue(result.isPresent());
            assertEquals(transition1, result.get()); // First one added
        }
        
        @Test
        @DisplayName("Should handle TaskSequenceStateTransition overwriting in map")
        void testTaskSequenceTransitionMapOverwrite() {
            TaskSequenceStateTransition transition1 = mock(TaskSequenceStateTransition.class);
            when(transition1.getActivate()).thenReturn(new HashSet<>(Arrays.asList(1L)));
            
            TaskSequenceStateTransition transition2 = mock(TaskSequenceStateTransition.class);
            when(transition2.getActivate()).thenReturn(new HashSet<>(Arrays.asList(1L))); // Same ID
            
            stateTransitions.addTransition(transition1);
            stateTransitions.addTransition(transition2);
            
            // Map should contain the last one added
            assertEquals(transition2, stateTransitions.getActionDefinitionTransitions().get(1L));
            
            // But transitions list should contain both
            assertEquals(2, stateTransitions.getTransitions().size());
        }
        
        @Test
        @DisplayName("Should handle very large state ID")
        void testVeryLargeStateId() {
            Long largeId = Long.MAX_VALUE;
            stateTransitions.setStateId(largeId);
            stateTransitions.setTransitionFinish(mockTransitionFinish);
            
            Optional<StateTransition> result = stateTransitions.getTransitionFunctionByActivatedStateId(largeId);
            assertTrue(result.isPresent());
            assertEquals(mockTransitionFinish, result.get());
        }
    }
}