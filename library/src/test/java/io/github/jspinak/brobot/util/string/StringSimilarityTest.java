package io.github.jspinak.brobot.util.string;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.test.BrobotTestBase;

public class StringSimilarityTest extends BrobotTestBase {

    @Test
    public void testIdenticalStrings() {
        String base = "Click Here";

        double similarity = StringSimilarity.similarity(base, base);

        assertEquals(1.0, similarity, 0.001, "Identical strings should have similarity of 1.0");
    }

    @Test
    public void testCaseVariations() {
        String base = "Click Here";
        String caps = "CLICK HERE";
        String lower = "click here";

        double baseToCaps = StringSimilarity.similarity(base, caps);
        double baseToLower = StringSimilarity.similarity(base, lower);
        double capsToLower = StringSimilarity.similarity(caps, lower);

        assertTrue(baseToCaps > 0.8, "Case variations should have high similarity");
        assertTrue(baseToLower > 0.8, "Case variations should have high similarity");
        assertTrue(capsToLower > 0.8, "Case variations should have high similarity");
    }

    @Test
    public void testConsensusScores() {
        String base = "Click Here";
        String caps = "CLICK HERE";
        String lower = "click here";

        // Each variation appears as many times as itself in the array [base, base, base, caps,
        // lower]
        // Consensus score for each would be: sum of similarities to all variations
        double clickHereScore =
                3 * StringSimilarity.similarity(base, base)
                        + StringSimilarity.similarity(base, caps)
                        + StringSimilarity.similarity(base, lower);

        double clickCAPSScore =
                3 * StringSimilarity.similarity(caps, base)
                        + StringSimilarity.similarity(caps, caps)
                        + StringSimilarity.similarity(caps, lower);

        double clickLowerScore =
                3 * StringSimilarity.similarity(lower, base)
                        + StringSimilarity.similarity(lower, caps)
                        + StringSimilarity.similarity(lower, lower);

        // Actually all three scores should be equal because similarity is symmetric
        // and each has 3 * 1.0 (self-similarity) + 2 * similarity to other variations
        // So the scores should be very close to each other
        double tolerance = 0.01;
        assertEquals(
                clickHereScore,
                clickCAPSScore,
                tolerance,
                "Consensus scores should be equal due to symmetry");
        assertEquals(
                clickHereScore,
                clickLowerScore,
                tolerance,
                "Consensus scores should be equal due to symmetry");
    }

    @Test
    public void testCompletelyDifferentStrings() {
        String str1 = "abc";
        String str2 = "xyz";

        double similarity = StringSimilarity.similarity(str1, str2);

        assertTrue(similarity < 0.5, "Completely different strings should have low similarity");
    }

    @Test
    public void testEmptyStrings() {
        String empty = "";
        String nonEmpty = "test";

        double emptyToEmpty = StringSimilarity.similarity(empty, empty);
        double emptyToNonEmpty = StringSimilarity.similarity(empty, nonEmpty);

        assertEquals(1.0, emptyToEmpty, 0.001, "Two empty strings should be identical");
        assertEquals(0.0, emptyToNonEmpty, 0.001, "Empty to non-empty should have 0 similarity");
    }
}
