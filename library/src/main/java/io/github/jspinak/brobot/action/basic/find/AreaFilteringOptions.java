package io.github.jspinak.brobot.action.basic.find;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration for filtering action results based on their pixel area.
 * <p>
 * This class encapsulates options for constraining matches by their minimum and maximum size.
 * It is primarily used by Find operations that do not have an inherent size, such as
 * color and motion detection, to filter out noise or irrelevant results.
 * <p>
 * It is an immutable object designed to be composed within other, more specific `Options` classes
 * like {@code ColorFindOptions} or {@code MotionFindOptions}.
 */
@Getter
@Builder(toBuilder = true)
public final class AreaFilteringOptions {

    /**
     * The minimum number of pixels for a match to be considered valid.
     * Used to filter out small, noisy results.
     */
    @Builder.Default
    private final int minArea = 1;
    
    /**
     * The maximum number of pixels for a match to be considered valid.
     * Used to filter out overly large or unintended results. 
     * A value less than or equal to 0 typically disables this check.
     */
    @Builder.Default
    private final int maxArea = -1;
}
