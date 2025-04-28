package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StateTransitionsJointTableTest {

    @Autowired
    private StateTransitionsJointTable stateTransitionsJointTable;

    @Autowired
    private StateTransitionsRepository stateTransitionsRepository;

    private StateTransitionsJointTable table;

    @BeforeEach
    void setUp() {
        stateTransitionsJointTable.emptyRepos();
        table = new StateTransitionsJointTable();
    }

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Test
    void testAddAndRetrieveTransition() {
        Long stateA = 1L;
        Long stateB = 2L;

        // Create and add a transition from A to B
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateId(stateA);
        ActionDefinitionStateTransition transition = new ActionDefinitionStateTransition();
        transition.setActivate(Collections.singleton(stateB));
        stateTransitions.addTransition(transition);
        stateTransitionsRepository.add(stateTransitions);
        stateTransitionsRepository.populateStateTransitionsJointTable();
        System.out.println(stateTransitions);

        // Now retrieve states with transitions to B
        stateTransitionsJointTable.print();
        Set<Long> statesWithTransitionsToB = stateTransitionsJointTable.getStatesWithTransitionsTo(stateB);

        assertFalse(statesWithTransitionsToB.isEmpty());
        assertTrue(statesWithTransitionsToB.contains(stateA));
    }

    @Test
    void testAddAndGetTransitions() {
        table.add(2L, 1L);
        table.add(3L, 1L);
        table.add(3L, 2L);

        Set<Long> parentsOf3 = table.getStatesWithTransitionsTo(3L);
        assertEquals(new HashSet<>(Arrays.asList(1L, 2L)), parentsOf3);

        Set<Long> childrenOf1 = table.getStatesWithTransitionsFrom(1L);
        assertEquals(new HashSet<>(Arrays.asList(2L, 3L)), childrenOf1);
    }

    @Test
    void testAddTransitionsToHiddenStates() {
        State activeState = new State();
        activeState.setId(1L);
        activeState.setHiddenStateIds(new HashSet<>(Arrays.asList(2L, 3L)));

        table.addTransitionsToHiddenStates(activeState);

        Map<Long, Set<Long>> hiddenTransitions = table.getIncomingTransitionsToPREVIOUS();
        assertEquals(new HashSet<>(List.of(1L)), hiddenTransitions.get(2L));
        assertEquals(new HashSet<>(List.of(1L)), hiddenTransitions.get(3L));
    }

    @Test
    void testRemoveTransitionsToHiddenStates() {
        State exitedState = new State();
        exitedState.setId(1L);
        exitedState.setHiddenStateIds(new HashSet<>(Arrays.asList(2L, 3L)));

        table.addTransitionsToHiddenStates(exitedState);
        table.removeTransitionsToHiddenStates(exitedState);

        Map<Long, Set<Long>> hiddenTransitions = table.getIncomingTransitionsToPREVIOUS();
        assertTrue(hiddenTransitions.get(2L).isEmpty());
        assertTrue(hiddenTransitions.get(3L).isEmpty());
    }

    @Test
    void testEmptyRepos() {
        table.add(2L, 1L);
        table.add(3L, 1L);

        State activeState = new State();
        activeState.setId(1L);
        activeState.setHiddenStateIds(new HashSet<>(List.of(4L)));
        table.addTransitionsToHiddenStates(activeState);

        table.emptyRepos();

        assertTrue(table.getIncomingTransitions().isEmpty());
        assertTrue(table.getOutgoingTransitions().isEmpty());
        assertTrue(table.getIncomingTransitionsToPREVIOUS().isEmpty());
    }

    @Test
    void testGetIncomingTransitionsWithHiddenTransitions() {
        table.add(2L, 1L);
        table.add(3L, 1L);

        State activeState = new State();
        activeState.setId(1L);
        activeState.setHiddenStateIds(new HashSet<>(List.of(4L)));
        table.addTransitionsToHiddenStates(activeState);

        Map<Long, Set<Long>> allIncoming = table.getIncomingTransitionsWithHiddenTransitions();

        assertEquals(new HashSet<>(List.of(1L)), allIncoming.get(2L));
        assertEquals(new HashSet<>(List.of(1L)), allIncoming.get(3L));
        assertEquals(new HashSet<>(List.of(1L)), allIncoming.get(4L));
    }
}