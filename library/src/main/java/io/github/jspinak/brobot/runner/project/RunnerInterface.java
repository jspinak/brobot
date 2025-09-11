package io.github.jspinak.brobot.runner.project;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the automation configuration for UI buttons in the Brobot Runner. Maps to the
 * "automation" object in the project schema.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RunnerInterface {
    private List<TaskButton> buttons;
}
