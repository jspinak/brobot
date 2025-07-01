package io.github.jspinak.brobot.model.element;

import lombok.Getter;
import java.util.Objects;

/**
 * Represents a directed movement from a start location to an end location.
 * <p>
 * This class is essential for representing actions that have a clear direction and
 * path, such as a mouse drag or a screen swipe. Unlike a Region, which only defines
 * a static area, a Movement encapsulates the dynamic concept of a transition
 * between two points.
 * <p>
 * It is an immutable object, ensuring that the definition of a movement cannot be
 * changed after it is created.
 *
 * @see Location
 * @see io.github.jspinak.brobot.action.composite.drag.Drag
 */
@Getter
public final class Movement {

    private final Location startLocation;
    private final Location endLocation;

    /**
     * Constructs a new Movement instance.
     *
     * @param startLocation The non-null starting point of the movement.
     * @param endLocation   The non-null ending point of the movement.
     */
    public Movement(Location startLocation, Location endLocation) {
        Objects.requireNonNull(startLocation, "Start location cannot be null");
        Objects.requireNonNull(endLocation, "End location cannot be null");
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    /**
     * Calculates the horizontal displacement of the movement.
     *
     * @return The change in the x-coordinate (end - start).
     */
    public int getDeltaX() {
        return endLocation.getCalculatedX() - startLocation.getCalculatedX();
    }

    /**
     * Calculates the vertical displacement of the movement.
     *
     * @return The change in the y-coordinate (end - start).
     */
    public int getDeltaY() {
        return endLocation.getCalculatedY() - startLocation.getCalculatedY();
    }

    @Override
    public String toString() {
        return String.format("Movement[from=%s, to=%s]", startLocation, endLocation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movement movement = (Movement) o;
        return startLocation.equals(movement.startLocation) && endLocation.equals(movement.endLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startLocation, endLocation);
    }
}
