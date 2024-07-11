package io.github.jspinak.brobot.datatypes.primitives.location;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * Location is calculated
 * - when the region is defined, first with the position in the region, then with the offset
 * - when the region is not defined, with the x,y values, then with the offset
 */
@Getter
@Setter
public class Location {

    private String name;
    private int x = -1;
    private int y = -1;
    private Region region;
    private Position position;
    private Positions.Name anchor;
    private int offsetX = 0;
    private int offsetY = 0;

    public Location() {
        this.x = 0;
        this.y = 0;
    }

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Location(org.sikuli.script.Location sikuliLocation) {
        this.x = sikuliLocation.x;
        this.y = sikuliLocation.y;
    }

    public Location(Location loc) {
        double percentOfW, percentOfH;
        if (isDefinedWithRegion()) {
            this.region = loc.getRegion();
            if (loc.getPercentOfW().isPresent()) percentOfW = loc.getPercentOfW().get();
            else percentOfW = .5;
            if (loc.getPercentOfH().isPresent()) percentOfH = loc.getPercentOfH().get();
            else percentOfH = .5;
            position = new Position(percentOfW, percentOfH);
        } else {
            x = loc.x;
            y = loc.y;
        }
        anchor = loc.anchor;
        name = loc.name;
    }

    public Location(Region region, org.sikuli.script.Location sikuliLocation) {
        this.region = region;
        setPosition(sikuliLocation.x, sikuliLocation.y);
    }

    public Location(Region region) {
        this.region = region;
        position = new Position(.5, .5);
    }

    public void setPosition(int newX, int newY) {
        int percentOfW, percentOfH;
        percentOfW = (newX - region.x()) / region.w();
        percentOfH = (newY - region.y()) / region.h();
        position = new Position(percentOfW, percentOfH);
    }

    public Location(Region region, Position position) {
        this.region = region;
        this.position = position;
    }

    public Location(Region region, Positions.Name position) {
        this.region = region;
        this.position = new Position(position);
    }

    public Location(Positions.Name position) {
        this.region = new Region();
        this.position = new Position(position);
    }

    public Location(Region region, double percentOfW, double percentOfH) {
        this.region = region;
        this.position = new Position(percentOfW, percentOfH);
    }

    public Location(Match match) {
        if (match.getTarget().region != null) this.region = new Region(match.getRegion());
        this.position = new Position(match.getTarget().getPosition());
        this.x = match.getTarget().x;
        this.y = match.getTarget().y;
        this.offsetX = match.getTarget().getOffsetX();
        this.offsetY = match.getTarget().getOffsetY();
    }

    public Location(Match match, Position position) {
        this.region = match.getRegion();
        this.position = position;
    }

    public Location(Match match, int percentOfW, int percentOfH) {
        this.region = match.getRegion();
        this.position = new Position(percentOfW, percentOfH);
    }

    public Location(Location location, int addX, int addY) {
        this.x = location.x + addX;
        this.y = location.y + addY;
    }

    public Location(org.sikuli.script.Location location, int addX, int addY) {
        this.x = location.x + addX;
        this.y = location.y + addY;
    }

    public Optional<Double> getPercentOfW() {
        if (isDefinedByXY()) return Optional.empty();
        return Optional.of(position.getPercentW());
    }

    public Optional<Double> getPercentOfH() {
        if (isDefinedByXY()) return Optional.empty();
        return Optional.of(position.getPercentH());
    }

    public void addPercentOfW(int addPercent) {
        if (isDefinedWithRegion()) position.addPercentW(addPercent);
    }

    public void addPercentOfH(int addPercent) {
        if (isDefinedWithRegion()) position.addPercentH(addPercent);
    }

    public void multiplyPercentOfW(double multiplyBy) {
        if (isDefinedWithRegion()) position.multiplyPercentW(multiplyBy);
    }

    public void multiplyPercentOfH(double multiplyBy) {
        if (isDefinedWithRegion()) position.multiplyPercentH(multiplyBy);
    }

    private org.sikuli.script.Location getSikuliLocationFromXY() {
        return new org.sikuli.script.Location(x + offsetX, y + offsetY);
    }

    private org.sikuli.script.Location getSikuliLocationFromRegion() {
        double locX = region.x() + (region.w() * position.getPercentW()) + offsetX;
        double locY = region.y() + (region.h() * position.getPercentH()) + offsetY;
        return new org.sikuli.script.Location(locX, locY);
    }

    public org.sikuli.script.Location sikuli() {
        if (isDefinedByXY()) return getSikuliLocationFromXY();
        return getSikuliLocationFromRegion();
    }

    public int getX() {
        return sikuli().x;
    }

    public int getY() {
        return sikuli().y;
    }

    public int getRegionW() {
        if (isDefinedByXY()) return 1;
        return region.w();
    }

    public int getRegionH() {
        if (isDefinedByXY()) return 1;
        return region.h();
    }

    public boolean defined() {
        return !isDefinedByXY() || getX() > 0 || getY() > 0;
    }

    public Match toMatch() {
        return new Match.Builder()
                .setRegion(getX(), getY(), 1, 1)
                .build();
    }

    public StateLocation asStateLocationInNullState() {
        return new StateLocation.Builder()
                .setOwnerStateName("null")
                .setLocation(this)
                .build();
    }

    public ObjectCollection asObjectCollection() {
        StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(this)
                .setOwnerStateName("null")
                .setPosition(Positions.Name.TOPLEFT)
                .build();
        return new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .build();
    }

    public Location getOpposite() {
        if (region == null) return this;
        return new Location(this.region,
                1 - position.getPercentW(),
                1 - position.getPercentH());
    }

