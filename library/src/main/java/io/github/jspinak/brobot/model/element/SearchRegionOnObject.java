package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.model.state.StateObject;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for deriving search regions from another state object's match.
 * This allows state objects to define their search areas dynamically based on
 * the location of other objects, even from different states.
 */
@Getter
@Setter
public class SearchRegionOnObject {
    
    private StateObject.Type targetType;
    private String targetStateName;
    private String targetObjectName;
    private AdjustOptions adjustments;
    private AbsoluteDimensions absoluteDimensions;

    public SearchRegionOnObject() {
        this.adjustments = new AdjustOptions();
    }

    /**
     * Adjustment options for the derived region.
     */
    @Getter
    @Setter
    public static class AdjustOptions {
        private int xAdjust = 0;
        private int yAdjust = 0;
        private int wAdjust = 0;
        private int hAdjust = 0;

        public AdjustOptions() {}

        public AdjustOptions(int xAdjust, int yAdjust, int wAdjust, int hAdjust) {
            this.xAdjust = xAdjust;
            this.yAdjust = yAdjust;
            this.wAdjust = wAdjust;
            this.hAdjust = hAdjust;
        }
    }

    /**
     * Absolute dimensions to override calculated dimensions.
     */
    @Getter
    @Setter
    public static class AbsoluteDimensions {
        private Integer width;
        private Integer height;

        public AbsoluteDimensions() {}

        public AbsoluteDimensions(Integer width, Integer height) {
            this.width = width;
            this.height = height;
        }

        public boolean hasWidth() {
            return width != null;
        }

        public boolean hasHeight() {
            return height != null;
        }
    }

    public static class Builder {
        private final SearchRegionOnObject searchRegionOnObject = new SearchRegionOnObject();

        public Builder targetType(StateObject.Type type) {
            searchRegionOnObject.targetType = type;
            return this;
        }

        public Builder targetState(String stateName) {
            searchRegionOnObject.targetStateName = stateName;
            return this;
        }

        public Builder targetObject(String objectName) {
            searchRegionOnObject.targetObjectName = objectName;
            return this;
        }

        public Builder xAdjust(int x) {
            if (searchRegionOnObject.adjustments == null) {
                searchRegionOnObject.adjustments = new AdjustOptions();
            }
            searchRegionOnObject.adjustments.xAdjust = x;
            return this;
        }

        public Builder yAdjust(int y) {
            if (searchRegionOnObject.adjustments == null) {
                searchRegionOnObject.adjustments = new AdjustOptions();
            }
            searchRegionOnObject.adjustments.yAdjust = y;
            return this;
        }

        public Builder wAdjust(int w) {
            if (searchRegionOnObject.adjustments == null) {
                searchRegionOnObject.adjustments = new AdjustOptions();
            }
            searchRegionOnObject.adjustments.wAdjust = w;
            return this;
        }

        public Builder hAdjust(int h) {
            if (searchRegionOnObject.adjustments == null) {
                searchRegionOnObject.adjustments = new AdjustOptions();
            }
            searchRegionOnObject.adjustments.hAdjust = h;
            return this;
        }

        public Builder adjustments(int x, int y, int w, int h) {
            searchRegionOnObject.adjustments = new AdjustOptions(x, y, w, h);
            return this;
        }

        public Builder width(int width) {
            if (searchRegionOnObject.absoluteDimensions == null) {
                searchRegionOnObject.absoluteDimensions = new AbsoluteDimensions();
            }
            searchRegionOnObject.absoluteDimensions.width = width;
            return this;
        }

        public Builder height(int height) {
            if (searchRegionOnObject.absoluteDimensions == null) {
                searchRegionOnObject.absoluteDimensions = new AbsoluteDimensions();
            }
            searchRegionOnObject.absoluteDimensions.height = height;
            return this;
        }

        public Builder absoluteDimensions(int width, int height) {
            searchRegionOnObject.absoluteDimensions = new AbsoluteDimensions(width, height);
            return this;
        }

        public SearchRegionOnObject build() {
            return searchRegionOnObject;
        }
    }
}