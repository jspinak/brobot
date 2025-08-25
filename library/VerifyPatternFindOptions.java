package io.github.jspinak.brobot.action.basic.find;

/**
 * Simple verification program for PatternFindOptions functionality
 */
public class VerifyPatternFindOptions {
    
    public static void main(String[] args) {
        System.out.println("Testing PatternFindOptions...\n");
        
        boolean allTestsPassed = true;
        
        // Test 1: Default builder
        System.out.println("Test 1: Default builder");
        try {
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            assert options != null : "Options should not be null";
            assert options.getStrategy() == PatternFindOptions.Strategy.FIRST : "Default strategy should be FIRST";
            assert options.getDoOnEach() == PatternFindOptions.DoOnEach.FIRST : "Default DoOnEach should be FIRST";
            assert options.getMatchFusionOptions() != null : "MatchFusionOptions should not be null";
            assert options.getFindStrategy() == FindStrategy.FIRST : "Should map to FindStrategy.FIRST";
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 2: Factory methods
        System.out.println("Test 2: Factory methods");
        try {
            PatternFindOptions quick = PatternFindOptions.forQuickSearch();
            PatternFindOptions precise = PatternFindOptions.forPreciseSearch();
            PatternFindOptions all = PatternFindOptions.forAllMatches();
            
            assert quick != null && quick.getStrategy() == PatternFindOptions.Strategy.FIRST : "Quick search should use FIRST";
            assert quick.getSimilarity() == 0.7 : "Quick search should use 0.7 similarity";
            assert !quick.isCaptureImage() : "Quick search should not capture image";
            
            assert precise != null && precise.getStrategy() == PatternFindOptions.Strategy.BEST : "Precise search should use BEST";
            assert precise.getSimilarity() == 0.9 : "Precise search should use 0.9 similarity";
            assert precise.isCaptureImage() : "Precise search should capture image";
            
            assert all != null && all.getStrategy() == PatternFindOptions.Strategy.ALL : "All matches should use ALL";
            assert all.getSimilarity() == 0.8 : "All matches should use 0.8 similarity";
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 3: Strategy mapping
        System.out.println("Test 3: Strategy mapping to FindStrategy");
        try {
            PatternFindOptions first = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST).build();
            PatternFindOptions best = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST).build();
            PatternFindOptions all = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL).build();
            PatternFindOptions each = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.EACH).build();
            
            assert first.getFindStrategy() == FindStrategy.FIRST : "FIRST should map to FindStrategy.FIRST";
            assert best.getFindStrategy() == FindStrategy.BEST : "BEST should map to FindStrategy.BEST";
            assert all.getFindStrategy() == FindStrategy.ALL : "ALL should map to FindStrategy.ALL";
            assert each.getFindStrategy() == FindStrategy.EACH : "EACH should map to FindStrategy.EACH";
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 4: Builder chaining
        System.out.println("Test 4: Builder chaining");
        try {
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setDoOnEach(PatternFindOptions.DoOnEach.BEST)
                .setSimilarity(0.9)
                .setSearchDuration(10.0)
                .setCaptureImage(true)
                .setMaxMatchesToActOn(5)
                .build();
            
            assert options.getStrategy() == PatternFindOptions.Strategy.ALL : "Strategy should be ALL";
            assert options.getDoOnEach() == PatternFindOptions.DoOnEach.BEST : "DoOnEach should be BEST";
            assert Math.abs(options.getSimilarity() - 0.9) < 0.01 : "Similarity should be 0.9";
            assert Math.abs(options.getSearchDuration() - 10.0) < 0.01 : "Search duration should be 10.0";
            assert options.isCaptureImage() : "Should capture image";
            assert options.getMaxMatchesToActOn() == 5 : "Max matches should be 5";
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 5: Copy constructor
        System.out.println("Test 5: Copy constructor");
        try {
            PatternFindOptions original = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.85)
                .setSearchDuration(5.0)
                .build();
            
            PatternFindOptions copy = new PatternFindOptions.Builder(original).build();
            
            assert copy.getStrategy() == original.getStrategy() : "Copy should have same strategy";
            assert Math.abs(copy.getSimilarity() - original.getSimilarity()) < 0.01 : "Copy should have same similarity";
            assert Math.abs(copy.getSearchDuration() - original.getSearchDuration()) < 0.01 : "Copy should have same search duration";
            
            // Modify copy and verify original is unchanged
            PatternFindOptions modified = new PatternFindOptions.Builder(original)
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
            
            assert original.getStrategy() == PatternFindOptions.Strategy.BEST : "Original should be unchanged";
            assert modified.getStrategy() == PatternFindOptions.Strategy.ALL : "Modified should have new strategy";
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 6: Match fusion options
        System.out.println("Test 6: Match fusion options");
        try {
            MatchFusionOptions fusion = MatchFusionOptions.builder()
                .setFusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
                .setMaxFusionDistanceX(20)
                .setMaxFusionDistanceY(20)
                .build();
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setMatchFusion(fusion)
                .build();
            
            assert options.getMatchFusionOptions() != null : "Match fusion should not be null";
            assert options.getMatchFusionOptions().getFusionMethod() == MatchFusionOptions.FusionMethod.ABSOLUTE : "Should use ABSOLUTE fusion";
            assert options.getMatchFusionOptions().getMaxFusionDistanceX() == 20 : "Fusion distance X should be 20";
            assert options.getMatchFusionOptions().getMaxFusionDistanceY() == 20 : "Fusion distance Y should be 20";
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Summary
        System.out.println("========================================");
        if (allTestsPassed) {
            System.out.println("✓ ALL TESTS PASSED!");
        } else {
            System.out.println("✗ SOME TESTS FAILED!");
        }
        System.out.println("========================================");
        
        System.exit(allTestsPassed ? 0 : 1);
    }
}