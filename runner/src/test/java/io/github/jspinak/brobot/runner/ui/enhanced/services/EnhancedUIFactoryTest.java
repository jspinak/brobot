package io.github.jspinak.brobot.runner.ui.enhanced.services;

import static org.junit.jupiter.api.Assertions.*;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.ui.AutomationStatusPanel;

@ExtendWith(MockitoExtension.class)
class EnhancedUIFactoryTest {

    @Mock private HotkeyManager hotkeyManager;

    private EnhancedUIFactory factory;

    @BeforeEach
    void setUp() {
        factory = new EnhancedUIFactory();

        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
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
        assertTrue(mainPanel.getStyleClass().contains("enhanced-automation-panel"));
    }

    @Test
    void testCreateMainPanelWithCustomConfiguration() {
        // Given
        factory.setConfiguration(
                EnhancedUIFactory.UIConfiguration.builder()
                        .panelPadding(30)
                        .panelSpacing(15)
                        .panelStyleClass("custom-panel")
                        .build());

        // When
        VBox mainPanel = factory.createMainPanel();

        // Then
        assertEquals(new Insets(30), mainPanel.getPadding());
        assertEquals(15, mainPanel.getSpacing());
        assertTrue(mainPanel.getStyleClass().contains("custom-panel"));
    }

    @Test
    void testCreateStatusPanel() {
        // When
        AutomationStatusPanel statusPanel = factory.createStatusPanel(hotkeyManager);

        // Then
        assertNotNull(statusPanel);
    }

    @Test
    void testCreateControlBar() {
        // When
        EnhancedUIFactory.ControlBarSection controlBar = factory.createControlBar();

        // Then
        assertNotNull(controlBar);
        assertNotNull(controlBar.getContainer());
        assertNotNull(controlBar.getRefreshButton());
        assertNotNull(controlBar.getPauseResumeButton());
        assertNotNull(controlBar.getStopButton());

        // Verify container
        assertTrue(controlBar.getContainer().getStyleClass().contains("control-bar"));
        assertEquals(10, controlBar.getContainer().getSpacing());

        // Verify buttons
        assertEquals("Refresh Functions", controlBar.getRefreshButton().getText());
        assertEquals("refreshAutomationButtons", controlBar.getRefreshButton().getId());

        assertEquals("Pause", controlBar.getPauseResumeButton().getText());
        assertEquals("pauseResumeExecution", controlBar.getPauseResumeButton().getId());
        assertTrue(controlBar.getPauseResumeButton().isDisable());

        assertEquals("Stop All", controlBar.getStopButton().getText());
        assertEquals("stopAllAutomation", controlBar.getStopButton().getId());
        assertTrue(controlBar.getStopButton().getStyleClass().contains("button-danger"));
    }

    @Test
    void testCreateSettingsBar() {
        // When
        EnhancedUIFactory.SettingsBarSection settingsBar = factory.createSettingsBar();

        // Then
        assertNotNull(settingsBar);
        assertNotNull(settingsBar.getContainer());
        assertNotNull(settingsBar.getConfigureHotkeysButton());
        assertNotNull(settingsBar.getAutoMinimizeCheckbox());

        // Verify container
        assertEquals(Pos.CENTER_LEFT, settingsBar.getContainer().getAlignment());
        assertEquals(15, settingsBar.getContainer().getSpacing());

        // Verify components
        assertEquals("Configure Hotkeys", settingsBar.getConfigureHotkeysButton().getText());
        assertEquals("Auto-minimize on start", settingsBar.getAutoMinimizeCheckbox().getText());
    }

    @Test
    void testCreateButtonPane() {
        // When
        FlowPane buttonPane = factory.createButtonPane();

        // Then
        assertNotNull(buttonPane);
        assertTrue(buttonPane.getStyleClass().contains("button-pane"));
        assertEquals(new Insets(10), buttonPane.getPadding());
        assertEquals(10, buttonPane.getHgap());
        assertEquals(10, buttonPane.getVgap());
        assertNotNull(buttonPane.getBorder());
    }

    @Test
    void testCreateButtonScrollPane() {
        // Given
        FlowPane buttonPane = factory.createButtonPane();

        // When
        ScrollPane scrollPane = factory.createButtonScrollPane(buttonPane);

        // Then
        assertNotNull(scrollPane);
        assertTrue(scrollPane.isFitToWidth());
        assertEquals(150, scrollPane.getPrefHeight());
        assertEquals(buttonPane, scrollPane.getContent());
    }

    @Test
    void testCreateLogArea() {
        // When
        TextArea logArea = factory.createLogArea();

        // Then
        assertNotNull(logArea);
        assertFalse(logArea.isEditable());
        assertTrue(logArea.isWrapText());
        assertEquals(200, logArea.getPrefHeight());
    }

