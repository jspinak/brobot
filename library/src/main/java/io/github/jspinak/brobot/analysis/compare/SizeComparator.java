package io.github.jspinak.brobot.analysis.compare;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Pattern;

/**
 * Determines size relationships between Pattern objects for image comparison operations.
 *
 * <p>This utility class analyzes the dimensional relationships between two Pattern objects to
 * determine if one can be completely contained within the other. This is a critical prerequisite
 * for image comparison operations where the smaller image needs to be searched for within the
 * larger image.
 *
 * <p>The class implements a strict containment check where one pattern must be able to completely
 * envelop the other in both width and height dimensions. Partial containment (where one dimension
 * fits but not the other) is not supported.
 *
 * @see Pattern
 * @see ImageComparer
 */
@Component
public class SizeComparator {

    /**
     * Determines if one Pattern can completely contain the other based on dimensions.
     *
     * <p>This method performs a strict containment check where one pattern must be smaller than or
     * equal to the other in both width AND height. If neither pattern can completely contain the
     * other (e.g., p1 is wider but p2 is taller), an empty list is returned.
     *
     * <p>When containment is possible, the returned list follows a specific order:
     *
     * <ul>
     *   <li>Index 0: The smaller (enveloped) pattern
     *   <li>Index 1: The larger (enveloping) pattern
     * </ul>
     *
     * <p>This ordering convention is relied upon by {@link ImageComparer} to set up the search
     * operation correctly.
     *
     * @param p1 The first Pattern to compare. Must not be null and must have a valid BufferedImage.
     * @param p2 The second Pattern to compare. Must not be null and must have a valid
     *     BufferedImage.
     * @return A list containing [smaller pattern, larger pattern] if one can contain the other, or
     *     an empty list if no complete containment is possible.
     * @throws NullPointerException if either pattern or their BufferedImages are null.
     */
    public List<Pattern> getEnvelopedFirstOrNone(Pattern p1, Pattern p2) {
        List<Pattern> patterns = new ArrayList<>();
        int p1w = p1.getBImage().getWidth();
        int p1h = p1.getBImage().getHeight();
        int p2w = p2.getBImage().getWidth();
        int p2h = p2.getBImage().getHeight();
        // if the width is greater and height smaller, or vice-versa, there's no fit.
        if (p1w > p2w && p1h < p2h) return patterns;
        if (p1w < p2w && p1h > p2h) return patterns;
        // if both are greater than or equal, that's the bigger one.
        if (p1w >= p2w && p1h >= p2h) {
            patterns.add(p2);
            patterns.add(p1);
            return patterns;
        }
        patterns.add(p1);
        patterns.add(p2);
        return patterns;
    }
}
