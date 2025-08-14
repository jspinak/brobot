package io.github.jspinak.brobot.tools.testing.mock.history;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Positions;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory for creating common mock ActionHistory patterns.
 * 
 * <p>This factory provides convenient methods for creating ActionHistory
 * instances for common UI testing scenarios. It includes presets for
 * screen positions, reliability patterns, and custom configurations.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * // Create history for an element in the center of the screen
 * ActionHistory centerHistory = MockActionHistoryFactory.forScreenPosition(
 *     Positions.Name.MIDDLEMIDDLE, 100, 50);
 * 
 * // Create history for a reliable button
 * ActionHistory buttonHistory = MockActionHistoryFactory.reliableButton(
 *     new Region(100, 200, 80, 30));
 * 
 * // Create history with custom configuration
 * ActionHistory customHistory = MockActionHistoryFactory.withConfig(config -> 
 *     config.successRate(0.85)
 *           .recordCount(15)
 *           .region(100, 100, 50, 50));
 * }</pre>
 */
public class MockActionHistoryFactory {
    
    private static final Map<String, Supplier<ActionHistory>> presetCache = new HashMap<>();
    
    /**
     * Creates ActionHistory for an element at a specific screen position.
     * 
     * @param position the screen position (e.g., TOPLEFT, MIDDLEMIDDLE)
     * @param width the width of the element
     * @param height the height of the element
     * @return ActionHistory configured for the specified position
     */
    public static ActionHistory forScreenPosition(Positions.Name position, int width, int height) {
        // Get screen dimensions (assuming standard 1920x1080 for mock)
        int screenWidth = 1920;
        int screenHeight = 1080;
        
        // Calculate position based on Positions.Name
        int x = 0, y = 0;
        switch (position) {
            case TOPLEFT:
                x = 0; y = 0;
                break;
            case TOPMIDDLE:
                x = screenWidth / 2 - width / 2; y = 0;
                break;
            case TOPRIGHT:
                x = screenWidth - width; y = 0;
                break;
            case MIDDLELEFT:
                x = 0; y = screenHeight / 2 - height / 2;
                break;
            case MIDDLEMIDDLE:
                x = screenWidth / 2 - width / 2; y = screenHeight / 2 - height / 2;
                break;
            case MIDDLERIGHT:
                x = screenWidth - width; y = screenHeight / 2 - height / 2;
                break;
            case BOTTOMLEFT:
                x = 0; y = screenHeight - height;
                break;
            case BOTTOMMIDDLE:
                x = screenWidth / 2 - width / 2; y = screenHeight - height;
                break;
            case BOTTOMRIGHT:
                x = screenWidth - width; y = screenHeight - height;
                break;
        }
        
        Region region = new Region(x, y, width, height);
        return MockActionHistoryBuilder.Presets.alwaysFound(region);
    }
    
    /**
     * Creates ActionHistory for a reliable UI button.
     * High success rate with quick response times.
     * 
     * @param buttonRegion the region where the button appears
     * @return ActionHistory for a reliable button
     */
    public static ActionHistory reliableButton(Region buttonRegion) {
        return MockActionHistoryBuilder.builder()
            .successRate(0.98)
            .matchRegion(buttonRegion)
            .minSimilarity(0.92)
            .maxSimilarity(0.99)
            .minDuration(20)
            .maxDuration(60)
            .recordCount(25)
            .build()
            .build();
    }
    
    /**
     * Creates ActionHistory for a dynamic text field.
     * Lower similarity due to changing content.
     * 
     * @param fieldRegion the region where the field appears
     * @return ActionHistory for a dynamic text field
     */
    public static ActionHistory dynamicTextField(Region fieldRegion) {
        return MockActionHistoryBuilder.builder()
            .successRate(0.85)
            .matchRegion(fieldRegion)
            .minSimilarity(0.70)
            .maxSimilarity(0.85)
            .minDuration(50)
            .maxDuration(150)
            .recordCount(20)
            .build()
            .build();
    }
    
    /**
     * Creates ActionHistory for a loading indicator.
     * Variable success rate as it appears/disappears.
     * 
     * @param indicatorRegion the region where the indicator appears
     * @return ActionHistory for a loading indicator
     */
    public static ActionHistory loadingIndicator(Region indicatorRegion) {
        return MockActionHistoryBuilder.builder()
            .successRate(0.60)
            .matchRegion(indicatorRegion)
            .minSimilarity(0.80)
            .maxSimilarity(0.95)
            .minDuration(100)
            .maxDuration(500)
            .recordCount(30)
            .build()
            .build();
    }
    
    /**
     * Creates ActionHistory for a menu item.
     * High reliability when visible, but may be hidden initially.
     * 
     * @param menuRegion the region where the menu item appears
     * @return ActionHistory for a menu item
     */
    public static ActionHistory menuItem(Region menuRegion) {
        return MockActionHistoryBuilder.builder()
            .successRate(0.90)
            .matchRegion(menuRegion)
            .minSimilarity(0.88)
            .maxSimilarity(0.98)
            .minDuration(30)
            .maxDuration(100)
            .recordCount(15)
            .build()
            .build();
    }
    
    /**
     * Creates ActionHistory for a modal dialog.
     * Very reliable when present, with high similarity.
     * 
     * @param dialogRegion the region where the dialog appears
     * @return ActionHistory for a modal dialog
     */
    public static ActionHistory modalDialog(Region dialogRegion) {
        return MockActionHistoryBuilder.builder()
            .successRate(1.0)
            .matchRegion(dialogRegion)
            .minSimilarity(0.95)
            .maxSimilarity(1.0)
            .minDuration(10)
            .maxDuration(40)
            .recordCount(10)
            .build()
            .build();
    }
    
    /**
     * Creates ActionHistory with a custom configuration.
     * 
     * @param configurator function to configure the builder
     * @return ActionHistory with custom configuration
     */
    public static ActionHistory withConfig(
            java.util.function.Function<MockActionHistoryBuilder.MockActionHistoryBuilderBuilder, 
                                       MockActionHistoryBuilder.MockActionHistoryBuilderBuilder> configurator) {
        MockActionHistoryBuilder.MockActionHistoryBuilderBuilder builder = 
            MockActionHistoryBuilder.builder();
        return configurator.apply(builder).build().build();
    }
    
    /**
     * Creates ActionHistory for a lower-left screen element.
     * Commonly used for status bars, notifications, or chat interfaces.
     * 
     * @param width element width
     * @param height element height
     * @return ActionHistory for lower-left element
     */
    public static ActionHistory lowerLeftElement(int width, int height) {
        // Place in lower left quadrant with some padding
        Region region = new Region(100, 590, width, height);
        return MockActionHistoryBuilder.Presets.reliable(region);
    }
    
    /**
     * Creates a cached ActionHistory that can be reused.
     * Useful for elements that appear frequently across tests.
     * 
     * @param cacheKey unique key for this configuration
     * @param historySupplier supplier to create the ActionHistory if not cached
     * @return cached or newly created ActionHistory
     */
    public static ActionHistory cached(String cacheKey, Supplier<ActionHistory> historySupplier) {
        return presetCache.computeIfAbsent(cacheKey, k -> historySupplier).get();
    }
    
    /**
     * Clears the preset cache.
     * Should be called between test suites to ensure isolation.
     */
    public static void clearCache() {
        presetCache.clear();
    }
}