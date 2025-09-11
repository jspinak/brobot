package io.github.jspinak.brobot.action.internal.execution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.action.internal.utility.ActionSuccessCriteria;
import io.github.jspinak.brobot.control.ExecutionController;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.ExecutionSession;
import io.github.jspinak.brobot.tools.ml.dataset.DatasetManager;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import io.github.jspinak.brobot.util.image.capture.ScreenshotCapture;

/**
 * Test suite for ActionExecution class. Tests the execution flow of actions including pre/post
 * conditions and lifecycle.
 */
@DisplayName("ActionExecution Tests")
public class ActionExecutionTest extends BrobotTestBase {

    @Mock private TimeProvider timeProvider;

    @Mock private IllustrationController illustrationController;

    @Mock private SearchRegionResolver searchRegionResolver;

    @Mock private ActionLifecycleManagement actionLifecycleManagement;

    @Mock private DatasetManager datasetManager;

    @Mock private ActionSuccessCriteria actionSuccessCriteria;

    @Mock private ActionResultFactory actionResultFactory;

    @Mock private ActionLogger actionLogger;

    @Mock private ScreenshotCapture screenshotCapture;

    @Mock private ExecutionSession executionSession;

    @Mock private ExecutionController executionController;

    @Mock private BrobotLogger brobotLogger;

    @Mock private StateMemory stateMemory;

    @Mock private ActionInterface actionInterface;

    @Mock private ActionConfig actionConfig;

    @Mock private ObjectCollection objectCollection;

