package io.github.jspinak.brobot.model.element;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Represents text extracted from GUI elements with inherent OCR variability.
 *
 * <p>Text encapsulates the stochastic nature of optical character recognition (OCR) in GUI
 * automation. Due to factors like font rendering, anti-aliasing, screen resolution, and OCR
 * algorithms, reading the same text from the screen multiple times may yield slightly different
 * results. This class captures that variability by storing multiple readings as a collection.
 *
 * <p>Key characteristics:
 *
 * <ul>
 *   <li><b>Stochastic Results</b>: Each OCR attempt may produce different strings
 *   <li><b>Multiple Readings</b>: Stores all variations encountered
 *   <li><b>Statistical Confidence</b>: More readings improve reliability
 *   <li><b>Variation Tracking</b>: Identifies common OCR errors and patterns
 * </ul>
 *
 * <p>Sources of variability:
 *
 * <ul>
 *   <li>Font anti-aliasing and subpixel rendering
 *   <li>Screen scaling and DPI settings
 *   <li>Background colors and contrast
 *   <li>Character spacing and kerning
 *   <li>OCR engine confidence thresholds
 * </ul>
 *
 * <p>Common use patterns:
 *
 * <ul>
 *   <li>Multiple OCR attempts for critical text verification
 *   <li>Fuzzy matching against expected values
 *   <li>Consensus determination from multiple readings
 *   <li>Error pattern analysis for specific fonts or contexts
 * </ul>
 *
 * <p>Example OCR variations:
 *
 * <ul>
 *   <li>"Submit" → ["Submit", "Subrnit", "Submit"]
 *   <li>"$100.00" → ["$100.00", "$100,00", "S100.00"]
 *   <li>"I/O Error" → ["I/O Error", "l/O Error", "I/0 Error"]
 * </ul>
 *
 * <p>In the model-based approach, Text objects acknowledge the inherent uncertainty in visual text
 * recognition. By capturing multiple readings, the framework can make more informed decisions about
 * text content, implement retry strategies, and handle OCR errors gracefully. This is crucial for
 * robust automation in real-world GUIs where perfect text recognition cannot be guaranteed.
 *
 * @since 1.0
 * @see StateText
 * @see MockText
 * @see StateString
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
