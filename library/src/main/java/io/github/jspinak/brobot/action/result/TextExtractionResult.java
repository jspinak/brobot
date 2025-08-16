package io.github.jspinak.brobot.action.result;

import io.github.jspinak.brobot.model.element.Text;
import io.github.jspinak.brobot.model.match.Match;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages text extraction results from action execution.
 * Handles accumulated text, selected text, and match-specific text.
 * 
 * This class encapsulates all text-related functionality that was
 * previously embedded in ActionResult.
 * 
 * @since 2.0
 */
@Data
public class TextExtractionResult {
    private Text accumulatedText = new Text();
    private String selectedText = "";
    private Map<Match, String> matchTextMap = new HashMap<>();
    
    /**
     * Creates an empty TextExtractionResult.
     */
    public TextExtractionResult() {}
    
    /**
     * Adds text to the accumulated text collection.
     * 
     * @param text Text to add
     */
    public void addText(String text) {
        if (text != null && !text.isEmpty()) {
            accumulatedText.add(text);
        }
    }
    
    /**
     * Adds text associated with a specific match.
     * 
     * @param match The match that produced the text
     * @param text The extracted text
     */
    public void addMatchText(Match match, String text) {
        if (match != null && text != null) {
            matchTextMap.put(match, text);
            addText(text);
        }
    }
    
    /**
     * Sets the selected/highlighted text.
     * 
     * @param text The selected text
     */
    public void setSelectedText(String text) {
        this.selectedText = text != null ? text : "";
    }
    
    /**
     * Gets all accumulated text as a single string.
     * 
     * @return Combined text with space separation
     */
    public String getCombinedText() {
        if (accumulatedText.isEmpty()) {
            return "";
        }
        return String.join(" ", accumulatedText.getAll());
    }
    
    /**
     * Gets all accumulated text as individual lines.
     * 
     * @return Text as list of strings
     */
    public java.util.List<String> getTextLines() {
        return accumulatedText.getAll();
    }
    
    /**
     * Gets text extracted from a specific match.
     * 
     * @param match The match to get text for
     * @return The text from that match, or empty string if none
     */
    public String getTextFromMatch(Match match) {
        return matchTextMap.getOrDefault(match, "");
    }
    
    /**
     * Merges text results from another instance.
     * 
     * @param other The TextExtractionResult to merge
     */
    public void merge(TextExtractionResult other) {
        if (other != null) {
            accumulatedText.addAll(other.accumulatedText);
            matchTextMap.putAll(other.matchTextMap);
            
            // If this has no selected text but other does, use other's
            if (selectedText.isEmpty() && !other.selectedText.isEmpty()) {
                selectedText = other.selectedText;
            }
        }
    }
    
    /**
     * Checks if any text has been extracted.
     * 
     * @return true if text exists
     */
    public boolean hasText() {
        return !accumulatedText.isEmpty() || !selectedText.isEmpty() || !matchTextMap.isEmpty();
    }
    
    /**
     * Checks if selected text exists.
     * 
     * @return true if selected text is not empty
     */
    public boolean hasSelectedText() {
        return !selectedText.isEmpty();
    }
    
    /**
     * Gets the total number of text segments.
     * 
     * @return Count of text segments
     */
    public int getTextCount() {
        return accumulatedText.size();
    }
    
    /**
     * Clears all text data.
     */
    public void clear() {
        accumulatedText = new Text();
        selectedText = "";
        matchTextMap.clear();
    }
    
    /**
     * Gets all unique text values from matches.
     * 
     * @return Set of unique text values
     */
    public java.util.Set<String> getUniqueMatchTexts() {
        return matchTextMap.values().stream()
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toSet());
    }
    
    /**
     * Formats the text results as a string summary.
     * 
     * @return Formatted text summary
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        
        if (hasSelectedText()) {
            sb.append("Selected: \"").append(selectedText).append("\"");
        }
        
        if (!accumulatedText.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("Extracted: ").append(getTextCount()).append(" segments");
            
            String combined = getCombinedText();
            if (combined.length() <= 100) {
                sb.append(" (\"").append(combined).append("\")");
            } else {
                sb.append(" (\"").append(combined.substring(0, 97)).append("...\")");
            }
        }
        
        if (!matchTextMap.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("Match texts: ").append(matchTextMap.size());
        }
        
        return sb.length() > 0 ? sb.toString() : "No text extracted";
    }
    
    @Override
    public String toString() {
        return format();
    }
}