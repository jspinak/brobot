package io.github.jspinak.brobot.runner.ui.config.services;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.ui.config.ConfigBrowserPanel;
import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Service for loading configuration data into the tree structure.
 */
@Service
public class ConfigurationLoaderService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoaderService.class);
    
    private final EventBus eventBus;
    private final AutomationProjectManager projectManager;
    private final StateService allStatesService;
    private final TreeManagementService treeManagementService;
    
    @Autowired
    public ConfigurationLoaderService(EventBus eventBus, 
                                    AutomationProjectManager projectManager,
                                    StateService allStatesService,
                                    TreeManagementService treeManagementService) {
        this.eventBus = eventBus;
        this.projectManager = projectManager;
        this.allStatesService = allStatesService;
        this.treeManagementService = treeManagementService;
    }
    
    /**
     * Loads a configuration into the tree structure.
     *
     * @param config The configuration to load
     * @param rootItem The root tree item
     * @throws IOException If loading fails
     */
    public void loadConfiguration(ConfigEntry config, TreeItem<ConfigBrowserPanel.ConfigItem> rootItem) 
            throws IOException {
        // Clear existing items
        treeManagementService.clearTree(rootItem);
        
        if (config == null) {
            return;
        }
        
        // Set root node name
        rootItem.setValue(new ConfigBrowserPanel.ConfigItem(config.getName(), ConfigBrowserPanel.ConfigItemType.ROOT));
        
        // Add configuration files
        TreeItem<ConfigBrowserPanel.ConfigItem> filesItem = createConfigurationFilesSection(config);
        
        // Add project structure if loaded
        AutomationProject project = projectManager.getCurrentProject();
        if (project != null) {
            rootItem.getChildren().addAll(
                filesItem,
                createMetadataSection(project),
                createStatesSection(),
                createTransitionsSection(),
                createAutomationSection(project)
            );
        } else {
            rootItem.getChildren().add(filesItem);
        }
        
        // Expand the root item
        rootItem.setExpanded(true);
        
        // Log success
        eventBus.publish(LogEvent.info(this, 
            "Configuration browser loaded: " + config.getName(), "Configuration"));
    }
    
    /**
     * Creates the configuration files section.
     *
     * @param config The configuration entry
     * @return The files tree item
     */
    private TreeItem<ConfigBrowserPanel.ConfigItem> createConfigurationFilesSection(ConfigEntry config) {
        TreeItem<ConfigBrowserPanel.ConfigItem> filesItem = 
            treeManagementService.createFolderItem("Configuration Files");
        
        // Project config file
        TreeItem<ConfigBrowserPanel.ConfigItem> projectConfigItem = 
            treeManagementService.createConfigFileItem(
                config.getProjectConfigFileName(),
                ConfigBrowserPanel.ConfigItemType.PROJECT_CONFIG,
                config.getProjectConfigPath()
            );
        
        // DSL config file
        TreeItem<ConfigBrowserPanel.ConfigItem> dslConfigItem = 
            treeManagementService.createConfigFileItem(
                config.getDslConfigFileName(),
                ConfigBrowserPanel.ConfigItemType.DSL_CONFIG,
                config.getDslConfigPath()
            );
        
        filesItem.getChildren().addAll(projectConfigItem, dslConfigItem);
        return filesItem;
    }
    
    /**
     * Creates the metadata section.
     *
     * @param project The automation project
     * @return The metadata tree item
     */
    private TreeItem<ConfigBrowserPanel.ConfigItem> createMetadataSection(AutomationProject project) {
        TreeItem<ConfigBrowserPanel.ConfigItem> metadataItem = 
            treeManagementService.createFolderItem("Metadata");
        
        metadataItem.getChildren().addAll(
            treeManagementService.createMetadataItem("Project: " + project.getName()),
            treeManagementService.createMetadataItem("Version: " + 
                (project.getVersion() != null ? project.getVersion() : "Not specified")),
            treeManagementService.createMetadataItem("Author: " + 
                (project.getAuthor() != null ? project.getAuthor() : "Not specified"))
        );
        
        return metadataItem;
    }
    
    /**
     * Creates the states section.
     *
     * @return The states tree item
     */
    private TreeItem<ConfigBrowserPanel.ConfigItem> createStatesSection() {
        TreeItem<ConfigBrowserPanel.ConfigItem> statesItem = 
            treeManagementService.createFolderItem("States");
        
        // Use allStatesService to get all states
        for (State state : allStatesService.getAllStates()) {
            TreeItem<ConfigBrowserPanel.ConfigItem> stateItem = 
                treeManagementService.createStateItem(state);
            
            // Add state images
            for (StateImage stateImage : state.getStateImages()) {
                TreeItem<ConfigBrowserPanel.ConfigItem> imageItem = 
                    treeManagementService.createStateImageItem(stateImage);
                stateItem.getChildren().add(imageItem);
            }
            
            statesItem.getChildren().add(stateItem);
            treeManagementService.addStateItem(state.getName(), stateItem);
        }
        
        return statesItem;
    }
    
    /**
     * Creates the transitions section.
     *
     * @return The transitions tree item
     */
    private TreeItem<ConfigBrowserPanel.ConfigItem> createTransitionsSection() {
        return treeManagementService.createFolderItem("State Transitions");
    }
    
    /**
     * Creates the automation section.
     *
     * @param project The automation project
     * @return The automation tree item
     */
    private TreeItem<ConfigBrowserPanel.ConfigItem> createAutomationSection(AutomationProject project) {
        TreeItem<ConfigBrowserPanel.ConfigItem> automationItem = 
            treeManagementService.createFolderItem("Automation");
        
        if (project.getAutomation() != null && project.getAutomation().getButtons() != null) {
            for (TaskButton button : project.getAutomation().getButtons()) {
                TreeItem<ConfigBrowserPanel.ConfigItem> buttonItem = 
                    treeManagementService.createAutomationButtonItem(button);
                automationItem.getChildren().add(buttonItem);
            }
        }
        
        return automationItem;
    }
}