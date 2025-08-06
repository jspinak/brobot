package com.example.movement.examples;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Random;

/**
 * Demonstrates basic mouse movement operations in Brobot v1.1.0.
 * Shows how to use MouseMoveOptions for direct mouse control.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BasicMovementExample {
    
    private final Action action;
    private final Random random = new Random();
    
    /**
     * Direct mouse movement using MouseMoveOptions
     */
    public void directMouseMovement() {
        log.info("=== Direct Mouse Movement ===");
        
        // Move to center of screen
        Location center = new Location(960, 540); // Assuming 1920x1080
        
        MouseMoveOptions moveToCenter = new MouseMoveOptions.Builder()
            .setMoveMouseDelay(0.5f)  // Control movement speed
            .setPauseAfterEnd(1.0)    // Pause after reaching destination
            .build();
        
        log.info("Moving to center of screen: {}", center);
        ObjectCollection centerCollection = new ObjectCollection.Builder()
            .withLocations(center)
            .build();
        
        ActionResult result = action.perform(moveToCenter, centerCollection);
        
        if (result.isSuccess()) {
            log.info("✓ Successfully moved to center");
        } else {
            log.error("✗ Movement failed");
        }
        
        // Move to corners with different speeds
        Location[] corners = {
            new Location(100, 100),    // Top-left
            new Location(1820, 100),   // Top-right
            new Location(1820, 980),   // Bottom-right
            new Location(100, 980)     // Bottom-left
        };
        
        float[] speeds = {0.3f, 0.7f, 0.5f, 0.2f}; // Different speeds for each corner
        
        for (int i = 0; i < corners.length; i++) {
            log.info("Moving to {} corner with speed {}", getCornerName(i), speeds[i]);
            
            MouseMoveOptions cornerMove = new MouseMoveOptions.Builder()
                .setMoveMouseDelay(speeds[i])
                .setPauseAfterEnd(0.5)
                .build();
                
            ObjectCollection cornerCollection = new ObjectCollection.Builder()
                .withLocations(corners[i])
                .build();
                
            action.perform(cornerMove, cornerCollection);
        }
    }
    
    private String getCornerName(int index) {
        return new String[]{"top-left", "top-right", "bottom-right", "bottom-left"}[index];
    }
    
    /**
     * Movement with different speeds and timing
     */
    public void movementWithTiming() {
        log.info("=== Movement with Timing Control ===");
        
        Location start = new Location(200, 500);
        Location end = new Location(1720, 500);
        
        // Very fast movement
        log.info("Lightning fast movement to start position");
        MouseMoveOptions lightningMove = new MouseMoveOptions.Builder()
            .setMoveMouseDelay(0.05f)  // Almost instant
            .setPauseBeforeBegin(0.1)
            .setPauseAfterEnd(0.5)
            .build();
        action.perform(lightningMove, new ObjectCollection.Builder().withLocations(start).build());
        
        // Slow, smooth movement
        log.info("Slow, smooth movement to end position");
        MouseMoveOptions smoothMove = new MouseMoveOptions.Builder()
            .setMoveMouseDelay(1.0f)   // Very slow and smooth
            .setPauseBeforeBegin(0.5)
            .setPauseAfterEnd(1.0)
            .build();
        action.perform(smoothMove, new ObjectCollection.Builder().withLocations(end).build());
        
        // Variable speed demonstration
        demonstrateVariableSpeed();
    }
    
    private void demonstrateVariableSpeed() {
        log.info("\n=== Variable Speed Movement ===");
        
        Location[] waypoints = {
            new Location(300, 300),
            new Location(600, 400),
            new Location(900, 300),
            new Location(1200, 400),
            new Location(1500, 300)
        };
        
        for (int i = 0; i < waypoints.length; i++) {
            // Speed increases with each waypoint
            float speed = 0.1f + (i * 0.2f);
            
            MouseMoveOptions variableMove = new MouseMoveOptions.Builder()
                .setMoveMouseDelay(speed)
                .setPauseAfterEnd(0.3)
                .build();
            
            log.info("Moving to waypoint {} with speed {}", i + 1, speed);
            action.perform(variableMove, new ObjectCollection.Builder()
                .withLocations(waypoints[i])
                .build());
        }
    }
    
    /**
     * Random movement within a region using clicks
     */
    public void randomMovementInRegion() {
        log.info("=== Random Movement in Region ===");
        
        // Define a region for random movement
        Region movementArea = new Region(400, 200, 1120, 680);
        
        log.info("Clicking randomly within region: {}", movementArea);
        
        for (int i = 0; i < 5; i++) {
            // Generate random location within region
            int x = movementArea.getX() + random.nextInt(movementArea.getW());
            int y = movementArea.getY() + random.nextInt(movementArea.getH());
            Location randomLoc = new Location(x, y);
            
            log.debug("Clicking at random location {}: {}", i + 1, randomLoc);
            
            ClickOptions randomClick = new ClickOptions.Builder()
                .setPauseAfterEnd(0.3 + random.nextDouble() * 0.7) // 0.3-1.0 seconds
                .build();
                
            action.perform(randomClick, 
                new ObjectCollection.Builder().withLocations(randomLoc).build());
        }
    }
    
    /**
     * Movement along a path using sequential clicks
     */
    public void pathMovement() {
        log.info("=== Path Movement Example ===");
        
        Location[] path = {
            new Location(500, 300),
            new Location(900, 400),
            new Location(1300, 500),
            new Location(900, 600),
            new Location(500, 700)
        };
        
        log.info("Following path with {} points", path.length);
        
        for (int i = 0; i < path.length; i++) {
            log.debug("Step {}: Moving to {}", i + 1, path[i]);
            
            ClickOptions pathClick = new ClickOptions.Builder()
                .setPauseBeforeBegin(0.2)
                .setPauseAfterEnd(0.5)
                .build();
                
            action.perform(pathClick,
                new ObjectCollection.Builder().withLocations(path[i]).build());
        }
    }
    
    /**
     * Hover effects using MouseMoveOptions
     */
    public void hoverEffects() {
        log.info("=== Hover Effects with MouseMove ===");
        
        // Create sample UI elements
        StateImage menuButton = new StateImage.Builder()
            .setName("MenuButton")
            .addPatterns("menu_button")
            .build();
            
        StateImage tooltip = new StateImage.Builder()
            .setName("Tooltip")
            .addPatterns("tooltip")
            .build();
        
        // Move to button and hover
        log.info("Moving to menu button and hovering...");
        
        ObjectCollection menuCollection = new ObjectCollection.Builder()
            .withImages(menuButton)
            .build();
        
        // Smooth approach and hover
        MouseMoveOptions hoverMove = new MouseMoveOptions.Builder()
            .setMoveMouseDelay(0.4f)   // Smooth approach
            .setPauseBeforeBegin(0.2)
            .setPauseAfterEnd(2.0)     // Hold hover for 2 seconds
            .build();
        
        ActionResult moveResult = action.perform(hoverMove, menuCollection);
        
        if (moveResult.isSuccess()) {
            log.info("Hovering - tooltip should appear");
            
            // Check if tooltip appeared
            ActionResult tooltipResult = action.find(tooltip);
            if (tooltipResult.isSuccess()) {
                log.info("✓ Tooltip detected!");
            }
            
            // Move away to hide tooltip
            Location awayLocation = new Location(100, 100);
            MouseMoveOptions moveAway = new MouseMoveOptions.Builder()
                .setMoveMouseDelay(0.3f)
                .build();
            
            action.perform(moveAway, new ObjectCollection.Builder()
                .withLocations(awayLocation)
                .build());
            
            log.info("Moved away - tooltip should disappear");
        }
    }
    
    /**
     * Complex movement patterns
     */
    public void complexMovementPatterns() {
        log.info("=== Complex Movement Patterns ===");
        
        // Spiral pattern
        createSpiralPattern();
        
        // Figure-8 pattern
        createFigure8Pattern();
    }
    
    private void createSpiralPattern() {
        log.info("\nCreating spiral pattern...");
        
        int centerX = 960;
        int centerY = 540;
        double angle = 0;
        double radius = 10;
        
        MouseMoveOptions spiralMove = new MouseMoveOptions.Builder()
            .setMoveMouseDelay(0.3f)
            .build();
        
        for (int i = 0; i < 50; i++) {
            radius += 5;
            angle += 0.5;
            
            int x = centerX + (int)(radius * Math.cos(angle));
            int y = centerY + (int)(radius * Math.sin(angle));
            
            action.perform(spiralMove, new ObjectCollection.Builder()
                .withLocations(new Location(x, y))
                .build());
        }
    }
    
    private void createFigure8Pattern() {
        log.info("\nCreating figure-8 pattern...");
        
        int centerX = 960;
        int centerY = 540;
        int radius = 150;
        
        MouseMoveOptions figure8Move = new MouseMoveOptions.Builder()
            .setMoveMouseDelay(0.4f)
            .build();
        
        for (double t = 0; t <= 2 * Math.PI; t += 0.1) {
            int x = centerX + (int)(radius * Math.sin(t));
            int y = centerY + (int)(radius * Math.sin(t) * Math.cos(t));
            
            action.perform(figure8Move, new ObjectCollection.Builder()
                .withLocations(new Location(x, y))
                .build());
        }
    }
    
    /**
     * Run all basic movement examples
     */
    public void runAllExamples() {
        directMouseMovement();
        log.info("");
        
        movementWithTiming();
        log.info("");
        
        randomMovementInRegion();
        log.info("");
        
        pathMovement();
        log.info("");
        
        hoverEffects();
        log.info("");
        
        complexMovementPatterns();
    }
}