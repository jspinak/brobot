package io.github.jspinak.brobot.datatypes.primitives.text;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Text read from the screen is a stochastic variable.
 * The results of different read iterations will not always be the same.
 * To account for this variability, Text stores a list of Strings corresponding to each read.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Text {

    List<String> strings = new ArrayList<>();

    public void add(String str) {
        strings.add(str);
    }

    public void addAll(Text text) {
        strings.addAll(text.getAll());
    }

    @JsonIgnore
    public List<String> getAll() {
        return strings;
    }

    public int size() {
        return strings.size();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return size() == 0;
    }

    @JsonIgnore
    public String get(int position) {
        return strings.get(position);
    }
}
