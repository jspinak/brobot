package io.github.jspinak.brobot.action.result;

import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides filtering operations for match collections.
 * Static utility methods for common filtering patterns.
 * 
 * This class extracts filtering operations that were previously
 * embedded in ActionResult.
 * 
 * @since 2.0
 */
public class MatchFilter {
    
    private MatchFilter() {
        // Utility class, prevent instantiation
    }
    
    /**
     * Filters matches by state object ID.
     * 
     * @param matches List of matches to filter
     * @param objectId The state object ID to filter by
     * @return Filtered list of matches
     */
    public static List<Match> byStateObject(List<Match> matches, String objectId) {
        if (matches == null || objectId == null) {
            return List.of();
        }
        
        return matches.stream()
                .filter(m -> m.getStateObjectData() != null &&
                           Objects.equals(m.getStateObjectData().getStateObjectId(), objectId))
                .collect(Collectors.toList());
    }
    
    /**
     * Filters matches by owner state name.
     * 
     * @param matches List of matches to filter
     * @param stateName The owner state name to filter by
     * @return Filtered list of matches
     */
    public static List<Match> byOwnerState(List<Match> matches, String stateName) {
        if (matches == null || stateName == null) {
            return List.of();
        }
        
        return matches.stream()
                .filter(m -> stateName.equals(m.getOwnerStateName()))
                .collect(Collectors.toList());
    }
    
    /**
     * Filters matches by minimum similarity score.
     * 
     * @param matches List of matches to filter
     * @param minScore Minimum score threshold (0.0-1.0)
     * @return Filtered list of matches
     */
    public static List<Match> byMinScore(List<Match> matches, double minScore) {
        if (matches == null) {
            return List.of();
        }
        
        return matches.stream()
                .filter(m -> m.getScore() >= minScore)
                .collect(Collectors.toList());
    }
    
    /**
     * Filters matches by maximum similarity score.
     * 
     * @param matches List of matches to filter
     * @param maxScore Maximum score threshold (0.0-1.0)
     * @return Filtered list of matches
     */
    public static List<Match> byMaxScore(List<Match> matches, double maxScore) {
        if (matches == null) {
            return List.of();
        }
        
        return matches.stream()
                .filter(m -> m.getScore() <= maxScore)
                .collect(Collectors.toList());
    }
    
    /**
     * Filters matches within a score range.
     * 
     * @param matches List of matches to filter
     * @param minScore Minimum score threshold
     * @param maxScore Maximum score threshold
     * @return Filtered list of matches
     */
    public static List<Match> byScoreRange(List<Match> matches, double minScore, double maxScore) {
        if (matches == null) {
            return List.of();
        }
        
        return matches.stream()
                .filter(m -> m.getScore() >= minScore && m.getScore() <= maxScore)
                .collect(Collectors.toList());
    }
    
    /**
     * Filters matches that contain a specific match within their bounds.
     * 
     * @param matches List of potential container matches
     * @param target The match that must be contained
     * @return Filtered list of containing matches
     */
    public static List<Match> containing(List<Match> matches, Match target) {
        if (matches == null || target == null) {
            return List.of();
        }
        
        return matches.stream()
                .filter(m -> contains(m, target))
                .collect(Collectors.toList());
    }
    
    /**
     * Filters matches that are contained within a region.
     * 
     * @param matches List of matches to filter
     * @param region The containing region
     * @return Filtered list of contained matches
     */
    public static List<Match> withinRegion(List<Match> matches, Region region) {
        if (matches == null || region == null) {
            return List.of();
        }
        
        return matches.stream()
                .filter(m -> isWithin(m, region))
                .collect(Collectors.toList());
    }
    
    /**
     * Filters matches that overlap with a region.
     * 
     * @param matches List of matches to filter
     * @param region The region to check overlap with
     * @return Filtered list of overlapping matches
     */
    public static List<Match> overlapping(List<Match> matches, Region region) {
        if (matches == null || region == null) {
            return List.of();
        }
        
        return matches.stream()
                .filter(m -> overlaps(m, region))
                .collect(Collectors.toList());
    }
    
    /**
     * Filters matches within a distance from a location.
     * 
     * @param matches List of matches to filter
     * @param location The reference location
     * @param maxDistance Maximum distance in pixels
     * @return Filtered list of nearby matches
     */
    public static List<Match> nearLocation(List<Match> matches, Location location, double maxDistance) {
        if (matches == null || location == null) {
            return List.of();
        }
        
        return matches.stream()
                .filter(m -> getDistance(m, location) <= maxDistance)
                .collect(Collectors.toList());
    }
    
