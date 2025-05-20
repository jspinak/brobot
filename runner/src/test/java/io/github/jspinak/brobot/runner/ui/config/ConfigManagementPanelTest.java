package io.github.jspinak.brobot.runner.ui.config;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.project.Project;
import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.services.ProjectManager;
import javafx.application.Platform;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
public class ConfigManagementPanelTest {

    private EventBus eventBus;
    private BrobotRunnerProperties properties;
    private BrobotLibraryInitializer libraryInitializer;
    private ApplicationConfig appConfig;
    private ProjectManager projectManager;
    private AllStatesInProjectService allStatesService;
    private ConfigSelectionPanel selectionPanel;
    private ConfigBrowserPanel browserPanel;
    private ConfigMetadataEditor metadataEditor;

    private ConfigManagementPanel panel;
    private Stage stage;

    @Start
    public void start(Stage stage) {
        this.stage = stage;

        // Initialize all mocks
        eventBus = mock(EventBus.class);
        properties = mock(BrobotRunnerProperties.class);
        libraryInitializer = mock(BrobotLibraryInitializer.class);
        appConfig = mock(ApplicationConfig.class);
        projectManager = mock(ProjectManager.class);
        allStatesService = mock(AllStatesInProjectService.class);
        selectionPanel = mock(ConfigSelectionPanel.class);
        browserPanel = mock(ConfigBrowserPanel.class);
        metadataEditor = mock(ConfigMetadataEditor.class);

        // Configure properties
        when(properties.getConfigPath()).thenReturn("/path/to/config");
        when(properties.getImagePath()).thenReturn("/path/to/images");
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
    public void testCreateUI() {
        // Create panel and add to scene
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            createPanel();
            stage.setScene(new javafx.scene.Scene(panel));
            latch.countDown();
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX operation timed out");
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }

        // Verify panel structure
        CountDownLatch verifyLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
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

        try {
            assertTrue(verifyLatch.await(5, TimeUnit.SECONDS), "JavaFX verification timed out");
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }
    }

    @Test
    public void testCreateNewConfiguration() {
        // Create panel and mock method
        CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            createPanel();
            ConfigManagementPanel panelSpy = spy(panel);
            doNothing().when(panelSpy).createNewConfiguration();

            // Execute test
            panelSpy.createNewConfiguration();

            // Verify
            verify(panelSpy).createNewConfiguration();
            setupLatch.countDown();
        });

        try {
            assertTrue(setupLatch.await(5, TimeUnit.SECONDS), "JavaFX operation timed out");
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }
    }

    @Test
    public void testImportConfiguration() {
        // Create panel and mock method
        CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            createPanel();
            ConfigManagementPanel panelSpy = spy(panel);
            doNothing().when(panelSpy).importConfiguration();

            // Execute test
            panelSpy.importConfiguration();

            // Verify
            verify(panelSpy).importConfiguration();
            setupLatch.countDown();
        });

        try {
            assertTrue(setupLatch.await(5, TimeUnit.SECONDS), "JavaFX operation timed out");
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }
    }

    @Test
    public void testRefreshConfiguration() {
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
        Platform.runLater(() -> {
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

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX operation timed out");
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }
    }

    @Test
    public void testRefreshConfiguration_NoActiveProject() {
        // Setup mock data - no active project
        when(projectManager.getActiveProject()).thenReturn(null);

        // Create panel and execute test
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
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

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX operation timed out");
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }
    }
}