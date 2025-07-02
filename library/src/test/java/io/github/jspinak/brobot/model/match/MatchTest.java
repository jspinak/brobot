package io.github.jspinak.brobot.model.match;

import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class MatchTest {

    @Test
    void builder_shouldCreateMatchWithRegion() {
        Match match = new Match.Builder()
                .setRegion(10, 20, 30, 40)
                .setName("testMatch")
                .setSimScore(0.95)
                .build();

        assertEquals("testMatch", match.getName());
        assertEquals(0.95, match.getScore());
        assertEquals(10, match.x());
        assertEquals(20, match.y());
        assertEquals(30, match.w());
        assertEquals(40, match.h());
    }

    @Test
    void constructor_withRegion_shouldSetTarget() {
        Region region = new Region(100, 150, 20, 30);
        Match match = new Match(region);

        assertNotNull(match.getTarget());
        assertEquals(region, match.getRegion());
    }

    @Test
    void toString_shouldReturnFormattedString() {
        Match match = new Match.Builder()
                .setRegion(10, 20, 30, 40)
                .setName("test")
                .setSimScore(0.9)
                .setText("hello")
                .build();

        String expected = "M[#test# R[10,20 30x40] simScore:0.9 text:hello]";
        assertEquals(expected, match.toString());
    }

    @Test
    void toStateImage_shouldCreateCorrectStateImage() {
        Image image = new Image(new BufferedImage(10,10,1));
        StateObjectMetadata sod = new StateObjectMetadata();
        sod.setOwnerStateName("TestState");
        sod.setStateObjectName("TestImage");

        Match match = new Match.Builder()
                .setImage(image)
                .setName("matchName")
                .setRegion(new Region(1,1,1,1))
                .setStateObjectData(sod)
                .build();

        var stateImage = match.toStateImage();
        assertThat(stateImage.getName()).isEqualTo("TestImage");
        assertThat(stateImage.getOwnerStateName()).isEqualTo("TestState");
        assertThat(stateImage.getPatterns()).hasSize(1);
    }
}