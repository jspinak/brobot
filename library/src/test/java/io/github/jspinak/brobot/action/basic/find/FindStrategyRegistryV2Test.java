package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.histogram.FindHistogram;
import io.github.jspinak.brobot.action.basic.find.motion.FindDynamicPixelMatches;
import io.github.jspinak.brobot.action.basic.find.motion.FindFixedPixelMatches;
import io.github.jspinak.brobot.action.basic.find.motion.FindMotion;
import io.github.jspinak.brobot.action.basic.find.motion.FindRegionsOfMotion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FindStrategyRegistryV2 with available components.
 */
class FindStrategyRegistryV2Test {
    
    private ModernFindStrategyRegistry registry;
    
    @Mock
    private FindHistogram findHistogram;
    
    @Mock
    private FindColor findColor;
    
    @Mock
    private FindMotion findMotion;
    
    @Mock
    private FindRegionsOfMotion findRegionsOfMotion;
    
    @Mock
    private ImageFinder findImageV2;
    
    @Mock
    private FindText findText;
    
    @Mock
    private FindSimilarImages findSimilarImages;
    
    @Mock
    private FindFixedPixelMatches findFixedPixelMatches;
    
    @Mock
    private FindDynamicPixelMatches findDynamicPixelMatches;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Create registry with all 9 components
        registry = new ModernFindStrategyRegistry(
            findHistogram, findColor, findMotion, findRegionsOfMotion,
            findImageV2, findText, findSimilarImages,
            findFixedPixelMatches, findDynamicPixelMatches
        );
    }
    
    @Test
    void get_shouldReturnCorrectFunctionForPatternFindOptions() {
        // Test uses the registry created in setUp
        
        PatternFindOptions options = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
            
        BiConsumer<ActionResult, List<ObjectCollection>> function = registry.get(options);
        
        assertNotNull(function);
        
        // Verify it calls the correct method
        ActionResult result = new ActionResult();
        List<ObjectCollection> collections = List.of(new ObjectCollection.Builder().build());
        function.accept(result, collections);
        
        verify(findImageV2).findBest(result, collections);
    }
    
    @Test
    void get_shouldHandleAllPatternFindStrategies() {
        // Test uses the registry created in setUp
        
        // Test FIRST
        PatternFindOptions firstOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build();
        BiConsumer<ActionResult, List<ObjectCollection>> firstFunction = registry.get(firstOptions);
        assertNotNull(firstFunction);
        
        // Test ALL
        PatternFindOptions allOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.ALL)
            .build();
        BiConsumer<ActionResult, List<ObjectCollection>> allFunction = registry.get(allOptions);
        assertNotNull(allFunction);
        
        // Test BEST
        PatternFindOptions bestOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
        BiConsumer<ActionResult, List<ObjectCollection>> bestFunction = registry.get(bestOptions);
        assertNotNull(bestFunction);
        
        // Test EACH
        PatternFindOptions eachOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.EACH)
            .build();
        BiConsumer<ActionResult, List<ObjectCollection>> eachFunction = registry.get(eachOptions);
        assertNotNull(eachFunction);
        
        // Verify one of them works correctly
        ActionResult result = new ActionResult();
        List<ObjectCollection> collections = List.of(new ObjectCollection.Builder().build());
        bestFunction.accept(result, collections);
        verify(findImageV2).findBest(result, collections);
    }
    
    @Test
    void get_shouldReturnNullForUnregisteredOptions() {
        // Test uses the registry created in setUp
        
        // Create a custom find options that's not registered
        BaseFindOptions unknownOptions = new TestFindOptions();
        
        BiConsumer<ActionResult, List<ObjectCollection>> function = registry.get(unknownOptions);
        
        // Should return null for unknown options type
        assertNull(function);
    }
    
    // Test implementation of BaseFindOptions for testing
    private static class TestFindOptions extends BaseFindOptions {
        public TestFindOptions() {
            super(new TestBuilder());
        }
        
        @Override
        public FindStrategy getFindStrategy() {
            return FindStrategy.CUSTOM;
        }
        
        // Proper builder implementation
        private static class TestBuilder extends Builder<TestBuilder> {
            @Override
            protected TestBuilder self() {
                return this;
            }
        }
    }
}