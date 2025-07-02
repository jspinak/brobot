package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.model.element.SearchRegions;
import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BaseFindOptions configuration class.
 */
class BaseFindOptionsTest {
    
    /**
     * Test implementation of BaseFindOptions for testing purposes.
     */
    static class TestFindOptions extends BaseFindOptions {
        private final FindStrategy strategy;
        
        private TestFindOptions(Builder builder) {
            super(builder);
            this.strategy = builder.strategy;
        }
        
        @Override
        public FindStrategy getFindStrategy() {
            return strategy;
        }
        
        static class Builder extends BaseFindOptions.Builder<Builder> {
            private FindStrategy strategy = FindStrategy.FIRST;
            
            public Builder setStrategy(FindStrategy strategy) {
                this.strategy = strategy;
                return this;
            }
            
            @Override
            protected Builder self() {
                return this;
            }
            
            public TestFindOptions build() {
                return new TestFindOptions(this);
            }
        }
    }
    
    @Test
    void builder_shouldSetDefaultValues() {
        TestFindOptions options = new TestFindOptions.Builder().build();
        
        assertEquals(Settings.MinSimilarity, options.getSimilarity());
        assertNotNull(options.getSearchRegions());
        assertFalse(options.isCaptureImage());
        assertFalse(options.isUseDefinedRegion());
        assertEquals(-1, options.getMaxMatchesToActOn());
        assertNotNull(options.getMatchAdjustmentOptions());
    }
    
    @Test
    void builder_shouldSetSimilarity() {
        double similarity = 0.85;
        TestFindOptions options = new TestFindOptions.Builder()
            .setSimilarity(similarity)
            .build();
        
        assertEquals(similarity, options.getSimilarity());
    }
    
    @Test
    void builder_shouldSetSearchRegions() {
        SearchRegions searchRegions = new SearchRegions();
        TestFindOptions options = new TestFindOptions.Builder()
            .setSearchRegions(searchRegions)
            .build();
        
        assertEquals(searchRegions, options.getSearchRegions());
    }
    
    @Test
    void builder_shouldSetCaptureImage() {
        TestFindOptions options = new TestFindOptions.Builder()
            .setCaptureImage(true)
            .build();
        
        assertTrue(options.isCaptureImage());
    }
    
    @Test
    void builder_shouldSetUseDefinedRegion() {
        TestFindOptions options = new TestFindOptions.Builder()
            .setUseDefinedRegion(true)
            .build();
        
        assertTrue(options.isUseDefinedRegion());
    }
    
    @Test
    void builder_shouldSetMaxMatchesToActOn() {
        int maxMatches = 5;
        TestFindOptions options = new TestFindOptions.Builder()
            .setMaxMatchesToActOn(maxMatches)
            .build();
        
        assertEquals(maxMatches, options.getMaxMatchesToActOn());
    }
    
    @Test
    void builder_shouldSetMatchAdjustmentOptions() {
        TestFindOptions options = new TestFindOptions.Builder()
            .setMatchAdjustment(new MatchAdjustmentOptions.Builder()
                .setAddX(10)
                .setAddY(20)
                .setAddW(30)
                .setAddH(40))
            .build();
        
        MatchAdjustmentOptions adjustment = options.getMatchAdjustmentOptions();
        assertEquals(10, adjustment.getAddX());
        assertEquals(20, adjustment.getAddY());
        assertEquals(30, adjustment.getAddW());
        assertEquals(40, adjustment.getAddH());
    }
    
    @Test
    void builder_shouldInheritActionConfigSettings() {
        TestFindOptions options = new TestFindOptions.Builder()
            .setPauseBeforeBegin(1.0)
            .setPauseAfterEnd(2.0)
            .setIllustrate(BaseFindOptions.Illustrate.YES)
            .build();
        
        assertEquals(1.0, options.getPauseBeforeBegin());
        assertEquals(2.0, options.getPauseAfterEnd());
        assertEquals(BaseFindOptions.Illustrate.YES, options.getIllustrate());
    }
    
    @Test
    void getFindStrategy_shouldReturnCorrectStrategy() {
        TestFindOptions options = new TestFindOptions.Builder()
            .setStrategy(FindStrategy.BEST)
            .build();
        
        assertEquals(FindStrategy.BEST, options.getFindStrategy());
    }
}