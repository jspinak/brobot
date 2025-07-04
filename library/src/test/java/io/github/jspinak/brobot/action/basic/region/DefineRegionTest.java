package io.github.jspinak.brobot.action.basic.region;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DefineRegion action without Spring dependencies.
 * Migrated from library-test module.
 */
@ExtendWith(MockitoExtension.class)
class DefineRegionTest {

    @Mock
    private DefineWithWindow defineWithWindow;
    
    @Mock
    private DefineWithMatch defineWithMatch;
    
    @Mock
    private DefineInsideAnchors defineInsideAnchors;
    
    @Mock
    private DefineOutsideAnchors defineOutsideAnchors;
    
    @Mock
    private DefineIncludingMatches defineIncludingMatches;

    private DefineRegion defineRegion;

    @BeforeEach
    void setUp() {
        defineRegion = new DefineRegion(
            defineWithWindow,
            defineWithMatch,
            defineInsideAnchors,
            defineOutsideAnchors,
            defineIncludingMatches
        );
    }

    @Test
    void getActionType_shouldReturnDefine() {
        assertEquals(ActionInterface.Type.DEFINE, defineRegion.getActionType());
    }

    @Test
    void perform_withFocusedWindow_shouldDelegateToDefineWithWindow() {
        // Setup
        DefineRegionOptions options = new DefineRegionOptions.Builder()
                .setDefineAs(DefineRegionOptions.DefineAs.FOCUSED_WINDOW)
                .build();
        
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(options);
        
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();

        // Action
        defineRegion.perform(actionResult, objectCollection);

        // Verification
        verify(defineWithWindow).perform(eq(actionResult), eq(objectCollection));
        verifyNoInteractions(defineWithMatch, defineInsideAnchors, defineOutsideAnchors, defineIncludingMatches);
    }

    @Test
    void perform_withMatch_shouldDelegateToDefineWithMatch() {
        // Setup
        DefineRegionOptions options = new DefineRegionOptions.Builder()
                .setDefineAs(DefineRegionOptions.DefineAs.MATCH)
                .build();
        
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(options);
        
        Pattern pattern = new Pattern.Builder()
                .setName("test-pattern")
                .build();
        
        StateImage stateImage = new StateImage.Builder()
                .addPattern(pattern)
                .build();
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();

        // Action
        defineRegion.perform(actionResult, objectCollection);

        // Verification
        verify(defineWithMatch).perform(eq(actionResult), eq(objectCollection));
        verifyNoInteractions(defineWithWindow, defineInsideAnchors, defineOutsideAnchors, defineIncludingMatches);
    }

    @Test
    void perform_withBelowMatch_shouldDelegateToDefineWithMatch() {
        // Setup
        DefineRegionOptions options = new DefineRegionOptions.Builder()
                .setDefineAs(DefineRegionOptions.DefineAs.BELOW_MATCH)
                .build();
        
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(options);
        
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();

        // Action
        defineRegion.perform(actionResult, objectCollection);

        // Verification
        verify(defineWithMatch).perform(eq(actionResult), eq(objectCollection));
        verifyNoInteractions(defineWithWindow, defineInsideAnchors, defineOutsideAnchors, defineIncludingMatches);
    }

    @Test
    void perform_withAboveMatch_shouldDelegateToDefineWithMatch() {
        // Setup
        DefineRegionOptions options = new DefineRegionOptions.Builder()
                .setDefineAs(DefineRegionOptions.DefineAs.ABOVE_MATCH)
                .build();
        
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(options);
        
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();

        // Action
        defineRegion.perform(actionResult, objectCollection);

        // Verification
        verify(defineWithMatch).perform(eq(actionResult), eq(objectCollection));
        verifyNoInteractions(defineWithWindow, defineInsideAnchors, defineOutsideAnchors, defineIncludingMatches);
    }

    @Test
    void perform_withLeftOfMatch_shouldDelegateToDefineWithMatch() {
        // Setup
        DefineRegionOptions options = new DefineRegionOptions.Builder()
                .setDefineAs(DefineRegionOptions.DefineAs.LEFT_OF_MATCH)
                .build();
        
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(options);
        
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();

        // Action
        defineRegion.perform(actionResult, objectCollection);

        // Verification
        verify(defineWithMatch).perform(eq(actionResult), eq(objectCollection));
        verifyNoInteractions(defineWithWindow, defineInsideAnchors, defineOutsideAnchors, defineIncludingMatches);
    }

