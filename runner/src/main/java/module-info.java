module runner {
    // Spring modules
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    
    // Open packages to Spring for reflection
    opens io.github.jspinak.brobot.runner to 
        spring.core, 
        spring.beans, 
        spring.context, 
        spring.boot.autoconfigure,
        javafx.graphics;
    
    // General export
    exports io.github.jspinak.brobot.runner;
}