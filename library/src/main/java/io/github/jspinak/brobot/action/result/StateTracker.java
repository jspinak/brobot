package io.github.jspinak.brobot.action.result;

import io.github.jspinak.brobot.model.match.Match;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracks state information discovered during action execution.
 * Manages active states and state-specific match associations.
 * 
 * This class encapsulates state tracking functionality that was
 * previously embedded in ActionResult.
 * 
 * @since 2.0
 */
@Data
public class StateTracker {
    private Set<String> activeStates = new HashSet<>();
    private Map<String, List<Match>> stateMatches = new HashMap<>();
    private Map<String, Integer> stateActivationCounts = new HashMap<>();
    
    /**
     * Creates an empty StateTracker.
     */
    public StateTracker() {}
    
    /**
     * Records a state as active.
     * 
     * @param stateName The name of the active state
     */
    public void recordActiveState(String stateName) {
        if (stateName != null && !stateName.isEmpty()) {
            activeStates.add(stateName);
            stateActivationCounts.merge(stateName, 1, Integer::sum);
        }
    }
    
    /**
     * Records a match associated with a state.
     * 
     * @param stateName The state name
     * @param match The match found in that state
     */
    public void recordStateMatch(String stateName, Match match) {
        if (stateName != null && match != null) {
            recordActiveState(stateName);
            stateMatches.computeIfAbsent(stateName, k -> new ArrayList<>()).add(match);
        }
    }
    
    /**
     * Records a match and extracts state information from it.
     * 
     * @param match The match to process
     */
    public void processMatch(Match match) {
        if (match != null && match.getStateObjectData() != null) {
            String stateName = match.getStateObjectData().getOwnerStateName();
            if (stateName != null) {
                recordStateMatch(stateName, match);
            }
        }
    }
    
    /**
     * Gets all active state names.
     * 
     * @return Set of active state names
     */
    public Set<String> getActiveStates() {
        return new HashSet<>(activeStates);
    }
    
    /**
     * Gets matches for a specific state.
     * 
     * @param stateName The state name
     * @return List of matches for that state, or empty list if none
     */
    public List<Match> getMatchesForState(String stateName) {
        return stateMatches.getOrDefault(stateName, new ArrayList<>());
    }
    
    /**
     * Gets the most frequently activated state.
     * 
     * @return Optional containing the most active state name
     */
    public Optional<String> getMostActiveState() {
        return stateActivationCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }
    
    /**
     * Gets the activation count for a state.
     * 
     * @param stateName The state name
     * @return Number of times the state was activated
     */
    public int getActivationCount(String stateName) {
        return stateActivationCounts.getOrDefault(stateName, 0);
    }
    
    /**
     * Checks if a state is active.
     * 
     * @param stateName The state name to check
     * @return true if the state is active
     */
    public boolean isStateActive(String stateName) {
        return activeStates.contains(stateName);
    }
    
    /**
     * Gets the total number of active states.
     * 
     * @return Count of active states
     */
    public int getActiveStateCount() {
        return activeStates.size();
    }
    
    /**
     * Gets the total number of matches across all states.
     * 
     * @return Total match count
     */
    public int getTotalMatchCount() {
        return stateMatches.values().stream()
                .mapToInt(List::size)
                .sum();
    }
    
    /**
     * Merges state tracking data from another instance.
     * 
     * @param other The StateTracker to merge
     */
    public void merge(StateTracker other) {
        if (other != null) {
            activeStates.addAll(other.activeStates);
            
            // Merge state matches
            other.stateMatches.forEach((state, matches) -> {
                stateMatches.computeIfAbsent(state, k -> new ArrayList<>()).addAll(matches);
            });
            
            // Merge activation counts
            other.stateActivationCounts.forEach((state, count) -> {
                stateActivationCounts.merge(state, count, Integer::sum);
            });
        }
    }
    
    /**
     * Clears all state tracking data.
     */
    public void clear() {
        activeStates.clear();
        stateMatches.clear();
        stateActivationCounts.clear();
    }
    
    /**
     * Gets states sorted by activation count.
     * 
     * @return List of state names sorted by activation frequency
     */
    public List<String> getStatesByActivationFrequency() {
        return stateActivationCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets a summary of state activity.
     * 
     * @return Map of state names to match counts
     */
    public Map<String, Integer> getStateSummary() {
        Map<String, Integer> summary = new HashMap<>();
        stateMatches.forEach((state, matches) -> {
            summary.put(state, matches.size());
        });
        return summary;
    }
    
    /**
     * Formats the state tracking data as a string summary.
     * 
     * @return Formatted state summary
     */
    public String format() {
        if (activeStates.isEmpty()) {
            return "No active states";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Active states: ").append(activeStates.size());
        sb.append(" [");
        
        List<String> sortedStates = getStatesByActivationFrequency();
        for (int i = 0; i < Math.min(5, sortedStates.size()); i++) {
            if (i > 0) sb.append(", ");
            String state = sortedStates.get(i);
            sb.append(state);
            
            int matchCount = getMatchesForState(state).size();
            if (matchCount > 0) {
                sb.append("(").append(matchCount).append(")");
            }
        }
        
        if (sortedStates.size() > 5) {
            sb.append(", ...");
        }
        
        sb.append("]");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return format();
    }
}