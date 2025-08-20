package io.github.jspinak.brobot.action.internal.utility;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Test helper classes for mocking different option types
class TypeOptions extends ActionConfig {
    public TypeOptions() { 
        super(new TestBuilder());
    }
    static class TestBuilder extends ActionConfig.Builder<TestBuilder> {
        @Override protected TestBuilder self() { return this; }
        public ActionConfig build() { return new TypeOptions(); }
    }
}
class DefineOptions extends ActionConfig {
    public DefineOptions() { 
        super(new TestBuilder());
    }
    static class TestBuilder extends ActionConfig.Builder<TestBuilder> {
        @Override protected TestBuilder self() { return this; }
        public ActionConfig build() { return new DefineOptions(); }
    }
}
class VanishOptions extends ActionConfig {
    public VanishOptions() { 
        super(new TestBuilder());
    }
    static class TestBuilder extends ActionConfig.Builder<TestBuilder> {
        @Override protected TestBuilder self() { return this; }
        public ActionConfig build() { return new VanishOptions(); }
    }
}
class DragOptions extends ActionConfig {
    public DragOptions() { 
        super(new TestBuilder());
    }
    static class TestBuilder extends ActionConfig.Builder<TestBuilder> {
        @Override protected TestBuilder self() { return this; }
        public ActionConfig build() { return new DragOptions(); }
    }
}
class MoveOptions extends ActionConfig {
    public MoveOptions() { 
        super(new TestBuilder());
    }
    static class TestBuilder extends ActionConfig.Builder<TestBuilder> {
        @Override protected TestBuilder self() { return this; }
        public ActionConfig build() { return new MoveOptions(); }
    }
}
class ScrollOptions extends ActionConfig {
    public ScrollOptions() { 
        super(new TestBuilder());
    }
    static class TestBuilder extends ActionConfig.Builder<TestBuilder> {
        @Override protected TestBuilder self() { return this; }
        public ActionConfig build() { return new ScrollOptions(); }
    }
}
class HighlightOptions extends ActionConfig {
    public HighlightOptions() { 
        super(new TestBuilder());
    }
    static class TestBuilder extends ActionConfig.Builder<TestBuilder> {
        @Override protected TestBuilder self() { return this; }
        public ActionConfig build() { return new HighlightOptions(); }
    }
}
class MouseDownOptions extends ActionConfig {
    public MouseDownOptions() { 
        super(new TestBuilder());
    }
    static class TestBuilder extends ActionConfig.Builder<TestBuilder> {
        @Override protected TestBuilder self() { return this; }
        public ActionConfig build() { return new MouseDownOptions(); }
    }
}
class MouseUpOptions extends ActionConfig {
    public MouseUpOptions() { 
        super(new TestBuilder());
    }
    static class TestBuilder extends ActionConfig.Builder<TestBuilder> {
        @Override protected TestBuilder self() { return this; }
        public ActionConfig build() { return new MouseUpOptions(); }
    }
}
class KeyDownOptions extends ActionConfig {
    public KeyDownOptions() { 
        super(new TestBuilder());
    }
    static class TestBuilder extends ActionConfig.Builder<TestBuilder> {
        @Override protected TestBuilder self() { return this; }
        public ActionConfig build() { return new KeyDownOptions(); }
    }
}
class KeyUpOptions extends ActionConfig {
    public KeyUpOptions() { 
        super(new TestBuilder());
    }
    static class TestBuilder extends ActionConfig.Builder<TestBuilder> {
        @Override protected TestBuilder self() { return this; }
        public ActionConfig build() { return new KeyUpOptions(); }
    }
}
class TestUnknownOptions extends ActionConfig {
    public TestUnknownOptions() { 
        super(new TestBuilder());
    }
    static class TestBuilder extends ActionConfig.Builder<TestBuilder> {
        @Override protected TestBuilder self() { return this; }
        public ActionConfig build() { return new TestUnknownOptions(); }
    }
}

/**
 * Comprehensive test suite for ActionSuccessCriteria - evaluates success conditions for actions.
 * Tests criteria for all action types and custom success conditions.
 */
@DisplayName("ActionSuccessCriteria Tests")
public class ActionSuccessCriteriaTest extends BrobotTestBase {
    
    private ActionSuccessCriteria actionSuccessCriteria;
    
    @Mock
    private ActionResult mockActionResult;
    
    @Mock
    private ActionConfig mockActionConfig;
    
    @Mock
    private Match mockMatch;
    
