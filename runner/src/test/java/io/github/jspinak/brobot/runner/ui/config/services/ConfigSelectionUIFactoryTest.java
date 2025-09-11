package io.github.jspinak.brobot.runner.ui.config.services;

import static org.junit.jupiter.api.Assertions.*;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.events.EventBus;

import atlantafx.base.theme.Styles;

@ExtendWith(MockitoExtension.class)
class ConfigSelectionUIFactoryTest {

    @Mock private EventBus eventBus;

    private ConfigSelectionUIFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ConfigSelectionUIFactory();

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
        assertTrue(mainPanel.getStyleClass().contains("config-selection-panel"));
        assertEquals(new Insets(15), mainPanel.getPadding());
        assertEquals(15, mainPanel.getSpacing());
    }

    @Test
    void testCreateMainPanelWithCustomConfiguration() {
        // Given
        factory.setConfiguration(
                ConfigSelectionUIFactory.UIConfiguration.builder()
                        .panelPadding(20)
                        .panelSpacing(10)
                        .build());

        // When
        VBox mainPanel = factory.createMainPanel();

        // Then
        assertEquals(new Insets(20), mainPanel.getPadding());
        assertEquals(10, mainPanel.getSpacing());
    }

    @Test
    void testCreateHeaderSection() {
        // When
        ConfigSelectionUIFactory.HeaderSection header = factory.createHeaderSection();

        // Then
        assertNotNull(header);
        assertNotNull(header.getTitleLabel());
        assertNotNull(header.getImportButton());
        assertNotNull(header.getBrowseButton());
        assertNotNull(header.getRefreshButton());
        assertNotNull(header.getHeaderActions());
        assertNotNull(header.getContainer());

        // Verify title
        assertEquals("Configuration Selection", header.getTitleLabel().getText());
        assertTrue(header.getTitleLabel().getStyleClass().contains("title-label"));

        // Verify buttons
        assertEquals("Import Configuration", header.getImportButton().getText());
        assertTrue(header.getImportButton().getStyleClass().contains("button-primary"));
        assertEquals("Browse Files", header.getBrowseButton().getText());
        assertEquals("Refresh", header.getRefreshButton().getText());

        // Verify header actions layout
        assertEquals(Pos.CENTER_RIGHT, header.getHeaderActions().getAlignment());
        assertEquals(3, header.getHeaderActions().getChildren().size());

        // Verify container layout
        assertEquals(Pos.CENTER_LEFT, header.getContainer().getAlignment());
        assertEquals(Priority.ALWAYS, HBox.getHgrow(header.getHeaderActions()));
    }

    @Test
    void testCreateHeaderSectionWithCustomButtonStyles() {
        // Given
        factory.setConfiguration(
                ConfigSelectionUIFactory.UIConfiguration.builder()
                        .primaryButtonClass("custom-primary")
                        .secondaryButtonClass("custom-secondary")
                        .build());

        // When
        ConfigSelectionUIFactory.HeaderSection header = factory.createHeaderSection();

        // Then
        assertTrue(header.getImportButton().getStyleClass().contains("custom-primary"));
        assertTrue(header.getBrowseButton().getStyleClass().contains("custom-secondary"));
        assertTrue(header.getRefreshButton().getStyleClass().contains("custom-secondary"));
    }

    @Test
    void testCreateSplitPaneSection() {
        // When
        ConfigSelectionUIFactory.SplitPaneSection splitPaneSection =
                factory.createSplitPaneSection(eventBus);

        // Then
        assertNotNull(splitPaneSection);
        assertNotNull(splitPaneSection.getSplitPane());
        assertNotNull(splitPaneSection.getRecentConfigsCard());
        assertNotNull(splitPaneSection.getDetailsCard());
        assertNotNull(splitPaneSection.getConfigTable());
        assertNotNull(splitPaneSection.getDetailsPanel());

        // Verify split pane
        assertTrue(splitPaneSection.getSplitPane().getStyleClass().contains("config-split-pane"));
        assertEquals(0.45, splitPaneSection.getSplitPane().getDividerPositions()[0], 0.01);

        // Verify cards
        assertTrue(
                splitPaneSection
                        .getRecentConfigsCard()
                        .getStyleClass()
                        .contains("recent-configurations-table"));
        assertTrue(
                splitPaneSection
                        .getRecentConfigsCard()
                        .getStyleClass()
                        .contains(Styles.ELEVATED_1));
        assertEquals(300, splitPaneSection.getRecentConfigsCard().getMinWidth());

        assertTrue(
                splitPaneSection
                        .getDetailsCard()
                        .getStyleClass()
                        .contains("configuration-details-card"));
        assertTrue(splitPaneSection.getDetailsCard().getStyleClass().contains(Styles.ELEVATED_1));
        assertEquals(400, splitPaneSection.getDetailsCard().getMinWidth());
    }

    @Test
    void testCreateSplitPaneSectionWithCustomConfiguration() {
        // Given
        factory.setConfiguration(
                ConfigSelectionUIFactory.UIConfiguration.builder()
                        .splitPaneDividerPosition(0.6)
                        .recentConfigsMinWidth(500)
                        .detailsMinWidth(600)
                        .cardElevation(Styles.ELEVATED_2)
                        .build());

        // When
        ConfigSelectionUIFactory.SplitPaneSection splitPaneSection =
                factory.createSplitPaneSection(eventBus);

        // Then
        assertEquals(0.6, splitPaneSection.getSplitPane().getDividerPositions()[0], 0.01);
        assertEquals(500, splitPaneSection.getRecentConfigsCard().getMinWidth());
        assertEquals(600, splitPaneSection.getDetailsCard().getMinWidth());
        assertTrue(
                splitPaneSection
                        .getRecentConfigsCard()
                        .getStyleClass()
                        .contains(Styles.ELEVATED_2));
        assertTrue(splitPaneSection.getDetailsCard().getStyleClass().contains(Styles.ELEVATED_2));
    }

    @Test
    void testAssembleUI() {
        // Given
        VBox mainPanel = factory.createMainPanel();
        ConfigSelectionUIFactory.HeaderSection header = factory.createHeaderSection();
        ConfigSelectionUIFactory.SplitPaneSection splitPane =
                factory.createSplitPaneSection(eventBus);

        // When
        ConfigSelectionUIFactory.AssembledUI assembled =
                factory.assembleUI(mainPanel, header, splitPane);

        // Then
        assertNotNull(assembled);
        assertEquals(mainPanel, assembled.getMainPanel());
        assertEquals(header, assembled.getHeader());
        assertEquals(splitPane, assembled.getSplitPane());

        // Verify main panel contains header and split pane
        assertEquals(2, mainPanel.getChildren().size());
        assertEquals(header.getContainer(), mainPanel.getChildren().get(0));
        assertEquals(splitPane.getSplitPane(), mainPanel.getChildren().get(1));
    }

    @Test
    void testCreateAlert() {
        // When
        Alert infoAlert =
                factory.createAlert(
                        Alert.AlertType.INFORMATION, "Test Title", "Test Header", "Test Content");

        // Then
        assertNotNull(infoAlert);
        assertEquals(Alert.AlertType.INFORMATION, infoAlert.getAlertType());
        assertEquals("Test Title", infoAlert.getTitle());
        assertEquals("Test Header", infoAlert.getHeaderText());
        assertEquals("Test Content", infoAlert.getContentText());
    }

    @Test
    void testConfigurationBuilder() {
        // Given
        ConfigSelectionUIFactory.UIConfiguration config =
                ConfigSelectionUIFactory.UIConfiguration.builder()
                        .panelPadding(25)
                        .panelSpacing(20)
                        .headerSpacing(15)
                        .splitPaneDividerPosition(0.5)
                        .recentConfigsMinWidth(350)
                        .detailsMinWidth(450)
                        .primaryButtonClass("btn-primary")
                        .secondaryButtonClass("btn-secondary")
                        .dangerButtonClass("btn-danger")
                        .cardElevation(Styles.ELEVATED_3)
                        .build();

        // Then
        assertEquals(25, config.getPanelPadding());
        assertEquals(20, config.getPanelSpacing());
        assertEquals(15, config.getHeaderSpacing());
        assertEquals(0.5, config.getSplitPaneDividerPosition());
        assertEquals(350, config.getRecentConfigsMinWidth());
        assertEquals(450, config.getDetailsMinWidth());
        assertEquals("btn-primary", config.getPrimaryButtonClass());
        assertEquals("btn-secondary", config.getSecondaryButtonClass());
        assertEquals("btn-danger", config.getDangerButtonClass());
        assertEquals(Styles.ELEVATED_3, config.getCardElevation());
    }

    @Test
    void testDefaultConfiguration() {
        // Given
        ConfigSelectionUIFactory.UIConfiguration config = factory.getConfiguration();

        // Then
        assertEquals(15, config.getPanelPadding());
        assertEquals(15, config.getPanelSpacing());
        assertEquals(10, config.getHeaderSpacing());
        assertEquals(0.45, config.getSplitPaneDividerPosition());
        assertEquals(300, config.getRecentConfigsMinWidth());
        assertEquals(400, config.getDetailsMinWidth());
        assertEquals("button-primary", config.getPrimaryButtonClass());
        assertEquals("", config.getSecondaryButtonClass());
        assertEquals(Styles.DANGER, config.getDangerButtonClass());
        assertEquals(Styles.ELEVATED_1, config.getCardElevation());
    }
}
