package io.github.jspinak.brobot.runner.project;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/** Represents a UI button configuration for the desktop runner that calls automation functions. */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskButton {
    private String id;
    private String label;
    private String tooltip;
    private String functionName;
    private Object parameters;
    private String category;
    private String icon;
    private ButtonPosition position;
    private ButtonStyling styling;
    private boolean confirmationRequired;
    private String confirmationMessage;

    /**
     * Creates a copy of this button configuration.
     *
     * @return A new button with the same configuration
     */
    public TaskButton copy() {
        TaskButton copy = new TaskButton();
        copy.setId(this.id);
        copy.setLabel(this.label);
        copy.setTooltip(this.tooltip);
        copy.setFunctionName(this.functionName);
        copy.setParameters(this.parameters);
        copy.setCategory(this.category);
        copy.setIcon(this.icon);
        copy.setConfirmationRequired(this.confirmationRequired);
        copy.setConfirmationMessage(this.confirmationMessage);

        if (this.position != null) {
            ButtonPosition positionCopy = new ButtonPosition();
            positionCopy.setRow(this.position.getRow());
            positionCopy.setColumn(this.position.getColumn());
            positionCopy.setOrder(this.position.getOrder());
            copy.setPosition(positionCopy);
        }

        if (this.styling != null) {
            ButtonStyling stylingCopy = new ButtonStyling();
            stylingCopy.setBackgroundColor(this.styling.getBackgroundColor());
            stylingCopy.setTextColor(this.styling.getTextColor());
            stylingCopy.setSize(this.styling.getSize());
            stylingCopy.setCustomClass(this.styling.getCustomClass());
            copy.setStyling(stylingCopy);
        }

        return copy;
    }

    /**
     * Gets the parameters as a Map for easier manipulation. Only works if parameters is a Map or
     * can be cast to a Map.
     *
     * @return The parameters as a Map or an empty Map if parameters is null or not a Map
     */
    public Map<String, Object> getParametersAsMap() {
        if (parameters == null) {
            return new HashMap<>();
        }

        if (parameters instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> paramsMap = (Map<String, Object>) parameters;
            return paramsMap;
        }

        return new HashMap<>();
    }

    /** Creates a simple string representation of the button configuration. */
    @Override
    public String toString() {
        return "Button{"
                + "id='"
                + id
                + '\''
                + ", label='"
                + label
                + '\''
                + ", functionName='"
                + functionName
                + '\''
                + ", category='"
                + category
                + '\''
                + '}';
    }

    /** Represents position information for a button in the UI. */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ButtonPosition {
        private Integer row;
        private Integer column;
        private Integer order;
    }

    /** Represents styling information for a button. */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ButtonStyling {
        private String backgroundColor;
        private String textColor;
        private String size;
        private String customClass;
    }
}
