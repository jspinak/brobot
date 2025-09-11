package com.example.movement.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.motion.MotionFindOptions;

/**
 * Simple working motion detection example using the real Brobot API.
 *
 * <p>The real MotionFindOptions only supports maxMovement parameter. Other options from the
 * documentation don't exist in the actual API.
 */
@Component
public class SimpleMotionExample {
    private static final Logger log = LoggerFactory.getLogger(SimpleMotionExample.class);

    @Autowired private Action action;

    /**
     * Basic Motion Detection using the real API.
     *
     * <p>In the real API, MotionFindOptions only has maxMovement setting. Features like minArea,
     * maxArea, similarity, etc. don't exist.
     */
    public void basicMotionDetection() {
        log.info("=== Basic Motion Detection with Real API ===");

        // Create motion find options - only maxMovement is available
        MotionFindOptions motionOptions =
                new MotionFindOptions.Builder()
                        .setMaxMovement(200) // Maximum pixels an object can move between frames
                        .setPauseBeforeBegin(1.0) // Wait before starting (inherited from base)
                        .build();

        // Create empty object collection (motion detection doesn't need specific objects)
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();

        // Perform motion detection
        ActionResult result = action.perform(motionOptions, objectCollection);

        if (result.isSuccess()) {
            log.info("Motion detection successful!");
            log.info("Found {} moving objects", result.getMatchList().size());

            // Process motion results
            result.getMatchList()
                    .forEach(
                            match -> {
                                log.info("  Movement detected at: {}", match.getTarget());
                            });
        } else {
            log.warn("No motion detected");
        }
    }

    /**
     * Motion Tracking Example - simplified version.
     *
     * <p>Shows how to use motion detection in practice, even with the limited API options.
     */
    public void trackMovement() {
        log.info("=== Movement Tracking Example ===");

        // Configure for fast movement tracking
        MotionFindOptions fastTracking =
                new MotionFindOptions.Builder()
                        .setMaxMovement(300) // Allow faster movement
                        .setPauseBeforeBegin(0.5)
                        .setMaxMatchesToActOn(10) // Track up to 10 objects
                        .build();

        ObjectCollection objects = new ObjectCollection.Builder().build();

        // Track movement
        ActionResult result = action.perform(fastTracking, objects);

        if (result.isSuccess()) {
            log.info("Tracked {} moving objects", result.getMatchList().size());

            // Analyze movement patterns
            if (result.getSceneAnalysisCollection() != null) {
                log.info("Scene analysis available");
                // In real implementation, would analyze scene data
            }
        } else {
            log.info("No movement detected in tracking");
        }
    }

    /** Demonstrates all motion detection examples. */
    public void runAllExamples() {
        log.info("Running motion detection examples with real Brobot API\n");

        basicMotionDetection();
        log.info("");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        trackMovement();
        log.info("");

        log.info("Motion detection examples completed!");
        log.info("");
        log.info("Note: The real MotionFindOptions API only supports:");
        log.info("  - maxMovement: Maximum pixel distance objects can move");
        log.info("  - Standard timing options (pauseBeforeBegin, pauseBetweenActions)");
        log.info("  - maxMatchesToActOn: Maximum number of objects to track");
        log.info("");
        log.info("Features from documentation that don't exist:");
        log.info("  ✗ minArea/maxArea for object size filtering");
        log.info("  ✗ similarity threshold for object matching");
        log.info("  ✗ illustrate option for visual debugging");
    }
}
