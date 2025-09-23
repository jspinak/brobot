package io.github.jspinak.brobot.action.internal.region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateStore;
import io.github.jspinak.brobot.util.region.RegionUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Resolves and updates search regions dynamically based on matches from other state objects. This
 * service enables cross-state search region definitions, allowing objects to define their search
 * areas based on the location of other objects, even from different states.
 */
@Component
@Slf4j
public class DynamicRegionResolver {

    private final StateStore stateStore;
    private final SearchRegionDependencyRegistry dependencyRegistry;

    public DynamicRegionResolver(
            StateStore stateStore, SearchRegionDependencyRegistry dependencyRegistry) {
        this.stateStore = stateStore;
        this.dependencyRegistry = dependencyRegistry;
    }

    /**
     * Updates search regions for a state object based on cross-state references.
     *
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
        log.info("updateStateImageSearchRegion called for '{}'", stateImage.getName());
        if (!shouldUpdateSearchRegion(stateImage)) {
            log.info(
                    "Skipping search region update for '{}' - conditions not met (no"
                            + " SearchRegionOnObject or already has defined region)",
                    stateImage.getName());
            return;
        }

        SearchRegionOnObject config = stateImage.getSearchRegionOnObject();
        if (config == null || actionResult == null) return;

        log.info(
                "IMPORTANT: Attempting to resolve search regions for '{}' based on '{}' from '{}'",
                stateImage.getName(),
                config.getTargetObjectName(),
                config.getTargetStateName());

        // Get all resolved regions from the ActionResult matches
        List<Region> resolvedRegions = resolveRegionsFromActionResult(config, actionResult);

        if (!resolvedRegions.isEmpty()) {
            // Set the first resolved region as a fixed search region for the StateImage
            stateImage.setFixedSearchRegion(resolvedRegions.get(0));

            log.info(
                    "✓ Resolved {} search regions for '{}' from '{}' matches",
                    resolvedRegions.size(),
                    stateImage.getName(),
                    config.getTargetObjectName());

            // Log the first region for debugging
            log.debug("  First region: {}", resolvedRegions.get(0));
        } else {
            log.warn(
                    "Could not resolve search regions for '{}' - target '{}' has no matches",
                    stateImage.getName(),
                    config.getTargetObjectName());
        }
    }

    private void updateStateLocationSearchRegion(
            StateLocation stateLocation, ActionResult actionResult) {
        if (stateLocation.getSearchRegionOnObject() == null || actionResult == null) return;

        // StateLocation uses its location as the center of a search region
        SearchRegionOnObject config = stateLocation.getSearchRegionOnObject();
        List<Region> resolvedRegions = resolveRegionsFromActionResult(config, actionResult);

        if (!resolvedRegions.isEmpty()) {
            // Update the location based on the center of the first region
            // (StateLocation typically represents a single point)
            Region region = resolvedRegions.get(0);
            int centerX = region.getX() + region.getW() / 2;
            int centerY = region.getY() + region.getH() / 2;
            stateLocation.setLocation(new Location(centerX, centerY));
            log.debug(
                    "Updated StateLocation '{}' to [{}, {}]",
                    stateLocation.getName(),
                    centerX,
                    centerY);
        }
    }

    private void updateStateRegionSearchRegion(StateRegion stateRegion, ActionResult actionResult) {
        // Similar implementation for StateRegion if it gets SearchRegionOnObject support
    }

    /**
     * Checks if the search region should be updated based on the conditions: 1. The object has a
     * SearchRegionOnObject configuration 2. The object does not already have a fixed search region
     *
     * <p>Note: We don't check if the object itself was found - that's irrelevant. What matters is
     * whether the TARGET object (referenced in SearchRegionOnObject) was found.
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
     * Resolves search regions from the ActionResult matches based on the SearchRegionOnObject
     * configuration. Filters matches by the target state and object names specified in the config.
     */
    private List<Region> resolveRegionsFromActionResult(
            SearchRegionOnObject config, ActionResult actionResult) {
        if (actionResult == null || actionResult.getMatchList() == null) {
            return new ArrayList<>();
        }

        String targetStateName = config.getTargetStateName();
        String targetObjectName = config.getTargetObjectName();

        // Filter matches by the target state and object names
        List<Match> sourceMatches =
                actionResult.getMatchList().stream()
                        .filter(
                                match -> {
                                    StateObjectMetadata metadata = match.getStateObjectData();
                                    return metadata != null
                                            && targetStateName.equals(metadata.getOwnerStateName())
                                            && targetObjectName.equals(
                                                    metadata.getStateObjectName());
                                })
                        .collect(Collectors.toList());

        if (sourceMatches.isEmpty()) {
            return new ArrayList<>();
        }

        List<Region> resolvedRegions = new ArrayList<>();
        MatchAdjustmentOptions adjustments = config.getAdjustments();

        // Create a region for each match with adjustments
        for (Match match : sourceMatches) {
            Region baseRegion = match.getRegion();

            int x = baseRegion.getX();
            int y = baseRegion.getY();
            int w = baseRegion.getW();
            int h = baseRegion.getH();

            if (adjustments != null) {
                x += adjustments.getAddX();
                y += adjustments.getAddY();
                w += adjustments.getAddW();
                h += adjustments.getAddH();

                // Apply absolute adjustments if set
                if (adjustments.getAbsoluteW() > 0) {
                    w = adjustments.getAbsoluteW();
                }
                if (adjustments.getAbsoluteH() > 0) {
                    h = adjustments.getAbsoluteH();
                }
            }

            resolvedRegions.add(new Region(x, y, w, h));
        }

        return resolvedRegions;
    }

