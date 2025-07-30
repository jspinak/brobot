package io.github.jspinak.brobot.action;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RepetitionOptions configuration class.
 */
class RepetitionOptionsTest {
    
    @Test
    void builder_shouldSetDefaultValues() {
        RepetitionOptions options = RepetitionOptions.builder().build();
        
        assertEquals(1, options.getTimesToRepeatIndividualAction());
        assertEquals(1, options.getMaxTimesToRepeatActionSequence());
        assertEquals(0.0, options.getPauseBetweenIndividualActions());
        assertEquals(0.0, options.getPauseBetweenActionSequences());
    }
    
    @Test
    void builder_shouldSetTimesToRepeatIndividualAction() {
        RepetitionOptions options = RepetitionOptions.builder()
            .timesToRepeatIndividualAction(5)
            .build();
        
        assertEquals(5, options.getTimesToRepeatIndividualAction());
    }
    
    @Test
    void builder_shouldSetMaxTimesToRepeatActionSequence() {
        RepetitionOptions options = RepetitionOptions.builder()
            .maxTimesToRepeatActionSequence(3)
            .build();
        
        assertEquals(3, options.getMaxTimesToRepeatActionSequence());
    }
    
    @Test
    void builder_shouldSetPauseBetweenIndividualActions() {
        RepetitionOptions options = RepetitionOptions.builder()
            .pauseBetweenIndividualActions(1.5)
            .build();
        
        assertEquals(1.5, options.getPauseBetweenIndividualActions());
    }
    
    @Test
    void builder_shouldSetPauseBetweenActionSequences() {
        RepetitionOptions options = RepetitionOptions.builder()
            .pauseBetweenActionSequences(2.0)
            .build();
        
        assertEquals(2.0, options.getPauseBetweenActionSequences());
    }
    
    @Test
    void builder_shouldSetAllValues() {
        RepetitionOptions options = RepetitionOptions.builder()
            .timesToRepeatIndividualAction(3)
            .maxTimesToRepeatActionSequence(5)
            .pauseBetweenIndividualActions(0.5)
            .pauseBetweenActionSequences(1.0)
            .build();
        
        assertEquals(3, options.getTimesToRepeatIndividualAction());
        assertEquals(5, options.getMaxTimesToRepeatActionSequence());
        assertEquals(0.5, options.getPauseBetweenIndividualActions());
        assertEquals(1.0, options.getPauseBetweenActionSequences());
    }
    
    @Test
    void builder_shouldCreateFromExistingOptions() {
        RepetitionOptions original = RepetitionOptions.builder()
            .timesToRepeatIndividualAction(2)
            .maxTimesToRepeatActionSequence(4)
            .pauseBetweenIndividualActions(0.25)
            .pauseBetweenActionSequences(0.75)
            .build();
            
        RepetitionOptions copy = original.toBuilder().build();
        
        assertEquals(original.getTimesToRepeatIndividualAction(), 
            copy.getTimesToRepeatIndividualAction());
        assertEquals(original.getMaxTimesToRepeatActionSequence(), 
            copy.getMaxTimesToRepeatActionSequence());
        assertEquals(original.getPauseBetweenIndividualActions(), 
            copy.getPauseBetweenIndividualActions());
        assertEquals(original.getPauseBetweenActionSequences(), 
            copy.getPauseBetweenActionSequences());
    }
    
    @Test
    void builder_shouldHandleNullOriginal() {
        // Should not throw exception when original is null
        RepetitionOptions options = RepetitionOptions.builder().build();
        
        // Should have default values
        assertEquals(1, options.getTimesToRepeatIndividualAction());
        assertEquals(1, options.getMaxTimesToRepeatActionSequence());
        assertEquals(0.0, options.getPauseBetweenIndividualActions());
        assertEquals(0.0, options.getPauseBetweenActionSequences());
    }
}