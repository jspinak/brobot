package io.github.jspinak.brobot.model.conditional;

/**
 * Placeholder for ConditionalActionChain.
 * In a real project, this would come from the Brobot library.
 */
public class ConditionalActionChain {
    
    public static ConditionalActionChain start() {
        return new ConditionalActionChain();
    }
    
    public ConditionalActionChain find(String imageName) {
        return this;
    }
    
    public ConditionalActionChain ifFound(Runnable action) {
        return this;
    }
    
    public ConditionalActionChain ifNotFound(Runnable action) {
        return this;
    }
    
    public ConditionalActionChain then(Runnable action) {
        return this;
    }
    
    public ConditionalActionChain orElse(Runnable action) {
        return this;
    }
    
    public void execute() {
        // Placeholder implementation
    }
}