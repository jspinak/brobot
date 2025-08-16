package io.github.jspinak.brobot.action.result;

import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import lombok.Data;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Manages a collection of Match objects found during action execution.
 * Provides operations for adding, sorting, filtering, and analyzing matches.
 * 
 * This class encapsulates all match-related operations that were previously
 * scattered throughout ActionResult, following the Single Responsibility Principle.
 * 
 * @since 2.0
 */
@Data
public class MatchCollection {
    private List<Match> matches = new ArrayList<>();
    private List<Match> initialMatches = new ArrayList<>();
    private int maxMatches = -1;
    
    /**
     * Creates an empty MatchCollection.
     */
    public MatchCollection() {}
    
    /**
     * Creates a MatchCollection with a maximum match limit.
     * 
     * @param maxMatches Maximum number of matches to retain (-1 for unlimited)
     */
    public MatchCollection(int maxMatches) {
        this.maxMatches = maxMatches;
    }
    
    /**
     * Adds one or more matches to the collection.
     * 
     * @param matchesToAdd Variable number of Match objects to add
     */
    public void add(Match... matchesToAdd) {
        for (Match match : matchesToAdd) {
            if (match != null) {
                matches.add(match);
                if (maxMatches > 0 && matches.size() > maxMatches) {
                    sortByScoreDescending();
                    matches = matches.subList(0, maxMatches);
                }
            }
        }
    }
    
    /**
     * Adds all matches from another collection.
     * 
     * @param other The MatchCollection to merge
     */
    public void addAll(MatchCollection other) {
        if (other != null && other.matches != null) {
            matches.addAll(other.matches);
            enforceMaxMatches();
        }
    }
    
    /**
     * Adds all matches from a list.
     * 
     * @param matchList List of matches to add
     */
    public void addAll(List<Match> matchList) {
        if (matchList != null) {
            matches.addAll(matchList);
            enforceMaxMatches();
        }
    }
    
    /**
     * Stores the initial state of matches before processing.
     */
    public void captureInitialState() {
        initialMatches = new ArrayList<>(matches);
    }
    
    /**
     * Sorts matches using the specified strategy.
     * 
     * @param strategy The sorting strategy to apply
     */
    public void sort(SortStrategy strategy) {
        switch (strategy) {
            case SCORE_ASCENDING:
                sortByScore();
                break;
            case SCORE_DESCENDING:
                sortByScoreDescending();
                break;
            case SIZE_ASCENDING:
                sortBySize();
                break;
            case SIZE_DESCENDING:
                sortBySizeDescending();
                break;
            default:
                break;
        }
    }
    
    /**
     * Sorts matches by score in ascending order (lowest scores first).
     */
    public void sortByScore() {
        matches.sort(Comparator.comparingDouble(Match::getScore));
    }
    
    /**
     * Sorts matches by score in descending order (highest scores first).
     */
    public void sortByScoreDescending() {
        matches.sort(Comparator.comparingDouble(Match::getScore).reversed());
    }
    
    /**
     * Sorts matches by size in ascending order (smallest first).
     */
    public void sortBySize() {
        matches.sort(Comparator.comparing(Match::size));
    }
    
    /**
     * Sorts matches by size in descending order (largest first).
     */
    public void sortBySizeDescending() {
        matches.sort(Comparator.comparing(Match::size).reversed());
    }
    
    /**
     * Sorts matches by distance from a location.
     * 
     * @param location The reference location
     */
    public void sortByDistanceFrom(Location location) {
        matches.sort(Comparator.comparingDouble(m -> getDistance(m, location)));
    }
    
    /**
     * Gets the best (highest scoring) match.
     * 
     * @return Optional containing the best match, or empty if no matches
     */
    public Optional<Match> getBest() {
        return matches.stream()
                .max(Comparator.comparingDouble(Match::getScore));
    }
    
    /**
     * Gets the match closest to a specified location.
     * 
     * @param location Target location
     * @return Optional containing the closest match, or empty if no matches
     */
    public Optional<Match> getClosestTo(Location location) {
        if (matches.isEmpty()) return Optional.empty();
        
        return matches.stream()
                .min(Comparator.comparingDouble(m -> getDistance(m, location)));
    }
    
    /**
     * Filters matches using a predicate.
     * 
     * @param predicate The filter condition
     * @return New MatchCollection containing filtered matches
     */
    public MatchCollection filter(Predicate<Match> predicate) {
        MatchCollection filtered = new MatchCollection(maxMatches);
        filtered.matches = matches.stream()
                .filter(predicate)
                .collect(Collectors.toList());
        return filtered;
    }
    