    /**
     * Resolves search regions from matches based on the SearchRegionOnObject configuration. Creates
     * regions around all matches (for FIND.ALL scenarios).
     *
     * @deprecated Use resolveRegionsFromActionResult instead
     */
    @Deprecated
    private List<Region> resolveRegionsFromMatches(SearchRegionOnObject config) {
        // Find all source matches
        List<Match> sourceMatches = findMatchesForConfig(config);
        if (sourceMatches.isEmpty()) {
            return new ArrayList<>();
        }

        List<Region> resolvedRegions = new ArrayList<>();
        MatchAdjustmentOptions adjustments = config.getAdjustments();

        // Create a region for each match with adjustments
        for (Match match : sourceMatches) {
            Region baseRegion = match.getRegion();

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

            resolvedRegions.add(new Region(x, y, w, h));
        }

        log.debug(
                "Resolved {} search regions from {} matches",
                resolvedRegions.size(),
                sourceMatches.size());
        return resolvedRegions;
    }

    /**
     * Finds matches that correspond to the SearchRegionOnObject configuration. Returns all matches
     * (for FIND.ALL scenarios) to calculate search regions.
     */
    private List<Match> findMatchesForConfig(SearchRegionOnObject config) {
        String targetStateName = config.getTargetStateName();
        String targetObjectName = config.getTargetObjectName();
        StateObject.Type targetType = config.getTargetType();

        log.debug("Looking for matches of '{}' from state '{}'", targetObjectName, targetStateName);

        // Get the target state from the state store
        Optional<State> targetStateOpt = stateStore.getState(targetStateName);
        if (targetStateOpt.isEmpty()) {
            log.warn("Target state '{}' not found in state store", targetStateName);
            return new ArrayList<>();
        }

        State targetState = targetStateOpt.get();
        log.debug("Found target state '{}' in store", targetStateName);

        // Find the target object in the state and get its lastMatchesFound
        switch (targetType) {
            case IMAGE:
                return targetState.getStateImages().stream()
                        .filter(img -> img.getName().equals(targetObjectName))
                        .findFirst()
                        .map(
                                img -> {
                                    log.info(
                                            "Found target StateImage '{}' (instance: {}), checking"
                                                    + " lastMatchesFound",
                                            img.getName(),
                                            System.identityHashCode(img));
                                    List<Match> lastMatches = img.getLastMatchesFound();
                                    log.info(
                                            "StateImage '{}' has {} last matches stored",
                                            img.getName(),
                                            lastMatches.size());
                                    if (lastMatches.isEmpty()) {
                                        log.warn(
                                                "No lastMatchesFound for '{}' - it was never found"
                                                        + " or not searched yet!",
                                                targetObjectName);
                                    } else {
                                        log.info(
                                                "  First match region: {}",
                                                lastMatches.get(0).getRegion());
                                    }
                                    return new ArrayList<>(
                                            lastMatches); // Return a copy to avoid modification
                                })
                        .orElseGet(
                                () -> {
                                    log.warn(
                                            "Target StateImage '{}' not found in state '{}'",
                                            targetObjectName,
                                            targetStateName);
                                    return new ArrayList<>();
                                });
            case REGION:
                return targetState.getStateRegions().stream()
                        .filter(reg -> reg.getName().equals(targetObjectName))
                        .findFirst()
                        .map(
                                reg -> {
                                    Match match =
                                            new Match.Builder()
                                                    .setRegion(reg.getSearchRegion())
                                                    .setStateObjectData(reg)
                                                    .build();
                                    List<Match> matches = new ArrayList<>();
                                    matches.add(match);
                                    return matches;
                                })
                        .orElse(new ArrayList<>());
            case LOCATION:
                return targetState.getStateLocations().stream()
                        .filter(loc -> loc.getName().equals(targetObjectName))
                        .findFirst()
                        .filter(loc -> loc.getLocation() != null)
                        .map(
                                loc -> {
                                    Match match =
                                            new Match.Builder()
                                                    .setRegion(
                                                            loc.getLocation().getCalculatedX(),
                                                            loc.getLocation().getCalculatedY(),
                                                            1,
                                                            1)
                                                    .setStateObjectData(loc)
                                                    .build();
                                    List<Match> matches = new ArrayList<>();
                                    matches.add(match);
                                    return matches;
                                })
                        .orElse(new ArrayList<>());
            default:
                return new ArrayList<>();
        }
    }

    /** Checks if a match corresponds to the specified configuration. */
    private boolean matchesConfig(
            Match match, String stateName, String objectName, StateObject.Type type) {
        return match.getStateObjectData() != null
                && stateName.equals(match.getStateObjectData().getOwnerStateName())
                && objectName.equals(match.getStateObjectData().getStateObjectName());
    }

