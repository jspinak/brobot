package io.github.jspinak.brobot.datatypes.primitives.location;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    private final Region region = new Region(0, 50, 100, 20);
    private final Location location = new Location(region, Positions.Name.BOTTOMLEFT);

    @Test
    void getX() {
        assertEquals(region.x(), location.getX());
    }

    @Test
    void getY() {
        assertEquals(region.y2(), location.getY());
    }
}