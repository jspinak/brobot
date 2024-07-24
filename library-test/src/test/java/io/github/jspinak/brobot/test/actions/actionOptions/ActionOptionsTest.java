package io.github.jspinak.brobot.test.actions.actionOptions;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
