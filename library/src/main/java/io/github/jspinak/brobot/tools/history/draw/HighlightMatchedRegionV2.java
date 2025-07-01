package io.github.jspinak.brobot.tools.history.draw;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

import org.sikuli.script.Region;
import org.springframework.stereotype.Component;

/**
 * Provides visual highlighting functionality for matches on screen with mock support.
 * <p>
 * This V2 wrapper class works with ActionConfig instead of ActionOptions.
 * It abstracts Sikuli's highlighting operations to visually indicate
 * matched regions on the screen. It supports both persistent highlights (turnOn/turnOff)
 * and timed highlights. The class handles a Sikuli limitation where Match objects
 * don't highlight properly by converting them to Region objects.
 * <p>
 * In mock mode with no screenshots, highlighting operations are logged instead of
 * displayed. When screenshots are available in mock mode, actual highlighting is
 * performed on the screenshots for visual verification.
 * <p>
 * The class ensures minimum visibility by setting a default size of 10x10 pixels
 * for zero-dimension matches to make them visible when highlighted.
 * 
 * @see Match
 * @see HighlightOptions
 * @see FrameworkSettings#mock
 */
@Component
public class HighlightMatchedRegionV2 {

    private final HighlightMatchedRegion legacyHighlighter;
    
    public HighlightMatchedRegionV2(HighlightMatchedRegion legacyHighlighter) {
        this.legacyHighlighter = legacyHighlighter;
    }

    /**
     * Activates a persistent highlight on the specified match.
     * <p>
     * The highlight remains visible until explicitly turned off with {@link #turnOff}.
     * In mock mode without screenshots, the operation is logged instead of displayed.
     * The highlight color is determined by the action configuration.
     * 
     * @param match The match to highlight. Must not be null.
     * @param stateObject Additional state information for logging purposes in mock mode.
     * @param config Configuration containing the highlight color to use.
     */
    public void turnOn(Match match, StateObjectMetadata stateObject, ActionConfig config) {
        String highlightColor = "red"; // default
        
        if (config instanceof HighlightOptions) {
            HighlightOptions highlightOptions = (HighlightOptions) config;
            highlightColor = highlightOptions.getHighlightColor();
        }
        
        if (FrameworkSettings.mock && FrameworkSettings.screenshots.isEmpty()) {
            ConsoleReporter.println("Highlight ON: " + match + " with color: " + highlightColor);
        } else {
            match.sikuli().highlightOn(highlightColor);
        }
    }

    /**
     * Deactivates a persistent highlight on the specified match.
     * <p>
     * This method removes highlights previously activated with {@link #turnOn}.
     * In mock mode without screenshots, no action is taken as there's no
     * actual highlight to remove.
     * 
     * @param match The match to stop highlighting. Must not be null.
     */
    public void turnOff(Match match) {
        if (!FrameworkSettings.mock || !FrameworkSettings.screenshots.isEmpty()) {
            match.sikuli().highlightOff();
        }
    }

    /**
     * Displays a timed highlight on the specified match for a configured duration.
     * <p>
     * This method works around a Sikuli limitation where Match objects don't
     * highlight properly by converting the match to a Region. For zero-dimension
     * matches (e.g., point clicks), it ensures visibility by setting a minimum
     * size of 10x10 pixels.
     * <p>
     * The method performs a brief 1-second highlight on the match itself before
     * highlighting the region, which helps with visual debugging.
     * 
     * @param match The match to highlight. Zero-dimension matches are expanded
     *              to 10x10 pixels for visibility.
     * @param stateObject Additional state information for logging in mock mode.
     * @param config Configuration containing highlight duration (seconds)
     *               and color settings.
     * @return {@code true} if the highlight was displayed (or logged in mock mode),
     *         always returns {@code true} in current implementation.
     * 
     * @implNote This method logs the highlighted region dimensions for debugging purposes.
     */
    public boolean highlight(Match match, StateObjectMetadata stateObject, ActionConfig config) {
        double highlightSeconds = 1.0; // default
        String highlightColor = "red"; // default
        
        if (config instanceof HighlightOptions) {
            HighlightOptions highlightOptions = (HighlightOptions) config;
            highlightSeconds = highlightOptions.getHighlightSeconds();
            highlightColor = highlightOptions.getHighlightColor();
        }
        
        if (FrameworkSettings.mock && FrameworkSettings.screenshots.isEmpty()) {
            ConsoleReporter.println("Highlight: " + match + " for " + highlightSeconds + " seconds with color: " + highlightColor);
            return true;
        }
        
        match.sikuli().highlight(1);
        Region highlightReg = match.getRegion().sikuli();
        if (match.w() == 0) highlightReg.w = 10;
        if (match.h() == 0) highlightReg.h = 10;
        ConsoleReporter.println("in HighlightRegion: " + highlightReg);
        highlightReg.highlight(highlightSeconds, highlightColor);
        return true;
    }
}