package io.github.jspinak.brobot.action.basic.find;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.test.BrobotTestBase;

/** Simple test to verify PatternFindOptions works correctly */
public class SimplePatternFindOptionsTest extends BrobotTestBase {

    @Test
    public void testDefaultBuilder() {
        PatternFindOptions options = new PatternFindOptions.Builder().build();

        assertNotNull(options);
        assertEquals(PatternFindOptions.Strategy.FIRST, options.getStrategy());
        assertEquals(PatternFindOptions.DoOnEach.FIRST, options.getDoOnEach());
        assertNotNull(options.getMatchFusionOptions());
    }

    @Test
    public void testFactoryMethods() {
        PatternFindOptions quick = PatternFindOptions.forQuickSearch();
        PatternFindOptions precise = PatternFindOptions.forPreciseSearch();
        PatternFindOptions all = PatternFindOptions.forAllMatches();

        assertNotNull(quick);
        assertNotNull(precise);
        assertNotNull(all);

        assertEquals(PatternFindOptions.Strategy.FIRST, quick.getStrategy());
        assertEquals(PatternFindOptions.Strategy.BEST, precise.getStrategy());
        assertEquals(PatternFindOptions.Strategy.ALL, all.getStrategy());
    }

    @Test
    public void testStrategyMapping() {
        PatternFindOptions first =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                        .build();
        PatternFindOptions best =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.BEST)
                        .build();
        PatternFindOptions all =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .build();
        PatternFindOptions each =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.EACH)
                        .build();

        assertEquals(FindStrategy.FIRST, first.getFindStrategy());
        assertEquals(FindStrategy.BEST, best.getFindStrategy());
        assertEquals(FindStrategy.ALL, all.getFindStrategy());
        assertEquals(FindStrategy.EACH, each.getFindStrategy());
    }

    @Test
    public void testBuilderChaining() {
        PatternFindOptions options =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setDoOnEach(PatternFindOptions.DoOnEach.BEST)
                        .setSimilarity(0.9)
                        .setSearchDuration(10.0)
                        .setCaptureImage(true)
                        .setMaxMatchesToActOn(5)
                        .build();

        assertNotNull(options);
        assertEquals(PatternFindOptions.Strategy.ALL, options.getStrategy());
        assertEquals(PatternFindOptions.DoOnEach.BEST, options.getDoOnEach());
        assertEquals(0.9, options.getSimilarity(), 0.01);
        assertEquals(10.0, options.getSearchDuration(), 0.01);
        assertTrue(options.isCaptureImage());
        assertEquals(5, options.getMaxMatchesToActOn());
    }

    @Test
    public void testCopyConstructor() {
        PatternFindOptions original =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.BEST)
                        .setSimilarity(0.85)
                        .build();

        PatternFindOptions copy = new PatternFindOptions.Builder(original).build();

        assertEquals(original.getStrategy(), copy.getStrategy());
        assertEquals(original.getSimilarity(), copy.getSimilarity(), 0.01);
    }
}
