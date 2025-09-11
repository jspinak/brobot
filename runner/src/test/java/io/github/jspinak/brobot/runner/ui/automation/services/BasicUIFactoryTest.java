package io.github.jspinak.brobot.runner.ui.automation.services;

import static org.junit.jupiter.api.Assertions.*;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicUIFactoryTest {

    private BasicUIFactory factory;

    @BeforeEach
    void setUp() {
        factory = new BasicUIFactory();
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @Test
    void testCreateMainPanel() {
        // When
        VBox mainPanel = factory.createMainPanel();

        // Then
        assertNotNull(mainPanel);
        assertEquals(new Insets(20), mainPanel.getPadding());
        assertEquals(10, mainPanel.getSpacing());
        assertTrue(mainPanel.getStyleClass().contains("automation-panel"));
    }

    @Test
    void testCreateMainPanelWithCustomConfiguration() {
        // Given
        BasicUIFactory.UIConfiguration config =
                BasicUIFactory.UIConfiguration.builder().panelPadding(30).panelSpacing(15).build();
        factory.setConfiguration(config);

        // When
        VBox mainPanel = factory.createMainPanel();

        // Then
        assertEquals(new Insets(30), mainPanel.getPadding());
        assertEquals(15, mainPanel.getSpacing());
    }

    @Test
    void testCreateTitleLabel() {
        // When
        Label titleLabel = factory.createTitleLabel();

        // Then
        assertNotNull(titleLabel);
        assertEquals("Automation Control", titleLabel.getText());
        assertTrue(titleLabel.getStyleClass().contains("title-label"));
        assertTrue(titleLabel.getStyle().contains("-fx-font-size: 16"));
        assertTrue(titleLabel.getStyle().contains("-fx-font-weight: bold"));
    }

    @Test
    void testCreateControlBar() {
        // When
        BasicUIFactory.ControlBarSection controlBar = factory.createControlBar();

        // Then
        assertNotNull(controlBar);
        assertNotNull(controlBar.getContainer());
        assertNotNull(controlBar.getRefreshButton());
        assertNotNull(controlBar.getPauseResumeButton());
        assertNotNull(controlBar.getStopAllButton());

        // Verify button properties
        assertEquals("Refresh Automation Buttons", controlBar.getRefreshButton().getText());
        assertEquals("refreshAutomationButtons", controlBar.getRefreshButton().getId());

        assertEquals("Pause Execution", controlBar.getPauseResumeButton().getText());
        assertEquals("pauseResumeExecution", controlBar.getPauseResumeButton().getId());
        assertTrue(controlBar.getPauseResumeButton().isDisable());

        assertEquals("Stop All Automation", controlBar.getStopAllButton().getText());
        assertEquals("stopAllAutomation", controlBar.getStopAllButton().getId());

        // Verify container
        assertTrue(controlBar.getContainer().getStyleClass().contains("control-bar"));
        assertEquals(10, controlBar.getContainer().getSpacing());
    }

    @Test
    void testCreateStatusSection() {
        // When
        BasicUIFactory.StatusSection statusSection = factory.createStatusSection();

        // Then
        assertNotNull(statusSection);
        assertNotNull(statusSection.getContainer());
        assertNotNull(statusSection.getStatusLabel());
        assertNotNull(statusSection.getProgressBar());

        // Verify label
        assertEquals("Status: Ready", statusSection.getStatusLabel().getText());
        assertTrue(statusSection.getStatusLabel().getStyleClass().contains("status-label"));
        assertTrue(statusSection.getStatusLabel().getStyle().contains("-fx-font-weight: bold"));

        // Verify progress bar
        assertEquals(0.0, statusSection.getProgressBar().getProgress());
        assertEquals(Double.MAX_VALUE, statusSection.getProgressBar().getPrefWidth());

        // Verify container
        VBox container = statusSection.getContainer();
        assertTrue(container.getStyleClass().contains("content-section"));
        assertEquals(5, container.getSpacing());
        assertEquals(new Insets(5), container.getPadding());
        assertNotNull(container.getBorder());
    }

    @Test
    void testCreateButtonPane() {
        // When
        BasicUIFactory.ButtonPaneSection buttonPane = factory.createButtonPane();

        // Then
        assertNotNull(buttonPane);
        assertNotNull(buttonPane.getButtonPane());
        assertNotNull(buttonPane.getScrollPane());

        // Verify flow pane
        FlowPane flowPane = buttonPane.getButtonPane();
        assertTrue(flowPane.getStyleClass().contains("button-pane"));
        assertEquals(new Insets(10), flowPane.getPadding());
        assertEquals(10, flowPane.getHgap());
        assertEquals(10, flowPane.getVgap());
        assertNotNull(flowPane.getBorder());

        // Verify scroll pane
        ScrollPane scrollPane = buttonPane.getScrollPane();
        assertTrue(scrollPane.isFitToWidth());
        assertEquals(200, scrollPane.getPrefHeight());
        assertEquals(flowPane, scrollPane.getContent());
    }

    @Test
    void testCreateLogArea() {
        // When
        TextArea logArea = factory.createLogArea();

        // Then
        assertNotNull(logArea);
        assertFalse(logArea.isEditable());
        assertTrue(logArea.isWrapText());
        assertEquals(300, logArea.getPrefHeight());
    }

    @Test
    void testCreateLogAreaWithCustomHeight() {
        // Given
        BasicUIFactory.UIConfiguration config =
                BasicUIFactory.UIConfiguration.builder().logAreaHeight(500).build();
        factory.setConfiguration(config);

        // When
        TextArea logArea = factory.createLogArea();

        // Then
        assertEquals(500, logArea.getPrefHeight());
    }

    @Test
    void testCreateSectionLabel() {
        // When
        Label label = factory.createSectionLabel("Test Section");

        // Then
        assertNotNull(label);
        assertEquals("Test Section", label.getText());
        assertTrue(label.getStyleClass().contains("section-label"));
    }

    @Test
    void testCreateSeparator() {
        // When
        Separator separator = factory.createSeparator();

        // Then
        assertNotNull(separator);
    }

    @Test
    void testAssembleUI() {
        // Given
        VBox mainPanel = factory.createMainPanel();
        Label titleLabel = factory.createTitleLabel();
        BasicUIFactory.StatusSection statusSection = factory.createStatusSection();
        BasicUIFactory.ControlBarSection controlBar = factory.createControlBar();
        BasicUIFactory.ButtonPaneSection buttonPane = factory.createButtonPane();
        TextArea logArea = factory.createLogArea();

        // When
        BasicUIFactory.AssembledUI assembled =
                factory.assembleUI(
                        mainPanel, titleLabel, statusSection, controlBar, buttonPane, logArea);

        // Then
        assertNotNull(assembled);
        assertEquals(mainPanel, assembled.getMainPanel());
        assertEquals(titleLabel, assembled.getTitleLabel());
        assertEquals(statusSection, assembled.getStatusSection());
        assertEquals(controlBar, assembled.getControlBar());
        assertEquals(buttonPane, assembled.getButtonPane());
        assertEquals(logArea, assembled.getLogArea());

        // Verify main panel has all components
        assertEquals(8, mainPanel.getChildren().size());

        // Verify order
        assertEquals(titleLabel, mainPanel.getChildren().get(0));
        assertTrue(mainPanel.getChildren().get(1) instanceof Separator);
        assertEquals(statusSection.getContainer(), mainPanel.getChildren().get(2));
        assertEquals(controlBar.getContainer(), mainPanel.getChildren().get(3));
        assertTrue(mainPanel.getChildren().get(4) instanceof Label);
        assertEquals(
                "Available Automation Functions:",
                ((Label) mainPanel.getChildren().get(4)).getText());
        assertEquals(buttonPane.getScrollPane(), mainPanel.getChildren().get(5));
        assertTrue(mainPanel.getChildren().get(6) instanceof Label);
        assertEquals("Automation Log:", ((Label) mainPanel.getChildren().get(6)).getText());
        assertEquals(logArea, mainPanel.getChildren().get(7));
    }

    @Test
    void testFullConfiguration() {
        // Given
        BasicUIFactory.UIConfiguration config =
                BasicUIFactory.UIConfiguration.builder()
                        .panelPadding(25)
                        .panelSpacing(12)
                        .controlBarSpacing(15)
                        .statusBoxSpacing(8)
                        .buttonScrollHeight(250)
                        .logAreaHeight(400)
                        .build();

        factory.setConfiguration(config);

        // When - Create all components
        VBox mainPanel = factory.createMainPanel();
        assertEquals(new Insets(25), mainPanel.getPadding());
        assertEquals(12, mainPanel.getSpacing());

        BasicUIFactory.ControlBarSection controlBar = factory.createControlBar();
        assertEquals(15, controlBar.getContainer().getSpacing());

        BasicUIFactory.StatusSection statusSection = factory.createStatusSection();
        assertEquals(8, statusSection.getContainer().getSpacing());

        BasicUIFactory.ButtonPaneSection buttonPane = factory.createButtonPane();
        assertEquals(250, buttonPane.getScrollPane().getPrefHeight());

        TextArea logArea = factory.createLogArea();
        assertEquals(400, logArea.getPrefHeight());
    }

    @Test
    void testComponentAccessors() {
        // Given
        VBox mainPanel = factory.createMainPanel();
        Label titleLabel = factory.createTitleLabel();
        BasicUIFactory.StatusSection statusSection = factory.createStatusSection();
        BasicUIFactory.ControlBarSection controlBar = factory.createControlBar();
        BasicUIFactory.ButtonPaneSection buttonPane = factory.createButtonPane();
        TextArea logArea = factory.createLogArea();

        BasicUIFactory.AssembledUI assembled =
                factory.assembleUI(
                        mainPanel, titleLabel, statusSection, controlBar, buttonPane, logArea);

        // Test all getters work correctly
        assertSame(mainPanel, assembled.getMainPanel());
        assertSame(titleLabel, assembled.getTitleLabel());
        assertSame(statusSection, assembled.getStatusSection());
        assertSame(controlBar, assembled.getControlBar());
        assertSame(buttonPane, assembled.getButtonPane());
        assertSame(logArea, assembled.getLogArea());

        // Test nested component accessors
        assertSame(statusSection.getContainer(), assembled.getStatusSection().getContainer());
        assertSame(statusSection.getStatusLabel(), assembled.getStatusSection().getStatusLabel());
        assertSame(statusSection.getProgressBar(), assembled.getStatusSection().getProgressBar());

        assertSame(controlBar.getContainer(), assembled.getControlBar().getContainer());
        assertSame(controlBar.getRefreshButton(), assembled.getControlBar().getRefreshButton());
        assertSame(
                controlBar.getPauseResumeButton(),
                assembled.getControlBar().getPauseResumeButton());
        assertSame(controlBar.getStopAllButton(), assembled.getControlBar().getStopAllButton());

        assertSame(buttonPane.getButtonPane(), assembled.getButtonPane().getButtonPane());
        assertSame(buttonPane.getScrollPane(), assembled.getButtonPane().getScrollPane());
    }
}
