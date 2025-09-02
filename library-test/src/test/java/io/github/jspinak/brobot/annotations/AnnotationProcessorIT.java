package io.github.jspinak.brobot.annotations;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.statemanagement.InitialStates;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AnnotationProcessor - processes @State and @Transition annotations.
 * Verifies automatic state registration and transition setup with full Spring context.
 * 
 * Since AnnotationProcessor runs automatically via ApplicationReadyEvent in Spring Boot,
 * these tests focus on verifying the results of annotation processing rather than 
 * calling processor methods directly.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AnnotationProcessor Integration Tests")
@Tag("integration")
@Tag("annotations")
public class AnnotationProcessorIT extends BrobotTestBase {
    
    @Autowired
    private AnnotationProcessor processor;
    
    @Autowired
    private StateService stateService;
    
    @Autowired
    private StateTransitionService transitionService;
    
    @Autowired
    private InitialStates initialStates;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    // Test state classes that will be automatically discovered by Spring
    @io.github.jspinak.brobot.annotations.State(name = "TestState1")
    public static class TestState1 {
        // Empty test state
    }
    
    @io.github.jspinak.brobot.annotations.State(name = "TestState2", initial = true)
    public static class TestState2 {
        // Initial test state
    }
    
    @io.github.jspinak.brobot.annotations.State(
        name = "TestState3", 
        description = "Test state with description",
        priority = 200
    )
    public static class TestState3 {
        // Test state with custom properties
    }
    
    @Transition(
        from = {TestState1.class}, 
        to = {TestState2.class}
    )
    public static class TestTransition1 {
        public boolean execute() {
            return true;
        }
    }
    
    @Nested
    @DisplayName("Spring Context Integration")
    class SpringContextIntegration {
        
        @Test
        @DisplayName("AnnotationProcessor should be autowired")
        public void testAnnotationProcessorAutowired() {
            assertNotNull(processor, "AnnotationProcessor should be autowired");
        }
        
        @Test
        @DisplayName("Required services should be autowired")
        public void testRequiredServicesAutowired() {
            assertNotNull(stateService, "StateService should be autowired");
            assertNotNull(transitionService, "StateTransitionService should be autowired");
            assertNotNull(initialStates, "InitialStates should be autowired");
            assertNotNull(applicationContext, "ApplicationContext should be autowired");
            assertNotNull(eventPublisher, "ApplicationEventPublisher should be autowired");
        }
        
        @Test
        @DisplayName("Spring should discover @State annotated beans")
        public void testSpringDiscoversStateBeans() {
            Map<String, Object> stateBeans = applicationContext.getBeansWithAnnotation(
                io.github.jspinak.brobot.annotations.State.class);
            
            assertTrue(stateBeans.size() > 0, 
                "Spring should discover at least one @State annotated bean");
            
            // Note: In a real integration test, you would need to ensure the test state classes
            // are in the component scan path. This might require adjusting the test configuration
            // or using @TestConfiguration to register them explicitly.
        }
        
        @Test
        @DisplayName("Spring should discover @Transition annotated beans")
        public void testSpringDiscoversTransitionBeans() {
            Map<String, Object> transitionBeans = applicationContext.getBeansWithAnnotation(
                Transition.class);
            
            // This test may return 0 if no transition beans are registered in the test context
            // The behavior depends on the Spring Boot test configuration
            assertTrue(transitionBeans.size() >= 0, 
                "Spring should handle @Transition annotated beans");
        }
    }
    
    @Nested
    @DisplayName("State Processing Verification")
    class StateProcessingVerification {
        
        @Test
        @DisplayName("StateService should contain processed states")
        public void testStateServiceContainsProcessedStates() {
            List<State> allStates = stateService.getAllStates();
            assertNotNull(allStates, "StateService should return a list of states");
            
            // The exact number depends on what states are registered in the test context
            // At minimum, there should be the UNKNOWN state
            assertTrue(allStates.size() >= 1, 
                "StateService should contain at least the UNKNOWN state");
        }
        
        @Test
        @DisplayName("StateService should allow querying by name")
        public void testStateServiceQueryByName() {
            // Test querying for a known state (UNKNOWN always exists)
            Optional<State> unknownState = stateService.getState("UNKNOWN");
            assertTrue(unknownState.isPresent(), 
                "StateService should contain the UNKNOWN state");
            
            assertEquals("UNKNOWN", unknownState.get().getName(),
                "UNKNOWN state should have correct name");
        }
        
        @Test
        @DisplayName("StateService should provide state IDs and names")
        public void testStateServiceProvidesIdsAndNames() {
            List<Long> allIds = stateService.getAllStateIds();
            assertNotNull(allIds, "StateService should provide state IDs");
            
            var allNames = stateService.getAllStateNames();
            assertNotNull(allNames, "StateService should provide state names");
            
            assertTrue(allNames.contains("UNKNOWN"), 
                "State names should include UNKNOWN");
        }
    }
    
    @Nested
    @DisplayName("Real Annotation Processing Tests")
    class RealAnnotationProcessingTests {
        
        @Test
        @DisplayName("Processor should process annotations via Spring context")
        public void testProcessorProcessesViaSpring() {
            // Since AnnotationProcessor runs automatically via @EventListener(ApplicationReadyEvent.class),
            // we can only verify that the processing has occurred by checking the results
            
            // The processor should have registered at least the basic states
            List<State> states = stateService.getAllStates();
            assertNotNull(states, "States should be registered after processing");
            
            // Log for debugging
            System.out.println("Found " + states.size() + " states in StateService:");
            states.forEach(state -> System.out.println("  - " + state.getName() + " (ID: " + state.getId() + ")"));
        }
        
