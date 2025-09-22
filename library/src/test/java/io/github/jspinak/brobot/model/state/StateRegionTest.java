package io.github.jspinak.brobot.model.state;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions.DefineAs;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.element.Anchor;
import io.github.jspinak.brobot.model.element.Anchors;
import io.github.jspinak.brobot.model.element.CrossStateAnchor;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for StateRegion - represents meaningful screen areas. Tests region
 * definition, state association, interaction tracking, and anchoring.
 */
@DisplayName("StateRegion Tests")
public class StateRegionTest extends BrobotTestBase {

    private StateRegion stateRegion;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        stateRegion = new StateRegion();
    }

    @Nested
    @DisplayName("Default Values")
    class DefaultValues {

        @Test
        @DisplayName("Default object type is REGION")
        public void testDefaultObjectType() {
            assertEquals(StateObject.Type.REGION, stateRegion.getObjectType());
        }

        @Test
        @DisplayName("Default name is empty string")
        public void testDefaultName() {
            assertEquals("", stateRegion.getName());
        }

        @Test
        @DisplayName("Default search region is initialized")
        public void testDefaultSearchRegion() {
            assertNotNull(stateRegion.getSearchRegion());
            assertEquals(0, stateRegion.getSearchRegion().x());
            assertEquals(0, stateRegion.getSearchRegion().y());
        }

        @Test
        @DisplayName("Default owner state is null")
        public void testDefaultOwnerState() {
            assertEquals("null", stateRegion.getOwnerStateName());
            assertEquals(0L, stateRegion.getOwnerStateId());
        }

        @Test
        @DisplayName("Default probabilities are 100")
        public void testDefaultProbabilities() {
            assertEquals(100, stateRegion.getStaysVisibleAfterClicked());
            assertEquals(100, stateRegion.getMockFindStochasticModifier());
        }

        @Test
        @DisplayName("Default position is center (0.5, 0.5)")
        public void testDefaultPosition() {
            Position pos = stateRegion.getPosition();
            assertEquals(0.5, pos.getPercentW());
            assertEquals(0.5, pos.getPercentH());
        }

        @Test
        @DisplayName("Default mock text")
        public void testDefaultMockText() {
            assertEquals("mock text", stateRegion.getMockText());
        }

        @Test
        @DisplayName("Default define strategy")
        public void testDefaultDefineStrategy() {
            assertEquals(DefineAs.OUTSIDE_ANCHORS, stateRegion.getDefineStrategy());
        }
    }

    @Nested
    @DisplayName("Region Configuration")
    class RegionConfiguration {

        @Test
        @DisplayName("Set search region")
        public void testSetSearchRegion() {
            Region region = new Region(100, 200, 300, 400);
            stateRegion.setSearchRegion(region);

            assertEquals(100, stateRegion.getSearchRegion().x());
            assertEquals(200, stateRegion.getSearchRegion().y());
            assertEquals(300, stateRegion.getSearchRegion().w());
            assertEquals(400, stateRegion.getSearchRegion().h());
        }

        @Test
        @DisplayName("Get region coordinates")
        public void testGetRegionCoordinates() {
            Region region = new Region(50, 75, 200, 150);
            stateRegion.setSearchRegion(region);

            assertEquals(50, stateRegion.x());
            assertEquals(75, stateRegion.y());
            assertEquals(200, stateRegion.w());
            assertEquals(150, stateRegion.h());
        }

        @Test
        @DisplayName("Region with negative coordinates")
        public void testNegativeCoordinates() {
            Region region = new Region(-50, -100, 200, 300);
            stateRegion.setSearchRegion(region);

            assertEquals(-50, stateRegion.x());
            assertEquals(-100, stateRegion.y());
        }

        @Test
        @DisplayName("Zero-sized region")
        public void testZeroSizedRegion() {
            Region region = new Region(100, 100, 0, 0);
            stateRegion.setSearchRegion(region);

            assertEquals(0, stateRegion.w());
            assertEquals(0, stateRegion.h());
        }
    }

    @Nested
    @DisplayName("State Association")
    class StateAssociation {

        @Test
        @DisplayName("Set owner state name")
        public void testSetOwnerStateName() {
            stateRegion.setOwnerStateName("LoginScreen");

            assertEquals("LoginScreen", stateRegion.getOwnerStateName());
        }

        @Test
        @DisplayName("Set owner state ID")
        public void testSetOwnerStateId() {
            stateRegion.setOwnerStateId(123L);

            assertEquals(123L, stateRegion.getOwnerStateId());
        }

        @ParameterizedTest
        @CsvSource({"MainMenu, 1", "Settings, 2", "Dialog, 3", "Wizard, 4"})
        @DisplayName("Various state associations")
        public void testVariousStateAssociations(String stateName, long stateId) {
            stateRegion.setOwnerStateName(stateName);
            stateRegion.setOwnerStateId(stateId);

            assertEquals(stateName, stateRegion.getOwnerStateName());
            assertEquals(stateId, stateRegion.getOwnerStateId());
        }
    }

    @Nested
    @DisplayName("Interaction Properties")
    class InteractionProperties {

        @Test
        @DisplayName("Set stays visible after clicked")
        public void testSetStaysVisibleAfterClicked() {
            stateRegion.setStaysVisibleAfterClicked(75);

            assertEquals(75, stateRegion.getStaysVisibleAfterClicked());
        }

        @Test
        @DisplayName("Set probability exists")
        public void testSetProbabilityExists() {
            stateRegion.setMockFindStochasticModifier(80);

            assertEquals(80, stateRegion.getMockFindStochasticModifier());
        }

        @Test
        @DisplayName("Track times acted on")
        public void testTrackTimesActedOn() {
            stateRegion.setTimesActedOn(10);

            assertEquals(10, stateRegion.getTimesActedOn());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 25, 50, 75, 100})
        @DisplayName("Various probability values")
        public void testVariousProbabilities(int probability) {
            stateRegion.setStaysVisibleAfterClicked(probability);
            stateRegion.setMockFindStochasticModifier(probability);

            assertEquals(probability, stateRegion.getStaysVisibleAfterClicked());
            assertEquals(probability, stateRegion.getMockFindStochasticModifier());
        }

        @Test
        @DisplayName("Increment times acted on")
        public void testIncrementTimesActedOn() {
            for (int i = 1; i <= 5; i++) {
                stateRegion.setTimesActedOn(i);
                assertEquals(i, stateRegion.getTimesActedOn());
            }
        }
    }

    @Nested
    @DisplayName("Position Configuration")
    class PositionConfiguration {

        @Test
        @DisplayName("Set click position")
        public void testSetClickPosition() {
            Position pos = new Position(0.25, 0.75);
            stateRegion.setPosition(pos);

            assertEquals(0.25, stateRegion.getPosition().getPercentW());
            assertEquals(0.75, stateRegion.getPosition().getPercentH());
        }

        @Test
        @DisplayName("Position at corners")
        public void testCornerPositions() {
            Position topLeft = new Position(0.0, 0.0);
            Position topRight = new Position(1.0, 0.0);
            Position bottomLeft = new Position(0.0, 1.0);
            Position bottomRight = new Position(1.0, 1.0);

            stateRegion.setPosition(topLeft);
            assertEquals(0.0, stateRegion.getPosition().getPercentW());
            assertEquals(0.0, stateRegion.getPosition().getPercentH());

            stateRegion.setPosition(bottomRight);
            assertEquals(1.0, stateRegion.getPosition().getPercentW());
            assertEquals(1.0, stateRegion.getPosition().getPercentH());
        }

        @Test
        @DisplayName("Position with named type")
        public void testPositionWithNamedType() {
            Position pos = new Position(Positions.Name.MIDDLEMIDDLE);
            stateRegion.setPosition(pos);

            assertEquals(0.5, stateRegion.getPosition().getPercentW());
            assertEquals(0.5, stateRegion.getPosition().getPercentH());
        }
    }

    @Nested
    @DisplayName("Anchor Configuration")
    class AnchorConfiguration {

        @Test
        @DisplayName("Default anchors initialized")
        public void testDefaultAnchors() {
            assertNotNull(stateRegion.getAnchors());
            assertTrue(stateRegion.getAnchors().getAnchorList().isEmpty());
        }

        @Test
        @DisplayName("Add single anchor")
        public void testAddSingleAnchor() {
            Anchor anchor = new Anchor(Positions.Name.TOPMIDDLE, new Position(0.5, 0.0));

            Anchors anchors = new Anchors();
            anchors.getAnchorList().add(anchor);
            stateRegion.setAnchors(anchors);

            assertEquals(1, stateRegion.getAnchors().getAnchorList().size());
            assertEquals(
                    Positions.Name.TOPMIDDLE,
                    stateRegion.getAnchors().getAnchorList().get(0).getAnchorInNewDefinedRegion());
        }

        @Test
        @DisplayName("Add multiple anchors")
        public void testAddMultipleAnchors() {
            Anchors anchors = new Anchors();
            anchors.getAnchorList().add(new Anchor(Positions.Name.TOPLEFT, new Position(0.0, 0.0)));
            anchors.getAnchorList()
                    .add(new Anchor(Positions.Name.TOPRIGHT, new Position(1.0, 0.0)));
            anchors.getAnchorList()
                    .add(new Anchor(Positions.Name.BOTTOMLEFT, new Position(0.0, 1.0)));

            stateRegion.setAnchors(anchors);

            assertEquals(3, stateRegion.getAnchors().getAnchorList().size());
        }

        @Test
        @DisplayName("Cross-state anchors")
        public void testCrossStateAnchors() {
            CrossStateAnchor crossAnchor = new CrossStateAnchor();
            crossAnchor.setSourceStateName("RelatedState");

            stateRegion.getCrossStateAnchors().add(crossAnchor);

            assertEquals(1, stateRegion.getCrossStateAnchors().size());
            assertEquals(
                    "RelatedState", stateRegion.getCrossStateAnchors().get(0).getSourceStateName());
        }
    }

    @Nested
    @DisplayName("Mock Text")
    class MockText {

        @Test
        @DisplayName("Set custom mock text")
        public void testSetMockText() {
            stateRegion.setMockText("Custom test text");

            assertEquals("Custom test text", stateRegion.getMockText());
        }

        @Test
        @DisplayName("Empty mock text")
        public void testEmptyMockText() {
            stateRegion.setMockText("");

            assertEquals("", stateRegion.getMockText());
        }

        @Test
        @DisplayName("Multi-line mock text")
        public void testMultiLineMockText() {
            String multiLine = "Line 1\nLine 2\nLine 3";
            stateRegion.setMockText(multiLine);

            assertEquals(multiLine, stateRegion.getMockText());
        }

        @Test
        @DisplayName("Special characters in mock text")
        public void testSpecialCharactersMockText() {
            String special = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
            stateRegion.setMockText(special);

            assertEquals(special, stateRegion.getMockText());
        }
    }

    @Nested
    @DisplayName("Action History")
    class ActionHistoryTests {

        @Test
        @DisplayName("Default action history initialized")
        public void testDefaultActionHistory() {
            assertNotNull(stateRegion.getMatchHistory());
        }

        @Test
        @DisplayName("Set custom action history")
        public void testSetActionHistory() {
            ActionHistory customHistory = new ActionHistory();
            stateRegion.setMatchHistory(customHistory);

            assertEquals(customHistory, stateRegion.getMatchHistory());
        }
    }

    @Nested
    @DisplayName("Define Strategy")
    class DefineStrategy {

        @ParameterizedTest
        @EnumSource(DefineAs.class)
        @DisplayName("All define strategies")
        public void testAllDefineStrategies(DefineAs strategy) {
            stateRegion.setDefineStrategy(strategy);

            assertEquals(strategy, stateRegion.getDefineStrategy());
        }

        @Test
        @DisplayName("Change define strategy")
        public void testChangeDefineStrategy() {
            stateRegion.setDefineStrategy(DefineAs.INSIDE_ANCHORS);
            assertEquals(DefineAs.INSIDE_ANCHORS, stateRegion.getDefineStrategy());

            stateRegion.setDefineStrategy(DefineAs.OUTSIDE_ANCHORS);
            assertEquals(DefineAs.OUTSIDE_ANCHORS, stateRegion.getDefineStrategy());
        }
    }

    @Nested
    @DisplayName("ID Generation")
    class IdGeneration {

        @Test
        @DisplayName("Generate ID with name and region")
        public void testIdWithNameAndRegion() {
            stateRegion.setName("InputField");
            stateRegion.setSearchRegion(new Region(100, 200, 300, 400));

            String id = stateRegion.getIdAsString();

            assertEquals("REGIONInputField100200300400", id);
        }

        @Test
        @DisplayName("Generate ID with empty name")
        public void testIdWithEmptyName() {
            stateRegion.setSearchRegion(new Region(50, 75, 100, 125));

            String id = stateRegion.getIdAsString();

            assertEquals("REGION5075100125", id);
        }

        @Test
        @DisplayName("ID changes with region")
        public void testIdChangesWithRegion() {
            stateRegion.setName("Dynamic");
            stateRegion.setSearchRegion(new Region(0, 0, 50, 50));
            String id1 = stateRegion.getIdAsString();

            stateRegion.setSearchRegion(new Region(100, 100, 150, 150));
            String id2 = stateRegion.getIdAsString();

            assertNotEquals(id1, id2);
        }
    }

    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {

        @Test
        @DisplayName("Text input field region")
        public void testTextInputField() {
            stateRegion.setName("UsernameField");
            stateRegion.setSearchRegion(new Region(200, 300, 400, 50));
            stateRegion.setOwnerStateName("LoginForm");
            stateRegion.setStaysVisibleAfterClicked(100); // Field stays visible
            stateRegion.setMockFindStochasticModifier(95);
            stateRegion.setMockText("user@example.com");
            stateRegion.setPosition(new Position(0.5, 0.5)); // Click in center

            assertEquals("UsernameField", stateRegion.getName());
            assertEquals(200, stateRegion.x());
            assertEquals(300, stateRegion.y());
            assertEquals(400, stateRegion.w());
            assertEquals(50, stateRegion.h());
            assertEquals("user@example.com", stateRegion.getMockText());
        }

        @Test
        @DisplayName("Dynamic content area")
        public void testDynamicContentArea() {
            stateRegion.setName("ContentPanel");
            stateRegion.setSearchRegion(new Region(50, 100, 800, 600));
            stateRegion.setOwnerStateName("MainView");
            stateRegion.setStaysVisibleAfterClicked(100);
            stateRegion.setMockFindStochasticModifier(100);

            // Add anchors for dynamic positioning
            Anchors anchors = new Anchors();
            Anchor topAnchor = new Anchor(Positions.Name.TOPMIDDLE, new Position(0.5, 0.0));
            anchors.getAnchorList().add(topAnchor);
            stateRegion.setAnchors(anchors);

            assertEquals(1, stateRegion.getAnchors().getAnchorList().size());
            assertEquals(
                    Positions.Name.TOPMIDDLE,
                    stateRegion.getAnchors().getAnchorList().get(0).getAnchorInNewDefinedRegion());
        }

        @Test
        @DisplayName("Button region that disappears")
        public void testDisappearingButton() {
            stateRegion.setName("SubmitButton");
            stateRegion.setSearchRegion(new Region(400, 500, 200, 60));
            stateRegion.setOwnerStateName("FormState");
            stateRegion.setStaysVisibleAfterClicked(0); // Disappears after click
            stateRegion.setMockFindStochasticModifier(90);
            stateRegion.setPosition(new Position(0.5, 0.5));

            assertEquals(0, stateRegion.getStaysVisibleAfterClicked());
            assertEquals(90, stateRegion.getMockFindStochasticModifier());
        }

        @Test
        @DisplayName("Frequently interacted region")
        public void testFrequentlyInteracted() {
            stateRegion.setName("RefreshArea");
            stateRegion.setSearchRegion(new Region(700, 50, 100, 50));
            stateRegion.setTimesActedOn(100);

            assertEquals(100, stateRegion.getTimesActedOn());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Null search region")
        public void testNullSearchRegion() {
            stateRegion.setSearchRegion(null);

            assertNull(stateRegion.getSearchRegion());

            // Accessing coordinates should handle null
            assertThrows(NullPointerException.class, () -> stateRegion.x());
        }

        @Test
        @DisplayName("Negative probabilities")
        public void testNegativeProbabilities() {
            // No validation, so negative values are accepted
            stateRegion.setStaysVisibleAfterClicked(-10);
            stateRegion.setMockFindStochasticModifier(-20);

            assertEquals(-10, stateRegion.getStaysVisibleAfterClicked());
            assertEquals(-20, stateRegion.getMockFindStochasticModifier());
        }

        @Test
        @DisplayName("Very large region")
        public void testVeryLargeRegion() {
            Region huge = new Region(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            stateRegion.setSearchRegion(huge);

            assertEquals(Integer.MAX_VALUE, stateRegion.w());
            assertEquals(Integer.MAX_VALUE, stateRegion.h());
        }

        @Test
        @DisplayName("Position outside 0-1 range")
        public void testPositionOutsideRange() {
            Position pos = new Position(1.5, -0.5);
            stateRegion.setPosition(pos);

            assertEquals(1.5, stateRegion.getPosition().getPercentW());
            assertEquals(-0.5, stateRegion.getPosition().getPercentH());
        }
    }
}
