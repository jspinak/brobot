package io.github.jspinak.brobot.statemanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.test.BrobotTestBase;

@DisplayName("StateIdResolver Tests")
public class StateIdResolverTest extends BrobotTestBase {

    @Mock private StateService stateService;

    private StateIdResolver stateIdResolver;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        stateIdResolver = new StateIdResolver(stateService);
    }

    @Nested
    @DisplayName("Single StateTransitions Conversion")
    class SingleStateTransitionsConversion {

        @Test
        @DisplayName("Should convert state name to ID")
        public void testConvertStateNameToId() {
            StateTransitions stateTransitions = new StateTransitions();
            stateTransitions.setStateName("LoginPage");
            stateTransitions.setStateId(null);

            when(stateService.getStateId("LoginPage")).thenReturn(100L);

            stateIdResolver.convertNamesToIds(stateTransitions);

            assertEquals(100L, stateTransitions.getStateId());
            verify(stateService).getStateId("LoginPage");
        }

        @Test
        @DisplayName("Should not overwrite existing state ID")
        public void testPreserveExistingStateId() {
            StateTransitions stateTransitions = new StateTransitions();
            stateTransitions.setStateName("LoginPage");
            stateTransitions.setStateId(50L); // Already has ID

            when(stateService.getStateId("LoginPage")).thenReturn(100L);

            stateIdResolver.convertNamesToIds(stateTransitions);

            assertEquals(50L, stateTransitions.getStateId()); // Original ID preserved
            verify(stateService).getStateId("LoginPage");
        }

        @Test
        @DisplayName("Should handle null state name")
        public void testNullStateName() {
            StateTransitions stateTransitions = new StateTransitions();
            stateTransitions.setStateName(null);

            when(stateService.getStateId(null)).thenReturn(null);

            stateIdResolver.convertNamesToIds(stateTransitions);

            assertNull(stateTransitions.getStateId());
            verify(stateService).getStateId(null);
        }

        @Test
        @DisplayName("Should handle unregistered state name")
        public void testUnregisteredStateName() {
            StateTransitions stateTransitions = new StateTransitions();
            stateTransitions.setStateName("UnknownState");

            when(stateService.getStateId("UnknownState")).thenReturn(null);

            stateIdResolver.convertNamesToIds(stateTransitions);

            assertNull(stateTransitions.getStateId());
            verify(stateService).getStateId("UnknownState");
        }
    }

    @Nested
    @DisplayName("JavaStateTransition Conversion")
    class JavaStateTransitionConversion {

        @Test
        @DisplayName("Should convert activate names to IDs")
        public void testConvertActivateNamesToIds() {
            StateTransitions stateTransitions = new StateTransitions();
            stateTransitions.setStateName("HomePage");

            JavaStateTransition javaTransition = new JavaStateTransition();
            javaTransition.setActivateNames(
                    new HashSet<>(Arrays.asList("Dashboard", "Menu", "Footer")));
            javaTransition.setActivate(new HashSet<>());

            stateTransitions.setTransitions(Collections.singletonList(javaTransition));

            when(stateService.getStateId("HomePage")).thenReturn(1L);
            when(stateService.getStateId("Dashboard")).thenReturn(10L);
            when(stateService.getStateId("Menu")).thenReturn(11L);
            when(stateService.getStateId("Footer")).thenReturn(12L);

            stateIdResolver.convertNamesToIds(stateTransitions);

            assertEquals(3, javaTransition.getActivate().size());
            assertTrue(javaTransition.getActivate().contains(10L));
            assertTrue(javaTransition.getActivate().contains(11L));
            assertTrue(javaTransition.getActivate().contains(12L));
        }

        @Test
        @DisplayName("Should skip null activate IDs")
        public void testSkipNullActivateIds() {
            StateTransitions stateTransitions = new StateTransitions();
            stateTransitions.setStateName("HomePage");

            JavaStateTransition javaTransition = new JavaStateTransition();
            javaTransition.setActivateNames(
                    new HashSet<>(Arrays.asList("ValidState", "InvalidState", "AnotherValid")));
            javaTransition.setActivate(new HashSet<>());

            stateTransitions.setTransitions(Collections.singletonList(javaTransition));

            when(stateService.getStateId("HomePage")).thenReturn(1L);
            when(stateService.getStateId("ValidState")).thenReturn(20L);
            when(stateService.getStateId("InvalidState")).thenReturn(null); // Not found
            when(stateService.getStateId("AnotherValid")).thenReturn(21L);

            stateIdResolver.convertNamesToIds(stateTransitions);

            assertEquals(2, javaTransition.getActivate().size());
            assertTrue(javaTransition.getActivate().contains(20L));
            assertTrue(javaTransition.getActivate().contains(21L));
            assertFalse(javaTransition.getActivate().contains(null));
        }

        @Test
        @DisplayName("Should handle empty activate names")
        public void testEmptyActivateNames() {
            StateTransitions stateTransitions = new StateTransitions();
            stateTransitions.setStateName("HomePage");

            JavaStateTransition javaTransition = new JavaStateTransition();
            javaTransition.setActivateNames(new HashSet<>());
            javaTransition.setActivate(new HashSet<>());

            stateTransitions.setTransitions(Collections.singletonList(javaTransition));

            when(stateService.getStateId("HomePage")).thenReturn(1L);

            stateIdResolver.convertNamesToIds(stateTransitions);

            assertTrue(javaTransition.getActivate().isEmpty());
        }

        @Test
        @DisplayName("Should preserve existing activate IDs")
        public void testPreserveExistingActivateIds() {
            StateTransitions stateTransitions = new StateTransitions();
            stateTransitions.setStateName("HomePage");

            JavaStateTransition javaTransition = new JavaStateTransition();
            javaTransition.setActivateNames(new HashSet<>(Arrays.asList("NewState")));
            Set<Long> existingIds = new HashSet<>(Arrays.asList(100L, 101L));
            javaTransition.setActivate(existingIds);

            stateTransitions.setTransitions(Collections.singletonList(javaTransition));

            when(stateService.getStateId("HomePage")).thenReturn(1L);
            when(stateService.getStateId("NewState")).thenReturn(102L);

            stateIdResolver.convertNamesToIds(stateTransitions);

            assertEquals(3, javaTransition.getActivate().size());
            assertTrue(javaTransition.getActivate().contains(100L));
            assertTrue(javaTransition.getActivate().contains(101L));
            assertTrue(javaTransition.getActivate().contains(102L));
        }
    }

    @Nested
    @DisplayName("Multiple Transitions")
    class MultipleTransitions {

        @Test
        @DisplayName("Should process multiple transitions")
        public void testMultipleTransitions() {
            StateTransitions stateTransitions = new StateTransitions();
            stateTransitions.setStateName("MainState");

            JavaStateTransition transition1 = new JavaStateTransition();
            transition1.setActivateNames(new HashSet<>(Arrays.asList("State1", "State2")));
            transition1.setActivate(new HashSet<>());

            JavaStateTransition transition2 = new JavaStateTransition();
            transition2.setActivateNames(new HashSet<>(Arrays.asList("State3", "State4")));
            transition2.setActivate(new HashSet<>());

            stateTransitions.setTransitions(Arrays.asList(transition1, transition2));

            when(stateService.getStateId("MainState")).thenReturn(1L);
            when(stateService.getStateId("State1")).thenReturn(10L);
            when(stateService.getStateId("State2")).thenReturn(11L);
            when(stateService.getStateId("State3")).thenReturn(12L);
            when(stateService.getStateId("State4")).thenReturn(13L);

            stateIdResolver.convertNamesToIds(stateTransitions);

            assertEquals(2, transition1.getActivate().size());
            assertTrue(transition1.getActivate().contains(10L));
            assertTrue(transition1.getActivate().contains(11L));

            assertEquals(2, transition2.getActivate().size());
            assertTrue(transition2.getActivate().contains(12L));
            assertTrue(transition2.getActivate().contains(13L));
        }

        @Test
        @DisplayName("Should handle mixed transition types")
        public void testMixedTransitionTypes() {
            StateTransitions stateTransitions = new StateTransitions();
            stateTransitions.setStateName("MainState");

            JavaStateTransition javaTransition = new JavaStateTransition();
            javaTransition.setActivateNames(new HashSet<>(Arrays.asList("JavaState")));
            javaTransition.setActivate(new HashSet<>());

            StateTransition regularTransition = mock(StateTransition.class);

            stateTransitions.setTransitions(Arrays.asList(javaTransition, regularTransition));

            when(stateService.getStateId("MainState")).thenReturn(1L);
            when(stateService.getStateId("JavaState")).thenReturn(10L);

            stateIdResolver.convertNamesToIds(stateTransitions);

            assertEquals(1, javaTransition.getActivate().size());
            assertTrue(javaTransition.getActivate().contains(10L));

            // Regular transition should not be processed
            verifyNoInteractions(regularTransition);
        }
    }

    @Nested
    @DisplayName("Batch Conversion")
    class BatchConversion {

        @Test
        @DisplayName("Should convert all state transitions in batch")
        public void testBatchConversion() {
            StateTransitions transitions1 = new StateTransitions();
            transitions1.setStateName("State1");

            StateTransitions transitions2 = new StateTransitions();
            transitions2.setStateName("State2");

            StateTransitions transitions3 = new StateTransitions();
            transitions3.setStateName("State3");

            List<StateTransitions> allTransitions =
                    Arrays.asList(transitions1, transitions2, transitions3);

            when(stateService.getStateId("State1")).thenReturn(1L);
            when(stateService.getStateId("State2")).thenReturn(2L);
            when(stateService.getStateId("State3")).thenReturn(3L);

            stateIdResolver.convertAllStateTransitions(allTransitions);

            assertEquals(1L, transitions1.getStateId());
            assertEquals(2L, transitions2.getStateId());
            assertEquals(3L, transitions3.getStateId());

            verify(stateService).getStateId("State1");
            verify(stateService).getStateId("State2");
            verify(stateService).getStateId("State3");
        }

        @Test
        @DisplayName("Should handle empty list")
        public void testEmptyBatchConversion() {
            List<StateTransitions> emptyList = new ArrayList<>();

            assertDoesNotThrow(() -> stateIdResolver.convertAllStateTransitions(emptyList));

            verifyNoInteractions(stateService);
        }

        @Test
        @DisplayName("Should handle null in batch list")
        public void testNullInBatchList() {
            StateTransitions validTransitions = new StateTransitions();
            validTransitions.setStateName("ValidState");

            List<StateTransitions> mixedList = Arrays.asList(validTransitions, null);

            when(stateService.getStateId("ValidState")).thenReturn(1L);

            assertThrows(
                    NullPointerException.class,
                    () -> stateIdResolver.convertAllStateTransitions(mixedList));
        }
    }

    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {

        @Test
        @DisplayName("Should handle deep transition hierarchy")
        public void testDeepTransitionHierarchy() {
            StateTransitions rootTransitions = new StateTransitions();
            rootTransitions.setStateName("RootState");

            JavaStateTransition level1 = new JavaStateTransition();
            level1.setActivateNames(new HashSet<>(Arrays.asList("Child1", "Child2")));
            level1.setActivate(new HashSet<>());

            JavaStateTransition level2 = new JavaStateTransition();
            level2.setActivateNames(
                    new HashSet<>(Arrays.asList("GrandChild1", "GrandChild2", "GrandChild3")));
            level2.setActivate(new HashSet<>());

            rootTransitions.setTransitions(Arrays.asList(level1, level2));

            when(stateService.getStateId("RootState")).thenReturn(1L);
            when(stateService.getStateId("Child1")).thenReturn(10L);
            when(stateService.getStateId("Child2")).thenReturn(11L);
            when(stateService.getStateId("GrandChild1")).thenReturn(20L);
            when(stateService.getStateId("GrandChild2")).thenReturn(21L);
            when(stateService.getStateId("GrandChild3")).thenReturn(22L);

            stateIdResolver.convertNamesToIds(rootTransitions);

            assertEquals(1L, rootTransitions.getStateId());

            assertEquals(2, level1.getActivate().size());
            assertTrue(level1.getActivate().containsAll(Arrays.asList(10L, 11L)));

            assertEquals(3, level2.getActivate().size());
            assertTrue(level2.getActivate().containsAll(Arrays.asList(20L, 21L, 22L)));
        }

        @Test
        @DisplayName("Should handle duplicate state names")
        public void testDuplicateStateNames() {
            StateTransitions stateTransitions = new StateTransitions();
            stateTransitions.setStateName("MainState");

            JavaStateTransition transition = new JavaStateTransition();
            transition.setActivateNames(
                    new HashSet<>(Arrays.asList("DupState", "DupState", "DupState")));
            transition.setActivate(new HashSet<>());

            stateTransitions.setTransitions(Collections.singletonList(transition));

            when(stateService.getStateId("MainState")).thenReturn(1L);
            when(stateService.getStateId("DupState")).thenReturn(10L);

            stateIdResolver.convertNamesToIds(stateTransitions);

            // Sets will only contain one instance of the ID (no duplicates)
            assertEquals(1, transition.getActivate().size());
            assertTrue(transition.getActivate().contains(10L));
        }

        @Test
        @DisplayName("Should handle large number of transitions")
        public void testLargeNumberOfTransitions() {
            StateTransitions stateTransitions = new StateTransitions();
            stateTransitions.setStateName("MainState");

            List<StateTransition> transitions = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                JavaStateTransition transition = new JavaStateTransition();
                transition.setActivateNames(new HashSet<>(Arrays.asList("State" + i)));
                transition.setActivate(new HashSet<>());
                transitions.add(transition);

                when(stateService.getStateId("State" + i)).thenReturn((long) i);
            }

            stateTransitions.setTransitions(transitions);
            when(stateService.getStateId("MainState")).thenReturn(1000L);

            stateIdResolver.convertNamesToIds(stateTransitions);

            assertEquals(1000L, stateTransitions.getStateId());

            for (int i = 0; i < 100; i++) {
                JavaStateTransition transition = (JavaStateTransition) transitions.get(i);
                assertEquals(1, transition.getActivate().size());
                assertTrue(transition.getActivate().contains((long) i));
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle null transitions list")
        public void testNullTransitionsList() {
            StateTransitions stateTransitions = new StateTransitions();
            stateTransitions.setStateName("MainState");
            stateTransitions.setTransitions(null);

            when(stateService.getStateId("MainState")).thenReturn(1L);

            // The current implementation throws NullPointerException with null transitions
            assertThrows(
                    NullPointerException.class,
                    () -> stateIdResolver.convertNamesToIds(stateTransitions),
                    "Should throw NullPointerException when transitions is null");
        }

        @Test
        @DisplayName("Should handle special characters in state names")
        public void testSpecialCharactersInStateNames() {
            StateTransitions stateTransitions = new StateTransitions();
            stateTransitions.setStateName("State-With_Special.Characters$123");

            JavaStateTransition transition = new JavaStateTransition();
            transition.setActivateNames(new HashSet<>(Arrays.asList("State@With#Symbols")));
            transition.setActivate(new HashSet<>());

            stateTransitions.setTransitions(Collections.singletonList(transition));

            when(stateService.getStateId("State-With_Special.Characters$123")).thenReturn(100L);
            when(stateService.getStateId("State@With#Symbols")).thenReturn(200L);

            stateIdResolver.convertNamesToIds(stateTransitions);

            assertEquals(100L, stateTransitions.getStateId());
            assertEquals(1, transition.getActivate().size());
            assertTrue(transition.getActivate().contains(200L));
        }
    }
}
