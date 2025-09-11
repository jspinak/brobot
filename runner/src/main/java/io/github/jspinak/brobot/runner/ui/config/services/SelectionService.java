package io.github.jspinak.brobot.runner.ui.config.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.control.TreeItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.ui.config.ConfigBrowserPanel;

/** Service for handling item selection and details display. */
@Service
public class SelectionService {
    private static final Logger logger = LoggerFactory.getLogger(SelectionService.class);

    /**
     * Processes the selection of a tree item and generates details text.
     *
     * @param item The selected tree item
     * @return Details text about the selected item
     */
    public String processSelection(TreeItem<ConfigBrowserPanel.ConfigItem> item) {
        if (item == null || item.getValue() == null) {
            return "";
        }

        ConfigBrowserPanel.ConfigItem configItem = item.getValue();

        try {
            switch (configItem.getType()) {
                case PROJECT_CONFIG:
                case DSL_CONFIG:
                    return processConfigFile(configItem);

                case STATE:
                    return processState(configItem);

                case STATE_IMAGE:
                    return processStateImage(configItem);

                case AUTOMATION_BUTTON:
                    return processAutomationButton(configItem);

                default:
                    return "Select an item to view details";
            }
        } catch (Exception e) {
            logger.error("Error processing selection", e);
            return "Error displaying details: " + e.getMessage();
        }
    }

    /**
     * Processes a configuration file selection.
     *
     * @param configItem The configuration item
     * @return The file contents
     * @throws IOException If file reading fails
     */
    private String processConfigFile(ConfigBrowserPanel.ConfigItem configItem) throws IOException {
        if (configItem.getData() instanceof Path path) {
            return Files.readString(path);
        }
        return "File path not available";
    }

    /**
     * Processes a state selection.
     *
     * @param configItem The configuration item
     * @return Details about the state
     */
    private String processState(ConfigBrowserPanel.ConfigItem configItem) {
        if (configItem.getData() instanceof State state) {
            StringBuilder details = new StringBuilder();
            details.append("State: ").append(state.getName()).append("\n\n");
            details.append("ID: ").append(state.getId()).append("\n");
            details.append("Images: ").append(state.getStateImages().size()).append("\n");

            // Add more state details if needed
            if (!state.getStateImages().isEmpty()) {
                details.append("\nState Images:\n");
                for (StateImage img : state.getStateImages()) {
                    details.append("  - ").append(img.getName()).append("\n");
                }
            }

            return details.toString();
        }
        return "State data not available";
    }

    /**
     * Processes a state image selection.
     *
     * @param configItem The configuration item
     * @return Details about the state image
     */
    private String processStateImage(ConfigBrowserPanel.ConfigItem configItem) {
        if (configItem.getData() instanceof StateImage stateImage) {
            StringBuilder details = new StringBuilder();
            details.append("State Image: ").append(stateImage.getName()).append("\n\n");

            if (!stateImage.getPatterns().isEmpty()) {
                String imagePath = stateImage.getPatterns().getFirst().getImgpath();
                if (imagePath != null) {
                    details.append("Path: ").append(imagePath).append("\n");
                }
            }

            // Add more image details if available

            return details.toString();
        }
        return "State image data not available";
    }

    /**
     * Processes an automation button selection.
     *
     * @param configItem The configuration item
     * @return Details about the automation button
     */
    private String processAutomationButton(ConfigBrowserPanel.ConfigItem configItem) {
        if (configItem.getData() instanceof TaskButton button) {
            StringBuilder details = new StringBuilder();
            details.append("Button: ").append(button.getLabel()).append("\n\n");
            details.append("Function: ").append(button.getFunctionName()).append("\n");
            details.append("Category: ")
                    .append(button.getCategory() != null ? button.getCategory() : "None")
                    .append("\n");
            details.append("Confirmation Required: ")
                    .append(button.isConfirmationRequired())
                    .append("\n");

            // Add parameters if available
            if (button.getParametersAsMap() != null && !button.getParametersAsMap().isEmpty()) {
                details.append("\nParameters:\n");
                button.getParametersAsMap()
                        .forEach(
                                (key, value) ->
                                        details.append("  ")
                                                .append(key)
                                                .append(": ")
                                                .append(value)
                                                .append("\n"));
            }

            // Add tooltip if configured
            if (button.getTooltip() != null) {
                details.append("\nTooltip: ").append(button.getTooltip()).append("\n");
            }

            return details.toString();
        }
        return "Button data not available";
    }

    /**
     * Extracts the image name from a state image for preview.
     *
     * @param item The selected tree item
     * @return The image name, or null if not a state image
     */
    public String getImageNameForPreview(TreeItem<ConfigBrowserPanel.ConfigItem> item) {
        if (item == null || item.getValue() == null) {
            return null;
        }

        ConfigBrowserPanel.ConfigItem configItem = item.getValue();
        if (configItem.getType() == ConfigBrowserPanel.ConfigItemType.STATE_IMAGE
                && configItem.getData() instanceof StateImage stateImage) {

            if (!stateImage.getPatterns().isEmpty()) {
                return stateImage.getPatterns().getFirst().getImgpath();
            }
        }

        return null;
    }
}
