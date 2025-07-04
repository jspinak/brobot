package io.github.jspinak.brobot.util.string;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

/**
 * Calculates string similarity using the Levenshtein distance algorithm.
 * <p>
 * This component provides methods to compute similarity scores between strings
 * based on their edit distance. The similarity score ranges from 0.0 (completely
 * different) to 1.0 (identical), making it useful for fuzzy string matching.
 * <p>
 * Algorithm details:
 * <ul>
 * <li>Uses Levenshtein distance (minimum edit operations needed)</li>
 * <li>Normalizes by the longer string's length for consistent scoring</li>
 * <li>Case-insensitive comparison in edit distance calculation</li>
 * <li>Optimized space complexity implementation</li>
 * </ul>
 * <p>
 * Similarity formula:
 * <pre>
 * similarity = (longerLength - editDistance) / longerLength
 * </pre>
 * <p>
 * Use cases:
 * <ul>
 * <li>OCR result validation and selection</li>
 * <li>Fuzzy text matching in UI automation</li>
 * <li>Detecting typos or variations in user input</li>
 * <li>Finding best matches in string collections</li>
 * <li>Duplicate detection with tolerance</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 * <li>Time complexity: O(m × n) where m, n are string lengths</li>
 * <li>Space complexity: O(min(m, n)) - optimized implementation</li>
 * <li>Suitable for moderate string lengths</li>
 * </ul>
 * <p>
 * Based on: https://stackoverflow.com/questions/955110/similarity-string-comparison-in-java/16018452
 * <p>
 * Thread safety: All methods are stateless and thread-safe.
 *
 * @see TextSelector
 */
@Component
public class StringSimilarity {

    /**
     * Calculates the similarity score between two strings.
     * <p>
     * Returns a normalized score between 0.0 and 1.0, where:
     * <ul>
     * <li>1.0 = Identical strings</li>
     * <li>0.5 = Half the characters need changing</li>
     * <li>0.0 = Completely different (edit distance equals longer length)</li>
     * </ul>
     * <p>
     * Algorithm steps:
     * <ol>
     * <li>Identify longer and shorter strings</li>
     * <li>Calculate edit distance between them</li>
     * <li>Normalize by longer string's length</li>
     * </ol>
     * <p>
     * Examples:
     * <ul>
     * <li>similarity("hello", "hello") = 1.0</li>
     * <li>similarity("hello", "hallo") = 0.8</li>
     * <li>similarity("hello", "help") = 0.6</li>
     * <li>similarity("abc", "xyz") = 0.0</li>
     * </ul>
     * <p>
     * Special cases:
     * <ul>
     * <li>Both empty strings: Returns 1.0 (considered identical)</li>
     * <li>One empty string: Returns 0.0</li>
     * <li>Order independent: similarity(a,b) = similarity(b,a)</li>
     * </ul>
     *
     * @param s1 the first string to compare
     * @param s2 the second string to compare
     * @return similarity score between 0.0 and 1.0 inclusive
     */
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
        /* // If you have Apache Commons Text, you can use it to calculate the edit distance:
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        return (longerLength - levenshteinDistance.apply(longer, shorter)) / (double) longerLength; */
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    /**
     * Calculates the Levenshtein edit distance between two strings.
     * <p>
     * The edit distance is the minimum number of single-character edits
     * (insertions, deletions, or substitutions) required to transform
     * one string into another.
     * <p>
     * Implementation details:
     * <ul>
     * <li>Space-optimized dynamic programming approach</li>
     * <li>Uses single array instead of full matrix</li>
     * <li>Case-insensitive comparison (converts to lowercase)</li>
     * <li>Processes strings character by character</li>
     * </ul>
     * <p>
     * Algorithm visualization:
     * <pre>
     * s1 = "cat", s2 = "cut"
     * Edit operations: substitute 'a' with 'u'
     * Edit distance = 1
     * </pre>
     * <p>
     * Examples:
     * <ul>
     * <li>editDistance("kitten", "sitting") = 3</li>
     * <li>editDistance("saturday", "sunday") = 3</li>
     * <li>editDistance("abc", "abc") = 0</li>
     * <li>editDistance("abc", "") = 3</li>
     * </ul>
     * <p>
     * Performance notes:
     * <ul>
     * <li>Time: O(m × n) where m = s1.length(), n = s2.length()</li>
     * <li>Space: O(n) - only stores one row of the DP matrix</li>
     * <li>Lowercase conversion adds overhead but ensures consistency</li>
     * </ul>
     * <p>
     * Based on: http://rosettacode.org/wiki/Levenshtein_distance#Java
     *
     * @param s1 the source string
     * @param s2 the target string
     * @return the minimum number of edits needed to transform s1 into s2
     */
    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    /**
     * Prints a formatted similarity report for two strings.
     * <p>
     * Outputs the similarity score with 3 decimal places along with
     * the compared strings in quotes for clarity. Useful for debugging
     * and analysis of string matching results.
     * <p>
     * Output format:
     * <pre>
     * 0.857 is the similarity between "hello" and "hallo"
     * </pre>
     * <p>
     * Use cases:
     * <ul>
     * <li>Debugging OCR results</li>
     * <li>Analyzing text matching thresholds</li>
     * <li>Logging similarity calculations</li>
     * <li>Testing string comparison algorithms</li>
     * </ul>
     *
     * @param s the first string to compare
     * @param t the second string to compare
     */
    public static void printSimilarity(String s, String t) {
        ConsoleReporter.println(String.format(
                "%.3f is the similarity between \"%s\" and \"%s\"", similarity(s, t), s, t));
    }

}
