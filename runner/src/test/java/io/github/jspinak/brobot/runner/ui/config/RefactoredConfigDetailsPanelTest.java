package io.github.jspinak.brobot.runner.ui.config;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.testutils.JavaFXTestBase;
import io.github.jspinak.brobot.runner.testutils.TestHelper;
import io.github.jspinak.brobot.runner.ui.management.LabelManager;
import io.github.jspinak.brobot.runner.ui.management.UIUpdateManager;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "JavaFX tests require display")
class RefactoredConfigDetailsPanelTest extends JavaFXTestBase {
    
    @Mock
    private EventBus eventBus;
    
    private LabelManager labelManager;
    private UIUpdateManager uiUpdateManager;
    private RefactoredConfigDetailsPanel panel;
    
    @BeforeEach
    void setUp() throws InterruptedException {
        MockitoAnnotations.openMocks(this);
        
        labelManager = new LabelManager();
        uiUpdateManager = new UIUpdateManager();
        uiUpdateManager.initialize();
        
        runAndWait(() -> {
            panel = new RefactoredConfigDetailsPanel(eventBus, labelManager, uiUpdateManager);
            panel.postConstruct();
        });
    }
    
    @Test
    void testInitialization() throws InterruptedException {
        runAndWait(() -> {
            // Verify panel has the correct style class
            assertTrue(panel.getStyleClass().contains("configuration-details"));
            
            // Verify children are created
            assertFalse(panel.getChildren().isEmpty());
            
            // Verify labels were created through LabelManager
            assertEquals(6, labelManager.getLabelCount());
        });
    }
    
    @Test
    void testSetConfiguration() throws InterruptedException {
        // Create test configuration
        ConfigEntry testConfig = TestHelper.createTestConfigEntry();
        testConfig.setDescription("Test Description");
        
        CountDownLatch updateLatch = new CountDownLatch(1);
        
        runAndWait(() -> {
            panel.setConfiguration(testConfig);
            updateLatch.countDown();
        });
        
        assertTrue(updateLatch.await(1, TimeUnit.SECONDS));
        
        // Verify labels were updated
        runAndWait(() -> {
            Label nameLabel = labelManager.getOrCreateLabel(panel, "config-name", "");
            assertEquals("TestConfig", nameLabel.getText());
            
            Label projectLabel = labelManager.getOrCreateLabel(panel, "config-project", "");
            assertEquals("TestProject", projectLabel.getText());
            
            Label imagePathLabel = labelManager.getOrCreateLabel(panel, "config-image-path", "");
            assertEquals("/test/images", imagePathLabel.getText());
        });
    }
    
    @Test
    void testClearConfiguration() throws InterruptedException {
        // First set a configuration
        ConfigEntry testConfig = TestHelper.createTestConfigEntry();
        
        runAndWait(() -> panel.setConfiguration(testConfig));
        
        // Now clear it
        runAndWait(() -> panel.clearConfiguration());
        
        // Verify labels were cleared
        runAndWait(() -> {
            Label nameLabel = labelManager.getOrCreateLabel(panel, "config-name", "");
            assertEquals("", nameLabel.getText());
            
            Label projectLabel = labelManager.getOrCreateLabel(panel, "config-project", "");
            assertEquals("", projectLabel.getText());
        });
    }
    
    @Test
    void testEditMetadataToggle() throws InterruptedException {
        // Set a configuration
        ConfigEntry testConfig = TestHelper.createTestConfigEntry();
        testConfig.setDescription("Original Description");
        testConfig.setAuthor("Original Author");
        testConfig.setVersion("1.0.0");
        
        runAndWait(() -> panel.setConfiguration(testConfig));
        
        // Verify the configuration was set
        runAndWait(() -> {
            // The panel should have the configuration
            assertEquals(testConfig, panel.getConfiguration());
            
            // Verify labels were updated with metadata
            Label nameLabel = labelManager.getOrCreateLabel(panel, "config-name", "");
            assertEquals("TestConfig", nameLabel.getText());
        });
    }
    
    @Test
    void testUIUpdateMetrics() throws InterruptedException {
        // Perform several updates
        ConfigEntry testConfig = TestHelper.createTestConfigEntry();
        
        for (int i = 0; i < 5; i++) {
            final int index = i;
            runAndWait(() -> {
                testConfig.setName("TestConfig" + index);
                panel.setConfiguration(testConfig);
            });
        }
        
        // Check metrics
        UIUpdateManager.UpdateMetrics metrics = uiUpdateManager.getMetrics("config-details-update");
        assertNotNull(metrics);
        assertTrue(metrics.getTotalUpdates() >= 5);
    }
    
    @Test
    void testPerformanceSummary() throws InterruptedException {
        // Trigger some updates
        ConfigEntry testConfig = TestHelper.createTestConfigEntry();
        
        runAndWait(() -> panel.setConfiguration(testConfig));
        
        String summary = panel.getPerformanceSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("Config Details Panel Performance"));
        assertTrue(summary.contains("UI Updates"));
        assertTrue(summary.contains("Labels managed: 6"));
    }
    
    @Test
    void testCleanup() throws InterruptedException {
        // Set a configuration to create some state
        ConfigEntry testConfig = TestHelper.createTestConfigEntry();
        
        runAndWait(() -> panel.setConfiguration(testConfig));
        
        int initialLabelCount = labelManager.getLabelCount();
        assertEquals(6, initialLabelCount);
        
        runAndWait(() -> panel.preDestroy());
        
        // Verify labels were removed
        assertEquals(0, labelManager.getLabelCount());
    }
    
    @Test
    void testConfigurationProperty() {
        ConfigEntry testConfig = TestHelper.createTestConfigEntry();
        testConfig.setName("TestConfig");
        
        panel.setConfiguration(testConfig);
        assertEquals(testConfig, panel.getConfiguration());
        
        panel.clearConfiguration();
        assertNull(panel.getConfiguration());
    }
}