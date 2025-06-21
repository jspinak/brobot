package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;

/**
 * Represents the absence of a match in the Brobot model-based GUI automation framework.
 * 
 * <p>NoMatch is a specialized Match subclass that explicitly represents failed search operations. 
 * Rather than using null values or empty results, NoMatch provides a concrete object that maintains 
 * the Match interface while clearly indicating that no visual element was found. This approach 
 * follows the Null Object pattern, enabling cleaner code without null checks.</p>
 * 
 * <p>Key characteristics:
 * <ul>
 *   <li><b>Zero Score</b>: Always has a match score of 0 indicating no similarity</li>
 *   <li><b>Empty Region</b>: Contains a 0x0 region at position (0,0)</li>
 *   <li><b>Named Identity</b>: Carries "no match" as its identifying name</li>
 *   <li><b>Valid Object</b>: Can be used in all contexts expecting a Match</li>
 * </ul>
 * </p>
 * 
 * <p>Benefits of NoMatch pattern:
 * <ul>
 *   <li>Eliminates null pointer exceptions in match processing</li>
 *   <li>Enables uniform handling of successful and failed searches</li>
 *   <li>Provides context about what was searched even when nothing was found</li>
 *   <li>Simplifies conditional logic in automation scripts</li>
 * </ul>
 * </p>
 * 
 * <p>Use cases:
 * <ul>
 *   <li>Representing failed Find operations without using null</li>
 *   <li>Placeholder in collections when some searches fail</li>
 *   <li>Default values in match-based data structures</li>
 *   <li>Testing and mock scenarios requiring explicit non-matches</li>
 * </ul>
 * </p>
 * 
 * <p>The Builder pattern implementation allows NoMatch instances to carry additional context 
 * such as the search image that failed to match or the scene that was searched. This information 
 * can be valuable for debugging and understanding why matches failed.</p>
 * 
 * <p>In the model-based approach, NoMatch enables robust handling of the inherent uncertainty 
 * in GUI element detection. By providing a concrete representation of "not found," the framework 
 * can gracefully handle missing elements without breaking automation flows.</p>
 * 
 * @since 1.0
 * @see Match
 * @see Matches
 * @see Find
 */
public class NoMatch extends Match {

    public NoMatch() {
        setName("no match");
        setRegion(new Region(0,0,0,0));
        setScore(0);
    }

    public static class Builder {
        private String name = "no match";
        private Region region = new Region(0,0,0,0);
        private Image searchImage;
        private Scene scene;
        private StateObjectData stateObjectData;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setRegion(Region region) {
            this.region = region;
            return this;
        }

        public Builder setSearchImage(Image searchImage) {
            this.searchImage = searchImage;
            return this;
        }

        public Builder setScene(Scene scene) {
            this.scene = scene;
            return this;
        }

        public Builder setStateObjectData(StateObjectData stateObjectData) {
            this.stateObjectData = stateObjectData;
            return this;
        }

        public Match build() {
            Match match = new Match();
            match.setName(name);
            match.setRegion(region);
            match.setSearchImage(searchImage);
            match.setScene(scene);
            match.setStateObjectData(stateObjectData);
            match.setScore(0);
            return match;
        }
    }
}
