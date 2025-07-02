package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
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
    
    private FindStrategyRegistryV2 registry;
    
    @Mock
    private FindImage findImage;
    
    @Mock
    private Object findText;  // Mock as Object since we don't have the actual class
    
    @Mock
    private Object findColor;
    
    @Mock
    private Object findHistogram;
    
    @Mock
    private Object findMotion;
    
    @Mock
    private Object dynamicPixels;
    
    @Mock
    private Object fixedPixels;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Create registry with available components
        // In a real test, we'd need the actual classes or proper mocks
    }
    
    @Test
    void get_shouldReturnCorrectFunctionForPatternFindOptions() {
        // Create a minimal registry with just FindImage
        registry = new FindStrategyRegistryV2(
            findImage, null, null, null, null, null, null
        );
        
        PatternFindOptions options = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
            
        BiConsumer<ActionResult, List<ObjectCollection>> function = registry.get(options);
        
        assertNotNull(function);
        
        // Verify it calls the correct method
        ActionResult result = new ActionResult();
        List<ObjectCollection> collections = List.of(new ObjectCollection.Builder().build());
        function.accept(result, collections);
        
        verify(findImage).findBest(result, collections);
    }
    
    @Test
    void get_shouldHandleAllPatternFindStrategies() {
        registry = new FindStrategyRegistryV2(
            findImage, null, null, null, null, null, null
        );
        
        // Test FIRST
        PatternFindOptions firstOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build();
        BiConsumer<ActionResult, List<ObjectCollection>> firstFunction = registry.get(firstOptions);
        assertNotNull(firstFunction);
        
        // Verify it maps to findAll (based on the registry implementation)
        ActionResult result = new ActionResult();
        List<ObjectCollection> collections = List.of(new ObjectCollection.Builder().build());
        firstFunction.accept(result, collections);
        verify(findImage).findAll(result, collections);
        
        // Test ALL
        PatternFindOptions allOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.ALL)
            .build();
        assertNotNull(registry.get(allOptions));
        
        // Test BEST
        PatternFindOptions bestOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
        assertNotNull(registry.get(bestOptions));
        
        // Test EACH
        PatternFindOptions eachOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.EACH)
            .build();
        assertNotNull(registry.get(eachOptions));
    }
    
    @Test
    void get_shouldReturnNullForUnregisteredOptions() {
        registry = new FindStrategyRegistryV2(
            findImage, null, null, null, null, null, null
        );
        
        // Create a custom find options that's not registered
        BaseFindOptions unknownOptions = new TestFindOptions();
        
        BiConsumer<ActionResult, List<ObjectCollection>> function = registry.get(unknownOptions);
        
        // Should return null for unknown options type
        assertNull(function);
    }
    
    // Test implementation of BaseFindOptions for testing
    private static class TestFindOptions extends BaseFindOptions {
        public TestFindOptions() {
            super(new Builder<TestFindOptions.Builder>() {
                @Override
                protected TestFindOptions.Builder self() {
                    return (TestFindOptions.Builder) this;
                }
            });
        }
        
        @Override
        public FindStrategy getFindStrategy() {
            return FindStrategy.CUSTOM;
        }
    }
}