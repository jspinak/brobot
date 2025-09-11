package io.github.jspinak.brobot.model.transition;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.navigation.transition.TaskSequenceStateTransition;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for the StateTransitionStore class which manages the repository of state
 * transitions in the Brobot framework.
 */
@DisplayName("StateTransitionStore Tests")
public class StateTransitionStoreTest extends BrobotTestBase {

    private StateTransitionStore store;
    private StateTransitionsJointTable mockJointTable;
    private StateTransitions mockStateTransitions1;
    private StateTransitions mockStateTransitions2;
    private StateTransitions mockStateTransitions3;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockJointTable = mock(StateTransitionsJointTable.class);
        store = new StateTransitionStore(mockJointTable);

        // Create mock StateTransitions
        mockStateTransitions1 = mock(StateTransitions.class);
        mockStateTransitions2 = mock(StateTransitions.class);
        mockStateTransitions3 = mock(StateTransitions.class);

        when(mockStateTransitions1.getStateId()).thenReturn(1L);
        when(mockStateTransitions2.getStateId()).thenReturn(2L);
        when(mockStateTransitions3.getStateId()).thenReturn(3L);
    }

    @Test
    @DisplayName("Should initialize with empty repository")
    void testInitialization() {
        // Then
        assertNotNull(store.getRepo());
        assertTrue(store.getRepo().isEmpty());
        assertNotNull(store.getStateTransitionsJointTable());
        assertSame(mockJointTable, store.getStateTransitionsJointTable());
    }

    @Test
    @DisplayName("Should add StateTransitions to repository")
    void testAddStateTransitions() {
        // When
        store.add(mockStateTransitions1);
        store.add(mockStateTransitions2);

        // Then
        assertEquals(2, store.getRepo().size());
        assertTrue(store.getRepo().contains(mockStateTransitions1));
        assertTrue(store.getRepo().contains(mockStateTransitions2));
    }

    @Test
    @DisplayName("Should get StateTransitions by state ID")
    void testGetByStateId() {
        // Given
        store.add(mockStateTransitions1);
        store.add(mockStateTransitions2);
        store.add(mockStateTransitions3);

        // When
        Optional<StateTransitions> result1 = store.get(1L);
        Optional<StateTransitions> result2 = store.get(2L);
        Optional<StateTransitions> result3 = store.get(3L);
        Optional<StateTransitions> notFound = store.get(999L);

        // Then
        assertTrue(result1.isPresent());
        assertEquals(mockStateTransitions1, result1.get());

        assertTrue(result2.isPresent());
        assertEquals(mockStateTransitions2, result2.get());

        assertTrue(result3.isPresent());
        assertEquals(mockStateTransitions3, result3.get());

        assertTrue(notFound.isEmpty());
    }

    @Test
    @DisplayName("Should handle null state ID in get")
    void testGetWithNullId() {
        // Given
        store.add(mockStateTransitions1);

        // When
        Optional<StateTransitions> result = store.get(null);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should populate joint table")
    void testPopulateJointTable() {
        // Given
        store.add(mockStateTransitions1);
        store.add(mockStateTransitions2);
        store.add(mockStateTransitions3);

        // When
        store.populateStateTransitionsJointTable();

        // Then
        verify(mockJointTable).addToJointTable(mockStateTransitions1);
        verify(mockJointTable).addToJointTable(mockStateTransitions2);
        verify(mockJointTable).addToJointTable(mockStateTransitions3);
    }

    @Test
    @DisplayName("Should get all state IDs")
    void testGetAllStateIds() {
        // Given
        StateTransitions nullIdTransitions = mock(StateTransitions.class);
        when(nullIdTransitions.getStateId()).thenReturn(null);

        store.add(mockStateTransitions1);
        store.add(mockStateTransitions2);
        store.add(mockStateTransitions3);
        store.add(nullIdTransitions);

        // When
        Set<Long> stateIds = store.getAllStateIds();

        // Then
        assertEquals(3, stateIds.size());
        assertTrue(stateIds.contains(1L));
        assertTrue(stateIds.contains(2L));
        assertTrue(stateIds.contains(3L));
        assertFalse(stateIds.contains(null));
    }

    @Test
    @DisplayName("Should get all transitions")
    void testGetAllTransitions() {
        // Given
        StateTransition transition1 = new TaskSequenceStateTransition();
        StateTransition transition2 = new TaskSequenceStateTransition();
        StateTransition finishTransition = new TaskSequenceStateTransition();

        when(mockStateTransitions1.getTransitions()).thenReturn(List.of(transition1, transition2));
        when(mockStateTransitions1.getTransitionFinish()).thenReturn(finishTransition);

        store.add(mockStateTransitions1);

        // When
        List<StateTransition> allTransitions = store.getAllTransitions();

        // Then
        assertEquals(3, allTransitions.size());
        assertTrue(allTransitions.contains(transition1));
        assertTrue(allTransitions.contains(transition2));
        assertTrue(allTransitions.contains(finishTransition));
    }

    @Test
    @DisplayName("Should get all StateTransitions as copy")
    void testGetAllStateTransitionsAsCopy() {
        // Given
        store.add(mockStateTransitions1);
        store.add(mockStateTransitions2);

        // When
        List<StateTransitions> copy = store.getAllStateTransitionsAsCopy();

        // Then
        assertEquals(2, copy.size());

        // Verify it's a copy by modifying it
        copy.clear();
        assertEquals(0, copy.size());
        assertEquals(2, store.getRepo().size()); // Original unchanged
    }

    @Test
    @DisplayName("Should empty repositories")
    void testEmptyRepos() {
        // Given
        store.add(mockStateTransitions1);
        store.add(mockStateTransitions2);
        assertEquals(2, store.getRepo().size());

        // When
        store.emptyRepos();

        // Then
        assertEquals(0, store.getRepo().size());
        verify(mockJointTable).emptyRepos();
    }

    @Test
    @DisplayName("Should handle concurrent modifications")
    void testConcurrentModifications() {
        // CopyOnWriteArrayList should handle concurrent modifications

        // Given
        store.add(mockStateTransitions1);

        // When - Iterate while modifying
        for (StateTransitions st : store.getRepo()) {
            store.add(mockStateTransitions2); // Add during iteration
        }

        // Then - No ConcurrentModificationException
        assertEquals(2, store.getRepo().size());
    }

    @Test
    @DisplayName("Should print repository contents")
    void testPrint() {
        // Given
        when(mockStateTransitions1.toString()).thenReturn("StateTransitions1");
        when(mockStateTransitions2.toString()).thenReturn("StateTransitions2");

        store.add(mockStateTransitions1);
        store.add(mockStateTransitions2);

        // Capture output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            // When
            store.print();

            // Then
            String output = outputStream.toString();
            assertTrue(output.contains("StateTransitionsRepository:"));
            assertTrue(output.contains("StateTransitions1"));
            assertTrue(output.contains("StateTransitions2"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @TestFactory
    @DisplayName("StateTransitionStore usage scenarios")
    Stream<DynamicTest> testUsageScenarios() {
        return Stream.of(
                dynamicTest(
                        "Empty store operations",
                        () -> {
                            StateTransitionStore emptyStore =
                                    new StateTransitionStore(mockJointTable);

                            assertTrue(emptyStore.getAllStateIds().isEmpty());
                            assertTrue(emptyStore.getAllTransitions().isEmpty());
                            assertTrue(emptyStore.getAllStateTransitionsAsCopy().isEmpty());
                            assertTrue(emptyStore.get(1L).isEmpty());
                        }),
                dynamicTest(
                        "Single state transitions",
                        () -> {
                            StateTransitionStore singleStore =
                                    new StateTransitionStore(mockJointTable);
                            singleStore.add(mockStateTransitions1);

                            assertEquals(1, singleStore.getRepo().size());
                            assertEquals(1, singleStore.getAllStateIds().size());
                            assertTrue(singleStore.get(1L).isPresent());
                            assertFalse(singleStore.get(2L).isPresent());
                        }),
                dynamicTest(
                        "Multiple states with same ID",
                        () -> {
                            StateTransitionStore duplicateStore =
                                    new StateTransitionStore(mockJointTable);
                            StateTransitions duplicate = mock(StateTransitions.class);
                            when(duplicate.getStateId()).thenReturn(1L);

                            duplicateStore.add(mockStateTransitions1);
                            duplicateStore.add(duplicate);

                            // Both added to repo
                            assertEquals(2, duplicateStore.getRepo().size());

                            // get() returns first match
                            Optional<StateTransitions> result = duplicateStore.get(1L);
                            assertTrue(result.isPresent());
                            assertEquals(mockStateTransitions1, result.get());
                        }),
                dynamicTest(
                        "Large repository performance",
                        () -> {
                            StateTransitionStore largeStore =
                                    new StateTransitionStore(mockJointTable);

                            // Add many transitions
                            for (long i = 0; i < 1000; i++) {
                                StateTransitions st = mock(StateTransitions.class);
                                when(st.getStateId()).thenReturn(i);
                                largeStore.add(st);
                            }

                            assertEquals(1000, largeStore.getRepo().size());
                            assertEquals(1000, largeStore.getAllStateIds().size());

                            // Lookup should still work
                            assertTrue(largeStore.get(500L).isPresent());
                            assertTrue(largeStore.get(999L).isPresent());
                            assertFalse(largeStore.get(1000L).isPresent());
                        }));
    }

    @Test
    @DisplayName("Should handle StateTransitions with empty transitions list")
    void testEmptyTransitionsList() {
        // Given
        when(mockStateTransitions1.getTransitions()).thenReturn(new ArrayList<>());
        when(mockStateTransitions1.getTransitionFinish())
                .thenReturn(new TaskSequenceStateTransition());

        store.add(mockStateTransitions1);

        // When
        List<StateTransition> allTransitions = store.getAllTransitions();

        // Then
        assertEquals(1, allTransitions.size()); // Only finish transition
    }

    @Test
    @DisplayName("Should maintain insertion order")
    void testInsertionOrder() {
        // Given
        store.add(mockStateTransitions3);
        store.add(mockStateTransitions1);
        store.add(mockStateTransitions2);

        // When
        List<StateTransitions> repo = store.getRepo();

        // Then - Order is preserved
        assertEquals(mockStateTransitions3, repo.get(0));
        assertEquals(mockStateTransitions1, repo.get(1));
        assertEquals(mockStateTransitions2, repo.get(2));
    }

    @Test
    @DisplayName("Should handle null StateTransitions")
    void testNullStateTransitions() {
        // When
        store.add(null);

        // Then
        assertEquals(1, store.getRepo().size());
        assertTrue(store.getRepo().contains(null));

        // getAllStateIds will throw NPE if null StateTransitions are present
        // This is the actual behavior - the code doesn't handle null StateTransitions
        assertThrows(NullPointerException.class, () -> store.getAllStateIds());
    }

    @Test
    @DisplayName("Should verify thread safety with CopyOnWriteArrayList")
    void testThreadSafety() {
        // CopyOnWriteArrayList provides thread safety
        List<StateTransitions> repo = store.getRepo();

        // Verify it's a CopyOnWriteArrayList
        assertTrue(repo instanceof java.util.concurrent.CopyOnWriteArrayList);
    }

    @Test
    @DisplayName("Should handle transitions with null finish")
    void testNullFinishTransition() {
        // Given
        when(mockStateTransitions1.getTransitions())
                .thenReturn(List.of(new TaskSequenceStateTransition()));
        when(mockStateTransitions1.getTransitionFinish()).thenReturn(null);

        store.add(mockStateTransitions1);

        // When
        List<StateTransition> allTransitions = store.getAllTransitions();

        // Then
        assertEquals(2, allTransitions.size()); // Regular transition + null
        assertTrue(allTransitions.contains(null));
    }

    @Test
    @DisplayName("Should support filtering operations")
    void testFilteringOperations() {
        // Given
        StateTransitions activeTransitions = mock(StateTransitions.class);
        when(activeTransitions.getStateId()).thenReturn(10L);

        StateTransitions inactiveTransitions = mock(StateTransitions.class);
        when(inactiveTransitions.getStateId()).thenReturn(20L);

        store.add(activeTransitions);
        store.add(inactiveTransitions);

        // When - Custom filtering
        Set<Long> activeIds = Set.of(10L);
        List<StateTransitions> activeOnly =
                store.getRepo().stream()
                        .filter(
                                st ->
                                        st.getStateId() != null
                                                && activeIds.contains(st.getStateId()))
                        .toList();

        // Then
        assertEquals(1, activeOnly.size());
        assertEquals(activeTransitions, activeOnly.get(0));
    }
}
