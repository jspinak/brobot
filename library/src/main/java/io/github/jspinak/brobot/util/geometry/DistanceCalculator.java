package io.github.jspinak.brobot.util.geometry;

import io.github.jspinak.brobot.util.geometry.Sector;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Comprehensive geometric calculations for distances, angles, and spatial relationships.
 * <p>
 * This utility class provides essential geometric operations for Brobot's spatial
 * reasoning, including distance measurements, angle calculations, and location
 * transformations. It handles the screen coordinate system where Y increases
 * downward, unlike traditional Cartesian coordinates.
 * <p>
 * Key capabilities:
 * <ul>
 * <li>Euclidean distance calculations between points and matches</li>
 * <li>Angle measurements in degrees (-180 to 180)</li>
 * <li>Angle normalization and scale conversion</li>
 * <li>Vector operations and location transformations</li>
 * <li>Sector analysis for angle clustering</li>
 * </ul>
 * <p>
 * Coordinate system notes:
 * <ul>
 * <li>Screen coordinates: Y increases downward</li>
 * <li>Angles: 0° is East, positive counterclockwise</li>
 * <li>Range: -180° to 180° (standard mathematical convention)</li>
 * <li>Conversions handle the inverted Y-axis automatically</li>
 * </ul>
 * <p>
 * Angle operations:
 * <ul>
 * <li>Median angle: Vector sum approach for averaging angles</li>
 * <li>Scale conversion: Handles circular wraparound at ±180°</li>
 * <li>Sector analysis: Finds gaps in angle distributions</li>
 * <li>Span calculation: Measures angular coverage</li>
 * </ul>
 * <p>
 * Common use cases:
 * <ul>
 * <li>Calculating distances between UI elements</li>
 * <li>Determining relative positions and directions</li>
 * <li>Gesture path analysis and validation</li>
 * <li>Spatial clustering and grouping</li>
 * <li>Navigation and movement planning</li>
 * </ul>
 * <p>
 * Thread safety: All methods are stateless and thread-safe.
 *
 * @see Location
 * @see Match
 * @see Sector
 * @see MovementAnalyzer
 */
@Component
public class DistanceCalculator {

