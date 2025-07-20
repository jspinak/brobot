package io.github.jspinak.brobot.runner.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.runner.project.AutomationConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive container for an entire Brobot automation project.
 * 
 * <p>AutomationProject represents a complete automation solution, encapsulating all states, 
 * transitions, configurations, and metadata needed to automate a specific application 
 * or workflow. It serves as the top-level organizational unit, enabling projects to 
 * be saved, loaded, shared, and version-controlled as cohesive units. This facilitates 
 * project management, collaboration, and deployment of automation solutions.</p>
 * 
 * <p>Core components:
 * <ul>
 *   <li><b>States</b>: Complete collection of application states</li>
 *   <li><b>State Transitions</b>: All defined navigation paths between states</li>
 *   <li><b>Automation UI</b>: User interface configuration for the automation</li>
 *   <li><b>Configuration</b>: Project-specific settings and parameters</li>
 *   <li><b>Metadata</b>: Descriptive information and versioning</li>
 * </ul>
 * </p>
 * 
 * <p>Metadata fields:
 * <ul>
 *   <li><b>Identity</b>: Name, ID, version for unique identification</li>
 *   <li><b>Attribution</b>: Author, organization, license information</li>
 *   <li><b>Temporal</b>: Creation and update timestamps</li>
 *   <li><b>Documentation</b>: Description, website, custom properties</li>
 * </ul>
 * </p>
 * 
 * <p>Project lifecycle:
 * <ol>
 *   <li>Creation: Define states and transitions</li>
 *   <li>Configuration: Set project parameters</li>
 *   <li>Development: Iterate on automation logic</li>
 *   <li>Testing: Validate automation behavior</li>
 *   <li>Deployment: Export for production use</li>
 *   <li>Maintenance: Update for application changes</li>
 * </ol>
 * </p>
 * 
 * <p>Serialization support:
 * <ul>
 *   <li>JSON serialization for file storage</li>
 *   <li>Database persistence for large projects</li>
 *   <li>Ignore unknown properties for compatibility</li>
 *   <li>Reset capability for clean reinitialization</li>
 * </ul>
 * </p>
 * 
 * <p>Custom properties usage:
 * <ul>
 *   <li>Application-specific configuration</li>
 *   <li>Environment settings (dev, test, prod)</li>
 *   <li>User credentials or API keys</li>
 *   <li>Feature flags and toggles</li>
 *   <li>Extended metadata</li>
 * </ul>
 * </p>
 * 
 * <p>Version control benefits:
 * <ul>
 *   <li>Track automation evolution over time</li>
 *   <li>Collaborate on automation development</li>
 *   <li>Roll back to previous versions</li>
 *   <li>Branch for different environments</li>
 *   <li>Merge automation improvements</li>
 * </ul>
 * </p>
 * 
 * <p>Example project structure:
 * <pre>
 * Project: "E-Commerce Automation"
 * - States: Login, Browse, Product, Cart, Checkout
 * - Transitions: Navigation paths between all states
 * - Configuration: Timeouts, retry policies, logging
 * - Automation UI: Control panel for operators
 * - Version: "2.1.0"
 * - Author: "Automation Team"
 * </pre>
 * </p>
 * 
 * <p>In the model-based approach, AutomationProject serves as the complete representation of 
 * an automation solution. It encapsulates the entire state model (Ω), transition 
 * graph, and execution parameters, making automation solutions portable, maintainable, 
 * and shareable across teams and environments.</p>
 * 
 * @since 1.0
 * @see State
 * @see StateTransitions
 * @see RunnerInterface
 * @see AutomationConfiguration
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutomationProject {
    private Long id = 0L;
    private String name;
    private String description;
    private String version;
    private String author;
    private LocalDateTime created;
    private LocalDateTime updated;
    private List<State> states;
    private List<StateTransitions> stateTransitions;
    private RunnerInterface automation;
    private AutomationConfiguration configuration;
    private String organization;
    private String website;
    private String license;
    private String createdDate;
    private Map<String, Object> customProperties = new HashMap<>();

    public void reset() {
        id = null;
        name = null;
        description = null;
        version = null;
        author = null;
        created = null;
        updated = null;
        states = null;
        stateTransitions = null;
        automation = null;
        configuration = null;
        organization = null;
        website = null;
        license = null;
        createdDate = null;
        customProperties = new HashMap<>();
    }
    
    /**
     * Gets the list of automation names from the automation UI.
     * 
     * @return List of automation names, or empty list if no automation is set
     */
    public List<String> getAutomationNames() {
        if (automation != null && automation.getButtons() != null) {
            return automation.getButtons().stream()
                    .map(TaskButton::getLabel)
                    .toList();
        }
        return List.of();
    }
    
    /**
     * Gets the list of automations (task buttons) from the automation UI.
     * 
     * @return List of TaskButton objects, or empty list if no automation is set
     */
    public List<TaskButton> getAutomations() {
        if (automation != null && automation.getButtons() != null) {
            return automation.getButtons();
        }
        return List.of();
    }

}