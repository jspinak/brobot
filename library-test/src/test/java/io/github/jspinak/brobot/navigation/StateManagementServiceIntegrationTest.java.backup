package io.github.jspinak.brobot.navigation;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateIdResolver;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;  
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.model.transition.StateTransition;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StateManagementServiceIntegrationTest {

    @Autowired
    private StateIdResolver stateManagementService;
    
    @MockBean
    private StateService allStatesInProjectService;
    
    @BeforeEach
    void setUp() {
        Mockito.reset(allStatesInProjectService);
    }
    
    @Test
    @Order(1)
    void testSpringContextLoads() {
        assertNotNull(stateManagementService, "StateManagementService should be autowired");
    }
    
    @Test
    @Order(2)
    void testConvertNamesToIds() {
        // Setup mock state ID lookups
        when(allStatesInProjectService.getStateId("FromState")).thenReturn(1L);
        when(allStatesInProjectService.getStateId("ToState1")).thenReturn(2L);
        when(allStatesInProjectService.getStateId("ToState2")).thenReturn(3L);
        
        // Create StateTransitions with JavaStateTransition
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateName("FromState");
        
        JavaStateTransition transition1 = new JavaStateTransition();
        transition1.getActivateNames().add("ToState1");
        transition1.getActivateNames().add("ToState2");
        
        stateTransitions.addTransition(transition1);
        
        // Execute conversion
        stateManagementService.convertNamesToIds(stateTransitions);
        
        // Verify
        assertEquals(1L, stateTransitions.getStateId());
        assertEquals(2, transition1.getActivate().size());
        assertTrue(transition1.getActivate().contains(2L));
        assertTrue(transition1.getActivate().contains(3L));
    }
    
    @Test
    @Order(3)
    void testConvertNamesToIdsWithExistingStateId() {
        // Setup mock
        when(allStatesInProjectService.getStateId("TestState")).thenReturn(5L);
        
        // Create StateTransitions with existing ID
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateName("TestState");
        stateTransitions.setStateId(10L); // Pre-existing ID
        
        // Execute
        stateManagementService.convertNamesToIds(stateTransitions);
        
        // Verify ID is not overwritten
        assertEquals(10L, stateTransitions.getStateId());
    }
    
    @Test
    @Order(4)
    void testConvertNamesToIdsWithNullStateId() {
        // Setup mock to return null
        when(allStatesInProjectService.getStateId("NonExistentState")).thenReturn(null);
        
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateName("NonExistentState");
        
        // Execute
        stateManagementService.convertNamesToIds(stateTransitions);
        
        // Verify
        assertNull(stateTransitions.getStateId());
    }
    
    @Test
    @Order(5)
    void testConvertAllStateTransitions() {
        // Setup mocks
        when(allStatesInProjectService.getStateId("State1")).thenReturn(1L);
        when(allStatesInProjectService.getStateId("State2")).thenReturn(2L);
        when(allStatesInProjectService.getStateId("State3")).thenReturn(3L);
        
        // Create multiple StateTransitions
        StateTransitions transitions1 = new StateTransitions();
        transitions1.setStateName("State1");
        
        StateTransitions transitions2 = new StateTransitions();
        transitions2.setStateName("State2");
        
        JavaStateTransition javaTransition = new JavaStateTransition();
        javaTransition.getActivateNames().add("State3");
        transitions2.addTransition(javaTransition);
        
        List<StateTransitions> allTransitions = Arrays.asList(transitions1, transitions2);
        
        // Execute
        stateManagementService.convertAllStateTransitions(allTransitions);
        
        // Verify
        assertEquals(1L, transitions1.getStateId());
        assertEquals(2L, transitions2.getStateId());
        assertTrue(javaTransition.getActivate().contains(3L));
    }
    
    @Test
    @Order(6)
    void testConvertNamesToIdsWithNonJavaStateTransition() {
        // Create a mock implementation of StateTransition that is not JavaStateTransition
        StateTransition nonJavaTransition = mock(StateTransition.class);
        
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateName("TestState");
        stateTransitions.getTransitions().add(nonJavaTransition);
        
        when(allStatesInProjectService.getStateId("TestState")).thenReturn(1L);
        
        // Execute - should handle non-JavaStateTransition gracefully
        assertDoesNotThrow(() -> stateManagementService.convertNamesToIds(stateTransitions));
        
        // Verify state ID is set but no activation methods are called on non-Java transition
        assertEquals(1L, stateTransitions.getStateId());
    }
    
    @Test
    @Order(7)
    void testConvertNamesToIdsWithEmptyTransitions() {
        when(allStatesInProjectService.getStateId("EmptyState")).thenReturn(1L);
        
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateName("EmptyState");
        // No transitions added
        
        // Execute
        stateManagementService.convertNamesToIds(stateTransitions);
        
        // Verify
        assertEquals(1L, stateTransitions.getStateId());
        assertTrue(stateTransitions.getTransitions().isEmpty());
    }
    
    @Test
    @Order(8)
    void testThreadSafety() throws InterruptedException {
        // Setup mock for concurrent access
        when(allStatesInProjectService.getStateId(anyString())).thenAnswer(invocation -> {
            String stateName = invocation.getArgument(0);
            return Long.parseLong(stateName.replaceAll("[^0-9]", ""));
        });
        
        // Create multiple threads converting different StateTransitions
        Thread[] threads = new Thread[10];
        StateTransitions[] transitionsArray = new StateTransitions[10];
        
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            transitionsArray[i] = new StateTransitions();
            transitionsArray[i].setStateName("State" + index);
            
            threads[i] = new Thread(() -> {
                stateManagementService.convertNamesToIds(transitionsArray[index]);
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for completion
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Verify all conversions succeeded
        for (int i = 0; i < transitionsArray.length; i++) {
            assertEquals((long) i, transitionsArray[i].getStateId());
        }
    }
}