package io.github.jspinak.brobot.datatypes.primitives.region;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Region;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RegionTest {

    private Region region;

    @BeforeEach
    void setup() {
        region = new Region(10, 20, 100, 50);
    }

    @Test
    void constructor_withCoordinates_shouldSetProperties() {
        Region region = new Region(10, 20, 100, 50);
        assertEquals(10, region.x());
        assertEquals(20, region.y());
        assertEquals(100, region.w());
        assertEquals(50, region.h());
    }

    @Test
    void constructor_withMatch_shouldSetProperties() {
        Match match = new Match(new Region(10, 20, 30, 40));
        Region region = new Region(match);
        assertEquals(10, region.x());
        assertEquals(20, region.y());
        assertEquals(30, region.w());
        assertEquals(40, region.h());
    }

    @Test
    void overlaps_shouldReturnTrueForOverlappingRegions() {
        Region r1 = new Region(0, 0, 100, 100);
        Region r2 = new Region(50, 50, 100, 100);
        assertTrue(r1.overlaps(r2));
    }

    @Test
    void overlaps_shouldReturnFalseForNonOverlappingRegions() {
        Region r1 = new Region(0, 0, 100, 100);
        Region r2 = new Region(101, 101, 100, 100);
        assertFalse(r1.overlaps(r2));
    }

    @Test
    void contains_shouldReturnTrueWhenRegionIsInside() {
        Region outer = new Region(0, 0, 200, 200);
        Region inner = new Region(50, 50, 50, 50);
        assertTrue(outer.contains(inner));
    }

    @Test
    void contains_shouldReturnFalseWhenRegionIsPartiallyOutside() {
        Region outer = new Region(0, 0, 100, 100);
        Region partial = new Region(50, 50, 100, 100);
        assertFalse(outer.contains(partial));
    }

    @Test
    void getUnion_shouldReturnBoundingBoxOfTwoRegions() {
        Region r1 = new Region(0, 0, 100, 100);
        Region r2 = new Region(50, 150, 100, 100);
        Region union = r1.getUnion(r2);
        assertEquals(0, union.x());
        assertEquals(0, union.y());
        assertEquals(150, union.w());
        assertEquals(250, union.h());
    }

    @Test
    void minus_shouldReturnNonOverlappingParts() {
        Region base = new Region(0, 0, 200, 200);
        Region subtract = new Region(50, 0, 100, 200);
        List<Region> result = base.minus(subtract);
        assertEquals(2, result.size());
        // Note: The result depends on the implementation of minus and merge.
        // This test assumes it will produce two distinct regions.
    }

    @Test
    void equals_shouldReturnTrueForIdenticalRegions() {
        Region r1 = new Region(10, 20, 30, 40);
        Region r2 = new Region(10, 20, 30, 40);
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void equals_shouldReturnFalseForDifferentRegions() {
        Region r1 = new Region(10, 20, 30, 40);
        Region r2 = new Region(11, 20, 30, 40);
        assertNotEquals(r1, r2);
    }

    @Test
    void constructor_withLocations_shouldCreateBoundingBox() {
        Location loc1 = new Location(10, 100);
        Location loc2 = new Location(60, 20);
        Region fromLocs = new Region(loc1, loc2);
        assertThat(fromLocs.x()).isEqualTo(10);
        assertThat(fromLocs.y()).isEqualTo(20);
        assertThat(fromLocs.w()).isEqualTo(50);
        assertThat(fromLocs.h()).isEqualTo(80);
    }

    @Test
    void copyConstructor_shouldCreateEqualObject() {
        Region copy = new Region(region);
        assertThat(copy).isEqualTo(region).isNotSameAs(region);
    }

    @Test
    void adjust_shouldModifyAllCoordinates() {
        region.adjust(5, -5, 10, -10);
        assertThat(region.x()).isEqualTo(5);
        assertThat(region.y()).isEqualTo(25);
        assertThat(region.w()).isEqualTo(10);
        assertThat(region.h()).isEqualTo(-10);
    }

    @Test
    void compareTo_shouldSortByYThenX() {
        Region other = new Region(5, 30, 100, 50); // y is greater
        assertThat(region.compareTo(other)).isLessThan(0);

        Region sameY = new Region(15, 20, 100, 50); // x is greater
        assertThat(region.compareTo(sameY)).isLessThan(0);
    }

    @Test
    void size_shouldReturnCorrectArea() {
        assertThat(region.size()).isEqualTo(5000);
    }

    @Test
    void setAsUnion_shouldModifyRegionToBeUnion() {
        Region other = new Region(0, 0, 15, 25);
        region.setAsUnion(other);
        assertThat(region.x()).isEqualTo(0);
        assertThat(region.y()).isEqualTo(0);
        assertThat(region.w()).isEqualTo(110);
        assertThat(region.h()).isEqualTo(70);
    }

    @Test
    void minus_whenNoOverlap_shouldReturnOriginalRegion() {
        Region nonOverlapping = new Region(200, 200, 10, 10);
        List<Region> result = region.minus(nonOverlapping);
        assertThat(result).containsExactly(region);
    }
}