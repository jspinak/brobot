package io.github.jspinak.brobot.action.internal.capture;

import io.github.jspinak.brobot.action.basic.region.DefineRegion;
import io.github.jspinak.brobot.model.element.Positions;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks which borders of a region have been defined during the region definition process.
 * 
 * <p>In the Brobot framework's Action Model (a), this class supports the dynamic definition
 * of regions by maintaining the state of which boundaries have been established. Each region
 * has four borders that can be defined: top, bottom, left, and right.</p>
 * 
 * <p>This class is crucial for the model-based automation approach as it enables:
 * <ul>
 *   <li>Incremental region definition using multiple anchor points</li>
 *   <li>Validation that all necessary borders have been defined</li>
 *   <li>Flexibility in defining regions from various anchor positions</li>
 * </ul>
 * </p>
 * 
 * <p>Anchor points can define either one or two borders depending on their position:
 * <ul>
 *   <li>Corner positions (TOPLEFT, TOPRIGHT, BOTTOMLEFT, BOTTOMRIGHT) define two borders</li>
 *   <li>Middle positions (TOPMIDDLE, BOTTOMMIDDLE, MIDDLELEFT, MIDDLERIGHT) define one border</li>
 * </ul>
 * </p>
 * 
 * <p>This tracking mechanism ensures that regions are properly bounded before being used
 * in automation tasks, preventing undefined behavior from incomplete region specifications.</p>
 * 
 * @see AnchorRegion
 * @see DefineRegion
 * @see Positions
 */
public class DefinedBorders {

    private Map<Positions.Name, Boolean> defined = new HashMap<>();
    {
        defined.put(Positions.Name.MIDDLELEFT, false);
        defined.put(Positions.Name.BOTTOMMIDDLE, false);
        defined.put(Positions.Name.TOPMIDDLE, false);
        defined.put(Positions.Name.MIDDLERIGHT, false);
    }

    /**
     * Marks the specified border(s) as defined based on the anchor position.
     * 
     * <p>For corner positions, this method marks both adjacent borders as defined.
     * For middle positions, only the corresponding single border is marked.</p>
     * 
     * @param border the anchor position that defines one or more borders
     */
    public void setAsDefined(Positions.Name border) {
        if (border == Positions.Name.TOPLEFT) {
            defined.put(Positions.Name.MIDDLELEFT, true);
            defined.put(Positions.Name.TOPMIDDLE, true);
        }
        else if (border == Positions.Name.TOPRIGHT) {
            defined.put(Positions.Name.MIDDLERIGHT, true);
            defined.put(Positions.Name.TOPMIDDLE, true);
        }
        else if (border == Positions.Name.BOTTOMLEFT) {
            defined.put(Positions.Name.MIDDLELEFT, true);
            defined.put(Positions.Name.BOTTOMMIDDLE, true);
        }
        else if (border == Positions.Name.BOTTOMRIGHT) {
            defined.put(Positions.Name.MIDDLERIGHT, true);
            defined.put(Positions.Name.BOTTOMMIDDLE, true);
        }
        else defined.put(border, true);
    }

    /**
     * Checks whether a specific border has been defined.
     * 
     * @param border the border position to check
     * @return true if the border has been defined, false otherwise
     */
    public boolean isDefined(Positions.Name border) {
        return defined.get(border);
    }

    /**
     * Checks whether all four borders of the region have been defined.
     * 
     * <p>This method is typically used to validate that a region is fully specified
     * before using it in automation tasks.</p>
     * 
     * @return true if all borders (top, bottom, left, right) have been defined, false otherwise
     */
    public boolean allBordersDefined() {
        for (Boolean defined : defined.values()) {
            if (!defined) return false;
        }
        return true;
    }
}
