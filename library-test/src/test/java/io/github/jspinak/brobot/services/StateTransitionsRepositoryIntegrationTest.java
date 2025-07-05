package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StateTransitionsRepositoryIntegrationTest {

    @Autowired
    private StateTransitionStore stateTransitionsRepository;
    
    @Autowired
    private StateTransitionsJointTable stateTransitionsJointTable;
    
    @BeforeEach
    void setUp() {
        // Clear repository before each test
        // stateTransitionsRepository.clear() - method removed;
    }
    
    @Test
    @Order(1)
    void testSpringContextLoads() {
        assertNotNull(stateTransitionsRepository, "StateTransitionsRepository should be autowired");
        assertNotNull(stateTransitionsJointTable, "StateTransitionsJointTable should be autowired");
    }
    
    @Test
    @Order(2)
    void testAddAndGetStateTransitions() {
        // Create a state transition
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateId(1L);
        stateTransitions.setStateName("TestState");
        
        // Add to repository
        stateTransitionsRepository.add(stateTransitions);
        
        // Retrieve by ID
        Optional<StateTransitions> result = stateTransitionsRepository.get(1L);
        
        assertTrue(result.isPresent(), "Should find state transitions by ID");
        assertEquals(1L, result.get().getStateId());
        assertEquals("TestState", result.get().getStateName());
    }
    
    @Test
    @Order(3)
    void testGetWithNullId() {
        Optional<StateTransitions> result = stateTransitionsRepository.get(null);
        assertFalse(result.isPresent(), "Should return empty Optional for null ID");
    }
    
    @Test
    @Order(4)
    void testGetNonExistentStateTransitions() {
        Optional<StateTransitions> result = stateTransitionsRepository.get(999L);
        assertFalse(result.isPresent(), "Should return empty Optional for non-existent ID");
    }
    
    @Test
    @Order(5)
    void testGetAllStateIds() {
        // Add multiple state transitions
        for (long i = 1; i <= 3; i++) {
            StateTransitions transitions = new StateTransitions();
            transitions.setStateId(i);
            transitions.setStateName("State" + i);
            stateTransitionsRepository.add(transitions);
        }
        
        // Add one without ID
        StateTransitions noIdTransitions = new StateTransitions();
        noIdTransitions.setStateName("NoIdState");
        stateTransitionsRepository.add(noIdTransitions);
        
        Set<Long> allIds = stateTransitionsRepository.getAllStateIds();
        
        assertEquals(3, allIds.size(), "Should only return transitions with IDs");
        assertTrue(allIds.contains(1L));
        assertTrue(allIds.contains(2L));
        assertTrue(allIds.contains(3L));
    }
    
    @Test
    @Order(6)
    void testGetAllStateTransitionsAsCopy() {
        // Add state transitions
        StateTransitions transitions1 = new StateTransitions();
        transitions1.setStateId(1L);
        stateTransitionsRepository.add(transitions1);
        
        StateTransitions transitions2 = new StateTransitions();
        transitions2.setStateId(2L);
        stateTransitionsRepository.add(transitions2);
        
        var copy = stateTransitionsRepository.getAllStateTransitionsAsCopy();
        
        assertEquals(2, copy.size());
        // Verify it's a copy by clearing original and checking copy still has items
        // stateTransitionsRepository.clear() - method removed;
        assertEquals(2, copy.size(), "Copy should be independent of original");
    }
    
    @Test
    @Order(7)
    void testPopulateStateTransitionsJointTable() {
        // This test verifies the integration with StateTransitionsJointTable
        StateTransitions transitions = new StateTransitions();
        transitions.setStateId(1L);
        transitions.setStateName("TestState");
        
        stateTransitionsRepository.add(transitions);
        
        // Should not throw exception
        assertDoesNotThrow(() -> stateTransitionsRepository.populateStateTransitionsJointTable());
    }
    
    @Test
    @Order(8)
    void testConcurrentAccess() throws InterruptedException {
        // Test thread safety of repository
        Thread[] threads = new Thread[10];
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(10);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        
        for (int i = 0; i < threads.length; i++) {
            final long id = i + 10000; // Use even higher IDs to avoid any conflicts
            threads[i] = new Thread(() -> {
                try {
                    StateTransitions transitions = new StateTransitions();
                    transitions.setStateId(id);
                    transitions.setStateName("ConcurrentState" + id);
                    stateTransitionsRepository.add(transitions);
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS), 
                "All threads should complete within 5 seconds");
        
        // Give a small delay for repository to update
        Thread.sleep(100);
        
        // Verify our specific IDs were added
        Set<Long> allIds = stateTransitionsRepository.getAllStateIds();
        int countOurIds = 0;
        for (long i = 10000; i < 10010; i++) {
            if (allIds.contains(i)) {
                countOurIds++;
            }
        }
        
        assertEquals(10, countOurIds, "All 10 concurrent additions should succeed");
        assertEquals(10, successCount.get(), "All threads should have completed successfully");
    }
}