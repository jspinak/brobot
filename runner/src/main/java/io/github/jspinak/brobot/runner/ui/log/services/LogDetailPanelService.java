package io.github.jspinak.brobot.runner.ui.log.services;

import io.github.jspinak.brobot.runner.ui.log.models.LogEntryViewModel;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing log detail display panels.
 * Provides various detail views and export capabilities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogDetailPanelService {
    
    private final StateVisualizationService stateVisualizationService;
    private final LogEntryViewModelFactory viewModelFactory;
    
    /**
     * Configuration for detail panel.
     */
    public static class DetailPanelConfiguration {
        private boolean showStateVisualization = true;
        private boolean showRawData = false;
        private boolean showMetadata = true;
        private boolean editableDetails = false;
        private String title = "Log Details";
        private int textAreaRows = 15;
        
        public static DetailPanelConfigurationBuilder builder() {
            return new DetailPanelConfigurationBuilder();
        }
        
        public static class DetailPanelConfigurationBuilder {
            private DetailPanelConfiguration config = new DetailPanelConfiguration();
            
            public DetailPanelConfigurationBuilder showStateVisualization(boolean show) {
                config.showStateVisualization = show;
                return this;
            }
            
            public DetailPanelConfigurationBuilder showRawData(boolean show) {
                config.showRawData = show;
                return this;
            }
            
            public DetailPanelConfigurationBuilder showMetadata(boolean show) {
                config.showMetadata = show;
                return this;
            }
            
            public DetailPanelConfigurationBuilder editableDetails(boolean editable) {
                config.editableDetails = editable;
                return this;
            }
            
            public DetailPanelConfigurationBuilder title(String title) {
                config.title = title;
                return this;
            }
            
            public DetailPanelConfigurationBuilder textAreaRows(int rows) {
                config.textAreaRows = rows;
                return this;
            }
            
            public DetailPanelConfiguration build() {
                return config;
            }
        }
    }
    
    /**
     * Detail panel implementation.
     */
    public static class DetailPanel extends VBox {
        private final TextArea detailTextArea;
        private final StateVisualizationService.StateVisualizationPanel statePanel;
        private final TabPane tabPane;
        private final Label titleLabel;
        private LogEntryViewModel currentEntry;
        
        public DetailPanel(DetailPanelConfiguration config, 
                         StateVisualizationService stateVisualizationService) {
            setPadding(new Insets(0, 0, 0, 10));
            setSpacing(10);
            
            // Title
            titleLabel = new Label(config.title);
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            
            // Detail text area
            detailTextArea = new TextArea();
            detailTextArea.setEditable(config.editableDetails);
            detailTextArea.setWrapText(true);
            detailTextArea.setPrefRowCount(config.textAreaRows);
            VBox.setVgrow(detailTextArea, Priority.ALWAYS);
            
            // Create tabs
            tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            
            Tab detailsTab = new Tab("Details", detailTextArea);
            tabPane.getTabs().add(detailsTab);
            
            // State visualization tab
            if (config.showStateVisualization) {
                statePanel = stateVisualizationService.createVisualizationPanel();
                Tab stateTab = new Tab("State Visualization", statePanel);
                tabPane.getTabs().add(stateTab);
            } else {
                statePanel = null;
            }
            
            // Raw data tab
            if (config.showRawData) {
                TextArea rawDataArea = new TextArea();
                rawDataArea.setEditable(false);
                rawDataArea.setFont(Font.font("Monospace", 12));
                Tab rawTab = new Tab("Raw Data", rawDataArea);
                tabPane.getTabs().add(rawTab);
            }
            
            // Metadata tab
            if (config.showMetadata) {
                TreeView<String> metadataTree = new TreeView<>();
                Tab metadataTab = new Tab("Metadata", metadataTree);
                tabPane.getTabs().add(metadataTab);
            }
            
            getChildren().addAll(titleLabel, tabPane);
            VBox.setVgrow(tabPane, Priority.ALWAYS);
        }
        
        public void setCurrentEntry(LogEntryViewModel entry) {
            this.currentEntry = entry;
        }
        
        public LogEntryViewModel getCurrentEntry() {
            return currentEntry;
        }
        
        public TextArea getDetailTextArea() {
            return detailTextArea;
        }
        
        public StateVisualizationService.StateVisualizationPanel getStatePanel() {
            return statePanel;
        }
        
        public TabPane getTabPane() {
            return tabPane;
        }
    }
    
    /**
     * Creates a detail panel with default configuration.
     */
    public DetailPanel createDetailPanel() {
        return createDetailPanel(DetailPanelConfiguration.builder().build());
    }
    
    /**
     * Creates a detail panel with custom configuration.
     */
    public DetailPanel createDetailPanel(DetailPanelConfiguration config) {
        return new DetailPanel(config, stateVisualizationService);
    }
    
    /**
     * Updates the detail panel with log entry information.
     */
    public void updateDetails(DetailPanel panel, LogEntryViewModel entry) {
        panel.setCurrentEntry(entry);
        
        if (entry == null) {
            clearDetails(panel);
            return;
        }
        
        // Update main details
        String detailText = viewModelFactory.formatDetailedText(
            entry, 
            LogEntryViewModelFactory.DetailFormat.DETAILED
        );
        panel.getDetailTextArea().setText(detailText);
        
        // Update state visualization if available
        if (panel.getStatePanel() != null) {
            updateStateVisualization(panel.getStatePanel(), entry.getRawLogData());
        }
        
        // Update other tabs if present
        updateRawDataTab(panel, entry);
        updateMetadataTab(panel, entry);
        
        log.trace("Updated detail panel for log entry: {}", entry.getTime());
    }
    
    /**
     * Clears the detail panel.
     */
    public void clearDetails(DetailPanel panel) {
        panel.setCurrentEntry(null);
        panel.getDetailTextArea().clear();
        
        if (panel.getStatePanel() != null) {
            stateVisualizationService.clearVisualization(panel.getStatePanel());
        }
        
        // Clear other tabs
        clearRawDataTab(panel);
        clearMetadataTab(panel);
        
        log.trace("Cleared detail panel");
    }
    
    /**
     * Updates state visualization based on log data.
     */
    private void updateStateVisualization(StateVisualizationService.StateVisualizationPanel statePanel, 
                                        LogData logData) {
        if (logData == null) {
            stateVisualizationService.clearVisualization(statePanel);
            return;
        }
        
        if (logData.getType() == LogEventType.TRANSITION) {
            List<String> fromStates = new ArrayList<>();
            if (logData.getFromStates() != null) {
                fromStates.add(logData.getFromStates());
            }
            
            List<String> toStates = logData.getToStateNames() != null ? 
                logData.getToStateNames() : new ArrayList<>();
                
            stateVisualizationService.visualizeTransition(statePanel, fromStates, toStates);
        } else if (logData.getCurrentStateName() != null) {
            stateVisualizationService.visualizeCurrentState(statePanel, logData.getCurrentStateName());
        } else {
            stateVisualizationService.clearVisualization(statePanel);
        }
    }
    
    /**
     * Updates raw data tab if present.
     */
    private void updateRawDataTab(DetailPanel panel, LogEntryViewModel entry) {
        Tab rawTab = findTab(panel.getTabPane(), "Raw Data");
        if (rawTab != null && rawTab.getContent() instanceof TextArea) {
            TextArea rawArea = (TextArea) rawTab.getContent();
            String rawText = viewModelFactory.formatDetailedText(
                entry, 
                LogEntryViewModelFactory.DetailFormat.JSON
            );
            rawArea.setText(rawText);
        }
    }
    
    /**
     * Updates metadata tab if present.
     */
    private void updateMetadataTab(DetailPanel panel, LogEntryViewModel entry) {
        Tab metadataTab = findTab(panel.getTabPane(), "Metadata");
        if (metadataTab != null && metadataTab.getContent() instanceof TreeView) {
            @SuppressWarnings("unchecked")
            TreeView<String> tree = (TreeView<String>) metadataTab.getContent();
            
            TreeItem<String> root = new TreeItem<>("Metadata");
            root.setExpanded(true);
            
            // Add basic properties
            TreeItem<String> props = new TreeItem<>("Properties");
            props.getChildren().add(new TreeItem<>("Time: " + entry.getTime()));
            props.getChildren().add(new TreeItem<>("Level: " + entry.getLevel()));
            props.getChildren().add(new TreeItem<>("Type: " + entry.getType()));
            props.getChildren().add(new TreeItem<>("Success: " + entry.isSuccess()));
            root.getChildren().add(props);
            
            // Add log data if available
            LogData data = entry.getRawLogData();
            if (data != null) {
                TreeItem<String> logDataItem = new TreeItem<>("Log Data");
                
                if (data.getActionType() != null) {
                    logDataItem.getChildren().add(new TreeItem<>("Action Type: " + data.getActionType()));
                }
                if (data.getCurrentStateName() != null) {
                    logDataItem.getChildren().add(new TreeItem<>("Current State: " + data.getCurrentStateName()));
                }
                if (data.getPerformance() != null) {
                    TreeItem<String> perfItem = new TreeItem<>("Performance");
                    perfItem.getChildren().add(new TreeItem<>("Duration: " + data.getPerformance().getActionDuration() + " ms"));
                    logDataItem.getChildren().add(perfItem);
                }
                
                root.getChildren().add(logDataItem);
            }
            
            tree.setRoot(root);
        }
    }
    
    /**
     * Clears raw data tab.
     */
    private void clearRawDataTab(DetailPanel panel) {
        Tab rawTab = findTab(panel.getTabPane(), "Raw Data");
        if (rawTab != null && rawTab.getContent() instanceof TextArea) {
            ((TextArea) rawTab.getContent()).clear();
        }
    }
    
    /**
     * Clears metadata tab.
     */
    private void clearMetadataTab(DetailPanel panel) {
        Tab metadataTab = findTab(panel.getTabPane(), "Metadata");
        if (metadataTab != null && metadataTab.getContent() instanceof TreeView) {
            @SuppressWarnings("unchecked")
            TreeView<String> tree = (TreeView<String>) metadataTab.getContent();
            tree.setRoot(null);
        }
    }
    
    /**
     * Finds a tab by name.
     */
    private Tab findTab(TabPane tabPane, String name) {
        return tabPane.getTabs().stream()
            .filter(tab -> name.equals(tab.getText()))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Exports details to string format.
     */
    public String exportDetails(DetailPanel panel, ExportFormat format) {
        LogEntryViewModel entry = panel.getCurrentEntry();
        if (entry == null) {
            return "";
        }
        
        switch (format) {
            case TEXT:
                return panel.getDetailTextArea().getText();
            case JSON:
                return viewModelFactory.formatDetailedText(
                    entry, 
                    LogEntryViewModelFactory.DetailFormat.JSON
                );
            case XML:
                return viewModelFactory.formatDetailedText(
                    entry, 
                    LogEntryViewModelFactory.DetailFormat.XML
                );
            case MARKDOWN:
                return exportAsMarkdown(entry);
            default:
                return panel.getDetailTextArea().getText();
        }
    }
    
    /**
     * Export formats.
     */
    public enum ExportFormat {
        TEXT,
        JSON,
        XML,
        MARKDOWN
    }
    
    /**
     * Exports as markdown format.
     */
    private String exportAsMarkdown(LogEntryViewModel entry) {
        StringBuilder md = new StringBuilder();
        
        md.append("# Log Entry\n\n");
        md.append("| Property | Value |\n");
        md.append("|----------|-------|\n");
        md.append("| Time | ").append(entry.getTime()).append(" |\n");
        md.append("| Level | **").append(entry.getLevel()).append("** |\n");
        md.append("| Type | ").append(entry.getType()).append(" |\n");
        md.append("| Success | ").append(entry.isSuccess() ? "✓" : "✗").append(" |\n");
        md.append("\n## Message\n\n");
        md.append(entry.getMessage()).append("\n");
        
        LogData data = entry.getRawLogData();
        if (data != null) {
            if (data.getErrorMessage() != null) {
                md.append("\n## Error\n\n");
                md.append("```\n").append(data.getErrorMessage()).append("\n```\n");
            }
            
            if (data.getCurrentStateName() != null) {
                md.append("\n## State Information\n\n");
                md.append("- Current State: ").append(data.getCurrentStateName()).append("\n");
            }
            
            if (data.getPerformance() != null && data.getPerformance().getActionDuration() > 0) {
                md.append("\n## Performance\n\n");
                md.append("- Duration: ").append(data.getPerformance().getActionDuration()).append(" ms\n");
            }
        }
        
        return md.toString();
    }
    
    /**
     * Creates a context menu for the detail panel.
     */
    public ContextMenu createDetailContextMenu(DetailPanel panel) {
        ContextMenu menu = new ContextMenu();
        
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> {
            String text = panel.getDetailTextArea().getSelectedText();
            if (text.isEmpty()) {
                text = panel.getDetailTextArea().getText();
            }
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            clipboard.setContent(content);
        });
        
        MenuItem selectAllItem = new MenuItem("Select All");
        selectAllItem.setOnAction(e -> panel.getDetailTextArea().selectAll());
        
        MenuItem exportItem = new MenuItem("Export...");
        exportItem.setOnAction(e -> {
            // This would trigger an export dialog
            log.info("Export requested for current log entry");
        });
        
        menu.getItems().addAll(copyItem, selectAllItem, new SeparatorMenuItem(), exportItem);
        
        return menu;
    }
}