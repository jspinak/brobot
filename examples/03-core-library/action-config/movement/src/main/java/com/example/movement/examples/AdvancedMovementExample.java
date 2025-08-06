package com.example.movement.examples;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.ConditionalActionChain;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates advanced movement patterns and combinations in Brobot v1.1.0.
 */
@Component
public class AdvancedMovementExample {
    private static final Logger log = LoggerFactory.getLogger(AdvancedMovementExample.class);
    
    private final Action action;
    
    // Example UI elements
    private StateImage slider;
    private StateImage colorPicker;
    private StateImage canvas;
    private StateImage toolbar;
    
    public AdvancedMovementExample(Action action) {
        this.action = action;
        initializeObjects();
    }
    
    private void initializeObjects() {
        slider = new StateImage.Builder()
            .setName("SliderHandle")
            .addPatterns("slider_handle.png")
            .build();
            
        colorPicker = new StateImage.Builder()
            .setName("ColorPicker")
            .addPatterns("color_picker.png")
            .build();
            
        canvas = new StateImage.Builder()
            .setName("Canvas")
            .addPatterns("canvas_area.png")
            .build();
            
        toolbar = new StateImage.Builder()
            .setName("Toolbar")
            .addPatterns("toolbar.png")
            .build();
    }
    
    /**
     * Complex gesture: Circle drawing using drag operations
     */
    public void drawCircleGesture() {
        log.info("=== Circle Drawing Gesture Example ===");
        
        // Find canvas
        ObjectCollection canvasCollection = new ObjectCollection.Builder()
            .withImages(canvas)
            .build();
            
        ActionResult findCanvas = action.perform(
            new PatternFindOptions.Builder().build(), 
            canvasCollection
        );
        
        if (findCanvas.isSuccess()) {
            findCanvas.getBestMatch().ifPresent(match -> {
                Region canvasRegion = match.getRegion();
                Location center = new Location(canvasRegion);
                int radius = 100;
                
                log.info("Drawing circle at {} with radius {}", center, radius);
                
                // Calculate circle points
                List<Location> circlePoints = calculateCirclePoints(center, radius, 12);
                
                // Draw circle by dragging between consecutive points
                for (int i = 0; i < circlePoints.size() - 1; i++) {
                    Location from = circlePoints.get(i);
                    Location to = circlePoints.get(i + 1);
                    
                    DragOptions drawSegment = new DragOptions.Builder()
                        .setDelayBetweenMouseDownAndMove(0.1)  // Quick drag
                        .setDelayAfterDrag(0.05)
                        .build();
                        
                    ObjectCollection fromLoc = new ObjectCollection.Builder()
                        .withLocations(from)
                        .build();
                        
                    ObjectCollection toLoc = new ObjectCollection.Builder()
                        .withLocations(to)
                        .build();
                        
                    action.perform(drawSegment, fromLoc, toLoc);
                }
                
                // Complete circle
                DragOptions completeCircle = new DragOptions.Builder()
                    .setDelayBetweenMouseDownAndMove(0.1)
                    .setDelayAfterDrag(0.05)
                    .build();
                    
                action.perform(completeCircle, 
                    new ObjectCollection.Builder().withLocations(circlePoints.get(circlePoints.size() - 1)).build(),
                    new ObjectCollection.Builder().withLocations(circlePoints.get(0)).build()
                );
                
                log.info("✓ Circle drawing completed");
            });
        }
    }
    
    private List<Location> calculateCirclePoints(Location center, int radius, int numPoints) {
        List<Location> points = new ArrayList<>();
        double angleStep = 2 * Math.PI / numPoints;
        
        for (int i = 0; i <= numPoints; i++) {
            double angle = i * angleStep;
            int x = (int) (center.getCalculatedX() + radius * Math.cos(angle));
            int y = (int) (center.getCalculatedY() + radius * Math.sin(angle));
            points.add(new Location(x, y));
        }
        
        return points;
    }
    
