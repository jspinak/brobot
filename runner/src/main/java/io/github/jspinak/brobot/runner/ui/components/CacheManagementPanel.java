package io.github.jspinak.brobot.runner.ui.components;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import io.github.jspinak.brobot.runner.cache.CacheManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * Panel for managing cache operations.
 * Extracted from ResourceMonitorPanel to improve modularity.
 */
public class CacheManagementPanel extends VBox {
    
    private final CacheManager cacheManager;
    private ComboBox<String> cacheSelector;
    private Label cacheStatsLabel;
    
    public CacheManagementPanel(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        initialize();
    }
    
    private void initialize() {
        setSpacing(12);
        setPadding(new Insets(0));
        getStyleClass().add("cache-management-panel");
        
        // Create cache controls
        HBox cacheControls = createCacheControls();
        
        // Create stats display
        cacheStatsLabel = new Label("No cache statistics available");
        cacheStatsLabel.getStyleClass().add(Styles.TEXT_MUTED);
        cacheStatsLabel.setWrapText(true);
        
        getChildren().addAll(cacheControls, cacheStatsLabel);
    }
    
    private HBox createCacheControls() {
        HBox controls = new HBox(12);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.getStyleClass().add("cache-controls");
        
        // Cache selector
        cacheSelector = new ComboBox<>();
        cacheSelector.getItems().addAll("All Caches", "State", "StateImage", "Pattern", "Matches");
        cacheSelector.setValue("All Caches");
        cacheSelector.getStyleClass().add("cache-selector");
        cacheSelector.setPrefWidth(150);
        
        // Clear cache button
        Button clearCacheButton = new Button("Clear Cache");
        clearCacheButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER);
        clearCacheButton.setOnAction(e -> clearSelectedCache());
        
        controls.getChildren().addAll(
            cacheSelector,
            clearCacheButton,
            new Spacer()
        );
        
        return controls;
    }
    
    /**
     * Update the cache statistics display.
     */
    public void updateCacheStats(Map<String, Map<String, Long>> allStats) {
        String selectedCache = cacheSelector.getValue();
        String statsText = formatCacheStats(allStats, selectedCache);
        cacheStatsLabel.setText(statsText);
    }
    
    /**
     * Refresh cache statistics from the cache manager.
     */
    public void refreshCacheStats() {
        Map<String, Map<String, Long>> allStats = cacheManager.getAllCacheStats();
        updateCacheStats(allStats);
    }
    
    private String formatCacheStats(Map<String, Map<String, Long>> allStats, String selectedCache) {
        StringBuilder statsText = new StringBuilder();
        
        if ("All Caches".equals(selectedCache)) {
            // Show summary for all caches
            if (allStats.isEmpty()) {
                return "No cache statistics available";
            }
            
            for (Map.Entry<String, Map<String, Long>> entry : allStats.entrySet()) {
                Map<String, Long> stats = entry.getValue();
                if (stats != null) {
                    statsText.append(entry.getKey()).append(": ");
                    statsText.append(stats.getOrDefault("size", 0L))
                            .append("/")
                            .append(stats.getOrDefault("maxSize", 0L));
                    statsText.append(" items, ");
                    statsText.append(stats.getOrDefault("hitRatio", 0L))
                            .append("% hit ratio\n");
                }
            }
        } else {
            // Show detailed stats for selected cache
            Map<String, Long> stats = allStats.get(selectedCache.toLowerCase());
            if (stats != null) {
                statsText.append("Size: ")
                        .append(stats.getOrDefault("size", 0L))
                        .append("/")
                        .append(stats.getOrDefault("maxSize", 0L))
                        .append("\n");
                statsText.append("Hits: ")
                        .append(stats.getOrDefault("hits", 0L))
                        .append("\n");
                statsText.append("Misses: ")
                        .append(stats.getOrDefault("misses", 0L))
                        .append("\n");
                statsText.append("Hit Ratio: ")
                        .append(stats.getOrDefault("hitRatio", 0L))
                        .append("%\n");
                statsText.append("Puts: ")
                        .append(stats.getOrDefault("puts", 0L));
            } else {
                statsText.append("No statistics available for ").append(selectedCache);
            }
        }
        
        return statsText.toString().trim();
    }
    
    private void clearSelectedCache() {
        String selectedCache = cacheSelector.getValue();
        
        if ("All Caches".equals(selectedCache)) {
            cacheManager.clearAllCaches();
        } else {
            // Clear specific cache
            switch (selectedCache) {
                case "State":
                    cacheManager.getStateCache().invalidateAll();
                    break;
                case "StateImage":
                    cacheManager.getStateImageCache().invalidateAll();
                    break;
                case "Pattern":
                    cacheManager.getPatternCache().invalidateAll();
                    break;
                case "Matches":
                    cacheManager.getMatchesCache().invalidateAll();
                    break;
            }
        }
        
        // Refresh stats after clearing
        updateCacheStats(cacheManager.getAllCacheStats());
    }
}