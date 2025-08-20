package io.github.jspinak.brobot.action.composite;

import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import io.github.jspinak.brobot.model.action.MouseButton;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for FindAndClick composite action.
 * Tests configuration combinations for find and click operations.
 */
@DisplayName("FindAndClick Tests")
public class FindAndClickTest extends BrobotTestBase {
    
    private PatternFindOptions defaultFindOptions;
    private ClickOptions defaultClickOptions;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        defaultFindOptions = new PatternFindOptions.Builder().build();
        defaultClickOptions = new ClickOptions.Builder().build();
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Default constructor creates valid configuration")
        public void testDefaultConstructor() {
            FindAndClick findAndClick = new FindAndClick();
            
            assertNotNull(findAndClick);
            assertNotNull(findAndClick.getFindOptions());
            assertNotNull(findAndClick.getClickOptions());
        }
        
        @Test
        @DisplayName("Constructor with find options only")
        public void testConstructorWithFindOptions() {
            PatternFindOptions customFind = new PatternFindOptions.Builder()
                .setSimilarity(0.95)
                .setSearchDuration(10.0)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(customFind);
            
            assertEquals(customFind, findAndClick.getFindOptions());
            assertNotNull(findAndClick.getClickOptions());
            assertEquals(0.95, findAndClick.getFindOptions().getSimilarity());
        }
        
        @Test
        @DisplayName("Constructor with both find and click options")
        public void testConstructorWithBothOptions() {
            PatternFindOptions customFind = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.9)
                .build();
            
            ClickOptions customClick = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(customFind, customClick);
            
