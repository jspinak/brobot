package io.github.jspinak.brobot.action.internal.region;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves and updates search regions dynamically based on matches from other state objects.
 * This service enables cross-state search region definitions, allowing objects to define
 * their search areas based on the location of other objects, even from different states.
 */
@Component
@Slf4j
public class DynamicRegionResolver {

    private final StateStore stateStore;
    private final SearchRegionDependencyRegistry dependencyRegistry;

    public DynamicRegionResolver(StateStore stateStore, SearchRegionDependencyRegistry dependencyRegistry) {
        this.stateStore = stateStore;
        this.dependencyRegistry = dependencyRegistry;
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
        MatchAdjustmentOptions adjustments = config.getAdjustments();
        
        int x = baseRegion.getX();
        int y = baseRegion.getY();
        int w = baseRegion.getW();
        int h = baseRegion.getH();
        
        if (adjustments != null) {
            x += adjustments.getAddX();
            y += adjustments.getAddY();
            w += adjustments.getAddW();
            h += adjustments.getAddH();
            
            // Apply absolute dimensions if specified (>= 0 means set)
            if (adjustments.getAbsoluteW() >= 0) w = adjustments.getAbsoluteW();
            if (adjustments.getAbsoluteH() >= 0) h = adjustments.getAbsoluteH();
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
    
    /**
     * Registers search region dependencies for state objects.
     * This should be called when states are initialized.
     */
    public void registerDependencies(List<StateObject> objects) {
        log.info("DynamicRegionResolver: Registering dependencies for {} objects", objects.size());
        int registeredCount = 0;
        
        for (StateObject obj : objects) {
            if (obj instanceof StateImage) {
                StateImage stateImage = (StateImage) obj;
                if (stateImage.getSearchRegionOnObject() != null) {
                    log.debug("Registering dependency for StateImage: {}", stateImage.getName());
                    dependencyRegistry.registerDependency(stateImage, stateImage.getSearchRegionOnObject());
                    registeredCount++;
                }
            } else if (obj instanceof StateLocation) {
                StateLocation stateLoc = (StateLocation) obj;
                if (stateLoc.getSearchRegionOnObject() != null) {
                    log.debug("Registering dependency for StateLocation: {}", stateLoc.getName());
                    dependencyRegistry.registerDependency(stateLoc, stateLoc.getSearchRegionOnObject());
                    registeredCount++;
                }
            }
            // Add StateRegion if it supports SearchRegionOnObject in the future
        }
        
        log.info("DynamicRegionResolver: Registered {} dependencies", registeredCount);
    }
    
    /**
     * Updates search regions for all objects that depend on the found matches.
     * This should be called after a successful find operation.
     */
    public void updateDependentSearchRegions(ActionResult actionResult) {
        List<Match> matches = actionResult.getMatchList();
        log.debug("updateDependentSearchRegions: Processing {} matches", matches.size());
        
        for (Match match : matches) {
            if (match.getStateObjectData() == null) {
                log.debug("Match has no StateObjectData, skipping");
                continue;
            }
            
            String sourceName = match.getStateObjectData().getOwnerStateName();
            String sourceObject = match.getStateObjectData().getStateObjectName();
            log.debug("Looking for dependents of {}:{}", sourceName, sourceObject);
            
            // Get all objects that depend on this match
            Set<SearchRegionDependencyRegistry.DependentObject> dependents = 
                dependencyRegistry.getDependents(sourceName, sourceObject);
            
            log.debug("Found {} dependents for {}:{}", dependents.size(), sourceName, sourceObject);
            
            for (SearchRegionDependencyRegistry.DependentObject dependent : dependents) {
                updateDependentSearchRegion(dependent, match);
            }
        }
    }
    
    /**
     * Updates a single dependent object's search region based on a source match.
     */
    private void updateDependentSearchRegion(SearchRegionDependencyRegistry.DependentObject dependent, 
                                            Match sourceMatch) {
        StateObject targetObject = dependent.getStateObject();
        SearchRegionOnObject config = dependent.getSearchRegionConfig();
        
        // Calculate the new region based on the source match
        Region baseRegion = sourceMatch.getRegion();
        MatchAdjustmentOptions adjustments = config.getAdjustments();
        
        int x = baseRegion.getX();
        int y = baseRegion.getY();
        int w = baseRegion.getW();
        int h = baseRegion.getH();
        
        // Apply adjustments if specified
        if (adjustments != null) {
            x += adjustments.getAddX();
            y += adjustments.getAddY();
            w += adjustments.getAddW();
            h += adjustments.getAddH();
            
            // Apply absolute dimensions if specified (>= 0 means set)
            if (adjustments.getAbsoluteW() >= 0) w = adjustments.getAbsoluteW();
            if (adjustments.getAbsoluteH() >= 0) h = adjustments.getAbsoluteH();
        }
        
        Region newRegion = new Region(x, y, w, h);
        
        // Update the target object's search region
        if (targetObject instanceof StateImage) {
            ((StateImage) targetObject).setFixedSearchRegion(newRegion);
            log.debug("Updated search region for {} based on {} to {}", 
                     targetObject.getName(), sourceMatch.getStateObjectData().getStateObjectName(), newRegion);
        } else if (targetObject instanceof StateLocation) {
            // For StateLocation, update the location based on the center of the region
            int centerX = newRegion.getX() + newRegion.getW() / 2;
            int centerY = newRegion.getY() + newRegion.getH() / 2;
            ((StateLocation) targetObject).setLocation(new Location(centerX, centerY));
            log.debug("Updated location for {} based on {} to ({}, {})", 
                     targetObject.getName(), sourceMatch.getStateObjectData().getStateObjectName(), centerX, centerY);
        }
    }
}