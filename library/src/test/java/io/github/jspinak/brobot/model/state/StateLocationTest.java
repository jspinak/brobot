package io.github.jspinak.brobot.model.state;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Anchors;
import io.github.jspinak.brobot.model.element.Anchor;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for StateLocation - represents meaningful screen coordinates.
 * Tests positioning, state association, interaction tracking, and anchoring.
 */
@DisplayName("StateLocation Tests")
public class StateLocationTest extends BrobotTestBase {
    
    @Mock
    private Location mockLocation;
    
    @Mock
    private Position mockPosition;
    
    @Mock
    private Anchors mockAnchors;
    
    @Mock
    private SearchRegionOnObject mockSearchRegion;
    
    private StateLocation stateLocation;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        stateLocation = new StateLocation();
    }
    
    @Nested
    @DisplayName("Basic Properties")
    class BasicProperties {
        
        @Test
        @DisplayName("Default object type is LOCATION")
        public void testDefaultObjectType() {
            assertEquals(StateObject.Type.LOCATION, stateLocation.getObjectType());
        }
        
        @Test
        @DisplayName("Set and get name")
        public void testSetName() {
            stateLocation.setName("SubmitButton");
            
            assertEquals("SubmitButton", stateLocation.getName());
        }
        
        @Test
        @DisplayName("Set and get location")
        public void testSetLocation() {
            Location loc = new Location(100, 200);
            stateLocation.setLocation(loc);
            
            assertEquals(loc, stateLocation.getLocation());
        }
        
        @Test
        @DisplayName("Default owner state is null")
        public void testDefaultOwnerState() {
            assertEquals("null", stateLocation.getOwnerStateName());
            assertNull(stateLocation.getOwnerStateId());
        }
        
        @Test
        @DisplayName("Set owner state information")
        public void testSetOwnerState() {
            stateLocation.setOwnerStateName("LoginState");
            stateLocation.setOwnerStateId(123L);
            
            assertEquals("LoginState", stateLocation.getOwnerStateName());
            assertEquals(123L, stateLocation.getOwnerStateId());
        }
    }
    
    @Nested
    @DisplayName("Probability Configuration")
    class ProbabilityConfiguration {
        
        @Test
        @DisplayName("Default probability stays visible is 100")
        public void testDefaultProbabilityStaysVisible() {
            assertEquals(100, stateLocation.getProbabilityStaysVisibleAfterClicked());
        }
        
        @Test
        @DisplayName("Set probability stays visible after clicked")
        public void testSetProbabilityStaysVisible() {
            stateLocation.setProbabilityStaysVisibleAfterClicked(75);
            
            assertEquals(75, stateLocation.getProbabilityStaysVisibleAfterClicked());
        }
        
        @Test
        @DisplayName("Default probability exists is 100")
        public void testDefaultProbabilityExists() {
            assertEquals(100, stateLocation.getProbabilityExists());
        }
        
        @Test
        @DisplayName("Set probability exists")
        public void testSetProbabilityExists() {
            stateLocation.setProbabilityExists(80);
            
            assertEquals(80, stateLocation.getProbabilityExists());
        }
        
        @ParameterizedTest
        @ValueSource(ints = {0, 25, 50, 75, 100})
        @DisplayName("Various probability values")
        public void testVariousProbabilities(int probability) {
            stateLocation.setProbabilityStaysVisibleAfterClicked(probability);
            stateLocation.setProbabilityExists(probability);
            
            assertEquals(probability, stateLocation.getProbabilityStaysVisibleAfterClicked());
            assertEquals(probability, stateLocation.getProbabilityExists());
        }
        
        @Test
        @DisplayName("Probability values outside 0-100 range")
        public void testProbabilitiesOutsideRange() {
            // Test that values outside range are accepted (no validation)
            stateLocation.setProbabilityStaysVisibleAfterClicked(-10);
            stateLocation.setProbabilityExists(150);
            
            assertEquals(-10, stateLocation.getProbabilityStaysVisibleAfterClicked());
            assertEquals(150, stateLocation.getProbabilityExists());
        }
    }
    
    @Nested
    @DisplayName("Interaction Tracking")
    class InteractionTracking {
        
        @Test
        @DisplayName("Default times acted on is 0")
        public void testDefaultTimesActedOn() {
            assertEquals(0, stateLocation.getTimesActedOn());
        }
        
        @Test
        @DisplayName("Increment times acted on")
        public void testIncrementTimesActedOn() {
            stateLocation.setTimesActedOn(5);
            
            assertEquals(5, stateLocation.getTimesActedOn());
        }
        
        @Test
        @DisplayName("Track multiple interactions")
        public void testMultipleInteractions() {
            for (int i = 1; i <= 10; i++) {
                stateLocation.setTimesActedOn(i);
                assertEquals(i, stateLocation.getTimesActedOn());
            }
        }
        
        @Test
        @DisplayName("Action history is initialized")
        public void testActionHistoryInitialized() {
            ActionHistory history = stateLocation.getMatchHistory();
            
            assertNotNull(history);
        }
        
        @Test
        @DisplayName("Set custom action history")
        public void testSetActionHistory() {
            ActionHistory customHistory = new ActionHistory();
            stateLocation.setMatchHistory(customHistory);
            
            assertEquals(customHistory, stateLocation.getMatchHistory());
        }
    }
    
    @Nested
    @DisplayName("Position and Anchoring")
    class PositionAndAnchoring {
        
        @Test
        @DisplayName("Set position")
        public void testSetPosition() {
            stateLocation.setPosition(mockPosition);
            
            assertEquals(mockPosition, stateLocation.getPosition());
        }
        
        @Test
        @DisplayName("Set anchors")
        public void testSetAnchors() {
            stateLocation.setAnchors(mockAnchors);
            
            assertEquals(mockAnchors, stateLocation.getAnchors());
        }
        
        @Test
        @DisplayName("Position with named location")
        public void testPositionWithNamedLocation() {
            Position pos = new Position(Positions.Name.TOPLEFT);
            stateLocation.setPosition(pos);
            
            assertEquals(0.0, stateLocation.getPosition().getPercentW());
            assertEquals(0.0, stateLocation.getPosition().getPercentH());
        }
        
        @Test
        @DisplayName("Anchor to another element")
        public void testAnchorToElement() {
            Anchors anchors = new Anchors();
            Anchor anchor = new Anchor(Positions.Name.TOPMIDDLE, new Position(0.5, 0.0));
            anchors.getAnchorList().add(anchor);
            
            stateLocation.setAnchors(anchors);
            
            assertEquals(1, stateLocation.getAnchors().getAnchorList().size());
            assertEquals(Positions.Name.TOPMIDDLE, 
                stateLocation.getAnchors().getAnchorList().get(0).getAnchorInNewDefinedRegion());
        }
    }
    
    @Nested
    @DisplayName("Search Region Configuration")
    class SearchRegionConfiguration {
        
        @Test
        @DisplayName("Set search region on object")
        public void testSetSearchRegion() {
            stateLocation.setSearchRegionOnObject(mockSearchRegion);
            
            assertEquals(mockSearchRegion, stateLocation.getSearchRegionOnObject());
        }
        
        @Test
        @DisplayName("Default search region is null")
        public void testDefaultSearchRegion() {
            assertNull(stateLocation.getSearchRegionOnObject());
        }
    }
    
    @Nested
    @DisplayName("ID Generation")
    class IdGeneration {
        
        @Test
        @DisplayName("Generate ID with location")
        public void testIdWithLocation() {
            when(mockLocation.getCalculatedX()).thenReturn(100);
            when(mockLocation.getCalculatedY()).thenReturn(200);
            
            stateLocation.setName("TestLocation");
            stateLocation.setLocation(mockLocation);
            
            String id = stateLocation.getIdAsString();
            
            assertEquals("LOCATIONTestLocation100200", id);
        }
        
        @Test
        @DisplayName("Generate ID without name")
        public void testIdWithoutName() {
            when(mockLocation.getCalculatedX()).thenReturn(50);
            when(mockLocation.getCalculatedY()).thenReturn(75);
            
            stateLocation.setLocation(mockLocation);
            
            String id = stateLocation.getIdAsString();
            
            assertEquals("LOCATIONnull5075", id);
        }
        
        @Test
        @DisplayName("ID changes with location")
        public void testIdChangesWithLocation() {
            Location loc1 = new Location(10, 20);
            Location loc2 = new Location(30, 40);
            
            stateLocation.setName("Dynamic");
            stateLocation.setLocation(loc1);
            String id1 = stateLocation.getIdAsString();
            
            stateLocation.setLocation(loc2);
            String id2 = stateLocation.getIdAsString();
            
            assertNotEquals(id1, id2);
        }
    }
    
    @Nested
    @DisplayName("Definition Check")
    class DefinitionCheck {
        
        @Test
        @DisplayName("Defined when location is set")
        public void testDefinedWithLocation() {
            stateLocation.setLocation(new Location(100, 100));
            
            assertTrue(stateLocation.defined());
        }
        
        @Test
        @DisplayName("Not defined when location is null")
        public void testNotDefinedWithoutLocation() {
            stateLocation.setLocation(null);
            
            assertFalse(stateLocation.defined());
        }
        
        @Test
        @DisplayName("Definition independent of other properties")
        public void testDefinitionIndependent() {
            stateLocation.setName("Test");
            stateLocation.setOwnerStateName("State");
            stateLocation.setPosition(mockPosition);
            
            assertFalse(stateLocation.defined());
            
            stateLocation.setLocation(new Location(0, 0));
            
            assertTrue(stateLocation.defined());
        }
    }
    
    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("Button location in login state")
        public void testLoginButtonLocation() {
            Location btnLocation = new Location(500, 400);
            
            stateLocation.setName("LoginButton");
            stateLocation.setLocation(btnLocation);
            stateLocation.setOwnerStateName("LoginState");
            stateLocation.setProbabilityStaysVisibleAfterClicked(0); // Button disappears after click
            stateLocation.setProbabilityExists(95); // Usually present
            
            assertEquals("LoginButton", stateLocation.getName());
            assertEquals(500, stateLocation.getLocation().getX());
            assertEquals(400, stateLocation.getLocation().getY());
            assertEquals(0, stateLocation.getProbabilityStaysVisibleAfterClicked());
        }
        
        @Test
        @DisplayName("Menu item that stays visible")
        public void testPersistentMenuItem() {
            Location menuLocation = new Location(100, 50);
            
            stateLocation.setName("FileMenu");
            stateLocation.setLocation(menuLocation);
            stateLocation.setOwnerStateName("MainMenuState");
            stateLocation.setProbabilityStaysVisibleAfterClicked(100); // Menu stays open
            stateLocation.setProbabilityExists(100); // Always present in this state
            
            assertEquals(100, stateLocation.getProbabilityStaysVisibleAfterClicked());
            assertEquals(100, stateLocation.getProbabilityExists());
        }
        
        @Test
        @DisplayName("Dynamic location with anchor")
        public void testDynamicAnchoredLocation() {
            Location baseLocation = new Location(200, 200);
            Position relativePos = new Position(Positions.Name.BOTTOMRIGHT);
            
            Anchors anchors = new Anchors();
            Anchor anchor = new Anchor(Positions.Name.BOTTOMRIGHT, new Position(1.0, 1.0));
            anchors.getAnchorList().add(anchor);
            
            stateLocation.setName("DialogButton");
            stateLocation.setLocation(baseLocation);
            stateLocation.setPosition(relativePos);
            stateLocation.setAnchors(anchors);
            stateLocation.setOwnerStateName("DialogState");
            
            assertTrue(stateLocation.defined());
            assertNotNull(stateLocation.getAnchors());
            assertEquals(1.0, stateLocation.getPosition().getPercentW());
            assertEquals(1.0, stateLocation.getPosition().getPercentH());
        }
        
        @Test
        @DisplayName("Frequently clicked location tracking")
        public void testFrequentlyClickedLocation() {
            stateLocation.setName("RefreshButton");
            stateLocation.setLocation(new Location(800, 100));
            stateLocation.setTimesActedOn(0);
            
            // Simulate multiple clicks
            for (int i = 1; i <= 50; i++) {
                stateLocation.setTimesActedOn(i);
            }
            
            assertEquals(50, stateLocation.getTimesActedOn());
        }
    }
    
    @Nested
    @DisplayName("State Association")
    class StateAssociation {
        
        @Test
        @DisplayName("Associate with state by name")
        public void testAssociateByName() {
            stateLocation.setOwnerStateName("HomeScreen");
            
            assertEquals("HomeScreen", stateLocation.getOwnerStateName());
        }
        
        @Test
        @DisplayName("Associate with state by ID")
        public void testAssociateById() {
            stateLocation.setOwnerStateId(999L);
            
            assertEquals(999L, stateLocation.getOwnerStateId());
        }
        
        @Test
        @DisplayName("Associate with both name and ID")
        public void testAssociateBoth() {
            stateLocation.setOwnerStateName("SettingsScreen");
            stateLocation.setOwnerStateId(456L);
            
            assertEquals("SettingsScreen", stateLocation.getOwnerStateName());
            assertEquals(456L, stateLocation.getOwnerStateId());
        }
        
        @ParameterizedTest
        @CsvSource({
            "LoginState, 1",
            "MainMenu, 2",
            "Settings, 3",
            "Dialog, 4"
        })
        @DisplayName("Various state associations")
        public void testVariousStateAssociations(String stateName, long stateId) {
            stateLocation.setOwnerStateName(stateName);
            stateLocation.setOwnerStateId(stateId);
            
            assertEquals(stateName, stateLocation.getOwnerStateName());
            assertEquals(stateId, stateLocation.getOwnerStateId());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Null location handling")
        public void testNullLocation() {
            stateLocation.setLocation(null);
            
            assertFalse(stateLocation.defined());
            
            // ID generation with null location should handle NPE
            assertThrows(NullPointerException.class, () -> stateLocation.getIdAsString());
        }
        
        @Test
        @DisplayName("Empty name handling")
        public void testEmptyName() {
            stateLocation.setName("");
            stateLocation.setLocation(new Location(0, 0));
            
            String id = stateLocation.getIdAsString();
            assertEquals("LOCATION00", id);
        }
        
        @Test
        @DisplayName("Negative coordinates")
        public void testNegativeCoordinates() {
            Location negLocation = new Location(-100, -200);
            stateLocation.setLocation(negLocation);
            
            assertTrue(stateLocation.defined());
            assertEquals(-100, stateLocation.getLocation().getX());
            assertEquals(-200, stateLocation.getLocation().getY());
        }
        
        @Test
        @DisplayName("Very large coordinates")
        public void testLargeCoordinates() {
            Location largeLocation = new Location(Integer.MAX_VALUE, Integer.MAX_VALUE);
            stateLocation.setLocation(largeLocation);
            
            assertTrue(stateLocation.defined());
            assertEquals(Integer.MAX_VALUE, stateLocation.getLocation().getX());
            assertEquals(Integer.MAX_VALUE, stateLocation.getLocation().getY());
        }
        
        @Test
        @DisplayName("Zero probabilities")
        public void testZeroProbabilities() {
            stateLocation.setProbabilityStaysVisibleAfterClicked(0);
            stateLocation.setProbabilityExists(0);
            
            assertEquals(0, stateLocation.getProbabilityStaysVisibleAfterClicked());
            assertEquals(0, stateLocation.getProbabilityExists());
        }
    }
}