    /**
     * Calculates Euclidean distance using the Pythagorean theorem.
     *
     * @param a first component (typically X distance)
     * @param b second component (typically Y distance)
     * @return hypotenuse length
     */
    private double dist(double a, double b) {
        return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    /**
     * Calculates X and Y distances between two locations.
     * <p>
     * Handles the screen coordinate system where Y increases downward
     * by inverting the Y difference to match mathematical conventions.
     *
     * @param loc1 starting location
     * @param loc2 ending location
     * @return list containing [deltaX, deltaY] in Cartesian coordinates
     */
    private List<Integer> getDistPair(Location loc1, Location loc2) {
        List<Integer> distPair = new ArrayList<>();
        distPair.add(loc2.getCalculatedX() - loc1.getCalculatedX());
        // the y difference is the opposite on the computer screen as in a cartesian plane
        distPair.add(loc1.getCalculatedY() - loc2.getCalculatedY());
        //System.out.print("|cartesian dist x.y "+distPair.get(0)+"."+distPair.get(1)+" ");
        return distPair;
    }

    /**
     * Converts distance pair to scalar distance.
     *
     * @param distPair [deltaX, deltaY] components
     * @return Euclidean distance
     */
    private double getDist(List<Integer> distPair) {
        return dist(distPair.get(0), distPair.get(1));
    }

    /**
     * Calculates angle in degrees from distance components.
     * <p>
     * Uses atan2 for proper quadrant handling. Returns angles
     * in standard mathematical convention: 0° = East, positive
     * counterclockwise, range -180° to 180°.
     *
     * @param distPair [deltaX, deltaY] components
     * @return angle in degrees
     */
    private double getDegree(List<Integer> distPair) {
        return Math.toDegrees(Math.atan2((double)distPair.get(1), (double)distPair.get(0)));
    }

    /**
     * Calculates distance between two locations.
     * <p>
     * Delegates to euclidean method for consistency.
     *
     * @param loc1 first location
     * @param loc2 second location
     * @return Euclidean distance in pixels
     */
    public double getDistance(Location loc1, Location loc2) { return euclidean(loc1, loc2); }

    /**
     * Calculates distance between centers of two matches.
     * <p>
     * Uses the center points of match regions for distance
     * calculation, appropriate for measuring spacing between
     * UI elements.
     *
     * @param match1 first match
     * @param match2 second match
     * @return distance between match centers in pixels
     */
    public double getDistance(Match match1, Match match2) {
        return Math.sqrt(Math.pow(match1.getRegion().sikuli().getCenter().getX() - match2.getRegion().sikuli().getCenter().getX(), 2) +
                Math.pow(match1.getRegion().sikuli().getCenter().getY() - match2.getRegion().sikuli().getCenter().getY(), 2));
    }

    /**
     * Calculates Euclidean distance between two locations.
     *
     * @param loc1 first location
     * @param loc2 second location
     * @return straight-line distance in pixels
     */
    public double euclidean(Location loc1, Location loc2) {
        return getDist(getDistPair(loc1, loc2));
    }

    /**
     * Calculates Euclidean distance from location to match center.
     *
     * @param loc starting location
     * @param match target match
     * @return distance to match center in pixels
     */
    public double euclidean(Location loc, Match match) {
        return getDist(getDistPair(loc, new Location(match)));
    }

    /**
     * Calculates angle from first location to second location.
     * <p>
     * Returns the direction angle in degrees, where 0° points East,
     * 90° points North (due to inverted Y), -90° points South,
     * and ±180° points West.
     *
     * @param loc1 starting location
     * @param loc2 target location
     * @return angle in degrees (-180 to 180)
     */
    public double getAngle(Location loc1, Location loc2) {
        return getDegree(getDistPair(loc1, loc2));
    }

    /**
     * Calculates angle from location to match center.
     *
     * @param loc starting location
     * @param match target match
     * @return angle in degrees (-180 to 180)
     */
    public double getAngle(Location loc, Match match) {
        return getDegree(getDistPair(loc, new Location(match)));
    }

    /**
     * Calculates angle from origin (0,0) to location.
     * <p>
     * Useful for determining absolute screen positions and
     * quadrant analysis.
     *
     * @param loc target location
     * @return angle from origin in degrees
     */
    public double getAngleFrom00(Location loc) {
        return getAngle(new Location(0,0), loc);
    }
    /**
     * Calculates the angular difference between two vectors from a common origin.
     * <p>
     * Returns the smallest angle between the vectors (acute angle),
     * always positive. For example, returns 30° instead of 330°.
     * <p>
     * Vector definitions:
     * <ul>
     * <li>Vector 1: from center to loc1</li>
     * <li>Vector 2: from center to loc2</li>
     * </ul>
     *
     * @param center common origin point for both vectors
     * @param loc1 endpoint of first vector
     * @param loc2 endpoint of second vector
     * @return acute angle between vectors in degrees (0 to 180)
     */
    public double getDegreesBetween(Location center, Location loc1, Location loc2) {
        double angle1 = getAngle(center, loc1);
        double angle2 = getAngle(center, loc2);
        return getDegreesBetween(angle1, angle2);
    }

    /**
     * Calculates angular difference between vectors to two matches.
     * <p>
     * Convenience method using match centers as vector endpoints.
     *
     * @param center origin match for both vectors
     * @param match1 endpoint of first vector
     * @param match2 endpoint of second vector
     * @return acute angle between vectors in degrees
     */
    public double getDegreesBetween(Match center, Match match1, Match match2) {
        return getDegreesBetween(new Location(center), new Location(match1), new Location(match2));
    }

    /**
     * Calculates the difference between two angles.
     * <p>
     * Handles circular wraparound to ensure the result represents
     * the shortest angular distance.
     *
     * @param angle1 first angle in degrees
     * @param angle2 second angle in degrees
     * @return angular difference in degrees
     */
    public double getDegreesBetween(double angle1, double angle2) {
        List<Double> adjustedAngles = convertAnglesToSameScale(angle1, angle2);
        return adjustedAngles.get(1) - adjustedAngles.get(0);
    }

    /**
     * Normalizes angles to prevent wraparound discontinuities.
     * <p>
     * On a circular scale, there's a discontinuity at ±180°. This method
     * adjusts angle values so that clustered angles don't span this break,
     * enabling proper averaging and comparison.
     * <p>
     * Algorithm:
     * <ol>
     * <li>Find median angle of the cluster</li>
     * <li>Identify the opposite angle (180° away)</li>
     * <li>Adjust angles near the break to maintain continuity</li>
     * </ol>
     * <p>
     * Example: [170°, -170°, -160°] becomes [170°, 190°, 200°]
     *
     * @param angles list of angles to normalize
     * @return normalized angles without wraparound breaks
     */
    public List<Double> convertAnglesToSameScale(List<Double> angles) {
        double medianAngle = getMedianAngle(angles);
        double oppositeMedian = getOppositeAngle(medianAngle);
        List<Double> newAngles = new ArrayList<>();
        angles.forEach(a -> {
            if (oppositeMedian > 0 && a > oppositeMedian) newAngles.add(a - 360);
            else if (oppositeMedian < 0 && a < oppositeMedian) newAngles.add(a + 360);
            else newAngles.add(a);
        });
        return newAngles;
    }

    /**
     * Calculates the angle 180° opposite to the given angle.
     * <p>
     * Maintains the -180° to 180° range convention.
     *
     * @param angle input angle in degrees
     * @return opposite angle, 180° away
     */
    public double getOppositeAngle(double angle) {
        return angle > 0 ? angle - 180 : angle + 180;
    }

    /**
     * Varargs version of angle scale conversion.
     *
     * @param angles variable number of angles to normalize
     * @return normalized angles without wraparound breaks
     */
    public List<Double> convertAnglesToSameScale(Double... angles) {
        return convertAnglesToSameScale(Arrays.asList(angles));
    }

    /**
     * Calculates the median angle using vector sum approach.
     * <p>
     * Converts angles to unit vectors, sums them, and finds the
     * resulting angle. This method properly handles angle averaging
     * on a circular scale.
     *
     * @param angles list of angles in degrees
     * @return median angle in degrees
     */
    public double getMedianAngle(List<Double> angles) {
        List<Location> standardLocations = new ArrayList<>();
        angles.forEach(angle -> standardLocations.add(getStandardLocation(angle)));
        return getMedianAngle(new Location(0,0), standardLocations.toArray(new Location[0]));
    }

    public double getMedianAngle(Double... angles) {
        return getMedianAngle(Arrays.asList(angles));
    }

    /**
     * Calculates median angle to multiple locations using vector sum.
     * <p>
     * This method finds the average direction by:
     * <ol>
     * <li>Summing all X coordinates</li>
     * <li>Summing all Y coordinates</li>
     * <li>Calculating angle to the summed point</li>
     * </ol>
     * This approach properly handles circular angle averaging.
     *
     * @param startLocation origin point for angle calculation
     * @param locations target locations
     * @return median angle from start to all locations
     */
    public double getMedianAngle(Location startLocation, List<Location> locations) {
        int x = locations.stream().mapToInt(Location::getCalculatedX).sum();
        int y = locations.stream().mapToInt(Location::getCalculatedY).sum();
        return getAngle(startLocation, new Location(x,y));
    }

    public double getMedianAngle(Location startLocation, Location... locations) {
        return getMedianAngle(startLocation, Arrays.asList(locations));
    }

    /**
     * Creates a standard reference location at given angle.
     * <p>
     * Generates a location 1000 pixels from origin at the specified
     * angle. Used for angle calculations and normalization where
     * actual distance doesn't matter.
     *
     * @param angle desired angle in degrees
     * @return location at angle with distance 1000 from origin
     */
    public Location getStandardLocation(double angle) {
        return getLocation(angle, 1000, new Location(0,0));
    }

    /**
     * Calculates a location at specified angle and distance from start.
     * <p>
     * Performs polar to Cartesian conversion, accounting for the
     * inverted Y-axis in screen coordinates. Results are rounded
     * to nearest pixel.
     * <p>
     * Coordinate transformation:
     * <ul>
     * <li>X = distance × cos(angle)</li>
     * <li>Y = -distance × sin(angle) (inverted for screen)</li>
     * </ul>
     *
     * @param angle direction in degrees (0° = East)
     * @param distance distance in pixels
     * @param startingLocation origin point
     * @return new location at specified angle and distance
     */
    public Location getLocation(double angle, double distance, Location startingLocation) {
        double addX = distance * Math.cos(Math.toRadians(angle));
        double addY = - distance * Math.sin(Math.toRadians(angle));
        return new Location((int)Math.round(startingLocation.getCalculatedX() + addX),
                (int)Math.round(startingLocation.getCalculatedY() + addY));
    }

    /**
     * Calculates displacement vector between two locations.
     * <p>
     * Returns a Location object representing the vector from
     * location1 to location2. The result can be used for
     * relative positioning and movement calculations.
     *
     * @param location1 starting location
     * @param location2 ending location
     * @return displacement vector as Location (deltaX, deltaY)
     */
    public Location getLocation(Location location1, Location location2) {
        return new Location(location2.getCalculatedX() - location1.getCalculatedX(), location2.getCalculatedY() - location1.getCalculatedY());
    }

    /**
     * Reverses angle scale normalization for a list.
     * <p>
     * Converts angles back to standard -180° to 180° range after
     * scale conversion operations.
     *
     * @param angles normalized angles to convert back
     * @return angles in standard -180° to 180° range
     */
    public List<Double> undueConversion(List<Double> angles) {
        List<Double> originalList = new ArrayList<>();
        angles.forEach(a -> originalList.add(undueConversion(a)));
        return originalList;
    }

    /**
     * Normalizes angle to standard -180° to 180° range.
     * <p>
     * Adjusts angles outside the standard range by adding or
     * subtracting 360° as needed.
     *
     * @param a angle to normalize
     * @return angle in -180° to 180° range
     */
    public double undueConversion(double a) {
        if (a < -179) return a + 360;
        else if (a > 180) return a - 360;
        return a;
    }

    /**
     * Finds the leftmost boundary angle in an angle distribution.
     * <p>
     * Identifies the angle that borders the largest gap when moving
     * counterclockwise. This represents the left edge of the main
     * cluster of angles.
     * <p>
     * Algorithm:
     * <ol>
     * <li>Find the largest angular gap between consecutive angles</li>
     * <li>Return the angle on the right side of this gap</li>
     * </ol>
     *
     * @param angles list of angles to analyze
     * @return leftmost angle of the main cluster
     */
    public double getLeftmostAngle(List<Double> angles) {
        Sector largestSector = getLargestSector(angles);
        return largestSector.getRightAngle();
    }

    /**
     * Finds the rightmost boundary angle in an angle distribution.
     * <p>
     * Identifies the angle that borders the largest gap when moving
     * clockwise. This represents the right edge of the main cluster.
     *
     * @param angles list of angles to analyze
     * @return rightmost angle of the main cluster
     */
    public double getRightmostAngle(List<Double> angles) {
        Sector largestSector = getLargestSector(angles);
        return largestSector.getLeftAngle();
    }

    /**
     * Calculates the angular span covered by a set of angles.
     * <p>
     * Returns the total angular coverage, which is 360° minus
     * the largest gap. This indicates how spread out the angles
     * are around the circle.
     *
     * @param angles list of angles to measure
     * @return angular span in degrees (0 to 360)
     */
    public double getSpan(List<Double> angles) {
        if (angles.isEmpty()) return 0.0;
        return 360 - getLargestSector(angles).getSpan();
    }

    /**
     * Finds the central angle of an angle distribution.
     * <p>
     * Calculates the angle that best represents the center of
     * the angle cluster, accounting for the largest gap. The
     * result points away from the gap toward the cluster center.
     *
     * @param angles list of angles to analyze
     * @return central angle of the distribution
     */
    public double getMiddleAngle(List<Double> angles) {
        Sector largestSector = getLargestSector(angles);
        double angleBetween = getMedianAngle(largestSector.getLeftAngle(), largestSector.getRightAngle());
        if (largestSector.getSpan() < 180) angleBetween *= -1;
        return angleBetween;
    }

    /**
     * Identifies the largest angular gap in a set of angles.
     * <p>
     * Finds the sector (angular range) with the maximum span between
     * consecutive angles. This is useful for identifying the main
     * cluster of angles and its boundaries.
     * <p>
     * Side effects: Modifies the input list by adding first element
     * to the end for circular comparison.
     *
     * @param angles list of angles to analyze; modified during processing
     * @return Sector representing the largest gap
     */
    public Sector getLargestSector(List<Double> angles) {
        Sector largestSector = new Sector(angles.get(0), angles.get(0));
        double largestGap = 0.0;
        angles.sort(Comparator.naturalOrder());
        angles.add(angles.get(0)); // add the first one more time to the end
        for (int i=0; i<angles.size()-1; i++) {
            Sector sector = new Sector(angles.get(i), angles.get(i+1));
            if (sector.getSpan() > largestGap) largestSector = sector;
        }
        return largestSector;
    }

}