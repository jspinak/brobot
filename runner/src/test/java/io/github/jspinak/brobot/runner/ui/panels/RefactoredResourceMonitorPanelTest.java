package io.github.jspinak.brobot.runner.ui.panels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.scene.control.Label;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.runner.cache.CacheManager;
import io.github.jspinak.brobot.runner.resources.ImageResourceManager;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.runner.session.SessionManager;
import io.github.jspinak.brobot.runner.testutils.ImprovedJavaFXTestBase;
import io.github.jspinak.brobot.runner.ui.management.LabelManager;
import io.github.jspinak.brobot.runner.ui.management.UIUpdateManager;
import io.github.jspinak.brobot.runner.ui.services.ResourceMonitoringService;

class RefactoredResourceMonitorPanelTest extends ImprovedJavaFXTestBase {

    @Mock private ResourceManager resourceManager;

    @Mock private ImageResourceManager imageResourceManager;

    @Mock private CacheManager cacheManager;

    @Mock private SessionManager sessionManager;

    private LabelManager labelManager;
    private UIUpdateManager uiUpdateManager;
    private RefactoredResourceMonitorPanel panel;

    @BeforeEach
    void setUp() throws InterruptedException {
        MockitoAnnotations.openMocks(this);

        labelManager = new LabelManager();
        uiUpdateManager = new UIUpdateManager();
        uiUpdateManager.initialize();

        // Set up default mock behaviors
        when(sessionManager.isSessionActive()).thenReturn(false);

        runAndWait(
                () -> {
                    panel =
                            new RefactoredResourceMonitorPanel(
                                    resourceManager,
                                    imageResourceManager,
                                    cacheManager,
                                    sessionManager,
                                    labelManager,
                                    uiUpdateManager);
                });
    }

    @Test
    void testInitialization() throws InterruptedException {
        runAndWait(
                () -> {
                    // Verify panel has the correct style class
                    assertTrue(panel.getStyleClass().contains("resource-monitor-panel"));

                    // Verify three cards are created
                    assertEquals(3, panel.getChildren().size());
                });
    }

    @Test
    void testLabelsCreatedWithLabelManager() throws InterruptedException {
        runAndWait(
                () -> {
                    // Verify labels were created through LabelManager
                    assertTrue(labelManager.getLabelCount() > 0);

                    // Check specific labels exist
                    Label totalLabel =
                            labelManager.getOrCreateLabel(panel, "resource-status-total", "0");
                    assertNotNull(totalLabel);

                    Label memoryLabel =
                            labelManager.getOrCreateLabel(panel, "resource-status-memory", "0 MB");
                    assertNotNull(memoryLabel);
                });
    }

    @Test
    void testUIUpdateWithResourceData() throws InterruptedException {
        // Create test data
        ResourceMonitoringService.ResourceData testData =
                new ResourceMonitoringService.ResourceData();
        testData.setTotalResources(100);
        testData.setCachedImages(50);
        testData.setActiveMats(25);
        testData.setMemoryMB(128.5);
        testData.setCacheStats(new HashMap<>());

        CountDownLatch updateLatch = new CountDownLatch(1);

        runAndWait(
                () -> {
                    // Trigger UI update
                    panel.handleMonitoringData(testData);
                    updateLatch.countDown();
                });

        assertTrue(updateLatch.await(1, TimeUnit.SECONDS));

        // Verify labels were updated
        runAndWait(
                () -> {
                    Label totalLabel =
                            labelManager.getOrCreateLabel(panel, "resource-status-total", "");
                    assertEquals("100", totalLabel.getText());

                    Label memoryLabel =
                            labelManager.getOrCreateLabel(panel, "resource-status-memory", "");
                    assertEquals("128.50 MB", memoryLabel.getText());
                });
    }

    @Test
    void testScheduledUpdatesRegistered() {
        // Verify that periodic updates were scheduled
        UIUpdateManager.UpdateMetrics metrics =
                uiUpdateManager.getMetrics("resource-monitor-update");
        assertNotNull(metrics);
    }

    @Test
    void testCleanupOnDestroy() throws InterruptedException {
        int initialLabelCount = labelManager.getLabelCount();

        runAndWait(
                () -> {
                    panel.preDestroy();
                });

        // Verify labels were removed
        assertEquals(0, labelManager.getLabelCount());

        // Verify scheduled update was cancelled
        assertFalse(uiUpdateManager.cancelScheduledUpdate("resource-monitor-update"));
    }

    @Test
    void testPerformanceSummary() throws InterruptedException {
        // Trigger some updates to generate metrics
        ResourceMonitoringService.ResourceData testData =
                new ResourceMonitoringService.ResourceData();
        testData.setTotalResources(100);
        testData.setCachedImages(50);
        testData.setActiveMats(25);
        testData.setMemoryMB(128.5);
        testData.setCacheStats(new HashMap<>());

        for (int i = 0; i < 5; i++) {
            runAndWait(() -> panel.handleMonitoringData(testData));
        }

        String summary = panel.getPerformanceSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("Total updates:"));
        assertTrue(summary.contains("Average duration:"));
    }

    @Test
    void testSessionInfoFormatting() throws InterruptedException {
        // Set up active session
        io.github.jspinak.brobot.runner.session.Session mockSession =
                new io.github.jspinak.brobot.runner.session.Session();
        mockSession.setId("12345678-abcd-efgh-ijkl");

        when(sessionManager.isSessionActive()).thenReturn(true);
        when(sessionManager.getCurrentSession()).thenReturn(mockSession);
        when(sessionManager.getLastAutosaveTime()).thenReturn(null);

        ResourceMonitoringService.ResourceData testData =
                new ResourceMonitoringService.ResourceData();
        testData.setTotalResources(0);
        testData.setCachedImages(0);
        testData.setActiveMats(0);
        testData.setMemoryMB(0);
        testData.setCacheStats(new HashMap<>());

        runAndWait(
                () -> {
                    panel.handleMonitoringData(testData);
                });

        // Verify session info was formatted correctly
        verify(sessionManager, atLeastOnce()).isSessionActive();
        verify(sessionManager, atLeastOnce()).getCurrentSession();
    }
}
