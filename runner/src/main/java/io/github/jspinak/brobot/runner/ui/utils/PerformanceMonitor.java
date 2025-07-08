package io.github.jspinak.brobot.runner.ui.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceMonitor {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    
    private static long startTime;
    private static String currentOperation;
    
    public static void start(String operation) {
        startTime = System.currentTimeMillis();
        currentOperation = operation;
        logger.info("PERF: Starting - {}", operation);
    }
    
    public static void checkpoint(String checkpoint) {
        long elapsed = System.currentTimeMillis() - startTime;
        logger.info("PERF: {} - {} ({}ms)", currentOperation, checkpoint, elapsed);
    }
    
    public static void end() {
        long elapsed = System.currentTimeMillis() - startTime;
        logger.info("PERF: Completed - {} (total: {}ms)", currentOperation, elapsed);
    }
}