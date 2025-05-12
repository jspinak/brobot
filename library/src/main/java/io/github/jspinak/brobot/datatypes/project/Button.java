package io.github.jspinak.brobot.datatypes.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a UI button for the desktop runner that calls automation functions.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Button {
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
     * Represents position information for a button in the UI.
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ButtonPosition {
        private Integer row;
        private Integer column;
        private Integer order;
    }

    /**
     * Represents styling information for a button.
     */
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