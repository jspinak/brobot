package io.github.jspinak.brobot.action.internal.region;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateStore;
import io.github.jspinak.brobot.util.region.RegionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        if (!shouldUpdateSearchRegion(stateImage)) {
            log.debug("Skipping search region update for '{}' - conditions not met", stateImage.getName());
            return;
        }
        
        SearchRegionOnObject config = stateImage.getSearchRegionOnObject();
        if (config == null) return;

        log.debug("Attempting to resolve search region for '{}' based on '{}' from '{}'",
                stateImage.getName(), config.getTargetObjectName(), config.getTargetStateName());
        
        Optional<Region> newRegion = resolveRegionFromMatch(config, actionResult);
        if (newRegion.isPresent()) {
            stateImage.setFixedSearchRegion(newRegion.get());
            log.info("✓ Resolved search region for '{}' -> {}", 
                    stateImage.getName(), newRegion.get());
        } else {
            log.debug("Could not resolve search region for '{}' - target not found", 
                    stateImage.getName());
        }
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
     * 1. The object has a SearchRegionOnObject configuration
     * 2. The object does not already have a fixed search region
     * 
     * Note: We don't check if the object itself was found - that's irrelevant.
     * What matters is whether the TARGET object (referenced in SearchRegionOnObject) was found.
     */
    private boolean shouldUpdateSearchRegion(StateImage stateImage) {
        // If there's no SearchRegionOnObject, there's nothing to update
        if (stateImage.getSearchRegionOnObject() == null) {
            return false;
        }
        
        // Update if the image doesn't already have a fixed region
        // The actual resolution will check if the target was found
        return !stateImage.hasDefinedSearchRegion();
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
        log.info("[DYNAMIC DEBUG] DynamicRegionResolver: Registering dependencies for {} objects", objects.size());
        int registeredCount = 0;
        
        for (StateObject obj : objects) {
            if (obj instanceof StateImage) {
                StateImage stateImage = (StateImage) obj;
                if (stateImage.getSearchRegionOnObject() != null) {
                    log.info("[DYNAMIC DEBUG] Registering dependency for StateImage: {} (owner: {})", 
                        stateImage.getName(), stateImage.getOwnerStateName());
                    log.info("[DYNAMIC DEBUG] Depends on: {}:{}", 
                        stateImage.getSearchRegionOnObject().getTargetStateName(),
                        stateImage.getSearchRegionOnObject().getTargetObjectName());
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
        if (matches.isEmpty()) return;
        
        // Group updates by source for concise logging
        Map<String, Integer> updateCounts = new HashMap<>();
        int totalUpdates = 0;
        
        for (Match match : matches) {
            if (match.getStateObjectData() == null) {
                continue;
            }
            
            String sourceName = match.getStateObjectData().getOwnerStateName();
            String sourceObject = match.getStateObjectData().getStateObjectName();
            String sourceKey = sourceName + ":" + sourceObject;
            
            // Get all objects that depend on this match
            Set<SearchRegionDependencyRegistry.DependentObject> dependents = 
                dependencyRegistry.getDependents(sourceName, sourceObject);
            
            if (!dependents.isEmpty()) {
                updateCounts.merge(sourceKey, dependents.size(), Integer::sum);
                totalUpdates += dependents.size();
                
                for (SearchRegionDependencyRegistry.DependentObject dependent : dependents) {
                    updateDependentSearchRegion(dependent, match);
                }
            }
        }
        
        // Log summary once
        if (totalUpdates > 0) {
            if (updateCounts.size() == 1) {
                Map.Entry<String, Integer> entry = updateCounts.entrySet().iterator().next();
                log.info("[DYNAMIC] Updated {} dependent regions for {}", entry.getValue(), entry.getKey());
            } else {
                log.info("[DYNAMIC] Updated {} dependent regions across {} sources", totalUpdates, updateCounts.size());
                if (log.isDebugEnabled()) {
                    updateCounts.forEach((source, count) -> 
                        log.debug("  {} → {} updates", source, count));
                }
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
            StateImage stateImage = (StateImage) targetObject;
            
            // Check if there's an existing fixed region
            boolean hasExistingFixed = false;
            Region existingFixed = null;
            if (!stateImage.getPatterns().isEmpty()) {
                Pattern firstPattern = stateImage.getPatterns().get(0);
                existingFixed = firstPattern.getSearchRegions().getFixedRegion();
                hasExistingFixed = existingFixed != null && existingFixed.isDefined();
            }
            
            // If there's an existing fixed region, check if it's within the new declarative region
            if (hasExistingFixed && existingFixed != null) {
                boolean isWithinNewRegion = RegionUtils.contains(newRegion, existingFixed);
                
                if (!isWithinNewRegion) {
                    // The fixed region is outside the declarative search region, so clear it
                    log.info("Fixed region {} for {} is outside new declarative region {}, clearing fixed region", 
                            existingFixed, targetObject.getName(), newRegion);
                    stateImage.getPatterns().forEach(pattern -> {
                        pattern.getSearchRegions().resetFixedRegion();
                        pattern.setFixed(false);
                    });
                    // Set the search regions to the new declarative region
                    stateImage.getPatterns().forEach(pattern -> {
                        pattern.getSearchRegions().setRegions(List.of(newRegion));
                    });
                } else {
                    log.debug("Fixed region {} for {} is within new declarative region {}, keeping fixed region", 
                            existingFixed, targetObject.getName(), newRegion);
                    // Still update the search regions even if keeping the fixed region
                    stateImage.getPatterns().forEach(pattern -> {
                        pattern.getSearchRegions().setRegions(List.of(newRegion));
                    });
                }
            } else {
                // No existing fixed region, just set the search regions
                stateImage.getPatterns().forEach(pattern -> {
                    pattern.getSearchRegions().setRegions(List.of(newRegion));
                });
                log.info("Set search regions for {} based on {} to {}", 
                         targetObject.getName(), sourceMatch.getStateObjectData().getStateObjectName(), newRegion);
            }
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