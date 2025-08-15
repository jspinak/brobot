package io.github.jspinak.brobot.action.internal.utility;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
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

    @BeforeEach
    void setUp() {
        success = new ActionSuccessCriteria();
        matches = mock(ActionResult.class);
    }

    @Test
    void testFindActionSuccess() {
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        when(matches.isEmpty()).thenReturn(false);
        Predicate<ActionResult> criteria = success.getCriteria(findOptions);
        assertTrue(criteria.test(matches));
    }

    @Test
    void testClickActionSuccess() {
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        when(matches.isEmpty()).thenReturn(false);
        Predicate<ActionResult> criteria = success.getCriteria(clickOptions);
        assertTrue(criteria.test(matches));
    }

    @Test
    void testDefineActionSuccess() {
        Region region = mock(Region.class);
        when(region.isDefined()).thenReturn(true);
        when(matches.getDefinedRegion()).thenReturn(region);
        when(matches.getDefinedRegion().isDefined()).thenReturn(true);
        // DEFINE action uses PatternFindOptions
        PatternFindOptions defineOptions = new PatternFindOptions.Builder().build();
        Predicate<ActionResult> criteria = success.getCriteria(defineOptions);
        assertTrue(criteria.test(matches));
    }

    @Test
    void testTypeActionAlwaysSuccess() {
        TypeOptions typeOptions = new TypeOptions.Builder().build();
        Predicate<ActionResult> criteria = success.getCriteria(typeOptions);
        assertTrue(criteria.test(matches));
    }

    @Test
    void testVanishActionSuccess() {
        VanishOptions vanishOptions = new VanishOptions.Builder().build();
        when(matches.isEmpty()).thenReturn(true);
        Predicate<ActionResult> criteria = success.getCriteria(vanishOptions);
        assertTrue(criteria.test(matches));
    }

    @Test
    void testDragActionSuccess() {
        DragOptions dragOptions = new DragOptions.Builder().build();
        when(matches.size()).thenReturn(2);
        Predicate<ActionResult> criteria = success.getCriteria(dragOptions);
        assertTrue(criteria.test(matches));
    }

    @Test
    void testClickWithSuccessCriteriaObjectsAppear() {
        // Click with success criteria - objects should appear (not empty)
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setSuccessCriteria(matches -> !matches.isEmpty())
                .build();
        when(matches.isEmpty()).thenReturn(false);
        // Since we set a custom success criteria, it should be used
        assertTrue(clickOptions.getSuccessCriteria().test(matches));
    }

    @Test
    void testClickWithSuccessCriteriaObjectsVanish() {
        // Click with success criteria - objects should vanish (empty)
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setSuccessCriteria(matches -> matches.isEmpty())
                .build();
        when(matches.isEmpty()).thenReturn(true);
        // Since we set a custom success criteria, it should be used
        assertTrue(clickOptions.getSuccessCriteria().test(matches));
    }

    @Test
    void testSetWithCustomSuccessCriteria() {
        // Setup - ClickOptions with custom success criteria
        Predicate<ActionResult> customCriteria = m -> false;
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setSuccessCriteria(customCriteria)
                .build();

        // Action
        success.set(clickOptions, matches);

        // Verification
        // Verify that the setSuccess method was called with the argument 'false'.
        verify(matches).setSuccess(false);
    }

    @Test
    void testSetWithDefaultSuccessCriteria() {
        // Setup - PatternFindOptions with default success criteria
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        when(matches.isEmpty()).thenReturn(false);

        // Action
        success.set(findOptions, matches);

        // Verification
        // Verify that the setSuccess method on the mock 'matches' was called exactly once with the argument 'true'.
        verify(matches).setSuccess(true);
    }
}