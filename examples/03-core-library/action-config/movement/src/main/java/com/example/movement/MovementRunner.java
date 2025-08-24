package com.example.movement;

import com.example.movement.examples.SimpleMotionExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Runs motion detection examples using the real Brobot API.
 */
@Component
public class MovementRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(MovementRunner.class);
    
    private final SimpleMotionExample simpleExample;
    
    public MovementRunner(SimpleMotionExample simpleExample) {
        this.simpleExample = simpleExample;
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("======================================");
        log.info("Motion Detection Examples");
        log.info("======================================");
        log.info("");
        
        // Run simple motion examples
        log.info(">>> Running Motion Detection Examples <<<");
        simpleExample.runAllExamples();
        
        log.info("======================================");
        log.info("All motion examples completed!");
        log.info("======================================");
    }
}