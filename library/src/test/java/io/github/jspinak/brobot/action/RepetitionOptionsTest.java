package io.github.jspinak.brobot.action;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RepetitionOptions configuration class.
 */
class RepetitionOptionsTest {
    
    @Test
    void builder_shouldSetDefaultValues() {
        RepetitionOptions options = new RepetitionOptions.Builder().build();
        
        assertEquals(1, options.getTimesToRepeatIndividualAction());
        assertEquals(1, options.getMaxTimesToRepeatActionSequence());
        assertEquals(0.0, options.getPauseBetweenIndividualActions());
        assertEquals(0.0, options.getPauseBetweenActionSequences());
    }
    
    @Test
    void builder_shouldSetTimesToRepeatIndividualAction() {
        RepetitionOptions options = new RepetitionOptions.Builder()
            .setTimesToRepeatIndividualAction(5)
            .build();
        
        assertEquals(5, options.getTimesToRepeatIndividualAction());
    }
    
    @Test
    void builder_shouldSetMaxTimesToRepeatActionSequence() {
        RepetitionOptions options = new RepetitionOptions.Builder()
            .setMaxTimesToRepeatActionSequence(3)
            .build();
        
        assertEquals(3, options.getMaxTimesToRepeatActionSequence());
    }
    
    @Test
    void builder_shouldSetPauseBetweenIndividualActions() {
        RepetitionOptions options = new RepetitionOptions.Builder()
            .setPauseBetweenIndividualActions(1.5)
            .build();
        
        assertEquals(1.5, options.getPauseBetweenIndividualActions());
    }
    
    @Test
    void builder_shouldSetPauseBetweenActionSequences() {
        RepetitionOptions options = new RepetitionOptions.Builder()
            .setPauseBetweenActionSequences(2.0)
            .build();
        
        assertEquals(2.0, options.getPauseBetweenActionSequences());
    }
    
    @Test
    void builder_shouldSetAllValues() {
        RepetitionOptions options = new RepetitionOptions.Builder()
            .setTimesToRepeatIndividualAction(3)
            .setMaxTimesToRepeatActionSequence(5)
            .setPauseBetweenIndividualActions(0.5)
            .setPauseBetweenActionSequences(1.0)
            .build();
        
        assertEquals(3, options.getTimesToRepeatIndividualAction());
        assertEquals(5, options.getMaxTimesToRepeatActionSequence());
        assertEquals(0.5, options.getPauseBetweenIndividualActions());
        assertEquals(1.0, options.getPauseBetweenActionSequences());
    }
    
    @Test
    void builder_shouldCreateFromExistingOptions() {
        RepetitionOptions original = new RepetitionOptions.Builder()
            .setTimesToRepeatIndividualAction(2)
            .setMaxTimesToRepeatActionSequence(4)
            .setPauseBetweenIndividualActions(0.25)
            .setPauseBetweenActionSequences(0.75)
            .build();
            
        RepetitionOptions copy = new RepetitionOptions.Builder(original).build();
        
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
        RepetitionOptions options = new RepetitionOptions.Builder(null).build();
        
        // Should have default values
        assertEquals(1, options.getTimesToRepeatIndividualAction());
        assertEquals(1, options.getMaxTimesToRepeatActionSequence());
        assertEquals(0.0, options.getPauseBetweenIndividualActions());
        assertEquals(0.0, options.getPauseBetweenActionSequences());
    }
}