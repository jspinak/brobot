package io.github.jspinak.brobot.runner.ui.automation.services;

import static org.junit.jupiter.api.Assertions.*;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;

@ExtendWith(MockitoExtension.class)
class ImprovedUILayoutFactoryTest {

    private ImprovedUILayoutFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ImprovedUILayoutFactory();
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @Test
    void testCreateButtonGroup() {
        // Given
        Button button1 = new Button("Button 1");
        Button button2 = new Button("Button 2");
        Label label = new Label("Label");

        // When
        HBox group = factory.createButtonGroup(button1, button2, label);

        // Then
        assertNotNull(group);
        assertEquals(3, group.getChildren().size());
        assertEquals(button1, group.getChildren().get(0));
        assertEquals(button2, group.getChildren().get(1));
        assertEquals(label, group.getChildren().get(2));
        assertEquals(10, group.getSpacing()); // Default spacing
    }

    @Test
    void testCreateSeparator() {
        // When
        Separator separator = factory.createSeparator();

        // Then
        assertNotNull(separator);
        assertEquals(javafx.geometry.Orientation.VERTICAL, separator.getOrientation());
    }

    @Test
    void testCreateSpacer() {
        // When
        Region spacer = factory.createSpacer();

        // Then
        assertNotNull(spacer);
        assertEquals(Priority.ALWAYS, HBox.getHgrow(spacer));
    }

    @Test
    void testCreateProjectInfo() {
        // When
        ImprovedUILayoutFactory.ProjectInfoSection projectInfo = factory.createProjectInfo();

        // Then
        assertNotNull(projectInfo);
        assertNotNull(projectInfo.getContainer());
        assertNotNull(projectInfo.getProjectLabel());

        assertEquals("No project loaded", projectInfo.getProjectLabel().getText());
        assertTrue(projectInfo.getProjectLabel().getStyleClass().contains("project-label"));
        assertEquals(200, projectInfo.getProjectLabel().getMinWidth());

        // Check container has two children
        HBox container = projectInfo.getContainer();
        assertEquals(2, container.getChildren().size());
        assertTrue(container.getChildren().get(0) instanceof Label);
        assertEquals("Project:", ((Label) container.getChildren().get(0)).getText());
    }

    @Test
    void testCreateExecutionControls() {
        // When
        ImprovedUILayoutFactory.ExecutionControls controls = factory.createExecutionControls();

        // Then
        assertNotNull(controls);
        assertNotNull(controls.getContainer());
        assertNotNull(controls.getStartButton());
        assertNotNull(controls.getPauseButton());
        assertNotNull(controls.getStopButton());

        // Verify button properties
        Button startButton = controls.getStartButton();
        assertEquals("Start", startButton.getText());
        assertTrue(startButton.getStyleClass().contains("primary"));
        assertTrue(startButton.getStyleClass().contains("control-button"));
        assertFalse(startButton.isDisable());

        Button pauseButton = controls.getPauseButton();
        assertEquals("Pause", pauseButton.getText());
        assertTrue(pauseButton.getStyleClass().contains("secondary"));
        assertTrue(pauseButton.isDisable());

        Button stopButton = controls.getStopButton();
        assertEquals("Stop", stopButton.getText());
        assertTrue(stopButton.getStyleClass().contains("danger"));
        assertTrue(stopButton.isDisable());
    }

    @Test
    void testCreateWindowControls() {
        // When
        ImprovedUILayoutFactory.WindowControls controls = factory.createWindowControls();

        // Then
        assertNotNull(controls);
        assertNotNull(controls.getContainer());
        assertNotNull(controls.getWindowControlButton());
        assertNotNull(controls.getHotkeyButton());

        assertEquals("Window", controls.getWindowControlButton().getText());
        assertEquals("Hotkeys", controls.getHotkeyButton().getText());

        assertTrue(controls.getWindowControlButton().getStyleClass().contains("secondary"));
        assertTrue(controls.getHotkeyButton().getStyleClass().contains("secondary"));
    }

    @Test
    void testCreateStatusSection() {
        // When
        ImprovedUILayoutFactory.StatusSection statusSection = factory.createStatusSection();

        // Then
        assertNotNull(statusSection);
        assertNotNull(statusSection.getContainer());
        assertNotNull(statusSection.getStatusLabel());
        assertNotNull(statusSection.getProgressBar());

        assertEquals("Ready", statusSection.getStatusLabel().getText());
        assertTrue(statusSection.getStatusLabel().getStyleClass().contains("status-label"));

        assertEquals(0.0, statusSection.getProgressBar().getProgress());
        assertEquals(200, statusSection.getProgressBar().getPrefWidth());
        assertTrue(statusSection.getProgressBar().getStyleClass().contains("progress-bar"));

        VBox container = statusSection.getContainer();
        assertEquals(4, container.getSpacing()); // Default status box spacing
    }

