package io.github.jspinak.brobot.action.basic.find;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PatternFindOptions configuration class.
 */
class PatternFindOptionsTest {
    
    @Test
    void builder_shouldSetDefaultValues() {
        PatternFindOptions options = new PatternFindOptions.Builder().build();
        
        assertEquals(PatternFindOptions.Strategy.FIRST, options.getStrategy());
        assertEquals(FindStrategy.FIRST, options.getFindStrategy());
    }
    
    @Test
    void builder_shouldSetStrategy() {
        PatternFindOptions options = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
        
        assertEquals(PatternFindOptions.Strategy.BEST, options.getStrategy());
        assertEquals(FindStrategy.BEST, options.getFindStrategy());
    }
    
    @Test
    void getFindStrategy_shouldMapCorrectly() {
        // Test FIRST
        PatternFindOptions firstOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build();
        assertEquals(FindStrategy.FIRST, firstOptions.getFindStrategy());
        
        // Test ALL
        PatternFindOptions allOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.ALL)
            .build();
        assertEquals(FindStrategy.ALL, allOptions.getFindStrategy());
        
        // Test BEST
        PatternFindOptions bestOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
        assertEquals(FindStrategy.BEST, bestOptions.getFindStrategy());
        
        // Test EACH
        PatternFindOptions eachOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.EACH)
            .build();
        assertEquals(FindStrategy.EACH, eachOptions.getFindStrategy());
    }
    
    @Test
    void builder_shouldInheritBaseFindOptionsSettings() {
        PatternFindOptions options = new PatternFindOptions.Builder()
            .setSimilarity(0.9)
            .setMaxMatchesToActOn(3)
            .setCaptureImage(true)
            .build();
        
        assertEquals(0.9, options.getSimilarity());
        assertEquals(3, options.getMaxMatchesToActOn());
        assertTrue(options.isCaptureImage());
    }
    
    @Test
    void builder_shouldCreateFromExistingOptions() {
        PatternFindOptions original = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .setSimilarity(0.85)
            .setMaxMatchesToActOn(5)
            .setPauseBeforeBegin(1.0)
            .build();
            
        PatternFindOptions copy = new PatternFindOptions.Builder(original).build();
        
        assertEquals(original.getStrategy(), copy.getStrategy());
        assertEquals(original.getSimilarity(), copy.getSimilarity());
        assertEquals(original.getMaxMatchesToActOn(), copy.getMaxMatchesToActOn());
        assertEquals(original.getPauseBeforeBegin(), copy.getPauseBeforeBegin());
    }
    
    @Test
    void strategyEnum_shouldHaveCorrectValues() {
        PatternFindOptions.Strategy[] values = PatternFindOptions.Strategy.values();
        assertEquals(4, values.length);
        
        assertNotNull(PatternFindOptions.Strategy.valueOf("FIRST"));
        assertNotNull(PatternFindOptions.Strategy.valueOf("ALL"));
        assertNotNull(PatternFindOptions.Strategy.valueOf("BEST"));
        assertNotNull(PatternFindOptions.Strategy.valueOf("EACH"));
    }
}