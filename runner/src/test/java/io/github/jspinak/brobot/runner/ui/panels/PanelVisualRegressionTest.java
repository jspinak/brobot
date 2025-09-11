package io.github.jspinak.brobot.runner.ui.panels;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.runner.cache.CacheManager;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.resources.ImageResourceManager;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.runner.session.SessionManager;
import io.github.jspinak.brobot.runner.testutils.ImprovedJavaFXTestBase;
import io.github.jspinak.brobot.runner.testutils.TestHelper;
import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;
import io.github.jspinak.brobot.runner.ui.config.RefactoredConfigDetailsPanel;
import io.github.jspinak.brobot.runner.ui.management.LabelManager;
import io.github.jspinak.brobot.runner.ui.management.UIUpdateManager;
import io.github.jspinak.brobot.runner.ui.services.ResourceMonitoringService;
import io.github.jspinak.brobot.runner.ui.testing.VisualRegressionTest;

/** Visual regression tests for refactored UI panels. */
class PanelVisualRegressionTest extends ImprovedJavaFXTestBase {

    @Mock private EventBus eventBus;

    @Mock private ResourceManager resourceManager;

    @Mock private ImageResourceManager imageResourceManager;

    @Mock private CacheManager cacheManager;

    @Mock private SessionManager sessionManager;

