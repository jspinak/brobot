package io.github.jspinak.brobot.runner.ui.config;

import lombok.Data;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.testutil.JavaFXTestUtils;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Data
public class ConfigBrowserPanelTest {

    @BeforeAll
    public static void initJavaFX() throws InterruptedException {
        JavaFXTestUtils.initJavaFX();
    }

    @Mock
    private EventBus eventBus;

    @Mock
    private AutomationProjectManager projectManager;

    @Mock
    private StateService allStatesService;

    private ConfigBrowserPanel browserPanel;

    @BeforeEach
    public void setUp() throws InterruptedException {
        JavaFXTestUtils.runOnFXThread(() -> {
            browserPanel = new ConfigBrowserPanel(eventBus, projectManager, allStatesService);
        });
    }

    @Test
    public void testSetConfiguration() throws IOException {
        // Arrange
        ConfigEntry config = new ConfigEntry(
                "Test Config",
                "Test Project",
                Paths.get("/path/to/project_config.json"),
                Paths.get("/path/to/dsl_config.json"),
                Paths.get("/path/to/images"),
                LocalDateTime.now()
        );

        AutomationProject mockProject = mock(AutomationProject.class);
        when(projectManager.getActiveProject()).thenReturn(mockProject);

        List<State> mockStates = new ArrayList<>();
        State mockState = mock(State.class);
        when(mockState.getName()).thenReturn("TestState");
        when(mockState.getId()).thenReturn(1L); // Set a valid ID
        when(mockState.getStateImages()).thenReturn(Collections.emptySet());
        mockStates.add(mockState);

        when(allStatesService.getAllStates()).thenReturn(mockStates);

        // Act
        browserPanel.setConfiguration(config);

        // Assert
        verify(eventBus).publish(any(LogEvent.class));
        assertTrue(browserPanel.navigateToState("TestState"));
    }

    @Test
    public void testNavigateToState() throws Exception {
        // Arrange
        // Add a state to the stateItems map using reflection
        Map<String, TreeItem<ConfigBrowserPanel.ConfigItem>> stateItems = getStateItemsMap();

        TreeItem<ConfigBrowserPanel.ConfigItem> mockItem = new TreeItem<>(
                new ConfigBrowserPanel.ConfigItem("TestState", ConfigBrowserPanel.ConfigItemType.STATE)
        );
        TreeItem<ConfigBrowserPanel.ConfigItem> parentItem = new TreeItem<>(
                new ConfigBrowserPanel.ConfigItem("States", ConfigBrowserPanel.ConfigItemType.FOLDER)
        );
        parentItem.getChildren().add(mockItem);

        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = getRootItem();
        rootItem.getChildren().add(parentItem);

        stateItems.put("TestState", mockItem);

        // Act
        boolean result = browserPanel.navigateToState("TestState");
        boolean nonExistentResult = browserPanel.navigateToState("NonExistentState");

        // Assert
        assertTrue(result, "Should return true for existing state");
        assertFalse(nonExistentResult, "Should return false for non-existent state");
    }

    @Test
    public void testClear() throws Exception {
        // Arrange
        ConfigEntry config = new ConfigEntry(
                "Test Config",
                "Test Project",
                Paths.get("/path/to/project_config.json"),
                Paths.get("/path/to/dsl_config.json"),
                Paths.get("/path/to/images"),
                LocalDateTime.now()
        );

        AutomationProject mockProject = mock(AutomationProject.class);
        when(projectManager.getActiveProject()).thenReturn(mockProject);

        List<State> mockStates = new ArrayList<>();
        State mockState = mock(State.class);
        when(mockState.getName()).thenReturn("TestState");
        when(mockState.getStateImages()).thenReturn(Collections.emptySet());
        mockStates.add(mockState);

        when(allStatesService.getAllStates()).thenReturn(mockStates);

        // First, set a configuration to populate the panel
        browserPanel.setConfiguration(config);

        // Get references to private fields for verification
        Map<String, TreeItem<ConfigBrowserPanel.ConfigItem>> stateItems = getStateItemsMap();
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = getRootItem();
        TextArea detailsTextArea = getDetailsTextArea();

        // Verify panel is populated
        assertFalse(stateItems.isEmpty(), "State items map should be populated");
        assertFalse(rootItem.getChildren().isEmpty(), "Root item should have children");

        // Act
        browserPanel.clear();

        // Assert
        assertTrue(stateItems.isEmpty(), "State items map should be empty after clear");
        assertEquals(0, rootItem.getChildren().size(), "Root item should have no children after clear");
        assertEquals("", detailsTextArea.getText(), "Details text area should be empty after clear");
    }

    // Helper methods to access private fields for testing
    @SuppressWarnings("unchecked")
    private Map<String, TreeItem<ConfigBrowserPanel.ConfigItem>> getStateItemsMap() throws Exception {
        Field field = ConfigBrowserPanel.class.getDeclaredField("stateItems");
        field.setAccessible(true);
        return (Map<String, TreeItem<ConfigBrowserPanel.ConfigItem>>) field.get(browserPanel);
    }

    private TreeItem<ConfigBrowserPanel.ConfigItem> getRootItem() throws Exception {
        Field field = ConfigBrowserPanel.class.getDeclaredField("rootItem");
        field.setAccessible(true);
        return (TreeItem<ConfigBrowserPanel.ConfigItem>) field.get(browserPanel);
    }

    private TreeView<ConfigBrowserPanel.ConfigItem> getConfigTree() throws Exception {
        Field field = ConfigBrowserPanel.class.getDeclaredField("configTree");
        field.setAccessible(true);
        return (TreeView<ConfigBrowserPanel.ConfigItem>) field.get(browserPanel);
    }

    private TextArea getDetailsTextArea() throws Exception {
        Field field = ConfigBrowserPanel.class.getDeclaredField("detailsTextArea");
        field.setAccessible(true);
        return (TextArea) field.get(browserPanel);
    }
}