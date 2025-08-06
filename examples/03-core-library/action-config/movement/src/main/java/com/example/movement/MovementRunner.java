package com.example.movement;

import com.example.movement.examples.BasicMovementExample;
import com.example.movement.examples.DragDropExample;
import com.example.movement.examples.ScrollingExample;
import com.example.movement.examples.AdvancedMovementExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Runs all movement examples.
 */
@Component
public class MovementRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(MovementRunner.class);
    
    private final BasicMovementExample basicExample;
    private final DragDropExample dragDropExample;
    private final ScrollingExample scrollingExample;
    private final AdvancedMovementExample advancedExample;
    
    public MovementRunner(BasicMovementExample basicExample,
                         DragDropExample dragDropExample,
                         ScrollingExample scrollingExample,
                         AdvancedMovementExample advancedExample) {
        this.basicExample = basicExample;
        this.dragDropExample = dragDropExample;
        this.scrollingExample = scrollingExample;
        this.advancedExample = advancedExample;
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("================================================");
        log.info("Brobot Movement Examples");
        log.info("================================================");
        log.info("");
        
        // Basic movement
        log.info(">>> Running Basic Movement Examples <<<");
        basicExample.runAllExamples();
        log.info("");
        
        Thread.sleep(1000);
        
        // Drag and drop
        log.info(">>> Running Drag and Drop Examples <<<");
        dragDropExample.runAllExamples();
        log.info("");
        
        Thread.sleep(1000);
        
        // Scrolling
        log.info(">>> Running Scrolling Examples <<<");
        scrollingExample.runAllExamples();
        log.info("");
        
        Thread.sleep(1000);
        
        // Advanced movements
        log.info(">>> Running Advanced Movement Examples <<<");
        advancedExample.runAllExamples();
        log.info("");
        
        log.info("================================================");
        log.info("All movement examples completed!");
        log.info("================================================");
        
        log.info("");
        log.info("Key takeaways:");
        log.info("✓ MouseMoveOptions for precise mouse positioning");
        log.info("✓ DragOptions for drag and drop operations");
        log.info("✓ ScrollOptions for scrolling control");
        log.info("✓ Custom drag sequences with MouseDown/Move/Up");
        log.info("✓ Movement patterns: SMOOTH, LINEAR, SIGMOID, RANDOM");
        log.info("✓ Timing control with pauses and movement duration");
        log.info("✓ Complex gestures and workflows possible");
        
        log.info("");
        log.info("Movement is essential for UI automation!");
    }
}