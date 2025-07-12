package io.github.jspinak.brobot.runner.ui.panels;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.RunnerInterface;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.ui.AutomationWindowController;
import io.github.jspinak.brobot.runner.ui.managers.LabelManager;
import io.github.jspinak.brobot.runner.ui.managers.UIUpdateManager;
import io.github.jspinak.brobot.runner.ui.registry.UIComponentRegistry;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for RefactoredAutomationPanel demonstrating the new architecture.
 */
@ExtendWith(ApplicationExtension.class)
class RefactoredAutomationPanelTest {
    
    @Mock private ApplicationContext context;
    @Mock private AutomationProjectManager projectManager;
    @Mock private BrobotRunnerProperties runnerProperties;
    @Mock private AutomationOrchestrator automationOrchestrator;
    @Mock private EventBus eventBus;
    @Mock private HotkeyManager hotkeyManager;
    @Mock private io.github.jspinak.brobot.runner.project.ProjectDefinition projectDefinition;
    @Mock private AutomationWindowController windowController;
    
    private UIComponentRegistry componentRegistry;
    private UIUpdateManager updateManager;
    private LabelManager labelManager;
    
    private RefactoredAutomationPanel panel;
    private Stage stage;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create real instances of managers
        componentRegistry = new UIComponentRegistry();
        updateManager = new UIUpdateManager();
        labelManager = new LabelManager();
    }
    
    @Start
    void start(Stage stage) {
        this.stage = stage;
        
        // Create panel with manual injection since we're not in Spring context
        panel = new RefactoredAutomationPanel() {
            {
                // Use instance initializer to set protected fields
                this.componentRegistry = componentRegistry;
                this.updateManager = updateManager;
                this.labelManager = labelManager;
            }
        };
        
        // Inject mocks via reflection or setter methods if available
        injectDependencies();
        
        Scene scene = new Scene(panel, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    private void injectDependencies() {
        try {
            // Use reflection to inject private fields
            var contextField = RefactoredAutomationPanel.class.getDeclaredField("context");
            contextField.setAccessible(true);
            contextField.set(panel, context);
            
            var projectManagerField = RefactoredAutomationPanel.class.getDeclaredField("projectManager");
            projectManagerField.setAccessible(true);
            projectManagerField.set(panel, projectManager);
            
            var automationOrchestratorField = RefactoredAutomationPanel.class.getDeclaredField("automationOrchestrator");
            automationOrchestratorField.setAccessible(true);
            automationOrchestratorField.set(panel, automationOrchestrator);
            
            var eventBusField = RefactoredAutomationPanel.class.getDeclaredField("eventBus");
            eventBusField.setAccessible(true);
            eventBusField.set(panel, eventBus);
            
            var hotkeyManagerField = RefactoredAutomationPanel.class.getDeclaredField("hotkeyManager");
            hotkeyManagerField.setAccessible(true);
            hotkeyManagerField.set(panel, hotkeyManager);
            
            var windowControllerField = RefactoredAutomationPanel.class.getDeclaredField("windowController");
            windowControllerField.setAccessible(true);
            windowControllerField.set(panel, windowController);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }
    
    @Test
    void testInitialization() throws Exception {
        runAndWait(() -> {
            panel.initialize();
            
            assertTrue(panel.isInitialized());
            assertTrue(componentRegistry.isRegistered(panel.getComponentId()));
            
            // Check that title label was created
            assertTrue(labelManager.hasLabel("automation_title"));
        });
    }
    
    @Test
    void testRefreshWithNoProject() throws Exception {
        when(projectManager.getActiveProject()).thenReturn(null);
        
        runAndWait(() -> {
            panel.initialize();
            panel.refresh();
            
            // Should clear buttons when no project
            FlowPane buttonPane = findButtonPane();
            assertEquals(0, buttonPane.getChildren().size());
        });
    }
    
    @Test
    void testRefreshWithProject() throws Exception {
        // Setup mock project with buttons
        AutomationProject project = mock(AutomationProject.class);
        RunnerInterface config = mock(RunnerInterface.class);
        
        TaskButton button1 = new TaskButton();
        button1.setId("btn1");
        button1.setLabel("Test Button 1");
        button1.setCategory("Category A");
        
        TaskButton button2 = new TaskButton();
        button2.setId("btn2");
        button2.setLabel("Test Button 2");
        button2.setCategory("Category A");
        
        TaskButton button3 = new TaskButton();
        button3.setId("btn3");
        button3.setLabel("Test Button 3");
        button3.setCategory("Category B");
        
        List<TaskButton> buttons = Arrays.asList(button1, button2, button3);
        
        when(projectManager.getActiveProject()).thenReturn(projectDefinition);
        when(projectManager.getActiveAutomationProject()).thenReturn(project);
        when(project.getAutomation()).thenReturn(config);
        when(config.getButtons()).thenReturn(buttons);
        
        runAndWait(() -> {
            panel.initialize();
            panel.refresh();
            
            FlowPane buttonPane = findButtonPane();
            
            // Should have 2 category boxes
            assertEquals(2, buttonPane.getChildren().size());
            
            // Check labels were created for categories
            assertTrue(labelManager.hasLabel("category_" + "Category A".hashCode()));
            assertTrue(labelManager.hasLabel("category_" + "Category B".hashCode()));
        });
    }
    
    @Test
    void testNoDuplicateLabelsOnMultipleRefresh() throws Exception {
        // Setup mock project
        AutomationProject project = mock(AutomationProject.class);
        RunnerInterface config = mock(RunnerInterface.class);
        
        TaskButton button = new TaskButton();
        button.setId("btn1");
        button.setLabel("Test Button");
        button.setCategory("Test Category");
        
        when(projectManager.getActiveProject()).thenReturn(projectDefinition);
        when(projectManager.getActiveAutomationProject()).thenReturn(project);
        when(project.getAutomation()).thenReturn(config);
        when(config.getButtons()).thenReturn(Arrays.asList(button));
        
        runAndWait(() -> {
            panel.initialize();
            
            // Refresh multiple times
            for (int i = 0; i < 5; i++) {
                panel.refresh();
            }
            
            // Should still have only one category label
            String categoryLabelId = "category_" + "Test Category".hashCode();
            assertTrue(labelManager.hasLabel(categoryLabelId));
            
            // Check that there's only one instance in the UI
            FlowPane buttonPane = findButtonPane();
            long categoryLabelCount = buttonPane.lookupAll(".category-label").stream()
                .filter(node -> node instanceof Label)
                .filter(node -> ((Label) node).getText().equals("Test Category"))
                .count();
            
            assertEquals(1, categoryLabelCount);
        });
    }
    
    @Test
    void testStatusUpdates() throws Exception {
        ExecutionStatus status = new ExecutionStatus();
        status.setState(ExecutionState.RUNNING);
        status.setProgress(0.5);
        
        when(automationOrchestrator.getExecutionStatus()).thenReturn(status);
        
        runAndWait(() -> {
            panel.initialize();
        });
        
        // Wait for status update
        Thread.sleep(1500);
        
        runAndWait(() -> {
            // Check that status label was updated
            assertTrue(labelManager.hasLabel("status_label"));
            
            // Since LabelManager doesn't have a get method, we'll check via UI lookup
            Label statusLabel = (Label) panel.lookup("#status_label");
            assertNotNull(statusLabel);
            assertTrue(statusLabel.getText().contains("Running"));
            
            // Check progress label
            assertTrue(labelManager.hasLabel("progress_label"));
            Label progressLabel = (Label) panel.lookup("#progress_label");
            assertNotNull(progressLabel);
            assertEquals("50%", progressLabel.getText());
        });
    }
    
    @Test
    void testCleanup() throws Exception {
        runAndWait(() -> {
            panel.initialize();
            assertTrue(componentRegistry.isRegistered(panel.getComponentId()));
            
            panel.cleanup();
            
            assertFalse(panel.isInitialized());
            assertFalse(componentRegistry.isRegistered(panel.getComponentId()));
        });
    }
    
    @Test
    void testHotkeyRegistration() throws Exception {
        runAndWait(() -> {
            panel.initialize();
        });
        
        // Verify hotkeys were registered
        verify(hotkeyManager).registerAction(eq(HotkeyManager.HotkeyAction.PAUSE), any());
        verify(hotkeyManager).registerAction(eq(HotkeyManager.HotkeyAction.RESUME), any());
        verify(hotkeyManager).registerAction(eq(HotkeyManager.HotkeyAction.STOP), any());
        verify(hotkeyManager).registerAction(eq(HotkeyManager.HotkeyAction.TOGGLE_PAUSE), any());
    }
    
    private FlowPane findButtonPane() {
        return (FlowPane) panel.lookup(".button-pane");
    }
    
    private void runAndWait(Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}