    /**
     * Filters matches by minimum score.
     * 
     * @param minScore Minimum score threshold
     * @return New MatchCollection with matches above threshold
     */
    public MatchCollection filterByMinScore(double minScore) {
        return filter(m -> m.getScore() >= minScore);
    }
    
    /**
     * Gets matches from a specific state object.
     * 
     * @param stateObjectId The state object identifier
     * @return List of matches from that state object
     */
    public List<Match> getByStateObject(String stateObjectId) {
        return matches.stream()
                .filter(m -> m.getStateObjectData() != null && 
                           Objects.equals(m.getStateObjectData().getStateObjectId(), stateObjectId))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets matches from a specific owner state.
     * 
     * @param ownerStateName The owner state name
     * @return List of matches from that state
     */
    public List<Match> getByOwnerState(String ownerStateName) {
        return matches.stream()
                .filter(m -> ownerStateName.equals(m.getOwnerStateName()))
                .collect(Collectors.toList());
    }
    
    /**
     * Extracts regions from all matches.
     * 
     * @return List of regions corresponding to match locations
     */
    public List<Region> getRegions() {
        return matches.stream()
                .map(Match::getRegion)
                .collect(Collectors.toList());
    }
    
    /**
     * Extracts target locations from all matches.
     * 
     * @return List of target locations for all matches
     */
    public List<Location> getLocations() {
        return matches.stream()
                .map(Match::getTarget)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets unique state object IDs from all matches.
     * 
     * @return Set of unique state object identifiers
     */
    public Set<String> getUniqueStateObjectIds() {
        return matches.stream()
                .filter(m -> m.getStateObjectData() != null)
                .map(m -> m.getStateObjectData().getStateObjectId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    /**
     * Gets unique owner state names from all matches.
     * 
     * @return Set of unique owner state names
     */
    public Set<String> getUniqueOwnerStates() {
        return matches.stream()
                .map(Match::getOwnerStateName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    /**
     * Checks if the collection contains a specific match.
     * 
     * @param match The match to search for
     * @return true if the match exists in this collection
     */
    public boolean contains(Match match) {
        return matches.contains(match);
    }
    
    /**
     * Gets the number of matches.
     * 
     * @return Count of matches in the collection
     */
    public int size() {
        return matches.size();
    }
    
    /**
     * Checks if the collection is empty.
     * 
     * @return true if no matches exist
     */
    public boolean isEmpty() {
        return matches.isEmpty();
    }
    
    /**
     * Clears all matches from the collection.
     */
    public void clear() {
        matches.clear();
        initialMatches.clear();
    }
    
    /**
     * Gets statistics about the match collection.
     * 
     * @return MatchStatistics object with statistical data
     */
    public MatchStatistics getStatistics() {
        return new MatchStatistics(matches);
    }
    
    /**
     * Updates the action count for all matches.
     * 
     * @param timesActedOn The count to set for all matches
     */
    public void setTimesActedOn(int timesActedOn) {
        matches.forEach(m -> m.setTimesActedOn(timesActedOn));
    }
    
    /**
     * Performs set subtraction with another collection.
     * 
     * @param other The collection to subtract
     * @return New MatchCollection with non-overlapping matches
     */
    public MatchCollection minus(MatchCollection other) {
        MatchCollection result = new MatchCollection(maxMatches);
        for (Match match : matches) {
            if (!other.contains(match)) {
                result.add(match);
            }
        }
        return result;
    }
    
    /**
     * Retains only matches that exist in both collections.
     * 
     * @param other The collection to intersect with
     * @return New MatchCollection with common matches
     */
    public MatchCollection intersection(MatchCollection other) {
        MatchCollection result = new MatchCollection(maxMatches);
        for (Match match : matches) {
            if (other.contains(match)) {
                result.add(match);
            }
        }
        return result;
    }
    
    private double getDistance(Match match, Location location) {
        int xDist = match.x() - location.getCalculatedX();
        int yDist = match.y() - location.getCalculatedY();
        return Math.sqrt(xDist * xDist + yDist * yDist);
    }
    
    private void enforceMaxMatches() {
        if (maxMatches > 0 && matches.size() > maxMatches) {
            sortByScoreDescending();
            matches = new ArrayList<>(matches.subList(0, maxMatches));
        }
    }
    
    /**
     * Sorting strategies for match collections.
     */
    public enum SortStrategy {
        SCORE_ASCENDING,
        SCORE_DESCENDING,
        SIZE_ASCENDING,
        SIZE_DESCENDING,
        DISTANCE_FROM_LOCATION
    }
}