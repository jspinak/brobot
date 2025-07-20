# Diagnostic Infrastructure Blueprint for Brobot Runner

## Overview

This blueprint defines a comprehensive diagnostic infrastructure that makes the Brobot Runner codebase highly debuggable for both AI assistants and human developers. The infrastructure provides real-time insights, execution tracing, and state inspection capabilities.

## Core Components

### 1. Diagnostic Interface Hierarchy

```java
// Base diagnostic interface
public interface DiagnosticCapable {
    DiagnosticInfo getDiagnosticInfo();
    default boolean isDiagnosticModeEnabled() { return false; }
    default void enableDiagnosticMode(boolean enabled) {}
}

// Extended diagnostic capabilities
public interface TracingCapable extends DiagnosticCapable {
    TraceInfo getTraceInfo();
    void startTrace(String operationName);
    void endTrace();
}

// Performance diagnostic interface
public interface PerformanceMonitorable extends DiagnosticCapable {
    PerformanceMetrics getPerformanceMetrics();
    void resetMetrics();
}

// State inspection interface
public interface StateInspectable extends DiagnosticCapable {
    Map<String, Object> inspectState();
    List<StateTransition> getStateHistory();
}
```

### 2. Diagnostic Data Models

```java
@Value
@Builder
public class DiagnosticInfo {
    String component;
    Map<String, Object> states;
    Instant timestamp;
    String correlationId;
    List<String> warnings;
    List<String> errors;
    
    public static DiagnosticInfo error(String component, Throwable error) {
        return DiagnosticInfo.builder()
                .component(component)
                .timestamp(Instant.now())
                .errors(List.of(error.getMessage()))
                .build();
    }
}

@Value
@Builder
public class TraceInfo {
    String operationName;
    String correlationId;
    Instant startTime;
    Instant endTime;
    List<TraceEvent> events;
    Map<String, Object> metadata;
}

@Value
@Builder
public class PerformanceMetrics {
    long totalOperations;
    double averageResponseTime;
    double p95ResponseTime;
    double p99ResponseTime;
    long errorCount;
    Map<String, Long> operationCounts;
}
```

### 3. Diagnostic Service Architecture

```java
@Service
@Slf4j
public class DiagnosticService {
    private final Map<String, DiagnosticCapable> registeredComponents = new ConcurrentHashMap<>();
    private final ExecutorService diagnosticExecutor = Executors.newCachedThreadPool();
    private final DiagnosticEventBus eventBus;
    private final DiagnosticRepository repository;
    
    // Component registration
    public void registerComponent(String componentId, DiagnosticCapable component) {
        registeredComponents.put(componentId, component);
        log.info("Registered diagnostic component: {}", componentId);
    }
    
    // System-wide diagnostic snapshot
    public SystemDiagnosticSnapshot captureSystemSnapshot() {
        String snapshotId = UUID.randomUUID().toString();
        Instant captureTime = Instant.now();
        
        Map<String, CompletableFuture<DiagnosticInfo>> futures = new HashMap<>();
        
        // Capture diagnostics from all components concurrently
        registeredComponents.forEach((id, component) -> {
            CompletableFuture<DiagnosticInfo> future = CompletableFuture.supplyAsync(
                    component::getDiagnosticInfo, diagnosticExecutor);
            futures.put(id, future);
        });
        
        // Wait for all captures to complete
        Map<String, DiagnosticInfo> diagnostics = new HashMap<>();
        futures.forEach((id, future) -> {
            try {
                diagnostics.put(id, future.get(5, TimeUnit.SECONDS));
            } catch (Exception e) {
                log.warn("Failed to capture diagnostics for component: {}", id, e);
                diagnostics.put(id, DiagnosticInfo.error(id, e));
            }
        });
        
        SystemDiagnosticSnapshot snapshot = SystemDiagnosticSnapshot.builder()
                .snapshotId(snapshotId)
                .captureTime(captureTime)
                .componentDiagnostics(diagnostics)
                .systemMetrics(captureSystemMetrics())
                .build();
        
        // Store snapshot for historical analysis
        repository.saveSnapshot(snapshot);
        
        // Publish event
        eventBus.publish(new DiagnosticSnapshotCapturedEvent(snapshot));
        
        return snapshot;
    }
}
```

### 4. Correlation ID Management

