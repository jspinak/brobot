package io.github.jspinak.brobot.datatypes.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.dsl.AutomationFunction;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * Represents the project configuration settings and references to automation functions.
 * Maps to the configuration object in project-schema.json and includes a reference to
 * automationFunctions from automation-dsl-schema.json.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectConfiguration {
    // Configuration settings from project-schema.json
    private Double minSimilarity = 0.7;
    private Double moveMouseDelay = 0.5;
    private Double delayBeforeMouseDown = 0.3;
    private Double delayAfterMouseDown = 0.3;
    private Double delayBeforeMouseUp = 0.3;
    private Double delayAfterMouseUp = 0.3;
    private Double typeDelay = 0.3;
    private Double pauseBetweenActions = 0.5;
    private Double maxWait = 10.0;
    private String imageDirectory;
    private String logLevel = "INFO";
    private Boolean illustrationEnabled = true;

    // Reference to automation functions defined elsewhere
    private List<AutomationFunction> automationFunctions;
}