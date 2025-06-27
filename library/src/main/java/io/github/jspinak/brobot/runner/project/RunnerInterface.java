package io.github.jspinak.brobot.runner.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.runner.project.TaskButton;
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
public class RunnerInterface {
    private List<TaskButton> buttons;
}