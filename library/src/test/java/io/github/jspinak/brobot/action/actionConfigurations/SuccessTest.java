package io.github.jspinak.brobot.action.actionConfigurations;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.internal.utility.ActionSuccessCriteria;
import io.github.jspinak.brobot.model.element.Region;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class SuccessTest {

    private ActionSuccessCriteria success;
    private ActionResult matches;
    private ActionOptions actionOptions;

    @BeforeEach
    void setUp() {
        success = new ActionSuccessCriteria();
        matches = mock(ActionResult.class);
        actionOptions = new ActionOptions();
    }

    @Test
    void testFindActionSuccess() {
        actionOptions.setAction(ActionOptions.Action.FIND);
        when(matches.isEmpty()).thenReturn(false);
        Predicate<ActionResult> criteria = success.getCriteria(actionOptions);
        assertTrue(criteria.test(matches));
    }

    @Test
    void testClickActionSuccess() {
        actionOptions.setAction(ActionOptions.Action.CLICK);
        when(matches.isEmpty()).thenReturn(false);
        Predicate<ActionResult> criteria = success.getCriteria(actionOptions);
        assertTrue(criteria.test(matches));
    }

    @Test
    void testDefineActionSuccess() {
        Region region = mock(Region.class);
        when(region.isDefined()).thenReturn(true);
        when(matches.getDefinedRegion()).thenReturn(region);
        actionOptions.setAction(ActionOptions.Action.DEFINE);
        when(matches.getDefinedRegion().isDefined()).thenReturn(true);
        Predicate<ActionResult> criteria = success.getCriteria(actionOptions);
        assertTrue(criteria.test(matches));
    }

    @Test
    void testTypeActionAlwaysSuccess() {
        actionOptions.setAction(ActionOptions.Action.TYPE);
        Predicate<ActionResult> criteria = success.getCriteria(actionOptions);
        assertTrue(criteria.test(matches));
    }

    @Test
    void testVanishActionSuccess() {
        actionOptions.setAction(ActionOptions.Action.VANISH);
        when(matches.isEmpty()).thenReturn(true);
        Predicate<ActionResult> criteria = success.getCriteria(actionOptions);
        assertTrue(criteria.test(matches));
    }

    @Test
    void testDragActionSuccess() {
        actionOptions.setAction(ActionOptions.Action.DRAG);
        when(matches.size()).thenReturn(2);
        Predicate<ActionResult> criteria = success.getCriteria(actionOptions);
        assertTrue(criteria.test(matches));
    }

    @Test
    void testClickUntilObjectsAppear() {
        actionOptions.setAction(ActionOptions.Action.CLICK_UNTIL);
        actionOptions.setClickUntil(ActionOptions.ClickUntil.OBJECTS_APPEAR);
        when(matches.isEmpty()).thenReturn(false);
        Predicate<ActionResult> criteria = success.getCriteria(actionOptions);
        assertTrue(criteria.test(matches));
    }

    @Test
    void testClickUntilObjectsVanish() {
        actionOptions.setAction(ActionOptions.Action.CLICK_UNTIL);
        actionOptions.setClickUntil(ActionOptions.ClickUntil.OBJECTS_VANISH);
        when(matches.isEmpty()).thenReturn(true);
        Predicate<ActionResult> criteria = success.getCriteria(actionOptions);
        assertTrue(criteria.test(matches));
    }

    @Test
    void testSetWithCustomSuccessCriteria() {
        // Setup
        Predicate<ActionResult> customCriteria = m -> false;
        actionOptions.setSuccessCriteria(customCriteria);

        // Action
        success.set(actionOptions, matches);

        // Verification
        // Verify that the setSuccess method was called with the argument 'false'.
        verify(matches).setSuccess(false);
    }

    @Test
    void testSetWithDefaultSuccessCriteria() {
        // Setup
        actionOptions.setAction(ActionOptions.Action.FIND);
        when(matches.isEmpty()).thenReturn(false);

        // Action
        success.set(actionOptions, matches);

        // Verification
        // Verify that the setSuccess method on the mock 'matches' was called exactly once with the argument 'true'.
        verify(matches).setSuccess(true);
    }
}