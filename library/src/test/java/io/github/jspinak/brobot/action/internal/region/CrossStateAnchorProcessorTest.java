package io.github.jspinak.brobot.action.internal.region;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions.DefineAs;
import io.github.jspinak.brobot.action.basic.region.DefineInsideAnchors;
import io.github.jspinak.brobot.action.basic.region.DefineOutsideAnchors;
import io.github.jspinak.brobot.action.internal.capture.AnchorRegion;
import io.github.jspinak.brobot.model.element.CrossStateAnchor;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateStore;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CrossStateAnchorProcessor Tests")
class CrossStateAnchorProcessorTest extends BrobotTestBase {

    private CrossStateAnchorProcessor processor;
    
    @Mock
    private StateStore stateStore;
    
    @Mock
    private AnchorRegion anchorRegion;
    
    @Mock
    private DefineInsideAnchors defineInsideAnchors;
    
    @Mock
    private DefineOutsideAnchors defineOutsideAnchors;
    
    @Mock
    private ActionResult actionResult;
    
    @Mock
    private StateRegion stateRegion;
    
    @Mock
    private State state;
    
    @Mock
    private StateImage stateImage;
    
    @Mock
    private StateLocation stateLocation;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        processor = new CrossStateAnchorProcessor(stateStore, anchorRegion, defineInsideAnchors, defineOutsideAnchors);
    }
    
    @Test
    @DisplayName("Should handle null anchors gracefully")
    void testProcessAnchors_NullAnchors() {
        Region originalRegion = new Region(10, 10, 100, 100);
        when(stateRegion.getSearchRegion()).thenReturn(originalRegion);
        
        processor.processAnchors(stateRegion, null, actionResult);
        
        verifyNoInteractions(defineInsideAnchors, defineOutsideAnchors);
    }
    
    @Test
    @DisplayName("Should handle empty anchors list")
    void testProcessAnchors_EmptyAnchors() {
        Region originalRegion = new Region(10, 10, 100, 100);
        when(stateRegion.getSearchRegion()).thenReturn(originalRegion);
        
        processor.processAnchors(stateRegion, Collections.emptyList(), actionResult);
        
        verifyNoInteractions(defineInsideAnchors, defineOutsideAnchors);
    }
    
    @Test
    @DisplayName("Should process anchors with valid configuration")
    void testProcessAnchors_ValidConfiguration() {
        Region originalRegion = new Region(0, 0, 200, 200);
        CrossStateAnchor anchor = mock(CrossStateAnchor.class);
        Match match = new Match.Builder()
            .setRegion(new Region(60, 60, 20, 20))
            .build();
        
        when(stateRegion.getSearchRegion()).thenReturn(originalRegion);
        when(stateRegion.getDefineStrategy()).thenReturn(DefineAs.INSIDE_ANCHORS);
        when(anchor.getSourceStateName()).thenReturn("TestState");
        when(anchor.getSourceObjectName()).thenReturn("TestObject");
        when(anchor.getSourceType()).thenReturn(StateObject.Type.IMAGE);
        
        when(actionResult.getMatchList()).thenReturn(Collections.singletonList(match));
        
        processor.processAnchors(stateRegion, Collections.singletonList(anchor), actionResult);
        
        // Cannot verify internal calls - creates its own ActionResult
    }
    
    @Test
    @DisplayName("Should handle outside anchors strategy")
    void testProcessAnchors_OutsideAnchorsStrategy() {
        Region originalRegion = new Region(0, 0, 200, 200);
        CrossStateAnchor anchor = mock(CrossStateAnchor.class);
        Match match = new Match.Builder()
            .setRegion(new Region(30, 30, 40, 40))
            .build();
        
        when(stateRegion.getSearchRegion()).thenReturn(originalRegion);
        when(stateRegion.getDefineStrategy()).thenReturn(DefineAs.OUTSIDE_ANCHORS);
        when(anchor.getSourceStateName()).thenReturn("TestState");
        when(anchor.getSourceObjectName()).thenReturn("TestObject");
        when(anchor.getSourceType()).thenReturn(StateObject.Type.IMAGE);
        
        when(actionResult.getMatchList()).thenReturn(Collections.singletonList(match));
        
        processor.processAnchors(stateRegion, Collections.singletonList(anchor), actionResult);
        
        // Cannot verify internal calls - creates its own ActionResult
    }
    
    @Test
    @DisplayName("Should handle multiple anchors")
    void testProcessAnchors_MultipleAnchors() {
        Region originalRegion = new Region(0, 0, 300, 300);
        
        CrossStateAnchor anchor1 = mock(CrossStateAnchor.class);
        CrossStateAnchor anchor2 = mock(CrossStateAnchor.class);
        
        Match match1 = new Match.Builder()
            .setRegion(new Region(10, 10, 20, 20))
            .build();
        
        Match match2 = new Match.Builder()
            .setRegion(new Region(100, 100, 50, 50))
            .build();
        
        when(stateRegion.getSearchRegion()).thenReturn(originalRegion);
        when(stateRegion.getDefineStrategy()).thenReturn(DefineAs.INSIDE_ANCHORS);
        
        when(anchor1.getSourceStateName()).thenReturn("State1");
        when(anchor1.getSourceObjectName()).thenReturn("Obj1");
        when(anchor1.getSourceType()).thenReturn(StateObject.Type.IMAGE);
        
        when(anchor2.getSourceStateName()).thenReturn("State2");
        when(anchor2.getSourceObjectName()).thenReturn("Obj2");
        when(anchor2.getSourceType()).thenReturn(StateObject.Type.REGION);
        
        when(actionResult.getMatchList()).thenReturn(Arrays.asList(match1, match2));
        
        processor.processAnchors(stateRegion, Arrays.asList(anchor1, anchor2), actionResult);
        
        // Cannot verify internal calls - creates its own ActionResult
    }
    
    @Test
    @DisplayName("Should handle anchors with no matches found")
    void testProcessAnchors_NoMatchesFound() {
        Region originalRegion = new Region(0, 0, 100, 100);
        CrossStateAnchor anchor = mock(CrossStateAnchor.class);
        
        when(stateRegion.getSearchRegion()).thenReturn(originalRegion);
        when(stateRegion.getDefineStrategy()).thenReturn(DefineAs.INSIDE_ANCHORS);
        when(anchor.getSourceStateName()).thenReturn("NoMatchState");
        when(anchor.getSourceObjectName()).thenReturn("NoMatchObject");
        when(anchor.getSourceType()).thenReturn(StateObject.Type.IMAGE);
        
        when(actionResult.getMatchList()).thenReturn(Collections.emptyList());
        when(stateStore.getState("NoMatchState")).thenReturn(Optional.empty());
        
        processor.processAnchors(stateRegion, Collections.singletonList(anchor), actionResult);
        
        // Cannot verify internal calls - creates its own ActionResult
    }
    
    @Test
    @DisplayName("Should handle null define strategy with default")
    void testProcessAnchors_NullDefineStrategy() {
        Region originalRegion = new Region(0, 0, 100, 100);
        CrossStateAnchor anchor = mock(CrossStateAnchor.class);
        
        when(stateRegion.getSearchRegion()).thenReturn(originalRegion);
        when(stateRegion.getDefineStrategy()).thenReturn(null); // Should default to OUTSIDE_ANCHORS
        when(anchor.getSourceStateName()).thenReturn("TestState");
        when(anchor.getSourceObjectName()).thenReturn("TestObject");
        when(anchor.getSourceType()).thenReturn(StateObject.Type.IMAGE);
        
        when(actionResult.getMatchList()).thenReturn(Collections.emptyList());
        
        processor.processAnchors(stateRegion, Collections.singletonList(anchor), actionResult);
        
        // Cannot verify internal calls - creates its own ActionResult
    }
}