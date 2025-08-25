package io.github.jspinak.brobot.tools.logging.visual;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogBuilder;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HighlightManagerTest extends BrobotTestBase {

    private HighlightManager highlightManager;
    
    @Mock
    private VisualFeedbackConfig config;
    
    @Mock
    private BrobotLogger brobotLogger;
    
    @Mock
    private LogBuilder logBuilder;
    
    @Mock
    private Action action;
    
    @Mock
    private VisualFeedbackConfig.FindHighlightConfig findConfig;
    
    @Mock
    private VisualFeedbackConfig.SearchRegionHighlightConfig searchConfig;
    
    @Mock
    private VisualFeedbackConfig.ErrorHighlightConfig errorConfig;
    
    @Mock
    private VisualFeedbackConfig.ClickHighlightConfig clickConfig;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Set up logging chain
        lenient().when(brobotLogger.log()).thenReturn(logBuilder);
        lenient().when(logBuilder.observation(anyString())).thenReturn(logBuilder);
        lenient().when(logBuilder.metadata(anyString(), any())).thenReturn(logBuilder);
        lenient().when(logBuilder.level(any())).thenReturn(logBuilder);
        lenient().when(logBuilder.message(anyString())).thenReturn(logBuilder);
        lenient().when(logBuilder.error(any(Exception.class))).thenReturn(logBuilder);
        lenient().doNothing().when(logBuilder).log();
        
        // Set up config
        lenient().when(config.getFind()).thenReturn(findConfig);
        lenient().when(config.getSearchRegion()).thenReturn(searchConfig);
        lenient().when(config.getError()).thenReturn(errorConfig);
        lenient().when(config.getClick()).thenReturn(clickConfig);
        
        // Set up default config values
        lenient().when(findConfig.getColor()).thenReturn("#00FF00");
        lenient().when(findConfig.getColorObject()).thenReturn(Color.GREEN);
        lenient().when(findConfig.getDuration()).thenReturn(2.0);
        lenient().when(findConfig.getBorderWidth()).thenReturn(3);
        lenient().when(findConfig.isFlash()).thenReturn(false);
        lenient().when(findConfig.getFlashCount()).thenReturn(2);
        lenient().when(findConfig.getFlashInterval()).thenReturn(300L);
        
        lenient().when(searchConfig.getColor()).thenReturn("#0000FF");
        lenient().when(searchConfig.getColorObject()).thenReturn(Color.BLUE);
        lenient().when(searchConfig.getDuration()).thenReturn(1.0);
        lenient().when(searchConfig.getBorderWidth()).thenReturn(2);
        lenient().when(searchConfig.isShowDimensions()).thenReturn(false);
        
        lenient().when(errorConfig.getColor()).thenReturn("#FF0000");
        lenient().when(errorConfig.getColorObject()).thenReturn(Color.RED);
        lenient().when(errorConfig.getDuration()).thenReturn(3.0);
        lenient().when(errorConfig.isShowCrossMark()).thenReturn(false);
        
        lenient().when(clickConfig.getColorObject()).thenReturn(Color.YELLOW);
        lenient().when(clickConfig.getDuration()).thenReturn(0.5);
        lenient().when(clickConfig.getRadius()).thenReturn(20);
        lenient().when(clickConfig.isRippleEffect()).thenReturn(false);
        
        highlightManager = new HighlightManager(config, brobotLogger);
        ReflectionTestUtils.setField(highlightManager, "action", action);
    }
    
    @Test
    public void testHighlightMatches_Enabled() {
        // Arrange
        when(config.isEnabled()).thenReturn(true);
        when(config.isAutoHighlightFinds()).thenReturn(true);
        
        List<Match> matches = new ArrayList<>();
        matches.add(new Match.Builder()
            .setRegion(new Region(100, 100, 50, 50))
            .setSimScore(0.95)
            .build());
        matches.add(new Match.Builder()
            .setRegion(new Region(200, 200, 60, 60))
            .setSimScore(0.90)
            .build());
        
        ActionResult mockResult = new ActionResult();
        mockResult.setSuccess(true);
        when(action.perform(any(HighlightOptions.class), any(ObjectCollection.class)))
            .thenReturn(mockResult);
        
        // Act
        highlightManager.highlightMatches(matches);
        
        // Assert
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder).observation("Highlighted matches");
        verify(logBuilder).metadata("matchCount", 2);
    }
    
    @Test
    public void testHighlightMatches_Disabled() {
        // Arrange
        when(config.isEnabled()).thenReturn(false);
        
        List<Match> matches = Arrays.asList(
            new Match.Builder()
                .setRegion(new Region(100, 100, 50, 50))
                .setSimScore(0.95)
                .build()
        );
        
        // Act
        highlightManager.highlightMatches(matches);
        
        // Assert
        verify(action, never()).perform(any(HighlightOptions.class), any(ObjectCollection.class));
        verify(brobotLogger, never()).log();
    }
    
    @Test
    public void testHighlightMatches_EmptyList() {
        // Arrange
        when(config.isEnabled()).thenReturn(true);
        when(config.isAutoHighlightFinds()).thenReturn(true);
        
        List<Match> matches = new ArrayList<>();
        
        // Act
        highlightManager.highlightMatches(matches);
        
        // Assert
        verify(action, never()).perform(any(HighlightOptions.class), any(ObjectCollection.class));
        verify(brobotLogger, never()).log();
    }
    
    @Test
    public void testHighlightMatches_WithFlash() {
        // Arrange
        when(config.isEnabled()).thenReturn(true);
        when(config.isAutoHighlightFinds()).thenReturn(true);
        when(findConfig.isFlash()).thenReturn(true);
        when(findConfig.getFlashCount()).thenReturn(3);
        when(findConfig.getFlashInterval()).thenReturn(200L);
        
        List<Match> matches = Arrays.asList(
            new Match.Builder()
                .setRegion(new Region(100, 100, 50, 50))
                .setSimScore(0.95)
                .build()
        );
        
        ActionResult mockResult = new ActionResult();
        mockResult.setSuccess(true);
        when(action.perform(any(HighlightOptions.class), any(ObjectCollection.class)))
            .thenReturn(mockResult);
        
        // Act
        highlightManager.highlightMatches(matches);
        
        // Assert
        verify(brobotLogger, atLeastOnce()).log();
        // Flash effect is async, so we just verify it was triggered
        verify(action, atLeastOnce()).perform(any(HighlightOptions.class), any(ObjectCollection.class));
    }
    
    @Test
    public void testHighlightSearchRegions_Enabled() {
        // Arrange
        when(config.isEnabled()).thenReturn(true);
        when(config.isAutoHighlightSearchRegions()).thenReturn(true);
        
        List<Region> regions = Arrays.asList(
            new Region(0, 0, 100, 100),
            new Region(200, 200, 150, 150)
        );
        
        ActionResult mockResult = new ActionResult();
        mockResult.setSuccess(true);
        mockResult.setMatchList(new ArrayList<>());
        when(action.perform(any(HighlightOptions.class), any(ObjectCollection.class)))
            .thenReturn(mockResult);
        
        // Act
        highlightManager.highlightSearchRegions(regions);
        
        // Assert
        ArgumentCaptor<HighlightOptions> optionsCaptor = ArgumentCaptor.forClass(HighlightOptions.class);
        ArgumentCaptor<ObjectCollection> collectionCaptor = ArgumentCaptor.forClass(ObjectCollection.class);
        verify(action).perform(optionsCaptor.capture(), collectionCaptor.capture());
        
        HighlightOptions capturedOptions = optionsCaptor.getValue();
        assertEquals(1.0, capturedOptions.getHighlightSeconds(), 0.01);
        assertTrue(capturedOptions.isHighlightAllAtOnce());
        
        ObjectCollection capturedCollection = collectionCaptor.getValue();
        assertEquals(2, capturedCollection.getStateRegions().size());
        
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder).observation("Highlighted search regions");
        verify(logBuilder).metadata("regionCount", 2);
    }
    
    @Test
    public void testHighlightSearchRegions_Disabled() {
        // Arrange
        when(config.isEnabled()).thenReturn(false);
        
        List<Region> regions = Arrays.asList(new Region(0, 0, 100, 100));
        
        // Act
        highlightManager.highlightSearchRegions(regions);
        
        // Assert
        verify(action, never()).perform(any(HighlightOptions.class), any(ObjectCollection.class));
    }
    
    @Test
    public void testHighlightSearchRegions_WithDimensions() {
        // Arrange
        when(config.isEnabled()).thenReturn(true);
        when(config.isAutoHighlightSearchRegions()).thenReturn(true);
        when(searchConfig.isShowDimensions()).thenReturn(true);
        
        List<Region> regions = Arrays.asList(
            new Region(0, 0, 100, 100)
        );
        
        ActionResult mockResult = new ActionResult();
        mockResult.setSuccess(true);
        when(action.perform(any(HighlightOptions.class), any(ObjectCollection.class)))
            .thenReturn(mockResult);
        
        // Act
        highlightManager.highlightSearchRegions(regions);
        
        // Assert
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder).observation("Region dimensions");
        verify(logBuilder).metadata("dimensions", "100x100");
    }
    
    @Test
    public void testHighlightSearchRegions_ActionNotAvailable() {
        // Arrange
        ReflectionTestUtils.setField(highlightManager, "action", null);
        when(config.isEnabled()).thenReturn(true);
        when(config.isAutoHighlightSearchRegions()).thenReturn(true);
        
        List<Region> regions = Arrays.asList(new Region(0, 0, 100, 100));
        
        // Act
        highlightManager.highlightSearchRegions(regions);
        
        // Assert
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder).error(any(IllegalStateException.class));
        verify(logBuilder).message("Cannot display highlights - Action component not available");
    }
    
    @Test
    public void testHighlightSearchRegionsWithContext() {
        // Arrange
        when(config.isEnabled()).thenReturn(true);
        when(config.isAutoHighlightSearchRegions()).thenReturn(true);
        
        List<HighlightManager.RegionWithContext> regionsWithContext = Arrays.asList(
            new HighlightManager.RegionWithContext(
                new Region(0, 0, 100, 100), "State1", "Object1"),
            new HighlightManager.RegionWithContext(
                new Region(200, 200, 150, 150), "State2", "Object2")
        );
        
        ActionResult mockResult = new ActionResult();
        mockResult.setSuccess(true);
        when(action.perform(any(HighlightOptions.class), any(ObjectCollection.class)))
            .thenReturn(mockResult);
        
        // Act
        highlightManager.highlightSearchRegionsWithContext(regionsWithContext);
        
        // Assert
        verify(action).perform(any(HighlightOptions.class), any(ObjectCollection.class));
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder, atLeastOnce()).observation(contains("Highlighting region for State1.Object1"));
        verify(logBuilder, atLeastOnce()).metadata("state", "State1");
        verify(logBuilder, atLeastOnce()).metadata("object", "Object1");
    }
    
    @Test
    public void testHighlightClick_Enabled() {
        // Arrange
        when(config.isEnabled()).thenReturn(true);
        when(config.getClick()).thenReturn(clickConfig);
        when(clickConfig.isEnabled()).thenReturn(true);
        when(clickConfig.isRippleEffect()).thenReturn(false);
        
        ActionResult mockResult = new ActionResult();
        mockResult.setSuccess(true);
        when(action.perform(any(HighlightOptions.class), any(ObjectCollection.class)))
            .thenReturn(mockResult);
        
        // Act
        highlightManager.highlightClick(100, 200);
        
        // Assert
        // Highlighting is async, so we verify it was initiated
        verify(action, timeout(1000).atLeastOnce())
            .perform(any(HighlightOptions.class), any(ObjectCollection.class));
    }
    
    @Test
    public void testHighlightClick_Disabled() {
        // Arrange
        when(config.isEnabled()).thenReturn(false);
        
        // Act
        highlightManager.highlightClick(100, 200);
        
        // Assert
        verify(action, never()).perform(any(HighlightOptions.class), any(ObjectCollection.class));
    }
    
    @Test
    public void testHighlightClick_WithRippleEffect() {
        // Arrange
        when(config.isEnabled()).thenReturn(true);
        when(config.getClick()).thenReturn(clickConfig);
        when(clickConfig.isEnabled()).thenReturn(true);
        when(clickConfig.isRippleEffect()).thenReturn(true);
        when(clickConfig.getRadius()).thenReturn(10);
        when(clickConfig.getDuration()).thenReturn(0.5);
        
        ActionResult mockResult = new ActionResult();
        mockResult.setSuccess(true);
        when(action.perform(any(HighlightOptions.class), any(ObjectCollection.class)))
            .thenReturn(mockResult);
        
        // Act
        highlightManager.highlightClick(100, 200);
        
        // Assert
        // Ripple effect is async with multiple steps
        verify(action, timeout(2000).atLeast(1))
            .perform(any(HighlightOptions.class), any(ObjectCollection.class));
    }
    
    @Test
    public void testHighlightError_Enabled() {
        // Arrange
        when(config.isEnabled()).thenReturn(true);
        when(config.getError()).thenReturn(errorConfig);
        when(errorConfig.isEnabled()).thenReturn(true);
        when(errorConfig.isShowCrossMark()).thenReturn(false);
        
        Region searchRegion = new Region(50, 50, 200, 200);
        
        ActionResult mockResult = new ActionResult();
        mockResult.setSuccess(true);
        when(action.perform(any(HighlightOptions.class), any(ObjectCollection.class)))
            .thenReturn(mockResult);
        
        // Act
        highlightManager.highlightError(searchRegion);
        
        // Assert
        verify(action, timeout(1000).atLeastOnce())
            .perform(any(HighlightOptions.class), any(ObjectCollection.class));
    }
    
    @Test
    public void testHighlightError_WithCrossMark() {
        // Arrange
        when(config.isEnabled()).thenReturn(true);
        when(config.getError()).thenReturn(errorConfig);
        when(errorConfig.isEnabled()).thenReturn(true);
        when(errorConfig.isShowCrossMark()).thenReturn(true);
        
        Region searchRegion = new Region(50, 50, 200, 200);
        
        ActionResult mockResult = new ActionResult();
        mockResult.setSuccess(true);
        when(action.perform(any(HighlightOptions.class), any(ObjectCollection.class)))
            .thenReturn(mockResult);
        
        // Act
        highlightManager.highlightError(searchRegion);
        
        // Assert
        // Cross mark creates additional highlights
        verify(action, timeout(1000).atLeast(2))
            .perform(any(HighlightOptions.class), any(ObjectCollection.class));
    }
    
    @Test
    public void testHighlightError_NullRegion() {
        // Arrange
        when(config.isEnabled()).thenReturn(true);
        when(config.getError()).thenReturn(errorConfig);
        when(errorConfig.isEnabled()).thenReturn(true);
        
        // Act
        highlightManager.highlightError(null);
        
        // Assert
        verify(action, never()).perform(any(HighlightOptions.class), any(ObjectCollection.class));
    }
    
    @Test
    public void testHighlightRegion_InMockMode() {
        // Arrange
        FrameworkSettings.mock = true;
        when(config.isEnabled()).thenReturn(true);
        when(config.isAutoHighlightFinds()).thenReturn(true);
        
        List<Match> matches = Arrays.asList(
            new Match.Builder()
                .setRegion(new Region(100, 100, 50, 50))
                .setSimScore(0.95)
                .build()
        );
        
        // Act
        highlightManager.highlightMatches(matches);
        
        // Assert
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder).observation("Mock highlight");
        verify(logBuilder).metadata("type", "FIND");
        verify(action, never()).perform(any(HighlightOptions.class), any(ObjectCollection.class));
    }
    
    @Test
    public void testClearAllHighlights() {
        // Act
        highlightManager.clearAllHighlights();
        
        // Assert
        verify(brobotLogger).log();
        verify(logBuilder).observation("Cleared all highlights");
    }
    
    @Test
    public void testGetConfig() {
        // Act
        VisualFeedbackConfig returnedConfig = highlightManager.getConfig();
        
        // Assert
        assertSame(config, returnedConfig);
    }
    
    @Test
    public void testGetColorName_CommonColors() {
        // Use reflection to test private method
        HighlightManager manager = new HighlightManager(config, brobotLogger);
        
        assertEquals("red", invokeGetColorName(manager, Color.RED));
        assertEquals("green", invokeGetColorName(manager, Color.GREEN));
        assertEquals("blue", invokeGetColorName(manager, Color.BLUE));
        assertEquals("yellow", invokeGetColorName(manager, Color.YELLOW));
        assertEquals("cyan", invokeGetColorName(manager, Color.CYAN));
        assertEquals("magenta", invokeGetColorName(manager, Color.MAGENTA));
        assertEquals("white", invokeGetColorName(manager, Color.WHITE));
        assertEquals("black", invokeGetColorName(manager, Color.BLACK));
        assertEquals("gray", invokeGetColorName(manager, Color.GRAY));
        assertEquals("orange", invokeGetColorName(manager, Color.ORANGE));
    }
    
    @Test
    public void testGetColorName_CustomColors() {
        HighlightManager manager = new HighlightManager(config, brobotLogger);
        
        // Test color approximation
        assertEquals("red", invokeGetColorName(manager, new Color(220, 50, 50)));
        assertEquals("green", invokeGetColorName(manager, new Color(50, 220, 50)));
        assertEquals("blue", invokeGetColorName(manager, new Color(50, 50, 220)));
        assertEquals("yellow", invokeGetColorName(manager, new Color(220, 220, 50)));
        assertEquals("cyan", invokeGetColorName(manager, new Color(50, 220, 220)));
        assertEquals("magenta", invokeGetColorName(manager, new Color(220, 50, 220)));
        assertEquals("orange", invokeGetColorName(manager, new Color(220, 160, 50)));
        assertEquals("white", invokeGetColorName(manager, new Color(200, 200, 200)));
        assertEquals("black", invokeGetColorName(manager, new Color(50, 50, 50)));
        assertEquals("gray", invokeGetColorName(manager, new Color(128, 128, 128)));
    }
    
    private String invokeGetColorName(HighlightManager manager, Color color) {
        try {
            return (String) ReflectionTestUtils.invokeMethod(manager, "getColorName", color);
        } catch (Exception e) {
            fail("Failed to invoke getColorName: " + e.getMessage());
            return null;
        }
    }
    
    @Test
    public void testHighlightMatches_NullRegionInMatch() {
        // Arrange
        when(config.isEnabled()).thenReturn(true);
        when(config.isAutoHighlightFinds()).thenReturn(true);
        
        List<Match> matches = Arrays.asList(
            new Match.Builder()
                .setRegion(null)  // Null region
                .setSimScore(0.95)
                .build(),
            new Match.Builder()
                .setRegion(new Region(100, 100, 50, 50))
                .setSimScore(0.90)
                .build()
        );
        
        ActionResult mockResult = new ActionResult();
        mockResult.setSuccess(true);
        when(action.perform(any(HighlightOptions.class), any(ObjectCollection.class)))
            .thenReturn(mockResult);
        
        // Act
        highlightManager.highlightMatches(matches);
        
        // Assert - Should still process the valid match
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder).observation("Highlighted matches");
        verify(logBuilder).metadata("matchCount", 2);
    }
    
    @Test
    public void testHighlightSearchRegions_ActionFailure() {
        // Arrange
        when(config.isEnabled()).thenReturn(true);
        when(config.isAutoHighlightSearchRegions()).thenReturn(true);
        
        List<Region> regions = Arrays.asList(new Region(0, 0, 100, 100));
        
        ActionResult mockResult = new ActionResult();
        mockResult.setSuccess(false);  // Action fails
        when(action.perform(any(HighlightOptions.class), any(ObjectCollection.class)))
            .thenReturn(mockResult);
        
        // Act
        highlightManager.highlightSearchRegions(regions);
        
        // Assert
        verify(action).perform(any(HighlightOptions.class), any(ObjectCollection.class));
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder).observation("Highlight action completed");
        verify(logBuilder).metadata("success", false);
    }
    
    @Test
    public void testHighlightSearchRegions_ExceptionHandling() {
        // Arrange
        when(config.isEnabled()).thenReturn(true);
        when(config.isAutoHighlightSearchRegions()).thenReturn(true);
        
        List<Region> regions = Arrays.asList(new Region(0, 0, 100, 100));
        
        when(action.perform(any(HighlightOptions.class), any(ObjectCollection.class)))
            .thenThrow(new RuntimeException("Test exception"));
        
        // Act
        highlightManager.highlightSearchRegions(regions);
        
        // Assert - Should handle exception gracefully
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder).observation("Highlighted search regions");
    }
}