    @Test
    void perform_withRightOfMatch_shouldDelegateToDefineWithMatch() {
        // Setup
        DefineRegionOptions options = new DefineRegionOptions.Builder()
                .setDefineAs(DefineRegionOptions.DefineAs.RIGHT_OF_MATCH)
                .build();
        
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(options);
        
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();

        // Action
        defineRegion.perform(actionResult, objectCollection);

        // Verification
        verify(defineWithMatch).perform(eq(actionResult), eq(objectCollection));
        verifyNoInteractions(defineWithWindow, defineInsideAnchors, defineOutsideAnchors, defineIncludingMatches);
    }

    @Test
    void perform_withInsideAnchors_shouldDelegateToDefineInsideAnchors() {
        // Setup
        DefineRegionOptions options = new DefineRegionOptions.Builder()
                .setDefineAs(DefineRegionOptions.DefineAs.INSIDE_ANCHORS)
                .build();
        
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(options);
        
        Pattern anchor1 = new Pattern.Builder()
                .setName("anchor1")
                .build();
        Pattern anchor2 = new Pattern.Builder()
                .setName("anchor2")
                .build();
        
        StateImage stateImage = new StateImage.Builder()
                .addPattern(anchor1)
                .addPattern(anchor2)
                .build();
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();

        // Action
        defineRegion.perform(actionResult, objectCollection);

        // Verification
        verify(defineInsideAnchors).perform(eq(actionResult), eq(objectCollection));
        verifyNoInteractions(defineWithWindow, defineWithMatch, defineOutsideAnchors, defineIncludingMatches);
    }

    @Test
    void perform_withOutsideAnchors_shouldDelegateToDefineOutsideAnchors() {
        // Setup
        DefineRegionOptions options = new DefineRegionOptions.Builder()
                .setDefineAs(DefineRegionOptions.DefineAs.OUTSIDE_ANCHORS)
                .build();
        
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(options);
        
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();

        // Action
        defineRegion.perform(actionResult, objectCollection);

        // Verification
        verify(defineOutsideAnchors).perform(eq(actionResult), eq(objectCollection));
        verifyNoInteractions(defineWithWindow, defineWithMatch, defineInsideAnchors, defineIncludingMatches);
    }

    @Test
    void perform_withIncludingMatches_shouldDelegateToDefineIncludingMatches() {
        // Setup
        DefineRegionOptions options = new DefineRegionOptions.Builder()
                .setDefineAs(DefineRegionOptions.DefineAs.INCLUDING_MATCHES)
                .build();
        
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(options);
        
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();

        // Add some existing matches to the result
        actionResult.add(new Match.Builder()
                .setRegion(10, 10, 20, 20)
                .build());
        actionResult.add(new Match.Builder()
                .setRegion(50, 50, 20, 20)
                .build());

        // Action
        defineRegion.perform(actionResult, objectCollection);

        // Verification
        verify(defineIncludingMatches).perform(eq(actionResult), eq(objectCollection));
        verifyNoInteractions(defineWithWindow, defineWithMatch, defineInsideAnchors, defineOutsideAnchors);
    }

    @Test
    void perform_withNonDefineRegionOptions_shouldThrowException() {
        // Setup
        ClickOptions clickOptions = new ClickOptions.Builder()
                .build();
        
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(clickOptions); // Wrong type
        
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();

        // Action & Verification
        assertThrows(IllegalArgumentException.class, () -> 
            defineRegion.perform(actionResult, objectCollection)
        );
    }

    @Test
    void perform_withNullDefineAs_shouldThrowException() {
        // Setup
        DefineRegionOptions options = new DefineRegionOptions.Builder()
                .setDefineAs(null)
                .build();
        
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(options);
        
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();

        // Action & Verification
        assertThrows(IllegalArgumentException.class, () -> 
            defineRegion.perform(actionResult, objectCollection)
        );
    }
}