    private LabelManager labelManager;
    private UIUpdateManager uiUpdateManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        labelManager = new LabelManager();
        uiUpdateManager = new UIUpdateManager();
        uiUpdateManager.initialize();
    }

    @Test
    void testExampleLabelManagedPanelVisual() throws Exception {
        runAndWait(
                () -> {
                    ExampleLabelManagedPanel panel =
                            TestHelper.createExamplePanel(labelManager, uiUpdateManager);
                    panel.initialize();

                    // Set some data
                    labelManager.updateLabel(panel, "statusLabel", "Active");
                    labelManager.updateLabel(panel, "progressLabel", "75%");
                    labelManager.updateLabel(panel, "infoLabel", "Processing...");

                    // Create scene
                    Scene scene = new Scene(panel, 400, 300);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.show();

                    // Wait for rendering
                    // waitForFxEvents(); // Can't be called inside runAndWait
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    // Visual regression testing
                    boolean matches =
                            VisualRegressionTest.captureAndCompare(
                                    panel, "example-label-managed-panel");
                    assertTrue(matches, "Visual regression detected in ExampleLabelManagedPanel");

                    // Test with different states
                    labelManager.updateLabel(panel, "statusLabel", "Error");
                    labelManager.updateLabel(panel, "progressLabel", "0%");
                    // waitForFxEvents(); // Can't be called inside runAndWait
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    matches =
                            VisualRegressionTest.captureAndCompare(
                                    panel, "example-label-managed-panel-error");
                    assertTrue(
                            matches,
                            "Visual regression detected in ExampleLabelManagedPanel error state");

                    stage.close();
                });
    }

    @Test
    void testRefactoredResourceMonitorPanelVisual() throws Exception {
        runAndWait(
                () -> {
                    RefactoredResourceMonitorPanel panel =
                            new RefactoredResourceMonitorPanel(
                                    resourceManager,
                                    imageResourceManager,
                                    cacheManager,
                                    sessionManager,
                                    labelManager,
                                    uiUpdateManager);
                    panel.postConstruct();

                    // Set mock data
                    when(sessionManager.isSessionActive()).thenReturn(true);

                    // Simulate resource data update
                    ResourceMonitoringService.ResourceData testData =
                            new ResourceMonitoringService.ResourceData();
                    testData.setTotalResources(150);
                    testData.setCachedImages(75);
                    testData.setActiveMats(25);
                    testData.setMemoryMB(256.5);
                    testData.setCacheStats(new HashMap<>());

                    panel.handleMonitoringData(testData);

                    // Create scene
                    Scene scene = new Scene(panel, 600, 400);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.show();

                    // Wait for rendering
                    // waitForFxEvents(); // Can't be called inside runAndWait
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    // Capture and compare
                    boolean matches =
                            VisualRegressionTest.captureAndCompare(panel, "resource-monitor-panel");
                    assertTrue(
                            matches,
                            "Visual regression detected in RefactoredResourceMonitorPanel");

                    stage.close();
                });
    }

    @Test
    void testRefactoredConfigDetailsPanelVisual() throws Exception {
        runAndWait(
                () -> {
                    RefactoredConfigDetailsPanel panel =
                            new RefactoredConfigDetailsPanel(
                                    eventBus, labelManager, uiUpdateManager);
                    panel.postConstruct();

                    // Create test configuration
                    ConfigEntry testConfig =
                            TestHelper.createTestConfigEntry("Test Configuration", "Test Project");
                    testConfig.setDescription(
                            "This is a test configuration for visual regression testing");

                    panel.setConfiguration(testConfig);

                    // Create scene
                    Scene scene = new Scene(panel, 700, 500);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.show();

                    // Wait for rendering
                    // waitForFxEvents(); // Can't be called inside runAndWait
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    // Capture and compare
                    boolean matches =
                            VisualRegressionTest.captureAndCompare(panel, "config-details-panel");
                    assertTrue(
                            matches, "Visual regression detected in RefactoredConfigDetailsPanel");

                    // Test empty state
                    panel.clearConfiguration();
                    // waitForFxEvents(); // Can't be called inside runAndWait
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    matches =
                            VisualRegressionTest.captureAndCompare(
                                    panel, "config-details-panel-empty");
                    assertTrue(
                            matches,
                            "Visual regression detected in RefactoredConfigDetailsPanel empty"
                                    + " state");

                    stage.close();
                });
    }

    @Test
    void testPanelLayoutIntegration() throws Exception {
        runAndWait(
                () -> {
                    // Create a layout with multiple panels
                    VBox container = new VBox(20);
                    container.setStyle("-fx-padding: 20; -fx-background-color: #f0f0f0;");

                    // Add example panel
                    ExampleLabelManagedPanel examplePanel =
                            TestHelper.createExamplePanel(labelManager, uiUpdateManager);
                    examplePanel.initialize();
                    labelManager.updateLabel(examplePanel, "statusLabel", "Connected");

                    // Add resource monitor summary
                    RefactoredResourceMonitorPanel resourcePanel =
                            new RefactoredResourceMonitorPanel(
                                    resourceManager,
                                    imageResourceManager,
                                    cacheManager,
                                    sessionManager,
                                    labelManager,
                                    uiUpdateManager);
                    resourcePanel.postConstruct();
                    resourcePanel.setMaxHeight(200);

                    container.getChildren().addAll(examplePanel, resourcePanel);

                    // Create scene
                    Scene scene = new Scene(container, 800, 600);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.show();

                    // Wait for rendering
                    // waitForFxEvents(); // Can't be called inside runAndWait
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    // Capture and compare
                    boolean matches =
                            VisualRegressionTest.captureAndCompare(
                                    container, "panel-integration-layout");
                    assertTrue(matches, "Visual regression detected in panel integration layout");

                    stage.close();
                });
    }

    @Test
    void testThemeVariations() throws Exception {
        runAndWait(
                () -> {
                    ExampleLabelManagedPanel panel =
                            TestHelper.createExamplePanel(labelManager, uiUpdateManager);
                    panel.initialize();

                    // Create scene
                    Scene scene = new Scene(panel, 400, 300);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.show();

                    // Test light theme
                    scene.getRoot().getStyleClass().add("theme-light");
                    // waitForFxEvents(); // Can't be called inside runAndWait
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    boolean matches =
                            VisualRegressionTest.captureAndCompare(
                                    panel, "example-panel-light-theme");
                    assertTrue(matches, "Visual regression in light theme");

                    // Test dark theme
                    scene.getRoot().getStyleClass().remove("theme-light");
                    scene.getRoot().getStyleClass().add("theme-dark");
                    // waitForFxEvents(); // Can't be called inside runAndWait
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    matches =
                            VisualRegressionTest.captureAndCompare(
                                    panel, "example-panel-dark-theme");
                    assertTrue(matches, "Visual regression in dark theme");

                    stage.close();
                });
    }

    @Test
    void testResponsiveLayout() throws Exception {
        runAndWait(
                () -> {
                    RefactoredConfigDetailsPanel panel =
                            new RefactoredConfigDetailsPanel(
                                    eventBus, labelManager, uiUpdateManager);
                    panel.postConstruct();

                    // Set test data
                    ConfigEntry testConfig =
                            TestHelper.createTestConfigEntry("Responsive Test", "Test Project");

                    panel.setConfiguration(testConfig);

                    // Test different sizes
                    Stage stage = new Stage();

                    // Small size
                    Scene smallScene = new Scene(panel, 400, 300);
                    stage.setScene(smallScene);
                    stage.show();
                    // waitForFxEvents(); // Can't be called inside runAndWait
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    boolean matches =
                            VisualRegressionTest.captureAndCompare(panel, "config-panel-small");
                    assertTrue(matches, "Visual regression in small layout");

                    // Medium size
                    stage.setWidth(600);
                    stage.setHeight(400);
                    // waitForFxEvents(); // Can't be called inside runAndWait
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    matches = VisualRegressionTest.captureAndCompare(panel, "config-panel-medium");
                    assertTrue(matches, "Visual regression in medium layout");

                    // Large size
                    stage.setWidth(1000);
                    stage.setHeight(700);
                    // waitForFxEvents(); // Can't be called inside runAndWait
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    matches = VisualRegressionTest.captureAndCompare(panel, "config-panel-large");
                    assertTrue(matches, "Visual regression in large layout");

                    stage.close();
                });
    }

    /**
     * Use this test to update baselines when UI changes are intentional. Uncomment and run when
     * needed.
     */
    // @Test
    void updateAllBaselineImages() {
        VisualRegressionTest.updateAllBaselines();
        VisualRegressionTest.generateReport();
    }
}