    public Location getOppositeTo(Location location) {
        int addX = 2 * (location.getX() - getX());
        int addY = 2 * (location.getY() - getY());
        return new Location(getX() + addX, getY() + addY);
    }

    public void adjustToRegion() {
        double percentOfW, percentOfH;
        percentOfW = Math.max(Math.min(1, position.getPercentW()), 0);
        percentOfH = Math.max(Math.min(1, position.getPercentH()), 0);
        position = new Position(percentOfW, percentOfH);
    }

    private boolean isDefinedByXY() {
        return region == null;
    }

    private boolean isDefinedWithRegion() {
        return !isDefinedByXY();
    }

    /*
    Java treats angles like this:
    0 degrees is straight right, or (1,0)
    -90 degrees is straight down, or (0,-1)
    90 degrees is straight up, or (1,0)
    both positive and negative continue until -179 meets 180 (-1,0)
    This is the opposite relationship for y-values on the screen (smaller values are up).
     */
    public void setFromCenter(double angle, double distance) {
        double rad = Math.toRadians(angle);
        // move from current point if not defined with a region
        int plusX = (int)(distance * Math.cos(rad));
        int minusY = (int)(distance * Math.sin(rad)); // the angle is the cartesian angle
        int ang = (int)Math.round(angle);
        int dist = (int)Math.round(distance);
        if (isDefinedByXY()) {
            x += plusX;
            y -= minusY;
        } else {// if defined by a region move from the center of the region
            Location center = new Location(region, Positions.Name.MIDDLEMIDDLE);
            setPosition(center.getX() + plusX, center.getY() - minusY);
        }
    }

    public boolean equals(Location l) {
        return (x == l.x && y == l.y &&
                //region.equals(l.region) && position == l.position &&
                isDefinedByXY() == l.isDefinedByXY());
    }

    /**
     * If the locations are mixed (defined by region and by xy) then it is converted to 'defined by xy'.
     * @param loc the location to add to this one
     */
    public void add(Location loc) {
        if (isDefinedByXY() && loc.isDefinedByXY()) {
            //Report.println("Adding x,y locations " + loc.x + " " + loc.y);
            x += loc.x;
            y += loc.y;
            return;
        }
        if (isDefinedWithRegion() && loc.isDefinedWithRegion()) {
            position.addPercentW(loc.position.getPercentW());
            position.addPercentH(loc.position.getPercentH());
            return;
        }
        if (isDefinedByXY()) {
            x += (int)(loc.position.getPercentW() * (double)region.w());
            y += (int)(loc.position.getPercentH() * (double)region.h());
            return;
        }
        //Report.println("percent W,H " + position.getPercentW() + " " + position.getPercentH());
        //Report.println("region W,H " + region.w + " " + region.h);
        //Report.println("x,y " + x + " " + y);
        x = getX() + loc.x;
        y = getY() + loc.y;
        //position.addPercentW((double)loc.x / region.w);
        //position.addPercentH((double)loc.y / region.h);
        //Report.println("percent W,H " + position.getPercentW() + " " + position.getPercentH());
        //Report.println("region W,H " + region.w + " " + region.h);
        //Report.println("x,y " + x + " " + y);
    }

    public void print() {
        Report.format("%d.%d ",x,y);
    }

    @Override
    public String toString() {
        return String.format("L[%d.%d]", getX(), getY());
    }

    public static class Builder {
        private String name;
        private int x = -1;
        private int y = -1;
        private Region region;
        private Position position = new Position(Positions.Name.MIDDLEMIDDLE);
        private Positions.Name anchor = Positions.Name.MIDDLEMIDDLE;
        private int offsetX = 0;
        private int offsetY = 0;

        public Builder called(String name) {
            this.name = name;
            return this;
        }

        public Builder setXY(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder setXY(org.sikuli.script.Location sikuliLocation) {
            this.x = sikuliLocation.x;
            this.y = sikuliLocation.y;
            return this;
        }

        public Builder copy(Location location) {
            this.anchor = location.anchor;
            x = location.x;
            y = location.y;
            offsetX = location.offsetX;
            offsetY = location.offsetY;
            position = new Position(location.position);
            if (location.region != null) region = new Region(location.region);
            return this;
        }

        public Builder setRegion(Region region) {
            this.region = region;
            return this;
        }

        public Builder setRegion(StateRegion region) {
            this.region = region.getSearchRegion();
            return this;
        }

        public Builder setPosition(Position position) {
            this.position = position;
            return this;
        }

        public Builder setPosition(Positions.Name positionName) {
            this.position = new Position(positionName);
            return this;
        }

        public Builder setPosition(int percentOfW, int percentOfH) {
            this.position = new Position(percentOfW, percentOfH);
            return this;
        }

        public Builder fromMatch(Match match) {
            this.region = new Region(match.getRegion());
            this.position = new Position(match.getTarget().getPosition());
            this.x = match.getTarget().x;
            this.y = match.getTarget().y;
            this.offsetX = match.getTarget().getOffsetX();
            this.offsetY = match.getTarget().getOffsetY();
            return this;
        }

        public Builder setAnchor(Positions.Name anchor) {
            this.anchor = anchor;
            return this;
        }

        public Builder setOffsetX(int offsetX) {
            this.offsetX = offsetX;
            return this;
        }

        public Builder setOffsetY(int offsetY) {
            this.offsetY = offsetY;
            return this;
        }

        public Location build() {
            Location location = new Location();
            location.setName(name);
            location.setX(x);
            location.setY(y);
            location.setRegion(region);
            location.setPosition(position);
            location.setAnchor(anchor);
            location.setOffsetX(offsetX);
            location.setOffsetY(offsetY);
            return location;
        }
    }

}
