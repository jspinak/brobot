package io.github.jspinak.brobot.tools.history;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for IllustrationController class.
 * Verifies illustration filtering logic and permission management.
 */
public class IllustrationControllerTest extends BrobotTestBase {

    @Mock
    private ImageFileUtilities mockImageUtils;

    @Mock
    private ActionVisualizer mockActionVisualizer;

    @Mock
    private VisualizationOrchestrator mockVisualizationOrchestrator;

    @Mock
    private LoggingVerbosityConfig mockLoggingConfig;

    private IllustrationController illustrationController;

    // Store original settings to restore after tests
    private boolean originalSaveHistory;
    private boolean originalDrawFind;
    private boolean originalDrawClick;
    private boolean originalDrawDrag;
    private boolean originalDrawRepeatedActions;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        // Save original settings
        originalSaveHistory = FrameworkSettings.saveHistory;
        originalDrawFind = FrameworkSettings.drawFind;
        originalDrawClick = FrameworkSettings.drawClick;
        originalDrawDrag = FrameworkSettings.drawDrag;
        originalDrawRepeatedActions = FrameworkSettings.drawRepeatedActions;

        // Setup test settings
        FrameworkSettings.saveHistory = true;
        FrameworkSettings.drawFind = true;
        FrameworkSettings.drawClick = true;
        FrameworkSettings.drawDrag = true;
        FrameworkSettings.drawRepeatedActions = false;