    /** Batch update search regions for multiple state objects. */
    public void updateSearchRegionsForObjects(
            List<StateObject> objects, ActionResult actionResult) {
        log.info(
                "DynamicRegionResolver.updateSearchRegionsForObjects called with {} objects",
                objects.size());
        objects.forEach(
                obj -> {
                    log.info(
                            "  Processing object: {} (type: {}, instance: {})",
                            obj.getName(),
                            obj.getClass().getSimpleName(),
                            System.identityHashCode(obj));
                    updateSearchRegions(obj, actionResult);
                });
    }

    /**
     * Registers search region dependencies for state objects. This should be called when states are
     * initialized.
     */
    public void registerDependencies(List<StateObject> objects) {
        log.info(
                "[DYNAMIC DEBUG] DynamicRegionResolver: Registering dependencies for {} objects",
                objects.size());
        int registeredCount = 0;

        for (StateObject obj : objects) {
            if (obj instanceof StateImage) {
                StateImage stateImage = (StateImage) obj;
                if (stateImage.getSearchRegionOnObject() != null) {
                    log.info(
                            "[DYNAMIC DEBUG] Registering dependency for StateImage: {} (owner: {})",
                            stateImage.getName(),
                            stateImage.getOwnerStateName());
                    log.info(
                            "[DYNAMIC DEBUG] Depends on: {}:{}",
                            stateImage.getSearchRegionOnObject().getTargetStateName(),
                            stateImage.getSearchRegionOnObject().getTargetObjectName());
                    dependencyRegistry.registerDependency(
                            stateImage, stateImage.getSearchRegionOnObject());
                    registeredCount++;
                }
            } else if (obj instanceof StateLocation) {
                StateLocation stateLoc = (StateLocation) obj;
                if (stateLoc.getSearchRegionOnObject() != null) {
                    log.debug("Registering dependency for StateLocation: {}", stateLoc.getName());
                    dependencyRegistry.registerDependency(
                            stateLoc, stateLoc.getSearchRegionOnObject());
                    registeredCount++;
                }
            }
            // Add StateRegion if it supports SearchRegionOnObject in the future
        }

        log.info("DynamicRegionResolver: Registered {} dependencies", registeredCount);
    }

    /**
     * Updates search regions for all objects that depend on the found matches. This should be
     * called after a successful find operation.
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
                log.info(
                        "[DYNAMIC] Updated {} dependent regions for {}",
                        entry.getValue(),
                        entry.getKey());
            } else {
                log.info(
                        "[DYNAMIC] Updated {} dependent regions across {} sources",
                        totalUpdates,
                        updateCounts.size());
                if (log.isDebugEnabled()) {
                    updateCounts.forEach(
                            (source, count) -> log.debug("  {} → {} updates", source, count));
                }
            }
        }
    }

    /** Updates a single dependent object's search region based on a source match. */
    private void updateDependentSearchRegion(
            SearchRegionDependencyRegistry.DependentObject dependent, Match sourceMatch) {
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
                    log.info(
                            "Fixed region {} for {} is outside new declarative region {}, clearing"
                                    + " fixed region",
                            existingFixed,
                            targetObject.getName(),
                            newRegion);
                    stateImage
                            .getPatterns()
                            .forEach(
                                    pattern -> {
                                        pattern.getSearchRegions().resetFixedRegion();
                                        pattern.setFixed(false);
                                    });
                    // Set the search regions to the new declarative region
                    stateImage
                            .getPatterns()
                            .forEach(
                                    pattern -> {
                                        pattern.getSearchRegions().setRegions(List.of(newRegion));
                                    });
                } else {
                    log.debug(
                            "Fixed region {} for {} is within new declarative region {}, keeping"
                                    + " fixed region",
                            existingFixed,
                            targetObject.getName(),
                            newRegion);
                    // Still update the search regions even if keeping the fixed region
                    stateImage
                            .getPatterns()
                            .forEach(
                                    pattern -> {
                                        pattern.getSearchRegions().setRegions(List.of(newRegion));
                                    });
                }
            } else {
                // No existing fixed region, just set the search regions
                stateImage
                        .getPatterns()
                        .forEach(
                                pattern -> {
                                    pattern.getSearchRegions().setRegions(List.of(newRegion));
                                });
                log.info(
                        "Set search regions for {} based on {} to {}",
                        targetObject.getName(),
                        sourceMatch.getStateObjectData().getStateObjectName(),
                        newRegion);
            }
        } else if (targetObject instanceof StateLocation) {
            // For StateLocation, update the location based on the center of the region
            int centerX = newRegion.getX() + newRegion.getW() / 2;
            int centerY = newRegion.getY() + newRegion.getH() / 2;
            ((StateLocation) targetObject).setLocation(new Location(centerX, centerY));
            log.debug(
                    "Updated location for {} based on {} to ({}, {})",
                    targetObject.getName(),
                    sourceMatch.getStateObjectData().getStateObjectName(),
                    centerX,
                    centerY);
        }
    }
}
