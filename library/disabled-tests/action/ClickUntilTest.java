package io.github.jspinak.brobot.action.composite.repeat;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.stateObject.StateImage;
import io.github.jspinak.brobot.model.state.stateObject.StateLocation;
import io.github.jspinak.brobot.model.state.stateObject.StateRegion;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ClickUntil Tests (Modern RepeatUntil Pattern)")
public class ClickUntilTest extends BrobotTestBase {

    private ModernRepeatUntilConfig config;
    private ObjectCollection actionCollection;
    private ObjectCollection conditionCollection;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        actionCollection = new ObjectCollection();
        conditionCollection = new ObjectCollection();
    }
    
    @Nested
    @DisplayName("Basic Configuration")
    class BasicConfiguration {
        
        @Test
        @DisplayName("Should create config with builder")
        public void testConfigBuilder() {
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(clickOptions)
                .setActionObjectCollection(actionCollection)
                .setUntilAction(findOptions)
                .setConditionObjectCollection(conditionCollection)
                .setMaxActions(10)
                .build();
            
            assertNotNull(config);
            assertEquals(clickOptions, config.getDoAction());
            assertEquals(actionCollection, config.getActionObjectCollection());
            assertEquals(findOptions, config.getUntilAction());
            assertEquals(conditionCollection, config.getConditionObjectCollection());
            assertEquals(10, config.getMaxActions());
        }
        
        @Test
        @DisplayName("Should have default max actions")
        public void testDefaultMaxActions() {
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(new ClickOptions.Builder().build())
                .setActionObjectCollection(actionCollection)
                .setUntilAction(new PatternFindOptions.Builder().build())
                .setConditionObjectCollection(conditionCollection)
                .build();
            
            assertEquals(3, config.getMaxActions());
        }
        
        @Test
        @DisplayName("Should initialize result fields")
        public void testInitialResultState() {
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(new ClickOptions.Builder().build())
                .setActionObjectCollection(actionCollection)
                .setUntilAction(new PatternFindOptions.Builder().build())
                .setConditionObjectCollection(conditionCollection)
                .build();
            
            assertEquals(0, config.getTotalSuccessfulActions());
            assertNotNull(config.getActionMatches());
            assertNotNull(config.getConditionMatches());
            assertFalse(config.isSuccess());
        }
    }
    
    @Nested
    @DisplayName("Click Until Patterns")
    class ClickUntilPatterns {
        
        @Test
        @DisplayName("Should configure click until object vanishes")
        public void testClickUntilVanish() {
            StateImage closeButton = StateImage.builder().build();
            actionCollection.addStateImage(closeButton);
            
            StateImage dialogBox = StateImage.builder().build();
            conditionCollection.addStateImage(dialogBox);
            
            ClickOptions clickOptions = new ClickOptions.Builder()
                .withClicks(1)
                .withPauseAfterClick(0.5)
                .build();
            
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .withSimilarity(0.8)
                .build();
            
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(clickOptions)
                .setActionObjectCollection(actionCollection)
                .setUntilAction(findOptions)
                .setConditionObjectCollection(conditionCollection)
                .setMaxActions(5)
                .build();
            
            assertEquals(5, config.getMaxActions());
            assertEquals(clickOptions, config.getDoAction());
        }
        
        @Test
        @DisplayName("Should configure click until object appears")
        public void testClickUntilAppears() {
            StateImage nextButton = StateImage.builder().build();
            actionCollection.addStateImage(nextButton);
            
            StateImage finishButton = StateImage.builder().build();
            conditionCollection.addStateImage(finishButton);
            
            ClickOptions clickOptions = new ClickOptions.Builder()
                .withClicks(1)
                .build();
            
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .withSearchTime(2.0)
                .build();
            
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(clickOptions)
                .setActionObjectCollection(actionCollection)
                .setUntilAction(findOptions)
                .setConditionObjectCollection(conditionCollection)
                .setMaxActions(10)
                .build();
            
            assertNotNull(config);
            assertEquals(10, config.getMaxActions());
        }
        
        @Test
        @DisplayName("Should configure click multiple targets until condition")
        public void testClickMultipleTargets() {
            StateImage button1 = StateImage.builder().build();
            StateImage button2 = StateImage.builder().build();
            StateLocation location = new StateLocation.Builder()
                .withLocation(new Location(100, 100))
                .build();
            
            actionCollection.addStateImage(button1);
            actionCollection.addStateImage(button2);
            actionCollection.addStateLocation(location);
            
            StateImage successMessage = StateImage.builder().build();
            conditionCollection.addStateImage(successMessage);
            
            ClickOptions clickOptions = new ClickOptions.Builder()
                .withClickAll(true)
                .build();
            
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .withFind(PatternFindOptions.Find.BEST)
                .build();
            
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(clickOptions)
                .setActionObjectCollection(actionCollection)
                .setUntilAction(findOptions)
                .setConditionObjectCollection(conditionCollection)
                .setMaxActions(15)
                .build();
            
            assertEquals(3, actionCollection.getStateImages().size() + actionCollection.getStateLocations().size());
        }
    }
    
    @Nested
    @DisplayName("Result Management")
    class ResultManagement {
        
        @Test
        @DisplayName("Should track successful actions")
        public void testSuccessfulActionTracking() {
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(new ClickOptions.Builder().build())
                .setActionObjectCollection(actionCollection)
                .setUntilAction(new PatternFindOptions.Builder().build())
                .setConditionObjectCollection(conditionCollection)
                .build();
            
            config.setTotalSuccessfulActions(5);
            assertEquals(5, config.getTotalSuccessfulActions());
            
            config.setTotalSuccessfulActions(8);
            assertEquals(8, config.getTotalSuccessfulActions());
        }
        
        @Test
        @DisplayName("Should store action matches")
        public void testActionMatchesStorage() {
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(new ClickOptions.Builder().build())
                .setActionObjectCollection(actionCollection)
                .setUntilAction(new PatternFindOptions.Builder().build())
                .setConditionObjectCollection(conditionCollection)
                .build();
            
            Match match = new Match.Builder()
                .withRegion(new Region(100, 100, 50, 50))
                .withScore(0.95)
                .build();
            
            ActionResult actionResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(match)
                .build();
            
            config.setActionMatches(actionResult);
            assertEquals(actionResult, config.getActionMatches());
            assertTrue(config.getActionMatches().isSuccess());
        }
        
        @Test
        @DisplayName("Should store condition matches")
        public void testConditionMatchesStorage() {
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(new ClickOptions.Builder().build())
                .setActionObjectCollection(actionCollection)
                .setUntilAction(new PatternFindOptions.Builder().build())
                .setConditionObjectCollection(conditionCollection)
                .build();
            
            Match match = new Match.Builder()
                .withRegion(new Region(200, 200, 60, 60))
                .withScore(0.88)
                .build();
            
            ActionResult conditionResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(match)
                .build();
            
            config.setConditionMatches(conditionResult);
            assertEquals(conditionResult, config.getConditionMatches());
            assertTrue(config.getConditionMatches().isSuccess());
        }
        
        @Test
        @DisplayName("Should track overall success")
        public void testOverallSuccess() {
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(new ClickOptions.Builder().build())
                .setActionObjectCollection(actionCollection)
                .setUntilAction(new PatternFindOptions.Builder().build())
                .setConditionObjectCollection(conditionCollection)
                .build();
            
            assertFalse(config.isSuccess());
            
            config.setSuccess(true);
            assertTrue(config.isSuccess());
        }
    }
    
    @Nested
    @DisplayName("Reset Functionality")
    class ResetFunctionality {
        
        @Test
        @DisplayName("Should reset times acted on")
        public void testResetTimesActedOn() {
            StateImage image1 = StateImage.builder().build();
            StateImage image2 = StateImage.builder().build();
            actionCollection.addStateImage(image1);
            actionCollection.addStateImage(image2);
            
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(new ClickOptions.Builder().build())
                .setActionObjectCollection(actionCollection)
                .setUntilAction(new PatternFindOptions.Builder().build())
                .setConditionObjectCollection(conditionCollection)
                .build();
            
            // Simulate acting on objects
            image1.setTimesActedOn(3);
            image2.setTimesActedOn(2);
            
            config.resetTimesActedOn();
            
            assertEquals(0, image1.getTimesActedOn());
            assertEquals(0, image2.getTimesActedOn());
        }
        
        @Test
        @DisplayName("Should only reset action collection, not condition")
        public void testSelectiveReset() {
            StateImage actionImage = StateImage.builder().build();
            StateImage conditionImage = StateImage.builder().build();
            actionCollection.addStateImage(actionImage);
            conditionCollection.addStateImage(conditionImage);
            
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(new ClickOptions.Builder().build())
                .setActionObjectCollection(actionCollection)
                .setUntilAction(new PatternFindOptions.Builder().build())
                .setConditionObjectCollection(conditionCollection)
                .build();
            
            actionImage.setTimesActedOn(5);
            conditionImage.setTimesActedOn(3);
            
            config.resetTimesActedOn();
            
            assertEquals(0, actionImage.getTimesActedOn());
            assertEquals(3, conditionImage.getTimesActedOn());
        }
    }
    
    @Nested
    @DisplayName("Common Use Cases")
    class CommonUseCases {
        
        @Test
        @DisplayName("Should configure dismiss popups pattern")
        public void testDismissPopupsPattern() {
            StateImage closeButton = StateImage.builder().build();
            StateImage popupWindow = StateImage.builder().build();
            
            actionCollection.addStateImage(closeButton);
            conditionCollection.addStateImage(popupWindow);
            
            ClickOptions dismissClick = new ClickOptions.Builder()
                .withClicks(1)
                .withPauseAfterClick(0.2)
                .build();
            
            PatternFindOptions checkPopup = new PatternFindOptions.Builder()
                .withSearchTime(0.5)
                .build();
            
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(dismissClick)
                .setActionObjectCollection(actionCollection)
                .setUntilAction(checkPopup)
                .setConditionObjectCollection(conditionCollection)
                .setMaxActions(5)
                .build();
            
            assertEquals(5, config.getMaxActions());
            assertEquals(0.2, ((ClickOptions)config.getDoAction()).getPauseAfterClick());
        }
        
        @Test
        @DisplayName("Should configure wizard navigation pattern")
        public void testWizardNavigationPattern() {
            StateImage nextButton = StateImage.builder().build();
            StateImage finishButton = StateImage.builder().build();
            
            actionCollection.addStateImage(nextButton);
            conditionCollection.addStateImage(finishButton);
            
            ClickOptions clickNext = new ClickOptions.Builder()
                .withClicks(1)
                .withPauseAfterClick(1.0)
                .build();
            
            PatternFindOptions findFinish = new PatternFindOptions.Builder()
                .withSimilarity(0.9)
                .withSearchTime(2.0)
                .build();
            
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(clickNext)
                .setActionObjectCollection(actionCollection)
                .setUntilAction(findFinish)
                .setConditionObjectCollection(conditionCollection)
                .setMaxActions(20)
                .build();
            
            assertEquals(20, config.getMaxActions());
            assertEquals(1.0, ((ClickOptions)config.getDoAction()).getPauseAfterClick());
            assertEquals(0.9, ((PatternFindOptions)config.getUntilAction()).getSimilarity());
        }
        
        @Test
        @DisplayName("Should configure clear list items pattern")
        public void testClearListItemsPattern() {
            StateImage deleteIcon = StateImage.builder().build();
            StateRegion listArea = new StateRegion.Builder()
                .withRegion(new Region(100, 100, 400, 300))
                .build();
            
            actionCollection.addStateImage(deleteIcon);
            conditionCollection.addStateRegion(listArea);
            
            ClickOptions deleteClick = new ClickOptions.Builder()
                .withClickType(ClickOptions.ClickType.RIGHT)
                .withClicks(1)
                .build();
            
            PatternFindOptions checkEmpty = new PatternFindOptions.Builder()
                .withFind(PatternFindOptions.Find.ALL)
                .build();
            
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(deleteClick)
                .setActionObjectCollection(actionCollection)
                .setUntilAction(checkEmpty)
                .setConditionObjectCollection(conditionCollection)
                .setMaxActions(50)
                .build();
            
            assertEquals(ClickOptions.ClickType.RIGHT, ((ClickOptions)config.getDoAction()).getClickType());
            assertEquals(50, config.getMaxActions());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle empty collections")
        public void testEmptyCollections() {
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(new ClickOptions.Builder().build())
                .setActionObjectCollection(new ObjectCollection())
                .setUntilAction(new PatternFindOptions.Builder().build())
                .setConditionObjectCollection(new ObjectCollection())
                .setMaxActions(1)
                .build();
            
            assertTrue(config.getActionObjectCollection().getStateImages().isEmpty());
            assertTrue(config.getConditionObjectCollection().getStateImages().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle max actions boundary")
        public void testMaxActionsBoundary() {
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(new ClickOptions.Builder().build())
                .setActionObjectCollection(actionCollection)
                .setUntilAction(new PatternFindOptions.Builder().build())
                .setConditionObjectCollection(conditionCollection)
                .setMaxActions(0)
                .build();
            
            assertEquals(0, config.getMaxActions());
            
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(new ClickOptions.Builder().build())
                .setActionObjectCollection(actionCollection)
                .setUntilAction(new PatternFindOptions.Builder().build())
                .setConditionObjectCollection(conditionCollection)
                .setMaxActions(Integer.MAX_VALUE)
                .build();
            
            assertEquals(Integer.MAX_VALUE, config.getMaxActions());
        }
        
        @Test
        @DisplayName("Should handle null action configs")
        public void testNullActionConfigs() {
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(null)
                .setActionObjectCollection(actionCollection)
                .setUntilAction(null)
                .setConditionObjectCollection(conditionCollection)
                .build();
            
            assertNull(config.getDoAction());
            assertNull(config.getUntilAction());
        }
    }
    
    @Nested
    @DisplayName("Success Criteria Patterns")
    class SuccessCriteriaPatterns {
        
        @Test
        @DisplayName("Should configure success when objects vanish")
        public void testSuccessWhenVanish() {
            ClickOptions clickOptions = new ClickOptions.Builder()
                .withSuccessCriteria(matches -> matches.isEmpty())
                .build();
            
            assertTrue(clickOptions.getSuccessCriteria().test(Collections.emptyList()));
            assertFalse(clickOptions.getSuccessCriteria().test(
                Arrays.asList(new Match.Builder().build())));
        }
        
        @Test
        @DisplayName("Should configure success when objects appear")
        public void testSuccessWhenAppear() {
            ClickOptions clickOptions = new ClickOptions.Builder()
                .withSuccessCriteria(matches -> !matches.isEmpty())
                .build();
            
            assertFalse(clickOptions.getSuccessCriteria().test(Collections.emptyList()));
            assertTrue(clickOptions.getSuccessCriteria().test(
                Arrays.asList(new Match.Builder().build())));
        }
        
        @Test
        @DisplayName("Should configure success with custom criteria")
        public void testCustomSuccessCriteria() {
            ClickOptions clickOptions = new ClickOptions.Builder()
                .withSuccessCriteria(matches -> matches.size() >= 3)
                .build();
            
            assertFalse(clickOptions.getSuccessCriteria().test(
                Arrays.asList(new Match.Builder().build())));
            assertTrue(clickOptions.getSuccessCriteria().test(
                Arrays.asList(
                    new Match.Builder().build(),
                    new Match.Builder().build(),
                    new Match.Builder().build()
                )));
        }
    }
    
    @Nested
    @DisplayName("Parameterized Tests")
    class ParameterizedTests {
        
        @ParameterizedTest
        @ValueSource(ints = {1, 3, 5, 10, 50, 100})
        @DisplayName("Should set various max action limits")
        public void testVariousMaxActions(int maxActions) {
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(new ClickOptions.Builder().build())
                .setActionObjectCollection(actionCollection)
                .setUntilAction(new PatternFindOptions.Builder().build())
                .setConditionObjectCollection(conditionCollection)
                .setMaxActions(maxActions)
                .build();
            
            assertEquals(maxActions, config.getMaxActions());
        }
        
        @ParameterizedTest
        @MethodSource("provideActionConfigPairs")
        @DisplayName("Should handle different action config combinations")
        public void testActionConfigCombinations(ActionConfig doAction, ActionConfig untilAction) {
            config = new ModernRepeatUntilConfig.Builder()
                .setDoAction(doAction)
                .setActionObjectCollection(actionCollection)
                .setUntilAction(untilAction)
                .setConditionObjectCollection(conditionCollection)
                .build();
            
            assertEquals(doAction, config.getDoAction());
            assertEquals(untilAction, config.getUntilAction());
        }
        
        static Stream<Arguments> provideActionConfigPairs() {
            return Stream.of(
                Arguments.of(
                    new ClickOptions.Builder().build(),
                    new PatternFindOptions.Builder().build()
                ),
                Arguments.of(
                    new ClickOptions.Builder().withClicks(2).build(),
                    new PatternFindOptions.Builder().withSimilarity(0.7).build()
                ),
                Arguments.of(
                    new ClickOptions.Builder().withClickType(ClickOptions.ClickType.RIGHT).build(),
                    new PatternFindOptions.Builder().withFind(PatternFindOptions.Find.ALL).build()
                )
            );
        }
    }
}