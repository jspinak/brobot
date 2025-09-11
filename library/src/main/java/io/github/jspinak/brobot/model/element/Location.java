package io.github.jspinak.brobot.model.element;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.util.location.LocationUtils;

import lombok.Data;

/**
 * Represents a point on the screen in the Brobot model-based GUI automation framework.
 *
 * <p>A Location defines a specific point that can be specified in two ways:
 *
 * <ul>
 *   <li>Absolute coordinates: Using x,y pixel values directly
 *   <li>Relative position: As a percentage position within a Region
 * </ul>
 *
 * Both methods support optional x,y offsets for fine-tuning the final position.
 *
 * <p>In the model-based approach, Locations are essential for:
 *
 * <ul>
 *   <li>Specifying click targets within GUI elements
 *   <li>Defining anchor points for spatial relationships between elements
 *   <li>Positioning the mouse for hover actions
 *   <li>Creating dynamic positions that adapt to different screen sizes
 * </ul>
 *
 * <p>The relative positioning feature is particularly powerful in model-based automation as it
 * allows locations to remain valid even when GUI elements move or resize, making automation more
 * robust to UI changes.
 *
 * @since 1.0
 * @see Region
 * @see Position
 * @see StateLocation
 * @see Match
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

    /** Creates a Location at the origin (0,0). */
    public Location() {
        this.x = 0;
        this.y = 0;
    }

    /**
     * Creates a Location at the specified absolute coordinates.
     *
     * @param x the x-coordinate in pixels
     * @param y the y-coordinate in pixels
     */
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

    /**
     * Creates a Location at the center of the specified Region.
     *
     * <p>This constructor is useful for targeting the middle of GUI elements in a way that remains
     * valid even if the element moves or resizes.
     *
     * @param region the Region to center the Location in
     */
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

    /**
     * Creates a Location at a specific position within a Region.
     *
     * <p>The Position specifies where within the Region this Location points to, using percentage
     * values (0.0 to 1.0) for both width and height.
     *
     * @param region the Region containing this Location
     * @param position the relative position within the Region
     */
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
        if (match.getTarget().getPosition() != null) {
            this.position = new Position(match.getTarget().getPosition());
        }
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

    /**
     * Converts this Location to a SikuliX Location for compatibility.
     *
     * <p>This method calculates the final screen coordinates (including any offsets) and returns
     * them as a SikuliX Location object for use with Sikuli operations.
     *
     * @return a SikuliX Location with the calculated screen coordinates
     */
    @JsonIgnore
    public org.sikuli.script.Location sikuli() {
        return LocationUtils.getSikuliLocation(this);
    }

    @JsonIgnore
    public int getCalculatedX() {
        if (region == null || position == null) {
            return x + offsetX;
        }
        return (int) (region.x() + (region.w() * position.getPercentW()) + offsetX);
    }

    @JsonIgnore
    public int getCalculatedY() {
        if (region == null || position == null) {
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

    /**
     * Checks if this Location has valid coordinates.
     *
     * <p>A Location is considered defined if it has either valid x,y coordinates or a valid Region
     * with position. This is important for validating click targets before attempting interactions.
     *
     * @return true if the Location can produce valid screen coordinates
     */
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
     * Adds another Location's coordinates to this Location.
     *
     * <p>If the locations use different definition methods (absolute vs relative), the result will
     * be converted to absolute coordinates. This is useful for creating offset positions from a
     * base location.
     *
     * @param loc the Location to add to this one
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
