package io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.special.StateText;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StateTextTest {

    @Test
    public void testBuilder() {
        StateText stateText = new StateText.Builder()
                .setName("testText")
                .setSearchRegion(new Region(1, 2, 3, 4))
                .setOwnerStateName("textOwner")
                .setText("some text")
                .build();

        assertEquals("testText", stateText.getName());
        assertEquals(1, stateText.getSearchRegion().getX());
        assertEquals("textOwner", stateText.getOwnerStateName());
        assertEquals("some text", stateText.getText());
    }

    @Test
    public void testGetId() {
        StateText stateText = new StateText.Builder().setText("id text").build();
        assertTrue(stateText.getId().contains("TEXT"));
        assertTrue(stateText.getId().contains("id text"));
    }

    @Test
    public void testDefined() {
        StateText definedText = new StateText.Builder().setText("defined").build();
        StateText undefinedText = new StateText.Builder().setText("").build();
        StateText nullText = new StateText.Builder().build();

        assertTrue(definedText.defined());
        assertFalse(undefinedText.defined());
        assertFalse(nullText.defined());
    }
}
