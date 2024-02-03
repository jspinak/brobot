package io.github.jspinak.brobot.analysis;

import io.github.jspinak.brobot.datatypes.primitives.angles.Sector;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Distance {

    private double dist(double a, double b) {
        return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    private List<Integer> getDistPair(Location loc1, Location loc2) {
        List<Integer> distPair = new ArrayList<>();
        distPair.add(loc2.getX() - loc1.getX());
        // the y difference is the opposite on the computer screen as in a cartesian plane
        distPair.add(loc1.getY() - loc2.getY());
        //System.out.print("|cartesian dist x.y "+distPair.get(0)+"."+distPair.get(1)+" ");
        return distPair;
    }

    private double getDist(List<Integer> distPair) {
        return dist(distPair.get(0), distPair.get(1));
    }

    private double getDegree(List<Integer> distPair) {
        return Math.toDegrees(Math.atan2((double)distPair.get(1), (double)distPair.get(0)));
    }

    public double getDistance(Location loc1, Location loc2) { return euclidean(loc1, loc2); }

    public double getDistance(Match match1, Match match2) {
        return Math.sqrt(Math.pow(match1.getRegion().sikuli().getCenter().getX() - match2.getRegion().sikuli().getCenter().getX(), 2) +
                Math.pow(match1.getRegion().sikuli().getCenter().getY() - match2.getRegion().sikuli().getCenter().getY(), 2));
    }

    public double euclidean(Location loc1, Location loc2) {
        return getDist(getDistPair(loc1, loc2));
    }

    public double euclidean(Location loc, Match match) {
        return getDist(getDistPair(loc, new Location(match)));
    }

    public double getAngle(Location loc1, Location loc2) {
        return getDegree(getDistPair(loc1, loc2));
    }

    public double getAngle(Location loc, Match match) {
        return getDegree(getDistPair(loc, new Location(match)));
    }

    public double getAngleFrom00(Location loc) {
        return getAngle(new Location(0,0), loc);
    }
    /**
     * Calculates the difference between the angles of 2 vectors.
     * The return value is the concave angle between the 2 vectors (i.e. 30 instead of 330).
     * Vector 1: start to loc1
     * Vector 2: start to loc2
     *
     * @param center the start point for both vectors
     * @param loc1 the end point for vector1
     * @param loc2 the end point for vector2
     * @return the angle to go from vector1 to vector2
     */
    public double getDegreesBetween(Location center, Location loc1, Location loc2) {
        double angle1 = getAngle(center, loc1);
        double angle2 = getAngle(center, loc2);
        return getDegreesBetween(angle1, angle2);
    }

    public double getDegreesBetween(Match center, Match match1, Match match2) {
        return getDegreesBetween(new Location(center), new Location(match1), new Location(match2));
    }

    public double getDegreesBetween(double angle1, double angle2) {
        List<Double> adjustedAngles = convertAnglesToSameScale(angle1, angle2);
        return adjustedAngles.get(1) - adjustedAngles.get(0);
    }

    /**
     * On a circular scale there must be a jump from one value to another.
     * The normal scale has this break at 180/-179 degrees.
     * This function assumes that the parameter values are clustered in one region, and makes sure that
     * these values do not include a break.
     *
     * @param angles the values to convert
     * @return a list of values without a break
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

    public double getOppositeAngle(double angle) {
        return angle > 0 ? angle - 180 : angle + 180;
    }

    public List<Double> convertAnglesToSameScale(Double... angles) {
        return convertAnglesToSameScale(Arrays.asList(angles));
    }

    public double getMedianAngle(List<Double> angles) {
        List<Location> standardLocations = new ArrayList<>();
        angles.forEach(angle -> standardLocations.add(getStandardLocation(angle)));
        return getMedianAngle(new Location(0,0), standardLocations.toArray(new Location[0]));
    }

    public double getMedianAngle(Double... angles) {
        return getMedianAngle(Arrays.asList(angles));
    }

    /**
     * Sums the x and y points and calculates the angle to this new summed Location
     * @param startLocation the start point
     * @param locations the locations
     * @return the median angle to all locations from the start point
     */
    public double getMedianAngle(Location startLocation, List<Location> locations) {
        int x = locations.stream().mapToInt(Location::getX).sum();
        int y = locations.stream().mapToInt(Location::getY).sum();
        return getAngle(startLocation, new Location(x,y));
    }

    public double getMedianAngle(Location startLocation, Location... locations) {
        return getMedianAngle(startLocation, Arrays.asList(locations));
    }

    /**
     * Returns a Location based on the starting point of (0,0) and a distance of 1000.
     * @param angle The angle of the desired Location.
     * @return a standard Location.
     */
    public Location getStandardLocation(double angle) {
        return getLocation(angle, 1000, new Location(0,0));
    }

    /**
     * Returns a Location given a starting point, an angle, and a distance.
     * The returned Location is not the exact location since it must be
     * converted to (int,int) for pixel space.
     * @param angle angle of the desired Location
     * @param distance distance from the starting Location to the desired Location
     * @param startingLocation the starting point
     * @return a new Location that approximates the desired location in pixel space.
     */
    public Location getLocation(double angle, double distance, Location startingLocation) {
        double addX = distance * Math.cos(Math.toRadians(angle));
        double addY = - distance * Math.sin(Math.toRadians(angle));
        return new Location((int)Math.round(startingLocation.getX() + addX),
                (int)Math.round(startingLocation.getY() + addY));
    }

    /**
     * Returns the distance from location1 to location2 as a Location object.
     * @param location1 start location
     * @param location2 end location
     * @return a Location representing the vector from location1 to location2
     */
    public Location getLocation(Location location1, Location location2) {
        return new Location(location2.getX() - location1.getX(), location2.getY() - location1.getY());
    }

    public List<Double> undueConversion(List<Double> angles) {
        List<Double> originalList = new ArrayList<>();
        angles.forEach(a -> originalList.add(undueConversion(a)));
        return originalList;
    }

    public double undueConversion(double a) {
        if (a < -179) return a + 360;
        else if (a > 180) return a - 360;
        return a;
    }

    /**
     * Returns the leftmost angle from a list of angles. The leftmost angle is defined as the
     * angle bordering on the largest gap in the angle list when moving counterclockwise.
     * @param angles a list of angles
     * @return the leftmost angle
     */
    public double getLeftmostAngle(List<Double> angles) {
        Sector largestSector = getLargestSector(angles);
        return largestSector.getRightAngle();
    }

    public double getRightmostAngle(List<Double> angles) {
        Sector largestSector = getLargestSector(angles);
        return largestSector.getLeftAngle();
    }

    public double getSpan(List<Double> angles) {
        if (angles.isEmpty()) return 0.0;
        return 360 - getLargestSector(angles).getSpan();
    }

    public double getMiddleAngle(List<Double> angles) {
        Sector largestSector = getLargestSector(angles);
        double angleBetween = getMedianAngle(largestSector.getLeftAngle(), largestSector.getRightAngle());
        if (largestSector.getSpan() < 180) angleBetween *= -1;
        return angleBetween;
    }

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
