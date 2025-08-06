# Advanced Illustration System Examples

This project demonstrates Brobot's **Advanced Illustration System** - an intelligent, context-aware visual documentation system with performance optimization and quality-based filtering.

## Overview

The advanced illustration system provides:

- **Context-aware decisions** - Illustrate based on action history and system state
- **Performance optimization** - Adaptive sampling, batching, and resource management
- **Quality-based filtering** - Focus on meaningful, high-quality visualizations
- **State-based priorities** - Always capture critical states and transitions
- **Granular configuration** - Fine-tune for different environments and use cases

## Project Structure

```
advanced-illustration-system/
├── src/main/java/com/example/illustration/
│   ├── AdvancedIllustrationApplication.java  # Spring Boot main
│   ├── AdvancedIllustrationRunner.java       # Runs all examples
│   ├── config/
│   │   ├── BasicIllustrationConfig.java     # Basic configurations
│   │   └── ContextAwareConfig.java          # Context-based filtering
│   └── examples/
│       ├── LoginWorkflowExample.java        # State-aware illustrations
│       ├── PerformanceOptimizationExample.java # Performance strategies
│       └── QualityFilteringExample.java     # Quality-based filtering
├── src/main/resources/
│   └── application.yml                       # Configuration
├── images/                                   # Place test images here
│   ├── authentication/
│   ├── data/
│   └── ui-elements/
├── illustrations/                            # Generated output
├── build.gradle
└── settings.gradle
```

## Examples Demonstrated

### 1. Login Workflow Example

Shows state-aware illustration configuration for critical authentication flows:

```java
IllustrationConfig.builder()
    // Always illustrate authentication states
    .alwaysIllustrateState("LOGIN_STATE")
    .alwaysIllustrateState("LOGIN_FAILURE")
    
    // Context filter for authentication priority
    .contextFilter("auth_priority", context -> {
        // Critical: Always illustrate login attempts
        if (context.hasActiveState("LOGIN_STATE")) {
            return true;
        }
        
        // Reduce illustrations once logged in
        if (context.hasActiveState("DASHBOARD_STATE")) {
            // Only illustrate failures
            return !context.getLastActionResult().isSuccess();
        }
        
        return true;
    })
    .build();
```

Key concepts:
- Critical states are always illustrated
- Routine operations have reduced illustration frequency
- Failures are prioritized for debugging

### 2. Performance Optimization Example

Demonstrates strategies for high-volume operations:

**Batching Configuration:**
```java
.batchConfig(BatchConfig.builder()
    .maxBatchSize(50)                      // Batch up to 50 illustrations
    .flushInterval(Duration.ofSeconds(30)) // Flush every 30 seconds
    .flushOnStateTransition(true)          // Flush when states change
    .maxMemoryUsageMB(100)                 // Memory limit
    .build())
```

**Adaptive Sampling:**
```java
.adaptiveSampling(true)
.samplingRate(ActionOptions.Action.MOVE, 0.1)   // Sample 10% of moves
.samplingRate(ActionOptions.Action.FIND, 0.5)   // Sample 50% of finds
.samplingRate(ActionOptions.Action.CLICK, 0.8)  // Sample 80% of clicks
```

**System-Aware Decisions:**
```java
.contextFilter("system_aware", context -> {
    double cpuUsage = context.getSystemMetrics().getCpuUsage();
    
    // Reduce illustrations under high load
    if (cpuUsage > 0.8) {
        // Only critical actions
        return context.getPriority() == IllustrationContext.Priority.CRITICAL;
    }
    
    // Normal load - use success rate based sampling
    return context.getRecentSuccessRate() < 0.9;
})
```

### 3. Quality Filtering Example

Shows quality-based filtering to focus on meaningful visualizations:

**Basic Quality Metrics:**
```java
.qualityThreshold(0.8) // Only illustrate >80% quality matches
.qualityMetrics(QualityMetrics.builder()
    .minSimilarity(0.75)      // Minimum image similarity
    .minConfidence(0.6)       // Minimum match confidence
    .useRegionSize(true)      // Consider region size
    .build())
```

