---
sidebar_position: 11
title: 'Advanced Illustration System'
---

# Advanced Illustration System

Brobot's advanced illustration system provides intelligent, context-aware visual documentation of automation actions with performance optimization and quality-based filtering.

## Overview

The enhanced illustration system includes:

- **Context-aware illustration decisions** based on action history and system state
- **Performance optimization** with adaptive sampling and batching
- **Quality-based filtering** to focus on meaningful visualizations
- **Granular configuration** for different environments and use cases
- **Resource management** to prevent system overload

## Configuration System

### Basic Configuration

```java
@Configuration
public class IllustrationConfig {
    
    @Bean
    public IllustrationConfig illustrationConfig() {
        return IllustrationConfig.builder()
            .globalEnabled(true)
            .actionEnabled(ActionType.FIND, true)
            .actionEnabled(ActionType.CLICK, true)
            .actionEnabled(ActionType.MOVE, false) // Disable noisy move illustrations
            .qualityThreshold(0.75) // Only illustrate high-quality matches
            .maxIllustrationsPerMinute(30) // Rate limiting
            .build();
    }
}
```

### Context-Based Filtering

```java
@Test
public void configureContextFilters() {
    IllustrationConfig config = IllustrationConfig.builder()
        .globalEnabled(true)
        // Only illustrate failures for debugging
        .contextFilter("failures_only", context -> 
            context.getLastActionResult() != null && 
            !context.getLastActionResult().isSuccess())
        // Only illustrate first occurrence of each action type
        .contextFilter("first_occurrence", context -> 
            context.isFirstExecution())
        // Only illustrate during retry attempts
        .contextFilter("retries_only", context -> 
            context.isRetryAttempt())
        .build();
        
    illustrationController.setConfig(config);
}
```

### Adaptive Sampling

```java
@Configuration
public class PerformanceOptimizedIllustrations {
    
    @Bean
    public IllustrationConfig performanceConfig() {
        return IllustrationConfig.builder()
            .globalEnabled(true)
            .adaptiveSampling(true) // Enable intelligent sampling
            // High-frequency actions get reduced sampling
            .samplingRate(ActionType.FIND, 1.0)   // Always illustrate find
            .samplingRate(ActionType.MOVE, 0.1)   // Sample 10% of moves
            .samplingRate(ActionType.CLICK, 0.5)  // Sample 50% of clicks
            // Performance-based batching
            .batchConfig(BatchConfig.builder()
                .maxBatchSize(20)
                .flushInterval(Duration.ofSeconds(10))
                .flushOnStateTransition(true)
                .maxMemoryUsageMB(50)
                .build())
            .build();
    }
}
```

## Quality-Based Filtering

### Basic Quality Metrics

```java
IllustrationConfig qualityFocusedConfig = IllustrationConfig.builder()
    .qualityThreshold(0.8) // Only illustrate matches with >80% quality
    .qualityMetrics(QualityMetrics.builder()
        .minSimilarity(0.75)      // Minimum image similarity
        .minConfidence(0.6)       // Minimum match confidence
        .useRegionSize(true)      // Consider region size in quality
        .useExecutionTime(false)  // Don't factor in timing
        .build())
    .build();
```

### Custom Quality Calculation

```java
IllustrationConfig customQualityConfig = IllustrationConfig.builder()
    .qualityThreshold(0.7)
    .qualityMetrics(QualityMetrics.builder()
        .customQualityCalculator(context -> {
            ActionResult result = context.getLastActionResult();
            if (result == null || result.getMatchList().isEmpty()) {
                return 0.0;
            }
            
            // Custom quality calculation
            double avgSimilarity = result.getMatchList().stream()
                .mapToDouble(match -> match.getScore())
                .average().orElse(0.0);
                
            // Boost quality for first executions
            if (context.isFirstExecution()) {
                avgSimilarity *= 1.2;
            }
            
            // Reduce quality for frequent actions
            if (context.getRecentIllustrationCount() > 10) {
                avgSimilarity *= 0.8;
            }
            
            return Math.min(1.0, avgSimilarity);
        })
        .build())
    .build();
```

## Performance Optimization

### Smart Sampling Strategies

```java
@Service
public class CustomIllustrationStrategy {
    
    @Autowired
    private IllustrationPerformanceOptimizer optimizer;
    
    public void configureAdaptiveSampling() {
        IllustrationConfig config = IllustrationConfig.builder()
            .adaptiveSampling(true)
            .samplingRate(ActionType.FIND, 1.0)
            .contextFilter("performance_aware", context -> {
                // Skip during high system load
                if (context.getSystemMetrics() != null && 
                    context.getSystemMetrics().isHighLoad()) {
                    return context.getPriority() == IllustrationContext.Priority.CRITICAL;
                }
                
                // Reduce frequency for repeated actions
                if (context.getConsecutiveFailures() > 0) {
                    return true; // Always illustrate during failures
                }
                
                // Sample based on recent success rate
                return context.getRecentSuccessRate() < 0.9; // Illustrate when success rate drops
            })
            .build();
            
        optimizer.setConfig(config);
    }
}
```

