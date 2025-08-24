package io.github.jspinak.brobot.action.internal.region;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions.DefineAs;
import io.github.jspinak.brobot.action.basic.region.DefineInsideAnchors;
import io.github.jspinak.brobot.action.basic.region.DefineOutsideAnchors;
import io.github.jspinak.brobot.action.internal.capture.AnchorRegion;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.CrossStateAnchor;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Processes cross-state anchors to define regions dynamically based on matches from other states.
 * This enables flexible region definitions that adapt to GUI changes by using reference
 * points from any state object in the model.
 */
@Component
public class CrossStateAnchorProcessor {

    private final StateStore stateStore;
    private final AnchorRegion anchorRegion;
    private final DefineInsideAnchors defineInsideAnchors;
    private final DefineOutsideAnchors defineOutsideAnchors;

    public CrossStateAnchorProcessor(StateStore stateStore, AnchorRegion anchorRegion,
                                    DefineInsideAnchors defineInsideAnchors,
                                    DefineOutsideAnchors defineOutsideAnchors) {
        this.stateStore = stateStore;
        this.anchorRegion = anchorRegion;
        this.defineInsideAnchors = defineInsideAnchors;
        this.defineOutsideAnchors = defineOutsideAnchors;
    }

    /**
     * Processes cross-state anchors for a StateRegion, updating its search region based on matches.
     * @param stateRegion The region to update
     * @param crossStateAnchors The anchors that reference other state objects
     * @param actionResult The current action result containing matches
     */
    public void processAnchors(StateRegion stateRegion, List<CrossStateAnchor> crossStateAnchors, 
                              ActionResult actionResult) {
        if (crossStateAnchors == null || crossStateAnchors.isEmpty()) return;
        
        // Collect anchor locations from cross-state matches
        List<LocationWithAnchor> anchorPoints = collectAnchorPoints(crossStateAnchors, actionResult);
        
        if (anchorPoints.isEmpty()) return;
        
        // Apply the definition strategy
        DefineAs strategy = stateRegion.getDefineStrategy();
        if (strategy == null) strategy = DefineAs.OUTSIDE_ANCHORS; // Default strategy
        
        Optional<Region> definedRegion = applyStrategy(strategy, anchorPoints, stateRegion.getSearchRegion());
        
        definedRegion.ifPresent(stateRegion::setSearchRegion);
    }

    /**
     * Collects anchor points from cross-state matches.
     */
    private List<LocationWithAnchor> collectAnchorPoints(List<CrossStateAnchor> anchors, 
                                                         ActionResult actionResult) {
        List<LocationWithAnchor> anchorPoints = new ArrayList<>();
        
        for (CrossStateAnchor anchor : anchors) {
            Optional<Match> match = findMatchForAnchor(anchor, actionResult);
            match.ifPresent(m -> {
                Location loc = anchor.getAdjustedLocation(m.getTarget(), m.getRegion());
                anchorPoints.add(new LocationWithAnchor(loc, anchor));
            });
        }
        
        return anchorPoints;
    }

    /**
     * Finds a match that corresponds to the cross-state anchor.
     */
    private Optional<Match> findMatchForAnchor(CrossStateAnchor anchor, ActionResult actionResult) {
        String sourceStateName = anchor.getSourceStateName();
        String sourceObjectName = anchor.getSourceObjectName();
        StateObject.Type sourceType = anchor.getSourceType();
        
        // First check in current action results
        for (Match match : actionResult.getMatchList()) {
            if (matchesAnchor(match, sourceStateName, sourceObjectName)) {
                return Optional.of(match);
            }
        }
        
        // Fall back to state store for historical matches
        Optional<State> stateOpt = stateStore.getState(sourceStateName);
        if (stateOpt.isEmpty()) return Optional.empty();
        
        State state = stateOpt.get();
        return findHistoricalMatch(state, sourceObjectName, sourceType);
    }

    /**
     * Checks if a match corresponds to the anchor specification.
     */
    private boolean matchesAnchor(Match match, String stateName, String objectName) {
        return match.getStateObjectData() != null &&
               stateName.equals(match.getStateObjectData().getOwnerStateName()) &&
               objectName.equals(match.getStateObjectData().getStateObjectName());
    }

