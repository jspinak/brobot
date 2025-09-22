package io.github.jspinak.brobot.model.state;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Positions;

@SpringBootTest
public class StateLocationTest {

    private static StateLocation stateLocation;

    @BeforeAll
    public static void setUp() {
        stateLocation =
                new StateLocation.Builder()
                        .setName("testLocation")
                        .setLocation(10, 20)
                        .setOwnerStateName("testState")
                        .setMockFindStochasticModifier(90)
                        .setTimesActedOn(5)
                        .setPosition(Positions.Name.TOPLEFT)
                        .build();
    }

    @Test
    public void testBuilder() {
        assertEquals("testLocation", stateLocation.getName());
        assertEquals(10, stateLocation.getLocation().getX());
        assertEquals(20, stateLocation.getLocation().getY());
        assertEquals("testState", stateLocation.getOwnerStateName());
        assertEquals(90, stateLocation.getProbabilityExists());
        assertEquals(5, stateLocation.getTimesActedOn());
    }

    @Test
    public void testGetIdAsString() {
        String id = stateLocation.getIdAsString();
        assertTrue(id.contains("LOCATION"));
        assertTrue(id.contains("testLocation"));
    }

    @Test
    public void testDefined() {
        assertTrue(stateLocation.defined());
        StateLocation undefinedLocation = new StateLocation.Builder().build();
        assertFalse(undefinedLocation.defined());
    }

    @Test
    public void testAddTimesActedOn() {
        StateLocation newLocation = new StateLocation.Builder().setTimesActedOn(5).build();
        newLocation.addTimesActedOn();
        assertEquals(6, newLocation.getTimesActedOn());
    }

    @Test
    public void testAsObjectCollection() {
        ObjectCollection objectCollection = stateLocation.asObjectCollection();
        assertFalse(objectCollection.getStateLocations().isEmpty());
        assertEquals(1, objectCollection.getStateLocations().size());
        assertEquals(stateLocation, objectCollection.getStateLocations().get(0));
    }

    @Test
    public void testToString() {
        String str = stateLocation.toString();
        assertTrue(str.contains("testLocation"));
        assertTrue(str.contains("testState"));
    }
}
