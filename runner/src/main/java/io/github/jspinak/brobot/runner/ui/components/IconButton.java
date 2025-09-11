package io.github.jspinak.brobot.runner.ui.components;

import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import io.github.jspinak.brobot.runner.ui.utils.IconMapper;

/**
 * Factory class for creating buttons with programmatic icons. This ensures consistent icon usage
 * across the application.
 */
public class IconButton {

    /**
     * Creates a button with a programmatic icon based on the label. If the label contains an emoji,
     * it will be cleaned and replaced with a programmatic icon.
     *
     * @param label The button label (may contain emoji)
     * @param iconRegistry The icon registry to get icons from
     * @return A button with clean label and programmatic icon
     */
    public static Button create(String label, IconRegistry iconRegistry) {
        // Get clean label without emoji
        String cleanLabel = IconMapper.processLabel(label);

        // Get icon name for this label
        String iconName = IconMapper.getIconName(label);

        // Create button with clean label
        Button button = new Button(cleanLabel);

        // Add icon if available
        if (iconName != null && iconRegistry != null) {
            ImageView icon = iconRegistry.getIconView(iconName, 16);
            if (icon != null) {
                button.setGraphic(icon);
            }
        }

        return button;
    }

    /** Creates a primary button with icon. */
    public static Button primary(String label, IconRegistry iconRegistry) {
        Button button = create(label, iconRegistry);
        button.getStyleClass().add("primary");
        return button;
    }

    /** Creates a secondary button with icon. */
    public static Button secondary(String label, IconRegistry iconRegistry) {
        Button button = create(label, iconRegistry);
        button.getStyleClass().add("secondary");
        return button;
    }

    /** Creates a danger button with icon. */
    public static Button danger(String label, IconRegistry iconRegistry) {
        Button button = create(label, iconRegistry);
        button.getStyleClass().add("danger");
        return button;
    }

    /**
     * Updates an existing button to use a programmatic icon.
     *
     * @param button The button to update
     * @param iconRegistry The icon registry
     */
    public static void updateIcon(Button button, IconRegistry iconRegistry) {
        if (button == null || iconRegistry == null) return;

        String label = button.getText();
        if (label == null) return;

        // Clean the label
        String cleanLabel = IconMapper.processLabel(label);
        button.setText(cleanLabel);

        // Get and set icon
        String iconName = IconMapper.getIconName(label);
        if (iconName != null) {
            ImageView icon = iconRegistry.getIconView(iconName, 16);
            if (icon != null) {
                button.setGraphic(icon);
            }
        }
    }
}
