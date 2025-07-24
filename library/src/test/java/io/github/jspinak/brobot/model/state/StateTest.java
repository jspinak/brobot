package io.github.jspinak.brobot.model.state;

import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.State;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateTest {

    private Pattern createTestPattern() {
        // Helper method to create a Pattern without file system access
        BufferedImage dummyImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        return new Pattern(dummyImage);
    }

    @Test
    void testStateBuilderAndOwnerNamePropagation() {
        // 1. Setup child objects using memory-based patterns
        StateImage image = new StateImage.Builder().addPattern(createTestPattern()).build();
        StateRegion region = new StateRegion.Builder().setSearchRegion(new Region(1, 1, 1, 1)).build();
        StateString str = new StateString.Builder().setString("test").build();
        StateLocation loc = new StateLocation.Builder().setLocation(1, 1).build();

        // 2. Build the State
        State state = new State.Builder("TestState")
                .withImages(image)
                .withRegions(region)
                .withStrings(str)
                .withLocations(loc)
                .build();

        // 3. Assert owner name propagation (the core of this test)
        assertEquals("TestState", state.getStateImages().iterator().next().getOwnerStateName());
        assertEquals("TestState", state.getStateRegions().iterator().next().getOwnerStateName());
        assertEquals("TestState", state.getStateStrings().iterator().next().getOwnerStateName());
        assertEquals("TestState", state.getStateLocations().iterator().next().getOwnerStateName());
    }

    @Test
    void testSetSearchRegionForAllImages() {
        StateImage image1 = new StateImage.Builder().addPattern(createTestPattern()).build();
        StateImage image2 = new StateImage.Builder().addPattern(createTestPattern()).build();
        State state = new State.Builder("RegionState").withImages(image1, image2).build();
        Region newRegion = new Region(10, 20, 30, 40);

        state.setSearchRegionForAllImages(newRegion);

        for (StateImage si : state.getStateImages()) {
            for (Pattern p : si.getPatterns()) {
                assertEquals(newRegion, p.getRegions().get(0));
            }
        }
    }

    @Test
    void testGetBoundaries() {
        StateRegion stateRegion = new StateRegion.Builder().setSearchRegion(new Region(0, 0, 10, 10)).build();

        Pattern patternWithFixedRegion = createTestPattern();
        patternWithFixedRegion.getSearchRegions().setFixedRegion(new Region(20, 0, 10, 10));
        StateImage stateImageWithFixed = new StateImage.Builder().addPattern(patternWithFixedRegion).build();

        State state = new State.Builder("BoundaryState")
                .withRegions(stateRegion)
                .withImages(stateImageWithFixed)
                .build();

        Region boundaries = state.getBoundaries();

        assertEquals(0, boundaries.x());
        assertEquals(0, boundaries.y());
        assertEquals(30, boundaries.w());
        assertEquals(10, boundaries.h());
    }

    @Test
    void testGetBoundariesWithSnapshots() {
        Match matchInSnapshot = new Match.Builder().setRegion(new Region(50, 50, 5, 5)).build();
        ActionRecord snapshot = new ActionRecord();
        snapshot.addMatch(matchInSnapshot);

        Pattern patternWithSnapshot = createTestPattern();
        patternWithSnapshot.getMatchHistory().addSnapshot(snapshot);

        StateImage stateImageWithSnapshot = new StateImage.Builder().addPattern(patternWithSnapshot).build();
        State state = new State.Builder("SnapshotBoundaryState").withImages(stateImageWithSnapshot).build();

        Region boundaries = state.getBoundaries();

        assertEquals(50, boundaries.x());
    }
}