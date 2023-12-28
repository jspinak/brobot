package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = BrobotTestApplication.class)
class AnchorRegionTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    AnchorRegion anchorRegion;

    @Test
    public void testFitRegionToAnchors() {
        DefinedBorders definedBorders = new DefinedBorders();
        Region region = new Region();
        Matches matches = new Matches();
        StateRegion reg1 = new StateRegion.Builder()
                .withSearchRegion(0,50,100,20)
                .addAnchor(Position.Name.TOPLEFT, Position.Name.BOTTOMLEFT)
                .build();
        StateRegion reg2 = new StateRegion.Builder()
                .withSearchRegion(1800,900,100,20)
                .addAnchor(Position.Name.BOTTOMRIGHT, Position.Name.TOPRIGHT)
                .build();
        Match m1;
        Match m2;
        m1 = new Match.Builder()
                .setMatch(reg1.getSearchRegion())
                .setStateObject(reg1)
                .build();
        m2 = new Match.Builder()
                .setMatch(reg2.getSearchRegion())
                .setStateObject(reg2)
                .build();
        matches.add(m1);
        matches.add(m2);
        anchorRegion.fitRegionToAnchors(definedBorders, region, matches);

        assertEquals(reg1.getSearchRegion().x, region.x);
        assertEquals(reg1.getSearchRegion().getY2(), region.y);
        assertEquals(reg2.getSearchRegion().getX2(), region.getX2());
        assertEquals(reg2.getSearchRegion().y, region.getY2());
    }

}