    @Mock
    private Region mockRegion;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        actionSuccessCriteria = new ActionSuccessCriteria();
    }
    
    @Nested
    @DisplayName("Pattern-Based Actions")
    class PatternBasedActions {
        
        @Test
        @DisplayName("FIND succeeds with matches")
        public void testFindSuccessWithMatches() {
            when(mockActionResult.isEmpty()).thenReturn(false);
            
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            Predicate<ActionResult> criteria = actionSuccessCriteria.getCriteria(findOptions);
            
            assertTrue(criteria.test(mockActionResult));
        }
        
        @Test
        @DisplayName("FIND fails without matches")
        public void testFindFailureWithoutMatches() {
            when(mockActionResult.isEmpty()).thenReturn(true);
            
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            Predicate<ActionResult> criteria = actionSuccessCriteria.getCriteria(findOptions);
            
            assertFalse(criteria.test(mockActionResult));
        }
        
        @Test
        @DisplayName("CLICK succeeds with matches")
        public void testClickSuccessWithMatches() {
            when(mockActionResult.isEmpty()).thenReturn(false);
            
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            Predicate<ActionResult> criteria = actionSuccessCriteria.getCriteria(clickOptions);
            
            assertTrue(criteria.test(mockActionResult));
        }
        
        @Test
        @DisplayName("CLICK fails without matches")
        public void testClickFailureWithoutMatches() {
            when(mockActionResult.isEmpty()).thenReturn(true);
            
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            Predicate<ActionResult> criteria = actionSuccessCriteria.getCriteria(clickOptions);
            
            assertFalse(criteria.test(mockActionResult));
        }
        
        @ParameterizedTest
        @EnumSource(value = ActionType.class, names = {"MOVE", "HIGHLIGHT", "CLASSIFY"})
        @DisplayName("Pattern actions succeed with matches")
        public void testPatternActionsSucceed(ActionType actionType) {
            when(mockActionResult.isEmpty()).thenReturn(false);
            
            ActionConfig config;
            switch (actionType) {
                case MOVE:
                    config = new MoveOptions();
                    break;
                case HIGHLIGHT:
                    config = new HighlightOptions();
                    break;
                default:
                    config = new HighlightOptions(); // Default for CLASSIFY or others
            }
            Predicate<ActionResult> criteria = actionSuccessCriteria.getCriteria(config);
            
            assertTrue(criteria.test(mockActionResult));
        }
        
        @ParameterizedTest
        @EnumSource(value = ActionType.class, names = {"MOVE", "HIGHLIGHT", "CLASSIFY"})
        @DisplayName("Pattern actions fail without matches")
        public void testPatternActionsFail(ActionType actionType) {
            when(mockActionResult.isEmpty()).thenReturn(true);
            
            ActionConfig config;
            switch (actionType) {
                case MOVE:
                    config = new MoveOptions();
                    break;
                case HIGHLIGHT:
                    config = new HighlightOptions();
                    break;
                default:
                    config = new HighlightOptions(); // Default for CLASSIFY or others
            }
            Predicate<ActionResult> criteria = actionSuccessCriteria.getCriteria(config);
            
            assertFalse(criteria.test(mockActionResult));
        }
    }
    
    @Nested
    @DisplayName("Always Succeed Actions")
    class AlwaysSucceedActions {
        
        @Test
        @DisplayName("TYPE always succeeds")
        public void testTypeAlwaysSucceeds() {
            TypeOptions typeOptions = new TypeOptions();
            Predicate<ActionResult> criteria = actionSuccessCriteria.getCriteria(typeOptions);
            
            // Test with empty result
            when(mockActionResult.isEmpty()).thenReturn(true);
            assertTrue(criteria.test(mockActionResult));
            
            // Test with matches
            when(mockActionResult.isEmpty()).thenReturn(false);
            assertTrue(criteria.test(mockActionResult));
        }
        
        @ParameterizedTest
        @EnumSource(value = ActionType.class, names = {
            "SCROLL_MOUSE_WHEEL", "MOUSE_DOWN", "MOUSE_UP", "KEY_DOWN", "KEY_UP"
        })
        @DisplayName("Input actions always succeed")
        public void testInputActionsAlwaysSucceed(ActionType actionType) {
            ActionConfig config;
            switch (actionType) {
                case SCROLL_MOUSE_WHEEL:
                    config = new ScrollOptions();
                    break;
                case MOUSE_DOWN:
                    config = new MouseDownOptions();
                    break;
                case MOUSE_UP:
                    config = new MouseUpOptions();
                    break;
                case KEY_DOWN:
                    config = new KeyDownOptions();
                    break;
                case KEY_UP:
                    config = new KeyUpOptions();
                    break;
                default:
                    config = new TypeOptions(); // Default to TypeOptions
            }
            Predicate<ActionResult> criteria = actionSuccessCriteria.getCriteria(config);
            
            // Always returns true regardless of result state
            assertTrue(criteria.test(mockActionResult));
            
            when(mockActionResult.isEmpty()).thenReturn(true);
            assertTrue(criteria.test(mockActionResult));
            
            when(mockActionResult.isEmpty()).thenReturn(false);
            assertTrue(criteria.test(mockActionResult));
        }
    }
    
    @Nested
    @DisplayName("Special Condition Actions")
    class SpecialConditionActions {
        
        @Test
        @DisplayName("DEFINE succeeds with defined region")
        public void testDefineSuccessWithDefinedRegion() {
            when(mockActionResult.getDefinedRegion()).thenReturn(mockRegion);
            when(mockRegion.isDefined()).thenReturn(true);
            
            DefineOptions defineOptions = new DefineOptions();
            Predicate<ActionResult> criteria = actionSuccessCriteria.getCriteria(defineOptions);
            
            assertTrue(criteria.test(mockActionResult));
        }
        
        @Test
        @DisplayName("DEFINE fails without defined region")
        public void testDefineFailureWithoutDefinedRegion() {
            when(mockActionResult.getDefinedRegion()).thenReturn(mockRegion);
            when(mockRegion.isDefined()).thenReturn(false);
            
            DefineOptions defineOptions = new DefineOptions();
            Predicate<ActionResult> criteria = actionSuccessCriteria.getCriteria(defineOptions);
            
            assertFalse(criteria.test(mockActionResult));
        }
        
        @Test
        @DisplayName("VANISH succeeds when no matches found")
        public void testVanishSuccessWithNoMatches() {
            when(mockActionResult.isEmpty()).thenReturn(true);
            
            VanishOptions vanishOptions = new VanishOptions();
            Predicate<ActionResult> criteria = actionSuccessCriteria.getCriteria(vanishOptions);
            
            assertTrue(criteria.test(mockActionResult));
        }
        
        @Test
        @DisplayName("VANISH fails when matches found")
        public void testVanishFailureWithMatches() {
            when(mockActionResult.isEmpty()).thenReturn(false);
            
            VanishOptions vanishOptions = new VanishOptions();
            Predicate<ActionResult> criteria = actionSuccessCriteria.getCriteria(vanishOptions);
            
            assertFalse(criteria.test(mockActionResult));
        }
        
        @Test
        @DisplayName("DRAG succeeds with exactly 2 points")
        public void testDragSuccessWithTwoPoints() {
            when(mockActionResult.size()).thenReturn(2);
            
            DragOptions dragOptions = new DragOptions();
            Predicate<ActionResult> criteria = actionSuccessCriteria.getCriteria(dragOptions);
            
            assertTrue(criteria.test(mockActionResult));
        }
        
        @ParameterizedTest
        @ValueSource(ints = {0, 1, 3, 5, 10})
        @DisplayName("DRAG fails without exactly 2 points")
        public void testDragFailureWithoutTwoPoints(int pointCount) {
            when(mockActionResult.size()).thenReturn(pointCount);
            
            DragOptions dragOptions = new DragOptions();
            Predicate<ActionResult> criteria = actionSuccessCriteria.getCriteria(dragOptions);
            
            assertFalse(criteria.test(mockActionResult));
        }
    }
    
    @Nested
    @DisplayName("Action Config Based Criteria")
    class ActionConfigBasedCriteria {
        
        @Test
        @DisplayName("Custom success criteria from config")
        public void testCustomSuccessCriteria() {
            Predicate<ActionResult> customCriteria = result -> result.size() > 5;
            when(mockActionConfig.getSuccessCriteria()).thenReturn(customCriteria);
            when(mockActionResult.size()).thenReturn(6);
            
            actionSuccessCriteria.set(mockActionConfig, mockActionResult);
            verify(mockActionResult).setSuccess(true);
        }
        
        @Test
        @DisplayName("Default criteria when no custom criteria")
        public void testDefaultCriteriaWhenNoCustom() {
            when(mockActionConfig.getSuccessCriteria()).thenReturn(null);
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            when(mockActionResult.isEmpty()).thenReturn(false);
            
            // Assuming getActionTypeFromConfig returns CLICK for ClickOptions
            actionSuccessCriteria.set(clickOptions, mockActionResult);
            
            verify(mockActionResult).setSuccess(true);
        }
    }
    
    @Nested
    @DisplayName("Evaluate Success Method")
    class EvaluateSuccessMethod {
        
        @Test
        @DisplayName("Sets success to true when criteria met")
        public void testSetsSuccessTrue() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            when(mockActionResult.isEmpty()).thenReturn(false);
            
            actionSuccessCriteria.set(findOptions, mockActionResult);
            
            verify(mockActionResult).setSuccess(true);
        }
        
        @Test
        @DisplayName("Sets success to false when criteria not met")
        public void testSetsSuccessFalse() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            when(mockActionResult.isEmpty()).thenReturn(true);
            
            actionSuccessCriteria.set(findOptions, mockActionResult);
            
            verify(mockActionResult).setSuccess(false);
        }
        
        @Test
        @DisplayName("Custom criteria overrides default")
        public void testCustomCriteriaOverride() {
            // Custom criteria that always returns false
            Predicate<ActionResult> customCriteria = result -> false;
            when(mockActionConfig.getSuccessCriteria()).thenReturn(customCriteria);
            
            actionSuccessCriteria.set(mockActionConfig, mockActionResult);
            
            verify(mockActionResult).setSuccess(false);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Null action config")
        public void testNullActionConfig() {
            // Should handle gracefully
            assertDoesNotThrow(() -> actionSuccessCriteria.set(null, mockActionResult));
        }
        
        @Test
        @DisplayName("Null action result")
        public void testNullActionResult() {
            // Should handle gracefully - no exception thrown
            assertDoesNotThrow(() -> actionSuccessCriteria.set(mockActionConfig, null));
        }
        
        @Test
        @DisplayName("Unknown action type")
        public void testUnknownActionType() {
            // Test with a potentially unregistered action type
            // Use a config that doesn't match any known patterns  
            ActionConfig unknownConfig = new TestUnknownOptions();
            
            Predicate<ActionResult> criteria = actionSuccessCriteria.getCriteria(unknownConfig);
            
            // Should return default or handle gracefully
            assertNotNull(criteria);
            // Test that it returns the default (FIND) criteria behavior
            when(mockActionResult.isEmpty()).thenReturn(false);
            assertTrue(criteria.test(mockActionResult));
        }
    }
    
    @Nested
    @DisplayName("Complex Success Scenarios")
    class ComplexSuccessScenarios {
        
        @Test
        @DisplayName("Chain of criteria evaluation")
        public void testChainOfCriteria() {
            // Test complex criteria that checks multiple conditions
            Predicate<ActionResult> complexCriteria = result -> 
                !result.isEmpty() && result.size() > 2 && result.getDuration().toMillis() < 5000;
            
            when(mockActionConfig.getSuccessCriteria()).thenReturn(complexCriteria);
            when(mockActionResult.isEmpty()).thenReturn(false);
            when(mockActionResult.size()).thenReturn(3);
            when(mockActionResult.getDuration()).thenReturn(Duration.ofMillis(3000));
            
            actionSuccessCriteria.set(mockActionConfig, mockActionResult);
            
            verify(mockActionResult).setSuccess(true);
        }
        
        @Test
        @DisplayName("Partial success conditions")
        public void testPartialSuccessConditions() {
            // Test criteria that considers partial success
            Predicate<ActionResult> partialCriteria = result -> result.size() >= 1;
            
            when(mockActionConfig.getSuccessCriteria()).thenReturn(partialCriteria);
            when(mockActionResult.size()).thenReturn(1);
            
            actionSuccessCriteria.set(mockActionConfig, mockActionResult);
            
            verify(mockActionResult).setSuccess(true);
        }
        
        @Test
        @DisplayName("Time-based success criteria")
        public void testTimeBasedCriteria() {
            // Test criteria based on execution time
            Predicate<ActionResult> timeCriteria = result -> result.getDuration().toMillis() < 2000;
            
            when(mockActionConfig.getSuccessCriteria()).thenReturn(timeCriteria);
            when(mockActionResult.getDuration()).thenReturn(Duration.ofMillis(1500));
            
            actionSuccessCriteria.set(mockActionConfig, mockActionResult);
            
            verify(mockActionResult).setSuccess(true);
        }
    }
}