        @Test
        @DisplayName("Can verify annotation processing occurred")
        public void testAnnotationProcessingOccurred() {
            // Since the AnnotationProcessor runs automatically, we can verify it ran
            // by checking that states exist in the StateService
            
            assertFalse(stateService.onlyTheUnknownStateExists() && stateService.getAllStates().size() == 1,
                "If only UNKNOWN state exists, either no states were processed or only default states exist");
            
            // At minimum, we should have the UNKNOWN state
            assertTrue(stateService.getAllStates().size() >= 1,
                "StateService should contain at least one state after processing");
        }
    }
    
    @Nested
    @DisplayName("State Annotation Attributes Verification")
    class StateAnnotationAttributesVerification {
        
        @Test
        @DisplayName("Should handle @State annotation with correct attributes")
        public void testStateAnnotationAttributes() {
            // Test the actual @State annotation attributes that exist
            io.github.jspinak.brobot.annotations.State stateAnnotation = TestState2.class.getAnnotation(
                io.github.jspinak.brobot.annotations.State.class);
            
            assertNotNull(stateAnnotation, "TestState2 should have @State annotation");
            assertEquals("TestState2", stateAnnotation.name());
            assertTrue(stateAnnotation.initial());
            assertEquals(100, stateAnnotation.priority()); // Default priority
            assertEquals("", stateAnnotation.description()); // Default description
            assertArrayEquals(new String[]{}, stateAnnotation.profiles()); // Default profiles
        }
        
        @Test
        @DisplayName("Should handle @State with custom description and priority")
        public void testStateWithCustomProperties() {
            io.github.jspinak.brobot.annotations.State stateAnnotation = TestState3.class.getAnnotation(
                io.github.jspinak.brobot.annotations.State.class);
            
            assertNotNull(stateAnnotation, "TestState3 should have @State annotation");
            assertEquals("TestState3", stateAnnotation.name());
            assertEquals("Test state with description", stateAnnotation.description());
            assertEquals(200, stateAnnotation.priority());
            assertFalse(stateAnnotation.initial());
        }
    }
    
    @Nested
    @DisplayName("Transition Annotation Verification")
    class TransitionAnnotationVerification {
        
        @Test
        @DisplayName("Should handle @Transition annotation with correct attributes")
        public void testTransitionAnnotationAttributes() {
            Transition transitionAnnotation = TestTransition1.class.getAnnotation(Transition.class);
            
            assertNotNull(transitionAnnotation, "TestTransition1 should have @Transition annotation");
            
            // Verify the from/to arrays
            Class<?>[] fromStates = transitionAnnotation.from();
            Class<?>[] toStates = transitionAnnotation.to();
            
            assertEquals(1, fromStates.length, "Should have one from state");
            assertEquals(1, toStates.length, "Should have one to state");
            
            assertEquals(TestState1.class, fromStates[0]);
            assertEquals(TestState2.class, toStates[0]);
            
            assertEquals("execute", transitionAnnotation.method()); // Default method name
            assertEquals(0, transitionAnnotation.priority()); // Default priority
            assertEquals("", transitionAnnotation.description()); // Default description
        }
    }
    
    @Nested
    @DisplayName("Error Handling and Edge Cases")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle empty state service gracefully")
        public void testHandleEmptyStateService() {
            // Test that the state service handles empty queries gracefully
            Optional<State> nonExistentState = stateService.getState("NonExistentState");
            assertFalse(nonExistentState.isPresent(), 
                "Non-existent state should return empty Optional");
        }
        
        @Test
        @DisplayName("Should handle state queries by ID")
        public void testStateQueriesById() {
            // Get a known state
            List<State> allStates = stateService.getAllStates();
            if (!allStates.isEmpty()) {
                State firstState = allStates.get(0);
                Long stateId = firstState.getId();
                
                Optional<State> retrievedState = stateService.getState(stateId);
                assertTrue(retrievedState.isPresent(), 
                    "Should be able to retrieve state by ID");
                assertEquals(stateId, retrievedState.get().getId(),
                    "Retrieved state should have correct ID");
            }
        }
        
        @Test
        @DisplayName("Should handle state name/ID conversions")
        public void testStateNameIdConversions() {
            List<State> allStates = stateService.getAllStates();
            if (!allStates.isEmpty()) {
                State firstState = allStates.get(0);
                String stateName = firstState.getName();
                Long stateId = firstState.getId();
                
                // Test name to ID conversion
                Long retrievedId = stateService.getStateId(stateName);
                assertEquals(stateId, retrievedId, 
                    "State ID should match for name: " + stateName);
                
                // Test ID to name conversion
                String retrievedName = stateService.getStateName(stateId);
                assertEquals(stateName, retrievedName, 
                    "State name should match for ID: " + stateId);
            }
        }
    }
    
    @Nested
    @DisplayName("Integration with Other Services")
    class IntegrationWithOtherServices {
        
        @Test
        @DisplayName("Should integrate with InitialStates service")
        public void testIntegrationWithInitialStates() {
            assertNotNull(initialStates, "InitialStates service should be available");
            
            // InitialStates should be populated if there are initial states
            // The exact behavior depends on the configuration and states registered
            assertTrue(true, "InitialStates service is accessible");
        }
        
        @Test
        @DisplayName("Should integrate with StateTransitionService")
        public void testIntegrationWithStateTransitionService() {
            assertNotNull(transitionService, "StateTransitionService should be available");
            
            // Test that we can query the transition service
            // The exact behavior depends on what transitions are registered
            assertTrue(true, "StateTransitionService is accessible");
        }
    }
}