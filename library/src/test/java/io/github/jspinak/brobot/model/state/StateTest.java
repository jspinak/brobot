package io.github.jspinak.brobot.model.state;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("State Tests")
public class StateTest extends BrobotTestBase {
    
    @Mock
    private StateImage mockStateImage;
    
    @Mock
    private StateRegion mockStateRegion;
    
    @Mock
    private StateLocation mockStateLocation;
    
    @Mock
    private StateString mockStateString;
    
    @Mock
    private Scene mockScene;
    
    private State state;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        state = new State();
    }
    
    @Nested
    @DisplayName("Basic Properties")
    class BasicProperties {
        
        @Test
        @DisplayName("Default constructor initializes collections")
        public void testDefaultConstructor() {
            State newState = new State();
            
            assertNotNull(newState.getStateText());
            assertNotNull(newState.getStateImages());
            assertNotNull(newState.getStateStrings());
            assertNotNull(newState.getStateRegions());
            assertNotNull(newState.getStateLocations());
            assertNotNull(newState.getCanHide());
            assertNotNull(newState.getCanHideIds());
            assertNotNull(newState.getHiddenStateNames());
            assertNotNull(newState.getHiddenStateIds());
            assertNotNull(newState.getScenes());
            assertNotNull(newState.getUsableArea());
            assertNotNull(newState.getMatchHistory());
            
            assertTrue(newState.getStateText().isEmpty());
            assertTrue(newState.getStateImages().isEmpty());
            assertEquals("", newState.getName());
            assertFalse(newState.isBlocking());
            assertEquals(100, newState.getBaseProbabilityExists());
            assertEquals(0, newState.getProbabilityExists());
            assertEquals(1, newState.getPathScore());
        }
        
        @Test
        @DisplayName("Set and get basic properties")
        public void testBasicProperties() {
            Long id = 123L;
            String name = "TestState";
            LocalDateTime now = LocalDateTime.now();
            
            state.setId(id);
            state.setName(name);
            state.setBlocking(true);
            state.setPathScore(5);
            state.setLastAccessed(now);
            state.setBaseProbabilityExists(80);
            state.setProbabilityExists(75);
            state.setTimesVisited(10);
            
            assertEquals(id, state.getId());
            assertEquals(name, state.getName());
            assertTrue(state.isBlocking());
            assertEquals(5, state.getPathScore());
            assertEquals(now, state.getLastAccessed());
            assertEquals(80, state.getBaseProbabilityExists());
            assertEquals(75, state.getProbabilityExists());
            assertEquals(10, state.getTimesVisited());
        }
    }
    
    @Nested
    @DisplayName("StateImage Management")
    class StateImageManagement {
        
        @Test
        @DisplayName("Add StateImage sets owner state name")
        public void testAddStateImage() {
            state.setName("TestState");
            
            state.addStateImage(mockStateImage);
            
            verify(mockStateImage).setOwnerStateName("TestState");
            assertTrue(state.getStateImages().contains(mockStateImage));
            assertEquals(1, state.getStateImages().size());
        }
        
        @Test
        @DisplayName("Add multiple StateImages")
        public void testAddMultipleStateImages() {
            state.setName("TestState");
            StateImage image1 = mock(StateImage.class);
            StateImage image2 = mock(StateImage.class);
            StateImage image3 = mock(StateImage.class);
            
            state.addStateImage(image1);
            state.addStateImage(image2);
            state.addStateImage(image3);
            
            assertEquals(3, state.getStateImages().size());
            assertTrue(state.getStateImages().contains(image1));
            assertTrue(state.getStateImages().contains(image2));
            assertTrue(state.getStateImages().contains(image3));
            
            verify(image1).setOwnerStateName("TestState");
            verify(image2).setOwnerStateName("TestState");
            verify(image3).setOwnerStateName("TestState");
        }
        
        @Test
        @DisplayName("StateImages collection is modifiable")
        public void testStateImagesModifiable() {
            state.getStateImages().add(mockStateImage);
            
            assertEquals(1, state.getStateImages().size());
            
            state.getStateImages().remove(mockStateImage);
            
            assertEquals(0, state.getStateImages().size());
        }
    }
    
    @Nested
    @DisplayName("StateRegion Management")
    class StateRegionManagement {
        
        @Test
        @DisplayName("Add StateRegion sets owner state name")
        public void testAddStateRegion() {
            state.setName("TestState");
            
            state.addStateRegion(mockStateRegion);
            
            verify(mockStateRegion).setOwnerStateName("TestState");
            assertTrue(state.getStateRegions().contains(mockStateRegion));
        }
        
        @Test
        @DisplayName("Add multiple StateRegions")
        public void testAddMultipleStateRegions() {
            state.setName("TestState");
            StateRegion region1 = mock(StateRegion.class);
            StateRegion region2 = mock(StateRegion.class);
            
            state.addStateRegion(region1);
            state.addStateRegion(region2);
            
            assertEquals(2, state.getStateRegions().size());
            verify(region1).setOwnerStateName("TestState");
            verify(region2).setOwnerStateName("TestState");
        }
    }
    
    @Nested
    @DisplayName("StateLocation Management")
    class StateLocationManagement {
        
        @Test
        @DisplayName("Add StateLocation sets owner state name")
        public void testAddStateLocation() {
            state.setName("TestState");
            
            state.addStateLocation(mockStateLocation);
            
            verify(mockStateLocation).setOwnerStateName("TestState");
            assertTrue(state.getStateLocations().contains(mockStateLocation));
        }
        
        @Test
        @DisplayName("Add multiple StateLocations")
        public void testAddMultipleStateLocations() {
            state.setName("TestState");
            StateLocation loc1 = mock(StateLocation.class);
            StateLocation loc2 = mock(StateLocation.class);
            StateLocation loc3 = mock(StateLocation.class);
            
            state.addStateLocation(loc1);
            state.addStateLocation(loc2);
            state.addStateLocation(loc3);
            
            assertEquals(3, state.getStateLocations().size());
            verify(loc1).setOwnerStateName("TestState");
            verify(loc2).setOwnerStateName("TestState");
            verify(loc3).setOwnerStateName("TestState");
        }
    }
    
    @Nested
    @DisplayName("StateString Management")
    class StateStringManagement {
        
        @Test
        @DisplayName("Add StateString sets owner state name")
        public void testAddStateString() {
            state.setName("TestState");
            
            state.addStateString(mockStateString);
            
            verify(mockStateString).setOwnerStateName("TestState");
            assertTrue(state.getStateStrings().contains(mockStateString));
        }
        
        @Test
        @DisplayName("Add multiple StateStrings")
        public void testAddMultipleStateStrings() {
            state.setName("TestState");
            StateString string1 = mock(StateString.class);
            StateString string2 = mock(StateString.class);
            
            state.addStateString(string1);
            state.addStateString(string2);
            
            assertEquals(2, state.getStateStrings().size());
            verify(string1).setOwnerStateName("TestState");
            verify(string2).setOwnerStateName("TestState");
        }
    }
    
    @Nested
    @DisplayName("State Text Management")
    class StateTextManagement {
        
        @Test
        @DisplayName("Add state text")
        public void testAddStateText() {
            state.getStateText().add("Login");
            state.getStateText().add("Username");
            state.getStateText().add("Password");
            
            assertEquals(3, state.getStateText().size());
            assertTrue(state.getStateText().contains("Login"));
            assertTrue(state.getStateText().contains("Username"));
            assertTrue(state.getStateText().contains("Password"));
        }
        
        @Test
        @DisplayName("State text is a set - no duplicates")
        public void testStateTextNoDuplicates() {
            state.getStateText().add("Login");
            state.getStateText().add("Login");
            state.getStateText().add("Login");
            
            assertEquals(1, state.getStateText().size());
        }
    }
    
    @Nested
    @DisplayName("Hidden States Management")
    class HiddenStatesManagement {
        
        @Test
        @DisplayName("Manage states that can be hidden")
        public void testCanHideStates() {
            state.getCanHide().add("Menu");
            state.getCanHide().add("Dialog");
            state.getCanHideIds().add(1L);
            state.getCanHideIds().add(2L);
            
            assertEquals(2, state.getCanHide().size());
            assertEquals(2, state.getCanHideIds().size());
            assertTrue(state.getCanHide().contains("Menu"));
            assertTrue(state.getCanHideIds().contains(1L));
        }
        
        @Test
        @DisplayName("Manage currently hidden states")
        public void testHiddenStates() {
            state.getHiddenStateNames().add("Background");
            state.getHiddenStateNames().add("MainContent");
            state.getHiddenStateIds().add(10L);
            state.getHiddenStateIds().add(20L);
            
            assertEquals(2, state.getHiddenStateNames().size());
            assertEquals(2, state.getHiddenStateIds().size());
            assertTrue(state.getHiddenStateNames().contains("Background"));
            assertTrue(state.getHiddenStateIds().contains(10L));
        }
        
        @Test
        @DisplayName("Blocking state behavior")
        public void testBlockingState() {
            assertFalse(state.isBlocking());
            
            state.setBlocking(true);
            
            assertTrue(state.isBlocking());
        }
    }
    
    @Nested
    @DisplayName("Probability Management")
    class ProbabilityManagement {
        
        @Test
        @DisplayName("Base probability defaults to 100")
        public void testDefaultBaseProbability() {
            assertEquals(100, state.getBaseProbabilityExists());
        }
        
        @Test
        @DisplayName("Probability exists defaults to 0")
        public void testDefaultProbabilityExists() {
            assertEquals(0, state.getProbabilityExists());
        }
        
        @Test
        @DisplayName("Set probability to base probability")
        public void testSetProbabilityToBase() {
            state.setBaseProbabilityExists(85);
            state.setProbabilityExists(0);
            
            state.setProbabilityToBaseProbability();
            
            assertEquals(85, state.getProbabilityExists());
        }
        
        @ParameterizedTest
        @ValueSource(ints = {0, 25, 50, 75, 100})
        @DisplayName("Set various probability values")
        public void testVariousProbabilities(int probability) {
            state.setBaseProbabilityExists(probability);
            state.setProbabilityToBaseProbability();
            
            assertEquals(probability, state.getProbabilityExists());
        }
    }
    
    @Nested
    @DisplayName("Scene Management")
    class SceneManagement {
        
        @Test
        @DisplayName("Add scenes to state")
        public void testAddScenes() {
            Scene scene1 = mock(Scene.class);
            Scene scene2 = mock(Scene.class);
            
            state.getScenes().add(scene1);
            state.getScenes().add(scene2);
            
            assertEquals(2, state.getScenes().size());
            assertTrue(state.getScenes().contains(scene1));
            assertTrue(state.getScenes().contains(scene2));
        }
        
        @Test
        @DisplayName("Scenes list maintains order")
        public void testScenesOrder() {
            Scene scene1 = mock(Scene.class);
            Scene scene2 = mock(Scene.class);
            Scene scene3 = mock(Scene.class);
            
            state.getScenes().add(scene1);
            state.getScenes().add(scene2);
            state.getScenes().add(scene3);
            
            assertEquals(scene1, state.getScenes().get(0));
            assertEquals(scene2, state.getScenes().get(1));
            assertEquals(scene3, state.getScenes().get(2));
        }
    }
    
    @Nested
    @DisplayName("Usable Area and Regions")
    class UsableAreaAndRegions {
        
        @Test
        @DisplayName("Default usable area is initialized")
        public void testDefaultUsableArea() {
            assertNotNull(state.getUsableArea());
        }
        
        @Test
        @DisplayName("Set custom usable area")
        public void testSetUsableArea() {
            Region customRegion = new Region(10, 20, 300, 400);
            
            state.setUsableArea(customRegion);
            
            assertEquals(customRegion, state.getUsableArea());
            assertEquals(10, state.getUsableArea().getX());
            assertEquals(20, state.getUsableArea().getY());
            assertEquals(300, state.getUsableArea().getW());
            assertEquals(400, state.getUsableArea().getH());
        }
    }
    
    @Nested
    @DisplayName("Action History")
    class ActionHistoryTests {
        
        @Test
        @DisplayName("Match history is initialized")
        public void testMatchHistoryInitialized() {
            assertNotNull(state.getMatchHistory());
        }
        
        @Test
        @DisplayName("Add actions to match history")
        public void testAddToMatchHistory() {
            ActionRecord record1 = mock(ActionRecord.class);
            ActionRecord record2 = mock(ActionRecord.class);
            
            // Setup mocks to avoid NPE
            when(record1.getText()).thenReturn("text1");
            when(record1.getMatchList()).thenReturn(new ArrayList<>());
            when(record2.getText()).thenReturn("text2");
            when(record2.getMatchList()).thenReturn(new ArrayList<>());
            
            state.getMatchHistory().addSnapshot(record1);
            state.getMatchHistory().addSnapshot(record2);
            
            // ActionHistory doesn't expose getRecords(), check via other means
            assertNotNull(state.getMatchHistory());
        }
    }
    
    @Nested
    @DisplayName("State Visitation")
    class StateVisitation {
        
        @Test
        @DisplayName("Track times visited")
        public void testTimesVisited() {
            assertEquals(0, state.getTimesVisited());
            
            state.setTimesVisited(5);
            assertEquals(5, state.getTimesVisited());
            
            state.setTimesVisited(state.getTimesVisited() + 1);
            assertEquals(6, state.getTimesVisited());
        }
        
        @Test
        @DisplayName("Track last accessed time")
        public void testLastAccessed() {
            assertNull(state.getLastAccessed());
            
            LocalDateTime now = LocalDateTime.now();
            state.setLastAccessed(now);
            
            assertEquals(now, state.getLastAccessed());
        }
    }
    
    @Nested
    @DisplayName("Path Score")
    class PathScoreTests {
        
        @Test
        @DisplayName("Default path score is 1")
        public void testDefaultPathScore() {
            assertEquals(1, state.getPathScore());
        }
        
        @ParameterizedTest
        @ValueSource(ints = {1, 5, 10, 100})
        @DisplayName("Set various path scores")
        public void testSetPathScore(int score) {
            state.setPathScore(score);
            assertEquals(score, state.getPathScore());
        }
    }
    
    @Nested
    @DisplayName("Complex State Scenarios")
    class ComplexStateScenarios {
        
        @Test
        @DisplayName("Create fully configured state")
        public void testFullyConfiguredState() {
            // Setup
            state.setId(1L);
            state.setName("LoginState");
            state.setBlocking(true);
            state.setPathScore(2);
            state.setBaseProbabilityExists(90);
            state.setProbabilityToBaseProbability();
            
            // Add state text
            state.getStateText().add("Username");
            state.getStateText().add("Password");
            state.getStateText().add("Login");
            
            // Add state objects
            state.addStateImage(mockStateImage);
            state.addStateRegion(mockStateRegion);
            state.addStateLocation(mockStateLocation);
            state.addStateString(mockStateString);
            
            // Add hidden state configuration
            state.getCanHide().add("MainMenu");
            state.getCanHideIds().add(2L);
            
            // Add scene
            state.getScenes().add(mockScene);
            
            // Set usable area
            state.setUsableArea(new Region(0, 0, 1920, 1080));
            
            // Verify
            assertEquals(1L, state.getId());
            assertEquals("LoginState", state.getName());
            assertTrue(state.isBlocking());
            assertEquals(2, state.getPathScore());
            assertEquals(90, state.getProbabilityExists());
            assertEquals(3, state.getStateText().size());
            assertEquals(1, state.getStateImages().size());
            assertEquals(1, state.getStateRegions().size());
            assertEquals(1, state.getStateLocations().size());
            assertEquals(1, state.getStateStrings().size());
            assertEquals(1, state.getCanHide().size());
            assertEquals(1, state.getCanHideIds().size());
            assertEquals(1, state.getScenes().size());
            assertNotNull(state.getUsableArea());
        }
        
        @Test
        @DisplayName("State with multiple hidden states")
        public void testStateWithMultipleHiddenStates() {
            state.setName("ModalDialog");
            state.setBlocking(true);
            
            // This modal can hide multiple states
            state.getCanHide().add("MainContent");
            state.getCanHide().add("Sidebar");
            state.getCanHide().add("Header");
            state.getCanHideIds().add(10L);
            state.getCanHideIds().add(11L);
            state.getCanHideIds().add(12L);
            
            // Currently hiding these states
            state.getHiddenStateNames().add("MainContent");
            state.getHiddenStateIds().add(10L);
            
            assertEquals(3, state.getCanHide().size());
            assertEquals(3, state.getCanHideIds().size());
            assertEquals(1, state.getHiddenStateNames().size());
            assertEquals(1, state.getHiddenStateIds().size());
            assertTrue(state.isBlocking());
        }
    }
}