package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Pattern;
// Test utility class - not a JUnit test
public class TestRegionDebug {
    public static void main(String[] args) {
        // Test the region creation and state image builder
        Region lowerLeftQuarter = Region.builder()
            .withScreenPercentage(0.0, 0.5, 0.5, 0.5)
            .build();
        
        System.out.println("Region created: " + lowerLeftQuarter);
        System.out.println("Region isDefined: " + lowerLeftQuarter.isDefined());
        
        StateImage claudePrompt = new StateImage.Builder()
            .addPatterns("test-pattern")
            .setName("TestImage")
            .setSearchRegionForAllPatterns(lowerLeftQuarter)
            .setFixedForAllPatterns(true)
            .build();
        
        System.out.println("StateImage created: " + claudePrompt.getName());
        System.out.println("Number of patterns: " + claudePrompt.getPatterns().size());
        
        for (Pattern p : claudePrompt.getPatterns()) {
            System.out.println("Pattern: " + p.getName());
            System.out.println("Is fixed: " + p.isFixed());
            System.out.println("Search regions: " + p.getSearchRegions());
            System.out.println("getRegions(fixed): " + p.getRegions());
            System.out.println("getRegionsForSearch(): " + p.getRegionsForSearch());
        }
    }
}