    private ActionExecution actionExecution;
    private AutoCloseable mockCloseable;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockCloseable = MockitoAnnotations.openMocks(this);
        actionExecution =
                new ActionExecution(
                        timeProvider,
                        illustrationController,
                        searchRegionResolver,
                        actionLifecycleManagement,
                        datasetManager,
                        actionSuccessCriteria,
                        actionResultFactory,
                        actionLogger,
                        screenshotCapture,
                        executionSession,
                        executionController,
                        brobotLogger,
                        stateMemory);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }

    @Nested
    @DisplayName("Basic Execution")
    class BasicExecution {

        @Test
        @DisplayName("Should execute action successfully")
        void shouldExecuteActionSuccessfully() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            expectedResult.setSuccess(true);
            when(actionResultFactory.init(
                            any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                    .thenReturn(expectedResult);
            when(actionLifecycleManagement.isMoreSequencesAllowed(any(ActionResult.class)))
                    .thenReturn(true, false);
            doNothing()
                    .when(actionSuccessCriteria)
                    .set(any(ActionConfig.class), any(ActionResult.class));

            // Act
            ActionResult result =
                    actionExecution.perform(
                            actionInterface, "test action", actionConfig, objectCollection);

            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(actionSuccessCriteria).set(any(ActionConfig.class), any(ActionResult.class));
        }

        @Test
        @DisplayName("Should handle action failure")
        void shouldHandleActionFailure() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            expectedResult.setSuccess(false);
            when(actionResultFactory.init(
                            any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                    .thenReturn(expectedResult);
            when(actionLifecycleManagement.isMoreSequencesAllowed(any(ActionResult.class)))
                    .thenReturn(false);
            doNothing()
                    .when(actionSuccessCriteria)
                    .set(any(ActionConfig.class), any(ActionResult.class));

            // Act
            ActionResult result =
                    actionExecution.perform(
                            actionInterface, "test action", actionConfig, objectCollection);

            // Assert
            assertNotNull(result);
            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("Should capture execution duration")
        void shouldCaptureExecutionDuration() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            expectedResult.setDuration(Duration.ofMillis(100));
            when(actionResultFactory.init(
                            any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                    .thenReturn(expectedResult);
            when(actionLifecycleManagement.isMoreSequencesAllowed(any(ActionResult.class)))
                    .thenReturn(false);
            when(actionLifecycleManagement.getCurrentDuration(any(ActionResult.class)))
                    .thenReturn(Duration.ofMillis(100));

            // Act
            ActionResult result =
                    actionExecution.perform(
                            actionInterface, "test action", actionConfig, objectCollection);

            // Assert
            assertNotNull(result.getDuration());
            assertTrue(result.getDuration().toMillis() >= 0);
        }
    }

    @Nested
    @DisplayName("Match Processing")
    class MatchProcessing {

        @Test
        @DisplayName("Should process single match")
        void shouldProcessSingleMatch() {
            // Arrange
            Match match =
                    new Match.Builder()
                            .setRegion(new Region(10, 10, 100, 100))
                            .setSimScore(0.95)
                            .build();

            ActionResult expectedResult = new ActionResult();
            expectedResult.add(match);
            expectedResult.setSuccess(true);

            when(actionResultFactory.init(
                            any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                    .thenReturn(expectedResult);
            when(actionLifecycleManagement.isMoreSequencesAllowed(any(ActionResult.class)))
                    .thenReturn(false);
            doNothing()
                    .when(actionSuccessCriteria)
                    .set(any(ActionConfig.class), any(ActionResult.class));

            // Act
            ActionResult result =
                    actionExecution.perform(
                            actionInterface, "test action", actionConfig, objectCollection);

            // Assert
            assertEquals(1, result.size());
            assertEquals(match, result.getMatchList().get(0));
        }

        @Test
        @DisplayName("Should process multiple matches")
        void shouldProcessMultipleMatches() {
            // Arrange
            List<Match> matches =
                    Arrays.asList(
                            new Match.Builder()
                                    .setRegion(new Region(10, 10, 50, 50))
                                    .setSimScore(0.95)
                                    .build(),
                            new Match.Builder()
                                    .setRegion(new Region(100, 100, 50, 50))
                                    .setSimScore(0.90)
                                    .build());

            ActionResult expectedResult = new ActionResult();
            matches.forEach(expectedResult::add);
            expectedResult.setSuccess(true);

            when(actionResultFactory.init(
                            any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                    .thenReturn(expectedResult);
            when(actionLifecycleManagement.isMoreSequencesAllowed(any(ActionResult.class)))
                    .thenReturn(false);
            doNothing()
                    .when(actionSuccessCriteria)
                    .set(any(ActionConfig.class), any(ActionResult.class));

            // Act
            ActionResult result =
                    actionExecution.perform(
                            actionInterface, "test action", actionConfig, objectCollection);

            // Assert
            assertEquals(2, result.size());
            assertEquals(matches, result.getMatchList());
        }

        @Test
        @DisplayName("Should handle empty matches")
        void shouldHandleEmptyMatches() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            expectedResult.setSuccess(false);

            when(actionResultFactory.init(
                            any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                    .thenReturn(expectedResult);
            when(actionLifecycleManagement.isMoreSequencesAllowed(any(ActionResult.class)))
                    .thenReturn(false);
            doNothing()
                    .when(actionSuccessCriteria)
                    .set(any(ActionConfig.class), any(ActionResult.class));

            // Act
            ActionResult result =
                    actionExecution.perform(
                            actionInterface, "test action", actionConfig, objectCollection);

            // Assert
            assertTrue(result.isEmpty());
            assertFalse(result.isSuccess());
        }
    }

    @Nested
    @DisplayName("Configuration Handling")
    class ConfigurationHandling {

        @Test
        @DisplayName("Should handle PatternFindOptions configuration")
        void shouldHandlePatternFindOptions() {
            // Arrange
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setSimilarity(0.8).build();

            ActionResult expectedResult = new ActionResult();
            expectedResult.setSuccess(true);

            when(actionResultFactory.init(
                            eq(findOptions), anyString(), any(ObjectCollection[].class)))
                    .thenReturn(expectedResult);
            when(actionLifecycleManagement.isMoreSequencesAllowed(any(ActionResult.class)))
                    .thenReturn(false);
            doNothing()
                    .when(actionSuccessCriteria)
                    .set(any(ActionConfig.class), any(ActionResult.class));

            // Act
            ActionResult result =
                    actionExecution.perform(
                            actionInterface, "find action", findOptions, objectCollection);

            // Assert
            assertNotNull(result);
            verify(actionResultFactory)
                    .init(eq(findOptions), anyString(), any(ObjectCollection[].class));
        }

        @ParameterizedTest
        @DisplayName("Should handle various pause durations")
        @ValueSource(longs = {0, 100, 500, 1000})
        void shouldHandleVariousPauseDurations(long pauseMillis) {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            expectedResult.setSuccess(true);

            when(actionConfig.getPauseBeforeBegin()).thenReturn(pauseMillis / 1000.0);
            when(actionConfig.getPauseAfterEnd()).thenReturn(pauseMillis / 1000.0);
            when(actionResultFactory.init(
                            any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                    .thenReturn(expectedResult);
            when(actionLifecycleManagement.isMoreSequencesAllowed(any(ActionResult.class)))
                    .thenReturn(false);
            doNothing()
                    .when(actionSuccessCriteria)
                    .set(any(ActionConfig.class), any(ActionResult.class));

            // Act
            ActionResult result =
                    actionExecution.perform(
                            actionInterface, "test action", actionConfig, objectCollection);

            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle null action interface")
        void shouldHandleNullActionInterface() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            expectedResult.setSuccess(false);

            when(actionResultFactory.init(
                            any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                    .thenReturn(expectedResult);

            // Act & Assert
            assertDoesNotThrow(
                    () -> actionExecution.perform(null, "test", actionConfig, objectCollection));
        }

        @Test
        @DisplayName("Should handle null object collection")
        void shouldHandleNullObjectCollection() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            expectedResult.setSuccess(false);

            when(actionResultFactory.init(
                            any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                    .thenReturn(expectedResult);
            when(actionLifecycleManagement.isMoreSequencesAllowed(any(ActionResult.class)))
                    .thenReturn(false);

            // Act
            ActionResult result =
                    actionExecution.perform(
                            actionInterface, "test action", actionConfig, (ObjectCollection) null);

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should propagate exception during execution")
        void shouldPropagateExceptionDuringExecution() {
            // Arrange
            when(actionLifecycleManagement.isMoreSequencesAllowed(any(ActionResult.class)))
                    .thenThrow(new RuntimeException("Test exception"));

            ActionResult expectedResult = new ActionResult();
            expectedResult.setSuccess(false);
            when(actionResultFactory.init(
                            any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                    .thenReturn(expectedResult);

            // Act & Assert - expect the exception to be thrown
            RuntimeException exception =
                    assertThrows(
                            RuntimeException.class,
                            () -> {
                                actionExecution.perform(
                                        actionInterface,
                                        "test action",
                                        actionConfig,
                                        objectCollection);
                            });

            assertEquals("Test exception", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("State Management")
    class StateManagement {

        @Test
        @DisplayName("Should handle state image processing")
        void shouldHandleStateImageProcessing() {
            // Arrange
            StateImage stateImage = new StateImage.Builder().setName("test-state").build();

            ObjectCollection collection = new ObjectCollection();
            collection.getStateImages().add(stateImage);

            ActionResult expectedResult = new ActionResult();
            expectedResult.setSuccess(true);

            when(actionResultFactory.init(
                            any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                    .thenReturn(expectedResult);
            when(actionLifecycleManagement.isMoreSequencesAllowed(any(ActionResult.class)))
                    .thenReturn(false);
            doNothing()
                    .when(actionSuccessCriteria)
                    .set(any(ActionConfig.class), any(ActionResult.class));

            // Act
            ActionResult result =
                    actionExecution.perform(
                            actionInterface, "state action", actionConfig, collection);

            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("Should handle multiple object collections")
        void shouldHandleMultipleObjectCollections() {
            // Arrange
            ObjectCollection collection1 = new ObjectCollection();
            ObjectCollection collection2 = new ObjectCollection();

            ActionResult expectedResult = new ActionResult();
            expectedResult.setSuccess(true);

            when(actionResultFactory.init(
                            any(ActionConfig.class), anyString(), any(ObjectCollection[].class)))
                    .thenReturn(expectedResult);
            when(actionLifecycleManagement.isMoreSequencesAllowed(any(ActionResult.class)))
                    .thenReturn(false);
            doNothing()
                    .when(actionSuccessCriteria)
                    .set(any(ActionConfig.class), any(ActionResult.class));

            // Act
            ActionResult result =
                    actionExecution.perform(
                            actionInterface,
                            "multi-collection action",
                            actionConfig,
                            collection1,
                            collection2);

            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
        }
    }
}
