package io.github.jspinak.brobot.stringUtils;

import io.github.jspinak.brobot.datatypes.primitives.text.Text;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * The TextSelector returns text from the stochastic Text variable (Strings are stored in a List),
 * which it believes has the highest probability of being the actual text on the screen.
 */
@Component
public class TextSelector {

    private Map<Method, Function<Text, String>> methods = new HashMap<>();
    {
        methods.put(Method.RANDOM, this::random);
        methods.put(Method.MOST_SIMILAR, this::mostSimilar);
    }

    public enum Method {
        RANDOM, MOST_SIMILAR
    }

    public String getString(Method method, Text text) {
        return methods.get(method).apply(text);
    }

    private String random(Text text) {
        Random rand = new Random();
        int r = rand.nextInt(text.size());
        return text.get(r);
    }

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
