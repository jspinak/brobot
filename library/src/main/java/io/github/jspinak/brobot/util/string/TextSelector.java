package io.github.jspinak.brobot.util.string;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Text;

/**
 * Selects the most appropriate string from a stochastic Text object using various strategies.
 *
 * <p>This component addresses the challenge of selecting a single string from a Text object that
 * contains multiple string variations. In automation scenarios, OCR or text detection may produce
 * multiple candidates for the same text region, and this class helps choose the most likely correct
 * one.
 *
 * <p>Selection strategies:
 *
 * <ul>
 *   <li><b>RANDOM</b>: Selects a random string from the candidates
 *   <li><b>MOST_SIMILAR</b>: Selects the string most similar to all others
 * </ul>
 *
 * <p>Algorithm details:
 *
 * <ul>
 *   <li>MOST_SIMILAR calculates pairwise similarity scores between all strings
 *   <li>The string with the highest cumulative similarity to all others is selected
 *   <li>This approach assumes the correct text appears similar to most variations
 * </ul>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>OCR result disambiguation when multiple readings exist
 *   <li>Selecting consensus text from multiple detection attempts
 *   <li>Handling stochastic text variations in UI automation
 * </ul>
 *
 * <p>Thread safety: Not thread-safe due to Random instance creation in random method. Consider
 * using ThreadLocalRandom for better concurrency.
 *
 * @see Text
 * @see StringSimilarity
 */
@Component
public class TextSelector {

    /**
     * Strategy pattern implementation mapping selection methods to their implementations.
     * Initialized in instance initializer block for clarity and compile-time safety.
     */
    private Map<Method, Function<Text, String>> methods = new HashMap<>();

    {
        methods.put(Method.RANDOM, this::random);
        methods.put(Method.MOST_SIMILAR, this::mostSimilar);
    }

    /**
     * Available text selection strategies.
     *
     * <p>
     *
     * <ul>
     *   <li>RANDOM: Quick selection with no preference, useful for testing
     *   <li>MOST_SIMILAR: Intelligent selection based on consensus similarity
     * </ul>
     */
    public enum Method {
        RANDOM,
        MOST_SIMILAR
    }

    /**
     * Selects a string from the Text object using the specified method.
     *
     * <p>This is the main entry point for text selection. The method parameter determines which
     * algorithm is used to select from multiple candidates.
     *
     * @param method the selection strategy to use
     * @param text the Text object containing string candidates
     * @return the selected string based on the chosen method
     * @throws NullPointerException if method is not found in the methods map
     */
    public String getString(Method method, Text text) {
        return methods.get(method).apply(text);
    }

    /**
     * Selects a random string from the Text object.
     *
     * <p>Uses uniform distribution for selection. Useful when:
     *
     * <ul>
     *   <li>All candidates are equally likely to be correct
     *   <li>Quick selection is more important than accuracy
     *   <li>Testing randomized behavior
     * </ul>
     *
     * <p>Performance: O(1) - Direct index access
     *
     * <p>Thread safety issue: Creates new Random instance per call. Consider using
     * ThreadLocalRandom.current() for better performance.
     *
     * @param text the Text object to select from
     * @return a randomly selected string
     */
    private String random(Text text) {
        Random rand = new Random();
        int r = rand.nextInt(text.size());
        return text.get(r);
    }

    /**
     * Selects the string most similar to all other strings in the Text object.
     *
     * <p>This method implements a consensus-based selection algorithm. It calculates the cumulative
     * similarity of each string to all others, then selects the one with the highest total
     * similarity score.
     *
     * <p>Algorithm:
     *
     * <ol>
     *   <li>For each string, calculate similarity to all other strings
     *   <li>Sum these similarities to get a consensus score
     *   <li>Select the string with the highest consensus score
     * </ol>
     *
     * <p>Edge cases:
     *
     * <ul>
     *   <li>Empty text: Returns empty string
     *   <li>1-2 strings: Returns first string (no meaningful consensus)
     *   <li>Multiple strings: Performs full consensus calculation
     * </ul>
     *
     * <p>Rationale: In OCR scenarios, the correct reading often appears similar to multiple
     * variations, while errors tend to be more unique.
     *
     * <p>Performance: O(n²) where n is the number of strings, due to pairwise comparison.
     *
     * @param text the Text object containing string candidates
     * @return the string with highest cumulative similarity to all others
     */
    private String mostSimilar(Text text) {
        if (text.size() == 0) return "";
        if (text.size() <= 2) return text.get(0);
        Map<String, Double> scores = new HashMap<>();
        for (String str : text.getAll()) {
            double sum = 0.0;
            for (String str2 : text.getAll()) {
                sum += StringSimilarity.similarity(str, str2);
            }
            scores.put(str, sum);
        }
        return Collections.max(scores.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
}
