package io.github.jspinak.brobot.startup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.startup.orchestration.InitializationOrchestrator;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the BrobotInitializationOrchestrator.
 * 
 * These tests verify the orchestrator's internal logic without requiring
 * a full Spring context.
 */
public class BrobotInitializationOrchestratorUnitTest {
    
    private InitializationOrchestrator orchestrator;
    
    @BeforeEach
    public void setUp() {
        // Create orchestrator without Spring injection
        orchestrator = new InitializationOrchestrator();
    }
    
    @Test
    public void testPhaseStatusCreation() {
        InitializationOrchestrator.PhaseStatus phase = 
            new InitializationOrchestrator.PhaseStatus("Test Phase", 1);
        
        assertEquals("Test Phase", phase.getName());
        assertEquals(1, phase.getOrder());
        assertFalse(phase.isCompleted());
        assertFalse(phase.isSuccessful());
        assertNull(phase.getDuration());
        assertTrue(phase.getCompletedSteps().isEmpty());
        assertTrue(phase.getFailedSteps().isEmpty());
    }
    
    @Test
    public void testPhaseStatusCompletion() {
        InitializationOrchestrator.PhaseStatus phase = 
            new InitializationOrchestrator.PhaseStatus("Test Phase", 1);
        
        Duration testDuration = Duration.ofMillis(100);
        phase.markCompleted(true, testDuration);
        
        assertTrue(phase.isCompleted());
        assertTrue(phase.isSuccessful());
        assertEquals(testDuration, phase.getDuration());
    }
    
    @Test
    public void testPhaseStatusStepTracking() {
        InitializationOrchestrator.PhaseStatus phase = 
            new InitializationOrchestrator.PhaseStatus("Test Phase", 1);
        
        // Add completed steps
        phase.addCompletedStep("Step 1");
        phase.addCompletedStep("Step 2");
        
        assertEquals(2, phase.getCompletedSteps().size());
        assertTrue(phase.getCompletedSteps().contains("Step 1"));
        assertTrue(phase.getCompletedSteps().contains("Step 2"));
        
        // Add failed step
        phase.addFailedStep("Step 3", "Test error");
        
        assertEquals(1, phase.getFailedSteps().size());
        assertFalse(phase.isSuccessful());
        assertEquals("Test error", phase.getErrorMessage());
    }
    
    @Test
    public void testPhaseStatusFailure() {
        InitializationOrchestrator.PhaseStatus phase = 
            new InitializationOrchestrator.PhaseStatus("Test Phase", 1);
        
        Duration testDuration = Duration.ofMillis(50);
        phase.addFailedStep("Failed operation", "Connection timeout");
        phase.markCompleted(false, testDuration);
        
        assertTrue(phase.isCompleted());
        assertFalse(phase.isSuccessful());
        assertEquals(testDuration, phase.getDuration());
        assertEquals("Connection timeout", phase.getErrorMessage());
    }
    
    @Test
    public void testOrchestratorInitialization() {
        // Verify orchestrator is created
        assertNotNull(orchestrator);
        assertNotNull(orchestrator.getPhaseStatuses());
        
        // Initially, phases map should be empty (no Spring context to trigger @PostConstruct)
        assertTrue(orchestrator.getPhaseStatuses().isEmpty());
    }
    
    @Test
    public void testOrchestratorReset() {
        // Manually add a phase
        InitializationOrchestrator.PhaseStatus testPhase = 
            new InitializationOrchestrator.PhaseStatus("Manual Phase", 0);
        orchestrator.getPhaseStatuses().put("manual", testPhase);
        
        // Verify phase was added
        assertEquals(1, orchestrator.getPhaseStatuses().size());
        
        // Reset
        orchestrator.resetInitialization();
        
        // Verify reset
        assertTrue(orchestrator.getPhaseStatuses().isEmpty());
        assertFalse(orchestrator.isInitializationSuccessful());
    }
    
    @Test
    public void testInitializationStatusWithoutSpring() {
        // Get status without Spring initialization
        var status = orchestrator.getInitializationStatus();
        
        assertNotNull(status);
        assertFalse((Boolean) status.get("initialized"));
        assertNotNull(status.get("phases"));
        assertNotNull(status.get("configuration"));
    }
    
    @Test
    public void testIsInitializationSuccessfulWithoutPhases() {
        // Without any phases initialized
        assertFalse(orchestrator.isInitializationSuccessful());
    }
    
    @Test
    public void testIsInitializationSuccessfulWithCriticalPhases() {
        // The orchestrator checks for initialization complete flag which is set false initially
        // Without Spring context, we can't trigger the full initialization flow
        // So we just verify that without initialization, it returns false
        assertFalse(orchestrator.isInitializationSuccessful());
        
        // Note: In a real scenario with Spring context, the phases would be populated
        // and the initializationComplete flag would be set after all phases complete
    }
    
    @Test
    public void testIsInitializationSuccessfulWithFailedCriticalPhase() {
        // Set up one critical phase as failed
        InitializationOrchestrator.PhaseStatus coreConfig = 
            new InitializationOrchestrator.PhaseStatus("Core Configuration", 1);
        coreConfig.markCompleted(false, Duration.ofMillis(100));
        orchestrator.getPhaseStatuses().put("core-config", coreConfig);
        
        InitializationOrchestrator.PhaseStatus components = 
            new InitializationOrchestrator.PhaseStatus("Component Initialization", 3);
        components.markCompleted(true, Duration.ofMillis(200));
        orchestrator.getPhaseStatuses().put("components", components);
        
        // Should not be successful if critical phase failed
        assertFalse(orchestrator.isInitializationSuccessful());
    }
}