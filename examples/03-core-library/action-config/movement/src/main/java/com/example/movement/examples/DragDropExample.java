package com.example.movement.examples;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Demonstrates drag and drop operations in Brobot v1.1.0.
 */
@Component
public class DragDropExample {
    private static final Logger log = LoggerFactory.getLogger(DragDropExample.class);
    
    private final Action action;
    
    // Example drag sources and targets
    private StateImage fileIcon;
    private StateImage folderIcon;
    private StateImage trashIcon;
    private StateImage dragHandle;
    
    public DragDropExample(Action action) {
        this.action = action;
        initializeObjects();
    }
    
    private void initializeObjects() {
        fileIcon = new StateImage.Builder()
            .setName("FileIcon")
            .addPatterns("file_icon.png")
            .build();
            
        folderIcon = new StateImage.Builder()
            .setName("FolderIcon")
            .addPatterns("folder_icon.png")
            .build();
            
        trashIcon = new StateImage.Builder()
            .setName("TrashIcon")
            .addPatterns("trash_icon.png")
            .build();
            
        dragHandle = new StateImage.Builder()
            .setName("DragHandle")
            .addPatterns("drag_handle.png")
            .build();
    }
    
    /**
     * Simple drag and drop operation
     */
    public void simpleDragDrop() {
        log.info("=== Simple Drag and Drop Example ===");
        
        // Configure drag operation
        DragOptions dragToFolder = new DragOptions.Builder()
            .setDelayBetweenMouseDownAndMove(0.5)  // Hold for 0.5 seconds before dragging
            .setDelayAfterDrag(0.5)  // Pause after drag
            .build();
            
        log.info("Dragging file to folder");
        
        // Create object collections for source and target
        ObjectCollection source = new ObjectCollection.Builder()
            .withImages(fileIcon)
            .build();
            
        ObjectCollection target = new ObjectCollection.Builder()
            .withImages(folderIcon)
            .build();
            
        ActionResult result = action.perform(dragToFolder, source, target);
        
        if (result.isSuccess()) {
            log.info("✓ Drag and drop completed successfully");
        } else {
            log.error("✗ Drag and drop failed");
        }
    }
    
