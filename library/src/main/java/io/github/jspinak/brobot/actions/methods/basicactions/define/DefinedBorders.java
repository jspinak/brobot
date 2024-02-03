package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;

import java.util.HashMap;
import java.util.Map;

/**
 * Each region has 4 borders that can be defined: top, bottom, left, right.
 * Anchor points can either define 1 or 2 borders, depending on their Positions. Middle
 * positions define only 1 border and corner positions define 2 borders.
 * This class keeps track of which borders have been defined.
 */
public class DefinedBorders {

    private Map<Positions.Name, Boolean> defined = new HashMap<>();
    {
        defined.put(Positions.Name.MIDDLELEFT, false);
        defined.put(Positions.Name.BOTTOMMIDDLE, false);
        defined.put(Positions.Name.TOPMIDDLE, false);
        defined.put(Positions.Name.MIDDLERIGHT, false);
    }

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

    public boolean isDefined(Positions.Name border) {
        return defined.get(border);
    }

    public boolean allBordersDefined() {
        for (Boolean defined : defined.values()) {
            if (!defined) return false;
        }
        return true;
    }
}
