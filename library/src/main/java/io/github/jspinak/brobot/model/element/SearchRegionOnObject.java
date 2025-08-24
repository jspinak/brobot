package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.state.StateObject;
import lombok.Builder;
import lombok.Data;

/**
 * Configuration for deriving search regions from another state object's match.
 * This allows state objects to define their search areas dynamically based on
 * the location of other objects, even from different states.
 */
@Data
@Builder(toBuilder = true, setterPrefix = "set")
public class SearchRegionOnObject {
    
    private StateObject.Type targetType;
    private String targetStateName;
    private String targetObjectName;
    
    /**
     * Optional adjustments to apply to the calculated region.
     * Uses the same adjustment options as match post-processing for consistency.
     */
    private MatchAdjustmentOptions adjustments;

}