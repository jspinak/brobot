package io.github.jspinak.brobot.action.actionOptions;

import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.action.ActionOptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActionOptionsTest {

    @Test
    void initialPauseValuesForClick() {
        ActionOptions click = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .build();
        assertEquals(0.0, click.getPauseBeforeMouseDown());
        assertEquals(0.0, click.getPauseAfterMouseDown());
        assertEquals(0.0, click.getPauseBeforeMouseUp());
        assertEquals(0.0, click.getPauseAfterMouseUp());
    }

    @Test
    void initialPauseValuesForDrag() {
        ActionOptions click = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .build();
        assertEquals(0.0, click.getPauseBeforeMouseDown());
        assertEquals(0.3, click.getPauseAfterMouseDown());
        assertEquals(0.3, click.getPauseBeforeMouseUp());
        assertEquals(0.0, click.getPauseAfterMouseUp());
    }

    @Test
    void initialPauseValuesForDrag2() {
        ActionOptions click = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .setPauseAfterMouseDown(0.0)
                .build();
        assertEquals(0.0, click.getPauseBeforeMouseDown());
        assertEquals(0.0, click.getPauseAfterMouseDown());
        assertEquals(0.3, click.getPauseBeforeMouseUp());
        assertEquals(0.0, click.getPauseAfterMouseUp());
    }

    @Test
    void testBuilder_DefaultValues() {
        // When building with no options set, default values should be applied.
        ActionOptions options = new ActionOptions.Builder().build();

        assertEquals(ActionOptions.Action.FIND, options.getAction(), "Default action should be FIND.");
        assertEquals(ActionOptions.Find.FIRST, options.getFind(), "Default find should be FIRST.");
        assertEquals(1, options.getMaxTimesToRepeatActionSequence(), "Default repeat sequence should be 1.");
        assertEquals(0, options.getMaxWait(), "Default maxWait should be 0.");
    }

    @Test
    void testBuilder_SetAllValues() {
        // This test verifies that setting various options in the builder correctly transfers them to the final object.
        Position newPos = new Position(20, 80);
        Region newRegion = new Region(10,10,10,10);

        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK_UNTIL)
                .setFind(ActionOptions.Find.BEST)
                .setMinSimilarity(0.95)
                .setMaxWait(10.5)
                .setPauseAfterEnd(2.0)
                .setTargetPosition(newPos)
                .addSearchRegion(newRegion)
                .setDefineAs(ActionOptions.DefineAs.OUTSIDE_ANCHORS)
                .setAbsoluteWidth(500)
                .build();

        assertEquals(ActionOptions.Action.CLICK_UNTIL, options.getAction());
        assertEquals(ActionOptions.Find.BEST, options.getFind());
        assertEquals(0.95, options.getSimilarity());
        assertEquals(10.5, options.getMaxWait());
        assertEquals(2.0, options.getPauseAfterEnd());
        assertEquals(newPos, options.getTargetPosition());
        assertTrue(options.getSearchRegions().getRegions().contains(newRegion));
        assertEquals(ActionOptions.DefineAs.OUTSIDE_ANCHORS, options.getDefineAs());
        assertEquals(500, options.getAbsoluteW());
    }

    @Test
    void testPauseLogic_ExplicitOverrides() {
        // Tests that explicitly setting a pause value overrides any default logic.
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setPauseBeforeMouseDown(5.0) // Explicitly set
                .build();

        assertEquals(5.0, options.getPauseBeforeMouseDown(), "Explicit pause should override defaults.");
    }

    @Test
    void testAddFindLogic() {
        // Tests the logic for creating a sequence of Find actions.
        ActionOptions.Builder builder = new ActionOptions.Builder()
                .setFind(ActionOptions.Find.COLOR); // Initial find is COLOR

        // The first call to addFind should include the initial find.
        builder.addFind(ActionOptions.Find.HISTOGRAM);

        ActionOptions options1 = builder.build();
        assertEquals(2, options1.getFindActions().size());
        assertEquals(ActionOptions.Find.COLOR, options1.getFindActions().get(0));
        assertEquals(ActionOptions.Find.HISTOGRAM, options1.getFindActions().get(1));

        // A subsequent call should just add to the list.
        builder.addFind(ActionOptions.Find.MOTION);
        ActionOptions options2 = builder.build();
        assertEquals(3, options2.getFindActions().size());
        assertEquals(ActionOptions.Find.MOTION, options2.getFindActions().get(2));
    }
}