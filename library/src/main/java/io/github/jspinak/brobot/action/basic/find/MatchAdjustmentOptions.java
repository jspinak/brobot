package io.github.jspinak.brobot.action.basic.find;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import lombok.Builder;
import lombok.Getter;

/**
 * Configuration for adjusting the position and dimensions of found matches.
 * <p>
 * This class encapsulates all parameters for post-processing the region of a {@link io.github.jspinak.brobot.model.match.Match}.
 * It allows for dynamic resizing or targeting of specific points within a match, providing
 * flexibility for subsequent actions like clicks or drags.
 * <p>
 * It is an immutable object designed to be composed within other {@code Options} classes.
 */
@Getter
@Builder(toBuilder = true, builderClassName = "Builder", builderMethodName = "builder", buildMethodName = "build", setterPrefix = "set")
@JsonDeserialize(builder = MatchAdjustmentOptions.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class MatchAdjustmentOptions {

    /**
     * Target position within a match's bounds (e.g., CENTER, TOP_LEFT).
     * This overrides any default position defined in the search pattern.
     */
    private final Position targetPosition;
    
    /**
     * Pixel offset from the calculated target position.
     * Useful for interacting near, but not directly on, an element.
     */
    private final Location targetOffset;
    
    /**
     * Number of pixels to add to the width of the match region.
     */
    @lombok.Builder.Default
    private final int addW = 0;
    
    /**
     * Number of pixels to add to the height of the match region.
     */
    @lombok.Builder.Default
    private final int addH = 0;
    
    /**
     * Absolute width of the match region, overriding its original width.
     * A value less than 0 disables this setting.
     */
    @lombok.Builder.Default
    private final int absoluteW = -1;
    
    /**
     * Absolute height of the match region, overriding its original height.
     * A value less than 0 disables this setting.
     */
    @lombok.Builder.Default
    private final int absoluteH = -1;
    
    /**
     * Number of pixels to add to the x-coordinate of the match region's origin.
     */
    @lombok.Builder.Default
    private final int addX = 0;
    
    /**
     * Number of pixels to add to the y-coordinate of the match region's origin.
     */
    @lombok.Builder.Default
    private final int addY = 0;

    /**
     * Builder class for MatchAdjustmentOptions.
     * Annotated for Jackson deserialization support.
     */
    @JsonPOJOBuilder(withPrefix = "set")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        // Lombok generates the implementation
    }
}