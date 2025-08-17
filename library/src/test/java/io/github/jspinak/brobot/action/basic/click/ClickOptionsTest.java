package io.github.jspinak.brobot.action.basic.click;

import io.github.jspinak.brobot.action.RepetitionOptions;
import io.github.jspinak.brobot.action.VerificationOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.MouseButton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ClickOptions configuration class.
 */
class ClickOptionsTest {
    
    @Test
    void builder_shouldSetDefaultValues() {
        ClickOptions options = new ClickOptions.Builder().build();
        
        assertEquals(1, options.getNumberOfClicks());
        assertNotNull(options.getMousePressOptions());
        assertNotNull(options.getVerificationOptions());
        assertNotNull(options.getRepetitionOptions());
        assertEquals(MouseButton.LEFT, options.getMousePressOptions().getButton()); // Check default button
        
        // Check convenience getters
        assertEquals(1, options.getTimesToRepeatIndividualAction());
        assertEquals(0.0, options.getPauseBetweenIndividualActions());
    }
    
    @Test
    void builder_shouldSetNumberOfClicks() {
        ClickOptions options = new ClickOptions.Builder()
            .setNumberOfClicks(3)
            .build();
        
        assertEquals(3, options.getNumberOfClicks());
    }
    
    @Test
    void builder_shouldEnforceMinimumClicks() {
        ClickOptions options = new ClickOptions.Builder()
            .setNumberOfClicks(-5)
            .build();
        
        assertEquals(1, options.getNumberOfClicks()); // Should be at least 1
    }
    
    @Test
    void builder_shouldSetMousePressOptions() {
        ClickOptions options = new ClickOptions.Builder()
            .setPressOptions(MousePressOptions.builder()
                .button(MouseButton.RIGHT)
                .pauseAfterMouseDown(0.5)
                .build())
            .build();
        
        assertEquals(MouseButton.RIGHT, options.getMousePressOptions().getButton());
        assertEquals(0.5, options.getMousePressOptions().getPauseAfterMouseDown());
    }
    
    @Test
    void builder_shouldSetVerificationOptions() {
        ClickOptions options = new ClickOptions.Builder()
            .setVerification(VerificationOptions.builder()
                .event(VerificationOptions.Event.TEXT_APPEARS)
                .text("Success"))
            .build();
        
        assertEquals(VerificationOptions.Event.TEXT_APPEARS, 
            options.getVerificationOptions().getEvent());
        assertEquals("Success", options.getVerificationOptions().getText());
    }
    
    @Test
    void builder_shouldSetRepetitionOptions() {
        ClickOptions options = new ClickOptions.Builder()
            .setRepetition(RepetitionOptions.builder()
                .timesToRepeatIndividualAction(5)
                .pauseBetweenIndividualActions(1.0))
            .build();
        
        assertEquals(5, options.getRepetitionOptions().getTimesToRepeatIndividualAction());
        assertEquals(1.0, options.getRepetitionOptions().getPauseBetweenIndividualActions());
        
        // Test convenience getters
        assertEquals(5, options.getTimesToRepeatIndividualAction());
        assertEquals(1.0, options.getPauseBetweenIndividualActions());
    }
    
    @Test
    void builder_shouldHandleClickTypes() {
        // Test single left click (default)
        ClickOptions leftClick = new ClickOptions.Builder()
            .setNumberOfClicks(1)
            .setPressOptions(MousePressOptions.builder()
                .button(MouseButton.LEFT)
                .build())
            .build();
        
        assertEquals(1, leftClick.getNumberOfClicks());
        assertEquals(MouseButton.LEFT, leftClick.getMousePressOptions().getButton());
        
        // Test double right click
        ClickOptions doubleRightClick = new ClickOptions.Builder()
            .setNumberOfClicks(2)
            .setPressOptions(MousePressOptions.builder()
                .button(MouseButton.RIGHT)
                .build())
            .build();
        
        assertEquals(2, doubleRightClick.getNumberOfClicks());
        assertEquals(MouseButton.RIGHT, doubleRightClick.getMousePressOptions().getButton());
        
        // Test single middle click
        ClickOptions middleClick = new ClickOptions.Builder()
            .setNumberOfClicks(1)
            .setPressOptions(MousePressOptions.builder()
                .button(MouseButton.MIDDLE)
                .build())
            .build();
        
        assertEquals(1, middleClick.getNumberOfClicks());
        assertEquals(MouseButton.MIDDLE, middleClick.getMousePressOptions().getButton());
    }
    
    @Test
    void builder_shouldCreateFromExistingOptions() {
        ClickOptions original = new ClickOptions.Builder()
            .setNumberOfClicks(2)
            .setPressOptions(MousePressOptions.builder()
                .button(MouseButton.RIGHT)
                .build())
            .setRepetition(RepetitionOptions.builder()
                .timesToRepeatIndividualAction(3))
            .setPauseBeforeBegin(1.0)
            .setPauseAfterEnd(2.0)
            .build();
            
        ClickOptions copy = new ClickOptions.Builder(original).build();
        
        assertEquals(original.getNumberOfClicks(), copy.getNumberOfClicks());
        assertEquals(original.getMousePressOptions().getButton(), 
            copy.getMousePressOptions().getButton());
        assertEquals(original.getTimesToRepeatIndividualAction(), 
            copy.getTimesToRepeatIndividualAction());
        assertEquals(original.getPauseBeforeBegin(), copy.getPauseBeforeBegin());
        assertEquals(original.getPauseAfterEnd(), copy.getPauseAfterEnd());
    }
    
    @Test
    void builder_shouldSupportFluentChaining() {
        ClickOptions options = new ClickOptions.Builder()
            .setNumberOfClicks(2)
            .setPauseBeforeBegin(0.5)
            .setPauseAfterEnd(1.0)
            .setIllustrate(ClickOptions.Illustrate.YES)
            .build();
        
        assertEquals(2, options.getNumberOfClicks());
        assertEquals(0.5, options.getPauseBeforeBegin());
        assertEquals(1.0, options.getPauseAfterEnd());
        assertEquals(ClickOptions.Illustrate.YES, options.getIllustrate());
    }
}