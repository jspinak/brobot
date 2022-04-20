package io.github.jspinak.brobot.database.primitives.location;

import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.database.state.ObjectCollection;
import io.github.jspinak.brobot.database.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.database.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.reports.Report;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;

import java.util.Optional;

import static io.github.jspinak.brobot.database.state.NullState.Enum.NULL;

/**
 * Location can be an absolute position (x,y) on the screen,
 * or a relative position (%w, %h) of a Region.
 * The relative position is used unless the Region is not defined
 * or the boolean 'definedByXY' is explicitly set to true;
 */
@Getter
@Setter
public class Location {

    private String name;
    private boolean definedByXY = false;
    private int x = -1;
    private int y = -1;
    private Region region;
    private Position position;
    private Position.Name anchor;

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
        int percentOfW, percentOfH;
        if (loc.getRegion().isPresent()) {
            this.region = loc.getRegion().get();
            if (loc.getPercentOfW().isPresent()) percentOfW = loc.getPercentOfW().get();
            else percentOfW = 50;
            if (loc.getPercentOfH().isPresent()) percentOfH = loc.getPercentOfH().get();
            else percentOfH = 50;
            position = new Position(percentOfW, percentOfH);
        } else {
            x = loc.x;
            y = loc.y;
            definedByXY = true;
        }
        anchor = loc.anchor;
    }

    public Location(Region region, org.sikuli.script.Location sikuliLocation) {
        this.region = region;
        setPosition(sikuliLocation.x, sikuliLocation.y);
    }

    public Location(Region region) {
        this.region = region;
        setPosition(region.getTarget().x, region.getTarget().y);
    }

    public void setPosition(int newX, int newY) {
        int percentOfW, percentOfH;
        percentOfW = (newX - region.x) * 100 / region.w;
        percentOfH = (newY - region.y) * 100 / region.h;
        System.out.println("percent of W,H: "+percentOfW+" "+percentOfH);
        position = new Position(percentOfW, percentOfH);
    }

    public Location(Region region, Position position) {
        this.region = region;
        this.position = position;
    }

    public Location(Region region, Position.Name position) {
        this.region = region;
        this.position = new Position(position);
    }

    public Location(Position.Name position) {
        this.region = new Region();
        this.position = new Position(position);
    }

    public Location(Region region, int percentOfW, int percentOfH) {
        this.region = region;
        this.position = new Position(percentOfW, percentOfH);
    }

    public Location(Match match) {
        this.region = new Region(match);
        int percentOfW, percentOfH;
        percentOfW = (match.getTarget().x - match.x) / match.w;
        percentOfH = (match.getTarget().y - match.y) / match.y;
        position = new Position(percentOfW, percentOfH);
    }

    public Location(Match match, Position position) {
        this.region = new Region(match);
        this.position = position;
    }

    public Location(Match match, int percentOfW, int percentOfH) {
        this.region = new Region(match);
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

    public Optional<Integer> getPercentOfW() {
        if (isDefinedByXY()) return Optional.empty();
        return Optional.of(position.getPercentW());
    }

    public Optional<Integer> getPercentOfH() {
        if (isDefinedByXY()) return Optional.empty();
        return Optional.of(position.getPercentH());
    }

    public Optional<Region> getRegion() {
        if (isDefinedByXY()) return Optional.empty();
        return Optional.of(region);
    }

    public void addPercentOfW(int addPercent) {
        if (!definedByXY) position.addPercentW(addPercent);
    }

    public void addPercentOfH(int addPercent) {
        if (!definedByXY) position.addPercentH(addPercent);
    }

    public void multiplyPercentOfW(double multiplyBy) {
        if (!definedByXY) position.multiplyPercentW(multiplyBy);
    }

    public void multiplyPercentOfH(double multiplyBy) {
        if (!definedByXY) position.multiplyPercentH(multiplyBy);
    }

    private org.sikuli.script.Location getSikuliLocationFromXY() {
        return new org.sikuli.script.Location(x, y);
    }

    private org.sikuli.script.Location getSikuliLocationFromRegion() {
        //int locX = region.x + Math.max(0, region.w * position.getPercentW() / 100 - 1);
        //int locY = region.y + Math.max(0, region.h * position.getPercentH() / 100 - 1);
        int locX = region.x + (region.w * position.getPercentW() / 100 - 1);
        int locY = region.y + (region.h * position.getPercentH() / 100 - 1);
        return new org.sikuli.script.Location(locX, locY);
    }

    public org.sikuli.script.Location getSikuliLocation() {
        if (isDefinedByXY()) return getSikuliLocationFromXY();
        return getSikuliLocationFromRegion();
    }

    public int getX() {
        return getSikuliLocation().x;
    }

    public int getY() {
        return getSikuliLocation().y;
    }

    public boolean defined() {
        return !isDefinedByXY() || getX() > 0 || getY() > 0;
    }

    public Match toMatch() {
        Match match = new Match(getX(), getY(), 1, 1,1, new Screen());
        match.setTarget(getX(), getY());
        return match;
    }

    public StateLocation inNullState() {
        return new StateLocation.Builder()
                .inState(NULL)
                .withLocation(this)
                .build();
    }

    public ObjectCollection asObjectCollection() {
        StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(this)
                .inState(NULL)
                .setPosition(Position.Name.TOPLEFT)
                .build();
        return new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .build();
    }

    public Location getOpposite() {
        if (region == null) return this;
        return new Location(this.region,
                100 - position.getPercentW(),
                100 - position.getPercentH());
    }

    public Location getOppositeTo(Location location) {
        int addX = 2 * (location.getX() - getX());
        int addY = 2 * (location.getY() - getY());
        return new Location(getX() + addX, getY() + addY);
    }

    public void adjustToRegion() {
        int percentOfW, percentOfH;
        percentOfW = Math.max(Math.min(100, position.getPercentW()), 0);
        percentOfH = Math.max(Math.min(100, position.getPercentH()), 0);
        position = new Position(percentOfW, percentOfH);
    }

    private boolean isDefinedByXY() {
        return definedByXY || region == null;
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
        if (definedByXY) {
            x += plusX;
            y -= minusY;
        } else {// if defined by a region move from the center of the region
            Location center = new Location(region, Position.Name.MIDDLEMIDDLE);
            setPosition(center.getX() + plusX, center.getY() - minusY);
        }
    }

    public boolean equals(Location l) {
        return (x == l.x && y == l.y &&
                //region.equals(l.region) && position == l.position &&
                definedByXY == l.definedByXY);
    }

    public void print() {
        Report.format("%d.%d ",x,y);
    }

    public static class Builder {
        private String name;
        private boolean definedByXY = false;
        private int x = -1;
        private int y = -1;
        private Region region;
        private Position position = new Position(Position.Name.MIDDLEMIDDLE);
        private Position.Name anchor = Position.Name.MIDDLEMIDDLE;

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
            this.definedByXY = location.definedByXY;
            x = location.x;
            y = location.y;
            if (location.isDefinedByXY()) return this;
            int percentOfW, percentOfH;
            if (location.getRegion().isPresent()) this.region = location.getRegion().get();
            if (location.getPercentOfW().isPresent()) percentOfW = location.getPercentOfW().get();
            else percentOfW = 50;
            if (location.getPercentOfH().isPresent()) percentOfH = location.getPercentOfH().get();
            else percentOfH = 50;
            position = new Position(percentOfW, percentOfH);
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

        public Builder setPosition(Position.Name positionName) {
            this.position = new Position(positionName);
            return this;
        }

        public Builder setPosition(int percentOfW, int percentOfH) {
            this.position = new Position(percentOfW, percentOfH);
            return this;
        }

        public Builder fromMatch(Match match) {
            this.region = new Region(match);
            int percentOfW, percentOfH;
            percentOfW = (match.getTarget().x - match.x) / match.w;
            percentOfH = (match.getTarget().y - match.y) / match.y;
            position = new Position(percentOfW, percentOfH);
            return this;
        }

        public Builder setAnchor(Position.Name anchor) {
            this.anchor = anchor;
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
            definedByXY = region == null;
            return location;
        }
    }

}
