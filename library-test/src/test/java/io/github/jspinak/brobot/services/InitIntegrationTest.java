package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetAllProfiles;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetKMeansProfiles;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.manageStates.StateManagementService;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InitIntegrationTest {

    @Autowired
    private Init init;
    
    @MockBean
    private AllStatesInProjectService allStatesInProjectService;
    
    @MockBean
    private SetAllProfiles setAllProfiles;
    
    @MockBean
    private SetKMeansProfiles setKMeansProfiles;
    
    @MockBean
    private StateManagementService stateManagementService;
    
    @MockBean
    private StateTransitionsInProjectService stateTransitionsInProjectService;
    
    @Autowired
    private StateTransitionsRepository stateTransitionsRepository;
    
    @BeforeEach
    void setUp() {
        // Clear repository before each test
        stateTransitionsRepository.emptyRepos();
        // Reset BrobotSettings
        BrobotSettings.initProfilesForDynamicImages = false;
        BrobotSettings.initProfilesForStaticfImages = false;
    }
    
    @Test
    @Order(1)
    void testSpringContextLoads() {
        assertNotNull(init, "Init service should be autowired");
    }
    
    @Test
    @Order(2)
    void testSetBundlePathAndPreProcessImages() {
        // Create test states with images
        StateImage stateImage1 = createTestStateImage("image1");
        StateImage stateImage2 = createTestStateImage("image2");
        
        State state1 = new State.Builder("State1")
                .withImages(stateImage1)
                .build();
        State state2 = new State.Builder("State2")
                .withImages(stateImage2)
                .build();
        
        when(allStatesInProjectService.getAllStates()).thenReturn(Arrays.asList(state1, state2));
        
        // Execute
        init.setBundlePathAndPreProcessImages("/test/path");
        
        // Verify
        verify(setAllProfiles, times(2)).setMatsAndColorProfiles(any(StateImage.class));
        assertEquals(1, stateImage1.getIndex());
        assertEquals(2, stateImage2.getIndex());
    }
    
    @Test
    @Order(3)
    void testPreProcessImagesWithDynamicImageSettings() {
        BrobotSettings.initProfilesForDynamicImages = true;
        
        StateImage dynamicImage = createTestStateImage("dynamic");
        dynamicImage.setDynamic(true);
        
        State state = new State.Builder("DynamicState")
                .withImages(dynamicImage)
                .build();
        
        when(allStatesInProjectService.getAllStates()).thenReturn(List.of(state));
        
        init.setBundlePathAndPreProcessImages("/test/path");
        
        verify(setKMeansProfiles, times(1)).setProfiles(dynamicImage);
    }
    
    @Test
    @Order(4)
    void testPreProcessImagesWithStaticImageSettings() {
        BrobotSettings.initProfilesForStaticfImages = true;
        
        StateImage staticImage = createTestStateImage("static");
        staticImage.setDynamic(false);
        
        State state = new State.Builder("StaticState")
                .withImages(staticImage)
                .build();
        
        when(allStatesInProjectService.getAllStates()).thenReturn(List.of(state));
        
        init.setBundlePathAndPreProcessImages("/test/path");
        
        verify(setKMeansProfiles, times(1)).setProfiles(staticImage);
    }
    
    @Test
    @Order(5)
    void testInitializeStateStructureWithNoStates() {
        when(allStatesInProjectService.onlyTheUnknownStateExists()).thenReturn(true);
        
        init.initializeStateStructure();
        
        verify(stateManagementService, never()).convertAllStateTransitions(any());
        verify(stateTransitionsInProjectService, never()).setupRepo();
    }
    
    @Test
    @Order(6)
    void testInitializeStateStructureWithStates() {
        when(allStatesInProjectService.onlyTheUnknownStateExists()).thenReturn(false);
        
        StateTransitions transitions = new StateTransitions();
        transitions.setStateId(1L);
        when(stateTransitionsInProjectService.getAllStateTransitions()).thenReturn(List.of(transitions));
        
        init.initializeStateStructure();
        
        verify(stateManagementService).convertAllStateTransitions(any());
        verify(stateTransitionsInProjectService).setupRepo();
    }
    
    @Test
    @Order(7)
    void testPopulateCanHideWithStateIds() {
        // Create states with canHide relationships
        State state1 = new State.Builder("State1").build();
        state1.setId(1L);
        state1.getCanHide().add("State2");
        
        State state2 = new State.Builder("State2").build();
        state2.setId(2L);
        
        when(allStatesInProjectService.getAllStates()).thenReturn(Arrays.asList(state1, state2));
        when(allStatesInProjectService.getState("State2")).thenReturn(Optional.of(state2));
        when(allStatesInProjectService.onlyTheUnknownStateExists()).thenReturn(false);
        
        init.initializeStateStructure();
        
        assertTrue(state1.getCanHideIds().contains(2L), "State1 should have State2's ID in canHideIds");
    }
    
    @Test
    @Order(8)
    void testAddImagePath() {
        // This test verifies the add method doesn't throw exceptions
        assertDoesNotThrow(() -> init.add("/additional/path"));
    }
    
    private StateImage createTestStateImage(String name) {
        BufferedImage dummyImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Pattern pattern = new Pattern(dummyImage);
        pattern.setName(name);
        
        StateImage stateImage = new StateImage.Builder()
                .addPattern(pattern)
                .build();
        stateImage.setName(name);
        return stateImage;
    }
}