### Batching for Performance

```java
@Test 
public void testBatchedIllustrations() {
    IllustrationConfig config = IllustrationConfig.builder()
        .batchConfig(BatchConfig.builder()
            .maxBatchSize(50)                    // Batch up to 50 illustrations
            .flushInterval(Duration.ofSeconds(30)) // Flush every 30 seconds
            .flushOnStateTransition(true)        // Flush when states change
            .maxMemoryUsageMB(100)               // Memory limit for batching
            .build())
        .contextFilter("batch_eligible", context -> {
            // Only batch low-priority, frequent actions
            return context.getPriority() == IllustrationContext.Priority.LOW &&
                   context.getCurrentAction() == ActionType.MOVE;
        })
        .build();
        
    illustrationController.setConfig(config);
    
    // Execute many actions - some will be batched for efficiency
    for (int i = 0; i < 100; i++) {
        actions.move(randomLocation());
    }
    
    // Verify performance optimization worked
    PerformanceMetrics.MetricsSnapshot metrics = 
        illustrationPerformanceOptimizer.getPerformanceMetrics();
    
    assertTrue("Some illustrations should be batched", 
               metrics.getIllustrationsBatched() > 0);
    assertTrue("Skip rate should be reasonable", 
               metrics.getSkipRate() < 0.8);
}
```

## State-Aware Illustrations

### Priority-Based Illustration

```java
IllustrationConfig stateAwareConfig = IllustrationConfig.builder()
    // Always illustrate critical states
    .alwaysIllustrateState("ERROR_STATE")
    .alwaysIllustrateState("LOGIN_FAILURE")
    .alwaysIllustrateState("PAYMENT_CONFIRMATION")
    
    // Never illustrate noisy intermediate states
    .neverIllustrateAction(ActionType.MOVE)
    
    .contextFilter("state_priority", context -> {
        // High priority for error conditions
        if (context.hasActiveState("ERROR_STATE", "WARNING_STATE")) {
            return true;
        }
        
        // Medium priority for authentication flows
        if (context.hasActiveState("LOGIN_STATE", "AUTHENTICATION")) {
            return context.getConsecutiveFailures() > 0; // Only on failures
        }
        
        // Low priority for routine operations
        return context.getRecentSuccessRate() < 0.95;
    })
    .build();
```

## Integration with Existing Code

### Migrating from Basic Illustrations

```java
// Old approach - simple enable/disable
@Configuration
public class OldIllustrationConfig {
    @Bean
    public BrobotSettings settings() {
        BrobotSettings settings = new BrobotSettings();
        settings.drawFind = true;
        settings.drawClick = true; 
        settings.saveHistory = true;
        return settings;
    }
}

// New approach - context-aware configuration
@Configuration
public class NewIllustrationConfig {
    @Bean
    public IllustrationConfig illustrationConfig() {
        return IllustrationConfig.builder()
            .globalEnabled(true)
            .actionEnabled(ActionType.FIND, true)
            .actionEnabled(ActionType.CLICK, true)
            
            // Add intelligent filtering
            .contextFilter("meaningful_only", context -> 
                context.isFirstExecution() || 
                context.getConsecutiveFailures() > 0 ||
                context.getLastActionResult() != null && 
                !context.getLastActionResult().isSuccess())
                
            // Add performance optimization    
            .samplingRate(ActionType.FIND, 0.8)
            .qualityThreshold(0.7)
            .maxIllustrationsPerMinute(60)
            .build();
    }
}
```

### ActionConfig Integration

The modern system uses ActionConfig with PatternFindOptions for flexible configuration:

```java
// Modern ActionConfig approach
PatternFindOptions findConfig = new PatternFindOptions.Builder()
    .setActionType(ActionType.FIND)
    .setIllustrate(ActionConfig.Illustrate.USE_GLOBAL)
    .setSimilarity(0.8)
    .build();

PatternClickOptions clickConfig = new PatternClickOptions.Builder()
    .setActionType(ActionType.CLICK)
    .setIllustrate(ActionConfig.Illustrate.YES)
    .setClickType(Click.Type.LEFT)
    .build();
    
// All configurations respect the advanced illustration rules
ActionResult findResult = actions.find(element).configure(findConfig);
ActionResult clickResult = actions.click(element).configure(clickConfig);
```