    @Test
    void testCreateTaskPanel() {
        // When
        ImprovedUILayoutFactory.TaskPanel taskPanel = factory.createTaskPanel();

        // Then
        assertNotNull(taskPanel);
        assertNotNull(taskPanel.getCard());
        assertNotNull(taskPanel.getScrollPane());
        assertNotNull(taskPanel.getTaskButtonsPane());

        // Verify card
        AtlantaCard card = taskPanel.getCard();
        assertEquals("Automation Tasks", card.getTitle());

        // Verify scroll pane
        ScrollPane scrollPane = taskPanel.getScrollPane();
        assertTrue(scrollPane.isFitToWidth());
        assertEquals(ScrollPane.ScrollBarPolicy.NEVER, scrollPane.getHbarPolicy());

        // Verify task buttons pane
        FlowPane buttonsPane = taskPanel.getTaskButtonsPane();
        assertEquals(12, buttonsPane.getHgap());
        assertEquals(12, buttonsPane.getVgap());
        assertEquals(new Insets(8), buttonsPane.getPadding());
        assertTrue(buttonsPane.getStyleClass().contains("task-buttons-pane"));
    }

    @Test
    void testCreateLogPanel() {
        // When
        ImprovedUILayoutFactory.LogPanel logPanel = factory.createLogPanel();

        // Then
        assertNotNull(logPanel);
        assertNotNull(logPanel.getCard());
        assertNotNull(logPanel.getContent());
        assertNotNull(logPanel.getLogArea());
        assertNotNull(logPanel.getAutoScrollCheck());
        assertNotNull(logPanel.getClearButton());

        // Verify card
        AtlantaCard card = logPanel.getCard();
        assertEquals("Execution Log", card.getTitle());

        // Verify log area
        TextArea logArea = logPanel.getLogArea();
        assertFalse(logArea.isEditable());
        assertTrue(logArea.isWrapText());
        assertTrue(logArea.getStyleClass().contains("log-area"));
        assertEquals(Priority.ALWAYS, VBox.getVgrow(logArea));

        // Verify auto-scroll checkbox
        CheckBox autoScroll = logPanel.getAutoScrollCheck();
        assertTrue(autoScroll.isSelected());
        assertEquals("Auto-scroll", autoScroll.getText());
        assertTrue(autoScroll.getStyleClass().contains("auto-scroll-check"));

        // Verify clear button
        Button clearButton = logPanel.getClearButton();
        assertEquals("Clear", clearButton.getText());
        assertTrue(clearButton.getStyleClass().contains("secondary"));
        assertTrue(clearButton.getStyleClass().contains("small"));
    }

    @Test
    void testCreateResponsiveSplitLayout() {
        // Given
        Region left = new VBox();
        Region right = new VBox();

        // When
        SplitPane splitPane = factory.createResponsiveSplitLayout(left, right);

        // Then
        assertNotNull(splitPane);
        assertEquals(2, splitPane.getItems().size());
        assertEquals(left, splitPane.getItems().get(0));
        assertEquals(right, splitPane.getItems().get(1));
        assertEquals(0.4, splitPane.getDividerPositions()[0], 0.01);

        assertEquals(300, left.getMinWidth());
        assertEquals(400, right.getMinWidth());
    }

    @Test
    void testConfiguration() {
        // Given
        ImprovedUILayoutFactory.LayoutConfiguration config =
                ImprovedUILayoutFactory.LayoutConfiguration.builder()
                        .splitPanePosition(0.6)
                        .controlBarSpacing(20)
                        .statusBoxSpacing(8)
                        .taskPaneHgap(20)
                        .taskPaneVgap(20)
                        .taskPanePadding(new Insets(16))
                        .build();

        factory.setConfiguration(config);

        // When - Create components with new configuration
        HBox buttonGroup = factory.createButtonGroup(new Button("Test"));
        assertEquals(20, buttonGroup.getSpacing());

        ImprovedUILayoutFactory.StatusSection statusSection = factory.createStatusSection();
        assertEquals(8, statusSection.getContainer().getSpacing());

        ImprovedUILayoutFactory.TaskPanel taskPanel = factory.createTaskPanel();
        assertEquals(20, taskPanel.getTaskButtonsPane().getHgap());
        assertEquals(20, taskPanel.getTaskButtonsPane().getVgap());
        assertEquals(new Insets(16), taskPanel.getTaskButtonsPane().getPadding());

        ImprovedUILayoutFactory.LogPanel logPanel = factory.createLogPanel();
        HBox logControls = (HBox) logPanel.getContent().getChildren().get(0);
        assertEquals(12, logControls.getSpacing()); // Default spacing

        SplitPane splitPane = factory.createResponsiveSplitLayout(new VBox(), new VBox());
        assertEquals(0.6, splitPane.getDividerPositions()[0], 0.01);
    }

    @Test
    void testAutoScrollBehavior() {
        // Given
        ImprovedUILayoutFactory.LogPanel logPanel = factory.createLogPanel();
        TextArea logArea = logPanel.getLogArea();
        CheckBox autoScrollCheck = logPanel.getAutoScrollCheck();

        // Initial state
        assertTrue(autoScrollCheck.isSelected());

        // When - Add text with auto-scroll enabled
        logArea.setText("Line 1\n");
        logArea.appendText("Line 2\n");

        // The listener should be attached
        assertEquals(
                1,
                logArea.textProperty().getValue().split("\n").length
                        + 1); // +1 for the trailing newline

        // When - Disable auto-scroll
        autoScrollCheck.setSelected(false);

        // Then - Text can still be added
        logArea.appendText("Line 3\n");
        assertTrue(logArea.getText().contains("Line 3"));
    }
}
