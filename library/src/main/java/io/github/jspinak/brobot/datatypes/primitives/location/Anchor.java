package io.github.jspinak.brobot.datatypes.primitives.location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines relative positioning constraints between visual elements in Brobot.
 * 
 * <p>Anchor provides a mechanism for defining regions relative to other visual elements, 
 * enabling adaptive layouts that maintain relationships between GUI components. This is 
 * essential for creating robust automation that handles dynamic interfaces where elements 
 * may shift position while maintaining their relative arrangements.</p>
 * 
 * <p>Conceptual model:
 * <ul>
 *   <li><b>Source Point</b>: A position within a match or existing region (positionInMatch)</li>
 *   <li><b>Target Anchor</b>: Which border(s) of the new region to define (anchorInNewDefinedRegion)</li>
 *   <li>The source point becomes the anchor point for the specified border(s)</li>
 * </ul>
 * </p>
 * 
 * <p>Border definition rules:
 * <ul>
 *   <li><b>Middle positions</b> (MIDDLELEFT, TOPMIDDLE, etc.): Define one border</li>
 *   <li><b>Corner positions</b> (TOPLEFT, BOTTOMRIGHT, etc.): Define two borders</li>
 *   <li>Example: MIDDLELEFT anchor sets the left border at the source point</li>
 *   <li>Example: TOPLEFT anchor sets both left and top borders at the source point</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Defining search regions relative to found patterns</li>
 *   <li>Creating input fields positioned relative to labels</li>
 *   <li>Establishing click targets based on visual landmarks</li>
 *   <li>Building adaptive layouts for different screen resolutions</li>
 *   <li>Maintaining spatial relationships in dynamic UIs</li>
 * </ul>
 * </p>
 * 
 * <p>Example scenarios:
 * <ul>
 *   <li>Text field to the right of a label: Label's MIDDLERIGHT anchors field's MIDDLELEFT</li>
 *   <li>Button below an image: Image's BOTTOMMIDDLE anchors button's TOPMIDDLE</li>
 *   <li>Dropdown aligned with input: Input's BOTTOMLEFT anchors dropdown's TOPLEFT</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Anchors enable the framework to handle variations in 
 * GUI layouts by expressing spatial relationships declaratively. This abstraction allows 
 * automation scripts to adapt to different themes, resolutions, and dynamic content 
 * arrangements without hardcoding absolute positions.</p>
 * 
 * @since 1.0
 * @see Anchors
 * @see Position
 * @see Positions
 * @see Region
 * @see StateRegion
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Anchor {

    private Positions.Name anchorInNewDefinedRegion; // the border of the region to define
    private Position positionInMatch; // the location in the match to use as a defining point

    // JPA requires an empty constructor
    public Anchor() {}

    public Anchor(Positions.Name anchorInNewDefinedRegion, Position positionInMatch) {
        this.anchorInNewDefinedRegion = anchorInNewDefinedRegion;
        this.positionInMatch = positionInMatch;
    }

    @Override
    public String toString() {
        return "Anchor:" +
                " anchorInNewDefinedRegion=" + anchorInNewDefinedRegion +
                " positionInMatch=" + positionInMatch;
    }

}
