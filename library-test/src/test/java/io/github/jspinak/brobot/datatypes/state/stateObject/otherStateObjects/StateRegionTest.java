package io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects;

import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StateRegionTest {

    private static StateRegion stateRegion;

    @BeforeAll
    public static void setUp() {
        stateRegion = new StateRegion.Builder()
                .setName("testRegion")
                .setSearchRegion(new Region(10, 20, 30, 40))
                .setOwnerStateName("testState")
                .setProbabilityExists(80)
                .setTimesActedOn(3)
                .build();
    }

    @Test
    public void testBuilder() {
        assertEquals("testRegion", stateRegion.getName());
        assertEquals(10, stateRegion.x());
        assertEquals(20, stateRegion.y());
        assertEquals(30, stateRegion.w());
        assertEquals(40, stateRegion.h());
        assertEquals("testState", stateRegion.getOwnerStateName());
        assertEquals(80, stateRegion.getProbabilityExists());
        assertEquals(3, stateRegion.getTimesActedOn());
    }

    @Test
    public void testGetIdAsString() {
        String id = stateRegion.getIdAsString();
        assertTrue(id.contains("REGION"));
        assertTrue(id.contains("testRegion"));
    }

    @Test
    public void testDefined() {
        assertTrue(stateRegion.defined());
        StateRegion undefinedRegion = new StateRegion.Builder().build();
        assertFalse(undefinedRegion.defined());
    }

    @Test
    public void testAddTimesActedOn() {
        StateRegion newRegion = new StateRegion.Builder().setTimesActedOn(3).build();
        newRegion.addTimesActedOn();
        assertEquals(4, newRegion.getTimesActedOn());
    }

    @Test
    public void testAsObjectCollection() {
        ObjectCollection objectCollection = stateRegion.asObjectCollection();
        assertFalse(objectCollection.getStateRegions().isEmpty());
        assertEquals(1, objectCollection.getStateRegions().size());
        assertEquals(stateRegion, objectCollection.getStateRegions().get(0));
    }

    @Test
    void addSnapshot() {
        StateRegion newRegion = new StateRegion.Builder().build();
        MatchSnapshot snapshot = new MatchSnapshot();
        newRegion.addSnapshot(snapshot);
        assertFalse(newRegion.getMatchHistory().getSnapshots().isEmpty());
    }
}
