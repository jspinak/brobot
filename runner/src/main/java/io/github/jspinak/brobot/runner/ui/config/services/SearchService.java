package io.github.jspinak.brobot.runner.ui.config.services;

import io.github.jspinak.brobot.runner.ui.config.ConfigBrowserPanel;
import javafx.scene.control.TreeItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for searching through the configuration tree.
 */
@Service
public class SearchService {
    
    /**
     * Result of a tree search operation.
     */
    public static class SearchResult {
        private final boolean found;
        private final List<TreeItem<ConfigBrowserPanel.ConfigItem>> matchingItems;
        
        public SearchResult(boolean found, List<TreeItem<ConfigBrowserPanel.ConfigItem>> matchingItems) {
            this.found = found;
            this.matchingItems = matchingItems;
        }
        
        public boolean isFound() {
            return found;
        }
        
        public List<TreeItem<ConfigBrowserPanel.ConfigItem>> getMatchingItems() {
            return matchingItems;
        }
    }
    
    /**
     * Searches the tree for items matching the search text.
     *
     * @param rootItem The root item to search from
     * @param searchText The text to search for
     * @return The search result
     */
    public SearchResult searchTree(TreeItem<ConfigBrowserPanel.ConfigItem> rootItem, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            resetTreeExpansion(rootItem);
            return new SearchResult(false, new ArrayList<>());
        }
        
        String lowerSearchText = searchText.toLowerCase();
        List<TreeItem<ConfigBrowserPanel.ConfigItem>> matchingItems = new ArrayList<>();
        boolean found = searchTreeRecursive(rootItem, lowerSearchText, matchingItems);
        
        return new SearchResult(found, matchingItems);
    }
    
    /**
     * Recursively searches the tree and expands matching branches.
     *
     * @param item The current item to search
     * @param searchText The search text (lowercase)
     * @param matchingItems List to collect matching items
     * @return true if this item or any children match
     */
    private boolean searchTreeRecursive(TreeItem<ConfigBrowserPanel.ConfigItem> item, 
                                      String searchText, 
                                      List<TreeItem<ConfigBrowserPanel.ConfigItem>> matchingItems) {
        // Check if this item matches
        boolean matches = item.getValue().toString().toLowerCase().contains(searchText);
        if (matches) {
            matchingItems.add(item);
        }
        
        // Check children recursively
        boolean childrenMatch = false;
        for (TreeItem<ConfigBrowserPanel.ConfigItem> child : item.getChildren()) {
            if (searchTreeRecursive(child, searchText, matchingItems)) {
                childrenMatch = true;
            }
        }
        
        // Expand this item if it matches or any children match
        item.setExpanded(matches || childrenMatch);
        
        return matches || childrenMatch;
    }
    
    /**
     * Resets the tree expansion to default state.
     *
     * @param rootItem The root item
     */
    public void resetTreeExpansion(TreeItem<ConfigBrowserPanel.ConfigItem> rootItem) {
        resetTreeExpansionRecursive(rootItem);
    }
    
    /**
     * Recursively resets tree expansion based on item type.
     *
     * @param item The current item
     */
    private void resetTreeExpansionRecursive(TreeItem<ConfigBrowserPanel.ConfigItem> item) {
        // Reset expansion state based on item type
        switch (item.getValue().getType()) {
            case ROOT:
            case FOLDER:
                item.setExpanded(true);
                break;
            default:
                item.setExpanded(false);
        }
        
        // Process children
        for (TreeItem<ConfigBrowserPanel.ConfigItem> child : item.getChildren()) {
            resetTreeExpansionRecursive(child);
        }
    }
    
    /**
     * Highlights the first matching item in the search results.
     *
     * @param searchResult The search result
     * @param treeView The tree view to highlight in
     */
    public void highlightFirstMatch(SearchResult searchResult, 
                                  javafx.scene.control.TreeView<ConfigBrowserPanel.ConfigItem> treeView) {
        if (searchResult.isFound() && !searchResult.getMatchingItems().isEmpty()) {
            TreeItem<ConfigBrowserPanel.ConfigItem> firstMatch = searchResult.getMatchingItems().get(0);
            
            // Ensure all parent nodes are expanded
            expandToItem(firstMatch);
            
            // Select and scroll to the item
            treeView.getSelectionModel().select(firstMatch);
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
}