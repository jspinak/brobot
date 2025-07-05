package io.github.jspinak.brobot.model.match;

import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchTest {

    private Match match = new Match.Builder()
            .setRegion(0, 50, 100, 20)
            .build();
    private StateRegion stateRegion = new StateRegion.Builder()
            .setSearchRegion(match.getRegion())
            .addAnchor(Positions.Name.TOPLEFT, Positions.Name.BOTTOMLEFT)
            .build();

    @Test
    void getTarget() {
        Match newMatch = new Match.Builder()
                .setMatch(match)
                .setStateObjectData(stateRegion)
                .build();
        Location location = newMatch.getTarget();
        System.out.println(newMatch);
        System.out.println(location);

        assertEquals(match.x(), newMatch.x());
        assertEquals(match.x() + match.w() / 2, location.getCalculatedX());
        assertEquals(match.y() + match.h() / 2, location.getCalculatedY());
    }

    @Test
    void toStateImage() {
        StateImage stateImage = match.toStateImage();
        assertEquals(50, stateImage.getLargestDefinedFixedRegionOrNewRegion().y());
    }

}