    @Test
    void testCreateLogAreaWithCustomHeight() {
        // Given
        factory.setConfiguration(
                EnhancedUIFactory.UIConfiguration.builder().logAreaHeight(300).build());

        // When
        TextArea logArea = factory.createLogArea();

        // Then
        assertEquals(300, logArea.getPrefHeight());
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
        AutomationStatusPanel statusPanel = factory.createStatusPanel(hotkeyManager);
        EnhancedUIFactory.ControlBarSection controlBar = factory.createControlBar();
        EnhancedUIFactory.SettingsBarSection settingsBar = factory.createSettingsBar();
        FlowPane buttonPane = factory.createButtonPane();
        ScrollPane buttonScrollPane = factory.createButtonScrollPane(buttonPane);
        TextArea logArea = factory.createLogArea();

        // When
        EnhancedUIFactory.AssembledUI assembled =
                factory.assembleUI(
                        mainPanel,
                        statusPanel,
                        controlBar,
                        settingsBar,
                        buttonPane,
                        buttonScrollPane,
                        logArea);

        // Then
        assertNotNull(assembled);
        assertEquals(mainPanel, assembled.getMainPanel());
        assertEquals(statusPanel, assembled.getStatusPanel());
        assertEquals(controlBar, assembled.getControlBar());
        assertEquals(settingsBar, assembled.getSettingsBar());
        assertEquals(buttonPane, assembled.getButtonPane());
        assertEquals(buttonScrollPane, assembled.getButtonScrollPane());
        assertEquals(logArea, assembled.getLogArea());

        // Verify main panel has all components
        assertEquals(9, mainPanel.getChildren().size());

        // Verify order
        assertEquals(statusPanel, mainPanel.getChildren().get(0));
        assertTrue(mainPanel.getChildren().get(1) instanceof Separator);
        assertEquals(controlBar.getContainer(), mainPanel.getChildren().get(2));
        assertEquals(settingsBar.getContainer(), mainPanel.getChildren().get(3));
        assertTrue(mainPanel.getChildren().get(4) instanceof Separator);
        assertTrue(mainPanel.getChildren().get(5) instanceof Label);
        assertEquals("Automation Functions:", ((Label) mainPanel.getChildren().get(5)).getText());
        assertEquals(buttonScrollPane, mainPanel.getChildren().get(6));
        assertTrue(mainPanel.getChildren().get(7) instanceof Label);
        assertEquals("Log:", ((Label) mainPanel.getChildren().get(7)).getText());
        assertEquals(logArea, mainPanel.getChildren().get(8));
    }

    @Test
    void testFullConfiguration() {
        // Given
        EnhancedUIFactory.UIConfiguration config =
                EnhancedUIFactory.UIConfiguration.builder()
                        .panelPadding(25)
                        .panelSpacing(12)
                        .controlBarSpacing(15)
                        .settingsBarSpacing(20)
                        .buttonPanePadding(15)
                        .buttonPaneHgap(12)
                        .buttonPaneVgap(12)
                        .logAreaHeight(250)
                        .panelStyleClass("custom-panel")
                        .controlBarStyleClass("custom-control-bar")
                        .buttonPaneStyleClass("custom-button-pane")
                        .sectionLabelStyleClass("custom-section")
                        .categoryLabelStyleClass("custom-category")
                        .dangerButtonStyleClass("custom-danger")
                        .build();

        factory.setConfiguration(config);

        // When - Create all components
        VBox mainPanel = factory.createMainPanel();
        assertEquals(new Insets(25), mainPanel.getPadding());
        assertEquals(12, mainPanel.getSpacing());
        assertTrue(mainPanel.getStyleClass().contains("custom-panel"));

        EnhancedUIFactory.ControlBarSection controlBar = factory.createControlBar();
        assertEquals(15, controlBar.getContainer().getSpacing());
        assertTrue(controlBar.getContainer().getStyleClass().contains("custom-control-bar"));
        assertTrue(controlBar.getStopButton().getStyleClass().contains("custom-danger"));

        EnhancedUIFactory.SettingsBarSection settingsBar = factory.createSettingsBar();
        assertEquals(20, settingsBar.getContainer().getSpacing());

        FlowPane buttonPane = factory.createButtonPane();
        assertEquals(new Insets(15), buttonPane.getPadding());
        assertEquals(12, buttonPane.getHgap());
        assertEquals(12, buttonPane.getVgap());
        assertTrue(buttonPane.getStyleClass().contains("custom-button-pane"));

        TextArea logArea = factory.createLogArea();
        assertEquals(250, logArea.getPrefHeight());

        Label sectionLabel = factory.createSectionLabel("Test");
        assertTrue(sectionLabel.getStyleClass().contains("custom-section"));
    }

    @Test
    void testCustomBorderStroke() {
        // Given
        BorderStroke customStroke =
                new BorderStroke(
                        Color.BLUE,
                        BorderStrokeStyle.DASHED,
                        new CornerRadii(10),
                        new BorderWidths(2));

        factory.setConfiguration(
                EnhancedUIFactory.UIConfiguration.builder()
                        .buttonPaneBorderStroke(customStroke)
                        .build());

        // When
        FlowPane buttonPane = factory.createButtonPane();

        // Then
        Border border = buttonPane.getBorder();
        assertNotNull(border);
        assertEquals(1, border.getStrokes().size());
        assertEquals(customStroke, border.getStrokes().get(0));
    }
}
