package io.github.jspinak.brobot.runner.ui.config;

import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.components.EnhancedTable;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
public class ConfigSelectionPanelTest {

    private EventBus eventBus;
    private BrobotRunnerProperties properties;
    private BrobotLibraryInitializer libraryInitializer;
    private ApplicationConfig appConfig;
    private ConfigDetailsPanel detailsPanel;
    private EnhancedTable<ConfigEntry> mockTable;
    private ConfigSelectionPanel panel;
    private Stage stage;

    @Start
    public void start(Stage stage) {
        this.stage = stage;

        // Initialize mocks
        eventBus = mock(EventBus.class);
        properties = mock(BrobotRunnerProperties.class);
        libraryInitializer = mock(BrobotLibraryInitializer.class);
        appConfig = mock(ApplicationConfig.class);
        detailsPanel = mock(ConfigDetailsPanel.class);
        mockTable = mock(EnhancedTable.class);

        // Configure mocks
        when(properties.getConfigPath()).thenReturn("/path/to/config");
        when(properties.getImagePath()).thenReturn("/path/to/images");

        // This is important - we need to set up this mock before creating the panel
        // since it will be called during panel initialization
        when(appConfig.getString(eq("recentConfigurations"), any())).thenReturn("{}");
    }

    private void createPanel() {
        // Create a fresh panel instance for each test
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                // Create panel
                panel = new ConfigSelectionPanel(
                        eventBus,
                        properties,
                        libraryInitializer,
                        appConfig
                );

                // Inject mock table to avoid real table issues
                Field tableField = ConfigSelectionPanel.class.getDeclaredField("recentConfigsTable");
                tableField.setAccessible(true);
                tableField.set(panel, mockTable);

                // Set mock details panel
                Field detailsPanelField = ConfigSelectionPanel.class.getDeclaredField("detailsPanel");
                detailsPanelField.setAccessible(true);
                detailsPanelField.set(panel, detailsPanel);

                // Set up table view mock
                TableView<ConfigEntry> tableView = mock(TableView.class);
                when(mockTable.getTableView()).thenReturn(tableView);

                // Set the scene
                stage.setScene(new Scene(panel, 800, 600));
                stage.show();

                latch.countDown();
            } catch (Exception e) {
                fail("Failed to create panel: " + e.getMessage());
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX operation timed out");
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }
    }

    @Test
    public void testAddRecentConfiguration() throws Exception {
        // Create panel
        createPanel();

        // Create test configuration
        ConfigEntry config = new ConfigEntry(
                "Test Config",
                "Test Project",
                Paths.get("/path/to/project_config.json"),
                Paths.get("/path/to/dsl_config.json"),
                Paths.get("/path/to/images"),
                LocalDateTime.now()
        );

        // Execute test on JavaFX thread
        CountDownLatch testLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Access the recentConfigs list using reflection
                Field recentConfigsField = ConfigSelectionPanel.class.getDeclaredField("recentConfigs");
                recentConfigsField.setAccessible(true);

                @SuppressWarnings("unchecked")
                List<ConfigEntry> recentConfigs = (List<ConfigEntry>) recentConfigsField.get(panel);
                if (recentConfigs == null) {
                    recentConfigs = new ArrayList<>();
                    recentConfigsField.set(panel, recentConfigs);
                }

                // Initial size
                int initialSize = recentConfigs.size();

                // Act
                panel.addRecentConfiguration(config);

                // Assert
                assertEquals(initialSize + 1, recentConfigs.size(), "Config should be added to list");
                assertEquals(config, recentConfigs.getFirst(), "Config should be at the start of the list");

                // Verify appConfig was updated
                verify(appConfig, atLeastOnce()).setString(eq("recentConfigurations"), anyString());

                testLatch.countDown();
            } catch (Exception e) {
                fail("Exception during test: " + e.getMessage());
            }
        });

        assertTrue(testLatch.await(5, TimeUnit.SECONDS), "Test operation timed out");
    }

    @Test
    public void testRefreshRecentConfigurations() throws Exception {
        // Create panel (will call loadRecentConfigurations once during init)
        createPanel();

        // Reset mock to clear the initial call count
        reset(appConfig);
        when(appConfig.getString(eq("recentConfigurations"), any())).thenReturn("{}");

        // Execute test on JavaFX thread
        CountDownLatch testLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Act
                panel.refreshRecentConfigurations();

                // Assert
                // Verify getString was called exactly once by refreshRecentConfigurations
                verify(appConfig, times(1)).getString(eq("recentConfigurations"), any());

                testLatch.countDown();
            } catch (Exception e) {
                fail("Exception during test: " + e.getMessage());
            }
        });

        assertTrue(testLatch.await(5, TimeUnit.SECONDS), "Test operation timed out");
    }

    @Test
    public void testGetSelectedConfiguration() throws Exception {
        // Create panel
        createPanel();

        // Create a mock TableView and selection model
        TableView<ConfigEntry> tableView = mock(TableView.class);
        javafx.scene.control.TableView.TableViewSelectionModel<ConfigEntry> selectionModel =
                mock(javafx.scene.control.TableView.TableViewSelectionModel.class);

        // Configure mocks
        when(mockTable.getTableView()).thenReturn(tableView);
        when(tableView.getSelectionModel()).thenReturn(selectionModel);

        // Create test config
        ConfigEntry config = new ConfigEntry(
                "Test Config",
                "Test Project",
                Paths.get("/path/to/project_config.json"),
                Paths.get("/path/to/dsl_config.json"),
                Paths.get("/path/to/images"),
                LocalDateTime.now()
        );

        // Execute test on JavaFX thread
        CountDownLatch testLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Test with no selection
                when(selectionModel.getSelectedItem()).thenReturn(null);
                assertNull(panel.getSelectedConfiguration());

                // Test with selection
                when(selectionModel.getSelectedItem()).thenReturn(config);
                assertEquals(config, panel.getSelectedConfiguration());

                testLatch.countDown();
            } catch (Exception e) {
                fail("Exception during test: " + e.getMessage());
            }
        });

        assertTrue(testLatch.await(5, TimeUnit.SECONDS), "Test operation timed out");
    }

    @Test
    public void testShowConfigDetails() throws Exception {
        // Create panel
        createPanel();

        // Create test config
        ConfigEntry config = new ConfigEntry(
                "Test Config",
                "Test Project",
                Paths.get("/path/to/project_config.json"),
                Paths.get("/path/to/dsl_config.json"),
                Paths.get("/path/to/images"),
                LocalDateTime.now()
        );

        // Execute test on JavaFX thread
        CountDownLatch testLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Access the private method
                java.lang.reflect.Method showDetailsMethod = ConfigSelectionPanel.class.getDeclaredMethod(
                        "showConfigDetails", ConfigEntry.class);
                showDetailsMethod.setAccessible(true);

                // Act
                showDetailsMethod.invoke(panel, config);

                // Assert
                verify(detailsPanel).setConfiguration(config);

                testLatch.countDown();
            } catch (Exception e) {
                fail("Exception during test: " + e.getMessage());
            }
        });

        assertTrue(testLatch.await(5, TimeUnit.SECONDS), "Test operation timed out");
    }

    @Test
    public void testLoadConfiguration() throws Exception {
        // Create panel
        createPanel();

        // Create test config
        ConfigEntry config = new ConfigEntry(
                "Test Config",
                "Test Project",
                Paths.get("/path/to/project_config.json"),
                Paths.get("/path/to/dsl_config.json"),
                Paths.get("/path/to/images"),
                LocalDateTime.now()
        );

        // Setup mock
        when(libraryInitializer.initializeWithConfig(any(Path.class), any(Path.class))).thenReturn(true);

        // Execute test on JavaFX thread
        CountDownLatch testLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Access the private loadConfiguration method
                java.lang.reflect.Method loadConfigMethod = ConfigSelectionPanel.class.getDeclaredMethod(
                        "loadConfiguration", ConfigEntry.class);
                loadConfigMethod.setAccessible(true);

                // Access the recentConfigs list to add our test config
                Field recentConfigsField = ConfigSelectionPanel.class.getDeclaredField("recentConfigs");
                recentConfigsField.setAccessible(true);

                @SuppressWarnings("unchecked")
                List<ConfigEntry> recentConfigs = (List<ConfigEntry>) recentConfigsField.get(panel);
                if (recentConfigs == null) {
                    recentConfigs = new ArrayList<>();
                    recentConfigsField.set(panel, recentConfigs);
                }

                // Add config to the list
                recentConfigs.add(config);

                // Act
                loadConfigMethod.invoke(panel, config);

                // Assert
                verify(libraryInitializer).initializeWithConfig(
                        config.getProjectConfigPath(), config.getDslConfigPath());
                assertEquals(config, recentConfigs.getFirst(), "Config should be moved to the front of the list");

                testLatch.countDown();
            } catch (Exception e) {
                fail("Exception during test: " + e.getMessage());
            }
        });

        assertTrue(testLatch.await(5, TimeUnit.SECONDS), "Test operation timed out");
    }
}