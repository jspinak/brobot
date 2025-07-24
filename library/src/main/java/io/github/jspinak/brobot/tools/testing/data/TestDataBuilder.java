package io.github.jspinak.brobot.tools.testing.data;

import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Builder for structured test data with versioning and variation support.
 * <p>
 * This builder provides a fluent interface for creating comprehensive test scenarios
 * with baseline data and controlled variations. It supports:
 * <ul>
 * <li>Baseline test data with known good configurations</li>
 * <li>Systematic variations for edge case testing</li>
 * <li>Version control for test data evolution</li>
 * <li>Reusable components for common test patterns</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * TestScenario scenario = testDataBuilder.scenario("login_flow")
 *     .withBaselineData()
 *     .withStateImage("login_button", "login_btn.png")
 *     .withRegion("search_area", new Region(100, 100, 200, 150))
 *     .withVariation("small_screen")
 *         .modifyRegion("search_area", r -> r.resize(0.8))
 *         .modifyImage("login_button", img -> img.withSimilarity(0.9))
 *     .withVariation("high_contrast")
 *         .modifyImage("login_button", img -> img.withSimilarity(0.6))
 *     .build();
 * }</pre>
 *
 * @see TestScenario
 * @see TestVariation
 */
@Component
public class TestDataBuilder {
    
    private final Map<String, TestScenario> baselineScenarios = new HashMap<>();
    
    /**
     * Creates a new test scenario builder with the specified name.
     *
     * @param name unique name for the test scenario
     * @return new scenario builder instance
     */
    public TestScenario.Builder scenario(String name) {
        return new TestScenario.Builder(name, this);
    }
    
    /**
     * Gets a previously created baseline scenario.
     *
     * @param name name of the scenario
     * @return the baseline scenario or null if not found
     */
    public TestScenario getBaselineScenario(String name) {
        return baselineScenarios.get(name);
    }
    
    /**
     * Stores a baseline scenario for reuse.
     *
     * @param name scenario name
     * @param scenario the scenario to store
     */
    void storeBaselineScenario(String name, TestScenario scenario) {
        baselineScenarios.put(name, scenario);
    }
    
    /**
     * Creates a quick scenario with common login elements.
     *
     * @return pre-configured login scenario builder
     */
    public TestScenario.Builder loginScenario() {
        return scenario("login_flow")
            .withStateImage("username_field", "username_input.png")
            .withStateImage("password_field", "password_input.png")
            .withStateImage("login_button", "login_btn.png")
            .withStateString("username_text", "Username:")
            .withStateString("password_text", "Password:")
            .withRegion("login_form", new Region(300, 200, 400, 300));
    }
    
    /**
     * Creates a quick scenario with navigation elements.
     *
     * @return pre-configured navigation scenario builder
     */
    public TestScenario.Builder navigationScenario() {
        return scenario("navigation_flow")
            .withStateImage("back_button", "back_arrow.png")
            .withStateImage("forward_button", "forward_arrow.png")
            .withStateImage("menu_button", "hamburger_menu.png")
            .withRegion("nav_bar", new Region(0, 0, 1200, 80));
    }
    
    /**
     * Creates a quick scenario for form testing.
     *
     * @return pre-configured form scenario builder
     */
    public TestScenario.Builder formScenario() {
        return scenario("form_flow")
            .withStateImage("submit_button", "submit_btn.png")
            .withStateImage("cancel_button", "cancel_btn.png")
            .withStateImage("reset_button", "reset_btn.png")
            .withRegion("form_area", new Region(200, 150, 600, 500));
    }
    
    /**
     * Creates common variations that can be applied to any scenario.
     *
     * @return map of variation names to variation builders
     */
    public Map<String, TestVariation.Builder> commonVariations() {
        Map<String, TestVariation.Builder> variations = new HashMap<>();
        
        variations.put("small_screen", new TestVariation.Builder("small_screen")
            .withDescription("Mobile or small screen layout")
            .withTransformation("scale_down", (name, obj) -> {
                if (obj instanceof StateImage) {
                    // TODO: StateImage doesn't have toBuilder() or getSimilarity() methods
                    // Need to update this transformation once the API is clarified
                    return obj;
                }
                return obj;
            }));
            
        variations.put("high_dpi", new TestVariation.Builder("high_dpi")
            .withDescription("High DPI display testing")
            .withTransformation("adjust_similarity", (name, obj) -> {
                if (obj instanceof StateImage) {
                    // TODO: StateImage doesn't have toBuilder() or getSimilarity() methods
                    // Need to update this transformation once the API is clarified
                    return obj;
                }
                return obj;
            }));
            
        variations.put("slow_system", new TestVariation.Builder("slow_system")
            .withDescription("Slow system response simulation")
            .withTransformation("increase_timeouts", (name, obj) -> {
                // Could modify timeout-related properties if available
                return obj;
            }));
            
        return variations;
    }
    
    /**
     * Validates that all required test data elements are present.
     *
     * @param scenario the scenario to validate
     * @return list of validation errors (empty if valid)
     */
    public List<String> validateScenario(TestScenario scenario) {
        List<String> errors = new ArrayList<>();
        
        if (scenario.getName() == null || scenario.getName().trim().isEmpty()) {
            errors.add("Scenario name is required");
        }
        
        if (scenario.getStateImages().isEmpty() && 
            scenario.getStateStrings().isEmpty() && 
            scenario.getRegions().isEmpty()) {
            errors.add("Scenario must contain at least one test element");
        }
        
        // Validate state images have patterns
        scenario.getStateImages().forEach((name, image) -> {
            if (image.getPatterns() == null || image.getPatterns().isEmpty()) {
                errors.add("StateImage '" + name + "' missing patterns");
            }
        });
        
        // Validate regions have valid dimensions
        scenario.getRegions().forEach((name, region) -> {
            if (region.w() <= 0 || region.h() <= 0) {
                errors.add("Region '" + name + "' has invalid dimensions");
            }
        });
        
        return errors;
    }
}