package io.github.jspinak.brobot.model.state;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StateStringTest {

    @Test
    public void testBuilder() {
        StateString stateString = new StateString.Builder()
                .setName("testString")
                .setSearchRegion(new Region(5, 5, 15, 15))
                .setOwnerStateName("owner")
                .setTimesActedOn(2)
                .setString("test")
                .build();

        assertEquals("testString", stateString.getName());
        assertEquals(5, stateString.getSearchRegion().getX());
        assertEquals("owner", stateString.getOwnerStateName());
        assertEquals(2, stateString.getTimesActedOn());
        assertEquals("test", stateString.getString());
    }

    @Test
    public void testGetId() {
        StateString stateString = new StateString.Builder().setString("test").build();
        assertTrue(stateString.getId().contains("STRING"));
        assertTrue(stateString.getId().contains("test"));
    }

    @Test
    public void testDefined() {
        StateString definedString = new StateString.Builder().setString("defined").build();
        assertTrue(definedString.defined());
    }

    @Test
    public void testInNullState() {
        StateString nullStateString = new StateString.InNullState().withString("test");
        assertEquals("null", nullStateString.getOwnerStateName());
        assertEquals("test", nullStateString.getString());
    }

    @Test
    public void testAddTimesActedOn() {
        StateString stateString = new StateString.Builder().setTimesActedOn(2).setString("test").build();
        stateString.addTimesActedOn();
        assertEquals(3, stateString.getTimesActedOn());
    }
}
