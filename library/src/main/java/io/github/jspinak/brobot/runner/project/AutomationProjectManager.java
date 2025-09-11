package io.github.jspinak.brobot.runner.project;

import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;

import lombok.Getter;
import lombok.Setter;

/**
 * Manages the active project configuration in the Brobot framework.
 *
 * <p>AutomationProjectManager serves as the central repository for project-level configuration,
 * maintaining the currently active Project and providing mechanisms to load new projects from JSON
 * definitions. A Project in Brobot encapsulates all the configuration, states, transitions, and
 * settings needed for a complete automation solution.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li><b>Project Loading</b>: Deserializes project definitions from JSON format
 *   <li><b>Active Project Management</b>: Maintains reference to the current project
 *   <li><b>Configuration Parsing</b>: Leverages JsonParser for robust JSON handling
 *   <li><b>Project Switching</b>: Supports changing active projects at runtime
 * </ul>
 *
 * <p>Project lifecycle:
 *
 * <ol>
 *   <li>Load project configuration from JSON string or file
 *   <li>Parse and validate the project structure
 *   <li>Set as active project for the automation framework
 *   <li>All services access configuration through this manager
 * </ol>
 *
 * <p>JSON project format:
 *
 * <ul>
 *   <li>States and their configurations
 *   <li>State transitions and navigation paths
 *   <li>Project-specific settings and parameters
 *   <li>Image resources and recognition patterns
 * </ul>
 *
 * <p>Common usage patterns:
 *
 * <ul>
 *   <li>Load project at application startup
 *   <li>Switch projects for different automation tasks
 *   <li>Access project configuration from various services
 *   <li>Reload project after external modifications
 * </ul>
 *
 * <p>Error handling:
 *
 * <ul>
 *   <li>Throws ConfigurationException for invalid JSON
 *   <li>Validates project structure during loading
 *   <li>Maintains previous project on load failure
 * </ul>
 *
 * <p>In the model-based approach, AutomationProjectManager is the gateway to the automation model.
 * It transforms declarative project definitions into the runtime configuration that drives all
 * automation behavior. This separation of configuration from code enables non-programmers to create
 * and modify automation projects through visual tools or direct JSON editing.
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
     * Parser for deserializing JSON project definitions. Handles complex project structures with
     * polymorphic types.
     */
    private final ConfigurationParser jsonParser;

    /**
     * The currently active project configuration.
     *
     * <p>Contains all states, transitions, and settings for the current automation task. May be
     * null before initial project loading.
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
     *
     * <p>Deserializes the JSON into a Project object and sets it as the active project. The JSON
     * must conform to the Project schema with all required fields properly defined.
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Replaces the current active project
     *   <li>Does not backup previous project
     *   <li>May leave activeProject unchanged on parse failure
     * </ul>
     *
     * <p>Example JSON structure:
     *
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

    /**
     * Gets the list of available projects. For now, returns a simple list with a default project.
     *
     * @return List of available project names
     */
    public List<String> getAvailableProjects() {
        // TODO: Implement actual project discovery from filesystem
        return List.of("Default Project", "Test Project");
    }

    /**
     * Gets the currently active project.
     *
     * @return The current AutomationProject or null if none is loaded
     */
    public AutomationProject getCurrentProject() {
        return activeProject;
    }

    /** Refreshes the list of available projects. For now, this is a no-op. */
    public void refreshProjects() {
        // TODO: Implement actual refresh logic
        // This would scan the projects directory and update available projects
    }
}
