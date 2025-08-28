package io.github.jspinak.brobot.action.basic.find;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

/**
 * Configuration for match fusion operations in Find actions.
 * <p>
 * This class encapsulates all parameters related to fusing multiple adjacent matches
 * into single larger matches. Match fusion is useful when searching for UI elements
 * that may be detected as multiple separate matches but logically represent a single
 * element (e.g., text that's broken across multiple lines).
 * <p>
 * It is an immutable object designed to be composed within Find options classes
 * and should be constructed using its inner {@link Builder}.
 * <p>
 * Match fusion works by:
 * <ol>
 *   <li>Finding all individual matches using the specified search strategy</li>
 *   <li>Analyzing spatial relationships between matches</li>
 *   <li>Merging matches that are within the specified distance thresholds</li>
 *   <li>Creating new composite matches that encompass the merged regions</li>
 * </ol>
 *
 * @see BaseFindOptions
 * @see PatternFindOptions
 */
@Getter
@Builder(toBuilder = true, builderClassName = "Builder", setterPrefix = "set")
@JsonDeserialize(builder = MatchFusionOptions.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class MatchFusionOptions {

    /**
     * Defines the method for fusing multiple matches into a single larger match.
     */
    public enum FusionMethod {
        /** No fusion is performed. Matches remain separate. */
        NONE,
        /** Fuses matches based on absolute pixel distance. */
        ABSOLUTE,
        /** Fuses matches based on distance relative to their size. */
        RELATIVE
    }

    @lombok.Builder.Default
    private final FusionMethod fusionMethod = FusionMethod.NONE;
    
    @lombok.Builder.Default
    private final int maxFusionDistanceX = 5;
    
    @lombok.Builder.Default
    private final int maxFusionDistanceY = 5;
    
    @lombok.Builder.Default
    private final int sceneToUseForCaptureAfterFusingMatches = 0;

    /**
     * Builder class for MatchFusionOptions.
     * Annotated for Jackson deserialization support.
     */
    @JsonPOJOBuilder(withPrefix = "set")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        // Lombok generates the implementation
    }
}