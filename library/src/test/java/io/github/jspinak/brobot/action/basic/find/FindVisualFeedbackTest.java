package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.tools.logging.visual.HighlightManager;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Unit test for visual feedback integration in FindPipeline.
 */
public class FindVisualFeedbackTest {
    
    @Mock
    private FindStrategyRegistryV2 findFunctions;
    
    @Mock
    private HighlightManager highlightManager;
    
    @Mock
    private VisualFeedbackConfig visualFeedbackConfig;
    
    @Mock
    private BrobotLogger brobotLogger;
    
    private FindPipeline findPipeline;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create a minimal FindPipeline with mocked dependencies
        findPipeline = new FindPipeline(
            mock(io.github.jspinak.brobot.analysis.color.profiles.ProfileSetBuilder.class),
            mock(io.github.jspinak.brobot.action.internal.find.OffsetLocationManagerV2.class),
            mock(io.github.jspinak.brobot.analysis.match.MatchFusion.class),
            mock(io.github.jspinak.brobot.action.internal.find.match.MatchAdjusterV2.class),
            mock(io.github.jspinak.brobot.action.internal.find.match.MatchContentExtractor.class),
            mock(io.github.jspinak.brobot.action.internal.find.NonImageObjectConverter.class),
            mock(io.github.jspinak.brobot.statemanagement.StateMemory.class),
            mock(io.github.jspinak.brobot.util.string.TextSelector.class),
            mock(io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver.class),
            highlightManager,
            visualFeedbackConfig
        );
        
        // Inject mocked HighlightManager and VisualFeedbackConfig
        ReflectionTestUtils.setField(findPipeline, "highlightManager", highlightManager);
        ReflectionTestUtils.setField(findPipeline, "visualFeedbackConfig", visualFeedbackConfig);
        ReflectionTestUtils.setField(findPipeline, "highlightEnabled", true);
    }
    
    @Test
    public void testHighlightSearchRegionsBeforeFind() {
        // Arrange
        when(visualFeedbackConfig.isEnabled()).thenReturn(true);
        when(visualFeedbackConfig.isAutoHighlightSearchRegions()).thenReturn(true);
        
        Region searchRegion = new Region(100, 100, 200, 200);
        StateRegion stateRegion = new StateRegion();
        stateRegion.setSearchRegion(searchRegion);
        
        ObjectCollection collection = new ObjectCollection.Builder()
            .withRegions(stateRegion)
            .build();
        
        BaseFindOptions options = new SimpleFindOptions();
        ActionResult result = new ActionResult();
        result.setActionConfig(options);
        
        // Mock the find function
        when(findFunctions.get(any(BaseFindOptions.class))).thenReturn((matches, collections) -> {
            // Do nothing for this test
        });
        
        // Act
        findPipeline.execute(options, result, collection);
        
        // Assert
        verify(highlightManager).highlightSearchRegions(argThat(regions -> 
            regions.size() == 1 && regions.get(0).equals(searchRegion)
        ));
    }
    
    @Test
    public void testHighlightMatchesAfterSuccessfulFind() {
        // Arrange
        when(visualFeedbackConfig.isEnabled()).thenReturn(true);
        when(visualFeedbackConfig.isAutoHighlightFinds()).thenReturn(true);
        when(visualFeedbackConfig.isAutoHighlightSearchRegions()).thenReturn(false);
        
        ObjectCollection collection = new ObjectCollection.Builder().build();
        
        BaseFindOptions options = new SimpleFindOptions();
        ActionResult result = new ActionResult();
        result.setActionConfig(options);
        
        // Create a match to be returned by the find operation
        Match match = new Match();
        match.setRegion(new Region(150, 150, 50, 50));
        match.setScore(0.95);
        
        // Mock the find function to add a match
        when(findFunctions.get(any(BaseFindOptions.class))).thenReturn((matches, collections) -> {
            matches.getMatchList().add(match);
            matches.setSuccess(true);
        });
        
        // Act
        findPipeline.execute(options, result, collection);
        
        // Assert
        verify(highlightManager).highlightMatches(argThat(matches -> 
            matches.size() == 1 && matches.get(0).equals(match)
        ));
    }
    
    @Test
    public void testNoHighlightingWhenDisabled() {
        // Arrange
        when(visualFeedbackConfig.isEnabled()).thenReturn(false);
        
        ObjectCollection collection = new ObjectCollection.Builder().build();
        
        BaseFindOptions options = new SimpleFindOptions();
        ActionResult result = new ActionResult();
        result.setActionConfig(options);
        
        // Mock the find function
        when(findFunctions.get(any(BaseFindOptions.class))).thenReturn((matches, collections) -> {
            // Do nothing
        });
        
        // Act
        findPipeline.execute(options, result, collection);
        
        // Assert
        verify(highlightManager, never()).highlightSearchRegions(any());
        verify(highlightManager, never()).highlightMatches(any());
    }
    
    /**
     * Simple implementation of BaseFindOptions for testing
     */
    private static class SimpleFindOptions extends BaseFindOptions {
        private final FindStrategy strategy;
        
        public SimpleFindOptions() {
            super(new Builder());
            this.strategy = FindStrategy.FIRST;
        }
        
        @Override
        public FindStrategy getFindStrategy() {
            return strategy;
        }
        
        private static class Builder extends BaseFindOptions.Builder<Builder> {
            @Override
            protected Builder self() {
                return this;
            }
        }
    }
}