    /**
     * Filters matches by minimum area.
     * 
     * @param matches List of matches to filter
     * @param minArea Minimum area in pixels
     * @return Filtered list of matches
     */
    public static List<Match> byMinArea(List<Match> matches, int minArea) {
        if (matches == null) {
            return List.of();
        }
        
        return matches.stream()
                .filter(m -> m.w() * m.h() >= minArea)
                .collect(Collectors.toList());
    }
    
    /**
     * Filters matches by maximum area.
     * 
     * @param matches List of matches to filter
     * @param maxArea Maximum area in pixels
     * @return Filtered list of matches
     */
    public static List<Match> byMaxArea(List<Match> matches, int maxArea) {
        if (matches == null) {
            return List.of();
        }
        
        return matches.stream()
                .filter(m -> m.w() * m.h() <= maxArea)
                .collect(Collectors.toList());
    }
    
    /**
     * Filters matches by custom predicate.
     * 
     * @param matches List of matches to filter
     * @param predicate The filter condition
     * @return Filtered list of matches
     */
    public static List<Match> byPredicate(List<Match> matches, Predicate<Match> predicate) {
        if (matches == null || predicate == null) {
            return List.of();
        }
        
        return matches.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the first N matches (after optional sorting).
     * 
     * @param matches List of matches
     * @param count Maximum number to return
     * @return List with at most 'count' matches
     */
    public static List<Match> limit(List<Match> matches, int count) {
        if (matches == null || count <= 0) {
            return List.of();
        }
        
        return matches.stream()
                .limit(count)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets unique matches by state object ID.
     * Returns one match per unique state object.
     * 
     * @param matches List of matches
     * @return List with one match per state object
     */
    public static List<Match> uniqueByStateObject(List<Match> matches) {
        if (matches == null) {
            return List.of();
        }
        
        return matches.stream()
                .filter(m -> m.getStateObjectData() != null)
                .collect(Collectors.toMap(
                    m -> m.getStateObjectData().getStateObjectId(),
                    m -> m,
                    (m1, m2) -> m1.getScore() > m2.getScore() ? m1 : m2))
                .values().stream()
                .collect(Collectors.toList());
    }
    
    /**
     * Removes duplicate matches at the same location.
     * Keeps the best scoring match at each location.
     * 
     * @param matches List of matches
     * @param tolerance Position tolerance for considering matches as duplicates
     * @return List without duplicates
     */
    public static List<Match> removeDuplicates(List<Match> matches, int tolerance) {
        if (matches == null) {
            return List.of();
        }
        
        List<Match> unique = new java.util.ArrayList<>();
        
        for (Match candidate : matches) {
            boolean isDuplicate = false;
            
            for (int i = 0; i < unique.size(); i++) {
                Match existing = unique.get(i);
                if (isNearby(candidate, existing, tolerance)) {
                    isDuplicate = true;
                    // Keep the better scoring match
                    if (candidate.getScore() > existing.getScore()) {
                        unique.set(i, candidate);
                    }
                    break;
                }
            }
            
            if (!isDuplicate) {
                unique.add(candidate);
            }
        }
        
        return unique;
    }
    
    // Helper methods
    
    private static boolean contains(Match container, Match target) {
        return container.x() <= target.x() &&
               container.y() <= target.y() &&
               container.x() + container.w() >= target.x() + target.w() &&
               container.y() + container.h() >= target.y() + target.h();
    }
    
    private static boolean isWithin(Match match, Region region) {
        return match.x() >= region.getX() &&
               match.y() >= region.getY() &&
               match.x() + match.w() <= region.getX() + region.getW() &&
               match.y() + match.h() <= region.getY() + region.getH();
    }
    
    private static boolean overlaps(Match match, Region region) {
        return !(match.x() + match.w() <= region.getX() ||
                region.getX() + region.getW() <= match.x() ||
                match.y() + match.h() <= region.getY() ||
                region.getY() + region.getH() <= match.y());
    }
    
    private static double getDistance(Match match, Location location) {
        Location matchCenter = match.getTarget();
        double dx = matchCenter.getCalculatedX() - location.getCalculatedX();
        double dy = matchCenter.getCalculatedY() - location.getCalculatedY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    private static boolean isNearby(Match m1, Match m2, int tolerance) {
        return Math.abs(m1.x() - m2.x()) <= tolerance &&
               Math.abs(m1.y() - m2.y()) <= tolerance;
    }
}