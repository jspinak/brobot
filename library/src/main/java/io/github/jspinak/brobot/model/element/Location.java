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

    /**
     * Creates a Location from a SikuliX Location.
     *
     * @param sikuliLocation the SikuliX location to convert
     */
    public Location(org.sikuli.script.Location sikuliLocation) {
        this.x = sikuliLocation.x;
        this.y = sikuliLocation.y;
    }

    /**
     * Copy constructor that creates a new Location from an existing Location.
     *
     * @param loc the Location to copy
     */
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

    /**
     * Creates a Location within a Region based on a SikuliX Location's coordinates.
     * The position within the region is calculated as a percentage.
     *
     * @param region the containing Region
     * @param sikuliLocation the absolute coordinates to convert to relative position
     */
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

    /**
     * Sets the position within the region based on absolute coordinates.
     * Converts the absolute coordinates to percentage positions within the region.
     *
     * @param newX the absolute x-coordinate
     * @param newY the absolute y-coordinate
     */
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

    /**
     * Creates a Location at a named position within a Region.
     *
     * @param region the containing Region
     * @param position the named position (e.g., TOP_LEFT, CENTER, BOTTOM_RIGHT)
     */
    public Location(Region region, Positions.Name position) {
        this.region = region;
        this.position = new Position(position);
    }

    /**
     * Creates a Location at a named position on the full screen.
     *
     * @param position the named position (e.g., TOP_LEFT, CENTER, BOTTOM_RIGHT)
     */
    public Location(Positions.Name position) {
        this.region = new Region();
        this.position = new Position(position);
    }

    /**
     * Creates a Location at a percentage-based position within a Region.
     *
     * @param region the containing Region
     * @param percentOfW the horizontal position as percentage (0.0 to 1.0)
     * @param percentOfH the vertical position as percentage (0.0 to 1.0)
     */
    public Location(Region region, double percentOfW, double percentOfH) {
        this.region = region;
        this.position = new Position(percentOfW, percentOfH);
    }

    /**
     * Creates a Location from a Match, preserving its target position and offsets.
     *
     * @param match the Match to extract location from
     */
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

    /**
     * Creates a Location at a specific position within a Match's region.
     *
     * @param match the Match providing the region
     * @param position the relative position within the match
     */
    public Location(Match match, Position position) {
        this.region = match.getRegion();
        this.position = position;
    }

    /**
     * Creates a Location at a percentage-based position within a Match's region.
     *
     * @param match the Match providing the region
     * @param percentOfW the horizontal position as percentage (0 to 100)
     * @param percentOfH the vertical position as percentage (0 to 100)
     */
    public Location(Match match, int percentOfW, int percentOfH) {
        this.region = match.getRegion();
        this.position = new Position(percentOfW, percentOfH);
    }

    /**
     * Creates a new Location offset from an existing Location.
     *
     * @param location the base Location
     * @param addX the horizontal offset in pixels
     * @param addY the vertical offset in pixels
     */
    public Location(Location location, int addX, int addY) {
        this.x = location.x + addX;
        this.y = location.y + addY;
    }

    /**
     * Creates a Location offset from a SikuliX Location.
     *
     * @param location the base SikuliX location
     * @param addX the horizontal offset in pixels
     * @param addY the vertical offset in pixels
     */
    public Location(org.sikuli.script.Location location, int addX, int addY) {
        this.x = location.x + addX;
        this.y = location.y + addY;
    }

    /**
     * Gets the horizontal position as a percentage of the region's width.
     *
     * @return Optional containing the percentage (0.0 to 1.0), or empty if location is absolute
     */
    public Optional<Double> getPercentOfW() {
        if (LocationUtils.isDefinedByXY(this)) return Optional.empty();
        return Optional.of(position.getPercentW());
    }

    /**
     * Gets the vertical position as a percentage of the region's height.
     *
     * @return Optional containing the percentage (0.0 to 1.0), or empty if location is absolute
     */
    public Optional<Double> getPercentOfH() {
        if (LocationUtils.isDefinedByXY(this)) return Optional.empty();
        return Optional.of(position.getPercentH());
    }

    /**
     * Adds to the horizontal percentage position within the region.
     *
     * @param addPercent the percentage to add (can be negative)
     */
    public void addPercentOfW(int addPercent) {
        if (LocationUtils.isDefinedWithRegion(this)) position.addPercentW(addPercent);
    }

    /**
     * Adds to the vertical percentage position within the region.
     *
     * @param addPercent the percentage to add (can be negative)
     */
    public void addPercentOfH(int addPercent) {
        if (LocationUtils.isDefinedWithRegion(this)) position.addPercentH(addPercent);
    }

    /**
     * Multiplies the horizontal percentage position by a factor.
     *
     * @param multiplyBy the multiplication factor
     */
    public void multiplyPercentOfW(double multiplyBy) {
        if (LocationUtils.isDefinedWithRegion(this)) position.multiplyPercentW(multiplyBy);
    }

    /**
     * Multiplies the vertical percentage position by a factor.
     *
     * @param multiplyBy the multiplication factor
     */
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

    /**
     * Calculates the absolute x-coordinate including any offsets.
     *
     * @return the calculated x-coordinate in screen pixels
     */
    @JsonIgnore
    public int getCalculatedX() {
        if (region == null || position == null) {
            return x + offsetX;
        }
        return (int) (region.x() + (region.w() * position.getPercentW()) + offsetX);
    }

    /**
     * Calculates the absolute y-coordinate including any offsets.
     *
     * @return the calculated y-coordinate in screen pixels
     */
    @JsonIgnore
    public int getCalculatedY() {
        if (region == null || position == null) {
            return y + offsetY;
        }
        return (int) (region.y() + (region.h() * position.getPercentH()) + offsetY);
    }

    /**
     * Gets the width of the containing region.
     *
     * @return the region width in pixels, or 0 if no region is set
     */
    @JsonIgnore
    public int getRegionW() {
        return LocationUtils.getRegionW(this);
    }

    /**
     * Gets the height of the containing region.
     *
     * @return the region height in pixels, or 0 if no region is set
     */
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

    /**
     * Converts this Location to a Match with a 1x1 pixel region at the location.
     *
     * @return a new Match at this location
     */
    @JsonIgnore
    public Match toMatch() {
        return LocationUtils.toMatch(this);
    }

    /**
     * Creates a StateLocation from this Location, owned by the NULL state.
     *
     * @return a StateLocation in the NULL state
     */
    @JsonIgnore
    public StateLocation asStateLocationInNullState() {
        return LocationUtils.asStateLocationInNullState(this);
    }

    /**
     * Converts this Location to an ObjectCollection for use with Action methods.
     *
     * @return an ObjectCollection containing this Location
     */
    @JsonIgnore
    public ObjectCollection asObjectCollection() {
        return LocationUtils.asObjectCollection(this);
    }

    /**
     * Gets the opposite location across the center of the region.
     * For example, if this location is in the top-left, returns bottom-right.
     *
     * @return the opposite Location
     */
    @JsonIgnore
    public Location getOpposite() {
        return LocationUtils.getOpposite(this);
    }

    /**
     * Gets the location opposite to this one relative to a given center location.
     *
     * @param location the center location
     * @return the opposite Location relative to the center
     */
    @JsonIgnore
    public Location getOppositeTo(Location location) {
        return LocationUtils.getOppositeTo(this, location);
    }

    /**
     * Adjusts this location's coordinates to ensure they fall within its region's boundaries.
     * Clamps coordinates that exceed the region bounds.
     */
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

    /**
     * Sets this location's position based on an angle and distance from its region's center.
     * Uses polar coordinates to position the location.
     *
     * @param angle the angle in radians from the center
     * @param distance the distance in pixels from the center
     */
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

    /**
     * Prints this location's details to the console for debugging.
     */
    public void print() {
        LocationUtils.print(this);
    }

    /**
     * Returns a string representation of this location.
     *
     * @return string representation including coordinates and/or region information
     */
    @Override
    public String toString() {
        return LocationUtils.toString(this);
    }

    /**
     * Builder class for constructing Location instances with a fluent API.
     */
    public static class Builder {
        private String name;
        private int x = -1;
        private int y = -1;
        private Region region;
        private Position position = new Position(Positions.Name.MIDDLEMIDDLE);
        private Positions.Name anchor = Positions.Name.MIDDLEMIDDLE;
        private int offsetX = 0;
        private int offsetY = 0;

        /**
         * Sets the name for this location.
         *
         * @param name the descriptive name
         * @return this builder for method chaining
         */
        public Builder called(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the absolute x,y coordinates.
         *
         * @param x the x-coordinate
         * @param y the y-coordinate
         * @return this builder for method chaining
         */
        public Builder setXY(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        /**
         * Sets the coordinates from a SikuliX Location.
         *
         * @param sikuliLocation the SikuliX location to copy coordinates from
         * @return this builder for method chaining
         */
        public Builder setXY(org.sikuli.script.Location sikuliLocation) {
            this.x = sikuliLocation.x;
            this.y = sikuliLocation.y;
            return this;
        }

        /**
         * Copies all properties from an existing Location.
         *
         * @param location the Location to copy from
         * @return this builder for method chaining
         */
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

        /**
         * Sets the containing region for this location.
         *
         * @param region the containing region
         * @return this builder for method chaining
         */
        public Builder setRegion(Region region) {
            this.region = region;
            return this;
        }

        /**
         * Sets the region from a StateRegion's search region.
         *
         * @param region the StateRegion to extract the search region from
         * @return this builder for method chaining
         */
        public Builder setRegion(StateRegion region) {
            this.region = region.getSearchRegion();
            return this;
        }

        /**
         * Sets the relative position within the region.
         *
         * @param position the position object with percentage values
         * @return this builder for method chaining
         */
        public Builder setPosition(Position position) {
            this.position = position;
            return this;
        }

        /**
         * Sets the position using a named position constant.
         *
         * @param positionName the named position (e.g., TOP_LEFT, CENTER)
         * @return this builder for method chaining
         */
        public Builder setPosition(Positions.Name positionName) {
            this.position = new Position(positionName);
            return this;
        }

        /**
         * Sets the position using percentage values.
         *
         * @param percentOfW horizontal position as percentage (0-100)
         * @param percentOfH vertical position as percentage (0-100)
         * @return this builder for method chaining
         */
        public Builder setPosition(int percentOfW, int percentOfH) {
            this.position = new Position(percentOfW, percentOfH);
            return this;
        }

        /**
         * Initializes this location from a Match's target location.
         *
         * @param match the Match to extract location data from
         * @return this builder for method chaining
         */
        public Builder fromMatch(Match match) {
            this.region = new Region(match.getRegion());
            this.position = new Position(match.getTarget().getPosition());
            this.x = match.getTarget().x;
            this.y = match.getTarget().y;
            this.offsetX = match.getTarget().getOffsetX();
            this.offsetY = match.getTarget().getOffsetY();
            return this;
        }

        /**
         * Sets the anchor position for this location.
         *
         * @param anchor the anchor position name
         * @return this builder for method chaining
         */
        public Builder setAnchor(Positions.Name anchor) {
            this.anchor = anchor;
            return this;
        }

        /**
         * Sets the horizontal offset from the calculated position.
         *
         * @param offsetX the x-axis offset in pixels
         * @return this builder for method chaining
         */
        public Builder setOffsetX(int offsetX) {
            this.offsetX = offsetX;
            return this;
        }

        /**
         * Sets the vertical offset from the calculated position.
         *
         * @param offsetY the y-axis offset in pixels
         * @return this builder for method chaining
         */
        public Builder setOffsetY(int offsetY) {
            this.offsetY = offsetY;
            return this;
        }

        /**
         * Builds and returns a new Location with the configured properties.
         *
         * @return a new Location instance
         */
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
