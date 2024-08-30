package io.github.jspinak.brobot.dsl;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;

@Component
public class ActionEncoder {

    public String encodeOptions(ActionOptions options) {
        if (options == null) {
            throw new DSLException(DSLException.ErrorCode.MISSING_REQUIRED_FIELD, "ActionOptions cannot be null");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("OPTIONS:\n");
        // Implement the logic to encode each option
        // For example:
        sb.append("action: ").append(options.getAction()).append("\n");
        sb.append("clickUntil: ").append(options.getClickUntil()).append("\n");
        // ... encode other options ...
        return sb.toString();
    }

    public String encodeObjectCollection(ObjectCollection objCollection) {
        StringBuilder sb = new StringBuilder();

        if (!objCollection.getStateLocations().isEmpty()) {
            sb.append("LOCATIONS:");
            for (StateLocation location : objCollection.getStateLocations()) {
                sb.append(encodeStateLocation(location)).append(",");
            }
            sb.setLength(sb.length() - 1); // Remove last comma
            sb.append(";");
        }

        if (!objCollection.getStateImages().isEmpty()) {
            sb.append("IMAGES:");
            for (StateImage image : objCollection.getStateImages()) {
                sb.append(encodeStateImage(image)).append(",");
            }
            sb.setLength(sb.length() - 1); // Remove last comma
            sb.append(";");
        }

        if (!objCollection.getStateRegions().isEmpty()) {
            sb.append("REGIONS:");
            for (StateRegion region : objCollection.getStateRegions()) {
                sb.append(encodeStateRegion(region)).append(",");
            }
            sb.setLength(sb.length() - 1); // Remove last comma
            sb.append(";");
        }

        if (!objCollection.getStateStrings().isEmpty()) {
            sb.append("STRINGS:");
            for (StateString str : objCollection.getStateStrings()) {
                sb.append(encodeStateString(str)).append(",");
            }
            sb.setLength(sb.length() - 1); // Remove last comma
            sb.append(";");
        }

        if (!objCollection.getMatches().isEmpty()) {
            sb.append("MATCHES:");
            for (Matches match : objCollection.getMatches()) {
                sb.append(encodeMatches(match)).append(",");
            }
            sb.setLength(sb.length() - 1); // Remove last comma
            sb.append(";");
        }

        if (!objCollection.getScenes().isEmpty()) {
            sb.append("SCENES:");
            for (Scene scene : objCollection.getScenes()) {
                sb.append(encodeScene(scene)).append(",");
            }
            sb.setLength(sb.length() - 1); // Remove last comma
            sb.append(";");
        }

        return sb.toString();
    }

    public String encodeStateLocation(StateLocation stateLocation) {
        Location location = stateLocation.getLocation();
        return String.format("L(%d,%d)", location.getX(), location.getY());
    }

    public String encodeStateImage(StateImage image) {
        return String.format("I(%s)", image.getName());
    }

    public String encodeStateRegion(StateRegion stateRegion) {
        Region region = stateRegion.getSearchRegion();
        return String.format("R(%d,%d,%d,%d)", region.getX(), region.getY(), region.getW(), region.getH());
    }

    public String encodeStateString(StateString str) {
        return String.format("S(%s)", str.getString());
    }

    public String encodeMatches(Matches matches) {
        // This might need a more complex implementation depending on what information you want to encode
        return String.format("M(%d)", matches.size());
    }

    public String encodeScene(Scene scene) {
        // This might need a more complex implementation depending on what information you want to encode
        return String.format("SC(%s)", scene.getId());
    }
}
