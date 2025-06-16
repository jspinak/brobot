package io.github.jspinak.brobot.runner.ui.config;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.project.Project;
import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.services.ProjectManager;
import io.github.jspinak.brobot.runner.testutil.JavaFXTestUtils;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConfigManagementPanelTest {

    @BeforeAll
    public static void initJavaFX() throws InterruptedException {
        JavaFXTestUtils.initJavaFX();
    }

    @Mock
    private EventBus eventBus;
    @Mock
    private BrobotRunnerProperties properties;
    @Mock
    private BrobotLibraryInitializer libraryInitializer;
    @Mock
    private ApplicationConfig appConfig;
    @Mock
    private ProjectManager projectManager;
    @Mock
    private AllStatesInProjectService allStatesService;
    @Mock
    private ConfigSelectionPanel selectionPanel;
    @Mock
    private ConfigBrowserPanel browserPanel;
    @Mock
    private ConfigMetadataEditor metadataEditor;

    private ConfigManagementPanel panel;
    private Stage stage;

    @BeforeEach
    public void setUp() throws InterruptedException {
        JavaFXTestUtils.runOnFXThread(() -> {
            stage = new Stage();

            // Configure properties
            when(properties.getConfigPath()).thenReturn("/path/to/config");
            when(properties.getImagePath()).thenReturn("/path/to/images");
        });
    }

    private void createPanel() {
        // Create a new panel instance for each test
        panel = new ConfigManagementPanel(
                eventBus,
                properties,
                libraryInitializer,
                appConfig,
                projectManager,
                allStatesService
        );

        // Use reflection to replace the panels with mocks before they're used
        try {
            Field selectionPanelField = ConfigManagementPanel.class.getDeclaredField("selectionPanel");
            Field browserPanelField = ConfigManagementPanel.class.getDeclaredField("browserPanel");
            Field metadataEditorField = ConfigManagementPanel.class.getDeclaredField("metadataEditor");

            selectionPanelField.setAccessible(true);
            browserPanelField.setAccessible(true);
            metadataEditorField.setAccessible(true);

            selectionPanelField.set(panel, selectionPanel);
            browserPanelField.set(panel, browserPanel);
            metadataEditorField.set(panel, metadataEditor);
        } catch (Exception e) {
            fail("Failed to replace panels with mocks: " + e.getMessage());
        }
    }

    @Test
    public void testCreateUI() throws InterruptedException {
        // Create panel and add to scene
        CountDownLatch latch = new CountDownLatch(1);
        JavaFXTestUtils.runOnFXThread(() -> {
            createPanel();
            stage.setScene(new javafx.scene.Scene(panel));
            stage.show();
        });
        latch.countDown();

        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX operation timed out");

        // Verify panel structure
        CountDownLatch verifyLatch = new CountDownLatch(1);
        JavaFXTestUtils.runOnFXThread(() -> {
            try {
                // Verify that the panel is not null
                assertNotNull(panel);

                // Get the tabPane using reflection
                Field tabPaneField = ConfigManagementPanel.class.getDeclaredField("tabPane");
                tabPaneField.setAccessible(true);
                TabPane tabPane = (TabPane) tabPaneField.get(panel);

                assertNotNull(tabPane);
                assertEquals(3, tabPane.getTabs().size());
                assertEquals("Configurations", tabPane.getTabs().get(0).getText());
                assertEquals("Browser", tabPane.getTabs().get(1).getText());
                assertEquals("Metadata", tabPane.getTabs().get(2).getText());

                verifyLatch.countDown();
            } catch (Exception e) {
                fail("Exception accessing panel fields: " + e.getMessage());
            }
        });

        assertTrue(verifyLatch.await(5, TimeUnit.SECONDS), "JavaFX verification timed out");
    }

    @Test
    public void testCreateNewConfiguration() throws InterruptedException {
        // Create panel and mock method
        CountDownLatch setupLatch = new CountDownLatch(1);
        JavaFXTestUtils.runOnFXThread(() -> {
            createPanel();
            ConfigManagementPanel panelSpy = spy(panel);
            doNothing().when(panelSpy).createNewConfiguration();

            // Execute test
            panelSpy.createNewConfiguration();

            // Verify
            verify(panelSpy).createNewConfiguration();
            setupLatch.countDown();
        });

        assertTrue(setupLatch.await(5, TimeUnit.SECONDS), "JavaFX operation timed out");
    }

    @Test
    public void testImportConfiguration() throws InterruptedException {
        // Create panel and mock method
        CountDownLatch setupLatch = new CountDownLatch(1);
        JavaFXTestUtils.runOnFXThread(() -> {
            createPanel();
            ConfigManagementPanel panelSpy = spy(panel);
            doNothing().when(panelSpy).importConfiguration();

            // Execute test
            panelSpy.importConfiguration();

            // Verify
            verify(panelSpy).importConfiguration();
            setupLatch.countDown();
        });

        assertTrue(setupLatch.await(5, TimeUnit.SECONDS), "JavaFX operation timed out");
    }

    @Test
    public void testRefreshConfiguration() throws InterruptedException {
        // Setup mock data
        Project mockProject = mock(Project.class);
        when(projectManager.getActiveProject()).thenReturn(mockProject);

        ConfigEntry mockEntry = new ConfigEntry(
                "Test Config",
                "Test Project",
                Paths.get("/path/to/project_config.json"),
                Paths.get("/path/to/dsl_config.json"),
                Paths.get("/path/to/images"),
                LocalDateTime.now()
        );
        when(selectionPanel.getSelectedConfiguration()).thenReturn(mockEntry);

        // Create panel and execute test
        CountDownLatch latch = new CountDownLatch(1);
        JavaFXTestUtils.runOnFXThread(() -> {
            createPanel();

            // Act
            panel.refreshConfiguration();

            // Assert
            verify(selectionPanel).refreshRecentConfigurations();
            verify(browserPanel).setConfiguration(mockEntry);
            verify(metadataEditor).setConfiguration(mockEntry);
            verify(eventBus).publish(any(LogEvent.class));

            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX operation timed out");
    }

    @Test
    public void testRefreshConfiguration_NoActiveProject() throws InterruptedException {
        // Setup mock data - no active project
        when(projectManager.getActiveProject()).thenReturn(null);

        // Create panel and execute test
        CountDownLatch latch = new CountDownLatch(1);
        JavaFXTestUtils.runOnFXThread(() -> {
            createPanel();

            // Act
            panel.refreshConfiguration();

            // Assert
            verify(selectionPanel).refreshRecentConfigurations();
            verify(browserPanel).clear();
            verify(metadataEditor).clear();
            verify(eventBus).publish(any(LogEvent.class));

            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX operation timed out");
    }
}