    /**
     * Finds historical matches from state objects.
     */
    private Optional<Match> findHistoricalMatch(State state, String objectName, StateObject.Type type) {
        switch (type) {
            case IMAGE:
                return state.getStateImages().stream()
                    .filter(img -> img.getName().equals(objectName))
                    .findFirst()
                    .flatMap(this::getLastMatchFromStateImage);
            case REGION:
                return state.getStateRegions().stream()
                    .filter(reg -> reg.getName().equals(objectName))
                    .findFirst()
                    .map(this::createMatchFromRegion);
            case LOCATION:
                return state.getStateLocations().stream()
                    .filter(loc -> loc.getName().equals(objectName))
                    .findFirst()
                    .map(this::createMatchFromLocation);
            default:
                return Optional.empty();
        }
    }

    /**
     * Gets the last match from a StateImage's history.
     */
    private Optional<Match> getLastMatchFromStateImage(StateImage stateImage) {
        List<ActionRecord> snapshots = stateImage.getMatchHistory().getSnapshots();
        if (snapshots.isEmpty()) return Optional.empty();
        
        ActionRecord lastSnapshot = snapshots.get(snapshots.size() - 1);
        List<Match> matches = lastSnapshot.getMatchList();
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
    }

    /**
     * Creates a match from a StateRegion.
     */
    private Match createMatchFromRegion(StateRegion stateRegion) {
        return new Match.Builder()
            .setRegion(stateRegion.getSearchRegion())
            .setStateObjectData(stateRegion)
            .build();
    }

    /**
     * Creates a match from a StateLocation.
     */
    private Match createMatchFromLocation(StateLocation stateLocation) {
        if (stateLocation.getLocation() == null) return null;
        return new Match.Builder()
            .setRegion(stateLocation.getLocation().getCalculatedX(),
                      stateLocation.getLocation().getCalculatedY(), 1, 1)
            .setStateObjectData(stateLocation)
            .build();
    }

    /**
     * Applies the definition strategy to the collected anchor points.
     */
    private Optional<Region> applyStrategy(DefineAs strategy, List<LocationWithAnchor> anchorPoints,
                                         Region currentRegion) {
        switch (strategy) {
            case INSIDE_ANCHORS:
                return applyInsideAnchorsStrategy(anchorPoints);
            case OUTSIDE_ANCHORS:
                return applyOutsideAnchorsStrategy(anchorPoints);
            default:
                // For other strategies, default to outside anchors
                return applyOutsideAnchorsStrategy(anchorPoints);
        }
    }

    /**
     * Applies the INSIDE_ANCHORS strategy - creates the smallest rectangle containing all points.
     */
    private Optional<Region> applyInsideAnchorsStrategy(List<LocationWithAnchor> anchorPoints) {
        // Create an ActionResult with proper configuration
        ActionResult actionResult = new ActionResult();
        
        // Add matches from anchor points to the result
        for (LocationWithAnchor point : anchorPoints) {
            Match match = createMatchAtLocation(point.location);
            actionResult.add(match);
        }
        
        // Use the injected DefineInsideAnchors component
        defineInsideAnchors.perform(actionResult);
        
        // Return the first defined region if any
        if (!actionResult.getDefinedRegions().isEmpty()) {
            return Optional.of(actionResult.getDefinedRegions().get(0));
        }
        
        return Optional.empty();
    }

    /**
     * Applies the OUTSIDE_ANCHORS strategy - creates the largest rectangle encompassing all points.
     */
    private Optional<Region> applyOutsideAnchorsStrategy(List<LocationWithAnchor> anchorPoints) {
        // Create an ActionResult with proper configuration
        ActionResult actionResult = new ActionResult();
        
        // Add matches from anchor points to the result
        for (LocationWithAnchor point : anchorPoints) {
            Match match = createMatchAtLocation(point.location);
            actionResult.add(match);
        }
        
        // Use the injected DefineOutsideAnchors component
        defineOutsideAnchors.perform(actionResult);
        
        // Return the first defined region if any
        if (!actionResult.getDefinedRegions().isEmpty()) {
            return Optional.of(actionResult.getDefinedRegions().get(0));
        }
        
        return Optional.empty();
    }

    /**
     * Creates a match at a specific location for anchor processing.
     */
    private Match createMatchAtLocation(Location location) {
        return new Match.Builder()
            .setRegion(location.getCalculatedX(), location.getCalculatedY(), 1, 1)
            .build();
    }

    /**
     * Container for location and its associated anchor.
     */
    private static class LocationWithAnchor {
        final Location location;
        final CrossStateAnchor anchor;
        
        LocationWithAnchor(Location location, CrossStateAnchor anchor) {
            this.location = location;
            this.anchor = anchor;
        }
    }
}