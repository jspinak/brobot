package io.github.jspinak.brobot.datatypes.primitives.region;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.state.NullState;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Rect;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;
import org.springframework.data.annotation.Id;

import java.util.*;

/**
 * Region extends the Sikuli class Region and adds, among other functionality, new
 * initializers, analysis tools, and points x2 and y2.
 */
@Embeddable
@Getter
@Setter
public class Region extends org.sikuli.script.Region implements Comparable<Region> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int x2 = -1; // x + w
    private int y2 = -1; // y + h

    public Region() {
        Screen screen = new Screen();
        setXYWH(screen.x, screen.y, screen.w, screen.h);
    }

    public Region(int x, int y, int w, int h) {
        setXYWH(x,y,w,h);
    }

    public Region(Match match) {
        setTo(match);
    }

    public Region(Region region) {
        setTo(region);
    }

    public Region(org.sikuli.script.Region region) { setTo(region); }

    public Region(Match match, int xAdjust, int yAdjust, int wAdjust, int hAdjust) {
        x = match.x - xAdjust;
        y = match.y - yAdjust;
        w = wAdjust;
        h = hAdjust;
        setXY2();
    }

    public Region(Rect rect) {
        x = rect.x();
        y = rect.y();
        w = rect.width();
        h = rect.height();
        setXY2();
    }

    public Region(Location location1, Location location2) {
        int x = Math.min(location1.getX(), location2.getY());
        int y = Math.min(location1.getY(), location2.getY());
        int x2 = Math.max(location1.getX(), location2.getY());
        int y2 = Math.max(location1.getY(), location2.getY());
        setXYWH(x, y, x2-x, y2-y);
    }

    public StateRegion inNullState() {
        return new StateRegion.Builder()
                .inState(NullState.Name.NULL.toString())
                .withSearchRegion(this)
                .build();
    }

    private void setXY2() {
        x2 = x + w;
        y2 = y + h;
    }

    public void setXYWH(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        setXY2();
    }

    public void setTo(Match match) {
        if (match == null) return;
        setXYWH(match.x, match.y, match.w, match.h);
    }

    public void setTo(org.sikuli.script.Region region) {
        if (region == null) return;
        setXYWH(region.x, region.y, region.w, region.h);
    }

    public void setX2(int x2) {
        this.x2 = x2;
        w = x2 - x;
    }

    public void setY2(int y2) {
        this.y2 = y2;
        h = y2 - y;
    }

    public int getX2() {
        return x + w;
    }

    public int getY2() {
        return y + h;
    }

    @Override
    public int compareTo(Region comparesTo) {
        /*for ascending order*/
        int yDiff = this.y - comparesTo.y;
        int xDiff = this.x - comparesTo.x;
        if (yDiff != 0) return yDiff;
        return xDiff;
    }

    public boolean isDefined() {
        return x!=0 || y!=0 || w!=new Screen().w || h!=new Screen().h;
    }

    /*
    Match is also allowed as a param since Match extends Region
    */
    public boolean overlaps(Region r) {
        Optional<Region> overlap = getOverlappingRegion(r);
        return overlap.isPresent();
    }

    public boolean overlaps(Rect rect) {
        Region r = new Region(rect);
        return overlaps(r);
    }

    /*
    Match is also allowed as a param since Match extends Region
     */
    public boolean contains(Region r) {
        return contains(r.getTopLeft()) && contains(r.getTopRight())
                && contains(r.getBottomLeft()) && contains(r.getBottomRight());
    }

    public boolean contains(Location l) {
        return contains(l.getSikuliLocation());
    }

    public boolean containsX(Location l) {
        return l.getX() >= x && l.getX() <= x + w;
    }

    public boolean containsY(Location l) {
        return l.getY() >= y && l.getY() <= y + h;
    }

    public int size() {
        return w * h;
    }

    public Match toMatch() {
        return new Match(x, y, w, h, 1, new Screen());
    }

    /**
     * Region uses the SikuliX raster (setRaster), which has a min cell size of 5x5.
     * For smaller cell sizes, or more in-depth work with grids, use the
     * Brobot Grid classes.
     * @param rows the y-partitions
     * @param columns the x-partitions
     * @return a list of regions (cells of the grid), added in order of left to right and then up to down
     */
    public List<Region> getGridRegions(int rows, int columns) {
        setRaster(rows, columns); // SikuliX raster (setRaster) has a min cell size of 5x5
        List<Region> regions = new ArrayList<>();
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                regions.add(new Region(getCell(i,j)));
            }
        }
        return regions;
    }

    public Optional<Integer> getGridNumber(Location location) {
        if (!contains(location)) return Optional.empty();
        if (!isRasterValid()) return Optional.of(-1);
        Region firstCell = new Region(getCell(0,0));
        int row = gridNumber(firstCell.h, firstCell.y, location.getY());
        int col = gridNumber(firstCell.w, firstCell.x, location.getX());
        return Optional.of(toGridNumber(row, col));
    }

    private int toGridNumber(int row, int col) {
       return row * this.getCols() + col;
    }

    private int toRow(int gridNumber) {
        return gridNumber / this.getCols();
    }

    private int toCol(int gridNumber) {
        return gridNumber % this.getCols();
    }

    public Region getGridRegion(int gridNumber) {
        if (gridNumber == -1) return this;
        return new Region(getCell(toRow(gridNumber), toCol(gridNumber)));
    }

    public Optional<Region> getGridRegion(Location location) {
        Optional<Integer> gridNumber = getGridNumber(location);
        if (gridNumber.isEmpty()) return Optional.empty();
        return Optional.of(getGridRegion(gridNumber.get()));
    }

    private int gridNumber(int oneGridSize, int regionStart, int locationValue) {
        int distFromEdge = locationValue - regionStart;
        return distFromEdge / oneGridSize;
    }

    public io.github.jspinak.brobot.datatypes.primitives.location.Location getRandomLocation() {
        Random randW = new Random();
        Random randH = new Random();
        return new io.github.jspinak.brobot.datatypes.primitives.location.Location(
                x + randW.nextInt(w), y + randH.nextInt(h));
    }

    public void print() {
        System.out.println("region: XYWH="+x+"."+y+"."+w+"."+h+"| ");
    }

    // sikuli is unable to process this class even though it is a child of the sikuli Region
    // casting '(org.sikuli.script.Region) this' does not work
    // returning a new sikuli Region works but eliminates any memory of previously found images
    // this should be ok, images with fixed regions should be defined as RegionImagePairs
    public org.sikuli.script.Region sikuli() {
        return new org.sikuli.script.Region(x,y,w,h);
    }

    public ObjectCollection asObjectCollection() {
        return new ObjectCollection.Builder()
                .withRegions(this)
                .build();
    }

    public boolean equals(Region r) {
        return x == r.x && y == r.y && w == r.w && h == r.h;
    }

    public Rect getJavaCVRect() {
        return new Rect(x, y, w, h);
    }

    public boolean contains(Rect rect) {
        return contains(new Region(rect));
    }

    public Optional<Rect> getOverlappingRect(Rect rect) {
        int x = Math.max(this.x, rect.x());
        int y = Math.max(this.y, rect.y());
        int x2 = Math.min(this.x+this.w, rect.x()+rect.width());
        int y2 = Math.min(this.y+this.h, rect.y()+rect.height());
        if (x2 <= x || y2 <= y) return Optional.empty();
        return Optional.of(new Rect(x, y, x2-x, y2-y));
    }

    public Optional<Region> getOverlappingRegion(Region r) {
        Optional<Rect> rect = getOverlappingRect(r.getJavaCVRect());
        return rect.map(Region::new);
    }

    public Region getUnion(Region r) {
        return new Region(this.union(r));
    }

    public void setAsUnion(Region r) {
        Region union = getUnion(r);
        this.x = union.x;
        this.y = union.y;
        this.w = union.w;
        this.h = union.h;
    }

    public Location getLocation() {
        return new Location(getTarget().x, getTarget().y);
    }

    /**
     * Finds the areas of this region that do not overlap with the parameter region.
     * Non-overlapping areas of the parameter region are not considered.
     * @param b the region that may overlap
     * @return a collection of Region objects comprising the non-overlapping areas.
     */
    public List<Region> minus(Region b) {
        setXY2();
        Optional<Region> overlapOpt = getOverlappingRegion(b);
        if (overlapOpt.isEmpty()) return List.of(this); // no overlap
        Region overlap = overlapOpt.get();
        List<Integer> xPoints = xPoints(b); // between 2 and 4 points
        List<Integer> yPoints = yPoints(b); // between 2 and 4 points
        List<Region> subRegions = getSubRegions(xPoints, yPoints);
        List<Region> uniqueRegions = removeRegion(subRegions, overlap);
        List<Region> merged = mergeAdjacent(uniqueRegions);
        return merged;
    }

    private List<Integer> xPoints(Region b) {
        List<Integer> points = new ArrayList<>();
        boolean allXExposedAtSomeY = b.y > y || b.y2 < y2;
        if (allXExposedAtSomeY) {
            points.add(x);
            points.add(x2);
        }
        if (b.x > x) {
            points.add(b.x);
        }
        if (x2 > b.x2) {
            points.add(b.x2);
        }
        Collections.sort(points);
        return points;
    }

    private List<Integer> yPoints(Region b) {
        List<Integer> points = new ArrayList<>();
        boolean allYExposedAtSomeX = b.x > x || b.x2 < x2;
        if (allYExposedAtSomeX) {
            points.add(y);
            points.add(y2);
        }
        if (b.y > y) {
            points.add(b.y);
        }
        if (y2 > b.y2) {
            points.add(b.y2);
        }
        Collections.sort(points);
        return points;
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
            if (!region.equals(toRemove)) newRegions.add(region);
        });
        return newRegions;
    }

    public static List<Region> mergeAdjacent(List<Region> regions) {
        if (regions.size() <= 1) return regions;
        regions.forEach(Region::setXY2); // update the x2 and y2 values
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
        Region m = new Region(regions.get(0));
        for (int i=1; i<=regions.size(); i++) {
            if (i==regions.size()) { // we've checked all regions
                merged.add(m);
                break;
            }
            Region r = regions.get(i);
            boolean horizontalMatch = m.x2 == r.x && m.y == r.y && m.y2 == r.y2;
            boolean verticalMatch = m.y2 == r.y && m.x == r.x && m.x2 == r.x2;
            if (horizontalMatch) m.setX2(r.x2);
            else if (verticalMatch) m.setY2(r.y2);
            else { // no match
                merged.add(m);
                m = new Region(r);
            }
        }
        return merged;
    }
}