## Monitoring and Debugging

### Performance Metrics

```java
@Test
public void monitorIllustrationPerformance() {
    // Execute some actions
    for (int i = 0; i < 100; i++) {
        actions.find(testElement);
    }
    
    // Get performance metrics
    PerformanceMetrics.MetricsSnapshot metrics = 
        illustrationPerformanceOptimizer.getPerformanceMetrics();
        
    System.out.println("Illustrations per minute: " + metrics.getIllustrationsPerMinute());
    System.out.println("Skip rate: " + (metrics.getSkipRate() * 100) + "%");
    System.out.println("High quality rate: " + (metrics.getHighQualityRate() * 100) + "%");
    System.out.println("Average processing time: " + metrics.getAverageProcessingTimeMs() + "ms");
    System.out.println("Average memory usage: " + metrics.getAverageMemoryUsageMB() + "MB");
    
    // Verify performance is within acceptable bounds
    assertTrue("Processing time should be reasonable", 
               metrics.getAverageProcessingTimeMs() < 500);
    assertTrue("Memory usage should be controlled", 
               metrics.getAverageMemoryUsageMB() < 10);
}
```

### Debugging Configuration

```java
IllustrationConfig debugConfig = IllustrationConfig.builder()
    .globalEnabled(true)
    // Debug mode - illustrate everything with detailed context
    .contextFilter("debug_mode", context -> {
        System.out.printf("Action: %s, Success: %s, Priority: %s%n", 
            context.getCurrentAction(),
            context.getLastActionResult() != null ? 
                context.getLastActionResult().isSuccess() : "N/A",
            context.getPriority());
        return true; // Illustrate everything in debug mode
    })
    .qualityThreshold(0.0) // No quality filtering in debug
    .maxIllustrationsPerMinute(Integer.MAX_VALUE) // No rate limiting
    .build();
```

## Best Practices

### Configuration Strategy

1. **Start with defaults** - Begin with sensible defaults and tune based on needs
2. **Monitor performance** - Use metrics to identify bottlenecks
3. **Test different environments** - Configurations may need adjustment for different systems
4. **Use state awareness** - Leverage state information for smarter illustration decisions

### Performance Guidelines

1. **Use sampling for high-frequency actions** - Reduce overhead for repetitive operations
2. **Enable batching for bulk operations** - Improve I/O efficiency
3. **Set appropriate quality thresholds** - Focus on meaningful illustrations
4. **Monitor system resources** - Prevent illustration system from impacting automation performance

### Quality Management

1. **Define clear quality metrics** - Establish what constitutes a valuable illustration
2. **Use custom quality calculators** - Tailor quality assessment to your specific needs
3. **Balance coverage and noise** - Ensure important events are captured without overwhelming detail
4. **Regular quality audits** - Periodically review illustration quality and adjust thresholds

### Context Utilization

1. **Leverage action history** - Use past executions to inform current decisions
2. **Consider system state** - Factor in current states and transitions
3. **Use priority levels** - Ensure critical actions are always illustrated
4. **Implement fallbacks** - Have sensible defaults when context is unavailable

## Configuration Reference

### IllustrationConfig Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `globalEnabled` | boolean | true | Master switch for all illustrations |
| `actionEnabledMap` | `Map<Action, Boolean>` | all true | Per-action enablement |
| `contextFilters` | `Map<String, Predicate>` | empty | Context-based filters |
| `samplingRates` | `Map<Action, Double>` | all 1.0 | Action sampling rates |
| `qualityThreshold` | double | 0.0 | Minimum quality for illustration |
| `maxIllustrationsPerMinute` | int | MAX_VALUE | Rate limiting |
| `adaptiveSampling` | boolean | false | Enable adaptive sampling |

### BatchConfig Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `maxBatchSize` | int | 10 | Maximum illustrations per batch |
| `flushInterval` | Duration | 5s | Maximum batch hold time |
| `flushOnStateTransition` | boolean | true | Flush on state changes |
| `maxMemoryUsageMB` | int | 100 | Memory limit for batching |

### QualityMetrics Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `minSimilarity` | double | 0.7 | Minimum match similarity |
| `minConfidence` | double | 0.5 | Minimum match confidence |
| `useRegionSize` | boolean | true | Factor in region size |
| `useExecutionTime` | boolean | false | Factor in execution time |
| `customQualityCalculator` | Function | null | Custom quality function |

The advanced illustration system provides powerful tools for creating meaningful, performance-optimized visual documentation of your automation execution while maintaining system responsiveness and focusing on the most important events.