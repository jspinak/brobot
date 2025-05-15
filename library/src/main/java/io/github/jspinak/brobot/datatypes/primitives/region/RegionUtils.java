package io.github.jspinak.brobot.datatypes.primitives.region;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import org.bytedeco.opencv.opencv_core.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RegionUtils {

    public static Region convertMatchToRegion(Match match) {
        return new Region(match.x(), match.y(), match.w(), match.h());
    }

    public static int[] extractRegionFromSikuli(org.sikuli.script.Match match) {
        return new int[] { match.x, match.y, match.w, match.h };
    }

    public static int[] calculateBoundingBox(Location loc1, Location loc2) {
        int x = Math.min(loc1.getCalculatedX(), loc2.getCalculatedX());
        int y = Math.min(loc1.getCalculatedY(), loc2.getCalculatedY());
        int x2 = Math.max(loc1.getCalculatedX(), loc2.getCalculatedX());
        int y2 = Math.max(loc1.getCalculatedY(), loc2.getCalculatedY());
        return new int[] { x, y, x2 - x, y2 - y };
    }

    public static int x2(Region region) {
        return region.getX() + region.getW();
    }

    public static int y2(Region region) {
        return region.getY() + region.getH();
    }

    /**
     * Sets x2 with respect to x and not to w (adjusts the width)
     * @param x2 the right boundary of the region
     */
    public static void adjustX2(Region region, int x2) {
        region.setW(x2 - region.getX());
    }

    /**
     * Sets y2 with respect to y and not to h (adjusts the height)
     * @param y2 the lower boundary of the region
     */
    public static void adjustY2(Region region, int y2) {
        region.setH(y2 - region.getY());
    }

    public static void adjustX(Region region, int newX) {
        int xDiff = newX - region.getX();
        region.setX(newX);
        region.setW(region.getW() - xDiff);
    }

    public static void adjustY(Region region, int newY) {
        int yDiff = newY - region.getY();
        region.setY(newY);
        region.setH(region.getH() - yDiff);
    }

    public static boolean isDefined(Region region) {
        int defaultWidth = Integer.parseInt(System.getenv().getOrDefault("SCREEN_WIDTH", "1920"));
        int defaultHeight = Integer.parseInt(System.getenv().getOrDefault("SCREEN_HEIGHT", "1080"));
        return region.getX() != 0 || region.getY() != 0 ||
                (region.getW() != defaultWidth && region.getW() >= 0) ||
                (region.getH() != defaultHeight && region.getH() >= 0);
    }

    public static int size(Region region) {
        return region.getW() * region.getH();
    }

    public static int compareRegions(Region r1, Region r2) {
        int yDiff = r1.getY() - r2.getY();
        int xDiff = r1.getX() - r2.getX();
        return (yDiff != 0) ? yDiff : xDiff;
    }

    // Spatial & Comparison Methods
    public static boolean overlaps(Region r1, Region r2) {
        return getOverlappingRegion(r1, r2).isPresent();
    }

    public static boolean overlaps(Region region, Rect rect) {
        return getOverlappingRect(region.getJavaCVRect(), rect).isPresent();
    }

    public static Optional<Region> getOverlappingRegion(Region r1, Region r2) {
        Optional<Rect> rect = getOverlappingRect(r1.getJavaCVRect(), r2.getJavaCVRect());
        return rect.map(Region::new);
    }

    public static Optional<Rect> getOverlappingRect(Rect rect1, Rect rect2) {
        int x = Math.max(rect1.x(), rect2.x());
        int y = Math.max(rect1.y(), rect2.y());
        int x2 = Math.min(rect1.x() + rect1.width(), rect2.x() + rect2.width());
        int y2 = Math.min(rect1.y() + rect1.height(), rect2.y() + rect2.height());
        if (x2 <= x || y2 <= y) return Optional.empty();
        return Optional.of(new Rect(x, y, x2 - x, y2 - y));
    }

    public static boolean contains(Region outer, Region inner) {
        return contains(outer, new Location(inner.getX(), inner.getY())) &&
               contains(outer, new Location(inner.getX() + inner.getW(), inner.getY())) &&
               contains(outer, new Location(inner.getX(), inner.getY() + inner.getH())) &&
               contains(outer, new Location(inner.getX() + inner.getW(), inner.getY() + inner.getH()));
    }

    public static boolean contains(Rect rect1, Rect rect2) {
        return contains(new Region(rect1), new Region(rect2));
    }

    public static boolean contains(Region region, Location loc) {
        return containsX(region, loc) && containsY(region, loc);
    }

    public static boolean containsX(Region region, Location loc) {
        return loc.getCalculatedX() >= region.getX() && loc.getCalculatedX() <= region.getX() + region.getW();
    }

    public static boolean containsY(Region region, Location loc) {
        return loc.getCalculatedY() >= region.getY() && loc.getCalculatedY() <= region.getY() + region.getH();
    }

    // Grid & Raster Functions
    public static Optional<Integer> getGridNumber(Region region, Location location) {
        if (!contains(region, location)) return Optional.empty();
        if (!region.sikuli().isRasterValid()) return Optional.of(-1);
        Region firstCell = new Region(region.sikuli().getCell(0, 0));
        int row = gridNumber(firstCell.getH(), firstCell.getY(), location.getCalculatedY());
        int col = gridNumber(firstCell.getW(), firstCell.getX(), location.getCalculatedX());
        return Optional.of(toGridNumber(region, row, col));
    }

    public static Optional<Integer> getGridNumber(Region region, org.sikuli.script.Location location) {
        if (!region.contains(location)) return Optional.empty();
        if (!region.sikuli().isRasterValid()) return Optional.of(-1);
        Region firstCell = new Region(region.sikuli().getCell(0, 0));
        int row = gridNumber(firstCell.getH(), firstCell.getY(), location.getY());
        int col = gridNumber(firstCell.getW(), firstCell.getX(), location.getX());
        return Optional.of(toGridNumber(region, row, col));
    }

    public static int gridNumber(int oneGridSize, int regionStart, int locationValue) {
        int distFromEdge = locationValue - regionStart;
        return distFromEdge / oneGridSize;
    }

    public static int toGridNumber(Region region, int row, int col) {
        return row * region.sikuli().getCols() + col;
    }

    public static int toRow(Region region, int gridNumber) {
        return gridNumber / region.sikuli().getCols();
    }

    public static int toCol(Region region, int gridNumber) {
        return gridNumber % region.sikuli().getCols();
    }

    // Spatial Transformations
    public static List<Integer> xPoints(Region a, Region b) {
        List<Integer> points = new ArrayList<>();
        boolean allXExposedAtSomeY = b.getY() > a.getY() || b.y2() < a.y2();
        if (allXExposedAtSomeY) {
            points.add(a.getX());
            points.add(a.x2());
        }
        if (b.getX() > a.getX()) points.add(b.getX());
        if (a.x2() > b.x2()) points.add(b.x2());
        Collections.sort(points);
        return points;
    }

    public static List<Integer> yPoints(Region a, Region b) {
        List<Integer> points = new ArrayList<>();
        boolean allYExposedAtSomeX = b.getX() > a.getX() || b.x2() < a.x2();
        if (allYExposedAtSomeX) {
            points.add(a.getY());
            points.add(a.y2());
        }
        if (b.getY() > a.getY()) points.add(b.getY());
        if (a.y2() > b.y2()) points.add(b.y2());
        Collections.sort(points);
        return points;
    }

    // Methods moved from Region class
    public static Location getRandomLocation(Region region) {
        Random randW = new Random();
        Random randH = new Random();
        return new Location(
                region.getX() + randW.nextInt(region.getW()),
                region.getY() + randH.nextInt(region.getH()));
    }

    public static boolean equals(Region r1, Region r2) {
        return r1.getX() == r2.getX() && r1.getY() == r2.getY() &&
                r1.getW() == r2.getW() && r1.getH() == r2.getH();
    }

    public static Rect getJavaCVRect(Region region) {
        return new Rect(region.getX(), region.getY(), region.getW(), region.getH());
    }

    public static Region getUnion(Region r1, Region r2) {
        int x = Math.min(r1.getX(), r2.getX());
        int y = Math.min(r1.getY(), r2.getY());
        int x2 = Math.max(r1.x2(), r2.x2());
        int y2 = Math.max(r1.y2(), r2.y2());
        return new Region(x, y, x2 - x, y2 - y);
    }

    public static Location getLocation(Region region) {
        return new Location(region.sikuli().getTarget().x, region.sikuli().getTarget().y);
    }

    /**
     * Region uses the SikuliX raster (setRaster), which has a min cell size of 5x5.
     * For smaller cell sizes, or more in-depth work with grids, use the
     * Brobot Grid classes.
     * @param rows the y-partitions
     * @param columns the x-partitions
     * @return a list of regions (cells of the grid), added in order of left to right and then up to down
     */
    public static List<Region> getGridRegions(Region region, int rows, int columns) {
        region.sikuli().setRaster(rows, columns); // SikuliX raster (setRaster) has a min cell size of 5x5
        List<Region> regions = new ArrayList<>();
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                regions.add(new Region(region.sikuli().getCell(i,j)));
            }
        }
        return regions;
    }

    /**
     * Finds the areas of region that do not overlap with the parameter region.
     * Non-overlapping areas of the parameter region are not considered.
     * @param a the main region
     * @param b the region that may overlap
     * @return a collection of Region objects comprising the non-overlapping areas.
     */
    public static List<Region> minus(Region a, Region b) {
        Optional<Region> overlapOpt = getOverlappingRegion(a, b);
        if (overlapOpt.isEmpty()) return List.of(a); // no overlap
        Region overlap = overlapOpt.get();
        List<Integer> xPoints = xPoints(a, b); // between 2 and 4 points
        List<Integer> yPoints = yPoints(a, b); // between 2 and 4 points
        List<Region> subRegions = getSubRegions(xPoints, yPoints);
        List<Region> uniqueRegions = removeRegion(subRegions, overlap);
        return mergeAdjacent(uniqueRegions);
    }

    /**
     * Return a list of all subregions created by the x and y points.
     * @param xPoints points along the x-axis
     * @param yPoints points along the y-axis
     * @return all regions created with these points as edges
     */
    public static List<Region> getSubRegions(List<Integer> xPoints, List<Integer> yPoints) {
        if (xPoints.size() < 2 || yPoints.size() < 2) return new ArrayList<>();
        List<Region> subRegions = new ArrayList<>();
        for (int i=0; i<xPoints.size()-1; i++) {
            for (int j=0; j<yPoints.size()-1; j++) {
                int x1 = xPoints.get(i);
                int x2 = xPoints.get(i+1);
                int y1 = yPoints.get(j);
                int y2 = yPoints.get(j+1);
                subRegions.add(new Region(x1, y1, x2-x1, y2-y1));
            }
        }
        return subRegions;
    }

    public static List<Region> removeRegion(List<Region> regions, Region toRemove) {
        List<Region> newRegions = new ArrayList<>();
        regions.forEach(region -> {
            if (!equals(region, toRemove)) newRegions.add(region);
        });
        return newRegions;
    }

    public static List<Region> mergeAdjacent(List<Region> regions) {
        if (regions.size() <= 1) return regions;
        boolean listIsSmaller = true;
        List<Region> merged = regions;
        while (listIsSmaller) {
            List<Region> newMerge = mergeIteration(merged);
            listIsSmaller = newMerge.size() < merged.size();
            merged = newMerge;
        }
        return merged;
    }

    private static List<Region> mergeIteration(List<Region> regions) {
        if (regions.size() <= 1) return regions;
        List<Region> merged = new ArrayList<>();
        Region m = new Region(regions.getFirst());
        for (int i=1; i<=regions.size(); i++) {
            if (i==regions.size()) { // we've checked all regions
                merged.add(m);
                break;
            }
            Region r = regions.get(i);
            boolean horizontalMatch = m.x2() == r.x() && m.y() == r.y() && m.y2() == r.y2();
            boolean verticalMatch = m.y2() == r.y() && m.x() == r.x() && m.x2() == r.x2();
            if (horizontalMatch) m.adjustX2(r.x2());
            else if (verticalMatch) m.adjustY2(r.y2());
            else { // no match
                merged.add(m);
                m = new Region(r);
            }
        }
        return merged;
    }
}