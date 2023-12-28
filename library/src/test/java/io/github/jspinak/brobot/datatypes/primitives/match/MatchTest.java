package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchTest {

    private Match match = new Match(new Region(0, 50, 100, 20));
    private StateRegion stateRegion = new StateRegion.Builder()
            .withSearchRegion(new Region(match))
            .addAnchor(Position.Name.TOPLEFT, Position.Name.BOTTOMLEFT)
            .build();

    @Test
    void getLocation() {
        Match newMatch = new Match.Builder()
                .setMatch(match)
                .setStateObject(stateRegion)
                .build();
        Location location = newMatch.getLocation();
        System.out.println(newMatch);
        System.out.println(location);

        assertEquals(match.x, newMatch.x);
        assertEquals(match.x + match.w / 2, location.getX());
        assertEquals(match.y + match.h / 2, location.getY());
    }
}