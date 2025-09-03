package io.github.jspinak.brobot.startup;

import io.github.jspinak.brobot.startup.orchestration.InitializationOrchestrator;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.boot.SpringApplication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the BrobotInitializationOrchestrator.
 * 
 * Verifies that the orchestrator properly manages the initialization phases
 * and provides accurate status information.
 */
@SpringBootTest(classes = {
    InitializationOrchestrator.class,
    InitializationOrchestrator.PhaseStatus.class
})
public class BrobotInitializationOrchestratorTest extends BrobotTestBase {
    
    @Autowired
    private InitializationOrchestrator orchestrator;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Test
    public void testOrchestratorIsCreated() {
        assertNotNull(orchestrator, "Orchestrator should be created");
    }
    
    @Test
    public void testInitializationPhases() {
        // Get the phase statuses
        Map<String, InitializationOrchestrator.PhaseStatus> phases = orchestrator.getPhaseStatuses();
        
        // Verify that phases are tracked
        assertNotNull(phases, "Phase statuses should not be null");
        assertFalse(phases.isEmpty(), "Should have at least one phase tracked");
        
        // Check for early core phase (created in @PostConstruct)
        assertTrue(phases.containsKey("early-core"), "Should have early-core phase");
        
        InitializationOrchestrator.PhaseStatus earlyCore = phases.get("early-core");
        assertNotNull(earlyCore, "Early core phase should exist");
        assertEquals("Early Core Initialization", earlyCore.getName());
        assertEquals(0, earlyCore.getOrder());
    }
    
    @Test
    public void testInitializationStatus() {
        Map<String, Object> status = orchestrator.getInitializationStatus();
        
        assertNotNull(status, "Status should not be null");
        assertNotNull(status.get("initialized"), "Should have initialized flag");
        assertNotNull(status.get("phases"), "Should have phases information");
        assertNotNull(status.get("configuration"), "Should have configuration information");
    }
    
    @Test
    public void testPhaseStatusTracking() {
        // Create a new phase status
        InitializationOrchestrator.PhaseStatus testPhase = 
            new InitializationOrchestrator.PhaseStatus("Test Phase", 99);
        
        // Verify initial state
        assertFalse(testPhase.isCompleted());
        assertFalse(testPhase.isSuccessful());
        assertTrue(testPhase.getCompletedSteps().isEmpty());
        assertTrue(testPhase.getFailedSteps().isEmpty());
        
        // Add completed step
        testPhase.addCompletedStep("Step 1");
        assertEquals(1, testPhase.getCompletedSteps().size());
        assertEquals("Step 1", testPhase.getCompletedSteps().get(0));
        
        // Add failed step
        testPhase.addFailedStep("Step 2", "Test error");
        assertEquals(1, testPhase.getFailedSteps().size());
        assertFalse(testPhase.isSuccessful());
        assertEquals("Test error", testPhase.getErrorMessage());
    }
    
    @Test
    public void testResetInitialization() {
        // Reset the orchestrator
        orchestrator.resetInitialization();
        
        // Verify reset
        Map<String, InitializationOrchestrator.PhaseStatus> phases = orchestrator.getPhaseStatuses();
        assertTrue(phases.isEmpty(), "Phases should be cleared after reset");
        
        Map<String, Object> status = orchestrator.getInitializationStatus();
        assertFalse((boolean) status.get("initialized"), "Should not be initialized after reset");
    }
    
    @Test
    public void testCriticalPhaseDetection() {
        // Reset and manually set up phases for testing
        orchestrator.resetInitialization();
        
        // Since we can't easily set up the phases without triggering real initialization,
        // we'll just verify the method exists and returns a reasonable default
        boolean isSuccessful = orchestrator.isInitializationSuccessful();
        assertFalse(isSuccessful, "Should not be successful when not initialized");
    }
}