        illustrationController = new IllustrationController(
                mockImageUtils, mockActionVisualizer, mockVisualizationOrchestrator);
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        // Restore original settings
        FrameworkSettings.saveHistory = originalSaveHistory;
        FrameworkSettings.drawFind = originalDrawFind;
        FrameworkSettings.drawClick = originalDrawClick;
        FrameworkSettings.drawDrag = originalDrawDrag;
        FrameworkSettings.drawRepeatedActions = originalDrawRepeatedActions;
    }

    @Nested
    @DisplayName("Permission Check Tests")
    class PermissionCheckTests {

        @Test
        @DisplayName("Should allow illustration when saveHistory is true")
        public void testAllowedWithSaveHistory() {
            FrameworkSettings.saveHistory = true;
            PatternFindOptions config = new PatternFindOptions.Builder().build();
            ObjectCollection collection = new ObjectCollection.Builder().build();

            boolean allowed = illustrationController.okToIllustrate(config, collection);

            assertTrue(allowed);
        }

        @Test
        @DisplayName("Should deny illustration when saveHistory is false and not explicit YES")
        public void testDeniedWithoutSaveHistory() {
            FrameworkSettings.saveHistory = false;
            PatternFindOptions config = new PatternFindOptions.Builder().build();
            ObjectCollection collection = new ObjectCollection.Builder().build();

            boolean allowed = illustrationController.okToIllustrate(config, collection);

            assertFalse(allowed);
        }

        @Test
        @DisplayName("Should allow illustration with explicit YES even without saveHistory")
        public void testAllowedWithExplicitYes() {
            FrameworkSettings.saveHistory = false;
            PatternFindOptions config = new PatternFindOptions.Builder()
                    .setIllustrate(ActionConfig.Illustrate.YES)
                    .build();
            ObjectCollection collection = new ObjectCollection.Builder().build();

            boolean allowed = illustrationController.okToIllustrate(config, collection);

            assertTrue(allowed);
        }

        @Test
        @DisplayName("Should deny illustration with explicit NO")
        public void testDeniedWithExplicitNo() {
            FrameworkSettings.saveHistory = true;
            PatternFindOptions config = new PatternFindOptions.Builder()
                    .setIllustrate(ActionConfig.Illustrate.NO)
                    .build();
            ObjectCollection collection = new ObjectCollection.Builder().build();

            boolean allowed = illustrationController.okToIllustrate(config, collection);

            assertFalse(allowed);
        }
    }

    @Nested
    @DisplayName("Action Type Permission Tests")
    class ActionTypePermissionTests {

        @Test
        @DisplayName("Should respect FIND action permission")
        public void testFindPermission() {
            FrameworkSettings.drawFind = false;
            PatternFindOptions config = new PatternFindOptions.Builder().build();
            ObjectCollection collection = new ObjectCollection.Builder().build();

            boolean allowed = illustrationController.okToIllustrate(config, collection);

            assertFalse(allowed);
        }

        @Test
        @DisplayName("Should respect CLICK action permission")
        public void testClickPermission() {
            FrameworkSettings.drawClick = false;
            ClickOptions config = new ClickOptions.Builder().build();
            ObjectCollection collection = new ObjectCollection.Builder().build();

            boolean allowed = illustrationController.okToIllustrate(config, collection);

            assertFalse(allowed);
        }

        @Test
        @DisplayName("Should respect DRAG action permission")
        public void testDragPermission() {
            FrameworkSettings.drawDrag = false;
            DragOptions config = new DragOptions.Builder().build();
            ObjectCollection collection = new ObjectCollection.Builder().build();

            boolean allowed = illustrationController.okToIllustrate(config, collection);

            assertFalse(allowed);
        }
    }

    @Nested
    @DisplayName("Repetition Detection Tests")
    class RepetitionDetectionTests {

        @Test
        @DisplayName("Should detect repeated actions with same collections")
        public void testDetectRepeatedActions() {
            PatternFindOptions config = new PatternFindOptions.Builder().build();
            ObjectCollection collection = new ObjectCollection.Builder().build();

            // First call should be allowed
            boolean firstAllowed = illustrationController.okToIllustrate(config, collection);
            assertTrue(firstAllowed);

            // Update last action state
            ActionResult result = new ActionResult();
            List<Region> regions = Collections.emptyList();
            illustrationController.illustrateWhenAllowed(result, regions, config, collection);

            // Second call with same parameters should be denied
            boolean secondAllowed = illustrationController.okToIllustrate(config, collection);
            assertFalse(secondAllowed);
        }

        @Test
        @DisplayName("Should allow different actions")
        public void testAllowDifferentActions() {
            PatternFindOptions findConfig = new PatternFindOptions.Builder().build();
            ClickOptions clickConfig = new ClickOptions.Builder().build();
            ObjectCollection collection = new ObjectCollection.Builder().build();

            // First FIND action
            ActionResult result = new ActionResult();
            List<Region> regions = Collections.emptyList();
            illustrationController.illustrateWhenAllowed(result, regions, findConfig, collection);

            // Different action type (CLICK) should be allowed
            boolean allowed = illustrationController.okToIllustrate(clickConfig, collection);
            assertTrue(allowed);
        }

        @Test
        @DisplayName("Should allow repeated actions when drawRepeatedActions is true")
        public void testAllowRepeatedWithSetting() {
            FrameworkSettings.drawRepeatedActions = true;
            PatternFindOptions config = new PatternFindOptions.Builder().build();
            ObjectCollection collection = new ObjectCollection.Builder().build();

            // First call
            ActionResult result = new ActionResult();
            List<Region> regions = Collections.emptyList();
            illustrationController.illustrateWhenAllowed(result, regions, config, collection);

            // Second call should still be allowed
            boolean allowed = illustrationController.okToIllustrate(config, collection);
            assertTrue(allowed);
        }
    }

    @Nested
    @DisplayName("Illustration Creation Tests")
    class IllustrationCreationTests {

        @Test
        @DisplayName("Should create illustration when allowed")
        public void testCreateIllustration() {
            PatternFindOptions config = new PatternFindOptions.Builder().build();
            ObjectCollection collection = new ObjectCollection.Builder().build();
            ActionResult result = new ActionResult();
            List<Region> regions = Arrays.asList(new Region(0, 0, 100, 100));

            boolean created = illustrationController.illustrateWhenAllowed(
                    result, regions, config, collection);

            assertTrue(created);
            verify(mockVisualizationOrchestrator).draw(result, regions, config);
        }

        @Test
        @DisplayName("Should not create illustration when denied")
        public void testNoIllustrationWhenDenied() {
            FrameworkSettings.saveHistory = false;
            PatternFindOptions config = new PatternFindOptions.Builder().build();
            ObjectCollection collection = new ObjectCollection.Builder().build();
            ActionResult result = new ActionResult();
            List<Region> regions = Collections.emptyList();

            boolean created = illustrationController.illustrateWhenAllowed(
                    result, regions, config, collection);

            assertFalse(created);
            verify(mockVisualizationOrchestrator, never()).draw(any(), any(), any());
        }

        @Test
        @DisplayName("Should handle illustration creation errors gracefully")
        public void testHandleIllustrationError() {
            PatternFindOptions config = new PatternFindOptions.Builder().build();
            ObjectCollection collection = new ObjectCollection.Builder().build();
            ActionResult result = new ActionResult();
            List<Region> regions = Collections.emptyList();

            // Mock exception during draw
            doThrow(new RuntimeException("Draw error"))
                    .when(mockVisualizationOrchestrator).draw(any(), any(), any());

            // Should not throw, but still return true (attempted to illustrate)
            boolean created = illustrationController.illustrateWhenAllowed(
                    result, regions, config, collection);

            assertTrue(created);
            verify(mockVisualizationOrchestrator).draw(result, regions, config);
        }
    }

    @Nested
    @DisplayName("State Management Tests")
    class StateManagementTests {

        @Test
        @DisplayName("Should update last action state after illustration")
        public void testUpdateLastActionState() {
            PatternFindOptions config = new PatternFindOptions.Builder()
                    .setStrategy(PatternFindOptions.Strategy.ALL)
                    .build();
            ObjectCollection collection = new ObjectCollection.Builder().build();
            ActionResult result = new ActionResult();
            List<Region> regions = Collections.emptyList();

            // Create illustration
            illustrationController.illustrateWhenAllowed(result, regions, config, collection);

            // Verify state was updated (check via repetition detection)
            boolean repeated = illustrationController.okToIllustrate(config, collection);
            assertFalse(repeated, "Should detect repetition after state update");
        }

        @Test
        @DisplayName("Should track different find strategies separately")
        public void testTrackFindStrategies() {
            PatternFindOptions firstConfig = new PatternFindOptions.Builder()
                    .setStrategy(PatternFindOptions.Strategy.FIRST)
                    .build();
            PatternFindOptions allConfig = new PatternFindOptions.Builder()
                    .setStrategy(PatternFindOptions.Strategy.ALL)
                    .build();
            ObjectCollection collection = new ObjectCollection.Builder().build();
            ActionResult result = new ActionResult();
            List<Region> regions = Collections.emptyList();

            // First with FIRST strategy
            illustrationController.illustrateWhenAllowed(result, regions, firstConfig, collection);

            // Different strategy should be allowed
            boolean allowed = illustrationController.okToIllustrate(allConfig, collection);
            assertTrue(allowed);
        }
    }

    @Nested
    @DisplayName("Verbose Logging Tests")
    class VerboseLoggingTests {

        @Test
        @DisplayName("Should check verbose logging configuration")
        public void testVerboseLoggingCheck() {
            // Inject logging config
            illustrationController = new IllustrationController(
                    mockImageUtils, mockActionVisualizer, mockVisualizationOrchestrator);

            // Use reflection to set the logging config
            try {
                java.lang.reflect.Field field = IllustrationController.class.getDeclaredField("loggingConfig");
                field.setAccessible(true);
                field.set(illustrationController, mockLoggingConfig);
            } catch (Exception e) {
                fail("Failed to set logging config: " + e.getMessage());
            }

            when(mockLoggingConfig.getVerbosity())
                    .thenReturn(LoggingVerbosityConfig.VerbosityLevel.VERBOSE);

            PatternFindOptions config = new PatternFindOptions.Builder().build();
            ObjectCollection collection = new ObjectCollection.Builder().build();

            // Should not affect functionality, just add verbose logging
            boolean allowed = illustrationController.okToIllustrate(config, collection);
            assertTrue(allowed);
        }
    }
}