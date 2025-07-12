package io.github.jspinak.brobot.runner.ui.config.services;

import io.github.jspinak.brobot.runner.ui.config.ConfigBrowserPanel;
import javafx.scene.control.TreeItem;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing the configuration tree structure.
 */
@Service
public class TreeManagementService {
    
    private final Map<String, TreeItem<ConfigBrowserPanel.ConfigItem>> stateItems = new HashMap<>();
    
    /**
     * Creates the root item for the configuration tree.
     *
     * @param name The name for the root item
     * @return The root tree item
     */
    public TreeItem<ConfigBrowserPanel.ConfigItem> createRootItem(String name) {
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = new TreeItem<>(
            new ConfigBrowserPanel.ConfigItem(name, ConfigBrowserPanel.ConfigItemType.ROOT)
        );
        rootItem.setExpanded(true);
        return rootItem;
    }
    
    /**
     * Clears all tree items and state mappings.
     *
     * @param rootItem The root item to clear
     */
    public void clearTree(TreeItem<ConfigBrowserPanel.ConfigItem> rootItem) {
        rootItem.getChildren().clear();
        stateItems.clear();
    }
    
    /**
     * Adds a state item to the tracking map.
     *
     * @param stateName The name of the state
     * @param stateItem The tree item for the state
     */
    public void addStateItem(String stateName, TreeItem<ConfigBrowserPanel.ConfigItem> stateItem) {
        stateItems.put(stateName, stateItem);
    }
    
    /**
     * Gets a state item by name.
     *
     * @param stateName The name of the state
     * @return The tree item for the state, or null if not found
     */
    public TreeItem<ConfigBrowserPanel.ConfigItem> getStateItem(String stateName) {
        return stateItems.get(stateName);
    }
    
    /**
     * Creates a folder item.
     *
     * @param name The name of the folder
     * @return The folder tree item
     */
    public TreeItem<ConfigBrowserPanel.ConfigItem> createFolderItem(String name) {
        return new TreeItem<>(new ConfigBrowserPanel.ConfigItem(name, ConfigBrowserPanel.ConfigItemType.FOLDER));
    }
    
    /**
     * Creates a configuration file item.
     *
     * @param fileName The name of the file
     * @param type The type of configuration file
     * @param data The associated data (e.g., file path)
     * @return The configuration file tree item
     */
    public TreeItem<ConfigBrowserPanel.ConfigItem> createConfigFileItem(String fileName, 
            ConfigBrowserPanel.ConfigItemType type, Object data) {
        TreeItem<ConfigBrowserPanel.ConfigItem> item = new TreeItem<>(
            new ConfigBrowserPanel.ConfigItem(fileName, type)
        );
        item.getValue().setData(data);
        return item;
    }
    
    /**
     * Creates a state item.
     *
     * @param state The state object
     * @return The state tree item
     */
    public TreeItem<ConfigBrowserPanel.ConfigItem> createStateItem(
            io.github.jspinak.brobot.model.state.State state) {
        TreeItem<ConfigBrowserPanel.ConfigItem> item = new TreeItem<>(
            new ConfigBrowserPanel.ConfigItem(state.getName(), ConfigBrowserPanel.ConfigItemType.STATE)
        );
        item.getValue().setData(state);
        return item;
    }
    
    /**
     * Creates a state image item.
     *
     * @param stateImage The state image object
     * @return The state image tree item
     */
    public TreeItem<ConfigBrowserPanel.ConfigItem> createStateImageItem(
            io.github.jspinak.brobot.model.state.StateImage stateImage) {
        TreeItem<ConfigBrowserPanel.ConfigItem> item = new TreeItem<>(
            new ConfigBrowserPanel.ConfigItem(stateImage.getName(), ConfigBrowserPanel.ConfigItemType.STATE_IMAGE)
        );
        item.getValue().setData(stateImage);
        return item;
    }
    
    /**
     * Creates an automation button item.
     *
     * @param button The automation button
     * @return The button tree item
     */
    public TreeItem<ConfigBrowserPanel.ConfigItem> createAutomationButtonItem(
            io.github.jspinak.brobot.runner.project.TaskButton button) {
        TreeItem<ConfigBrowserPanel.ConfigItem> item = new TreeItem<>(
            new ConfigBrowserPanel.ConfigItem(button.getLabel(), ConfigBrowserPanel.ConfigItemType.AUTOMATION_BUTTON)
        );
        item.getValue().setData(button);
        return item;
    }
    
    /**
     * Creates a metadata item.
     *
     * @param text The metadata text
     * @return The metadata tree item
     */
    public TreeItem<ConfigBrowserPanel.ConfigItem> createMetadataItem(String text) {
        return new TreeItem<>(new ConfigBrowserPanel.ConfigItem(text, ConfigBrowserPanel.ConfigItemType.METADATA));
    }
}