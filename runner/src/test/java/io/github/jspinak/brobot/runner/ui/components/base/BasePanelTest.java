package io.github.jspinak.brobot.runner.ui.components.base;

import io.github.jspinak.brobot.runner.ui.managers.LabelManager;
import io.github.jspinak.brobot.runner.ui.managers.UIUpdateManager;
import io.github.jspinak.brobot.runner.ui.registry.UIComponentRegistry;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BasePanel abstract class functionality.
 */
@ExtendWith(ApplicationExtension.class)
class BasePanelTest {
    
    private UIComponentRegistry componentRegistry;
    private UIUpdateManager updateManager;
    private LabelManager labelManager;
    private TestPanel testPanel;
    private Stage stage;
    
    @BeforeEach
    void setUp() {
        componentRegistry = new UIComponentRegistry();
        updateManager = new UIUpdateManager();
        labelManager = new LabelManager();
    }
    
    @Start
    void start(Stage stage) {
        this.stage = stage;
        testPanel = new TestPanel();
        
        // Inject dependencies
        testPanel.componentRegistry = componentRegistry;
        testPanel.updateManager = updateManager;
        testPanel.labelManager = labelManager;
        
        Scene scene = new Scene(testPanel, 400, 300);
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    void testInitialization() throws Exception {
        runAndWait(() -> {
            assertFalse(testPanel.isInitialized());
            assertFalse(testPanel.initializeCalled.get());
            
            testPanel.initialize();
            
            assertTrue(testPanel.isInitialized());
            assertTrue(testPanel.initializeCalled.get());
            assertTrue(componentRegistry.isRegistered(testPanel.getComponentId()));
        });
    }
    
    @Test
    void testDoubleInitialization() throws Exception {
        runAndWait(() -> {
            testPanel.initialize();
            testPanel.initializeCount.set(0);
            
            // Second initialization should be ignored
            testPanel.initialize();
            
            assertEquals(0, testPanel.initializeCount.get());
        });
    }
    
    @Test
    void testRefresh() throws Exception {
        runAndWait(() -> {
            // Refresh before initialization should be ignored
            testPanel.refresh();
            assertEquals(0, testPanel.refreshCount.get());
            
            testPanel.initialize();
            testPanel.refresh();
            
            assertEquals(1, testPanel.refreshCount.get());
        });
    }
    
    @Test
    void testCleanup() throws Exception {
        runAndWait(() -> {
            testPanel.initialize();
            
            // Schedule a periodic update
            testPanel.schedulePeriodicUpdate(() -> {}, 1);
            
            assertTrue(updateManager.isTaskScheduled(testPanel.getComponentId()));
            assertTrue(componentRegistry.isRegistered(testPanel.getComponentId()));
            
            testPanel.cleanup();
            
            assertFalse(testPanel.isInitialized());
            assertFalse(updateManager.isTaskScheduled(testPanel.getComponentId()));
            assertFalse(componentRegistry.isRegistered(testPanel.getComponentId()));
            assertTrue(testPanel.cleanupCalled.get());
        });
    }
    
    @Test
    void testIsValid() throws Exception {
        runAndWait(() -> {
            assertFalse(testPanel.isValid());
            
            testPanel.initialize();
            assertTrue(testPanel.isValid());
            
            testPanel.setVisible(false);
            assertFalse(testPanel.isValid());
            
            testPanel.setVisible(true);
            assertTrue(testPanel.isValid());
        });
    }
    
    @Test
    void testSchedulePeriodicUpdate() throws Exception {
        AtomicInteger updateCount = new AtomicInteger(0);
        
        runAndWait(() -> {
            testPanel.initialize();
            testPanel.schedulePeriodicUpdate(updateCount::incrementAndGet, 1);
        });
        
        // Wait for updates
        Thread.sleep(2500);
        
        assertTrue(updateCount.get() >= 2);
    }
    
    @Test
    void testComponentIdGeneration() throws Exception {
        runAndWait(() -> {
            String id = testPanel.getComponentId();
            assertNotNull(id);
            assertTrue(id.startsWith("TestPanel_"));
        });
    }
    
    @Test
    void testStyleClass() throws Exception {
        runAndWait(() -> {
            testPanel.initialize();
            assertTrue(testPanel.getStyleClass().contains("base-panel"));
        });
    }
    
    /**
     * Test implementation of BasePanel
     */
    private static class TestPanel extends BasePanel {
        final AtomicBoolean initializeCalled = new AtomicBoolean(false);
        final AtomicBoolean cleanupCalled = new AtomicBoolean(false);
        final AtomicInteger initializeCount = new AtomicInteger(0);
        final AtomicInteger refreshCount = new AtomicInteger(0);
        
        @Override
        protected void doInitialize() {
            initializeCalled.set(true);
            initializeCount.incrementAndGet();
            
            // Add some test content
            Label label = labelManager.getOrCreateLabel("test_label", "Test Content");
            getChildren().add(label);
        }
        
        @Override
        protected void doRefresh() {
            refreshCount.incrementAndGet();
            labelManager.updateLabel("test_label", "Refreshed Content");
        }
        
        @Override
        protected void doCleanup() {
            cleanupCalled.set(true);
        }
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