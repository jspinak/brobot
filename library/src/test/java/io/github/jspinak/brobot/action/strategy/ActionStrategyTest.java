package io.github.jspinak.brobot.action.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

@DisplayName("ActionStrategy Tests")
public class ActionStrategyTest extends BrobotTestBase {

    // Custom test implementation of ActionStrategy
    private static class TestActionStrategy implements ActionStrategy {
        private final String name;
        private final boolean canHandleResult;
        private final ActionResult executeResult;

        public TestActionStrategy(
                String name, boolean canHandleResult, ActionResult executeResult) {
            this.name = name;
            this.canHandleResult = canHandleResult;
            this.executeResult = executeResult;
        }

        @Override
        public ActionResult execute(ActionConfig actionConfig, ObjectCollection targets) {
            return executeResult;
        }

        @Override
        public boolean canHandle(ActionConfig actionConfig) {
            return canHandleResult;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    // Mock implementation for testing
    private static class MockActionStrategy implements ActionStrategy {
        private int executeCallCount = 0;
        private int canHandleCallCount = 0;

        @Override
        public ActionResult execute(ActionConfig actionConfig, ObjectCollection targets) {
            executeCallCount++;
            ActionResult result = new ActionResult();
            result.setSuccess(true);
            return result;
        }

        @Override
        public boolean canHandle(ActionConfig actionConfig) {
            canHandleCallCount++;
            return actionConfig != null;
        }

        @Override
        public String getName() {
            return "MockStrategy";
        }

        public int getExecuteCallCount() {
            return executeCallCount;
        }

        public int getCanHandleCallCount() {
            return canHandleCallCount;
        }
    }

    private ActionStrategy actionStrategy;
    private MockActionStrategy mockStrategy;

    @Mock private ActionResult mockActionResult;

    @Mock private StateImage mockStateImage;

    @Mock private Region mockRegion;

    @Mock private Location mockLocation;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        when(mockActionResult.isSuccess()).thenReturn(true);
        when(mockStateImage.getName()).thenReturn("TestImage");

        mockStrategy = new MockActionStrategy();
    }

    @Nested
    @DisplayName("Strategy Execution")
    class StrategyExecution {

