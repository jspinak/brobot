package io.github.jspinak.brobot.util.location;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

/**
 * Comprehensive utility methods for Location manipulation and calculations in Brobot.
 * 
 * <p>LocationUtils provides a rich set of static methods for working with Location objects, 
 * supporting both absolute (x,y) and relative (region-based) location representations. 
 * These utilities handle conversions, calculations, and transformations while maintaining 
 * the dual nature of Location objects in the framework.</p>
 * 
 * <p>Operation categories:
 * <ul>
 *   <li><b>Conversions</b>: Transform between Brobot and SikuliX location representations</li>
 *   <li><b>Type Checking</b>: Determine if locations are absolute or relative</li>
 *   <li><b>Calculations</b>: Geometric operations like opposites, additions, angles</li>
 *   <li><b>Transformations</b>: Convert locations to matches, state objects, collections</li>
 *   <li><b>Utilities</b>: Validation, comparison, string representation</li>
 * </ul>
 * </p>
 * 
 * <p>Location type handling:
 * <ul>
 *   <li><b>Absolute (XY)</b>: Defined by x,y coordinates directly</li>
 *   <li><b>Relative (Region)</b>: Defined by position within a region</li>
 *   <li>Methods adapt behavior based on location type</li>
 *   <li>Supports conversion between types when needed</li>
 * </ul>
 * </p>
 * 
 * <p>Key operations:
 * <ul>
 *   <li>{@code getSikuliLocation()}: Convert to SikuliX format with offset support</li>
 *   <li>{@code getOpposite()}: Find opposite point within region</li>
 *   <li>{@code setFromCenter()}: Position based on angle and distance</li>
 *   <li>{@code adjustToRegion()}: Ensure location stays within bounds</li>
 *   <li>{@code add()}: Combine locations with type awareness</li>
 * </ul>
 * </p>
 * 
 * <p>Integration features:
 * <ul>
 *   <li>Creates StateLocation objects for state-based operations</li>
 *   <li>Generates ObjectCollections for action execution</li>
 *   <li>Converts to Match objects for result compatibility</li>
 *   <li>Supports null state operations for stateless contexts</li>
 * </ul>
 * </p>
 * 
 * <p>Geometric calculations:
 * <ul>
 *   <li>Polar coordinate support (angle/distance from center)</li>
 *   <li>Opposite point calculations (reflection)</li>
 *   <li>Vector addition with type-aware semantics</li>
 *   <li>Boundary enforcement for relative locations</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, LocationUtils enables sophisticated spatial reasoning 
 * about point locations while abstracting the complexity of dual representation modes. 
 * This flexibility allows the framework to work efficiently with both fixed screen 
 * coordinates and adaptive region-based positions.</p>
 * 
 * @since 1.0
 * @see Location
 * @see Position
 * @see Region
 * @see StateLocation
 */
public class LocationUtils {

    /**
     * Get a Sikuli Location from x,y coordinates
     */
    public static org.sikuli.script.Location getSikuliLocationFromXY(int x, int y, int offsetX, int offsetY) {
        return new org.sikuli.script.Location(x + offsetX, y + offsetY);
    }

    /**
     * Get a Sikuli Location from a region and position
     */
    public static org.sikuli.script.Location getSikuliLocationFromRegion(Region region, Position position, int offsetX, int offsetY) {
        double locX = region.x() + (region.w() * position.getPercentW()) + offsetX;
        double locY = region.y() + (region.h() * position.getPercentH()) + offsetY;
        return new org.sikuli.script.Location(locX, locY);
    }

    /**
     * Get Sikuli Location based on definition type (by XY or by Region)
     */
    public static org.sikuli.script.Location getSikuliLocation(Location location) {
        if (isDefinedByXY(location)) {
            return getSikuliLocationFromXY(location.getCalculatedX(), location.getCalculatedY(),
                    location.getOffsetX(), location.getOffsetY());
        }
        return getSikuliLocationFromRegion(location.getRegion(), location.getPosition(),
                location.getOffsetX(), location.getOffsetY());
    }

    /**
     * Check if location is defined by x,y coordinates
     */
    public static boolean isDefinedByXY(Location location) {
        return location.getRegion() == null;
    }

    /**
     * Check if location is defined with a region
     */
    public static boolean isDefinedWithRegion(Location location) {
        return !isDefinedByXY(location);
    }

    /**
     * Get region width
     */
    public static int getRegionW(Location location) {
        if (isDefinedByXY(location)) return 1;
        return location.getRegion().w();
    }

    /**
     * Get region height
     */
    public static int getRegionH(Location location) {
        if (isDefinedByXY(location)) return 1;
        return location.getRegion().h();
    }

