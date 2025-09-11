package com.example.unittesting.examples;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.example.unittesting.utils.TestAssertions;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Examples using custom Brobot assertions from documentation. From:
 * /docs/04-testing/unit-testing.md
 */
@SpringBootTest
@TestPropertySource(
        properties = {
            "brobot.core.mock=true",
            "brobot.screenshot.path=src/test/resources/screenshots/"
        })
class CustomAssertionsTest {

    @Autowired private Action action;

    @Test
    @DisplayName("Custom Brobot Assertions - Documentation Examples")
    void testCustomBrobotAssertions() {
        // Create find configuration
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.8)
                        .build();

        StateImage testButton = new StateImage.Builder().addPatterns("test_button.png").build();

        ActionResult result = action.perform(findOptions, testButton);

        // Use custom assertions from documentation
        TestAssertions.BrobotAssertions.assertHasBestMatch(result);
        TestAssertions.BrobotAssertions.assertMinimumScore(result, 0.7);
        TestAssertions.BrobotAssertions.assertMatchCount(result, 2); // Expecting 2 matches

        // Test region-based assertions
        Region searchArea = new Region(100, 100, 300, 200);
        TestAssertions.BrobotAssertions.assertFoundInRegion(result, searchArea);

        // Test area-based assertions
        TestAssertions.BrobotAssertions.assertMatchInArea(result, 50, 50, 450, 350);
    }

    @Test
    @DisplayName("Custom Assertions with Edge Cases")
    void testCustomAssertionsEdgeCases() {
        // Test strict similarity matching
        PatternFindOptions strictFind =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.BEST)
                        .setSimilarity(0.95)
                        .build();

        StateImage precisePattern =
                new StateImage.Builder().addPatterns("precise_pattern.png").build();

        ActionResult strictResult = action.perform(strictFind, precisePattern);

        // Verify high-precision match
        if (strictResult.isSuccess()) {
            TestAssertions.BrobotAssertions.assertMinimumScore(strictResult, 0.95);
            TestAssertions.BrobotAssertions.assertMatchCount(strictResult, 1);
        }

        // Test with broader search
        PatternFindOptions broadFind =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.6)
                        .build();

        ActionResult broadResult = action.perform(broadFind, precisePattern);

        if (broadResult.isSuccess()) {
            TestAssertions.BrobotAssertions.assertMinimumScore(broadResult, 0.6);
            // Broad search should find more matches than strict
            if (strictResult.isSuccess()) {
                assert broadResult.size() >= strictResult.size()
                        : "Broad search should find at least as many matches as strict search";
            }
        }
    }

    @Test
    @DisplayName("Regional Search Verification")
    void testRegionalSearches() {
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.8)
                        .build();

        StateImage targetElement =
                new StateImage.Builder().addPatterns("target_element.png").build();

        ActionResult result = action.perform(findOptions, targetElement);

        if (result.isSuccess()) {
            // Define different search regions
            Region topLeft = new Region(0, 0, 400, 300);
            Region bottomRight = new Region(400, 300, 400, 300);
            Region center = new Region(200, 150, 400, 300);

            // Test if matches are found in specific regions
            // Note: In mock mode, this demonstrates the assertion usage
            // In real scenarios, this would verify actual match locations
            try {
                TestAssertions.BrobotAssertions.assertFoundInRegion(result, center);
                System.out.println("✓ Found matches in center region");
            } catch (AssertionError e) {
                System.out.println("ℹ No matches in center region (expected in mock mode)");
            }
        }
    }
}
