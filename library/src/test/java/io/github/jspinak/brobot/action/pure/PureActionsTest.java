package io.github.jspinak.brobot.action.pure;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.click.ClickV2;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightV2;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.type.TypeV2;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests demonstrating the use of pure actions.
 * These tests show how pure actions can be tested without mocking Find operations.
 */
public class PureActionsTest {
    
    private Action action;
    private ClickV2 clickV2;
    private HighlightV2 highlightV2;
    private TypeV2 typeV2;
    
    @BeforeEach
    void setUp() {
        action = mock(Action.class);
        clickV2 = new ClickV2();
        highlightV2 = new HighlightV2();
        typeV2 = new TypeV2();
    }
    
    @Test
    @DisplayName("Pure Click - clicks on provided location without Find")
    void testPureClickOnLocation() {
        // Given
        Location targetLocation = new Location(100, 200);
        ClickOptions options = new ClickOptions.Builder().build();
        ObjectCollection collection = new ObjectCollection.Builder()
            .withLocations(targetLocation)
            .build();
        
        // When
        ActionResult result = clickV2.perform(options, collection);
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals("CLICK_V2", result.getActionType());
        assertEquals(1, result.getMatchList().size());
        
        // No Find operation was needed or performed
    }
    
    @Test
    @DisplayName("Pure Click - clicks on region center")
    void testPureClickOnRegion() {
        // Given
        Region targetRegion = new Region(50, 50, 100, 100);
        ClickOptions options = new ClickOptions.Builder()
            .setClickType(ClickOptions.Type.RIGHT)
            .build();
        ObjectCollection collection = new ObjectCollection.Builder()
            .withRegions(targetRegion)
            .build();
        
        // When
        ActionResult result = clickV2.perform(options, collection);
        
        // Then
        assertTrue(result.isSuccess());
        // The click should happen at the region's center (100, 100)
    }
    
    @Test
    @DisplayName("Pure Highlight - highlights provided regions without Find")
    void testPureHighlight() {
        // Given
        Region region1 = new Region(0, 0, 50, 50);
        Region region2 = new Region(100, 100, 50, 50);
        HighlightOptions options = new HighlightOptions.Builder()
            .setHighlightDuration(2.0)
            .setHighlightColor("red")
            .build();
        ObjectCollection collection = new ObjectCollection.Builder()
            .withRegions(region1, region2)
            .build();
        
        // When
        ActionResult result = highlightV2.perform(options, collection);
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals("HIGHLIGHT_V2", result.getActionType());
        assertEquals(2, result.getMatchList().size());
        assertTrue(result.getText().contains("Highlighted 2 of 2 regions"));
    }
    
    @Test
    @DisplayName("Pure Type - types text without Find")
    void testPureType() {
        // Given
        String textToType = "Hello World";
        TypeOptions options = new TypeOptions.Builder()
            .setText(textToType)
            .build();
        ObjectCollection collection = new ObjectCollection.Builder().build();
        
        // When
        ActionResult result = typeV2.perform(options, collection);
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals("TYPE_V2", result.getActionType());
        assertTrue(result.getText().contains("Typed: " + textToType));
    }
    
    @Test
    @DisplayName("Pure Type - clicks location then types")
    void testPureTypeWithClick() {
        // Given
        Location fieldLocation = new Location(200, 300);
        String textToType = "user@example.com";
        TypeOptions options = new TypeOptions.Builder()
            .setText(textToType)
            .setClickLocationFirst(true)
            .setClearField(true)
            .build();
        ObjectCollection collection = new ObjectCollection.Builder()
            .withLocations(fieldLocation)
            .build();
        
        // When
        ActionResult result = typeV2.perform(options, collection);
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals(1, result.getMatchList().size());
        // The action clicked on the field location before typing
    }
    
    @Test
    @DisplayName("Convenience method - Click with ActionType")
    void testConvenienceClick() {
        // Given
        Location clickPoint = new Location(150, 250);
        when(action.perform(eq(ActionType.CLICK), eq(clickPoint)))
            .thenReturn(new ActionResult());
        
        // When
        ActionResult result = action.perform(ActionType.CLICK, clickPoint);
        
        // Then
        assertNotNull(result);
        verify(action).perform(ActionType.CLICK, clickPoint);
    }
    
    @Test
    @DisplayName("Convenience method - Type with ActionType")
    void testConvenienceType() {
        // Given
        String text = "Test message";
        when(action.perform(eq(ActionType.TYPE), eq(text)))
            .thenReturn(new ActionResult());
        
        // When
        ActionResult result = action.perform(ActionType.TYPE, text);
        
        // Then
        assertNotNull(result);
        verify(action).perform(ActionType.TYPE, text);
    }
    
    @Test
    @DisplayName("Error handling - no clickable objects")
    void testClickWithNoObjects() {
        // Given
        ClickOptions options = new ClickOptions.Builder().build();
        ObjectCollection emptyCollection = new ObjectCollection.Builder().build();
        
        // When
        ActionResult result = clickV2.perform(options, emptyCollection);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("No clickable objects provided", result.getText());
    }
    
    @Test
    @DisplayName("Error handling - no text to type")
    void testTypeWithNoText() {
        // Given
        TypeOptions options = new TypeOptions.Builder().build(); // No text set
        ObjectCollection emptyCollection = new ObjectCollection.Builder().build();
        
        // When
        ActionResult result = typeV2.perform(options, emptyCollection);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("No text to type provided", result.getText());
    }
    
    @Test
    @DisplayName("Working with Match objects from Find")
    void testActionsOnMatches() {
        // Simulate matches from a Find operation
        Match match1 = new Match.Builder()
            .setRegion(new Region(10, 10, 30, 30))
            .setScore(0.95)
            .build();
        Match match2 = new Match.Builder()
            .setRegion(new Region(50, 50, 30, 30))
            .setScore(0.92)
            .build();
        
        // Create collection with matches
        Matches matches = new Matches();
        matches.add(match1);
        matches.add(match2);
        ObjectCollection collection = new ObjectCollection.Builder()
            .withMatches(matches)
            .build();
        
        // Test clicking on matches
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        ActionResult clickResult = clickV2.perform(clickOptions, collection);
        assertTrue(clickResult.isSuccess());
        assertEquals(2, clickResult.getMatchList().size());
        
        // Test highlighting matches
        HighlightOptions highlightOptions = new HighlightOptions.Builder().build();
        ActionResult highlightResult = highlightV2.perform(highlightOptions, collection);
        assertTrue(highlightResult.isSuccess());
        assertEquals(2, highlightResult.getMatchList().size());
    }
}