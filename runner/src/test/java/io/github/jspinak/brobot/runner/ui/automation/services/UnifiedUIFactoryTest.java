package io.github.jspinak.brobot.runner.ui.automation.services;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.ui.AutomationStatusPanel;

@ExtendWith(MockitoExtension.class)
class UnifiedUIFactoryTest {

    @Mock private HotkeyManager hotkeyManager;

    private UnifiedUIFactory factory;

    @BeforeEach
    void setUp() {
        factory = new UnifiedUIFactory();
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @Test
    void testCreateTitleLabel() {
        // When
        Label titleLabel = factory.createTitleLabel();

        // Then
        assertNotNull(titleLabel);
        assertEquals("Automation Control", titleLabel.getText());
        assertTrue(titleLabel.getStyleClass().contains("title-label"));
        assertTrue(titleLabel.getStyle().contains("-fx-font-size: 18"));
        assertTrue(titleLabel.getStyle().contains("-fx-font-weight: bold"));
    }

    @Test
    void testCreateTitleLabelWithCustomConfiguration() {
        // Given
        UnifiedUIFactory.UIConfiguration config =
                UnifiedUIFactory.UIConfiguration.builder()
                        .titleText("Custom Title")
                        .titleFontSize(24)
                        .build();
        factory.setConfiguration(config);

        // When
        Label titleLabel = factory.createTitleLabel();

        // Then
        assertEquals("Custom Title", titleLabel.getText());
        assertTrue(titleLabel.getStyle().contains("-fx-font-size: 24"));
    }

    @Test
    void testCreateControlBar() {
        // When
        UnifiedUIFactory.ControlBar controlBar = factory.createControlBar();

        // Then
        assertNotNull(controlBar);
        assertNotNull(controlBar.getContainer());
        assertNotNull(controlBar.getRefreshButton());
        assertNotNull(controlBar.getPauseResumeButton());
        assertNotNull(controlBar.getStopAllButton());
        assertNotNull(controlBar.getConfigureHotkeysButton());
        assertNotNull(controlBar.getAutoMinimizeCheckBox());

        // Verify button properties
        assertEquals("Refresh", controlBar.getRefreshButton().getText());
        assertEquals("refreshAutomationButtons", controlBar.getRefreshButton().getId());

        assertEquals("Pause", controlBar.getPauseResumeButton().getText());
        assertEquals("pauseResumeExecution", controlBar.getPauseResumeButton().getId());
        assertTrue(controlBar.getPauseResumeButton().isDisable());

        assertEquals("Stop All", controlBar.getStopAllButton().getText());
        assertEquals("stopAllAutomation", controlBar.getStopAllButton().getId());
        assertTrue(controlBar.getStopAllButton().getStyleClass().contains("danger"));

        assertEquals("⌨ Hotkeys", controlBar.getConfigureHotkeysButton().getText());
        assertEquals("Auto-minimize", controlBar.getAutoMinimizeCheckBox().getText());

        // Verify container
        HBox container = controlBar.getContainer();
        assertTrue(container.getStyleClass().contains("control-bar"));
        assertEquals(10, container.getSpacing());
    }

    @Test
    void testCreateProgressSection() {
        // When
        UnifiedUIFactory.ProgressSection progressSection = factory.createProgressSection();

        // Then
        assertNotNull(progressSection);
        assertNotNull(progressSection.getContainer());
        assertNotNull(progressSection.getStatusLabel());
        assertNotNull(progressSection.getProgressBar());

        // Verify status label
        assertEquals("Status: Ready", progressSection.getStatusLabel().getText());
        assertTrue(progressSection.getStatusLabel().getStyleClass().contains("status-label"));
        assertTrue(progressSection.getStatusLabel().getStyle().contains("-fx-font-weight: bold"));

        // Verify progress bar
        assertEquals(0.0, progressSection.getProgressBar().getProgress());
        assertEquals(Double.MAX_VALUE, progressSection.getProgressBar().getPrefWidth());

        // Verify container
        VBox container = progressSection.getContainer();
        assertTrue(container.getStyleClass().contains("content-section"));
        assertEquals(5, container.getSpacing());
    }

    @Test
    void testCreateButtonPane() {
        // When
        UnifiedUIFactory.ButtonPane buttonPane = factory.createButtonPane();

        // Then
        assertNotNull(buttonPane);
        assertNotNull(buttonPane.getFlowPane());
        assertNotNull(buttonPane.getScrollPane());

        // Verify flow pane
        assertTrue(buttonPane.getFlowPane().getStyleClass().contains("button-pane"));
        assertEquals(10, buttonPane.getFlowPane().getHgap());
        assertEquals(10, buttonPane.getFlowPane().getVgap());

        // Verify scroll pane
        assertTrue(buttonPane.getScrollPane().isFitToWidth());
        assertEquals(200, buttonPane.getScrollPane().getPrefHeight());
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
        assertTrue(logArea.getStyleClass().contains("automation-log"));
    }

    @Test
    void testCreateLogAreaWithCustomHeight() {
        // Given
        UnifiedUIFactory.UIConfiguration config =
                UnifiedUIFactory.UIConfiguration.builder().logAreaHeight(500).build();
        factory.setConfiguration(config);

        // When
        TextArea logArea = factory.createLogArea();

        // Then
        assertEquals(500, logArea.getPrefHeight());
    }

    @Test
    void testCreateSectionLabel() {
        // When
        Label sectionLabel = factory.createSectionLabel("Test Section");

        // Then
        assertNotNull(sectionLabel);
        assertEquals("Test Section", sectionLabel.getText());
        assertTrue(sectionLabel.getStyleClass().contains("section-label"));
    }

    @Test
    void testCreateMainContainer() {
        // When
        VBox mainContainer = factory.createMainContainer();

        // Then
        assertNotNull(mainContainer);
        assertTrue(mainContainer.getStyleClass().contains("automation-panel"));
        assertEquals(20, mainContainer.getPadding().getTop());
        assertEquals(10, mainContainer.getSpacing());
    }

    @Test
    void testCreateMainContainerWithCustomConfiguration() {
        // Given
        UnifiedUIFactory.UIConfiguration config =
                UnifiedUIFactory.UIConfiguration.builder()
                        .sectionPadding(30)
                        .mainSpacing(15)
                        .build();
        factory.setConfiguration(config);

        // When
        VBox mainContainer = factory.createMainContainer();

        // Then
        assertEquals(30, mainContainer.getPadding().getTop());
        assertEquals(15, mainContainer.getSpacing());
    }

    @Test
    void testCreateSeparator() {
        // When
        Separator separator = factory.createSeparator();

        // Then
        assertNotNull(separator);
    }

    @Test
    void testCreateEnhancedStatusPanel() {
        // When
        AutomationStatusPanel statusPanel = factory.createEnhancedStatusPanel(hotkeyManager);

        // Then
        assertNotNull(statusPanel);
        assertEquals(80, statusPanel.getPrefHeight());
    }

    @Test
    void testAssembleMainPanel() {
        // Given
        Label titleLabel = factory.createTitleLabel();
        AutomationStatusPanel statusPanel = factory.createEnhancedStatusPanel(hotkeyManager);
        UnifiedUIFactory.ProgressSection progressSection = factory.createProgressSection();
        UnifiedUIFactory.ControlBar controlBar = factory.createControlBar();
        UnifiedUIFactory.ButtonPane buttonPane = factory.createButtonPane();
        TextArea logArea = factory.createLogArea();

        // When
        UnifiedUIFactory.AssembledPanel assembled =
                factory.assembleMainPanel(
                        titleLabel, statusPanel, progressSection, controlBar, buttonPane, logArea);

        // Then
        assertNotNull(assembled);
        assertNotNull(assembled.getMainContainer());
        assertEquals(titleLabel, assembled.getTitleLabel());
        assertEquals(statusPanel, assembled.getStatusPanel());
        assertEquals(progressSection, assembled.getProgressSection());
        assertEquals(controlBar, assembled.getControlBar());
        assertEquals(buttonPane, assembled.getButtonPane());
        assertEquals(logArea, assembled.getLogArea());

        // Verify container structure
        VBox mainContainer = assembled.getMainContainer();
        assertTrue(mainContainer.getChildren().size() >= 8); // All components + separators + labels

        // Verify order of children
        assertEquals(titleLabel, mainContainer.getChildren().get(0));
        assertTrue(mainContainer.getChildren().get(1) instanceof Separator);
        assertEquals(statusPanel, mainContainer.getChildren().get(2));
        assertEquals(progressSection.getContainer(), mainContainer.getChildren().get(3));
        assertEquals(controlBar.getContainer(), mainContainer.getChildren().get(4));
    }

    @Test
    void testUIConfigurationBuilder() {
        // When
        UnifiedUIFactory.UIConfiguration config =
                UnifiedUIFactory.UIConfiguration.builder()
                        .titleText("My Automation")
                        .titleFontSize(20)
                        .controlBarSpacing(15)
                        .mainSpacing(12)
                        .sectionPadding(25)
                        .logAreaHeight(400)
                        .buttonScrollHeight(250)
                        .build();

        factory.setConfiguration(config);

        // Then - Verify configuration is applied
        Label titleLabel = factory.createTitleLabel();
        assertEquals("My Automation", titleLabel.getText());
        assertTrue(titleLabel.getStyle().contains("-fx-font-size: 20"));

        UnifiedUIFactory.ControlBar controlBar = factory.createControlBar();
        assertEquals(15, controlBar.getContainer().getSpacing());

        VBox mainContainer = factory.createMainContainer();
        assertEquals(12, mainContainer.getSpacing());
        assertEquals(25, mainContainer.getPadding().getTop());

        TextArea logArea = factory.createLogArea();
        assertEquals(400, logArea.getPrefHeight());

        UnifiedUIFactory.ButtonPane buttonPane = factory.createButtonPane();
        assertEquals(250, buttonPane.getScrollPane().getPrefHeight());
    }
}
