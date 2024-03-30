package io.github.jspinak.brobot.test.datatypes.primitives.region;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegionTest {

    List<Region> getRegions() {
        List<Integer> xs = new ArrayList<>();
        xs.add(2);
        xs.add(5);
        xs.add(10);
        List<Integer> ys = new ArrayList<>();
        ys.add(2);
        ys.add(5);
        ys.add(10);
        return Region.getSubRegions(xs, ys);
    }

    @Test
    void minus() {
        Region a = new Region(100, 100, 100, 100);
        Region b = new Region(120, 120, 60, 60);
        Region c = new Region(50, 50, 100, 100);
        Region d = new Region(120, 80, 60, 60);

        List<Region> ab = a.minus(b);
        List<Region> ac = a.minus(c);
        List<Region> ad = a.minus(d);

        System.out.println(ab);
        assertEquals(4, ab.size());
        System.out.println(ac);
        assertEquals(2, ac.size());
        System.out.println(ad);
        assertEquals(3, ad.size());
    }

    @Test
    void getSubRegions() {
        List<Region> regions = getRegions();
        assertEquals(4, regions.size());

        Region reg1 = new Region(2,2,3,3);
        Region reg2 = new Region(2,5,3,5);
        Region reg3 = new Region(5,2,5,3);
        Region reg4 = new Region(5,5,5,5);

        assertTrue(reg1.equals(regions.get(0)));
        assertTrue(reg2.equals(regions.get(1)));
        assertTrue(reg3.equals(regions.get(2)));
        assertTrue(reg4.equals(regions.get(3)));
    }

    @Test
    void removeRegion() {
        List<Region> regions = getRegions();
        List<Region> removed = Region.removeRegion(regions, new Region(2,5,3,5));
        assertEquals(3, removed.size());
    }

    @Test
    void mergeAdjacent() {
        List<Region> regions = getRegions();
        List<Region> merged = Region.mergeAdjacent(regions);
        assertTrue(new Region(2,2,8,8).equals(merged.get(0))); // all subregions should merge
    }

    @Test
    void mergeAdjacent2() {
        List<Region> regions = getRegions();
        regions = Region.removeRegion(regions, new Region(2,2,3,3));
        List<Region> merged = Region.mergeAdjacent(regions);
        System.out.println(regions);
        System.out.println(merged);
        assertEquals(2, merged.size());
    }
}