**Custom Quality Calculation:**
```java
.customQualityCalculator(context -> {
    double avgSimilarity = calculateAverageSimilarity(context);
    
    // Boost quality for important scenarios
    if (context.isFirstExecution()) {
        avgSimilarity *= 1.2; // Important for documentation
    }
    
    if (!context.getLastActionResult().isSuccess()) {
        avgSimilarity *= 1.5; // Important for debugging
    }
    
    // Reduce quality for noise
    if (context.getRecentIllustrationCount() > 10) {
        avgSimilarity *= 0.8; // Reduce frequent actions
    }
    
    return Math.min(1.0, avgSimilarity);
})
```

## Running the Examples

1. **In Mock Mode** (default):
   ```bash
   ./gradlew bootRun
   ```
   Runs with simulated UI interactions to demonstrate configuration behavior.

2. **With Real UI**:
   - Add screenshots to the `images/` directory
   - Set `brobot.core.mock: false` in `application.yml`
   - Run the application

3. **View Output**:
   - Check the `illustrations/` directory for generated visualizations
   - Review console logs for illustration decisions

## Configuration Strategies

### 1. **Development Environment**
- Enable all illustrations for debugging
- No quality filtering
- Detailed logging

```java
IllustrationConfig.builder()
    .globalEnabled(true)
    .qualityThreshold(0.0)
    .maxIllustrationsPerMinute(Integer.MAX_VALUE)
    .build()
```

### 2. **Testing Environment**
- Focus on failures and first occurrences
- Moderate quality filtering
- Performance optimization

```java
IllustrationConfig.builder()
    .contextFilter("test_mode", context -> 
        context.isFirstExecution() || 
        !context.getLastActionResult().isSuccess())
    .qualityThreshold(0.7)
    .adaptiveSampling(true)
    .build()
```

### 3. **Production Environment**
- Minimal illustrations for critical events
- High quality threshold
- Aggressive performance optimization

```java
IllustrationConfig.builder()
    .contextFilter("production", context -> 
        context.getPriority() == IllustrationContext.Priority.CRITICAL ||
        context.getConsecutiveFailures() > 2)
    .qualityThreshold(0.9)
    .maxIllustrationsPerMinute(10)
    .build()
```

## Performance Metrics

The system provides detailed performance metrics:

```java
PerformanceMetrics.MetricsSnapshot metrics = 
    performanceOptimizer.getPerformanceMetrics();

// Available metrics:
metrics.getIllustrationsPerMinute()    // Rate of illustration generation
metrics.getSkipRate()                  // Percentage skipped by filters
metrics.getHighQualityRate()           // Percentage meeting quality threshold
metrics.getAverageProcessingTimeMs()   // Average time to generate
metrics.getAverageMemoryUsageMB()      // Memory consumption
metrics.getIllustrationsBatched()      // Number batched for efficiency
```

## Best Practices

1. **Start Conservative**: Begin with default settings and adjust based on needs
2. **Monitor Performance**: Use metrics to identify bottlenecks
3. **Test Configurations**: Different environments need different settings
4. **Use State Information**: Leverage state context for smarter decisions
5. **Balance Coverage**: Ensure important events are captured without noise

## Integration with ActionConfig

The system works seamlessly with both legacy and new action configurations:

```java
// Legacy ActionOptions
ActionOptions options = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.FIND)
    .setIllustrate(ActionOptions.Illustrate.YES)
    .build();

// New ActionConfig
PatternFindOptions findConfig = new PatternFindOptions.Builder()
    .setIllustrate(ActionConfig.Illustrate.USE_GLOBAL)
    .setSimilarity(0.8)
    .build();

// Both respect advanced configuration rules
```

## Troubleshooting

### High Memory Usage
- Reduce `maxBatchSize` in BatchConfig
- Lower `maxInMemoryIllustrations`
- Enable compression in BatchConfig

### Missing Important Illustrations
- Check quality threshold isn't too high
- Verify context filters aren't too restrictive
- Ensure critical states are in `alwaysIllustrateState`

### Too Many Illustrations
- Enable adaptive sampling
- Increase quality threshold
- Add rate limiting with `maxIllustrationsPerMinute`

## Next Steps

1. Run the examples to see different configurations in action
2. Experiment with custom quality calculators
3. Create context filters for your specific use cases
4. Monitor performance metrics and adjust accordingly
5. Integrate with your automation projects

## Related Documentation

- [Advanced Illustration System Guide](../../advanced-illustration-system.md)
- [Action Configuration](../../../action-config/README.md)
- [Performance Optimization](../../../../04-performance/optimization.md)