package com.example.movement.examples;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.mouse.ScrollOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Demonstrates scrolling operations in Brobot v1.1.0 using ScrollOptions.
 */
@Component
@Slf4j
public class ScrollingExample {
    
    private final Action action;
    
    // Example scrollable areas
    private StateImage documentArea;
    private StateImage listView;
    private StateImage webPage;
    private StateImage scrollBar;
    private Region scrollRegion;
    
    public ScrollingExample(Action action) {
        this.action = action;
        initializeObjects();
    }
    
    private void initializeObjects() {
        documentArea = new StateImage.Builder()
            .setName("DocumentArea")
            .addPatterns("document_area.png")
            .build();
            
        listView = new StateImage.Builder()
            .setName("ListView")
            .addPatterns("list_view.png")
            .build();
            
        webPage = new StateImage.Builder()
            .setName("WebPage")
            .addPatterns("web_page.png")
            .build();
            
        scrollBar = new StateImage.Builder()
            .setName("ScrollBar")
            .addPatterns("scroll_bar.png")
            .build();
            
        // Define a scrollable region
        scrollRegion = new Region(400, 200, 1120, 680);
    }
    
    /**
     * Basic scrolling using ScrollOptions
     */
    public void basicScrolling() {
        log.info("=== Basic Scrolling with ScrollOptions ===");
        
        // Move to scrollable area first
        ObjectCollection scrollArea = new ObjectCollection.Builder()
            .withRegions(scrollRegion)
            .build();
        
        action.perform(new MouseMoveOptions.Builder().build(), scrollArea);
        
        // Scroll down
        log.info("Scrolling down 5 steps");
        ScrollOptions scrollDown = new ScrollOptions.Builder()
            .setDirection(ScrollOptions.Direction.DOWN)
            .setScrollSteps(5)
            .setPauseAfterEnd(0.5)
            .build();
        
        ActionResult downResult = action.perform(scrollDown, scrollArea);
        if (downResult.isSuccess()) {
            log.info("✓ Scrolled down successfully");
        }
        
        // Pause
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll up
        log.info("Scrolling up 5 steps");
        ScrollOptions scrollUp = new ScrollOptions.Builder()
            .setDirection(ScrollOptions.Direction.UP)
            .setScrollSteps(5)
            .setPauseAfterEnd(0.5)
            .build();
        
        ActionResult upResult = action.perform(scrollUp, scrollArea);
        if (upResult.isSuccess()) {
            log.info("✓ Scrolled up successfully");
        }
    }
    
    /**
     * Smooth scrolling with different speeds
     */
    public void smoothScrolling() {
        log.info("=== Smooth Scrolling Example ===");
        
        ObjectCollection scrollArea = new ObjectCollection.Builder()
            .withRegions(scrollRegion)
            .build();
        
        // Slow, smooth scroll
        log.info("Slow smooth scrolling down");
        ScrollOptions slowScroll = new ScrollOptions.Builder()
            .setDirection(ScrollOptions.Direction.DOWN)
            .setScrollSteps(1)  // One step at a time
            .setPauseBeforeBegin(0.1)
            .setPauseAfterEnd(0.2)
            .build();
        
        for (int i = 0; i < 10; i++) {
            action.perform(slowScroll, scrollArea);
        }
        
        // Fast scrolling
        log.info("Fast scrolling up");
        ScrollOptions fastScroll = new ScrollOptions.Builder()
            .setDirection(ScrollOptions.Direction.UP)
            .setScrollSteps(10)  // Many steps at once
            .setPauseAfterEnd(0.1)
            .build();
        
        action.perform(fastScroll, scrollArea);
    }
    
