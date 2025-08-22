package io.github.jspinak.brobot.action.basic.find;

/**
 * Verification program for FindStrategy enum
 */
public class VerifyFindStrategy {
    
    public static void main(String[] args) {
        System.out.println("Testing FindStrategy enum...\n");
        
        boolean allTestsPassed = true;
        
        // Test 1: Basic pattern strategies
        System.out.println("Test 1: Basic pattern strategies");
        try {
            FindStrategy first = FindStrategy.FIRST;
            FindStrategy each = FindStrategy.EACH;
            FindStrategy all = FindStrategy.ALL;
            FindStrategy best = FindStrategy.BEST;
            
            assert first != null && first.name().equals("FIRST");
            assert each != null && each.name().equals("EACH");
            assert all != null && all.name().equals("ALL");
            assert best != null && best.name().equals("BEST");
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 2: Special strategies
        System.out.println("Test 2: Special strategies");
        try {
            FindStrategy universal = FindStrategy.UNIVERSAL;
            FindStrategy custom = FindStrategy.CUSTOM;
            
            assert universal != null && universal.name().equals("UNIVERSAL");
            assert custom != null && custom.name().equals("CUSTOM");
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 3: Analysis strategies
        System.out.println("Test 3: Analysis strategies");
        try {
            FindStrategy color = FindStrategy.COLOR;
            FindStrategy histogram = FindStrategy.HISTOGRAM;
            FindStrategy similarImages = FindStrategy.SIMILAR_IMAGES;
            FindStrategy states = FindStrategy.STATES;
            
            assert color != null && color.name().equals("COLOR");
            assert histogram != null && histogram.name().equals("HISTOGRAM");
            assert similarImages != null && similarImages.name().equals("SIMILAR_IMAGES");
            assert states != null && states.name().equals("STATES");
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 4: Motion strategies
        System.out.println("Test 4: Motion strategies");
        try {
            FindStrategy motion = FindStrategy.MOTION;
            FindStrategy regionsOfMotion = FindStrategy.REGIONS_OF_MOTION;
            FindStrategy fixedPixels = FindStrategy.FIXED_PIXELS;
            FindStrategy dynamicPixels = FindStrategy.DYNAMIC_PIXELS;
            
            assert motion != null && motion.name().equals("MOTION");
            assert regionsOfMotion != null && regionsOfMotion.name().equals("REGIONS_OF_MOTION");
            assert fixedPixels != null && fixedPixels.name().equals("FIXED_PIXELS");
            assert dynamicPixels != null && dynamicPixels.name().equals("DYNAMIC_PIXELS");
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 5: Text strategy
        System.out.println("Test 5: Text strategy");
        try {
            FindStrategy allWords = FindStrategy.ALL_WORDS;
            assert allWords != null && allWords.name().equals("ALL_WORDS");
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 6: Enum operations
        System.out.println("Test 6: Enum operations");
        try {
            // Test valueOf
            FindStrategy fromString = FindStrategy.valueOf("FIRST");
            assert fromString == FindStrategy.FIRST;
            
            // Test values()
            FindStrategy[] allStrategies = FindStrategy.values();
            assert allStrategies != null && allStrategies.length > 0;
            
            // Count strategies
            int count = 0;
            for (FindStrategy s : allStrategies) {
                count++;
            }
            assert count == 15 : "Should have 15 strategies, found " + count;
            
            // Test ordinals are unique
            assert FindStrategy.FIRST.ordinal() != FindStrategy.BEST.ordinal();
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 7: Compatibility with PatternFindOptions
        System.out.println("Test 7: Compatibility with PatternFindOptions");
        try {
            PatternFindOptions firstOpt = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
            assert firstOpt.getFindStrategy() == FindStrategy.FIRST;
            
            PatternFindOptions bestOpt = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build();
            assert bestOpt.getFindStrategy() == FindStrategy.BEST;
            
            PatternFindOptions allOpt = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
            assert allOpt.getFindStrategy() == FindStrategy.ALL;
            
            PatternFindOptions eachOpt = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.EACH)
                .build();
            assert eachOpt.getFindStrategy() == FindStrategy.EACH;
            
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