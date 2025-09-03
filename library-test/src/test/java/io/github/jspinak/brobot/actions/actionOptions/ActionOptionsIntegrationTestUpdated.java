package io.github.jspinak.brobot.actions.actionOptions;

import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests demonstrating the ActionConfig API.
 * 
 * Original test intended to verify:
 * 1. DragOptions creation with custom locations and pauses
 * 2. PatternFindOptions with search regions and strategies
 * 3. ClickOptions with mouse movement after action
 * 4. Default pause values from framework settings
 * 5. Complex drag configurations
 * 6. Preset find strategies
 * 7. JSON serialization of options
 * 
 * Rewritten to use actual available APIs:
 * - DragOptions only has mousePressOptions, delayBetweenMouseDownAndMove, and
 * delayAfterDrag
 * - PatternFindOptions has Strategy enum and similarity settings
 * - ClickOptions has numberOfClicks and mousePressOptions
 * - No direct location setters on DragOptions (locations come from
 * ObjectCollection)
 */
@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ActionOptionsIntegrationTestUpdated extends BrobotIntegrationTestBase {

    @BeforeEach
    void setUp() {
        super.setUpBrobotEnvironment();
        FrameworkSettings.mock = true;
    }

    @Test
    @Order(1)
    @DisplayName("Should create DragOptions with delay configuration")
    void testDragOptionsCreation() {
        /*
         * Original test tried to set from/to locations on DragOptions.
         * Actual API: DragOptions configures delays and mouse button.
         * The source and target locations come from the ObjectCollection passed to the
         * action.
         */

        MousePressOptions pressOptions = MousePressOptions.builder()
                .setButton(MouseButton.LEFT)
                .build();

        DragOptions options = new DragOptions.Builder()
                .setMousePressOptions(pressOptions)
                .setDelayBetweenMouseDownAndMove(0.5)
                .setDelayAfterDrag(1.0)
                .build();

        assertNotNull(options, "DragOptions should be created");
        assertNotNull(options.getMousePressOptions());
        assertEquals(MouseButton.LEFT, options.getMousePressOptions().getButton());
        assertEquals(0.5, options.getDelayBetweenMouseDownAndMove(), 0.001);
        assertEquals(1.0, options.getDelayAfterDrag(), 0.001);
    }

    @Test
    @Order(2)
    @DisplayName("Should create PatternFindOptions with search strategy")
    void testFindOptionsWithStrategy() {
        /*
         * Original test tried to use FindStrategy.ALL and add search regions.
         * Actual API: PatternFindOptions has Strategy enum (FIRST, ALL, BEST, EACH)
         * Search regions are set on StateImage objects, not on the options.
         */

        PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.8)
                .build();

        assertNotNull(options);
        assertEquals(PatternFindOptions.Strategy.ALL, options.getStrategy());
        assertEquals(0.8, options.getSimilarity(), 0.001);
    }

    @Test
    @Order(3)
    @DisplayName("Should create ClickOptions with mouse button configuration")
    void testClickOptionsWithMouseButton() {
        /*
         * Original test tried to set click type and mouse movement after action.
         * Actual API: ClickOptions has numberOfClicks and mousePressOptions.
         * Mouse movement is handled separately, not part of ClickOptions.
         */

        MousePressOptions pressOptions = MousePressOptions.builder()
                .setButton(MouseButton.RIGHT)
                .build();

        ClickOptions options = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .setPressOptions(pressOptions)
                .build();

        assertNotNull(options);
        assertEquals(2, options.getNumberOfClicks());
        assertNotNull(options.getMousePressOptions());
        assertEquals(MouseButton.RIGHT, options.getMousePressOptions().getButton());
    }

    @Test
    @Order(4)
    @DisplayName("Should use default values when not specified")
    void testDefaultValues() {
        /*
         * Original test verified framework default pause values.
         * Actual API: Each option type has its own defaults.
         */

        // DragOptions defaults
        DragOptions dragOptions = new DragOptions.Builder().build();
        assertNotNull(dragOptions.getMousePressOptions());
        assertEquals(MouseButton.LEFT, dragOptions.getMousePressOptions().getButton());
        assertEquals(0.5, dragOptions.getDelayBetweenMouseDownAndMove(), 0.001);
        assertEquals(0.5, dragOptions.getDelayAfterDrag(), 0.001);

        // ClickOptions defaults
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        assertEquals(1, clickOptions.getNumberOfClicks());
        assertNotNull(clickOptions.getMousePressOptions());
        assertEquals(MouseButton.LEFT, clickOptions.getMousePressOptions().getButton());

        // PatternFindOptions defaults
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        assertEquals(PatternFindOptions.Strategy.FIRST, findOptions.getStrategy());
        assertEquals(0.7, findOptions.getSimilarity(), 0.001);
    }

    @Test
    @Order(5)
    @DisplayName("Should create complex drag configuration with custom mouse button")
    void testComplexDragConfiguration() {
        /*
         * Original test tried complex drag with multiple locations and pauses.
         * Actual API: Configure mouse button and delays.
         */

        MousePressOptions pressOptions = MousePressOptions.builder()
                .setButton(MouseButton.MIDDLE)
                .setPauseBeforeMouseDown(0.2)
                .setPauseAfterMouseDown(0.3)
                .build();

        DragOptions options = new DragOptions.Builder()
                .setMousePressOptions(pressOptions)
                .setDelayBetweenMouseDownAndMove(1.0)
                .setDelayAfterDrag(2.0)
                .setPauseAfterEnd(0.5)
                .build();

        assertNotNull(options);
        assertEquals(MouseButton.MIDDLE, options.getMousePressOptions().getButton());
        assertEquals(0.2, options.getMousePressOptions().getPauseBeforeMouseDown(), 0.001);
        assertEquals(0.3, options.getMousePressOptions().getPauseAfterMouseDown(), 0.001);
        assertEquals(1.0, options.getDelayBetweenMouseDownAndMove(), 0.001);
        assertEquals(2.0, options.getDelayAfterDrag(), 0.001);
        assertEquals(0.5, options.getPauseAfterEnd(), 0.001);
    }

    @Test
    @Order(6)
    @DisplayName("Should use preset PatternFindOptions strategies")
    void testPresetFindStrategies() {
        /*
         * Original test checked preset strategies.
         * Actual API: PatternFindOptions has factory methods for common configurations.
         */

        // Quick search preset
        PatternFindOptions quickOptions = PatternFindOptions.forQuickSearch();
        assertEquals(PatternFindOptions.Strategy.FIRST, quickOptions.getStrategy());
        assertEquals(0.7, quickOptions.getSimilarity(), 0.001);
        assertEquals(1, quickOptions.getMaxMatchesToActOn());

        // All matches preset
        PatternFindOptions allOptions = PatternFindOptions.forAllMatches();
        assertEquals(PatternFindOptions.Strategy.ALL, allOptions.getStrategy());

        // Custom best match configuration
        PatternFindOptions bestOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.95)
                .build();
        assertEquals(PatternFindOptions.Strategy.BEST, bestOptions.getStrategy());
        assertEquals(0.95, bestOptions.getSimilarity(), 0.001);
    }

    @Test
    @Order(7)
    @DisplayName("Should configure PatternFindOptions with capture settings")
    void testFindOptionsWithCapture() {
        /*
         * Original test tried to configure wait times and regions.
         * Actual API: Configure capture and similarity settings.
         */

        PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.95)
                .setCaptureImage(true)
                .setMaxMatchesToActOn(5)
                .build();

        assertNotNull(options);
        assertEquals(PatternFindOptions.Strategy.BEST, options.getStrategy());
        assertEquals(0.95, options.getSimilarity(), 0.001);
        assertTrue(options.isCaptureImage());
        assertEquals(5, options.getMaxMatchesToActOn());
    }

    @Test
    @Order(8)
    @DisplayName("Should inherit base ActionConfig properties")
    void testActionConfigInheritance() {
        /*
         * All option types extend ActionConfig and inherit common properties.
         */

        DragOptions dragOptions = new DragOptions.Builder()
                .setPauseAfterEnd(1.5)
                .setPauseBeforeBegin(0.5)
                .build();

        // Test inherited properties
        assertEquals(1.5, dragOptions.getPauseAfterEnd(), 0.001);
        assertEquals(0.5, dragOptions.getPauseBeforeBegin(), 0.001);

        ClickOptions clickOptions = new ClickOptions.Builder()
                .setPauseAfterEnd(2.0)
                .build();

        assertEquals(2.0, clickOptions.getPauseAfterEnd(), 0.001);
    }

    @AfterEach
    void tearDown() {
        FrameworkSettings.mock = false;
    }
}