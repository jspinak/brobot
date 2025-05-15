package io.github.jspinak.brobot.datatypes.primitives.location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * Location is calculated
 * - when the region is defined, first with the position in the region, then with the offset
 * - when the region is not defined, with the x,y values, then with the offset
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
        if (LocationUtils.isDefinedWithRegion(loc)) {
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
        if (LocationUtils.isDefinedByXY(this)) return Optional.empty();
        return Optional.of(position.getPercentW());
    }

    public Optional<Double> getPercentOfH() {
        if (LocationUtils.isDefinedByXY(this)) return Optional.empty();
        return Optional.of(position.getPercentH());
    }

    public void addPercentOfW(int addPercent) {
        if (LocationUtils.isDefinedWithRegion(this)) position.addPercentW(addPercent);
    }

    public void addPercentOfH(int addPercent) {
        if (LocationUtils.isDefinedWithRegion(this)) position.addPercentH(addPercent);
    }

    public void multiplyPercentOfW(double multiplyBy) {
        if (LocationUtils.isDefinedWithRegion(this)) position.multiplyPercentW(multiplyBy);
    }

    public void multiplyPercentOfH(double multiplyBy) {
        if (LocationUtils.isDefinedWithRegion(this)) position.multiplyPercentH(multiplyBy);
    }

    @JsonIgnore
    private org.sikuli.script.Location getSikuliLocationFromXY() {
        return LocationUtils.getSikuliLocationFromXY(x, y, offsetX, offsetY);
    }

    @JsonIgnore
    private org.sikuli.script.Location getSikuliLocationFromRegion() {
        return LocationUtils.getSikuliLocationFromRegion(region, position, offsetX, offsetY);
    }

    @JsonIgnore
    public org.sikuli.script.Location sikuli() {
        return LocationUtils.getSikuliLocation(this);
    }

    @JsonIgnore
    public int getCalculatedX() {
        if (region == null) {
            return x + offsetX;
        }
        return (int) (region.x() + (region.w() * position.getPercentW()) + offsetX);
    }

    @JsonIgnore
    public int getCalculatedY() {
        if (region == null) {
            return y + offsetY;
        }
        return (int) (region.y() + (region.h() * position.getPercentH()) + offsetY);
    }

    @JsonIgnore
    public int getRegionW() {
        return LocationUtils.getRegionW(this);
    }

    @JsonIgnore
    public int getRegionH() {
        return LocationUtils.getRegionH(this);
    }

    @JsonIgnore
    public boolean defined() {
        return LocationUtils.isDefined(this);
    }

    @JsonIgnore
    public Match toMatch() {
        return LocationUtils.toMatch(this);
    }

    @JsonIgnore
    public StateLocation asStateLocationInNullState() {
        return LocationUtils.asStateLocationInNullState(this);
    }

    @JsonIgnore
    public ObjectCollection asObjectCollection() {
        return LocationUtils.asObjectCollection(this);
    }

    @JsonIgnore
    public Location getOpposite() {
        return LocationUtils.getOpposite(this);
    }

    @JsonIgnore
    public Location getOppositeTo(Location location) {
        return LocationUtils.getOppositeTo(this, location);
    }

    @JsonIgnore
    public void adjustToRegion() {
        LocationUtils.adjustToRegion(this);
    }

    @JsonIgnore
    private boolean isDefinedByXY() {
        return LocationUtils.isDefinedByXY(this);
    }

    @JsonIgnore
    private boolean isDefinedWithRegion() {
        return LocationUtils.isDefinedWithRegion(this);
    }

    @JsonIgnore
    public void setFromCenter(double angle, double distance) {
        LocationUtils.setFromCenter(this, angle, distance);
    }

    /**
     * If the locations are mixed (defined by region and by xy) then it is converted to 'defined by xy'.
     * @param loc the location to add to this one
     */
    public void add(Location loc) {
        LocationUtils.add(this, loc);
    }

    public void print() {
        LocationUtils.print(this);
    }

    @Override
    public String toString() {
        return LocationUtils.toString(this);
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