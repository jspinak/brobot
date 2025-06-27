package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.internal.capture.AnchorRegion;
import io.github.jspinak.brobot.action.internal.capture.DefinedBorders;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateRegion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
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
        ActionResult matches = new ActionResult();
        StateRegion reg1 = new StateRegion.Builder()
                .setSearchRegion(0,50,100,20)
                .addAnchor(Positions.Name.TOPLEFT, Positions.Name.BOTTOMLEFT)
                .build();
        StateRegion reg2 = new StateRegion.Builder()
                .setSearchRegion(1800,900,100,20)
                .addAnchor(Positions.Name.BOTTOMRIGHT, Positions.Name.TOPRIGHT)
                .build();
        Match m1;
        Match m2;
        m1 = new Match.Builder()
                .setRegion(reg1.getSearchRegion())
                .setStateObjectData(reg1)
                .setAnchors(reg1.getAnchors())
                .build();
        m2 = new Match.Builder()
                .setRegion(reg2.getSearchRegion())
                .setStateObjectData(reg2)
                .setAnchors(reg2.getAnchors())
                .build();
        matches.add(m1);
        matches.add(m2);
        anchorRegion.fitRegionToAnchors(definedBorders, region, matches);

        assertEquals(reg1.getSearchRegion().x(), region.x());
        assertEquals(reg1.getSearchRegion().y2(), region.y());
        assertEquals(reg2.getSearchRegion().x2(), region.x2());
        assertEquals(reg2.getSearchRegion().y(), region.y2());
    }

}