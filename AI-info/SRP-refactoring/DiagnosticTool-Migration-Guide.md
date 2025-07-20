# DiagnosticTool Migration Guide

## Overview
This guide details the refactoring of DiagnosticTool from a monolithic 441-line class into a service-oriented architecture following the Single Responsibility Principle.

## Service Architecture

### 1. SystemInfoCollectorService
**Responsibility**: Collect system and JVM information

```java
@Service
public class SystemInfoCollectorService implements DiagnosticCapable {
    private final RuntimeMXBean runtimeBean;
    private final OperatingSystemMXBean osBean;
    
    public SystemInfo collectSystemInfo();
    public Map<String, String> collectEnvironmentVariables();
    public Map<String, String> collectSystemProperties();
    public double getCpuUsage();
}
```

### 2. MemoryDiagnosticService
**Responsibility**: Analyze memory usage and health

```java
@Service
public class MemoryDiagnosticService implements DiagnosticCapable {
    private final MemoryMXBean memoryBean;
    
    public MemoryInfo collectMemoryInfo();
    public Map<String, MemoryPoolInfo> analyzeMemoryPools();
    public HealthCheckItem checkMemoryHealth();
    public String formatMemoryReport();
}
```

### 3. ThreadDiagnosticService
**Responsibility**: Monitor thread state and health

```java
@Service
public class ThreadDiagnosticService implements DiagnosticCapable {
    private final ThreadMXBean threadBean;
    
    public ThreadInfo collectThreadInfo();
    public List<ThreadInfo> detectDeadlocks();
    public Map<Thread.State, Integer> analyzeThreadStates();
    public HealthCheckItem checkThreadHealth();
}
```

### 4. PerformanceDiagnosticService
**Responsibility**: Collect performance metrics

```java
@Service
public class PerformanceDiagnosticService implements DiagnosticCapable {
    private final List<GarbageCollectorMXBean> gcBeans;
    private final PerformanceProfiler performanceProfiler;
    
    public PerformanceMetrics collectPerformanceMetrics();
    public Map<String, GCInfo> analyzeGCMetrics();
    public HealthCheckItem checkGCHealth();
    public double calculateGCOverhead();
}
```

### 5. HealthCheckService
**Responsibility**: Run comprehensive health checks

```java
@Service
public class HealthCheckService implements DiagnosticCapable {
    private final MemoryDiagnosticService memoryDiagnostic;
    private final ThreadDiagnosticService threadDiagnostic;
    private final PerformanceDiagnosticService performanceDiagnostic;
    private final ErrorHandler errorHandler;
    
    public HealthCheckResult runHealthChecks();
    public HealthCheckItem checkDiskSpace();
    public HealthCheckItem checkErrorRate();
    public HealthStatus determineOverallHealth(List<HealthCheckItem> checks);
}
```

### 6. DiagnosticReportService
**Responsibility**: Format and export diagnostic reports

```java
@Service
public class DiagnosticReportService {
    public String formatReport(DiagnosticReport report);
    public Path exportDiagnostics(DiagnosticReport report, Path outputDir);
    public String formatUptime(long uptimeMs);
    public String formatBytes(long bytes);
}
```

### 7. DiagnosticTool (Orchestrator)
**Responsibility**: Coordinate diagnostic services

```java
@Component
public class DiagnosticTool implements DiagnosticCapable {
    private final SystemInfoCollectorService systemInfo;
    private final MemoryDiagnosticService memoryDiagnostic;
    private final ThreadDiagnosticService threadDiagnostic;
    private final PerformanceDiagnosticService performanceDiagnostic;
    private final HealthCheckService healthCheck;
    private final DiagnosticReportService reportService;
    
    public DiagnosticReport runDiagnostics();
    public HealthCheckResult runHealthChecks();
    public Path exportDiagnostics(Path outputDir);
}
```

## Migration Steps

### Phase 1: Extract SystemInfoCollectorService
1. Create SystemInfoCollectorService class
2. Move RuntimeMXBean and OperatingSystemMXBean
3. Extract system info collection methods
4. Add CPU usage calculation
5. Implement DiagnosticCapable

### Phase 2: Extract MemoryDiagnosticService
1. Create MemoryDiagnosticService class
2. Move MemoryMXBean and memory pool logic
3. Extract memory collection methods
4. Move memory health check
5. Add memory formatting utilities

### Phase 3: Extract ThreadDiagnosticService
1. Create ThreadDiagnosticService class
2. Move ThreadMXBean operations
3. Extract thread analysis methods
4. Move deadlock detection
5. Add thread health check

### Phase 4: Extract PerformanceDiagnosticService
1. Create PerformanceDiagnosticService class
2. Move GC beans and performance profiler
3. Extract GC metric collection
4. Move GC health check
5. Add performance calculations

### Phase 5: Extract HealthCheckService
1. Create HealthCheckService class
2. Aggregate health checks from other services
3. Add disk space check
4. Add error rate check
5. Implement overall health determination

### Phase 6: Extract DiagnosticReportService
1. Create DiagnosticReportService class
2. Move report formatting logic
3. Move export functionality
4. Extract formatting utilities
5. Add report templates

### Phase 7: Refactor Orchestrator
1. Update DiagnosticTool to use services
2. Remove direct MXBean usage
3. Implement service coordination
4. Maintain public API
5. Add diagnostic aggregation

## Code Migration Examples

### Before (Monolithic)
```java
private SystemInfo collectSystemInfo() {
    return SystemInfo.builder()
        .osName(System.getProperty("os.name"))
        .osVersion(System.getProperty("os.version"))
        .javaVersion(System.getProperty("java.version"))
        .uptime(runtimeBean.getUptime())
        .build();
}
```

### After (Service-Oriented)
```java
// In SystemInfoCollectorService
public SystemInfo collectSystemInfo() {
    return SystemInfo.builder()
        .osName(System.getProperty("os.name"))
        .osVersion(System.getProperty("os.version"))
        .javaVersion(System.getProperty("java.version"))
        .uptime(runtimeBean.getUptime())
        .build();
}

// In DiagnosticTool (Orchestrator)
public DiagnosticReport runDiagnostics() {
    return DiagnosticReport.builder()
        .systemInfo(systemInfo.collectSystemInfo())
        .memoryInfo(memoryDiagnostic.collectMemoryInfo())
        .threadInfo(threadDiagnostic.collectThreadInfo())
        .build();
}
```

## Testing Strategy

### Unit Tests per Service
1. SystemInfoCollectorServiceTest
   - System property collection
   - Environment variable collection
   - CPU usage calculation

2. MemoryDiagnosticServiceTest
   - Memory info collection
   - Memory pool analysis
   - Health check thresholds

3. ThreadDiagnosticServiceTest
   - Thread state analysis
   - Deadlock detection
   - Thread count monitoring

4. PerformanceDiagnosticServiceTest
   - GC metric collection
   - Performance calculations
   - GC overhead analysis

5. HealthCheckServiceTest
   - Individual health checks
   - Overall health determination
   - Threshold validation

6. DiagnosticReportServiceTest
   - Report formatting
   - Export functionality
   - Utility formatting

### Integration Tests
1. Full diagnostic workflow
2. Service coordination
3. Report generation

## Success Metrics

1. **Maintainability**: Each service under 150 lines
2. **Testability**: 90%+ test coverage per service
3. **Performance**: No degradation in diagnostic collection
4. **Modularity**: Services usable independently