package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.model.match.Match;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationTest {

    @Test
    void constructor_withXY_shouldSetAbsolutePosition() {
        Location loc = new Location(100, 200);
        assertThat(loc.getCalculatedX()).isEqualTo(100);
        assertThat(loc.getCalculatedY()).isEqualTo(200);
        assertThat(loc.getRegion()).isNull();
    }

    @Test
    void constructor_withRegion_shouldCenterInRegion() {
        Region region = new Region(100, 100, 200, 200);
        Location loc = new Location(region);
        assertThat(loc.getCalculatedX()).isEqualTo(200); // 100 + 200 * 0.5
        assertThat(loc.getCalculatedY()).isEqualTo(200); // 100 + 200 * 0.5
    }

    @Test
    void constructor_withRegionAndPosition_shouldSetCorrectRelativeLocation() {
        Region region = new Region(100, 200, 100, 50);
        Position pos = new Position(Positions.Name.TOPLEFT); // (0.0, 0.0)
        Location loc = new Location(region, pos);
        assertThat(loc.getCalculatedX()).isEqualTo(100); // 100 + 100 * 0.0
        assertThat(loc.getCalculatedY()).isEqualTo(200); // 200 + 50 * 0.0
    }

    @Test
    void getCalculatedX_withRegionAndOffset_shouldApplyOffset() {
        Region region = new Region(100, 100, 100, 100);
        Location loc = new Location(region); // Center is (150, 150)
        loc.setOffsetX(25);
        assertThat(loc.getCalculatedX()).isEqualTo(175);
    }

    @Test
    void getCalculatedY_withXYAndOffset_shouldApplyOffset() {
        Location loc = new Location(50, 80);
        loc.setOffsetY(-30);
        assertThat(loc.getCalculatedY()).isEqualTo(50);
    }

    @Test
    void constructor_withMatch_shouldExtractLocation() {
        Match match = new Match.Builder()
                .setRegion(200, 300, 50, 50)
                .setPosition(new Position(0, 0)) // Top-left of the match
                .setOffset(new Location(5, 5))
                .build();

        Location loc = new Location(match);

        // The location from the match's target
        assertThat(loc.getCalculatedX()).isEqualTo(205); // 200 + 50 * 0.0 + 5
        assertThat(loc.getCalculatedY()).isEqualTo(305); // 300 + 50 * 0.0 + 5
    }

    @Test
    void locationBuilder_shouldBuildCorrectLocation() {
        Region region = new Region(100, 100, 200, 100);
        Location loc = new Location.Builder()
                .called("TestLocation")
                .setRegion(region)
                .setPosition(Positions.Name.BOTTOMRIGHT) // (1.0, 1.0)
                .setOffsetX(-10)
                .setOffsetY(-20)
                .build();

        assertThat(loc.getName()).isEqualTo("TestLocation");
        assertThat(loc.getCalculatedX()).isEqualTo(290); // 100 + 200*1.0 - 10
        assertThat(loc.getCalculatedY()).isEqualTo(180); // 100 + 100*1.0 - 20
    }

    @Test
    void add_shouldCombineLocations() {
        Location loc1 = new Location(10, 20);
        Location loc2 = new Location(5, -5);
        loc1.add(loc2);
        assertThat(loc1.getCalculatedX()).isEqualTo(15);
        assertThat(loc1.getCalculatedY()).isEqualTo(15);
    }

    @Test
    void toString_shouldReturnFormattedString() {
        Location loc = new Location(123, 456);
        assertThat(loc.toString()).isEqualTo("L[123.456]");
    }
}