            assertEquals(customFind, findAndClick.getFindOptions());
            assertEquals(customClick, findAndClick.getClickOptions());
            assertEquals(PatternFindOptions.Strategy.BEST, findAndClick.getFindOptions().getStrategy());
            assertEquals(2, findAndClick.getClickOptions().getNumberOfClicks());
        }
    }
    
    @Nested
    @DisplayName("Find Options Configuration")
    class FindOptionsConfiguration {
        
        @Test
        @DisplayName("Configure find type")
        public void testConfigureFindType() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(findOptions);
            
            assertEquals(PatternFindOptions.Strategy.ALL, 
                findAndClick.getFindOptions().getStrategy());
        }
        
        @Test
        @DisplayName("Configure similarity threshold")
        public void testConfigureSimilarity() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSimilarity(0.85)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(findOptions);
            
            assertEquals(0.85, findAndClick.getFindOptions().getSimilarity());
        }
        
        @Test
        @DisplayName("Configure timeout")
        public void testConfigureTimeout() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSearchDuration(30.0)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(findOptions);
            
            assertEquals(30.0, findAndClick.getFindOptions().getSearchDuration());
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0.5, 0.7, 0.8, 0.9, 0.95, 0.99, 1.0})
        @DisplayName("Various similarity values")
        public void testVariousSimilarities(double similarity) {
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSimilarity(similarity)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(findOptions);
            
            assertEquals(similarity, findAndClick.getFindOptions().getSimilarity());
        }
    }
    
    @Nested
    @DisplayName("Click Options Configuration")
    class ClickOptionsConfiguration {
        
        @Test
        @DisplayName("Configure single click")
        public void testSingleClick() {
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(defaultFindOptions, clickOptions);
            
            assertEquals(1, findAndClick.getClickOptions().getNumberOfClicks());
        }
        
        @Test
        @DisplayName("Configure double click")
        public void testDoubleClick() {
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(defaultFindOptions, clickOptions);
            
            assertEquals(2, findAndClick.getClickOptions().getNumberOfClicks());
        }
        
        @Test
        @DisplayName("Configure right click")
        public void testRightClick() {
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setPressOptions(MousePressOptions.builder()
                    .setButton(MouseButton.RIGHT)
                    .build())
                .build();
            
            FindAndClick findAndClick = new FindAndClick(defaultFindOptions, clickOptions);
            
            assertEquals(MouseButton.RIGHT, 
                findAndClick.getClickOptions().getMousePressOptions().getButton());
        }
        
        @Test
        @DisplayName("Configure middle click")
        public void testMiddleClick() {
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setPressOptions(MousePressOptions.builder()
                    .setButton(MouseButton.MIDDLE)
                    .build())
                .build();
            
            FindAndClick findAndClick = new FindAndClick(defaultFindOptions, clickOptions);
            
            assertEquals(MouseButton.MIDDLE, 
                findAndClick.getClickOptions().getMousePressOptions().getButton());
        }
    }
    
    @Nested
    @DisplayName("Combined Configurations")
    class CombinedConfigurations {
        
        @Test
        @DisplayName("Find best match and double-click")
        public void testFindBestAndDoubleClick() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.95)
                .build();
            
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(findOptions, clickOptions);
            
            assertEquals(PatternFindOptions.Strategy.BEST, 
                findAndClick.getFindOptions().getStrategy());
            assertEquals(2, findAndClick.getClickOptions().getNumberOfClicks());
        }
        
        @Test
        @DisplayName("Find all matches and click each")
        public void testFindAllAndClickEach() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSearchDuration(5.0)
                .build();
            
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(findOptions, clickOptions);
            
            assertEquals(PatternFindOptions.Strategy.ALL, 
                findAndClick.getFindOptions().getStrategy());
            assertEquals(5.0, findAndClick.getFindOptions().getSearchDuration());
        }
        
        @Test
        @DisplayName("Quick find with immediate click")
        public void testQuickFindAndClick() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.7) // Lower similarity for speed
                .setSearchDuration(0.5)
                .build();
            
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(findOptions, clickOptions);
            
            assertEquals(PatternFindOptions.Strategy.FIRST, 
                findAndClick.getFindOptions().getStrategy());
            assertEquals(0.7, findAndClick.getFindOptions().getSimilarity());
            assertEquals(0.5, findAndClick.getFindOptions().getSearchDuration());
        }
        
        @Test
        @DisplayName("Precise find with right-click context menu")
        public void testPreciseFindAndRightClick() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.99)
                .setSearchDuration(10.0)
                .build();
            
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setPressOptions(MousePressOptions.builder()
                    .setButton(MouseButton.RIGHT)
                    .setPauseAfterMouseUp(0.5)
                    .build())
                .build();
            
            FindAndClick findAndClick = new FindAndClick(findOptions, clickOptions);
            
            assertEquals(0.99, findAndClick.getFindOptions().getSimilarity());
            assertEquals(MouseButton.RIGHT, 
                findAndClick.getClickOptions().getMousePressOptions().getButton());
            assertEquals(0.5, 
                findAndClick.getClickOptions().getMousePressOptions().getPauseAfterMouseUp());
        }
    }
    
    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {
        
        @Test
        @DisplayName("Builder creates default configuration")
        public void testBuilderDefaults() {
            FindAndClick.Builder builder = new FindAndClick.Builder();
            FindAndClick findAndClick = builder.build();
            
            assertNotNull(findAndClick);
            assertNotNull(findAndClick.getFindOptions());
            assertNotNull(findAndClick.getClickOptions());
        }
        
        @Test
        @DisplayName("Builder with custom find options")
        public void testBuilderWithFindOptions() {
            PatternFindOptions customFind = new PatternFindOptions.Builder()
                .setSimilarity(0.9)
                .build();
            
            FindAndClick findAndClick = new FindAndClick.Builder()
                .withFindOptions(customFind)
                .build();
            
            assertEquals(0.9, findAndClick.getFindOptions().getSimilarity());
        }
        
        @Test
        @DisplayName("Builder with custom click options")
        public void testBuilderWithClickOptions() {
            ClickOptions customClick = new ClickOptions.Builder()
                .setNumberOfClicks(3)
                .build();
            
            FindAndClick findAndClick = new FindAndClick.Builder()
                .withClickOptions(customClick)
                .build();
            
            assertEquals(3, findAndClick.getClickOptions().getNumberOfClicks());
        }
        
        @Test
        @DisplayName("Builder with both custom options")
        public void testBuilderWithBothOptions() {
            PatternFindOptions customFind = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
            
            ClickOptions customClick = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .build();
            
            FindAndClick findAndClick = new FindAndClick.Builder()
                .withFindOptions(customFind)
                .withClickOptions(customClick)
                .build();
            
            assertEquals(PatternFindOptions.Strategy.ALL, 
                findAndClick.getFindOptions().getStrategy());
            assertEquals(2, findAndClick.getClickOptions().getNumberOfClicks());
        }
    }
    
    @Nested
    @DisplayName("Use Case Scenarios")
    class UseCaseScenarios {
        
        @Test
        @DisplayName("Button click scenario")
        public void testButtonClickScenario() {
            // Standard button click with moderate similarity
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.8)
                .setSearchDuration(3.0)
                .build();
            
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(findOptions, clickOptions);
            
            assertEquals(PatternFindOptions.Strategy.FIRST, 
                findAndClick.getFindOptions().getStrategy());
            assertEquals(1, findAndClick.getClickOptions().getNumberOfClicks());
        }
        
        @Test
        @DisplayName("File double-click scenario")
        public void testFileDoubleClickScenario() {
            // Find specific file icon and double-click to open
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.9)
                .setSearchDuration(5.0)
                .build();
            
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(findOptions, clickOptions);
            
            assertEquals(PatternFindOptions.Strategy.BEST, 
                findAndClick.getFindOptions().getStrategy());
            assertEquals(2, findAndClick.getClickOptions().getNumberOfClicks());
        }
        
        @Test
        @DisplayName("Context menu scenario")
        public void testContextMenuScenario() {
            // Right-click for context menu
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.85)
                .build();
            
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setPressOptions(MousePressOptions.builder()
                    .setButton(MouseButton.RIGHT)
                    .setPauseAfterMouseUp(0.3)
                    .build())
                .build();
            
            FindAndClick findAndClick = new FindAndClick(findOptions, clickOptions);
            
            assertEquals(MouseButton.RIGHT, 
                findAndClick.getClickOptions().getMousePressOptions().getButton());
        }
        
        @Test
        @DisplayName("Multiple checkbox selection scenario")
        public void testMultipleCheckboxScenario() {
            // Find all checkboxes and click each one
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.85)
                .setSearchDuration(5.0)
                .build();
            
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(findOptions, clickOptions);
            
            assertEquals(PatternFindOptions.Strategy.ALL, 
                findAndClick.getFindOptions().getStrategy());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Minimum similarity with maximum clicks")
        public void testMinSimilarityMaxClicks() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSimilarity(0.0)
                .build();
            
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(Integer.MAX_VALUE)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(findOptions, clickOptions);
            
            assertEquals(0.0, findAndClick.getFindOptions().getSimilarity());
            assertEquals(Integer.MAX_VALUE, findAndClick.getClickOptions().getNumberOfClicks());
        }
        
        @Test
        @DisplayName("Maximum wait time")
        public void testMaximumWaitTime() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSearchDuration(Double.MAX_VALUE)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(findOptions);
            
            assertEquals(Double.MAX_VALUE, findAndClick.getFindOptions().getSearchDuration());
        }
        
        @Test
        @DisplayName("Zero wait time for immediate action")
        public void testZeroWaitTime() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSearchDuration(0.0)
                .build();
            
            FindAndClick findAndClick = new FindAndClick(findOptions);
            
            assertEquals(0.0, findAndClick.getFindOptions().getSearchDuration());
        }
    }
}