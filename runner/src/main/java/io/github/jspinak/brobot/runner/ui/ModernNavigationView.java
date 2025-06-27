package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import io.github.jspinak.brobot.runner.ui.navigation.NavigationManager;
import io.github.jspinak.brobot.runner.model.element.Screen;
import io.github.jspinak.brobot.runner.ui.navigation.ScreenRegistry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Modern sidebar navigation component with collapsible menu and icons.
 */
@Component
@RequiredArgsConstructor
public class ModernNavigationView extends VBox {
    private final NavigationManager navigationManager;
    private final ScreenRegistry screenRegistry;
    private final IconRegistry iconRegistry;
    
    private final Map<String, Button> navigationButtons = new HashMap<>();
    private boolean collapsed = false;
    private Label sectionLabel;
    
    // Icon mappings for navigation items
    private final Map<String, String> iconMappings = Map.of(
        "configuration", "settings",
        "automation", "play",
        "resources", "chart",
        "logs", "list",
        "showcase", "grid"
    );
    
    public void initialize() {
        getStyleClass().addAll("navigation-sidebar");
        setSpacing(8);
        setPadding(new Insets(16));
        setMinWidth(240);
        setPrefWidth(240);
        setFillWidth(true);
        
        // Add section header
        sectionLabel = new Label("NAVIGATION");
        sectionLabel.getStyleClass().add("navigation-section-label");
        getChildren().add(sectionLabel);
        
        // Add separator
        Separator separator = new Separator();
        separator.getStyleClass().add("navigation-separator");
        VBox.setMargin(separator, new Insets(8, 0, 8, 0));
        getChildren().add(separator);
        
        // Create navigation buttons
        createNavigationButtons();
        
        // Add spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);
        
        // Add collapse/expand button at bottom
        Button collapseButton = createCollapseButton();
        getChildren().add(collapseButton);
    }
    
    private void createNavigationButtons() {
        String[] screens = {"configuration", "automation", "resources", "logs", "showcase"};
        
        for (String screenId : screens) {
            Optional<Screen> screenOpt = screenRegistry.getScreen(screenId);
            if (screenOpt.isPresent()) {
                Screen screen = screenOpt.get();
                Button navButton = createNavigationButton(screenId, screen.getTitle());
                navigationButtons.put(screenId, navButton);
                getChildren().add(navButton);
            }
        }
        
        // Set first button as active
        if (!navigationButtons.isEmpty()) {
            navigationButtons.values().iterator().next().getStyleClass().add("active");
        }
    }
    
    private Button createNavigationButton(String screenId, String title) {
        Button button = new Button(title);
        button.getStyleClass().addAll("navigation-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setGraphicTextGap(12);
        
        // Add icon
        String iconName = iconMappings.getOrDefault(screenId, screenId);
        ImageView icon = iconRegistry.getIconView(iconName, 20);
        if (icon != null) {
            button.setGraphic(icon);
        }
        
        // Handle click
        button.setOnAction(e -> {
            // Remove active class from all buttons
            navigationButtons.values().forEach(b -> b.getStyleClass().remove("active"));
            // Add active class to clicked button
            button.getStyleClass().add("active");
            // Navigate to screen
            navigationManager.navigateTo(screenId);
        });
        
        return button;
    }
    
    private Button createCollapseButton() {
        Button collapseButton = new Button();
        collapseButton.getStyleClass().addAll("navigation-button", "collapse-button");
        collapseButton.setMaxWidth(Double.MAX_VALUE);
        collapseButton.setAlignment(Pos.CENTER_LEFT);
        collapseButton.setGraphicTextGap(12);
        
        ImageView icon = iconRegistry.getIconView("chevron-left", 20);
        if (icon != null) {
            collapseButton.setGraphic(icon);
        }
        collapseButton.setText("Collapse");
        
        collapseButton.setOnAction(e -> toggleCollapse(collapseButton));
        
        return collapseButton;
    }
    
    private void toggleCollapse(Button collapseButton) {
        collapsed = !collapsed;
        
        if (collapsed) {
            // Collapse sidebar
            setMinWidth(60);
            setPrefWidth(60);
            sectionLabel.setVisible(false);
            sectionLabel.setManaged(false);
            
            // Hide button text
            navigationButtons.values().forEach(button -> {
                button.setText("");
                Tooltip.install(button, new Tooltip(button.getText()));
            });
            
            collapseButton.setText("");
            ImageView icon = iconRegistry.getIconView("chevron-right", 20);
            if (icon != null) {
                collapseButton.setGraphic(icon);
            }
        } else {
            // Expand sidebar
            setMinWidth(240);
            setPrefWidth(240);
            sectionLabel.setVisible(true);
            sectionLabel.setManaged(true);
            
            // Restore button text
            String[] screens = {"configuration", "automation", "resources", "logs", "showcase"};
            for (String screenId : screens) {
                Button button = navigationButtons.get(screenId);
                if (button != null) {
                    Optional<Screen> screenOpt = screenRegistry.getScreen(screenId);
                    screenOpt.ifPresent(screen -> {
                        button.setText(screen.getTitle());
                        Tooltip.uninstall(button, null);
                    });
                }
            }
            
            collapseButton.setText("Collapse");
            ImageView icon = iconRegistry.getIconView("chevron-left", 20);
            if (icon != null) {
                collapseButton.setGraphic(icon);
            }
        }
    }
}