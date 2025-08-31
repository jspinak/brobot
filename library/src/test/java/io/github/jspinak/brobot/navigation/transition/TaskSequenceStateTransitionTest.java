package io.github.jspinak.brobot.navigation.transition;

import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for TaskSequenceStateTransition.
 * Tests all aspects of task sequence-based state transitions.
 */
@DisplayName("TaskSequenceStateTransition Tests")
class TaskSequenceStateTransitionTest extends BrobotTestBase {
    
    @Mock
    private TaskSequence mockTaskSequence;
    
    private TaskSequenceStateTransition transition;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        transition = new TaskSequenceStateTransition();
    }
    
    @Nested
    @DisplayName("Basic Properties")
    class BasicProperties {
        
        @Test
        @DisplayName("Should return TaskSequence when present")
        void testGetTaskSequence() {
            transition.setActionDefinition(mockTaskSequence);
            
            Optional<TaskSequence> result = transition.getTaskSequenceOptional();
            
            assertTrue(result.isPresent());
            assertEquals(mockTaskSequence, result.get());
        }
        
        @Test
        @DisplayName("Should return empty optional when TaskSequence is null")
        void testGetTaskSequenceNull() {
            transition.setActionDefinition(null);
            
            Optional<TaskSequence> result = transition.getTaskSequenceOptional();
            
            assertFalse(result.isPresent());
        }
        
        @Test
        @DisplayName("Should initialize with default values")
        void testInitialization() {
            assertNull(transition.getActionDefinition());
            assertEquals(StateTransition.StaysVisible.NONE, transition.getStaysVisibleAfterTransition());
            assertNotNull(transition.getActivate());
            assertNotNull(transition.getExit());
            assertTrue(transition.getActivate().isEmpty());
            assertTrue(transition.getExit().isEmpty());
            assertEquals(0, transition.getScore());
            assertEquals(0, transition.getTimesSuccessful());
        }
    }
    
    @Nested
    @DisplayName("ActionDefinition Management")
    class ActionDefinitionManagement {
        
        @Test
        @DisplayName("Should set and get ActionDefinition")
        void testSetGetActionDefinition() {
            transition.setActionDefinition(mockTaskSequence);
            
            assertEquals(mockTaskSequence, transition.getActionDefinition());
            assertTrue(transition.getTaskSequenceOptional().isPresent());
        }
        
        @Test
        @DisplayName("Should handle null ActionDefinition")
        void testNullActionDefinition() {
            transition.setActionDefinition(null);
            
            assertNull(transition.getActionDefinition());
            assertFalse(transition.getTaskSequenceOptional().isPresent());
        }
        
        @Test
        @DisplayName("Should replace existing ActionDefinition")
        void testReplaceActionDefinition() {
            TaskSequence first = mock(TaskSequence.class);
            TaskSequence second = mock(TaskSequence.class);
            
            transition.setActionDefinition(first);
            assertEquals(first, transition.getActionDefinition());
            
            transition.setActionDefinition(second);
            assertEquals(second, transition.getActionDefinition());
        }
    }
    
    @Nested
    @DisplayName("State Management")
    class StateManagement {
        
        @Test
        @DisplayName("Should manage activation states")
        void testActivationStates() {
            Set<Long> states = new HashSet<>(Arrays.asList(1L, 2L, 3L));
            transition.setActivate(states);
            
            assertEquals(states, transition.getActivate());
            assertEquals(3, transition.getActivate().size());
            assertTrue(transition.getActivate().contains(2L));
        }
        
        @Test
        @DisplayName("Should manage exit states")
        void testExitStates() {
            Set<Long> states = new HashSet<>(Arrays.asList(10L, 20L));
            transition.setExit(states);
            
            assertEquals(states, transition.getExit());
            assertEquals(2, transition.getExit().size());
            assertTrue(transition.getExit().contains(20L));
        }
        
        @Test
        @DisplayName("Should handle empty state sets")
        void testEmptyStateSets() {
            transition.setActivate(new HashSet<>());
            transition.setExit(new HashSet<>());
            
            assertTrue(transition.getActivate().isEmpty());
            assertTrue(transition.getExit().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle large state sets")
        void testLargeStateSets() {
            Set<Long> largeSet = new HashSet<>();
            for (long i = 0; i < 1000; i++) {
                largeSet.add(i);
            }
            
            transition.setActivate(largeSet);
            assertEquals(1000, transition.getActivate().size());
        }
        
        @Test
        @DisplayName("Should handle duplicate state IDs")
        void testDuplicateStateIds() {
            Set<Long> states = new HashSet<>(Arrays.asList(1L, 1L, 2L, 2L, 3L));
            transition.setActivate(states);
            
            // Set removes duplicates
            assertEquals(3, transition.getActivate().size());
        }
        
        @Test
        @DisplayName("Should maintain separate activate and exit sets")
        void testSeparateSets() {
            Set<Long> activateSet = new HashSet<>(Arrays.asList(1L, 2L));
            Set<Long> exitSet = new HashSet<>(Arrays.asList(3L, 4L));
            
            transition.setActivate(activateSet);
            transition.setExit(exitSet);
            
            assertEquals(2, transition.getActivate().size());
            assertEquals(2, transition.getExit().size());
            assertFalse(transition.getActivate().contains(3L));
            assertFalse(transition.getExit().contains(1L));
        }
    }
    
    @Nested
    @DisplayName("Visibility Management")
    class VisibilityManagement {
        
        @Test
        @DisplayName("Should set visibility to TRUE")
        void testVisibilityTrue() {
            transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.TRUE);
            assertEquals(StateTransition.StaysVisible.TRUE, 
                transition.getStaysVisibleAfterTransition());
        }
        
        @Test
        @DisplayName("Should set visibility to FALSE")
        void testVisibilityFalse() {
            transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.FALSE);
            assertEquals(StateTransition.StaysVisible.FALSE, 
                transition.getStaysVisibleAfterTransition());
        }
        
        @Test
        @DisplayName("Should set visibility to NONE")
        void testVisibilityNone() {
            transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.NONE);
            assertEquals(StateTransition.StaysVisible.NONE, 
                transition.getStaysVisibleAfterTransition());
        }
        
        @Test
        @DisplayName("Should handle visibility changes")
        void testVisibilityChanges() {
            transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.TRUE);
            assertEquals(StateTransition.StaysVisible.TRUE, 
                transition.getStaysVisibleAfterTransition());
            
            transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.FALSE);
            assertEquals(StateTransition.StaysVisible.FALSE, 
                transition.getStaysVisibleAfterTransition());
            
            transition.setStaysVisibleAfterTransition(null);
            assertNull(transition.getStaysVisibleAfterTransition());
        }
    }
    
    @Nested
    @DisplayName("Score and Metrics")
    class ScoreAndMetrics {
        
        @ParameterizedTest
        @ValueSource(ints = {0, 1, 10, 50, 100, 500, 1000, Integer.MAX_VALUE})
        @DisplayName("Should handle various score values")
        void testScoreValues(int score) {
            transition.setScore(score);
            assertEquals(score, transition.getScore());
        }
        
        @Test
        @DisplayName("Should handle negative scores")
        void testNegativeScore() {
            transition.setScore(-50);
            assertEquals(-50, transition.getScore());
            
            transition.setScore(Integer.MIN_VALUE);
            assertEquals(Integer.MIN_VALUE, transition.getScore());
        }
        
        @Test
        @DisplayName("Should track success count")
        void testSuccessCount() {
            assertEquals(0, transition.getTimesSuccessful());
            
            transition.setTimesSuccessful(10);
            assertEquals(10, transition.getTimesSuccessful());
            
            // Increment
            transition.setTimesSuccessful(transition.getTimesSuccessful() + 1);
            assertEquals(11, transition.getTimesSuccessful());
        }
        
        @Test
        @DisplayName("Should handle overflow in success count")
        void testSuccessCountOverflow() {
            transition.setTimesSuccessful(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, transition.getTimesSuccessful());
            
            // This would overflow in real scenario
            transition.setTimesSuccessful(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, transition.getTimesSuccessful());
        }
    }
    
    @Nested
    @DisplayName("ToString Method")
    class ToStringMethod {
        
        @Test
        @DisplayName("Should format empty transition")
        void testToStringEmpty() {
            String result = transition.toString();
            
            assertNotNull(result);
            assertTrue(result.contains("activate=[]"));
            assertTrue(result.contains("exit=[]"));
        }
        
        @Test
        @DisplayName("Should format transition with states")
        void testToStringWithStates() {
            transition.setActivate(new HashSet<>(Arrays.asList(1L, 2L, 3L)));
            transition.setExit(new HashSet<>(Arrays.asList(10L, 20L)));
            
            String result = transition.toString();
            
            assertNotNull(result);
            assertTrue(result.contains("activate="));
            assertTrue(result.contains("exit="));
            // Check for at least one ID from each set
            assertTrue(result.contains("1") || result.contains("2") || result.contains("3"));
            assertTrue(result.contains("10") || result.contains("20"));
        }
        
        @Test
        @DisplayName("Should handle single state in sets")
        void testToStringSingleState() {
            transition.setActivate(new HashSet<>(Arrays.asList(42L)));
            transition.setExit(new HashSet<>(Arrays.asList(99L)));
            
            String result = transition.toString();
            
            assertTrue(result.contains("42"));
            assertTrue(result.contains("99"));
        }
        
        @Test
        @DisplayName("Should handle null state sets gracefully")
        void testToStringNullSets() {
            // Force nulls (normally shouldn't happen)
            transition.setActivate(null);
            transition.setExit(null);
            
            // Should not throw exception
            String result = transition.toString();
            assertNotNull(result);
        }
    }
    
    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("Should maintain all properties after multiple updates")
        void testPropertyConsistency() {
            // Set all properties
            transition.setActionDefinition(mockTaskSequence);
            transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.TRUE);
            transition.setActivate(new HashSet<>(Arrays.asList(1L, 2L)));
            transition.setExit(new HashSet<>(Arrays.asList(3L, 4L)));
            transition.setScore(100);
            transition.setTimesSuccessful(5);
            
            // Verify all remain set
            assertEquals(mockTaskSequence, transition.getActionDefinition());
            assertEquals(StateTransition.StaysVisible.TRUE, 
                transition.getStaysVisibleAfterTransition());
            assertEquals(2, transition.getActivate().size());
            assertEquals(2, transition.getExit().size());
            assertEquals(100, transition.getScore());
            assertEquals(5, transition.getTimesSuccessful());
            
            // Update some properties
            transition.setScore(200);
            transition.setTimesSuccessful(10);
            
            // Verify updates and others unchanged
            assertEquals(200, transition.getScore());
            assertEquals(10, transition.getTimesSuccessful());
            assertEquals(mockTaskSequence, transition.getActionDefinition());
            assertEquals(2, transition.getActivate().size());
        }
        
        @Test
        @DisplayName("Should handle transition with overlapping activate and exit states")
        void testOverlappingStates() {
            // This shouldn't normally happen but test behavior
            Set<Long> activateStates = new HashSet<>(Arrays.asList(1L, 2L, 3L));
            Set<Long> exitStates = new HashSet<>(Arrays.asList(1L, 2L, 3L));
            
            transition.setActivate(activateStates);
            transition.setExit(exitStates);
            
            assertEquals(activateStates, transition.getActivate());
            assertEquals(exitStates, transition.getExit());
            
            // Since they're the same reference with @Data, modifying one affects both if same object was passed
            // But we passed different objects, so they should be independent
            transition.getActivate().add(4L);
            assertEquals(4, transition.getActivate().size());
            // Exit should still have 3 since it's a different object
            assertEquals(3, transition.getExit().size());
        }
        
        @Test
        @DisplayName("Should compare transitions by score")
        void testScoreComparison() {
            TaskSequenceStateTransition other = new TaskSequenceStateTransition();
            
            transition.setScore(10);
            other.setScore(20);
            
            List<StateTransition> transitions = Arrays.asList(other, transition);
            transitions.sort(Comparator.comparingInt(StateTransition::getScore));
            
            assertEquals(transition, transitions.get(0));
            assertEquals(other, transitions.get(1));
        }
        
        @Test
        @DisplayName("Should handle transition with maximum states")
        void testMaximumStates() {
            Set<Long> maxStates = new HashSet<>();
            for (long i = 0; i < 10000; i++) {
                maxStates.add(i);
            }
            
            transition.setActivate(maxStates);
            
            assertEquals(10000, transition.getActivate().size());
            assertTrue(transition.getActivate().contains(5000L));
            
            // toString should handle large sets
            String result = transition.toString();
            assertNotNull(result);
            assertTrue(result.length() > 0);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle Long.MAX_VALUE state ID")
        void testMaxLongStateId() {
            Set<Long> states = new HashSet<>(Arrays.asList(Long.MAX_VALUE, 0L, -1L));
            transition.setActivate(states);
            
            assertTrue(transition.getActivate().contains(Long.MAX_VALUE));
            assertTrue(transition.getActivate().contains(0L));
            assertTrue(transition.getActivate().contains(-1L));
        }
        
        @Test
        @DisplayName("Should handle negative state IDs")
        void testNegativeStateIds() {
            Set<Long> states = new HashSet<>(Arrays.asList(-1L, -100L, -999L));
            transition.setActivate(states);
            
            assertEquals(3, transition.getActivate().size());
            assertTrue(transition.getActivate().contains(-100L));
        }
        
        @Test
        @DisplayName("Should use same reference for state sets with @Data")
        void testStateSetReference() {
            Set<Long> original = new HashSet<>(Arrays.asList(1L, 2L));
            transition.setActivate(original);
            
            // With Lombok @Data, the setter doesn't make a defensive copy
            // So modifying original will affect the transition's set
            original.add(3L);
            
            // Transition's set is the same reference, so it will have the new element
            assertEquals(3, transition.getActivate().size());
            assertTrue(transition.getActivate().contains(3L));
            
            // They are the same object
            assertSame(original, transition.getActivate());
        }
    }
}