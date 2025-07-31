package com.example.quickstart;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * Minimal Complete Example - The absolute minimum code needed for a working Brobot application
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.quickstart",
    "io.github.jspinak.brobot"
})
public class MinimalBrobot {
    
    public static void main(String[] args) {
        // Start Spring context
        ApplicationContext context = SpringApplication.run(MinimalBrobot.class, args);
        
        // Get the Action bean
        Action action = context.getBean(Action.class);
        
        // Create an image to find
        StateImage button = new StateImage.Builder()
                .addPatterns("button")  // No .png extension needed
                .build();
        
        // Find and click it
        action.click(button);
        
        // Done!
    }
}