    /**
     * Scroll to find element using ScrollOptions
     */
    public void scrollToFind() {
        log.info("=== Scroll to Find Element Example ===");
        
        // Target element we're looking for
        StateImage targetElement = new StateImage.Builder()
            .setName("TargetElement")
            .addPatterns("target_element")
            .build();
            
        log.info("Scrolling to find target element");
        
        ObjectCollection scrollArea = new ObjectCollection.Builder()
            .withRegions(scrollRegion)
            .build();
        
        // Move to scrollable area
        action.perform(new MouseMoveOptions.Builder().build(), scrollArea);
        
        int maxScrollAttempts = 10;
        boolean found = false;
        
        // Configure scroll for searching
        ScrollOptions searchScroll = new ScrollOptions.Builder()
            .setDirection(ScrollOptions.Direction.DOWN)
            .setScrollSteps(3)  // Scroll 3 steps at a time
            .setPauseAfterEnd(0.5)  // Pause to check for element
            .build();
        
        for (int i = 0; i < maxScrollAttempts && !found; i++) {
            log.debug("Scroll attempt {}", i + 1);
            
            // Check if element is visible
            PatternFindOptions quickFind = new PatternFindOptions.Builder()
                .setSimilarity(0.8)
                .build();
                
            ObjectCollection targetCollection = new ObjectCollection.Builder()
                .withImages(targetElement)
                .build();
                
            ActionResult findResult = action.perform(quickFind, targetCollection);
            
            if (findResult.isSuccess()) {
                log.info("✓ Found target element after {} scrolls", i);
                found = true;
                
                // Optional: Click on the found element
                Match bestMatch = findResult.getBestMatch().get();
                ObjectCollection clickTarget = new ObjectCollection.Builder()
                    .withLocations(new Location(bestMatch.getRegion()))
                    .build();
                action.perform(new ClickOptions.Builder().build(), clickTarget);
            } else {
                // Scroll down to continue searching
                action.perform(searchScroll, scrollArea);
            }
        }
        
        if (!found) {
            log.warn("Target element not found after {} scroll attempts", maxScrollAttempts);
        }
    }
    
    /**
     * Scroll using scroll bar
     */
    public void scrollBarScrolling() {
        log.info("=== Scroll Bar Scrolling Example ===");
        
        // Find the scroll bar
        ActionResult scrollBarResult = action.find(scrollBar);
        
        if (scrollBarResult.isSuccess()) {
            scrollBarResult.getBestMatch().ifPresent(match -> {
                Region scrollBarRegion = match.getRegion();
                
                // Click at different positions on the scroll bar
                // Top of scroll bar
                log.info("Scrolling to top via scroll bar");
                Location scrollRegionCenter = new Location(scrollBarRegion);
                Location topScroll = new Location(
                    scrollRegionCenter.getCalculatedX(),
                    scrollBarRegion.getY() + 20
                );
                ObjectCollection topScrollTarget = new ObjectCollection.Builder()
                    .withLocations(topScroll)
                    .build();
                action.perform(new ClickOptions.Builder().build(), topScrollTarget);
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Middle of scroll bar
                log.info("Scrolling to middle via scroll bar");
                ObjectCollection centerTarget = new ObjectCollection.Builder()
                    .withLocations(new Location(scrollBarRegion))
                    .build();
                action.perform(new ClickOptions.Builder().build(), centerTarget);
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Bottom of scroll bar
                log.info("Scrolling to bottom via scroll bar");
                Location scrollRegionCenter2 = new Location(scrollBarRegion);
                Location bottomScroll = new Location(
                    scrollRegionCenter2.getCalculatedX(),
                    scrollBarRegion.getY() + scrollBarRegion.getH() - 20
                );
                ObjectCollection bottomScrollTarget = new ObjectCollection.Builder()
                    .withLocations(bottomScroll)
                    .build();
                action.perform(new ClickOptions.Builder().build(), bottomScrollTarget);
            });
        } else {
            log.warn("Scroll bar not found");
        }
    }
    
