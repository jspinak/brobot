package io.github.jspinak.brobot.datatypes.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * Represents the automation configuration for UI buttons in the Brobot Runner.
 * Maps to the "automation" object in the project schema.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutomationUI {
    private List<Button> buttons;
}