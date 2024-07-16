package io.github.jspinak.brobot.actions.methods.basicactions.find.compareImages;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CompareSize {

    /**
     * Returns an empty list if neither image fits in the other.
     * Otherwise, the enveloped pattern is returned first and the enveloping pattern second.
     * @param p1 a pattern
     * @param p2 another pattern
     * @return the enveloped pattern first or an empty list is no pattern can envelop the other.
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
