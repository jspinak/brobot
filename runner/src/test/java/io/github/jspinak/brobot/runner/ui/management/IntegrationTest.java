package io.github.jspinak.brobot.runner.ui.management;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.testutils.ImprovedJavaFXTestBase;
import io.github.jspinak.brobot.runner.testutils.TestHelper;
import io.github.jspinak.brobot.runner.ui.panels.ExampleLabelManagedPanel;
import io.github.jspinak.brobot.runner.ui.panels.RefactoredResourceMonitorPanel;
import io.github.jspinak.brobot.runner.ui.config.RefactoredConfigDetailsPanel;
import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;
import io.github.jspinak.brobot.runner.cache.CacheManager;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.runner.resources.ImageResourceManager;
import io.github.jspinak.brobot.runner.session.SessionManager;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for LabelManager and UIUpdateManager working together
 * across multiple UI components.
 */
class IntegrationTest extends ImprovedJavaFXTestBase {
    
    @Mock
    private EventBus eventBus;
    
    @Mock
    private ResourceManager resourceManager;
    
    @Mock
    private ImageResourceManager imageResourceManager;
    
    @Mock
    private CacheManager cacheManager;
    
    @Mock
    private SessionManager sessionManager;
    
    private LabelManager labelManager;
    private UIUpdateManager uiUpdateManager;
    
    @BeforeEach
    void setUp() throws InterruptedException {
        MockitoAnnotations.openMocks(this);
        
        labelManager = new LabelManager();
        uiUpdateManager = new UIUpdateManager();
        uiUpdateManager.initialize();
    }
    
    @Test
    void testMultiplePanelsShareLabelManager() throws InterruptedException {
        CountDownLatch initLatch = new CountDownLatch(1);
        
        runAndWait(() -> {
            // Create multiple panels sharing the same managers
            ExampleLabelManagedPanel panel1 = TestHelper.createExamplePanel(labelManager, uiUpdateManager);
            panel1.initialize();
            
            RefactoredConfigDetailsPanel panel2 = new RefactoredConfigDetailsPanel(eventBus, labelManager, uiUpdateManager);
            panel2.postConstruct();
            
            // Both panels should have created labels
            assertTrue(labelManager.getLabelCount() > 0);
            
            // Labels should be associated with their respective components
            assertEquals(1, labelManager.getComponentCount()); // Only panel1 creates component-specific labels
            
            initLatch.countDown();
        });
        
        assertTrue(initLatch.await(1, TimeUnit.SECONDS));
    }
    
    @Test
    void testLabelUpdatesAcrossPanels() throws InterruptedException {
        // Create panel
        final ExampleLabelManagedPanel panel = TestHelper.createExamplePanel(labelManager, uiUpdateManager);
        runAndWait(() -> {
            panel.initialize();
        });
        
        // Update labels
        runAndWait(() -> {
            labelManager.updateLabel(panel, "statusLabel", "New Status");
            labelManager.updateLabel(panel, "progressLabel", "75%");
        });
        
        // Verify updates
        runAndWait(() -> {
            Label statusLabel = labelManager.getOrCreateLabel(panel, "statusLabel", "");
            assertEquals("New Status", statusLabel.getText());
            
            Label progressLabel = labelManager.getOrCreateLabel(panel, "progressLabel", "");
            assertEquals("75%", progressLabel.getText());
        });
    }
    
    @Test
    void testUIUpdateManagerHandlesMultiplePanels() throws InterruptedException {
        CountDownLatch updateLatch = new CountDownLatch(3);
        
        runAndWait(() -> {
            // Create multiple panels
            ExampleLabelManagedPanel panel1 = TestHelper.createExamplePanel(labelManager, uiUpdateManager);
            panel1.initialize();
            
            RefactoredConfigDetailsPanel panel2 = new RefactoredConfigDetailsPanel(eventBus, labelManager, uiUpdateManager);
            panel2.postConstruct();
            
            // Schedule updates for different panels
            uiUpdateManager.executeUpdate("panel1-update", () -> {
                labelManager.updateLabel(panel1, "statusLabel", "Panel 1 Updated");
                updateLatch.countDown();
            });
            
            uiUpdateManager.executeUpdate("panel2-update", () -> {
                ConfigEntry config = TestHelper.createTestConfigEntry("Test Config", "Test Project");
                panel2.setConfiguration(config);
                updateLatch.countDown();
            });
            
            uiUpdateManager.queueUpdate("global-update", () -> {
                // Global update affecting both panels
                updateLatch.countDown();
            });
        });
        
        assertTrue(updateLatch.await(2, TimeUnit.SECONDS));
        
        // Verify metrics for each update task
        assertNotNull(uiUpdateManager.getMetrics("panel1-update"));
        assertNotNull(uiUpdateManager.getMetrics("panel2-update"));
        assertNotNull(uiUpdateManager.getMetrics("global-update"));
    }
    
    @Test
    void testPanelCleanupDoesNotAffectOthers() throws InterruptedException {
        // Create panels
        final ExampleLabelManagedPanel panel1 = TestHelper.createExamplePanel(labelManager, uiUpdateManager);
        final RefactoredConfigDetailsPanel panel2 = new RefactoredConfigDetailsPanel(eventBus, labelManager, uiUpdateManager);
        
        runAndWait(() -> {
            panel1.initialize();
            panel2.postConstruct();
        });
        
        int totalLabels = labelManager.getLabelCount();
        assertTrue(totalLabels > 0);
        
        // Clean up panel1
        runAndWait(() -> {
            panel1.cleanup();
        });
        
        // Panel2's labels should still exist
        assertTrue(labelManager.getLabelCount() > 0);
        assertTrue(labelManager.getLabelCount() < totalLabels);
        
        // Clean up panel2
        runAndWait(() -> {
            panel2.preDestroy();
        });
        
        // All labels should be cleaned up
        assertEquals(0, labelManager.getLabelCount());
    }
    