    /**
     * Scroll in specific areas
     */
    public void targetedScrolling() {
        log.info("=== Targeted Scrolling Example ===");
        
        // Find and scroll in document area
        log.info("Scrolling in document area");
        ActionResult findDoc = action.perform(
            new PatternFindOptions.Builder().build(), 
            new ObjectCollection.Builder().withImages(documentArea).build()
        );
        
        if (findDoc.isSuccess()) {
            findDoc.getBestMatch().ifPresent(match -> {
                Region docRegion = match.getRegion();
                
                // Click in document center
                ObjectCollection docTarget = new ObjectCollection.Builder()
                    .withLocations(new Location(docRegion))
                    .build();
                action.perform(new ClickOptions.Builder().build(), docTarget);
                
                // Scroll down in document
                log.info("Scrolling down in document");
                for (int i = 0; i < 5; i++) {
                    action.type(new ObjectCollection.Builder()
                        .withStrings("{PGDOWN}")
                        .build());
                    
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                log.info("✓ Scrolled in document area");
            });
        }
        
        // Find and scroll in list view
        log.info("Scrolling in list view");
        ActionResult findList = action.perform(
            new PatternFindOptions.Builder().build(), 
            new ObjectCollection.Builder().withImages(listView).build()
        );
        
        if (findList.isSuccess()) {
            findList.getBestMatch().ifPresent(match -> {
                // Click in list view
                ObjectCollection matchTarget = new ObjectCollection.Builder()
                    .withLocations(new Location(match.getRegion()))
                    .build();
                action.perform(new ClickOptions.Builder().build(), matchTarget);
                
                // Scroll using arrow keys for precise control
                log.info("Scrolling down in list");
                for (int i = 0; i < 10; i++) {
                    action.type(new ObjectCollection.Builder()
                        .withStrings("{DOWN}")
                        .build());
                    
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                log.info("✓ Scrolled in list view");
            });
        }
    }
    
    /**
     * Precise scrolling in different UI elements
     */
    public void preciseScrolling() {
        log.info("=== Precise Scrolling Example ===");
        
        // Find document area and scroll within it
        ObjectCollection docCollection = new ObjectCollection.Builder()
            .withImages(documentArea)
            .build();
        
        ActionResult docResult = action.find(docCollection);
        if (docResult.isSuccess()) {
            log.info("Scrolling in document area");
            
            // Create collection for the found region
            Region docRegion = docResult.getBestMatch().get().getRegion();
            ObjectCollection docScrollArea = new ObjectCollection.Builder()
                .withRegions(docRegion)
                .build();
            
            // Page-by-page scrolling
            ScrollOptions pageScroll = new ScrollOptions.Builder()
                .setDirection(ScrollOptions.Direction.DOWN)
                .setScrollSteps(10)  // Approximate page
                .setPauseAfterEnd(1.0)
                .build();
            
            for (int i = 0; i < 3; i++) {
                log.info("Scrolling to page {}", i + 2);
                action.perform(pageScroll, docScrollArea);
            }
        }
        
        // Find list view and scroll precisely
        ObjectCollection listCollection = new ObjectCollection.Builder()
            .withImages(listView)
            .build();
        
        ActionResult listResult = action.find(listCollection);
        if (listResult.isSuccess()) {
            log.info("Scrolling in list view");
            
            Region listRegion = listResult.getBestMatch().get().getRegion();
            ObjectCollection listScrollArea = new ObjectCollection.Builder()
                .withRegions(listRegion)
                .build();
            
            // Item-by-item scrolling
            ScrollOptions itemScroll = new ScrollOptions.Builder()
                .setDirection(ScrollOptions.Direction.DOWN)
                .setScrollSteps(1)  // One item at a time
                .setPauseAfterEnd(0.3)
                .build();
            
            for (int i = 0; i < 5; i++) {
                log.info("Scrolling to item {}", i + 1);
                action.perform(itemScroll, listScrollArea);
            }
        }
    }
    
    /**
     * Advanced scrolling patterns
     */
    public void advancedScrollingPatterns() {
        log.info("=== Advanced Scrolling Patterns ===");
        
        ObjectCollection scrollArea = new ObjectCollection.Builder()
            .withRegions(scrollRegion)
            .build();
        
        // Momentum scrolling simulation
        log.info("Simulating momentum scrolling");
        int[] steps = {5, 4, 3, 2, 1};  // Decreasing steps
        
        for (int step : steps) {
            ScrollOptions momentumScroll = new ScrollOptions.Builder()
                .setDirection(ScrollOptions.Direction.DOWN)
                .setScrollSteps(step)
                .setPauseAfterEnd(0.1 * step)  // Longer pause for larger scrolls
                .build();
            
            action.perform(momentumScroll, scrollArea);
        }
        
        // Elastic scrolling simulation
        log.info("Simulating elastic scrolling");
        
        // Scroll past the end
        ScrollOptions overScroll = new ScrollOptions.Builder()
            .setDirection(ScrollOptions.Direction.DOWN)
            .setScrollSteps(20)  // Large scroll
            .setPauseAfterEnd(0.2)
            .build();
        
        action.perform(overScroll, scrollArea);
        
        // Bounce back
        ScrollOptions bounceBack = new ScrollOptions.Builder()
            .setDirection(ScrollOptions.Direction.UP)
            .setScrollSteps(3)
            .setPauseAfterEnd(0.5)
            .build();
        
        action.perform(bounceBack, scrollArea);
    }
    
    /**
     * Run all scrolling examples
     */
    public void runAllExamples() {
        basicScrolling();
        log.info("");
        
        smoothScrolling();
        log.info("");
        
        scrollToFind();
        log.info("");
        
        scrollBarScrolling();
        log.info("");
        
        targetedScrolling();
        log.info("");
        
        preciseScrolling();
        log.info("");
        
        advancedScrollingPatterns();
    }
}