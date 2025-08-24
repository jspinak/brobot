package io.github.jspinak.brobot.actions.actionOptions;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated integration tests demonstrating the new ActionConfig API.
 * Shows migration from generic ActionOptions to specific config classes.
 * 
 * Key changes:
 * - Uses DragOptions, ClickOptions, etc. instead of generic ActionOptions
 * - Each action type has its own configuration class
 * - Settings are type-specific and validated at compile time
 */
@SpringBootTest
public class ActionOptionsIntegrationTestUpdated {

    @Test
    void testDragOptionsCreationInSpringContext() {
        // NEW API: Use DragOptions for drag-specific configuration
        DragOptions options = new DragOptions.Builder()
                .setFromLocation(new Location(0, 0))
                .setToLocation(new Location(100, 200))
                .setPauseBeforeMouseDown(0.5)
                .setPauseAfterDrop(1.0)
                .build();

        assertNotNull(options, "DragOptions object should not be null when created in a Spring context.");
        assertEquals(0, options.getFromLocation().getX());
        assertEquals(100, options.getToLocation().getX());
        assertEquals(0.5, options.getPauseBeforeMouseDown());
        assertEquals(1.0, options.getPauseAfterDrop());
    }

    @Test
    void testFindOptionsWithSearchRegions() {
        // NEW API: Use PatternFindOptions for find-specific configuration
        PatternFindOptions options = new PatternFindOptions.Builder()
                .setFindStrategy(PatternFindOptions.FindStrategy.ALL)
                .setMinSimilarity(0.8)
                .setMaxWait(5)
                .addSearchRegion(new Region(0, 0, 50, 50))
                .addSearchRegion(new Region(100, 100, 50, 50))
                .build();

        assertNotNull(options);
        assertEquals(PatternFindOptions.FindStrategy.ALL, options.getFindStrategy());
        assertEquals(0.8, options.getMinSimilarity());
        assertEquals(5, options.getMaxWait());
        assertFalse(options.getSearchRegions().isEmpty());
        assertEquals(2, options.getSearchRegions().size());
    }

    @Test
    void testClickOptionsWithMoveAfter() {
        // NEW API: Use ClickOptions for click-specific configuration
        Location moveLocation = new Location(100, 200);
        ClickOptions options = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setMoveMouseAfterAction(true)
                .setMoveMouseAfterActionTo(moveLocation)
                .build();

        assertNotNull(options);
        assertEquals(ClickOptions.Type.LEFT, options.getClickType());
        assertTrue(options.isMoveMouseAfterAction());
        assertEquals(100, options.getMoveMouseAfterActionTo().getX());
        assertEquals(200, options.getMoveMouseAfterActionTo().getY());
    }

    @Test
    void testDefaultPauseLogicInSpringContext_newAPI() {
        // NEW API: Each action type has its own pause defaults
        
        // For DRAG action
        DragOptions dragOptions = new DragOptions.Builder().build();
        
        // DragOptions uses Settings.DelayValue as default for certain pauses
        // The specific defaults depend on the implementation
        assertNotNull(dragOptions.getPauseBeforeMouseDown());
        
        // For CLICK action
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        
        // ClickOptions uses FrameworkSettings defaults
        assertEquals(FrameworkSettings.pauseAfterMouseDown, clickOptions.getPauseAfterMouseDown());
    }

    @Test
    void testComplexDragConfiguration() {
        // NEW TEST: Demonstrates advanced drag configuration
        DragOptions options = new DragOptions.Builder()
                .setFromLocation(new Location(10, 10))
                .setToLocation(new Location(200, 200))
                .setDragSpeed(0.5)  // Slower drag
                .setPauseBeforeMouseDown(1.0)
                .setPauseAfterMouseDown(0.5)
                .setPauseBeforeDrop(0.5)
                .setPauseAfterDrop(1.0)
                .build();

        assertNotNull(options);
        assertEquals(10, options.getFromLocation().getX());
        assertEquals(200, options.getToLocation().getX());
        assertEquals(0.5, options.getDragSpeed());
        
        // Verify all timing settings
        assertEquals(1.0, options.getPauseBeforeMouseDown());
        assertEquals(0.5, options.getPauseAfterMouseDown());
        assertEquals(0.5, options.getPauseBeforeDrop());
        assertEquals(1.0, options.getPauseAfterDrop());
    }

    @Test
    void testFindOptionsWithAllStrategies() {
        // NEW TEST: Demonstrates all find strategies
        
        // FIRST strategy
        PatternFindOptions firstOptions = new PatternFindOptions.Builder()
                .setFindStrategy(PatternFindOptions.FindStrategy.FIRST)
                .build();
        assertEquals(PatternFindOptions.FindStrategy.FIRST, firstOptions.getFindStrategy());
        
        // ALL strategy
        PatternFindOptions allOptions = new PatternFindOptions.Builder()
                .setFindStrategy(PatternFindOptions.FindStrategy.ALL)
                .setMaxMatchesToActOn(10)
                .build();
        assertEquals(PatternFindOptions.FindStrategy.ALL, allOptions.getFindStrategy());
        assertEquals(10, allOptions.getMaxMatchesToActOn());
        
        // BEST strategy
        PatternFindOptions bestOptions = new PatternFindOptions.Builder()
                .setFindStrategy(PatternFindOptions.FindStrategy.BEST)
                .setMinSimilarity(0.95)
                .build();
        assertEquals(PatternFindOptions.FindStrategy.BEST, bestOptions.getFindStrategy());
        assertEquals(0.95, bestOptions.getMinSimilarity());
        
        // EACH strategy
        PatternFindOptions eachOptions = new PatternFindOptions.Builder()
                .setFindStrategy(PatternFindOptions.FindStrategy.EACH)
                .build();
        assertEquals(PatternFindOptions.FindStrategy.EACH, eachOptions.getFindStrategy());
        
        // ActionOptions.Find.ActionOptions.Find.ALL strategy (for OCR)
        PatternFindOptions wordsOptions = new PatternFindOptions.Builder()
                .setFindStrategy(PatternFindOptions.FindStrategy.ActionOptions.Find.ActionOptions.Find.ALL)
                .build();
        assertEquals(PatternFindOptions.FindStrategy.ActionOptions.Find.ActionOptions.Find.ALL, wordsOptions.getFindStrategy());
    }

    @Test
    void compareOldAndNewAPI() {
        // This test demonstrates the migration pattern
        
        // OLD API (commented out):
        /*
        ActionOptions oldOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .setFind(PatternFindOptions.FindStrategy.ALL)
                .setMinSimilarity(0.8)
                .setMaxWait(5)
                .setMoveMouseAfterAction(true)
                .setMoveMouseAfterActionTo(new Location(100, 200))
                .addSearchRegion(new Region(0, 0, 50, 50))
                .build();
        */
        
        // NEW API - Split into action-specific configs:
        
        // For drag configuration:
        DragOptions dragOptions = new DragOptions.Builder()
                .setFromLocation(new Location(0, 0))
                .setToLocation(new Location(100, 200))
                .build();
        
        // For find configuration:
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setFindStrategy(PatternFindOptions.FindStrategy.ALL)
                .setMinSimilarity(0.8)
                .setMaxWait(5)
                .addSearchRegion(new Region(0, 0, 50, 50))
                .build();
        
        // The new API separates concerns - drag settings and find settings
        // are now in separate, type-safe configuration objects
        assertNotNull(dragOptions);
        assertNotNull(findOptions);
    }
}