```java
@Component
@Slf4j
public class CorrelationIdManager {
    private static final ThreadLocal<String> correlationId = new ThreadLocal<>();
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    
    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
    
    public void setCorrelationId(String id) {
        correlationId.set(id);
        MDC.put("correlationId", id);
    }
    
    public String getCorrelationId() {
        String id = correlationId.get();
        if (id == null) {
            id = generateCorrelationId();
            setCorrelationId(id);
        }
        return id;
    }
    
    public void clear() {
        correlationId.remove();
        MDC.remove("correlationId");
    }
    
    // Interceptor for HTTP requests
    @Component
    public class CorrelationIdInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                                Object handler) {
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null) {
                correlationId = generateCorrelationId();
            }
            setCorrelationId(correlationId);
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            return true;
        }
        
        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                   Object handler, Exception ex) {
            clear();
        }
    }
}
```

### 5. Execution Tracing System

```java
@Component
@Slf4j
public class ExecutionTracer {
    private final Map<String, ExecutionTrace> activeTraces = new ConcurrentHashMap<>();
    private final TracingRepository repository;
    
    public ExecutionTrace startTrace(String operationName, Map<String, Object> metadata) {
        String traceId = UUID.randomUUID().toString();
        String correlationId = MDC.get("correlationId");
        
        ExecutionTrace trace = ExecutionTrace.builder()
                .traceId(traceId)
                .operationName(operationName)
                .correlationId(correlationId)
                .startTime(Instant.now())
                .metadata(metadata)
                .events(new CopyOnWriteArrayList<>())
                .build();
        
        activeTraces.put(traceId, trace);
        log.debug("Started trace: {} for operation: {}", traceId, operationName);
        
        return trace;
    }
    
    public void addEvent(String traceId, TraceEvent event) {
        ExecutionTrace trace = activeTraces.get(traceId);
        if (trace != null) {
            trace.addEvent(event);
        }
    }
    
    public void endTrace(String traceId, TraceStatus status) {
        ExecutionTrace trace = activeTraces.remove(traceId);
        if (trace != null) {
            trace.setEndTime(Instant.now());
            trace.setStatus(status);
            trace.setDuration(Duration.between(trace.getStartTime(), trace.getEndTime()));
            
            // Store completed trace
            repository.saveTrace(trace);
            
            log.debug("Completed trace: {} with status: {}", traceId, status);
        }
    }
    
    // Annotation-based tracing
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Trace {
        String value() default "";
    }
    
    @Aspect
    @Component
    public class TracingAspect {
        @Around("@annotation(trace)")
        public Object trace(ProceedingJoinPoint joinPoint, Trace trace) throws Throwable {
            String operationName = StringUtils.hasText(trace.value()) ? 
                    trace.value() : joinPoint.getSignature().toShortString();
            
            ExecutionTrace executionTrace = startTrace(operationName, 
                    extractMethodParameters(joinPoint));
            
            try {
                Object result = joinPoint.proceed();
                endTrace(executionTrace.getTraceId(), TraceStatus.SUCCESS);
                return result;
            } catch (Throwable e) {
                executionTrace.addEvent(TraceEvent.error(e.getMessage()));
                endTrace(executionTrace.getTraceId(), TraceStatus.FAILED);
                throw e;
            }
        }
    }
}
```

### 6. State Inspection Framework

```java
@Component
public class StateInspector {
    private final Map<String, StateInspectable> inspectableComponents = new ConcurrentHashMap<>();
    
    public void registerInspectable(String componentId, StateInspectable component) {
        inspectableComponents.put(componentId, component);
    }
    
    public ComponentStateReport inspectComponent(String componentId) {
        StateInspectable component = inspectableComponents.get(componentId);
        if (component == null) {
            throw new ComponentNotFoundException(
                    String.format("Component '%s' not found. Available components: %s",
                            componentId, inspectableComponents.keySet()));
        }
        
        Map<String, Object> currentState = component.inspectState();
        List<StateTransition> history = component.getStateHistory();
        DiagnosticInfo diagnostics = component.getDiagnosticInfo();
        
        return ComponentStateReport.builder()
                .componentId(componentId)
                .currentState(currentState)
                .stateHistory(history)
                .diagnostics(diagnostics)
                .inspectionTime(Instant.now())
                .build();
    }
    
    public SystemStateReport inspectSystem() {
        Map<String, ComponentStateReport> componentReports = new HashMap<>();
        
        inspectableComponents.forEach((id, component) -> {
            try {
                componentReports.put(id, inspectComponent(id));
            } catch (Exception e) {
                log.error("Failed to inspect component: {}", id, e);
            }
        });
        
        return SystemStateReport.builder()
                .reports(componentReports)
                .systemHealth(calculateSystemHealth(componentReports))
                .timestamp(Instant.now())
                .build();
    }
}
```

