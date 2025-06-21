package io.github.jspinak.brobot.datatypes.primitives.location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
/**
 * Collection of Anchor objects for defining complex relative positioning constraints.
 * 
 * <p>Anchors manages multiple Anchor objects, enabling sophisticated spatial relationships 
 * between visual elements. This collection allows defining regions with multiple constraints, 
 * creating more precise and flexible positioning rules than single anchors allow.</p>
 * 
 * <p>Key capabilities:
 * <ul>
 *   <li><b>Multiple Constraints</b>: Apply several positioning rules simultaneously</li>
 *   <li><b>Complex Shapes</b>: Define non-rectangular or irregular regions</li>
 *   <li><b>Redundant Positioning</b>: Use multiple anchors for robustness</li>
 *   <li><b>Dynamic Adjustment</b>: Adapt to varying element arrangements</li>
 * </ul>
 * </p>
 * 
 * <p>Common patterns:
 * <ul>
 *   <li>Two anchors to define a rectangular region between elements</li>
 *   <li>Four anchors for precise boundary definition</li>
 *   <li>Multiple anchors for average positioning (center of mass)</li>
 *   <li>Fallback anchors when primary references may be absent</li>
 * </ul>
 * </p>
 * 
 * <p>Use cases:
 * <ul>
 *   <li>Defining regions bounded by multiple visual landmarks</li>
 *   <li>Creating adaptive search areas in complex layouts</li>
 *   <li>Establishing relationships with multiple reference points</li>
 *   <li>Building fault-tolerant positioning strategies</li>
 * </ul>
 * </p>
 * 
 * <p>Example scenario - Form field bounded by multiple elements:
 * <ul>
 *   <li>Anchor 1: Label's right edge defines field's left boundary</li>
 *   <li>Anchor 2: Submit button's top edge defines field's bottom boundary</li>
 *   <li>Result: Input field positioned relative to both label and button</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Anchors collections enable expressing complex spatial 
 * relationships that mirror real GUI layouts. This allows automation to handle sophisticated 
 * interfaces where elements have multiple spatial dependencies, maintaining robustness 
 * across different screen configurations and dynamic content.</p>
 * 
 * @since 1.0
 * @see Anchor
 * @see StateObject
 * @see Pattern
 * @see StateRegion
 * @see StateLocation
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Anchors {

    private List<Anchor> anchorList = new ArrayList<>();

    public void add(Anchor anchor) {
        anchorList.add(anchor);
    }

    public int size() {
        return anchorList.size();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Anchors:");
        anchorList.forEach(anchor -> stringBuilder.append(" ").append(anchor));
        return stringBuilder.toString();
    }

}
