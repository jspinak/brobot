package io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.attributes;

import java.util.HashMap;
import java.util.Map;

import static io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.attributes.AttributeTypes.Attribute.*;

/**
 * MULTIPLE_MATCHES allows for multiple matches, but doesn't require it. Otherwise, the image is given
 *   the SINGLE_MATCH Attribute. SINGLE_MATCH is much more common; for this reason only
 *   MULTIPLE_MATCHES is specified in the filename.
 * VARIABLE_LOCATION allows for variable locations; otherwise, FIXED_LOCATION is used. Only
 *   VARIABLE_LOCATION can be specified in the filename.
 * APPEARS is specified by a '_' without a following character: i.e. _12 or _1,4,6
 */

public class AttributeTypes {

    public enum Attribute {
        MULTIPLE_MATCHES, SINGLE_MATCH, VARIABLE_LOCATION, FIXED_LOCATION,
        DEFINE, GROUP_DEFINE, APPEARS_EXCLUSIVELY,
        DOESNT_APPEAR, APPEARS, REGION, TRANSFER, LOCATION
    }

    public enum Tag {
        STATE_NAME, IMAGE_NAME, ATTRIBUTE, TRANSITION
    }

    public static Map<Character, Tag> tags = new HashMap<>();
    static {
        tags.put('-', Tag.IMAGE_NAME);
        tags.put('_', Tag.ATTRIBUTE);
        tags.put('~', Tag.TRANSITION);
    }

    public static Map<Character, Attribute> attributes = new HashMap<>();
    static {
        attributes.put('e', APPEARS_EXCLUSIVELY);
        attributes.put('x', DOESNT_APPEAR);
        attributes.put('m', MULTIPLE_MATCHES);
        attributes.put('v', VARIABLE_LOCATION);
        attributes.put('d', DEFINE);
        attributes.put('g', GROUP_DEFINE);
        attributes.put('r', REGION);
        attributes.put('t', TRANSFER);
        attributes.put('l', LOCATION);
    }
}