### 7. Performance Monitoring

```java
@Component
public class PerformanceMonitor {
    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    
    public class MonitoredOperation implements AutoCloseable {
        private final Timer.Sample sample;
        private final String operationName;
        
        private MonitoredOperation(String operationName) {
            this.operationName = operationName;
            this.sample = Timer.start(meterRegistry);
        }
        
        @Override
        public void close() {
            Timer timer = timers.computeIfAbsent(operationName, 
                    name -> Timer.builder(name)
                            .description("Operation duration")
                            .register(meterRegistry));
            sample.stop(timer);
        }
    }
    
    public MonitoredOperation startOperation(String operationName) {
        counters.computeIfAbsent(operationName + ".started", 
                name -> Counter.builder(name).register(meterRegistry))
                .increment();
        
        return new MonitoredOperation(operationName);
    }
    
    public PerformanceReport generateReport(String operationName) {
        Timer timer = timers.get(operationName);
        if (timer == null) {
            return PerformanceReport.empty(operationName);
        }
        
        return PerformanceReport.builder()
                .operationName(operationName)
                .count(timer.count())
                .mean(Duration.ofNanos((long) timer.mean(TimeUnit.NANOSECONDS)))
                .max(Duration.ofNanos((long) timer.max(TimeUnit.NANOSECONDS)))
                .percentiles(Map.of(
                        0.5, Duration.ofNanos((long) timer.percentile(0.5, TimeUnit.NANOSECONDS)),
                        0.95, Duration.ofNanos((long) timer.percentile(0.95, TimeUnit.NANOSECONDS)),
                        0.99, Duration.ofNanos((long) timer.percentile(0.99, TimeUnit.NANOSECONDS))
                ))
                .build();
    }
}
```

### 8. Diagnostic REST API

```java
@RestController
@RequestMapping("/api/diagnostics")
@RequiredArgsConstructor
@Slf4j
public class DiagnosticController {
    private final DiagnosticService diagnosticService;
    private final StateInspector stateInspector;
    private final ExecutionTracer executionTracer;
    private final PerformanceMonitor performanceMonitor;
    
    @GetMapping("/health")
    public ResponseEntity<HealthReport> getHealth() {
        HealthReport report = HealthReport.builder()
                .status(HealthStatus.UP)
                .components(diagnosticService.getComponentHealth())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/snapshot")
    public ResponseEntity<SystemDiagnosticSnapshot> captureSnapshot() {
        log.info("Capturing system diagnostic snapshot");
        SystemDiagnosticSnapshot snapshot = diagnosticService.captureSystemSnapshot();
        return ResponseEntity.ok(snapshot);
    }
    
    @GetMapping("/state/{componentId}")
    public ResponseEntity<ComponentStateReport> inspectComponent(
            @PathVariable String componentId) {
        try {
            ComponentStateReport report = stateInspector.inspectComponent(componentId);
            return ResponseEntity.ok(report);
        } catch (ComponentNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/traces/active")
    public ResponseEntity<List<ExecutionTrace>> getActiveTraces() {
        return ResponseEntity.ok(executionTracer.getActiveTraces());
    }
    
    @GetMapping("/performance/{operationName}")
    public ResponseEntity<PerformanceReport> getPerformanceReport(
            @PathVariable String operationName) {
        PerformanceReport report = performanceMonitor.generateReport(operationName);
        return ResponseEntity.ok(report);
    }
    
    @PostMapping("/debug/enable")
    public ResponseEntity<Void> enableDebugMode(@RequestParam(defaultValue = "true") boolean enable) {
        diagnosticService.enableGlobalDebugMode(enable);
        return ResponseEntity.ok().build();
    }
}
```

### 9. Diagnostic Event System