    /**
     * Drag with different delays
     */
    public void dragWithDifferentDelays() {
        log.info("=== Drag Delay Example ===");
        
        double[] delays = {0.2, 0.5, 1.0};
        String[] delayNames = {"fast", "normal", "slow"};
        
        for (int i = 0; i < delays.length; i++) {
            log.info("Dragging with {} delay ({} seconds)", delayNames[i], delays[i]);
            
            DragOptions dragWithDelay = new DragOptions.Builder()
                .setDelayBetweenMouseDownAndMove(delays[i])
                .setDelayAfterDrag(0.3)
                .build();
                
            ObjectCollection source = new ObjectCollection.Builder()
                .withImages(fileIcon)
                .build();
                
            ObjectCollection target = new ObjectCollection.Builder()
                .withImages(trashIcon)
                .build();
                
            action.perform(dragWithDelay, source, target);
            
            // Pause between drags
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Drag from image to location
     */
    public void dragToLocation() {
        log.info("=== Drag to Location Example ===");
        
        // Find the drag handle
        PatternFindOptions findHandle = new PatternFindOptions.Builder()
            .setSimilarity(0.9)
            .build();
            
        ObjectCollection handleCollection = new ObjectCollection.Builder()
            .withImages(dragHandle)
            .build();
            
        ActionResult findResult = action.perform(findHandle, handleCollection);
        
        if (findResult.isSuccess()) {
            findResult.getBestMatch().ifPresent(match -> {
                Location startLoc = new Location(match.getRegion());
                Location endLoc = new Location(startLoc.getCalculatedX() + 300, startLoc.getCalculatedY() + 200);
                
                log.info("Performing drag from {} to {}", startLoc, endLoc);
                
                // Use DragOptions to drag from image to location
                DragOptions dragToLocation = new DragOptions.Builder()
                    .setDelayBetweenMouseDownAndMove(0.5)
                    .setDelayAfterDrag(0.5)
                    .build();
                
                ObjectCollection targetLocation = new ObjectCollection.Builder()
                    .withLocations(endLoc)
                    .build();
                    
                ActionResult dragResult = action.perform(dragToLocation, handleCollection, targetLocation);
                
                if (dragResult.isSuccess()) {
                    log.info("✓ Drag to location completed");
                } else {
                    log.error("✗ Drag to location failed");
                }
            });
        } else {
            log.error("Could not find drag handle");
        }
    }
    
    /**
     * Drag between regions
     */
    public void dragBetweenRegions() {
        log.info("=== Drag Between Regions Example ===");
        
        // Define source and target regions
        Region sourceRegion = new Region(100, 200, 200, 200);
        Region targetRegion = new Region(1200, 400, 300, 300);
        
        log.info("Dragging from region {} to region {}", sourceRegion, targetRegion);
        
        // Use region centers for drag
        Location sourceCenter = new Location(sourceRegion);
        Location targetCenter = new Location(targetRegion);
        
        DragOptions regionDrag = new DragOptions.Builder()
            .setDelayBetweenMouseDownAndMove(0.8)
            .setDelayAfterDrag(0.5)
            .build();
            
        ObjectCollection sourceCollection = new ObjectCollection.Builder()
            .withLocations(sourceCenter)
            .build();
            
        ObjectCollection targetCollection = new ObjectCollection.Builder()
            .withLocations(targetCenter)
            .build();
            
        ActionResult result = action.perform(regionDrag, sourceCollection, targetCollection);
        
        if (result.isSuccess()) {
            log.info("✓ Region drag completed");
        } else {
            log.error("✗ Region drag failed");
        }
    }
    
    /**
     * Complex drag workflow
     */
    public void complexDragWorkflow() {
        log.info("=== Complex Drag Workflow ===");
        
        // Workflow: Drag multiple files to folder, then folder to trash
        StateImage[] files = new StateImage[3];
        for (int i = 0; i < 3; i++) {
            files[i] = new StateImage.Builder()
                .setName("File" + (i + 1))
                .addPatterns("file" + (i + 1) + ".png")
                .build();
        }
        
        // Step 1: Drag each file to folder
        log.info("Step 1: Organizing files into folder");
        for (StateImage file : files) {
            DragOptions dragToFolder = new DragOptions.Builder()
                .setDelayBetweenMouseDownAndMove(0.3)
                .setDelayAfterDrag(0.3)
                .build();
                
            ObjectCollection fileCollection = new ObjectCollection.Builder()
                .withImages(file)
                .build();
                
            ObjectCollection folderCollection = new ObjectCollection.Builder()
                .withImages(folderIcon)
                .build();
                
            ActionResult result = action.perform(dragToFolder, fileCollection, folderCollection);
            
            if (result.isSuccess()) {
                log.debug("✓ {} dragged to folder", file.getName());
            } else {
                log.warn("✗ Failed to drag {} to folder", file.getName());
            }
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Step 2: Drag folder to trash
        log.info("Step 2: Moving folder to trash");
        DragOptions dragToTrash = new DragOptions.Builder()
            .setDelayBetweenMouseDownAndMove(1.0) // Longer hold for important operation
            .setDelayAfterDrag(0.5)
            .build();
            
        ObjectCollection folderSource = new ObjectCollection.Builder()
            .withImages(folderIcon)
            .build();
            
        ObjectCollection trashTarget = new ObjectCollection.Builder()
            .withImages(trashIcon)
            .build();
            
        ActionResult trashResult = action.perform(dragToTrash, folderSource, trashTarget);
        
        if (trashResult.isSuccess()) {
            log.info("✓ Folder moved to trash");
        } else {
            log.error("✗ Failed to move folder to trash");
        }
    }
    
    /**
     * Run all drag and drop examples
     */
    public void runAllExamples() {
        simpleDragDrop();
        log.info("");
        
        dragWithDifferentDelays();
        log.info("");
        
        dragToLocation();
        log.info("");
        
        dragBetweenRegions();
        log.info("");
        
        complexDragWorkflow();
    }
}