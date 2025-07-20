package io.github.jspinak.brobot.action.internal.region;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Resolves and updates search regions dynamically based on matches from other state objects.
 * This service enables cross-state search region definitions, allowing objects to define
 * their search areas based on the location of other objects, even from different states.
 */
@Component
public class DynamicRegionResolver {

    private final StateStore stateStore;

    public DynamicRegionResolver(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    /**
     * Updates search regions for a state object based on cross-state references.
     * @param targetObject The object to update
     * @param actionResult The action result containing all matches
     */
    public void updateSearchRegions(StateObject targetObject, ActionResult actionResult) {
        // Only process objects that support cross-state search regions
        if (targetObject instanceof StateImage) {
            updateStateImageSearchRegion((StateImage) targetObject, actionResult);
        } else if (targetObject instanceof StateLocation) {
            updateStateLocationSearchRegion((StateLocation) targetObject, actionResult);
        } else if (targetObject instanceof StateRegion) {
            updateStateRegionSearchRegion((StateRegion) targetObject, actionResult);
        }
    }

    private void updateStateImageSearchRegion(StateImage stateImage, ActionResult actionResult) {
        if (!shouldUpdateSearchRegion(stateImage)) return;
        
        SearchRegionOnObject config = stateImage.getSearchRegionOnObject();
        if (config == null) return;

        Optional<Region> newRegion = resolveRegionFromMatch(config, actionResult);
        newRegion.ifPresent(stateImage::setFixedSearchRegion);
    }

    private void updateStateLocationSearchRegion(StateLocation stateLocation, ActionResult actionResult) {
        if (stateLocation.getSearchRegionOnObject() == null) return;
        
        // StateLocation uses its location as the center of a search region
        SearchRegionOnObject config = stateLocation.getSearchRegionOnObject();
        Optional<Region> newRegion = resolveRegionFromMatch(config, actionResult);
        
        if (newRegion.isPresent()) {
            // Update the location based on the center of the new region
            Region region = newRegion.get();
            int centerX = region.getX() + region.getW() / 2;
            int centerY = region.getY() + region.getH() / 2;
            stateLocation.setLocation(new Location(centerX, centerY));
        }
    }

    private void updateStateRegionSearchRegion(StateRegion stateRegion, ActionResult actionResult) {
        // Similar implementation for StateRegion if it gets SearchRegionOnObject support
    }

    /**
     * Checks if the search region should be updated based on the conditions:
     * 1. The object does not have a defined search area
     * 2. The object was found (has match history)
     */
    private boolean shouldUpdateSearchRegion(StateImage stateImage) {
        return !stateImage.hasDefinedSearchRegion() && 
               stateImage.getMatchHistory().getTimesFound() > 0;
    }

    /**
     * Resolves a region from a match based on the SearchRegionOnObject configuration.
     */
    private Optional<Region> resolveRegionFromMatch(SearchRegionOnObject config, ActionResult actionResult) {
        // Find the source match
        Optional<Match> sourceMatch = findMatchForConfig(config, actionResult);
        if (sourceMatch.isEmpty()) return Optional.empty();

        Match match = sourceMatch.get();
        Region baseRegion = match.getRegion();

        // Apply adjustments
        SearchRegionOnObject.AdjustOptions adjustments = config.getAdjustments();
        if (adjustments == null) adjustments = new SearchRegionOnObject.AdjustOptions();

        int x = baseRegion.getX() + adjustments.getXAdjust();
        int y = baseRegion.getY() + adjustments.getYAdjust();
        int w = baseRegion.getW() + adjustments.getWAdjust();
        int h = baseRegion.getH() + adjustments.getHAdjust();

        // Apply absolute dimensions if specified
        SearchRegionOnObject.AbsoluteDimensions absoluteDims = config.getAbsoluteDimensions();
        if (absoluteDims != null) {
            if (absoluteDims.hasWidth()) w = absoluteDims.getWidth();
            if (absoluteDims.hasHeight()) h = absoluteDims.getHeight();
        }

        return Optional.of(new Region(x, y, w, h));
    }

    /**
     * Finds a match that corresponds to the SearchRegionOnObject configuration.
     */
    private Optional<Match> findMatchForConfig(SearchRegionOnObject config, ActionResult actionResult) {
        String targetStateName = config.getTargetStateName();
        String targetObjectName = config.getTargetObjectName();
        StateObject.Type targetType = config.getTargetType();

        // First, try to find in the current matches
        List<Match> matchList = actionResult.getMatchList();
        for (Match match : matchList) {
            if (matchesConfig(match, targetStateName, targetObjectName, targetType)) {
                return Optional.of(match);
            }
        }

        // If not found in current matches, try to get from state store
        Optional<State> targetStateOpt = stateStore.getState(targetStateName);
        if (targetStateOpt.isEmpty()) return Optional.empty();
        
        State targetState = targetStateOpt.get();

        // Find the target object in the state
        switch (targetType) {
            case IMAGE:
                return targetState.getStateImages().stream()
                    .filter(img -> img.getName().equals(targetObjectName))
                    .findFirst()
                    .flatMap(img -> {
                        List<ActionRecord> snapshots = img.getMatchHistory().getSnapshots();
                        if (snapshots.isEmpty()) return Optional.empty();
                        ActionRecord lastSnapshot = snapshots.get(snapshots.size() - 1);
                        List<Match> matches = lastSnapshot.getMatchList();
                        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
                    });
            case REGION:
                return targetState.getStateRegions().stream()
                    .filter(reg -> reg.getName().equals(targetObjectName))
                    .findFirst()
                    .map(reg -> new Match.Builder()
                        .setRegion(reg.getSearchRegion())
                        .setStateObjectData(reg)
                        .build());
            case LOCATION:
                return targetState.getStateLocations().stream()
                    .filter(loc -> loc.getName().equals(targetObjectName))
                    .findFirst()
                    .filter(loc -> loc.getLocation() != null)
                    .map(loc -> new Match.Builder()
                        .setRegion(loc.getLocation().getCalculatedX(), 
                                  loc.getLocation().getCalculatedY(), 1, 1)
                        .setStateObjectData(loc)
                        .build());
            default:
                return Optional.empty();
        }
    }

    /**
     * Checks if a match corresponds to the specified configuration.
     */
    private boolean matchesConfig(Match match, String stateName, String objectName, StateObject.Type type) {
        return match.getStateObjectData() != null &&
               stateName.equals(match.getStateObjectData().getOwnerStateName()) &&
               objectName.equals(match.getStateObjectData().getStateObjectName());
    }

    /**
     * Batch update search regions for multiple state objects.
     */
    public void updateSearchRegionsForObjects(List<StateObject> objects, ActionResult actionResult) {
        objects.forEach(obj -> updateSearchRegions(obj, actionResult));
    }
}