    /**
     * Check if location is defined
     */
    public static boolean isDefined(Location location) {
        return !isDefinedByXY(location) || location.getCalculatedX() > 0 || location.getCalculatedY() > 0;
    }

    /**
     * Convert location to match
     */
    public static Match toMatch(Location location) {
        return new Match.Builder()
                .setRegion(location.getCalculatedX(), location.getCalculatedY(), 1, 1)
                .build();
    }

    /**
     * Create a StateLocation in null state
     */
    public static StateLocation asStateLocationInNullState(Location location) {
        return new StateLocation.Builder()
                .setOwnerStateName("null")
                .setLocation(location)
                .build();
    }

    /**
     * Create an ObjectCollection
     */
    public static ObjectCollection asObjectCollection(Location location) {
        StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(location)
                .setOwnerStateName("null")
                .setPosition(Positions.Name.TOPLEFT)
                .build();
        return new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .build();
    }

    /**
     * Get the opposite location
     */
    public static Location getOpposite(Location location) {
        if (location.getRegion() == null) return location;
        return new Location(location.getRegion(),
                1 - location.getPosition().getPercentW(),
                1 - location.getPosition().getPercentH());
    }

    /**
     * Get the location opposite to another location
     */
    public static Location getOppositeTo(Location location, Location otherLocation) {
        int addX = 2 * (otherLocation.getCalculatedX() - location.getCalculatedX());
        int addY = 2 * (otherLocation.getCalculatedY() - location.getCalculatedY());
        return new Location(location.getCalculatedX() + addX, location.getCalculatedY() + addY);
    }

    /**
     * Adjust location to be within region bounds
     */
    public static void adjustToRegion(Location location) {
        if (!isDefinedWithRegion(location)) return;

        double percentOfW = Math.max(Math.min(1, location.getPosition().getPercentW()), 0);
        double percentOfH = Math.max(Math.min(1, location.getPosition().getPercentH()), 0);
        location.setPosition(new Position(percentOfW, percentOfH));
    }

    /**
     * Set location based on angle and distance from center
     */
    public static void setFromCenter(Location location, double angle, double distance) {
        double rad = Math.toRadians(angle);
        int plusX = (int)(distance * Math.cos(rad));
        int minusY = (int)(distance * Math.sin(rad)); // the angle is the cartesian angle

        if (isDefinedByXY(location)) {
            location.setX(location.getCalculatedX() + plusX);
            location.setY(location.getCalculatedY() - minusY);
        } else { // if defined by a region move from the center of the region
            Location center = new Location(location.getRegion(), Positions.Name.MIDDLEMIDDLE);
            location.setPosition(center.getCalculatedX() + plusX, center.getCalculatedY() - minusY);
        }
    }

    /**
     * Check if two locations are equal
     */
    public static boolean equals(Location location1, Location location2) {
        return (location1.getCalculatedX() == location2.getCalculatedX() &&
                location1.getCalculatedY() == location2.getCalculatedY() &&
                isDefinedByXY(location1) == isDefinedByXY(location2));
    }

    /**
     * Add one location to another
     */
    public static void add(Location location, Location locationToAdd) {
        if (isDefinedByXY(location) && isDefinedByXY(locationToAdd)) {
            location.setX(location.getCalculatedX() + locationToAdd.getCalculatedX());
            location.setY(location.getCalculatedY() + locationToAdd.getCalculatedY());
            return;
        }

        if (isDefinedWithRegion(location) && isDefinedWithRegion(locationToAdd)) {
            Position position = location.getPosition();
            position.addPercentW(locationToAdd.getPosition().getPercentW());
            position.addPercentH(locationToAdd.getPosition().getPercentH());
            return;
        }

        if (isDefinedByXY(location)) {
            location.setX(location.getCalculatedX() +
                    (int)(locationToAdd.getPosition().getPercentW() *
                            (double)location.getRegion().w()));
            location.setY(location.getCalculatedY() +
                    (int)(locationToAdd.getPosition().getPercentH() *
                            (double)location.getRegion().h()));
            return;
        }

        location.setX(location.getCalculatedX() + locationToAdd.getCalculatedX());
        location.setY(location.getCalculatedX() + locationToAdd.getCalculatedY());
    }

    /**
     * Print location
     */
    public static void print(Location location) {
        ConsoleReporter.format("%d.%d ", location.getCalculatedX(), location.getCalculatedY());
    }

    /**
     * Get string representation of location
     */
    public static String toString(Location location) {
        return String.format("L[%d.%d]", location.getCalculatedX(), location.getCalculatedY());
    }
}