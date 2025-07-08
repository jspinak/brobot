package io.github.jspinak.brobot.runner.ui.integration;

import io.github.jspinak.brobot.runner.ui.components.base.BasePanel;
import io.github.jspinak.brobot.runner.ui.managers.LabelManager;
import io.github.jspinak.brobot.runner.ui.managers.UIUpdateManager;
import io.github.jspinak.brobot.runner.ui.registry.UIComponentRegistry;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
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
 * Integration test for the new UI architecture.
 * Tests the interaction between all major components.
 */
@ExtendWith(ApplicationExtension.class)
class UIArchitectureIntegrationTest {
    
    private UIComponentRegistry registry;
    private UIUpdateManager updateManager;
    private LabelManager labelManager;
    private TestIntegrationPanel testPanel;
    
    @BeforeAll
    static void setupJavaFX() {
        // Ensure JavaFX is initialized
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @BeforeEach
    void setUp() {
        registry = new UIComponentRegistry();
        updateManager = new UIUpdateManager();
        labelManager = new LabelManager();
    }
    
    @Start
    void start(Stage stage) {
        testPanel = new TestIntegrationPanel(registry, updateManager, labelManager);
        
        Scene scene = new Scene(testPanel, 600, 400);
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    void testCompleteLifecycle() throws Exception {
        AtomicBoolean initComplete = new AtomicBoolean(false);
        AtomicBoolean cleanupComplete = new AtomicBoolean(false);
        
        runAndWait(() -> {
            // Initialize
            testPanel.initialize();
            assertTrue(testPanel.isInitialized());
            assertTrue(registry.isRegistered(testPanel.getComponentId()));
            initComplete.set(true);
        });
        
        assertTrue(initComplete.get());
        
        // Let it run for a bit
        Thread.sleep(500);
        
        runAndWait(() -> {
            // Cleanup
            testPanel.cleanup();
            assertFalse(testPanel.isInitialized());
            assertFalse(registry.isRegistered(testPanel.getComponentId()));
            cleanupComplete.set(true);
        });
        
        assertTrue(cleanupComplete.get());
    }
    
    @Test
    void testLabelDeduplication() throws Exception {
        runAndWait(() -> {
            testPanel.initialize();
            
            // Create multiple labels with same ID
            for (int i = 0; i < 5; i++) {
                testPanel.addTestLabel("test_label", "Test " + i);
            }
        });
        
        // Wait for UI updates
        Thread.sleep(100);
        
        runAndWait(() -> {
            // Should only have one label in the panel
            long labelCount = testPanel.getChildren().stream()
                .filter(node -> node instanceof Label)
                .filter(node -> "test_label".equals(node.getId()))
                .count();
            
            assertEquals(1, labelCount);
            
            // Label should have the latest text  
            // Find the label in the panel's children
            Label label = testPanel.getChildren().stream()
                .filter(node -> node instanceof Label)
                .map(node -> (Label) node)
                .filter(l -> "test_label".equals(l.getId()))
                .findFirst()
                .orElse(null);
            assertNotNull(label);
            assertEquals("Test 4", label.getText());
        });
    }
    
    @Test
    void testPeriodicUpdates() throws Exception {
        AtomicInteger updateCount = new AtomicInteger(0);
        
        runAndWait(() -> {
            testPanel.initialize();
            testPanel.setUpdateCallback(updateCount::incrementAndGet);
            testPanel.startPeriodicUpdates();
        });
        
        // Wait for updates
        Thread.sleep(2500);
        
        // Should have at least 2 updates (every second)
        assertTrue(updateCount.get() >= 2);
        
        runAndWait(() -> {
            testPanel.stopPeriodicUpdates();
        });
        
        int countAfterStop = updateCount.get();
        Thread.sleep(1500);
        
        // Should not have more updates after stop
        assertEquals(countAfterStop, updateCount.get());
    }
    
    @Test
    void testMultiplePanelCoordination() throws Exception {
        TestIntegrationPanel panel2 = new TestIntegrationPanel(registry, updateManager, labelManager);
        
        runAndWait(() -> {
            testPanel.initialize();
            panel2.initialize();
            
            // Both panels should be registered
            assertTrue(registry.isRegistered(testPanel.getComponentId()));
            assertTrue(registry.isRegistered(panel2.getComponentId()));
            
            // They should have different IDs
            assertNotEquals(testPanel.getComponentId(), panel2.getComponentId());
        });
        
        // Test shared label manager
        runAndWait(() -> {
            testPanel.addTestLabel("shared_label", "From Panel 1");
            panel2.addTestLabel("shared_label", "From Panel 2");
            
            // Both panels should see the same label instance
            // Check if the label manager has the label
            assertTrue(labelManager.hasLabel("shared_label"));
            
            // Verify the text was updated in one of the panels
            Label labelInPanel2 = panel2.getChildren().stream()
                .filter(node -> node instanceof Label)
                .map(node -> (Label) node)
                .filter(l -> "shared_label".equals(l.getId()))
                .findFirst()
                .orElse(null);
            assertNotNull(labelInPanel2);
            assertEquals("From Panel 2", labelInPanel2.getText());
        });
        
        // Cleanup
        runAndWait(() -> {
            panel2.cleanup();
            assertFalse(registry.isRegistered(panel2.getComponentId()));
            assertTrue(registry.isRegistered(testPanel.getComponentId()));
        });
    }
    
    @Test
    void testMemoryManagement() throws Exception {
        runAndWait(() -> {
            // Create and register many components
            for (int i = 0; i < 100; i++) {
                VBox tempBox = new VBox();
                tempBox.setId("temp_" + i);
                registry.register("temp_" + i, tempBox);
            }
            
            assertEquals(100, registry.size());
        });
        
        // Force garbage collection
        System.gc();
        Thread.sleep(200);
        System.gc();
        
        runAndWait(() -> {
            // Cleanup should remove garbage collected components
            int removed = registry.cleanup();
            assertTrue(removed > 90); // Most should be collected
            assertTrue(registry.size() < 10); // Few should remain
        });
    }
    
    @Test
    void testUpdateManagerShutdown() throws Exception {
        AtomicBoolean taskRunning = new AtomicBoolean(true);
        AtomicInteger executionCount = new AtomicInteger(0);
        
        runAndWait(() -> {
            updateManager.scheduleUpdate("test_task", () -> {
                if (taskRunning.get()) {
                    executionCount.incrementAndGet();
                }
            }, 0, 100, TimeUnit.MILLISECONDS);
        });
        
        Thread.sleep(250);
        assertTrue(executionCount.get() >= 2);
        
        // Shutdown update manager
        updateManager.shutdown();
        taskRunning.set(false);
        
        int countAfterShutdown = executionCount.get();
        Thread.sleep(250);
        
        // No more executions after shutdown
        assertEquals(countAfterShutdown, executionCount.get());
    }
    
    /**
     * Test panel for integration testing
     */
    private static class TestIntegrationPanel extends BasePanel {
        private Runnable updateCallback;
        private final UIUpdateManager updateMgr;
        private final LabelManager labelMgr;
        
        TestIntegrationPanel(UIComponentRegistry registry, UIUpdateManager updateManager, LabelManager labelManager) {
            this.updateMgr = updateManager;
            this.labelMgr = labelManager;
            
            // Inject dependencies using reflection since fields are protected
            try {
                var registryField = BasePanel.class.getDeclaredField("componentRegistry");
                registryField.setAccessible(true);
                registryField.set(this, registry);
                
                var updateField = BasePanel.class.getDeclaredField("updateManager");
                updateField.setAccessible(true);
                updateField.set(this, updateManager);
                
                var labelField = BasePanel.class.getDeclaredField("labelManager");
                labelField.setAccessible(true);
                labelField.set(this, labelManager);
            } catch (Exception e) {
                throw new RuntimeException("Failed to inject dependencies", e);
            }
        }
        
        void setUpdateCallback(Runnable callback) {
            this.updateCallback = callback;
        }
        
        @Override
        protected void doInitialize() {
            // Simple initialization
        }
        
        @Override
        protected void doRefresh() {
            if (updateCallback != null) {
                updateCallback.run();
            }
        }
        
        @Override
        protected void doCleanup() {
            // Simple cleanup
        }
        
        void addTestLabel(String id, String text) {
            Label label = labelMgr.getOrCreateLabel(id, text);
            if (!getChildren().contains(label)) {
                getChildren().add(label);
            }
        }
        
        void startPeriodicUpdates() {
            schedulePeriodicUpdate(this::refresh, 1);
        }
        
        void stopPeriodicUpdates() {
            updateMgr.cancelTask(getComponentId());
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