    /**
     * Slider manipulation using drag
     */
    public void manipulateSlider() {
        log.info("=== Slider Manipulation Example ===");
        
        // Find slider handle
        ObjectCollection sliderCollection = new ObjectCollection.Builder()
            .withImages(slider)
            .build();
            
        ActionResult findSlider = action.perform(
            new PatternFindOptions.Builder()
                .setSimilarity(0.9)
                .build(), 
            sliderCollection
        );
        
        if (findSlider.isSuccess()) {
            findSlider.getBestMatch().ifPresent(match -> {
                Location sliderPos = new Location(match.getRegion());
                
                log.info("Moving slider from {} to different positions", sliderPos);
                
                // Drag slider to different positions
                int[] offsets = {-100, -50, 0, 50, 100};
                
                for (int offset : offsets) {
                    Location targetPos = new Location(sliderPos.getCalculatedX() + offset, sliderPos.getCalculatedY());
                    
                    log.debug("Moving slider to offset: {}", offset);
                    
                    // Drag to specific position
                    DragOptions sliderDrag = new DragOptions.Builder()
                        .setDelayBetweenMouseDownAndMove(0.5)
                        .setDelayAfterDrag(0.3)
                        .build();
                        
                    ObjectCollection targetCollection = new ObjectCollection.Builder()
                        .withLocations(targetPos)
                        .build();
                        
                    action.perform(sliderDrag, sliderCollection, targetCollection);
                    
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                log.info("✓ Slider manipulation completed");
            });
        }
    }
    
    /**
     * Combined movement workflow using ConditionalActionChain
     */
    public void combinedMovementWorkflow() {
        log.info("=== Combined Movement Workflow Example ===");
        
        // Workflow: Select color, draw on canvas, save
        
        // Step 1: Find and click color picker
        log.info("Step 1: Selecting color");
        ConditionalActionChain selectColor = ConditionalActionChain
            .find(new PatternFindOptions.Builder()
                .setSimilarity(0.8)
                .build())
            .ifFound(new ClickOptions.Builder().build())
            .ifNotFoundLog("Color picker not found");
            
        selectColor.perform(action, new ObjectCollection.Builder()
            .withImages(colorPicker)
            .build());
        
        // Step 2: Draw on canvas
        log.info("Step 2: Drawing on canvas");
        ActionResult canvasResult = action.find(canvas);
        
        if (canvasResult.isSuccess()) {
            canvasResult.getBestMatch().ifPresent(match -> {
                Region drawArea = match.getRegion();
                drawPattern(drawArea);
            });
        }
        
        // Step 3: Save using keyboard shortcut
        log.info("Step 3: Saving work");
        action.type(new ObjectCollection.Builder()
            .withStrings("^s")  // Ctrl+S
            .build());
        
        log.info("✓ Combined workflow completed");
    }
    
    private void drawPattern(Region drawArea) {
        Location start = new Location(
            drawArea.getX() + 50, 
            drawArea.getY() + 50
        );
        
        // Simple zigzag pattern using drags
        Location[] pattern = {
            start,
            new Location(start.getCalculatedX() + 100, start.getCalculatedY() + 50),
            new Location(start.getCalculatedX() + 50, start.getCalculatedY() + 100),
            new Location(start.getCalculatedX() + 150, start.getCalculatedY() + 150)
        };
        
        // Draw pattern with drags
        for (int i = 0; i < pattern.length - 1; i++) {
            DragOptions drawLine = new DragOptions.Builder()
                .setDelayBetweenMouseDownAndMove(0.2)
                .setDelayAfterDrag(0.1)
                .build();
                
            action.perform(drawLine,
                new ObjectCollection.Builder().withLocations(pattern[i]).build(),
                new ObjectCollection.Builder().withLocations(pattern[i + 1]).build()
            );
        }
    }
    
    /**
     * Hover effects demonstration
     */
    public void hoverEffects() {
        log.info("=== Hover Effects Example ===");
        
        // Find toolbar items
        ConditionalActionChain findToolbar = ConditionalActionChain
            .find(new PatternFindOptions.Builder().build())
            .ifFoundDo(result -> {
                result.getBestMatch().ifPresent(match -> {
                    Region toolbarRegion = match.getRegion();
                    
                    log.info("Hovering over toolbar items");
                    
                    // Calculate item positions (assuming horizontal toolbar)
                    int itemCount = 5;
                    int itemWidth = toolbarRegion.getW() / itemCount;
                    
                    for (int i = 0; i < itemCount; i++) {
                        Location itemCenter = new Location(
                            toolbarRegion.getX() + (i * itemWidth) + (itemWidth / 2),
                            toolbarRegion.getY() + (toolbarRegion.getH() / 2)
                        );
                        
                        log.debug("Hovering over item {}", i + 1);
                        
                        // Click with pause to simulate hover
                        ClickOptions hoverClick = new ClickOptions.Builder()
                            .setPauseBeforeBegin(0.3)
                            .setPauseAfterEnd(1.0)  // Hover for 1 second
                            .setNumberOfClicks(0)   // Don't actually click
                            .build();
                            
                        action.perform(hoverClick,
                            new ObjectCollection.Builder()
                                .withLocations(itemCenter)
                                .build());
                    }
                    
                    log.info("✓ Hover effects completed");
                });
            })
            .ifNotFoundLog("Toolbar not found");
            
        findToolbar.perform(action, new ObjectCollection.Builder()
            .withImages(toolbar)
            .build());
    }
    
    /**
     * Gesture movements using drag patterns
     */
    public void gestureMovements() {
        log.info("=== Gesture Movements Example ===");
        
        Location center = new Location(960, 540);
        
        // Swipe right gesture
        log.info("Performing swipe right");
        performSwipe(center, SwipeDirection.RIGHT, 200, 0.3);
        
        // Swipe left gesture
        log.info("Performing swipe left");
        performSwipe(center, SwipeDirection.LEFT, 200, 0.3);
        
        // Diagonal swipe
        log.info("Performing diagonal swipe");
        performDiagonalSwipe(center, 150, 0.4);
        
        log.info("✓ Gesture movements completed");
    }
    
    private enum SwipeDirection {
        LEFT, RIGHT, UP, DOWN
    }
    
    private void performSwipe(Location start, SwipeDirection direction, int distance, double duration) {
        Location end;
        switch (direction) {
            case RIGHT:
                end = new Location(start.getCalculatedX() + distance, start.getCalculatedY());
                break;
            case LEFT:
                end = new Location(start.getCalculatedX() - distance, start.getCalculatedY());
                break;
            case UP:
                end = new Location(start.getCalculatedX(), start.getCalculatedY() - distance);
                break;
            case DOWN:
                end = new Location(start.getCalculatedX(), start.getCalculatedY() + distance);
                break;
            default:
                return;
        }
        
        // Perform swipe as a drag
        DragOptions swipe = new DragOptions.Builder()
            .setDelayBetweenMouseDownAndMove(duration)
            .setDelayAfterDrag(0.2)
            .build();
            
        action.perform(swipe,
            new ObjectCollection.Builder().withLocations(start).build(),
            new ObjectCollection.Builder().withLocations(end).build()
        );
    }
    
    private void performDiagonalSwipe(Location start, int distance, double duration) {
        Location end = new Location(
            start.getCalculatedX() + distance,
            start.getCalculatedY() - distance
        );
        
        DragOptions diagonalSwipe = new DragOptions.Builder()
            .setDelayBetweenMouseDownAndMove(duration)
            .setDelayAfterDrag(0.2)
            .build();
            
        action.perform(diagonalSwipe,
            new ObjectCollection.Builder().withLocations(start).build(),
            new ObjectCollection.Builder().withLocations(end).build()
        );
    }
    
    /**
     * Color-based movement targeting
     */
    public void colorBasedMovement() {
        log.info("=== Color-Based Movement Example ===");
        
        // Find all red buttons using color detection
        ColorFindOptions findRed = new ColorFindOptions.Builder()
            .setColorStrategy(ColorFindOptions.Color.MU)
            .setSimilarity(0.85)
            .build();
            
        ObjectCollection colorTargets = new ObjectCollection.Builder()
            .withImages(new StateImage.Builder()
                .setName("RedButton")
                .addPatterns("red_button_sample.png")
                .build())
            .build();
            
        ActionResult colorResult = action.perform(findRed, colorTargets);
        
        if (colorResult.isSuccess()) {
            log.info("Found {} red elements", colorResult.getMatchList().size());
            
            // Click on each red element
            colorResult.getMatchList().forEach(match -> {
                Location matchLocation = new Location(match.getRegion());
                log.debug("Clicking red element at {}", matchLocation);
                ObjectCollection clickTarget = new ObjectCollection.Builder()
                    .withLocations(matchLocation)
                    .build();
                action.perform(new ClickOptions.Builder().build(), clickTarget);
                
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }
    
    /**
     * Run all advanced movement examples
     */
    public void runAllExamples() {
        drawCircleGesture();
        log.info("");
        
        manipulateSlider();
        log.info("");
        
        combinedMovementWorkflow();
        log.info("");
        
        hoverEffects();
        log.info("");
        
        gestureMovements();
        log.info("");
        
        colorBasedMovement();
    }
}