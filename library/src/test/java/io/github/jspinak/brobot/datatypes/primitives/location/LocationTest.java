package io.github.jspinak.brobot.datatypes.primitives.location;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    private Region region = new Region(0, 50, 100, 20);
    private Location location = new Location(region, Position.Name.BOTTOMLEFT);

    @Test
    void getX() {
        assertEquals(region.getX(), location.getX());
    }

    @Test
    void getY() {
        assertEquals(region.getY2(), location.getY());
    }
}