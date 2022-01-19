package actions.methods.basicactions.define;

import com.brobot.multimodule.database.primitives.location.Position;

import java.util.HashMap;
import java.util.Map;

import static com.brobot.multimodule.database.primitives.location.Position.Name.*;

/**
 * Each region has 4 borders that can be defined: top, bottom, left, right.
 * Anchor points can either define 1 or 2 borders, depending on their Positions. Middle
 * positions define only 1 border and corner positions define 2 borders.
 * This class keeps track of which borders have been defined.
 */
public class DefinedBorders {

    private Map<Position.Name, Boolean> defined = new HashMap<>();
    {
        defined.put(MIDDLELEFT, false);
        defined.put(BOTTOMMIDDLE, false);
        defined.put(TOPMIDDLE, false);
        defined.put(MIDDLERIGHT, false);
    }

    public void setAsDefined(Position.Name border) {
        if (border == TOPLEFT) {
            defined.put(MIDDLELEFT, true);
            defined.put(TOPMIDDLE, true);
        }
        else if (border == Position.Name.TOPRIGHT) {
            defined.put(MIDDLERIGHT, true);
            defined.put(TOPMIDDLE, true);
        }
        else if (border == BOTTOMLEFT) {
            defined.put(MIDDLELEFT, true);
            defined.put(BOTTOMMIDDLE, true);
        }
        else if (border == BOTTOMRIGHT) {
            defined.put(MIDDLERIGHT, true);
            defined.put(BOTTOMMIDDLE, true);
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
