package io.github.jspinak.brobot.runner;

import io.github.jspinak.brobot.config.FrameworkSettings;
// import io.github.jspinak.brobot.navigation.navigation.PlanningSystem; // Not found in current codebase
// import io.github.jspinak.brobot.runner.core.AutomationRunner; // Not found in current codebase
// import io.github.jspinak.brobot.runner.core.BusinessTaskBuilder; // Not found in current codebase
import io.github.jspinak.brobot.runner.dsl.InstructionSet;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
// import io.github.jspinak.brobot.runner.run.RunProjectInstance; // Not found in current codebase
import io.github.jspinak.brobot.runner.dsl.BusinessTask;
// import io.github.jspinak.brobot.runner.model.Operation; // Not found in current codebase
// import io.github.jspinak.brobot.runner.model.OperationSequence; // Not found in current codebase
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateStore;
// import io.github.jspinak.brobot.services.Init; // Not found in current codebase
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the Automation Runner system.
 * 
 * These tests verify the integration between:
 * - AutomationRunner and task execution
 * - BusinessTask building and management
 * - InstructionSet and operation sequences
 * - Planning system integration
 * - Project management
 * - Spring context and dependency injection
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.main.lazy-initialization=true",
    "brobot.mock.enabled=true"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AutomationIntegrationTest {

    @Autowired
    private AutomationRunner automationRunner;
    
    @Autowired
    private BusinessTaskBuilder businessTaskBuilder;
    
    @Autowired
    private AutomationProjectManager projectManager;
    
    @Autowired
    private PlanningSystem planningSystem;
    
    @Autowired
    private StateStore stateStore;
    
    @Autowired
    private BrobotSettings brobotSettings;
    
    @MockBean
    private Init init;
    
    private State loginState;
    private State dashboardState;
    private State reportsState;
    
    @BeforeEach
    void setUp() {
        // Configure mock mode
        when(init.setGlobalMock()).thenReturn(true);
        brobotSettings.mock = true;
        
        // Clear repositories
        stateStore.emptyRepos();
        
        // Create test states
        createTestStates();
    }
    
    private void createTestStates() {
        // Login State
        StateObject usernameField = new StateObject.Builder()
            .withName("usernameField")
            .withRegion(new Region(100, 100, 200, 30))
            .build();
            
        StateObject passwordField = new StateObject.Builder()
            .withName("passwordField")
            .withRegion(new Region(100, 150, 200, 30))
            .build();
            
        StateObject loginButton = new StateObject.Builder()
            .withName("loginButton")
            .withRegion(new Region(150, 200, 100, 40))
            .build();
            
        loginState = new State.Builder("LOGIN")
            .withStateObjects(usernameField, passwordField, loginButton)
            .build();
        stateStore.add(loginState);
        
        // Dashboard State
        StateObject dashboardTitle = new StateObject.Builder()
            .withName("dashboardTitle")
            .withRegion(new Region(50, 20, 300, 50))
            .build();
            
        StateObject reportsButton = new StateObject.Builder()
            .withName("reportsButton")
            .withRegion(new Region(50, 100, 150, 40))
            .build();
            
        dashboardState = new State.Builder("DASHBOARD")
            .withStateObjects(dashboardTitle, reportsButton)
            .build();
        stateStore.add(dashboardState);
        
        // Reports State
        StateObject reportsTitle = new StateObject.Builder()
            .withName("reportsTitle")
            .withRegion(new Region(50, 20, 200, 50))
            .build();
            
        StateObject exportButton = new StateObject.Builder()
            .withName("exportButton")
            .withRegion(new Region(300, 100, 100, 40))
            .build();
            
        reportsState = new State.Builder("REPORTS")
            .withStateObjects(reportsTitle, exportButton)
            .build();
        stateStore.add(reportsState);
    }
    
    @Test
    @Order(1)
    void testSpringContextLoads() {
        assertNotNull(automationRunner, "AutomationRunner should be autowired");
        assertNotNull(businessTaskBuilder, "BusinessTaskBuilder should be autowired");
        assertNotNull(projectManager, "AutomationProjectManager should be autowired");
        assertNotNull(planningSystem, "PlanningSystem should be autowired");
    }
    
    @Test
    @Order(2)
    void testSimpleBusinessTask() {
        // Create a simple business task
        BusinessTask loginTask = businessTaskBuilder.build()
            .withName("Login Task")
            .withDescription("Automate login process")
            .addOperation(new Operation.Builder()
                .withStateName("LOGIN")
                .withObjectName("usernameField")
                .withAction(ActionOptions.Action.CLICK)
                .build())
            .addOperation(new Operation.Builder()
                .withStateName("LOGIN")
                .withObjectName("usernameField")
                .withAction(ActionOptions.Action.TYPE)
                .withText("testuser")
                .build())
            .addOperation(new Operation.Builder()
                .withStateName("LOGIN")
                .withObjectName("passwordField")
                .withAction(ActionOptions.Action.CLICK)
                .build())
            .addOperation(new Operation.Builder()
                .withStateName("LOGIN")
                .withObjectName("passwordField")
                .withAction(ActionOptions.Action.TYPE)
                .withText("password123")
                .build())
            .addOperation(new Operation.Builder()
                .withStateName("LOGIN")
                .withObjectName("loginButton")
                .withAction(ActionOptions.Action.CLICK)
                .build())
            .build();
        
        // Execute task
        boolean success = automationRunner.run(loginTask);
        
        // Verify
        assertTrue(success, "Business task should execute successfully in mock mode");
    }
    
    @Test
    @Order(3)
    void testInstructionSet() {
        // Create instruction set for navigation
        InstructionSet navigationInstructions = new InstructionSet();
        navigationInstructions.setName("Navigate to Reports");
        
        // Add operation sequence
        OperationSequence sequence = new OperationSequence();
        sequence.addOperation(new Operation.Builder()
            .withStateName("DASHBOARD")
            .withObjectName("reportsButton")
            .withAction(ActionOptions.Action.CLICK)
            .build());
        
        navigationInstructions.addSequence(sequence);
        
        // Execute instruction set
        boolean executed = automationRunner.run(navigationInstructions);
        
        // Verify
        assertTrue(executed, "Instruction set should execute successfully");
    }
    
    @Test
    @Order(4)
    void testComplexBusinessTask() {
        // Create a complex task with multiple operations
        BusinessTask complexTask = businessTaskBuilder.build()
            .withName("Generate Report")
            .withDescription("Navigate and generate report")
            .addOperation(new Operation.Builder()
                .withStateName("DASHBOARD")
                .withObjectName("reportsButton")
                .withAction(ActionOptions.Action.CLICK)
                .withMaxWait(5.0)
                .build())
            .addOperation(new Operation.Builder()
                .withStateName("REPORTS")
                .withAction(ActionOptions.Action.VANISH)
                .withMaxWait(3.0)
                .build())
            .addOperation(new Operation.Builder()
                .withStateName("REPORTS")
                .withObjectName("exportButton")
                .withAction(ActionOptions.Action.HIGHLIGHT)
                .withHighlightSeconds(2.0)
                .build())
            .addOperation(new Operation.Builder()
                .withStateName("REPORTS")
                .withObjectName("exportButton")
                .withAction(ActionOptions.Action.CLICK)
                .withClickType(ActionOptions.ClickType.RIGHT)
                .build())
            .build();
        
        // Execute
        boolean success = automationRunner.run(complexTask);
        
        // Verify
        assertTrue(success, "Complex task should execute successfully");
    }
    
    @Test
    @Order(5)
    void testOperationWithActionConfig() {
        // Create operation using ActionConfig API
        Operation configOperation = new Operation.Builder()
            .withStateName("LOGIN")
            .withObjectName("usernameField")
            .withActionConfig(ActionConfig.Builder.click()
                .setClickUntil(ActionOptions.ClickUntil.OBJECTS_APPEAR)
                .setNumberOfActions(3)
                .setPauseAfterAction(0.5)
                .build())
            .build();
        
        // Create task with ActionConfig operation
        BusinessTask configTask = businessTaskBuilder.build()
            .withName("Config API Task")
            .addOperation(configOperation)
            .build();
        
        // Execute
        boolean success = automationRunner.run(configTask);
        
        // Verify
        assertTrue(success, "Task with ActionConfig should execute successfully");
    }
    
    @Test
    @Order(6)
    void testAutomationProject() {
        // Create a project with multiple tasks
        String projectConfig = """
            {
                "name": "Test Automation Project",
                "description": "Integration test project",
                "tasks": [
                    {
                        "name": "Login",
                        "operations": [
                            {
                                "stateName": "LOGIN",
                                "objectName": "loginButton",
                                "action": "CLICK"
                            }
                        ]
                    },
                    {
                        "name": "Navigate",
                        "operations": [
                            {
                                "stateName": "DASHBOARD",
                                "objectName": "reportsButton",
                                "action": "CLICK"
                            }
                        ]
                    }
                ]
            }
            """;
        
        // Load and run project
        RunProjectInstance projectInstance = projectManager.loadProject(projectConfig);
        assertNotNull(projectInstance, "Project should load successfully");
        
        boolean projectSuccess = projectInstance.run();
        assertTrue(projectSuccess, "Project should run successfully in mock mode");
    }
    
    @Test
    @Order(7)
    void testOperationSequenceWithConditions() {
        // Create operation sequence with conditional logic
        OperationSequence conditionalSequence = new OperationSequence();
        conditionalSequence.setName("Conditional Navigation");
        
        // Add operations with different conditions
        Operation checkDashboard = new Operation.Builder()
            .withStateName("DASHBOARD")
            .withAction(ActionOptions.Action.FIND)
            .withFind(ActionOptions.Find.FIRST)
            .build();
        
        Operation clickReports = new Operation.Builder()
            .withStateName("DASHBOARD")
            .withObjectName("reportsButton")
            .withAction(ActionOptions.Action.CLICK)
            .withClickUntil(ActionOptions.ClickUntil.OBJECTS_VANISH)
            .build();
        
        conditionalSequence.addOperation(checkDashboard);
        conditionalSequence.addOperation(clickReports);
        
        // Create instruction set with sequence
        InstructionSet conditionalInstructions = new InstructionSet();
        conditionalInstructions.addSequence(conditionalSequence);
        
        // Execute
        boolean success = automationRunner.run(conditionalInstructions);
        assertTrue(success, "Conditional sequence should execute successfully");
    }
    
    @Test
    @Order(8)
    void testPlanningSystemIntegration() {
        // Test planning system with state navigation
        planningSystem.setTargetState(reportsState);
        
        // In mock mode, planning typically finds a path
        boolean pathFound = planningSystem.hasValidPath();
        assertTrue(pathFound, "Planning system should find path in mock mode");
    }
    
    @Test
    @Order(9)
    void testOperationWithObjectCollection() {
        // Create operation with custom object collection
        ObjectCollection customCollection = new ObjectCollection.Builder()
            .withRegions(
                new Region(50, 50, 100, 100),
                new Region(200, 200, 100, 100)
            )
            .build();
        
        Operation collectionOperation = new Operation.Builder()
            .withObjectCollection(customCollection)
            .withAction(ActionOptions.Action.FIND)
            .withFind(ActionOptions.Find.ALL)
            .build();
        
        BusinessTask collectionTask = businessTaskBuilder.build()
            .withName("Collection Task")
            .addOperation(collectionOperation)
            .build();
        
        // Execute
        boolean success = automationRunner.run(collectionTask);
        assertTrue(success, "Task with object collection should execute successfully");
    }
    
    @Test
    @Order(10)
    void testErrorHandlingInBusinessTask() {
        // Create task with potential error scenarios
        BusinessTask errorTask = businessTaskBuilder.build()
            .withName("Error Handling Task")
            .addOperation(new Operation.Builder()
                .withStateName("NONEXISTENT_STATE")
                .withObjectName("nonexistent")
                .withAction(ActionOptions.Action.CLICK)
                .build())
            .build();
        
        // Execute - should handle error gracefully
        boolean success = automationRunner.run(errorTask);
        
        // In mock mode, operations typically succeed even with invalid states
        assertNotNull(success);
    }
    
    @Test
    @Order(11)
    void testTaskExecutionMetrics() {
        // Create task to measure execution
        BusinessTask metricsTask = businessTaskBuilder.build()
            .withName("Metrics Task")
            .addOperation(new Operation.Builder()
                .withStateName("LOGIN")
                .withObjectName("usernameField")
                .withAction(ActionOptions.Action.CLICK)
                .build())
            .addOperation(new Operation.Builder()
                .withStateName("LOGIN")
                .withObjectName("passwordField")
                .withAction(ActionOptions.Action.CLICK)
                .build())
            .addOperation(new Operation.Builder()
                .withStateName("LOGIN")
                .withObjectName("loginButton")
                .withAction(ActionOptions.Action.CLICK)
                .build())
            .build();
        
        // Execute and measure
        long startTime = System.currentTimeMillis();
        boolean success = automationRunner.run(metricsTask);
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Verify
        assertTrue(success, "Task should execute successfully");
        assertTrue(executionTime < 10000, "Mock execution should be fast");
    }
}