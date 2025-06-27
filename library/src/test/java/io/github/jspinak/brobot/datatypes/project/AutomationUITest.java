package io.github.jspinak.brobot.datatypes.project;

import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.project.RunnerInterface;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the AutomationUI class.
 */
class AutomationUITest {

    @Test
    void testGetAndSetButtons() {
        RunnerInterface runnerInterface = new RunnerInterface();
        List<TaskButton> buttons = new ArrayList<>();
        TaskButton button1 = new TaskButton();
        button1.setId("btn1");
        buttons.add(button1);

        runnerInterface.setButtons(buttons);

        assertNotNull(runnerInterface.getButtons());
        assertEquals(1, runnerInterface.getButtons().size());
        assertEquals("btn1", runnerInterface.getButtons().get(0).getId());
    }
}