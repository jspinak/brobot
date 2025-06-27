package io.github.jspinak.brobot.runner.project;

import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * Manages the active project configuration in the Brobot framework.
 * 
 * <p>AutomationProjectManager serves as the central repository for project-level configuration, 
 * maintaining the currently active Project and providing mechanisms to load new projects 
 * from JSON definitions. A Project in Brobot encapsulates all the configuration, states, 
 * transitions, and settings needed for a complete automation solution.</p>
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li><b>Project Loading</b>: Deserializes project definitions from JSON format</li>
 *   <li><b>Active Project Management</b>: Maintains reference to the current project</li>
 *   <li><b>Configuration Parsing</b>: Leverages JsonParser for robust JSON handling</li>
 *   <li><b>Project Switching</b>: Supports changing active projects at runtime</li>
 * </ul>
 * </p>
 * 
 * <p>Project lifecycle:
 * <ol>
 *   <li>Load project configuration from JSON string or file</li>
 *   <li>Parse and validate the project structure</li>
 *   <li>Set as active project for the automation framework</li>
 *   <li>All services access configuration through this manager</li>
 * </ol>
 * </p>
 * 
 * <p>JSON project format:
 * <ul>
 *   <li>States and their configurations</li>
 *   <li>State transitions and navigation paths</li>
 *   <li>Project-specific settings and parameters</li>
 *   <li>Image resources and recognition patterns</li>
 * </ul>
 * </p>
 * 
 * <p>Common usage patterns:
 * <ul>
 *   <li>Load project at application startup</li>
 *   <li>Switch projects for different automation tasks</li>
 *   <li>Access project configuration from various services</li>
 *   <li>Reload project after external modifications</li>
 * </ul>
 * </p>
 * 
 * <p>Error handling:
 * <ul>
 *   <li>Throws ConfigurationException for invalid JSON</li>
 *   <li>Validates project structure during loading</li>
 *   <li>Maintains previous project on load failure</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, AutomationProjectManager is the gateway to the automation model. 
 * It transforms declarative project definitions into the runtime configuration that drives 
 * all automation behavior. This separation of configuration from code enables non-programmers 
 * to create and modify automation projects through visual tools or direct JSON editing.</p>
 * 
 * @since 1.0
 * @see AutomationProject
 * @see ConfigurationParser
 * @see StateService
 * @see StateTransitionService
 */
@Component
@Getter
@Setter
public class AutomationProjectManager {
    /**
     * Parser for deserializing JSON project definitions.
     * Handles complex project structures with polymorphic types.
     */
    private final ConfigurationParser jsonParser;

    /**
     * The currently active project configuration.
     * <p>
     * Contains all states, transitions, and settings for the current
     * automation task. May be null before initial project loading.
     */
    private AutomationProject activeProject;

    /**
     * Constructs an AutomationProjectManager with required JSON parsing capability.
     *
     * @param jsonParser Parser for handling project JSON deserialization
     */
    public AutomationProjectManager(ConfigurationParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    /**
     * Loads a project from a JSON string representation.
     * <p>
     * Deserializes the JSON into a Project object and sets it as the
     * active project. The JSON must conform to the Project schema with
     * all required fields properly defined.
     * <p>
     * Side effects:
     * <ul>
     *   <li>Replaces the current active project</li>
     *   <li>Does not backup previous project</li>
     *   <li>May leave activeProject unchanged on parse failure</li>
     * </ul>
     * <p>
     * Example JSON structure:
     * <pre>
     * {
     *   "name": "MyAutomation",
     *   "states": [...],
     *   "transitions": [...],
     *   "settings": {...}
     * }
     * </pre>
     *
     * @param json JSON string containing project definition
     * @throws ConfigurationException if JSON is invalid or missing required fields
     * @see ConfigurationParser#convertJson(String, Class)
     */
    public void loadProject(String json) throws ConfigurationException {
        // Direct deserialization
        this.activeProject = jsonParser.convertJson(json, AutomationProject.class);
    }
}
