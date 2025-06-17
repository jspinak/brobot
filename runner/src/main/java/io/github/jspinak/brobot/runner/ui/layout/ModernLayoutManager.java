package io.github.jspinak.brobot.runner.ui.layout;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Manages the modern layout with responsive behavior and animations.
 */
@Component
public class ModernLayoutManager {
    
    @Getter
    private final BooleanProperty sidebarCollapsed = new SimpleBooleanProperty(false);
    
    private static final double SIDEBAR_EXPANDED_WIDTH = 240;
    private static final double SIDEBAR_COLLAPSED_WIDTH = 60;
    private static final Duration ANIMATION_DURATION = Duration.millis(200);
    
    /**
     * Creates a responsive horizontal layout with sidebar and content.
     */
    public HBox createResponsiveLayout(Region sidebar, Region content) {
        HBox layout = new HBox();
        layout.getStyleClass().add("main-layout");
        
        // Configure sidebar
        sidebar.setMinWidth(SIDEBAR_EXPANDED_WIDTH);
        sidebar.setPrefWidth(SIDEBAR_EXPANDED_WIDTH);
        sidebar.setMaxHeight(Double.MAX_VALUE);
        
        // Configure content to grow
        HBox.setHgrow(content, Priority.ALWAYS);
        content.setMaxWidth(Double.MAX_VALUE);
        content.setMaxHeight(Double.MAX_VALUE);
        
        layout.getChildren().addAll(sidebar, content);
        
        return layout;
    }
    
    /**
     * Creates a responsive vertical layout with header, content, and footer.
     */
    public VBox createVerticalLayout(Region header, Region content, Region footer) {
        VBox layout = new VBox();
        layout.getStyleClass().add("vertical-layout");
        
        // Configure header
        header.setMinHeight(Region.USE_PREF_SIZE);
        header.setMaxWidth(Double.MAX_VALUE);
        
        // Configure content to grow
        VBox.setVgrow(content, Priority.ALWAYS);
        content.setMaxWidth(Double.MAX_VALUE);
        content.setMaxHeight(Double.MAX_VALUE);
        
        // Configure footer
        footer.setMinHeight(Region.USE_PREF_SIZE);
        footer.setMaxWidth(Double.MAX_VALUE);
        
        layout.getChildren().addAll(header, content, footer);
        
        return layout;
    }
    
    /**
     * Toggles the sidebar with animation.
     */
    public void toggleSidebar(Region sidebar) {
        double targetWidth = sidebarCollapsed.get() ? SIDEBAR_EXPANDED_WIDTH : SIDEBAR_COLLAPSED_WIDTH;
        
        Timeline timeline = new Timeline(
            new KeyFrame(ANIMATION_DURATION,
                new KeyValue(sidebar.prefWidthProperty(), targetWidth),
                new KeyValue(sidebar.minWidthProperty(), targetWidth)
            )
        );
        
        timeline.setOnFinished(e -> sidebarCollapsed.set(!sidebarCollapsed.get()));
        timeline.play();
    }
    
    /**
     * Makes a region responsive to its parent size.
     */
    public void makeResponsive(Region region) {
        region.setMaxWidth(Double.MAX_VALUE);
        region.setMaxHeight(Double.MAX_VALUE);
    }
    
    /**
     * Adds responsive padding that adjusts based on window size.
     */
    public void addResponsivePadding(Region region) {
        region.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            if (width < 1200) {
                region.setStyle("-fx-padding: 16;");
            } else if (width < 1600) {
                region.setStyle("-fx-padding: 24;");
            } else {
                region.setStyle("-fx-padding: 32;");
            }
        });
    }
}