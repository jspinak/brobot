package io.github.jspinak.brobot.datatypes.primitives.location;

import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    private final Region region = new Region(0, 50, 100, 20);
    private final Location locationDefinedByRegion = new Location(region, Positions.Name.BOTTOMLEFT);

    @Test
    void getSikuliLocationFromLocationDefinedByRegion() {
        org.sikuli.script.Location sikuliLocation = locationDefinedByRegion.sikuli();
        System.out.format("%d %d %d %d", sikuliLocation.x, sikuliLocation.y, locationDefinedByRegion.getCalculatedX(), locationDefinedByRegion.getCalculatedY());
        assertEquals(sikuliLocation.x, locationDefinedByRegion.getCalculatedX());
        assertEquals(sikuliLocation.y, locationDefinedByRegion.getCalculatedY());
    }

    @Test
    void getCalculatedX() {
        assertEquals(region.x(), locationDefinedByRegion.getCalculatedX());
    }

    @Test
    void getCalculatedY() {
        assertEquals(region.y2(), locationDefinedByRegion.getCalculatedY());
    }
}