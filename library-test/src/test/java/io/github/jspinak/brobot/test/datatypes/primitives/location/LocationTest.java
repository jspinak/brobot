package io.github.jspinak.brobot.test.datatypes.primitives.location;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    private final Region region = new Region(0, 50, 100, 20);
    private final Location locationDefinedByRegion = new Location(region, Positions.Name.BOTTOMLEFT);

    @Test
    void getSikuliLocationFromLocationDefinedByRegion() {
        org.sikuli.script.Location sikuliLocation = locationDefinedByRegion.sikuli();
        System.out.format("%d %d %d %d", sikuliLocation.x, sikuliLocation.y, locationDefinedByRegion.getX(), locationDefinedByRegion.getY());
        assertEquals(sikuliLocation.x, locationDefinedByRegion.getX());
        assertEquals(sikuliLocation.y, locationDefinedByRegion.getY());
    }

    @Test
    void getX() {
        assertEquals(region.x(), locationDefinedByRegion.getX());
    }

    @Test
    void getY() {
        assertEquals(region.y2(), locationDefinedByRegion.getY());
    }
}