package io.github.jspinak.brobot.tools.testing.wrapper;

import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.testing.mock.action.MockText;
import io.github.jspinak.brobot.action.internal.text.GetTextWrapper;
import io.github.jspinak.brobot.config.environment.ExecutionMode;

import org.springframework.stereotype.Component;

/**
 * Wrapper for Text operations that routes to mock or live implementation.
 * 
 * This wrapper provides a stable API for text extraction operations while
 * allowing the underlying implementation to switch between mock and live modes.
 */
@Component
public class TextWrapper {
    
    private final ExecutionMode executionMode;
    private final MockText mockText;
    private final GetTextWrapper getTextWrapper;
    
    public TextWrapper(ExecutionMode executionMode,
                      MockText mockText,
                      GetTextWrapper getTextWrapper) {
        this.executionMode = executionMode;
        this.mockText = mockText;
        this.getTextWrapper = getTextWrapper;
    }
    
    /**
     * Extracts and sets text content for a match object.
     */
    public void setText(Match match) {
        if (executionMode.isMock()) {
            match.setText(mockText.getString(match));
        } else {
            getTextWrapper.setText(match);
        }
    }
}