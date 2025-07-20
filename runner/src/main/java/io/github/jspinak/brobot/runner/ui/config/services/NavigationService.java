package io.github.jspinak.brobot.runner.ui.config.services;

import io.github.jspinak.brobot.runner.ui.config.ConfigBrowserPanel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for navigating within the configuration browser.
 */
@Service
public class NavigationService {
    
    private final TreeManagementService treeManagementService;
    
    @Autowired
    public NavigationService(TreeManagementService treeManagementService) {
        this.treeManagementService = treeManagementService;
    }
    
    /**
     * Navigates to a specific state in the configuration browser.
     *
     * @param stateName The name of the state to navigate to
     * @param treeView The tree view to navigate in
     * @return true if the state was found and selected, false otherwise
     */
    public boolean navigateToState(String stateName, TreeView<ConfigBrowserPanel.ConfigItem> treeView) {
        TreeItem<ConfigBrowserPanel.ConfigItem> stateItem = treeManagementService.getStateItem(stateName);
        if (stateItem != null) {
            // Expand parents
            expandToItem(stateItem);
            
            // Select the item
            treeView.getSelectionModel().select(stateItem);
            
            // Scroll to the item
            int index = treeView.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                treeView.scrollTo(index);
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * Navigates to a specific tree item.
     *
     * @param item The item to navigate to
     * @param treeView The tree view to navigate in
     */
    public void navigateToItem(TreeItem<ConfigBrowserPanel.ConfigItem> item, 
                             TreeView<ConfigBrowserPanel.ConfigItem> treeView) {
        if (item != null) {
            // Expand parents
            expandToItem(item);
            
            // Select the item
            treeView.getSelectionModel().select(item);
            
            // Scroll to the item
            int index = treeView.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                treeView.scrollTo(index);
            }
        }
    }
    
    /**
     * Expands all parent nodes up to the specified item.
     *
     * @param item The item to expand to
     */
    private void expandToItem(TreeItem<ConfigBrowserPanel.ConfigItem> item) {
        TreeItem<ConfigBrowserPanel.ConfigItem> parent = item.getParent();
        while (parent != null) {
            parent.setExpanded(true);
            parent = parent.getParent();
        }
    }
    
    /**
     * Navigates to the first item of a specific type.
     *
     * @param itemType The type of item to find
     * @param rootItem The root item to search from
     * @param treeView The tree view to navigate in
     * @return true if an item was found and selected, false otherwise
     */
    public boolean navigateToFirstItemOfType(ConfigBrowserPanel.ConfigItemType itemType,
                                           TreeItem<ConfigBrowserPanel.ConfigItem> rootItem,
                                           TreeView<ConfigBrowserPanel.ConfigItem> treeView) {
        TreeItem<ConfigBrowserPanel.ConfigItem> foundItem = findFirstItemOfType(rootItem, itemType);
        if (foundItem != null) {
            navigateToItem(foundItem, treeView);
            return true;
        }
        return false;
    }
    
    /**
     * Recursively finds the first item of a specific type.
     *
     * @param item The current item to search from
     * @param itemType The type to find
     * @return The first matching item, or null if not found
     */
    private TreeItem<ConfigBrowserPanel.ConfigItem> findFirstItemOfType(
            TreeItem<ConfigBrowserPanel.ConfigItem> item,
            ConfigBrowserPanel.ConfigItemType itemType) {
        
        if (item.getValue().getType() == itemType) {
            return item;
        }
        
        for (TreeItem<ConfigBrowserPanel.ConfigItem> child : item.getChildren()) {
            TreeItem<ConfigBrowserPanel.ConfigItem> found = findFirstItemOfType(child, itemType);
            if (found != null) {
                return found;
            }
        }
        
        return null;
    }
}