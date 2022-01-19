package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.database.primitives.location.Position;

import java.util.HashMap;
import java.util.Map;

/**
 * Each region has 4 borders that can be defined: top, bottom, left, right.
 * Anchor points can either define 1 or 2 borders, depending on their Positions. Middle
 * positions define only 1 border and corner positions define 2 borders.
 * This class keeps track of which borders have been defined.
 */
public class DefinedBorders {

    private Map<Position.Name, Boolean> defined = new HashMap<>();
    {
        defined.put(Position.Name.MIDDLELEFT, false);
        defined.put(Position.Name.BOTTOMMIDDLE, false);
        defined.put(Position.Name.TOPMIDDLE, false);
        defined.put(Position.Name.MIDDLERIGHT, false);
    }

    public void setAsDefined(Position.Name border) {
        if (border == Position.Name.TOPLEFT) {
            defined.put(Position.Name.MIDDLELEFT, true);
            defined.put(Position.Name.TOPMIDDLE, true);
        }
        else if (border == Position.Name.TOPRIGHT) {
            defined.put(Position.Name.MIDDLERIGHT, true);
            defined.put(Position.Name.TOPMIDDLE, true);
        }
        else if (border == Position.Name.BOTTOMLEFT) {
            defined.put(Position.Name.MIDDLELEFT, true);
            defined.put(Position.Name.BOTTOMMIDDLE, true);
        }
        else if (border == Position.Name.BOTTOMRIGHT) {
            defined.put(Position.Name.MIDDLERIGHT, true);
            defined.put(Position.Name.BOTTOMMIDDLE, true);
        }
        else defined.put(border, true);
    }

    public boolean isDefined(Position.Name border) {
        return defined.get(border);
    }

    public boolean allBordersDefined() {
        for (Boolean defined : defined.values()) {
            if (!defined) return false;
        }
        return true;
    }
}
