package io.github.jspinak.brobot.action.internal.region;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateStore;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("DynamicRegionResolver Tests")
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Test incompatible with CI environment")
class DynamicRegionResolverTest extends BrobotTestBase {

    private DynamicRegionResolver resolver;
    
    @Mock
    private StateStore stateStore;
    
    @Mock
    private SearchRegionDependencyRegistry dependencyRegistry;
    
    @Mock
    private ActionResult actionResult;
    
    @Mock
    private StateImage stateImage;
    
    @Mock
    private StateLocation stateLocation;
    
    @Mock
    private StateRegion stateRegion;
    
    @Mock
    private State state;
    
    @Mock
    private SearchRegionOnObject searchRegionConfig;
    
    @Mock
    private MatchAdjustmentOptions adjustmentOptions;
    
    @Mock
    private ActionHistory matchHistory;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        resolver = new DynamicRegionResolver(stateStore, dependencyRegistry);
        
        // Set up default mock behavior for StateImage
        when(stateImage.getMatchHistory()).thenReturn(matchHistory);
        when(matchHistory.getTimesFound()).thenReturn(0);
    }
    
    @Test
    @DisplayName("Should update search regions for StateImage")
    void testUpdateSearchRegions_StateImage() {
        when(stateImage.getSearchRegionOnObject()).thenReturn(searchRegionConfig);
        when(stateImage.hasDefinedSearchRegion()).thenReturn(false);
        when(stateImage.getMatchHistory()).thenReturn(matchHistory);
        when(matchHistory.getTimesFound()).thenReturn(1);
        
        when(searchRegionConfig.getTargetStateName()).thenReturn("SourceState");
        when(searchRegionConfig.getTargetObjectName()).thenReturn("SourceObject");
        when(searchRegionConfig.getTargetType()).thenReturn(StateObject.Type.IMAGE);
        
        // Create StateObjectMetadata with proper name and owner
        StateObjectMetadata sourceMetadata = new StateObjectMetadata();
        sourceMetadata.setOwnerStateName("SourceState");
        sourceMetadata.setStateObjectName("SourceObject");
        
        Match sourceMatch = new Match.Builder()
            .setRegion(new Region(10, 10, 50, 50))
            .setStateObjectData(sourceMetadata)
            .build();
        
        when(actionResult.getMatchList()).thenReturn(Collections.singletonList(sourceMatch));
        
        resolver.updateSearchRegions(stateImage, actionResult);
        
        verify(stateImage).setFixedSearchRegion(any(Region.class));
    }
    
    @Test
    @DisplayName("Should update search regions for StateLocation")
    void testUpdateSearchRegions_StateLocation() {
        when(stateLocation.getSearchRegionOnObject()).thenReturn(searchRegionConfig);
        when(searchRegionConfig.getTargetStateName()).thenReturn("SourceState");
        when(searchRegionConfig.getTargetObjectName()).thenReturn("SourceObject");
        when(searchRegionConfig.getTargetType()).thenReturn(StateObject.Type.IMAGE);
        
        // Create StateObjectMetadata with proper name and owner
        StateObjectMetadata sourceMetadata = new StateObjectMetadata();
        sourceMetadata.setOwnerStateName("SourceState");
        sourceMetadata.setStateObjectName("SourceObject");
        
        Match sourceMatch = new Match.Builder()
            .setRegion(new Region(20, 20, 60, 60))
            .setStateObjectData(sourceMetadata)
            .build();
        
        when(actionResult.getMatchList()).thenReturn(Collections.singletonList(sourceMatch));
        
        resolver.updateSearchRegions(stateLocation, actionResult);
        
        verify(stateLocation).setLocation(any(Location.class));
    }
    
    @Test
    @DisplayName("Should not update search regions for StateRegion")
    void testUpdateSearchRegions_StateRegion() {
        resolver.updateSearchRegions(stateRegion, actionResult);
        
        verifyNoInteractions(stateRegion);
    }
    
    @Test
    @DisplayName("Should not update StateImage when no search region config")
    void testUpdateStateImageSearchRegion_NoConfig() {
        when(stateImage.getSearchRegionOnObject()).thenReturn(null);
        
        resolver.updateSearchRegions(stateImage, actionResult);
        
        verify(stateImage, never()).setFixedSearchRegion(any());
    }
    
    @Test
    @DisplayName("Should not update StateImage when search region already set")
    void testUpdateStateImageSearchRegion_AlreadySet() {
        when(stateImage.getSearchRegionOnObject()).thenReturn(searchRegionConfig);
        when(stateImage.hasDefinedSearchRegion()).thenReturn(true);
        
        resolver.updateSearchRegions(stateImage, actionResult);
        
        verify(stateImage, never()).setFixedSearchRegion(any());
    }
    
    @Test
    @DisplayName("Should update StateLocation with center of resolved region")
    void testUpdateStateLocationSearchRegion() {
        when(stateLocation.getSearchRegionOnObject()).thenReturn(searchRegionConfig);
        when(searchRegionConfig.getTargetStateName()).thenReturn("SourceState");
        when(searchRegionConfig.getTargetObjectName()).thenReturn("SourceObject");
        when(searchRegionConfig.getTargetType()).thenReturn(StateObject.Type.IMAGE);
        
        // Create StateObjectMetadata with proper name and owner
        StateObjectMetadata sourceMetadata = new StateObjectMetadata();
        sourceMetadata.setOwnerStateName("SourceState");
        sourceMetadata.setStateObjectName("SourceObject");
        
        Region sourceRegion = new Region(40, 40, 80, 80);
        Match sourceMatch = new Match.Builder()
            .setRegion(sourceRegion)
            .setStateObjectData(sourceMetadata)
            .build();
        
        when(actionResult.getMatchList()).thenReturn(Collections.singletonList(sourceMatch));
        
        resolver.updateSearchRegions(stateLocation, actionResult);
        
        // Center of region (40,40,80,80) is at (80,80)
        verify(stateLocation).setLocation(argThat(loc -> 
            loc.getCalculatedX() == 80 && loc.getCalculatedY() == 80
        ));
    }
    
    @Test
    @DisplayName("Should update search regions for multiple objects")
    void testUpdateSearchRegionsForObjects() {
        StateImage image1 = mock(StateImage.class);
        StateLocation location1 = mock(StateLocation.class);
        ActionHistory history1 = mock(ActionHistory.class);
        List<StateObject> objects = Arrays.asList(image1, location1);
        
        SearchRegionOnObject config1 = mock(SearchRegionOnObject.class);
        SearchRegionOnObject config2 = mock(SearchRegionOnObject.class);
        
        when(image1.getSearchRegionOnObject()).thenReturn(config1);
        when(image1.hasDefinedSearchRegion()).thenReturn(false);
        when(image1.getMatchHistory()).thenReturn(history1);
        when(history1.getTimesFound()).thenReturn(1);
        
        when(location1.getSearchRegionOnObject()).thenReturn(config2);
        
        when(config1.getTargetStateName()).thenReturn("State1");
        when(config1.getTargetObjectName()).thenReturn("Object1");
        when(config1.getTargetType()).thenReturn(StateObject.Type.IMAGE);
        
        when(config2.getTargetStateName()).thenReturn("State2");
        when(config2.getTargetObjectName()).thenReturn("Object2");
        when(config2.getTargetType()).thenReturn(StateObject.Type.IMAGE);
        
        // Create StateObjectMetadata with proper names and owners
        StateObjectMetadata metadata1 = new StateObjectMetadata();
        metadata1.setOwnerStateName("State1");
        metadata1.setStateObjectName("Object1");
        
        StateObjectMetadata metadata2 = new StateObjectMetadata();
        metadata2.setOwnerStateName("State2");
        metadata2.setStateObjectName("Object2");
        
        Match match1 = new Match.Builder()
            .setRegion(new Region(10, 10, 30, 30))
            .setStateObjectData(metadata1)
            .build();
        Match match2 = new Match.Builder()
            .setRegion(new Region(40, 40, 60, 60))
            .setStateObjectData(metadata2)
            .build();
        
        when(actionResult.getMatchList()).thenReturn(Arrays.asList(match1, match2));
        
        resolver.updateSearchRegionsForObjects(objects, actionResult);
        
        // Verify both objects were processed
        verify(image1).setFixedSearchRegion(any(Region.class));
        verify(location1).setLocation(any(Location.class));
    }
    
    @Test
    @DisplayName("Should register dependencies for objects")
    void testRegisterDependencies() {
        StateImage image = mock(StateImage.class);
        StateLocation location = mock(StateLocation.class);
        
        SearchRegionOnObject config1 = mock(SearchRegionOnObject.class);
        SearchRegionOnObject config2 = mock(SearchRegionOnObject.class);
        
        when(image.getSearchRegionOnObject()).thenReturn(config1);
        when(location.getSearchRegionOnObject()).thenReturn(config2);
        
        List<StateObject> objects = Arrays.asList(image, location);
        
        resolver.registerDependencies(objects);
        
        verify(dependencyRegistry).registerDependency(image, config1);
        verify(dependencyRegistry).registerDependency(location, config2);
    }
    
    @Test
    @DisplayName("Should handle null ActionResult gracefully")
    void testUpdateSearchRegions_NullActionResult() {
        resolver.updateSearchRegions(stateImage, null);
        
        verify(stateImage, never()).setFixedSearchRegion(any());
    }
    
    @Test
    @DisplayName("Should handle empty matches in ActionResult")
    void testUpdateSearchRegions_NoMatches() {
        when(stateImage.getSearchRegionOnObject()).thenReturn(searchRegionConfig);
        when(stateImage.hasDefinedSearchRegion()).thenReturn(false);
        when(stateImage.getMatchHistory()).thenReturn(matchHistory);
        when(matchHistory.getTimesFound()).thenReturn(1);
        
        when(searchRegionConfig.getTargetStateName()).thenReturn("TestState");
        when(searchRegionConfig.getTargetObjectName()).thenReturn("TestObject");
        when(searchRegionConfig.getTargetType()).thenReturn(StateObject.Type.IMAGE);
        
        when(actionResult.getMatchList()).thenReturn(Collections.emptyList());
        when(stateStore.getState("TestState")).thenReturn(Optional.empty());
        
        resolver.updateSearchRegions(stateImage, actionResult);
        
        verify(stateImage, never()).setFixedSearchRegion(any());
    }
}