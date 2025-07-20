# Brobot AspectJ Implementation

## Overview

This directory contains AspectJ implementations that add powerful cross-cutting features to the Brobot framework without modifying existing code. These aspects provide monitoring, error handling, and lifecycle management capabilities.

## Implemented Aspects

### 1. SikuliInterceptionAspect
**Location**: `core/SikuliInterceptionAspect.java`

**Purpose**: Intercepts all Sikuli method calls to provide centralized error handling, mock mode support, and performance metrics.

**Features**:
- Automatic error translation from FindFailed to ActionFailedException
- Mock mode implementation without changing wrapper classes
- Performance metrics collection for all Sikuli operations
- Comprehensive operation logging
- Screenshot capture on failures (when ScreenCapture is available)

**Configuration**:
```properties
brobot.aspects.sikuli.enabled=true
brobot.aspects.sikuli.log-level=DEBUG
brobot.aspects.sikuli.performance-warning-threshold=5000
```

### 2. ActionLifecycleAspect
**Location**: `core/ActionLifecycleAspect.java`

**Purpose**: Manages the complete lifecycle of action executions, centralizing cross-cutting concerns previously scattered in ActionExecution.

**Features**:
- Pre-execution setup (timing, logging, pause points)
- Post-execution tasks (screenshots, metrics collection)
- Configurable action pauses
- Action context tracking for use by other aspects
- Automatic success/failure determination

**Configuration**:
```properties
brobot.aspects.action-lifecycle.enabled=true
brobot.aspects.action-lifecycle.log-events=true
brobot.aspects.action-lifecycle.capture-before-screenshot=false
brobot.aspects.action-lifecycle.capture-after-screenshot=true
brobot.action.pre-pause=0
brobot.action.post-pause=0
```

### 3. PerformanceMonitoringAspect
**Location**: `monitoring/PerformanceMonitoringAspect.java`

**Purpose**: Provides comprehensive performance monitoring for all Brobot operations without code changes.

**Features**:
- Method-level execution time tracking
- Statistical analysis (min, max, avg, percentiles)
- Performance trend detection
- Slow operation alerts
- Periodic performance reports
- Memory usage correlation

**Configuration**:
```properties
brobot.aspects.performance.enabled=true
brobot.aspects.performance.alert-threshold=10000
brobot.aspects.performance.report-interval=300
brobot.aspects.performance.track-memory=true
```

### 4. StateTransitionAspect
**Location**: `monitoring/StateTransitionAspect.java`

**Purpose**: Tracks and analyzes state transitions to provide insights into navigation patterns and state machine behavior.

**Features**:
- Real-time state transition graph building
- Success/failure rate tracking
- Transition timing analysis
- State machine visualization generation (DOT format)
- Unreachable state detection
- Navigation pattern analytics

**Configuration**:
```properties
brobot.aspects.state-transition.enabled=true
brobot.aspects.state-transition.generate-visualizations=true
brobot.aspects.state-transition.visualization-dir=./state-visualizations
brobot.aspects.state-transition.track-success-rates=true
```

## AspectJ Configuration

### Maven Dependencies
Add to your `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### Enable AspectJ
Ensure your Spring Boot application has AspectJ enabled:
```java
@SpringBootApplication
@EnableAspectJAutoProxy
public class BrobotApplication {
    // ...
}
```

## Benefits Achieved

1. **Code Separation**: Cross-cutting concerns are now separated from business logic
2. **Zero Intrusion**: No changes required to existing Brobot code
3. **Centralized Control**: All aspects can be enabled/disabled via configuration
4. **Comprehensive Monitoring**: Performance and behavior tracking without manual instrumentation
5. **Improved Debugging**: Better visibility into action execution and state transitions
6. **Mock Mode Simplification**: Mock behavior centralized in one location

## Usage Examples

### Accessing Performance Metrics
```java
@Autowired
private PerformanceMonitoringAspect performanceAspect;

// Get performance statistics
Map<String, MethodPerformanceStats> stats = performanceAspect.getPerformanceStats();
stats.forEach((method, methodStats) -> {
    System.out.printf("%s: avg=%dms, calls=%d%n", 
        method, methodStats.getAverageTime(), methodStats.getTotalCalls());
});
```

### Accessing State Transition Data
```java
@Autowired
private StateTransitionAspect stateAspect;

// Get state graph
Map<String, StateNode> graph = stateAspect.getStateGraph();

// Get transition statistics
Map<String, TransitionStats> transitions = stateAspect.getTransitionStats();
```

### Getting Current Action Context
```java
@Autowired
private ActionLifecycleAspect lifecycleAspect;

// Get current action context (useful in other aspects)
Optional<ActionContext> context = lifecycleAspect.getCurrentActionContext();
context.ifPresent(ctx -> {
    System.out.println("Current action: " + ctx.getActionType());
});
```

## Future Enhancements

The implementation plan includes additional aspects for:
- Error Recovery (with retry policies)
- Dataset Collection (for ML training)
- Multi-Monitor Routing
- Visual Feedback

These can be implemented following the same patterns established in the current aspects.

## Troubleshooting

### Aspects Not Working
1. Ensure AspectJ is enabled in your application
2. Check that the aspect is enabled in properties
3. Verify the pointcut expressions match your package structure

### Performance Impact
- Use compile-time weaving for better performance
- Disable debug logging in production
- Adjust report intervals based on your needs

### Memory Usage
- Performance and state tracking maintain in-memory data
- Use the reset methods periodically if needed
- Configure appropriate limits for tracked data

## Conclusion

These AspectJ implementations demonstrate the power of aspect-oriented programming in improving the Brobot framework. They provide valuable cross-cutting features while maintaining clean separation from the core business logic.