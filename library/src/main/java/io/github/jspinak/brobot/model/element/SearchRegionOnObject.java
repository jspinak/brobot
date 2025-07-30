package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.model.state.StateObject;
import lombok.Builder;
import lombok.Data;

/**
 * Configuration for deriving search regions from another state object's match.
 * This allows state objects to define their search areas dynamically based on
 * the location of other objects, even from different states.
 */
@Data
@Builder(toBuilder = true)
public class SearchRegionOnObject {
    
    private StateObject.Type targetType;
    private String targetStateName;
    private String targetObjectName;
    @Builder.Default
    private AdjustOptions adjustments = AdjustOptions.builder().build();
    private AbsoluteDimensions absoluteDimensions;

    /**
     * Adjustment options for the derived region.
     */
    @Data
    @Builder(toBuilder = true)
    public static class AdjustOptions {
        @Builder.Default
        private int xAdjust = 0;
        @Builder.Default
        private int yAdjust = 0;
        @Builder.Default
        private int wAdjust = 0;
        @Builder.Default
        private int hAdjust = 0;
    }

    /**
     * Absolute dimensions to override calculated dimensions.
     */
    @Data
    @Builder(toBuilder = true)
    public static class AbsoluteDimensions {
        private Integer width;
        private Integer height;

        public boolean hasWidth() {
            return width != null;
        }

        public boolean hasHeight() {
            return height != null;
        }
    }

}