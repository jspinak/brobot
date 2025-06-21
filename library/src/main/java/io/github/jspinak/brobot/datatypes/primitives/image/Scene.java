package io.github.jspinak.brobot.datatypes.primitives.image;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a captured screenshot or screen state as a searchable pattern.
 * 
 * <p>Scene encapsulates a full or partial screenshot that serves as a reference 
 * image for pattern matching operations. Unlike individual Pattern objects that 
 * typically represent specific UI elements, a Scene captures a broader view of 
 * the application state, providing context for finding multiple patterns within 
 * a single screen capture.</p>
 * 
 * <p>Key characteristics:
 * <ul>
 *   <li><b>Screenshot Storage</b>: Holds a complete or partial screen capture</li>
 *   <li><b>Pattern Container</b>: Wraps the screenshot as a searchable Pattern</li>
 *   <li><b>Persistent Identity</b>: Maintains database ID for tracking and logging</li>
 *   <li><b>Context Provider</b>: Offers broader view than individual elements</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Storing reference screenshots for state verification</li>
 *   <li>Creating mock environments from captured scenes</li>
 *   <li>Analyzing screen layouts and element positions</li>
 *   <li>Debugging by comparing expected vs actual scenes</li>
 *   <li>Building training data for pattern recognition</li>
 * </ul>
 * </p>
 * 
 * <p>Relationship to other components:
 * <ul>
 *   <li>Contains a Pattern object for the screenshot image</li>
 *   <li>Can be searched for smaller patterns within the scene</li>
 *   <li>Used by mock systems to simulate screen states</li>
 *   <li>Stored in databases for historical analysis</li>
 * </ul>
 * </p>
 * 
 * <p>Typical workflow:
 * <ol>
 *   <li>Capture screenshot of current application state</li>
 *   <li>Create Scene with the captured image</li>
 *   <li>Store Scene for later reference or analysis</li>
 *   <li>Search within Scene for specific patterns</li>
 *   <li>Use Scene for mock testing or verification</li>
 * </ol>
 * </p>
 * 
 * <p>In the model-based approach, Scene objects bridge the gap between abstract 
 * state representations and concrete visual captures. They provide the visual 
 * evidence of states, enable offline analysis, and support mock testing scenarios 
 * where live application interaction isn't available or desired.</p>
 * 
 * @since 1.0
 * @see Pattern
 * @see Image
 * @see StateImage
 * @see SceneCreator
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Scene {

    private Long id = -1L;
    private Pattern pattern;

    public Scene(Pattern pattern) {
        this.pattern = pattern;
    }

    public Scene(String filename) {
        this.pattern = new Pattern(filename);
    }

    public Scene() { // for JSON deserialization
        this.pattern = new Pattern();
    }

    @Override
    public String toString() {
        return "Scene{" +
                "id=" + id +
                ", pattern=" + (pattern != null ? pattern.getName() : "null") +
                '}';
    }
}
