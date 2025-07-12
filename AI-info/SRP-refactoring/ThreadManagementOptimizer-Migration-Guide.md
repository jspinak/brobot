# ThreadManagementOptimizer Migration Guide

## Overview
This guide details the refactoring of ThreadManagementOptimizer from a monolithic 465-line class into a service-oriented architecture following the Single Responsibility Principle.

## Service Architecture

### 1. ThreadPoolManagementService
**Responsibility**: Lifecycle management of thread pools

```java
@Service
public class ThreadPoolManagementService implements DiagnosticCapable {
    // Pool registry and lifecycle management
    Map<String, ManagedThreadPool> pools;
    
    public ExecutorService createPool(String name, ThreadPoolConfig config);
    public Optional<ExecutorService> getPool(String name);
    public void shutdownPool(String name);
    public void shutdownAll();
    public Map<String, ThreadPoolHealth> getAllPoolHealth();
}
```

### 2. ThreadMonitoringService
**Responsibility**: Monitor thread health and collect metrics

```java
@Service
public class ThreadMonitoringService implements DiagnosticCapable {
    // Thread monitoring and metrics
    ThreadMXBean threadBean;
    
    public ThreadStatistics getCurrentStatistics();
    public List<ThreadContentionInfo> detectContention();
    public void enableContentionMonitoring(boolean enable);
    public Map<Long, ThreadInfo> getThreadDetails();
}
```

### 3. ThreadPoolFactoryService
**Responsibility**: Create thread pools with appropriate configurations

```java
@Service
public class ThreadPoolFactoryService {
    // Factory methods for different pool types
    
    public ThreadPoolExecutor createDefaultPool(String name);
    public ThreadPoolExecutor createIOIntensivePool(String name);
    public ThreadPoolExecutor createCPUIntensivePool(String name);
    public ThreadPoolExecutor createCustomPool(String name, ThreadPoolConfig config);
    public ThreadFactory createThreadFactory(String poolName);
}
```

### 4. ThreadOptimizationService
**Responsibility**: Optimize thread usage based on system metrics

```java
@Service
public class ThreadOptimizationService implements DiagnosticCapable {
    // Optimization strategies
    
    public OptimizationResult optimizeThreadUsage(ThreadStatistics stats);
    public void adjustPoolSize(String poolName, PoolAdjustment adjustment);
    public OptimizationStrategy determineStrategy(SystemMetrics metrics);
    public void applyOptimizations(List<OptimizationAction> actions);
}
```

### 5. ThreadManagementOptimizer (Orchestrator)
**Responsibility**: Coordinate services and maintain public API

```java
@Component
public class ThreadManagementOptimizer implements DiagnosticCapable {
    // Thin orchestrator
    private final ThreadPoolManagementService poolManagement;
    private final ThreadMonitoringService monitoring;
    private final ThreadPoolFactoryService factory;
    private final ThreadOptimizationService optimization;
    
    // Delegate to services while maintaining API compatibility
}
```

## Migration Steps

### Phase 1: Extract ThreadPoolManagementService
1. Create ThreadPoolManagementService class
2. Move pool registry (Map<String, ManagedThreadPool>)
3. Move pool lifecycle methods
4. Move ManagedThreadPool inner class
5. Add diagnostic capabilities

### Phase 2: Extract ThreadMonitoringService  
1. Create ThreadMonitoringService class
2. Move ThreadMXBean and monitoring logic
3. Move thread contention detection
4. Move statistics reporting
5. Extract ThreadStatistics model

### Phase 3: Extract ThreadPoolFactoryService
1. Create ThreadPoolFactoryService class
2. Move ThreadPoolConfig class
3. Move OptimizedThreadFactory class
4. Move pool creation methods
5. Add configuration templates

### Phase 4: Extract ThreadOptimizationService
1. Create ThreadOptimizationService class
2. Move optimization logic
3. Move CPU threshold management
4. Create optimization strategies
5. Add diagnostic capabilities

### Phase 5: Refactor Orchestrator
1. Update ThreadManagementOptimizer to use services
2. Maintain public API compatibility
3. Add service coordination logic
4. Implement diagnostic aggregation

## Code Migration Examples

### Before (Monolithic)
```java
public ExecutorService createOptimizedPool(String name, ThreadPoolConfig config) {
    ManagedThreadPool pool = new ManagedThreadPool(name, config);
    threadPools.put(name, pool);
    return pool;
}
```

### After (Service-Oriented)
```java
// In ThreadPoolManagementService
public ExecutorService createPool(String name, ThreadPoolConfig config) {
    ManagedThreadPool pool = factory.createCustomPool(name, config);
    pools.put(name, pool);
    monitoring.registerPool(name, pool);
    return pool;
}

// In ThreadManagementOptimizer (Orchestrator)
public ExecutorService createOptimizedPool(String name, ThreadPoolConfig config) {
    return poolManagement.createPool(name, config);
}
```

## Testing Strategy

### Unit Tests per Service
1. ThreadPoolManagementServiceTest
   - Pool creation and retrieval
   - Shutdown behavior
   - Health monitoring

2. ThreadMonitoringServiceTest
   - Statistics collection
   - Contention detection
   - Metric accuracy

3. ThreadPoolFactoryServiceTest
   - Pool configurations
   - Thread factory behavior
   - Configuration validation

4. ThreadOptimizationServiceTest
   - Optimization strategies
   - Threshold management
   - Action application

### Integration Tests
1. Thread management workflow tests
2. Service coordination tests
3. Performance benchmarks

## Rollback Plan

If issues arise:
1. The refactored code maintains the same public API
2. Git branch allows easy rollback
3. Comprehensive tests ensure behavior preservation
4. Gradual rollout possible with feature flags

## Success Metrics

1. **Maintainability**: Each service under 200 lines
2. **Testability**: 90%+ test coverage per service
3. **Performance**: No degradation in thread management
4. **Clarity**: Clear separation of concerns