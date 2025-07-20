package io.github.jspinak.brobot.runner.ui.registry;

import javafx.scene.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for UI components that tracks all created components.
 * Uses WeakReferences to prevent memory leaks.
 * 
 * This helps prevent duplicate component creation and provides
 * a central place to track all UI components for debugging.
 */
@Slf4j
@Component
public class UIComponentRegistry {
    private final Map<String, WeakReference<Node>> components = new ConcurrentHashMap<>();
    
    /**
     * Registers a UI component with the given ID.
     * 
     * @param id Unique identifier for the component
     * @param component The JavaFX node to register
     */
    public void register(String id, Node component) {
        if (id == null || component == null) {
            log.warn("Cannot register null id or component");
            return;
        }
        
        // Check if component already exists
        Optional<Node> existing = get(id);
        if (existing.isPresent() && existing.get() == component) {
            log.debug("Component {} already registered", id);
            return;
        }
        
        components.put(id, new WeakReference<>(component));
        log.debug("Registered component: {} ({})", id, component.getClass().getSimpleName());
    }
    
    /**
     * Gets a registered component by ID.
     * 
     * @param id The component ID
     * @return Optional containing the component if found and not garbage collected
     */
    public Optional<Node> get(String id) {
        if (id == null) {
            return Optional.empty();
        }
        
        WeakReference<Node> ref = components.get(id);
        if (ref != null) {
            Node component = ref.get();
            if (component != null) {
                return Optional.of(component);
            } else {
                // Component was garbage collected, remove the reference
                components.remove(id);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Checks if a component with the given ID is registered.
     * 
     * @param id The component ID
     * @return true if component exists and hasn't been garbage collected
     */
    public boolean isRegistered(String id) {
        return get(id).isPresent();
    }
    
    /**
     * Unregisters a component.
     * 
     * @param id The component ID to unregister
     */
    public void unregister(String id) {
        if (id != null) {
            components.remove(id);
            log.debug("Unregistered component: {}", id);
        }
    }
    
    /**
     * Clears all registered components.
     */
    public void clear() {
        int count = components.size();
        components.clear();
        log.info("Cleared {} components from registry", count);
    }
    
    /**
     * Gets the count of registered components.
     * Note: This includes components that may have been garbage collected.
     * 
     * @return The number of registered component references
     */
    public int size() {
        return components.size();
    }
    
    /**
     * Cleans up garbage collected references.
     * 
     * @return The number of references removed
     */
    public int cleanup() {
        int initialSize = components.size();
        
        // Remove entries where the component has been garbage collected
        components.entrySet().removeIf(entry -> entry.getValue().get() == null);
        
        int removed = initialSize - components.size();
        if (removed > 0) {
            log.debug("Cleaned up {} garbage collected component references", removed);
        }
        
        return removed;
    }
    
    /**
     * Gets a summary of all registered components.
     * Useful for debugging.
     * 
     * @return Map of component IDs to their class names
     */
    public Map<String, String> getComponentSummary() {
        cleanup(); // Clean up first
        
        return components.entrySet().stream()
            .filter(entry -> entry.getValue().get() != null)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().get().getClass().getSimpleName()
            ));
    }
    
    /**
     * Logs the current state of the registry.
     * Useful for debugging duplicate component issues.
     */
    public void logRegistryState() {
        Map<String, String> summary = getComponentSummary();
        
        log.info("UI Component Registry State:");
        log.info("Total registered components: {}", summary.size());
        
        if (log.isDebugEnabled()) {
            summary.forEach((id, className) -> 
                log.debug("  {} -> {}", id, className)
            );
        }
    }
}