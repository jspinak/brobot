package io.github.jspinak.brobot.datatypes.project;

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
        AutomationUI automationUI = new AutomationUI();
        List<Button> buttons = new ArrayList<>();
        Button button1 = new Button();
        button1.setId("btn1");
        buttons.add(button1);

        automationUI.setButtons(buttons);

        assertNotNull(automationUI.getButtons());
        assertEquals(1, automationUI.getButtons().size());
        assertEquals("btn1", automationUI.getButtons().get(0).getId());
    }
}