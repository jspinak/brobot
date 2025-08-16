package io.github.jspinak.brobot.action.result;

import io.github.jspinak.brobot.model.element.Region;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages regions created or defined during action execution.
 * Handles both anonymous and named regions.
 * 
 * This class encapsulates region management functionality that was
 * previously embedded in ActionResult.
 * 
 * @since 2.0
 */
@Data
public class RegionManager {
    private List<Region> definedRegions = new ArrayList<>();
    private Map<String, Region> namedRegions = new HashMap<>();
    
    /**
     * Creates an empty RegionManager.
     */
    public RegionManager() {}
    
    /**
     * Defines a new region.
     * 
     * @param region The region to define
     */
    public void defineRegion(Region region) {
        if (region != null) {
            definedRegions.add(region);
        }
    }
    
    /**
     * Defines a named region.
     * 
     * @param name The name for the region
     * @param region The region to define
     */
    public void defineNamedRegion(String name, Region region) {
        if (name != null && !name.isEmpty() && region != null) {
            namedRegions.put(name, region);
            definedRegions.add(region);
        }
    }
    
    /**
     * Gets the primary (first) defined region.
     * 
     * @return The first defined region, or empty region if none
     */
    public Region getPrimaryRegion() {
        if (definedRegions.isEmpty()) {
            return new Region();
        }
        return definedRegions.get(0);
    }
    
    /**
     * Gets a region by name.
     * 
     * @param name The region name
     * @return Optional containing the region, or empty if not found
     */
    public Optional<Region> getRegion(String name) {
        return Optional.ofNullable(namedRegions.get(name));
    }
    
    /**
     * Gets all defined regions.
     * 
     * @return List of all defined regions
     */
    public List<Region> getAllRegions() {
        return new ArrayList<>(definedRegions);
    }
    
    /**
     * Gets all named regions.
     * 
     * @return Map of region names to regions
     */
    public Map<String, Region> getNamedRegions() {
        return new HashMap<>(namedRegions);
    }
    
    /**
     * Gets the number of defined regions.
     * 
     * @return Count of defined regions
     */
    public int size() {
        return definedRegions.size();
    }
    
    /**
     * Checks if any regions are defined.
     * 
     * @return true if no regions are defined
     */
    public boolean isEmpty() {
        return definedRegions.isEmpty();
    }
    
    /**
     * Checks if a named region exists.
     * 
     * @param name The region name
     * @return true if the named region exists
     */
    public boolean hasRegion(String name) {
        return namedRegions.containsKey(name);
    }
    
    /**
     * Removes a named region.
     * 
     * @param name The region name to remove
     * @return The removed region, or null if not found
     */
    public Region removeRegion(String name) {
        Region removed = namedRegions.remove(name);
        if (removed != null) {
            definedRegions.remove(removed);
        }
        return removed;
    }
    
    /**
     * Gets the union of all defined regions.
     * Creates a bounding box containing all regions.
     * 
     * @return Optional containing the union region, or empty if no regions
     */
    public Optional<Region> getUnion() {
        if (definedRegions.isEmpty()) {
            return Optional.empty();
        }
        
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        
        for (Region r : definedRegions) {
            minX = Math.min(minX, r.getX());
            minY = Math.min(minY, r.getY());
            maxX = Math.max(maxX, r.getX() + r.getW());
            maxY = Math.max(maxY, r.getY() + r.getH());
        }
        
        return Optional.of(new Region(minX, minY, maxX - minX, maxY - minY));
    }
    
    /**
     * Gets the intersection of all defined regions.
     * Returns the area common to all regions.
     * 
     * @return Optional containing the intersection, or empty if no overlap
     */
    public Optional<Region> getIntersection() {
        if (definedRegions.isEmpty()) {
            return Optional.empty();
        }
        
        if (definedRegions.size() == 1) {
            return Optional.of(definedRegions.get(0));
        }
        
        Region intersection = definedRegions.get(0);
        for (int i = 1; i < definedRegions.size(); i++) {
            Region current = definedRegions.get(i);
            
            int x = Math.max(intersection.getX(), current.getX());
            int y = Math.max(intersection.getY(), current.getY());
            int x2 = Math.min(intersection.getX() + intersection.getW(), 
                            current.getX() + current.getW());
            int y2 = Math.min(intersection.getY() + intersection.getH(), 
                            current.getY() + current.getH());
            
            if (x2 <= x || y2 <= y) {
                // No intersection
                return Optional.empty();
            }
            
            intersection = new Region(x, y, x2 - x, y2 - y);
        }
        
        return Optional.of(intersection);
    }
    
    /**
     * Gets regions sorted by area.
     * 
     * @param ascending true for smallest first, false for largest first
     * @return List of regions sorted by area
     */
    public List<Region> getRegionsByArea(boolean ascending) {
        Comparator<Region> comparator = Comparator.comparingInt(r -> r.getW() * r.getH());
        if (!ascending) {
            comparator = comparator.reversed();
        }
        
        return definedRegions.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
    
    /**
     * Merges region data from another instance.
     * 
     * @param other The RegionManager to merge
     */
    public void merge(RegionManager other) {
        if (other != null) {
            definedRegions.addAll(other.definedRegions);
            namedRegions.putAll(other.namedRegions);
        }
    }
    
    /**
     * Clears all region data.
     */
    public void clear() {
        definedRegions.clear();
        namedRegions.clear();
    }
    
    /**
     * Formats the region data as a string summary.
     * 
     * @return Formatted region summary
     */
    public String format() {
        if (definedRegions.isEmpty()) {
            return "No regions defined";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Regions: ").append(definedRegions.size());
        
        if (!namedRegions.isEmpty()) {
            sb.append(" (").append(namedRegions.size()).append(" named)");
        }
        
        if (definedRegions.size() <= 3) {
            sb.append(" [");
            for (int i = 0; i < definedRegions.size(); i++) {
                if (i > 0) sb.append(", ");
                Region r = definedRegions.get(i);
                sb.append(String.format("%dx%d@(%d,%d)", r.getW(), r.getH(), r.getX(), r.getY()));
            }
            sb.append("]");
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return format();
    }
}