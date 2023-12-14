package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import org.junit.jupiter.api.Test;
import org.sikuli.script.Match;

import static org.junit.jupiter.api.Assertions.*;

class MatchObjectTest {

    private Match match = new Match(new org.sikuli.script.Region(0, 50, 100, 20), 1);
    private StateRegion stateRegion = new StateRegion.Builder()
            .withSearchRegion(new Region(match))
            .addAnchor(Position.Name.TOPLEFT, Position.Name.BOTTOMLEFT)
            .build();

    @Test
    void getLocation() {
        MatchObject matchObject;
        try {
            matchObject = new MatchObject(match, stateRegion, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Location location = matchObject.getLocation();

        assertEquals(match.x, matchObject.getMatch().x);
        assertEquals(match.x + match.w / 2, location.getX());
        assertEquals(match.y + match.h / 2, location.getY());
    }
}