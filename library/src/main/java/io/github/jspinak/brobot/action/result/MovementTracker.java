package io.github.jspinak.brobot.action.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Movement;

import lombok.Data;

/**
 * Tracks movement operations performed during action execution. Manages drag operations and other
 * movement-based actions.
 *
 * <p>This class encapsulates movement tracking functionality that was previously embedded in
 * ActionResult.
 *
 * @since 2.0
 */
@Data
public class MovementTracker {
    private List<Movement> movements = new ArrayList<>();

    /** Creates an empty MovementTracker. */
    public MovementTracker() {}

    /**
     * Records a movement operation.
     *
     * @param movement The movement to record
     */
    public void recordMovement(Movement movement) {
        if (movement != null) {
            movements.add(movement);
        }
    }

    /**
     * Records a movement from start to end location.
     *
     * @param start The starting location
     * @param end The ending location
     */
    public void recordMovement(Location start, Location end) {
        if (start != null && end != null) {
            movements.add(new Movement(start, end));
        }
    }

    /**
     * Gets the first movement if one exists. Convenience method for simple, single-segment actions.
     *
     * @return Optional containing the first movement
     */
    public Optional<Movement> getFirstMovement() {
        if (movements.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(movements.get(0));
    }

    /**
     * Gets the last movement if one exists.
     *
     * @return Optional containing the last movement
     */
    public Optional<Movement> getLastMovement() {
        if (movements.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(movements.get(movements.size() - 1));
    }

    /**
     * Gets all movements in sequence.
     *
     * @return List of all movements
     */
    public List<Movement> getMovementSequence() {
        return new ArrayList<>(movements);
    }

    /**
     * Gets the total distance of all movements.
     *
     * @return Total distance in pixels
     */
    public double getTotalDistance() {
        return movements.stream().mapToDouble(this::calculateDistance).sum();
    }

    /**
     * Gets the average movement distance.
     *
     * @return Average distance per movement
     */
    public double getAverageDistance() {
        if (movements.isEmpty()) {
            return 0.0;
        }
        return getTotalDistance() / movements.size();
    }

    /**
     * Gets the number of movements.
     *
     * @return Count of movements
     */
    public int size() {
        return movements.size();
    }

    /**
     * Checks if any movements exist.
     *
     * @return true if no movements recorded
     */
    public boolean isEmpty() {
        return movements.isEmpty();
    }

    /**
     * Gets the starting location of the movement sequence.
     *
     * @return Optional containing the initial location
     */
    public Optional<Location> getStartLocation() {
        return getFirstMovement().map(Movement::getStartLocation);
    }

    /**
     * Gets the ending location of the movement sequence.
     *
     * @return Optional containing the final location
     */
    public Optional<Location> getEndLocation() {
        return getLastMovement().map(Movement::getEndLocation);
    }

    /**
     * Checks if the movements form a closed path. A path is closed if start and end locations are
     * the same.
     *
     * @param tolerance Distance tolerance for considering points equal
     * @return true if the path is closed
     */
    public boolean isClosedPath(double tolerance) {
        if (movements.isEmpty()) {
            return false;
        }

        Optional<Location> start = getStartLocation();
        Optional<Location> end = getEndLocation();

        if (!start.isPresent() || !end.isPresent()) {
            return false;
        }

        Location startLoc = start.get();
        Location endLoc = end.get();

        double distance =
                Math.sqrt(
                        Math.pow(startLoc.getCalculatedX() - endLoc.getCalculatedX(), 2)
                                + Math.pow(startLoc.getCalculatedY() - endLoc.getCalculatedY(), 2));

        return distance <= tolerance;
    }

    /**
     * Gets the bounding box of all movement points.
     *
     * @return Optional containing the bounding region
     */
    public Optional<io.github.jspinak.brobot.model.element.Region> getBoundingBox() {
        if (movements.isEmpty()) {
            return Optional.empty();
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Movement m : movements) {
            Location start = m.getStartLocation();
            Location end = m.getEndLocation();

            minX = Math.min(minX, Math.min(start.getCalculatedX(), end.getCalculatedX()));
            minY = Math.min(minY, Math.min(start.getCalculatedY(), end.getCalculatedY()));
            maxX = Math.max(maxX, Math.max(start.getCalculatedX(), end.getCalculatedX()));
            maxY = Math.max(maxY, Math.max(start.getCalculatedY(), end.getCalculatedY()));
        }

        return Optional.of(
                new io.github.jspinak.brobot.model.element.Region(
                        minX, minY, maxX - minX, maxY - minY));
    }

    /**
     * Merges movement data from another instance.
     *
     * @param other The MovementTracker to merge
     */
    public void merge(MovementTracker other) {
        if (other != null) {
            movements.addAll(other.movements);
        }
    }

    /** Clears all movement data. */
    public void clear() {
        movements.clear();
    }

    /**
     * Formats the movement data as a string summary.
     *
     * @return Formatted movement summary
     */
    public String format() {
        if (movements.isEmpty()) {
            return "No movements";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Movements: ").append(movements.size());
        sb.append(String.format(", Total distance: %.1fpx", getTotalDistance()));

        if (movements.size() == 1) {
            Movement m = movements.get(0);
            sb.append(
                    String.format(
                            " [(%d,%d)→(%d,%d)]",
                            m.getStartLocation().getCalculatedX(),
                            m.getStartLocation().getCalculatedY(),
                            m.getEndLocation().getCalculatedX(),
                            m.getEndLocation().getCalculatedY()));
        } else if (movements.size() > 1) {
            sb.append(String.format(", Avg: %.1fpx", getAverageDistance()));
            if (isClosedPath(5.0)) {
                sb.append(" (closed path)");
            }
        }

        return sb.toString();
    }

    private double calculateDistance(Movement movement) {
        Location start = movement.getStartLocation();
        Location end = movement.getEndLocation();

        double dx = end.getCalculatedX() - start.getCalculatedX();
        double dy = end.getCalculatedY() - start.getCalculatedY();

        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return format();
    }
}
