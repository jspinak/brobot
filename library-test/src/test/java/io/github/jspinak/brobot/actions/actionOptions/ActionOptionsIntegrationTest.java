package io.github.jspinak.brobot.actions.actionOptions;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.action.ActionOptions;

import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ActionOptionsIntegrationTest {

    @Test
    void testCreationInSpringContext() {
        // Verifies that a complex ActionOptions object can be built in a Spring test environment.
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .setFind(ActionOptions.Find.ALL)
                .setMinSimilarity(0.8)
                .setMaxWait(5)
                .setMoveMouseAfterAction(true)
                .setMoveMouseAfterActionTo(new Location(100, 200))
                .addSearchRegion(new Region(0, 0, 50, 50))
                .build();

        assertNotNull(options, "ActionOptions object should not be null when created in a Spring context.");
        assertEquals(ActionOptions.Action.DRAG, options.getAction());
        assertEquals(100, options.getMoveMouseAfterActionTo().getX());
        assertFalse(options.getSearchRegions().isEmpty());
    }

    @Test
    void testDefaultPauseLogicInSpringContext() {
        // Verifies the default pause logic works as expected within the context.
        // For a DRAG action, pauseAfterMouseDown should be Settings.DelayValue
        ActionOptions dragOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .build();

        // CORRECTED ASSERTION: Check against the library constant directly, not a magic number.
        assertEquals(Settings.DelayValue, dragOptions.getPauseAfterMouseDown());

        // For a CLICK action, pauseAfterMouseDown should be BrobotSettings.pauseAfterMouseDown.
        ActionOptions clickOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .build();

        // CORRECTED ASSERTION: Check against the application's settings constant directly.
        assertEquals(FrameworkSettings.pauseAfterMouseDown, clickOptions.getPauseAfterMouseDown());
    }
}