        @Test
        @DisplayName("Should execute action with ClickOptions")
        public void testExecuteWithClickOptions() {
            actionStrategy = new TestActionStrategy("ClickStrategy", true, mockActionResult);

            ClickOptions clickOptions = new ClickOptions.Builder().build();
            ObjectCollection targets =
                    new ObjectCollection.Builder().withImages(mockStateImage).build();

            ActionResult result = actionStrategy.execute(clickOptions, targets);

            assertNotNull(result);
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("Should execute action with PatternFindOptions")
        public void testExecuteWithFindOptions() {
            actionStrategy = new TestActionStrategy("FindStrategy", true, mockActionResult);

            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            ObjectCollection targets =
                    new ObjectCollection.Builder().withImages(mockStateImage).build();

            ActionResult result = actionStrategy.execute(findOptions, targets);

            assertNotNull(result);
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("Should execute action with TypeOptions")
        public void testExecuteWithTypeOptions() {
            actionStrategy = new TestActionStrategy("TypeStrategy", true, mockActionResult);

            TypeOptions typeOptions = new TypeOptions.Builder().build();
            ObjectCollection targets =
                    new ObjectCollection.Builder().withRegions(mockRegion).build();

            ActionResult result = actionStrategy.execute(typeOptions, targets);

            assertNotNull(result);
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("Should handle null action config")
        public void testExecuteWithNullConfig() {
            ObjectCollection targets =
                    new ObjectCollection.Builder().withImages(mockStateImage).build();

            ActionResult result = mockStrategy.execute(null, targets);

            assertNotNull(result);
            assertEquals(1, mockStrategy.getExecuteCallCount());
        }

        @Test
        @DisplayName("Should handle null targets")
        public void testExecuteWithNullTargets() {
            ClickOptions clickOptions = new ClickOptions.Builder().build();

            ActionResult result = mockStrategy.execute(clickOptions, null);

            assertNotNull(result);
            assertEquals(1, mockStrategy.getExecuteCallCount());
        }

        @Test
        @DisplayName("Should handle empty targets")
        public void testExecuteWithEmptyTargets() {
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            ObjectCollection emptyTargets = new ObjectCollection.Builder().build();

            ActionResult result = mockStrategy.execute(clickOptions, emptyTargets);

            assertNotNull(result);
            assertEquals(1, mockStrategy.getExecuteCallCount());
        }
    }

    @Nested
    @DisplayName("Strategy Handling Check")
    class StrategyHandlingCheck {

        @Test
        @DisplayName("Should handle supported action config")
        public void testCanHandleSupportedConfig() {
            actionStrategy = new TestActionStrategy("TestStrategy", true, mockActionResult);

            ClickOptions clickOptions = new ClickOptions.Builder().build();

            assertTrue(actionStrategy.canHandle(clickOptions));
        }

        @Test
        @DisplayName("Should not handle unsupported action config")
        public void testCannotHandleUnsupportedConfig() {
            actionStrategy = new TestActionStrategy("TestStrategy", false, mockActionResult);

            ClickOptions clickOptions = new ClickOptions.Builder().build();

            assertFalse(actionStrategy.canHandle(clickOptions));
        }

        @Test
        @DisplayName("Should handle null config appropriately")
        public void testCanHandleNullConfig() {
            assertFalse(mockStrategy.canHandle(null));
            assertEquals(1, mockStrategy.getCanHandleCallCount());
        }

        @ParameterizedTest
        @ValueSource(strings = {"ClickOptions", "FindOptions", "TypeOptions", "DragOptions"})
        @DisplayName("Should check handling for various config types")
        public void testCanHandleVariousConfigs(String configType) {
            ActionConfig config =
                    switch (configType) {
                        case "ClickOptions" -> new ClickOptions.Builder().build();
                        case "FindOptions" -> new PatternFindOptions.Builder().build();
                        case "TypeOptions" -> new TypeOptions.Builder().build();
                        default -> new ClickOptions.Builder().build();
                    };

            boolean canHandle = mockStrategy.canHandle(config);

            assertTrue(canHandle);
            assertEquals(1, mockStrategy.getCanHandleCallCount());
        }
    }

    @Nested
    @DisplayName("Strategy Naming")
    class StrategyNaming {

        @Test
        @DisplayName("Should return correct strategy name")
        public void testGetName() {
            actionStrategy = new TestActionStrategy("CustomStrategy", true, mockActionResult);

            assertEquals("CustomStrategy", actionStrategy.getName());
        }

        @Test
        @DisplayName("Should handle empty name")
        public void testEmptyName() {
            actionStrategy = new TestActionStrategy("", true, mockActionResult);

            assertEquals("", actionStrategy.getName());
        }

        @Test
        @DisplayName("Should handle null name")
        public void testNullName() {
            actionStrategy = new TestActionStrategy(null, true, mockActionResult);

            assertNull(actionStrategy.getName());
        }

        @ParameterizedTest
        @ValueSource(strings = {"MockStrategy", "LiveStrategy", "TestStrategy", "DebugStrategy"})
        @DisplayName("Should support various strategy names")
        public void testVariousStrategyNames(String name) {
            actionStrategy = new TestActionStrategy(name, true, mockActionResult);

            assertEquals(name, actionStrategy.getName());
        }
    }

    @Nested
    @DisplayName("Strategy Chain of Responsibility")
    class StrategyChainOfResponsibility {

        @Test
        @DisplayName("Should find first capable strategy")
        public void testFindFirstCapableStrategy() {
            ActionStrategy strategy1 = new TestActionStrategy("Strategy1", false, null);
            ActionStrategy strategy2 = new TestActionStrategy("Strategy2", true, mockActionResult);
            ActionStrategy strategy3 = new TestActionStrategy("Strategy3", true, null);

            ClickOptions config = new ClickOptions.Builder().build();

            // Simulate chain of responsibility pattern
            ActionStrategy capableStrategy = null;
            for (ActionStrategy strategy : new ActionStrategy[] {strategy1, strategy2, strategy3}) {
                if (strategy.canHandle(config)) {
                    capableStrategy = strategy;
                    break;
                }
            }

            assertNotNull(capableStrategy);
            assertEquals("Strategy2", capableStrategy.getName());
        }

        @Test
        @DisplayName("Should handle no capable strategy")
        public void testNoCapableStrategy() {
            ActionStrategy strategy1 = new TestActionStrategy("Strategy1", false, null);
            ActionStrategy strategy2 = new TestActionStrategy("Strategy2", false, null);

            ClickOptions config = new ClickOptions.Builder().build();

            ActionStrategy capableStrategy = null;
            for (ActionStrategy strategy : new ActionStrategy[] {strategy1, strategy2}) {
                if (strategy.canHandle(config)) {
                    capableStrategy = strategy;
                    break;
                }
            }

            assertNull(capableStrategy);
        }
    }

    @Nested
    @DisplayName("Strategy Result Handling")
    class StrategyResultHandling {

        @Test
        @DisplayName("Should return successful result")
        public void testSuccessfulResult() {
            ActionResult successResult = new ActionResult();
            successResult.setSuccess(true);

            actionStrategy = new TestActionStrategy("SuccessStrategy", true, successResult);

            ActionResult result =
                    actionStrategy.execute(
                            new ClickOptions.Builder().build(),
                            new ObjectCollection.Builder().build());

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("Should return failed result")
        public void testFailedResult() {
            ActionResult failureResult = new ActionResult();
            failureResult.setSuccess(false);

            actionStrategy = new TestActionStrategy("FailureStrategy", true, failureResult);

            ActionResult result =
                    actionStrategy.execute(
                            new ClickOptions.Builder().build(),
                            new ObjectCollection.Builder().build());

            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("Should handle null result")
        public void testNullResult() {
            actionStrategy = new TestActionStrategy("NullStrategy", true, null);

            ActionResult result =
                    actionStrategy.execute(
                            new ClickOptions.Builder().build(),
                            new ObjectCollection.Builder().build());

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Strategy Performance")
    class StrategyPerformance {

        @Test
        @DisplayName("Should track execution count")
        public void testExecutionCount() {
            ClickOptions config = new ClickOptions.Builder().build();
            ObjectCollection targets = new ObjectCollection.Builder().build();

            for (int i = 0; i < 5; i++) {
                mockStrategy.execute(config, targets);
            }

            assertEquals(5, mockStrategy.getExecuteCallCount());
        }

        @Test
        @DisplayName("Should track canHandle check count")
        public void testCanHandleCheckCount() {
            ClickOptions config = new ClickOptions.Builder().build();

            for (int i = 0; i < 10; i++) {
                mockStrategy.canHandle(config);
            }

            assertEquals(10, mockStrategy.getCanHandleCallCount());
        }

        @Test
        @DisplayName("Should handle rapid successive calls")
        public void testRapidSuccessiveCalls() {
            ClickOptions config = new ClickOptions.Builder().build();
            ObjectCollection targets = new ObjectCollection.Builder().build();

            // Simulate rapid calls
            for (int i = 0; i < 100; i++) {
                ActionResult result = mockStrategy.execute(config, targets);
                assertNotNull(result);
            }

            assertEquals(100, mockStrategy.getExecuteCallCount());
        }
    }
}
