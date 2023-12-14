package io.github.jspinak.brobot.datatypes.primitives.region;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.datatypes.state.NullState;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Rect;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Region extends the Sikuli class Region and adds, among other functionality, new
 * initializers, analysis tools, and points x2 and y2.
 */
@Getter
@Setter
public class Region extends org.sikuli.script.Region implements Comparable<Region> {

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

    @Override
    public int compareTo(Region comparesTo) {
        /*for ascending order*/
        int yDiff = this.y - comparesTo.y;
        int xDiff = this.x - comparesTo.x;
        if (yDiff != 0) return yDiff;
        return xDiff;
    }

    public boolean defined() {
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
        if (rect.isEmpty()) return Optional.empty();
        return Optional.of(new Region(rect.get()));
    }

    public Region getUnion(Region r) {
        int x = Math.min(this.x, r.x);
        int y = Math.min(this.y, r.y);
        int x2 = Math.max(this.x + this.w, r.x + r.w);
        int y2 = Math.max(this.y + this.h, r.y + r.h);
        return new Region(x, y, x2-x, y2-y);
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
}
