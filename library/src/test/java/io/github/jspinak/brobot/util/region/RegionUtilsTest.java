package io.github.jspinak.brobot.util.region;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RegionUtilsTest {

    @Test
    void calculateBoundingBox_shouldReturnCorrectDimensions() {
        Location loc1 = new Location(10, 20);
        Location loc2 = new Location(110, 70);
        int[] box = RegionUtils.calculateBoundingBox(loc1, loc2);
        assertArrayEquals(new int[]{10, 20, 100, 50}, box);
    }

    @Test
    void getOverlappingRegion_shouldReturnCorrectOverlap() {
        Region r1 = new Region(0, 0, 100, 100);
        Region r2 = new Region(50, 50, 100, 100);
        Region expectedOverlap = new Region(50, 50, 50, 50);
        Region actualOverlap = RegionUtils.getOverlappingRegion(r1, r2).orElse(null);
        assertEquals(expectedOverlap, actualOverlap);
    }

    @Test
    void getOverlappingRegion_shouldReturnEmptyForNonOverlapping() {
        Region r1 = new Region(0, 0, 100, 100);
        Region r2 = new Region(101, 101, 100, 100);
        assertTrue(RegionUtils.getOverlappingRegion(r1, r2).isEmpty());
    }

    @Test
    void mergeAdjacent_shouldMergeHorizontally() {
        Region r1 = new Region(0, 0, 100, 100);
        Region r2 = new Region(100, 0, 50, 100);
        List<Region> merged = RegionUtils.mergeAdjacent(List.of(r1, r2));
        assertEquals(1, merged.size());
        assertEquals(new Region(0, 0, 150, 100), merged.get(0));
    }

    @Test
    void mergeAdjacent_shouldMergeVertically() {
        Region r1 = new Region(0, 0, 100, 100);
        Region r2 = new Region(0, 100, 100, 50);
        List<Region> merged = RegionUtils.mergeAdjacent(List.of(r1, r2));
        assertEquals(1, merged.size());
        assertEquals(new Region(0, 0, 100, 150), merged.get(0));
    }

    @Test
    void adjustX_and_adjustY_shouldModifyRegionCorrectly() {
        Region region = new Region(10, 20, 100, 50);

        RegionUtils.adjustX(region, 15);
        assertThat(region.x()).isEqualTo(15);
        assertThat(region.w()).isEqualTo(95); // 100 - (15-10)

        RegionUtils.adjustY(region, 30);
        assertThat(region.y()).isEqualTo(30);
        assertThat(region.h()).isEqualTo(40); // 50 - (30-20)
    }

    @Test
    void contains_point_shouldReturnCorrectResult() {
        Region region = new Region(0, 0, 100, 100);
        assertThat(RegionUtils.contains(region, new Location(50, 50))).isTrue();
        assertThat(RegionUtils.contains(region, new Location(101, 50))).isFalse();
    }
}