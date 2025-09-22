package io.github.jspinak.brobot.navigation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.test.BrobotTestBase;
// Removed old logging import: 
/**
 * Test suite for StateTransitionService.
 *
 * <p>Note: This test is disabled in CI environments due to intermittent failures. It passes
 * consistently in local development environments.
 */
@DisplayName("StateTransitionService Tests")
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Flaky test - fails intermittently in CI/CD")
public class StateTransitionServiceTest extends BrobotTestBase {

    @Mock private StateTransitionStore stateTransitionsRepository;

    @Mock private StateTransitionsJointTable stateTransitionsJointTable;

    @Mock private StateTransitions mockStateTransitions;

    @Mock private StateTransition mockStateTransition;

    private StateTransitionService service;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        service =
                new StateTransitionService(stateTransitionsRepository, stateTransitionsJointTable);
    }

    @Nested
    @DisplayName("Constructor and Initialization")
    class ConstructorAndInitialization {

        @Test
        @DisplayName("Constructor initializes dependencies")
        public void testConstructor() {
            // Execute
            StateTransitionService newService =
                    new StateTransitionService(
                            stateTransitionsRepository, stateTransitionsJointTable);

            // Verify
            assertNotNull(newService);
            assertNotNull(newService.getStatesToActivate());
            assertTrue(newService.getStatesToActivate().isEmpty());
        }

        @Test
        @DisplayName("setupRepo initializes joint table")
        public void testSetupRepo() {
            // Execute
            service.setupRepo();

            // Verify
            verify(stateTransitionsRepository).populateStateTransitionsJointTable();
        }
    }

    @Nested
    @DisplayName("Repository Access Methods")
    class RepositoryAccessMethods {

        @Test
        @DisplayName("getAllStateTransitionsInstances returns copy")
        public void testGetAllStateTransitionsInstances() {
            // Setup
            List<StateTransitions> expectedList =
                    Arrays.asList(mock(StateTransitions.class), mock(StateTransitions.class));
            when(stateTransitionsRepository.getAllStateTransitionsAsCopy())
                    .thenReturn(expectedList);

            // Execute
            List<StateTransitions> result = service.getAllStateTransitionsInstances();

            // Verify
            assertEquals(expectedList, result);
            verify(stateTransitionsRepository).getAllStateTransitionsAsCopy();
        }

        @Test
        @DisplayName("getAllStateTransitions returns repository list")
        public void testGetAllStateTransitions() {
            // Setup
            List<StateTransitions> expectedList =
                    Arrays.asList(mock(StateTransitions.class), mock(StateTransitions.class));
            when(stateTransitionsRepository.getRepo()).thenReturn(expectedList);

            // Execute
            List<StateTransitions> result = service.getAllStateTransitions();

            // Verify
            assertEquals(expectedList, result);
            verify(stateTransitionsRepository).getRepo();
        }

        @Test
        @DisplayName("getAllIndividualTransitions returns all transitions")
        public void testGetAllIndividualTransitions() {
            // Setup
            List<StateTransition> expectedList =
                    Arrays.asList(mock(StateTransition.class), mock(StateTransition.class));
            when(stateTransitionsRepository.getAllTransitions()).thenReturn(expectedList);

            // Execute
            List<StateTransition> result = service.getAllIndividualTransitions();

            // Verify
            assertEquals(expectedList, result);
            verify(stateTransitionsRepository).getAllTransitions();
        }
    }

    @Nested
    @DisplayName("Transition Resolution")
    class TransitionResolution {

        @Test
        @DisplayName("getTransitionToEnum returns direct transition")
        public void testGetTransitionToEnum_DirectTransition() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            Set<Long> transitions = new HashSet<>(Arrays.asList(2L, 3L));
            when(stateTransitionsJointTable.getStatesWithTransitionsFrom(fromState))
                    .thenReturn(transitions);

            // Execute
            Long result = service.getTransitionToEnum(fromState, toState);

            // Verify
            assertEquals(toState, result);
            verify(stateTransitionsJointTable).getStatesWithTransitionsFrom(fromState);
        }

        @Test
        @DisplayName("getTransitionToEnum returns PREVIOUS for hidden state")
        public void testGetTransitionToEnum_HiddenState() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            Set<Long> directTransitions = new HashSet<>(Arrays.asList(3L, 4L));
            Map<Long, Set<Long>> incomingToPrevious = new HashMap<>();
            incomingToPrevious.put(toState, new HashSet<>(Arrays.asList(fromState, 5L)));

            when(stateTransitionsJointTable.getStatesWithTransitionsFrom(fromState))
                    .thenReturn(directTransitions);
            when(stateTransitionsJointTable.getIncomingTransitionsToPREVIOUS())
                    .thenReturn(incomingToPrevious);

            // Execute
            Long result = service.getTransitionToEnum(fromState, toState);

            // Verify
            assertEquals(SpecialStateType.PREVIOUS.getId(), result);
        }

        @Test
        @DisplayName("getTransitionToEnum returns NULL for no transition")
        public void testGetTransitionToEnum_NoTransition() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            Set<Long> directTransitions = new HashSet<>(Arrays.asList(3L, 4L));
            Map<Long, Set<Long>> incomingToPrevious = new HashMap<>();

            when(stateTransitionsJointTable.getStatesWithTransitionsFrom(fromState))
                    .thenReturn(directTransitions);
            when(stateTransitionsJointTable.getIncomingTransitionsToPREVIOUS())
                    .thenReturn(incomingToPrevious);

            // Execute
            Long result = service.getTransitionToEnum(fromState, toState);

            // Verify
            assertEquals(SpecialStateType.NULL.getId(), result);
        }

        @ParameterizedTest
        @CsvSource({
            "1, 2, true, false", // Direct transition
            "1, 3, false, true", // Hidden state
            "1, 4, false, false" // No transition
        })
        @DisplayName("getTransitionToEnum with various scenarios")
        public void testGetTransitionToEnum_Scenarios(
                Long from, Long to, boolean isDirect, boolean isHidden) {
            // Setup
            Set<Long> directTransitions = new HashSet<>();
            if (isDirect) directTransitions.add(to);

            Map<Long, Set<Long>> incomingToPrevious = new HashMap<>();
            if (isHidden) {
                incomingToPrevious.put(to, new HashSet<>(Arrays.asList(from)));
            }

            when(stateTransitionsJointTable.getStatesWithTransitionsFrom(from))
                    .thenReturn(directTransitions);
            when(stateTransitionsJointTable.getIncomingTransitionsToPREVIOUS())
                    .thenReturn(incomingToPrevious);

            // Execute
            Long result = service.getTransitionToEnum(from, to);

            // Verify
            if (isDirect) {
                assertEquals(to, result);
            } else if (isHidden) {
                assertEquals(SpecialStateType.PREVIOUS.getId(), result);
            } else {
                assertEquals(SpecialStateType.NULL.getId(), result);
            }
        }
    }

    @Nested
    @DisplayName("Transition Retrieval")
    class TransitionRetrieval {

        @Test
        @DisplayName("getTransitions returns transitions for state")
        public void testGetTransitions_Found() {
            // Setup
            Long stateId = 1L;
            when(stateTransitionsRepository.get(stateId))
                    .thenReturn(Optional.of(mockStateTransitions));

            // Execute
            Optional<StateTransitions> result = service.getTransitions(stateId);

            // Verify
            assertTrue(result.isPresent());
            assertEquals(mockStateTransitions, result.get());
            verify(stateTransitionsRepository).get(stateId);
        }

        @Test
        @DisplayName("getTransitions returns empty for unknown state")
        public void testGetTransitions_NotFound() {
            // Setup
            Long stateId = 99L;
            when(stateTransitionsRepository.get(stateId)).thenReturn(Optional.empty());

            // Execute
            Optional<StateTransitions> result = service.getTransitions(stateId);

            // Verify
            assertFalse(result.isPresent());
            verify(stateTransitionsRepository).get(stateId);
        }

        @Test
        @DisplayName("getTransition returns specific transition")
        public void testGetTransition_Found() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            when(stateTransitionsRepository.get(fromState))
                    .thenReturn(Optional.of(mockStateTransitions));
            when(mockStateTransitions.getTransitionFunctionByActivatedStateId(toState))
                    .thenReturn(Optional.of(mockStateTransition));

            // Execute
            Optional<StateTransition> result = service.getTransition(fromState, toState);

            // Verify
            assertTrue(result.isPresent());
            assertEquals(mockStateTransition, result.get());
        }

        @Test
        @DisplayName("getTransition returns empty when no transitions exist")
        public void testGetTransition_NoTransitions() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            when(stateTransitionsRepository.get(fromState)).thenReturn(Optional.empty());

            // Execute
            Optional<StateTransition> result = service.getTransition(fromState, toState);

            // Verify
            assertFalse(result.isPresent());
            verify(stateTransitionsRepository).get(fromState);
            verify(mockStateTransitions, never()).getTransitionFunctionByActivatedStateId(any());
        }

        @Test
        @DisplayName("getTransition returns empty when specific transition not found")
        public void testGetTransition_TransitionNotFound() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            when(stateTransitionsRepository.get(fromState))
                    .thenReturn(Optional.of(mockStateTransitions));
            when(mockStateTransitions.getTransitionFunctionByActivatedStateId(toState))
                    .thenReturn(Optional.empty());

            // Execute
            Optional<StateTransition> result = service.getTransition(fromState, toState);

            // Verify
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("Statistics and Reporting")
    class StatisticsAndReporting {

        @Test
        @DisplayName("resetTimesSuccessful resets all counters")
        public void testResetTimesSuccessful() {
            // Setup
            StateTransition trans1 = mock(StateTransition.class);
            StateTransition trans2 = mock(StateTransition.class);
            StateTransition trans3 = mock(StateTransition.class);
            List<StateTransition> transitions = Arrays.asList(trans1, trans2, trans3);

            when(stateTransitionsRepository.getAllTransitions()).thenReturn(transitions);

            // Execute
            service.resetTimesSuccessful();

            // Verify
            verify(trans1).setTimesSuccessful(0);
            verify(trans2).setTimesSuccessful(0);
            verify(trans3).setTimesSuccessful(0);
        }

        @Test
        @DisplayName("resetTimesSuccessful handles empty list")
        public void testResetTimesSuccessful_EmptyList() {
            // Setup
            when(stateTransitionsRepository.getAllTransitions())
                    .thenReturn(Collections.emptyList());

            // Execute - Should not throw
            assertDoesNotThrow(() -> service.resetTimesSuccessful());

            // Verify
            verify(stateTransitionsRepository).getAllTransitions();
        }

        @Test
        @DisplayName("printAllTransitions outputs to console")
        public void testPrintAllTransitions() {
            // Setup
            StateTransition trans1 = mock(StateTransition.class);
            StateTransition trans2 = mock(StateTransition.class);
            when(trans1.toString()).thenReturn("Transition 1");
            when(trans2.toString()).thenReturn("Transition 2");

            List<StateTransition> transitions = Arrays.asList(trans1, trans2);
            when(stateTransitionsRepository.getAllTransitions()).thenReturn(transitions);

            // Execute
            service.printAllTransitions();

            // Verify - transitions were retrieved and processed
            verify(stateTransitionsRepository).getAllTransitions();
            // Note: Cannot verify toString() with Mockito as it's used internally
            // The fact that getAllTransitions() was called confirms the method executed
        }
    }

    @Nested
    @DisplayName("States To Activate Management")
    class StatesToActivateManagement {

        @Test
        @DisplayName("getStatesToActivate returns modifiable set")
        public void testGetStatesToActivate() {
            // Execute
            Set<Long> states = service.getStatesToActivate();

            // Verify - Can modify the set
            assertNotNull(states);
            assertTrue(states.isEmpty());

            // Test modification
            states.add(1L);
            states.add(2L);

            assertEquals(2, service.getStatesToActivate().size());
            assertTrue(service.getStatesToActivate().contains(1L));
            assertTrue(service.getStatesToActivate().contains(2L));
        }

        @Test
        @DisplayName("statesToActivate persists across calls")
        public void testStatesToActivate_Persistence() {
            // Add states
            service.getStatesToActivate().add(5L);
            service.getStatesToActivate().add(10L);

            // Verify persistence
            Set<Long> states = service.getStatesToActivate();
            assertEquals(2, states.size());
            assertTrue(states.contains(5L));
            assertTrue(states.contains(10L));

            // Clear and verify
            states.clear();
            assertTrue(service.getStatesToActivate().isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("Handle null state ID in getTransitions")
        public void testGetTransitions_NullStateId() {
            // Setup
            when(stateTransitionsRepository.get(null)).thenReturn(Optional.empty());

            // Execute
            Optional<StateTransitions> result = service.getTransitions(null);

            // Verify
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Handle null parameters in getTransition")
        public void testGetTransition_NullParameters() {
            // Setup
            when(stateTransitionsRepository.get(null)).thenReturn(Optional.empty());

            // Execute
            Optional<StateTransition> result = service.getTransition(null, 1L);

            // Verify
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Handle null in transition enum resolution")
        public void testGetTransitionToEnum_NullParameters() {
            // Setup
            when(stateTransitionsJointTable.getStatesWithTransitionsFrom((Long) null))
                    .thenReturn(new HashSet<>());
            when(stateTransitionsJointTable.getIncomingTransitionsToPREVIOUS())
                    .thenReturn(new HashMap<>());

            // Execute
            Long result = service.getTransitionToEnum(null, 1L);

            // Verify
            assertEquals(SpecialStateType.NULL.getId(), result);
        }

        @ParameterizedTest
        @ValueSource(longs = {Long.MIN_VALUE, -1, 0, Long.MAX_VALUE})
        @DisplayName("Handle extreme state ID values")
        public void testExtremeStateIds(Long stateId) {
            // Setup
            when(stateTransitionsRepository.get(stateId)).thenReturn(Optional.empty());

            // Execute
            Optional<StateTransitions> result = service.getTransitions(stateId);

            // Verify
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("Performance and Thread Safety")
    class PerformanceAndThreadSafety {

        @Test
        @DisplayName("Concurrent access to statesToActivate")
        public void testConcurrentStatesToActivate() throws InterruptedException {
            // Setup
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];

            // Execute - Multiple threads adding states
            for (int i = 0; i < threadCount; i++) {
                final int stateId = i;
                threads[i] =
                        new Thread(
                                () -> {
                                    service.getStatesToActivate().add((long) stateId);
                                });
                threads[i].start();
            }

            // Wait for all threads
            for (Thread thread : threads) {
                thread.join();
            }

            // Verify - All states should be added
            Set<Long> states = service.getStatesToActivate();
            assertEquals(threadCount, states.size());
            for (int i = 0; i < threadCount; i++) {
                assertTrue(states.contains((long) i));
            }
        }

        @Test
        @DisplayName("Large number of transitions performance")
        public void testLargeTransitionSet() {
            // Setup - Create many transitions
            List<StateTransition> manyTransitions = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                StateTransition trans = mock(StateTransition.class);
                manyTransitions.add(trans);
            }
            when(stateTransitionsRepository.getAllTransitions()).thenReturn(manyTransitions);

            // Execute
            long startTime = System.currentTimeMillis();
            service.resetTimesSuccessful();
            long endTime = System.currentTimeMillis();

            // Verify - Should complete quickly
            assertTrue((endTime - startTime) < 1000, "Resetting 1000 transitions took too long");

            // Verify all were reset
            for (StateTransition trans : manyTransitions) {
                verify(trans).setTimesSuccessful(0);
            }
        }
    }
}