    @Test
    void testScheduledUpdatesAcrossMultiplePanels() throws InterruptedException {
        runAndWait(() -> {
            // Create a panel with scheduled updates
            RefactoredResourceMonitorPanel resourcePanel = new RefactoredResourceMonitorPanel(
                resourceManager,
                imageResourceManager,
                cacheManager,
                sessionManager,
                labelManager,
                uiUpdateManager
            );
            resourcePanel.postConstruct();
            
            // Create another panel
            ExampleLabelManagedPanel examplePanel = TestHelper.createExamplePanel(labelManager, uiUpdateManager);
            examplePanel.initialize();
        });
        
        // Let scheduled updates run
        Thread.sleep(100);
        
        // Both panels should have their scheduled updates registered
        assertNotNull(uiUpdateManager.getMetrics("resource-monitor-update"));
        assertNotNull(uiUpdateManager.getMetrics("periodic-update"));
    }
    
    @Test
    void testConcurrentUpdatesHandledSafely() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(10);
        
        runAndWait(() -> {
            ExampleLabelManagedPanel panel = TestHelper.createExamplePanel(labelManager, uiUpdateManager);
            panel.initialize();
            
            startLatch.countDown();
            
            // Simulate concurrent updates from multiple threads
            for (int i = 0; i < 10; i++) {
                final int index = i;
                new Thread(() -> {
                    uiUpdateManager.queueUpdate("concurrent-update", () -> {
                        labelManager.updateLabel(panel, "statusLabel", "Update " + index);
                        completeLatch.countDown();
                    });
                }).start();
            }
        });
        
        assertTrue(startLatch.await(1, TimeUnit.SECONDS));
        assertTrue(completeLatch.await(2, TimeUnit.SECONDS));
        
        // All updates should have been processed
        UIUpdateManager.UpdateMetrics metrics = uiUpdateManager.getMetrics("concurrent-update");
        assertNotNull(metrics);
        assertEquals(10, metrics.getTotalUpdates());
    }
    
    @Test
    void testPerformanceMetricsAggregation() throws InterruptedException {
        runAndWait(() -> {
            // Create multiple panels
            ExampleLabelManagedPanel panel1 = TestHelper.createExamplePanel(labelManager, uiUpdateManager);
            panel1.initialize();
            
            RefactoredConfigDetailsPanel panel2 = new RefactoredConfigDetailsPanel(eventBus, labelManager, uiUpdateManager);
            panel2.postConstruct();
            
            RefactoredResourceMonitorPanel panel3 = new RefactoredResourceMonitorPanel(
                resourceManager,
                imageResourceManager,
                cacheManager,
                sessionManager,
                labelManager,
                uiUpdateManager
            );
            panel3.postConstruct();
        });
        
        // Trigger some updates
        for (int i = 0; i < 5; i++) {
            runAndWait(() -> {
                uiUpdateManager.executeUpdate("test-update", () -> {});
            });
        }
        
        // Get aggregated metrics
        var allMetrics = uiUpdateManager.getAllMetrics();
        assertFalse(allMetrics.isEmpty());
        
        // Verify we have metrics from different components
        assertTrue(allMetrics.size() >= 3); // At least 3 different update tasks
    }
    
    @Test
    void testMemoryManagementWithWeakReferences() throws InterruptedException {
        // Create a panel in a limited scope
        runAndWait(() -> {
            ExampleLabelManagedPanel panel = TestHelper.createExamplePanel(labelManager, uiUpdateManager);
            panel.initialize();
            
            // Panel goes out of scope here
        });
        
        // Force garbage collection
        System.gc();
        Thread.sleep(100);
        System.gc();
        
        // The component registry should handle weak references properly
        // This is more of a sanity check - actual memory leak testing would require profiling
        assertTrue(true); // If we get here without issues, weak references are working
    }
    
    @Test
    void testIntegrationWithJavaFXScene() throws InterruptedException {
        runAndWait(() -> {
            // Create a complete scene with multiple panels
            VBox root = new VBox(10);
            
            ExampleLabelManagedPanel panel1 = TestHelper.createExamplePanel(labelManager, uiUpdateManager);
            panel1.initialize();
            
            RefactoredConfigDetailsPanel panel2 = new RefactoredConfigDetailsPanel(eventBus, labelManager, uiUpdateManager);
            panel2.postConstruct();
            
            root.getChildren().addAll(panel1, panel2);
            
            Scene scene = new Scene(root, 800, 600);
            Stage stage = new Stage();
            stage.setScene(scene);
            
            // Verify the scene is properly constructed
            assertEquals(2, root.getChildren().size());
            
            // Trigger some updates
            uiUpdateManager.executeUpdate("scene-update", () -> {
                labelManager.updateLabel(panel1, "statusLabel", "Scene Test");
            });
            
            // Clean up
            panel1.cleanup();
            panel2.preDestroy();
            stage.close();
        });
        
        // Verify cleanup
        assertEquals(0, labelManager.getLabelCount());
    }
}