```java
@Component
public class DiagnosticEventBus {
    private final Map<Class<?>, List<Consumer<?>>> subscribers = new ConcurrentHashMap<>();
    private final BlockingQueue<DiagnosticEvent> eventQueue = new LinkedBlockingQueue<>();
    private final ExecutorService eventProcessor;
    
    public DiagnosticEventBus() {
        this.eventProcessor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "diagnostic-event-processor");
            thread.setDaemon(true);
            return thread;
        });
        startEventProcessor();
    }
    
    public <T extends DiagnosticEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(handler);
    }
    
    public void publish(DiagnosticEvent event) {
        event.setTimestamp(Instant.now());
        event.setCorrelationId(MDC.get("correlationId"));
        
        try {
            eventQueue.offer(event, 100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.warn("Failed to publish diagnostic event: {}", event);
        }
    }
    
    private void startEventProcessor() {
        eventProcessor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    DiagnosticEvent event = eventQueue.take();
                    processEvent(event);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    private void processEvent(DiagnosticEvent event) {
        List<Consumer<?>> handlers = subscribers.get(event.getClass());
        if (handlers != null) {
            handlers.forEach(handler -> {
                try {
                    ((Consumer<DiagnosticEvent>) handler).accept(event);
                } catch (Exception e) {
                    log.error("Error processing diagnostic event", e);
                }
            });
        }
    }
}
```

### 10. AI-Specific Diagnostic Tools

```java
@Component
@Slf4j
public class AIDiagnosticAssistant {
    private final DiagnosticService diagnosticService;
    private final StateInspector stateInspector;
    
    /**
     * Generates a comprehensive diagnostic report optimized for AI analysis
     */
    public AIOptimizedDiagnosticReport generateAIReport(String problemDescription) {
        log.info("Generating AI-optimized diagnostic report for: {}", problemDescription);
        
        // Capture current system state
        SystemDiagnosticSnapshot snapshot = diagnosticService.captureSystemSnapshot();
        
        // Analyze recent errors
        List<ErrorContext> recentErrors = diagnosticService.getRecentErrors(10);
        
        // Identify anomalies
        List<Anomaly> anomalies = detectAnomalies(snapshot);
        
        // Generate suggestions
        List<DiagnosticSuggestion> suggestions = generateSuggestions(
                problemDescription, snapshot, recentErrors, anomalies);
        
        return AIOptimizedDiagnosticReport.builder()
                .problemDescription(problemDescription)
                .systemSnapshot(snapshot)
                .recentErrors(recentErrors)
                .detectedAnomalies(anomalies)
                .suggestions(suggestions)
                .relevantLogs(extractRelevantLogs(problemDescription))
                .generatedAt(Instant.now())
                .build();
    }
    
    /**
     * Interactive diagnostic session for AI
     */
    public DiagnosticSession startDiagnosticSession() {
        String sessionId = UUID.randomUUID().toString();
        
        return new DiagnosticSession(sessionId) {
            @Override
            public DiagnosticResponse query(String query) {
                // Process natural language diagnostic queries
                return processDiagnosticQuery(query);
            }
            
            @Override
            public void close() {
                // Cleanup session resources
                log.info("Closing diagnostic session: {}", sessionId);
            }
        };
    }
}
```

## Integration Guidelines

### 1. Component Registration

Every major component should implement `DiagnosticCapable`:

```java
@Component
public class MyService implements DiagnosticCapable {
    @PostConstruct
    public void init() {
        diagnosticService.registerComponent("myService", this);
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        return DiagnosticInfo.builder()
                .component("MyService")
                .states(Map.of(
                    "status", getStatus(),
                    "queueSize", getQueueSize(),
                    "lastProcessedTime", getLastProcessedTime()
                ))
                .build();
    }
}
```

### 2. Method Tracing

Use the `@Trace` annotation for important operations:

```java
@Trace("processOrder")
public OrderResult processOrder(Order order) {
    // Method implementation
}
```

### 3. Performance Monitoring

Wrap critical operations with performance monitoring:

```java
try (var operation = performanceMonitor.startOperation("order.processing")) {
    // Process order
}
```

### 4. State Inspection

Implement `StateInspectable` for stateful components:

```java
@Override
public Map<String, Object> inspectState() {
    return Map.of(
        "activeOrders", activeOrders.size(),
        "processingQueue", processingQueue.size(),
        "configuration", currentConfig
    );
}
```

## Benefits for AI Debugging

1. **Comprehensive Context**: AI can request full system snapshots
2. **Execution Tracing**: Follow operation flow with correlation IDs
3. **State History**: Understand how system state evolved
4. **Performance Insights**: Identify bottlenecks and anomalies
5. **Natural Language Queries**: AI can interact with diagnostic sessions
6. **Structured Error Information**: Detailed error contexts with suggestions

This infrastructure makes the Brobot Runner highly observable and debuggable, enabling AI assistants to quickly understand system behavior and diagnose issues.