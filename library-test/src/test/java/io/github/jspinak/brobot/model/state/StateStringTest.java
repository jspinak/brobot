package io.github.jspinak.brobot.model.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.jspinak.brobot.model.element.Region;

@SpringBootTest
public class StateStringTest {

    @BeforeAll
    static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    public void testBuilder() {
        StateString stateString =
                new StateString.Builder()
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
        StateString stateString =
                new StateString.Builder().setTimesActedOn(2).setString("test").build();
        stateString.addTimesActedOn();
        assertEquals(3, stateString.getTimesActedOn());
    }
}
