# Migration Guide: ExecutionController Refactoring

This guide provides step-by-step instructions for refactoring the ExecutionController from a monolithic class with 7+ responsibilities into a clean, modular architecture following the Single Responsibility Principle.

## Overview

### Current State
- **File**: `ExecutionController.java`
- **Lines**: ~380
- **Responsibilities**: Thread management, execution, state management, timeouts, safety checks, status updates, logging, resource lifecycle

### Target State
- 6 focused classes, each with a single responsibility
- Full diagnostic capabilities for AI debugging
- Clean separation of concerns
- Improved testability

## Pre-Migration Checklist

- [ ] Create a feature branch: `feature/refactor-execution-controller`
- [ ] Ensure all tests are passing
- [ ] Document current behavior and edge cases
- [ ] Set up performance benchmarks
- [ ] Create rollback plan

## Step-by-Step Migration

### Phase 1: Create New Package Structure (Day 1)

1. Create the new package structure:
```
src/main/java/io/github/jspinak/brobot/runner/
└── execution/
    ├── core/
    │   ├── ExecutionService.java
    │   └── ExecutionContext.java
    ├── thread/
    │   ├── ExecutionThreadManager.java
    │   └── ThreadPoolMetrics.java
    ├── state/
    │   └── ExecutionStateManager.java (existing, enhance)
    ├── timeout/
    │   └── ExecutionTimeoutManager.java
    ├── safety/
    │   └── ExecutionSafetyService.java
    ├── diagnostics/
    │   ├── ExecutionDiagnostics.java
    │   └── DiagnosticInfo.java
    └── controller/
        └── ExecutionController.java (refactored)
```

2. Create base interfaces in a common package:
```java
// src/main/java/io/github/jspinak/brobot/runner/common/diagnostics/DiagnosticCapable.java
public interface DiagnosticCapable {
    DiagnosticInfo getDiagnosticInfo();
    void enableDiagnosticMode(boolean enabled);
}

// src/main/java/io/github/jspinak/brobot/runner/common/lifecycle/Lifecycle.java
public interface Lifecycle {
    void initialize();
    void shutdown();
    boolean isInitialized();
}
```

### Phase 2: Extract Thread Management (Day 2)

1. Create `ExecutionThreadManager`:

```java
package io.github.jspinak.brobot.runner.execution.thread;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class ExecutionThreadManager implements DiagnosticCapable, AutoCloseable {
    private static final int CORE_POOL_SIZE = 1;
    private static final String THREAD_NAME_PREFIX = "Brobot-Execution-";
    
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutor;
    private final ThreadPoolMetrics metrics;
    
    public ExecutionThreadManager() {
        this.executorService = createExecutorService();
        this.scheduledExecutor = Executors.newScheduledThreadPool(1, 
            new DiagnosticThreadFactory(THREAD_NAME_PREFIX + "Scheduler-"));
        this.metrics = new ThreadPoolMetrics(executorService);
        
        log.info("ExecutionThreadManager initialized with {} core threads", CORE_POOL_SIZE);
    }
    
    // Move thread creation logic from ExecutionController
    private ExecutorService createExecutorService() {
        return new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            CORE_POOL_SIZE,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new DiagnosticThreadFactory(THREAD_NAME_PREFIX),
            new DiagnosticRejectedExecutionHandler()
        );
    }
    
    public <T> CompletableFuture<T> submit(Callable<T> task, String taskDescription) {
        // Implementation from IMPLEMENTATION_EXAMPLES.md
    }
    
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(task, delay, unit);
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        // Implementation from IMPLEMENTATION_EXAMPLES.md
    }
    
    @Override
    @PreDestroy
    public void close() {
        log.info("Shutting down ExecutionThreadManager");
        executorService.shutdown();
        scheduledExecutor.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
            scheduledExecutor.shutdownNow();
        }
    }
}
```

2. Update `ExecutionController` to use `ExecutionThreadManager`:

```java
// In ExecutionController.java
public class ExecutionController {
    // Remove:
    // private final ExecutorService executorService;
    // private final ScheduledExecutorService timeoutScheduler;
    
    // Add:
    private final ExecutionThreadManager threadManager;
    
    // Update constructor
    public ExecutionController(ExecutionThreadManager threadManager, /*...*/) {
        this.threadManager = threadManager;
        // Remove executor service initialization
    }
    
    // Update methods to use threadManager
    private void executeTask(Runnable task) {
        // Old: executorService.submit(task);
        // New:
        threadManager.submit(() -> {
            task.run();
            return null;
        }, "user-task");
    }
}
```

3. Create comprehensive tests:

```java
@SpringBootTest
class ExecutionThreadManagerTest {
    @Autowired
    private ExecutionThreadManager threadManager;
    
    @Test
    void testTaskSubmission() {
        // Given
        String expectedResult = "test-result";
        
        // When
        CompletableFuture<String> future = threadManager.submit(
            () -> expectedResult, 
            "test-task"
        );
        
        // Then
        assertThat(future.join()).isEqualTo(expectedResult);
    }
    
    @Test
    void testDiagnostics() {
        // When
        DiagnosticInfo info = threadManager.getDiagnosticInfo();
        
        // Then
        assertThat(info.getComponent()).isEqualTo("ExecutionThreadManager");
        assertThat(info.getStates()).containsKey("poolSize");
    }
}
```

### Phase 3: Extract Timeout Management (Day 3)

1. Create `ExecutionTimeoutManager`:

```java
package io.github.jspinak.brobot.runner.execution.timeout;

@Component
@Slf4j
public class ExecutionTimeoutManager implements DiagnosticCapable {
    private final ExecutionThreadManager threadManager;
    private final Map<String, ScheduledFuture<?>> activeTimeouts = new ConcurrentHashMap<>();
    
    public ExecutionTimeoutManager(ExecutionThreadManager threadManager) {
        this.threadManager = threadManager;
    }
    
    public void scheduleTimeout(String executionId, Duration timeout, Runnable onTimeout) {
        log.debug("Scheduling timeout for execution {} after {}", executionId, timeout);
        
        ScheduledFuture<?> future = threadManager.schedule(
            () -> {
                log.warn("Execution {} timed out after {}", executionId, timeout);
                activeTimeouts.remove(executionId);
                onTimeout.run();
            },
            timeout.toMillis(),
            TimeUnit.MILLISECONDS
        );
        
        activeTimeouts.put(executionId, future);
    }
    
    public void cancelTimeout(String executionId) {
        ScheduledFuture<?> future = activeTimeouts.remove(executionId);
        if (future != null) {
            future.cancel(false);
            log.debug("Cancelled timeout for execution {}", executionId);
        }
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        return DiagnosticInfo.builder()
            .component("ExecutionTimeoutManager")
            .state("activeTimeouts", activeTimeouts.size())
            .state("timeoutIds", new ArrayList<>(activeTimeouts.keySet()))
            .build();
    }
}
```

2. Update `ExecutionController`:

```java
// Remove timeout scheduling logic
// Add dependency on ExecutionTimeoutManager
private final ExecutionTimeoutManager timeoutManager;

// In execution method:
String executionId = UUID.randomUUID().toString();
if (options.getTimeout() != null) {
    timeoutManager.scheduleTimeout(
        executionId,
        options.getTimeout(),
        () -> handleTimeout(executionId)
    );
}
```

### Phase 4: Extract Safety Service (Day 4)

1. Create `ExecutionSafetyService`:

```java
package io.github.jspinak.brobot.runner.execution.safety;

@Component
@Slf4j
public class ExecutionSafetyService implements DiagnosticCapable {
    private final SafetyManager safetyManager;
    private final AtomicLong safetyCheckCount = new AtomicLong();
    private final AtomicLong safetyViolations = new AtomicLong();
    
    public ExecutionSafetyService(SafetyManager safetyManager) {
        this.safetyManager = safetyManager;
    }
    
    public SafetyCheckResult performSafetyCheck(String operationName) {
        safetyCheckCount.incrementAndGet();
        
        try {
            boolean isSafe = safetyManager.checkSafety(operationName);
            
            if (!isSafe) {
                safetyViolations.incrementAndGet();
                log.warn("Safety check failed for operation: {}", operationName);
            }
            
            return SafetyCheckResult.builder()
                .operationName(operationName)
                .safe(isSafe)
                .timestamp(Instant.now())
                .build();
                
        } catch (Exception e) {
            log.error("Error during safety check", e);
            return SafetyCheckResult.failure(operationName, e);
        }
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        return DiagnosticInfo.builder()
            .component("ExecutionSafetyService")
            .state("totalChecks", safetyCheckCount.get())
            .state("violations", safetyViolations.get())
            .state("violationRate", calculateViolationRate())
            .build();
    }
}
```

### Phase 5: Create Core Execution Service (Day 5)

1. Create `ExecutionService`:

```java
package io.github.jspinak.brobot.runner.execution.core;

@Service
@Slf4j
public class ExecutionService implements DiagnosticCapable {
    private final ExecutionThreadManager threadManager;
    private final ExecutionTimeoutManager timeoutManager;
    private final ExecutionSafetyService safetyService;
    private final ExecutionStateManager stateManager;
    private final CorrelationIdProvider correlationIdProvider;
    
    // Implementation based on IMPLEMENTATION_EXAMPLES.md
    
    @Traceable
    @Timed(value = "execution.duration")
    public CompletableFuture<ExecutionResult> execute(
            String taskName, 
            Supplier<Object> task,
            ExecutionOptions options) {
        
        String correlationId = correlationIdProvider.generate();
        MDC.put("correlationId", correlationId);
        
        try {
            // Verify state
            Verify.state(!stateManager.hasActiveExecution(), 
                "Cannot start execution: another execution is active");
            
            // Safety check
            SafetyCheckResult safetyResult = safetyService.performSafetyCheck(taskName);
            if (!safetyResult.isSafe()) {
                throw new ExecutionSafetyException("Safety check failed", safetyResult);
            }
            
            // Create execution context
            ExecutionContext context = ExecutionContext.builder()
                .taskName(taskName)
                .correlationId(correlationId)
                .startTime(Instant.now())
                .options(options)
                .build();
            
            // Update state
            stateManager.startExecution(context);
            
            // Setup timeout if specified
            if (options.getTimeout() != null) {
                timeoutManager.scheduleTimeout(
                    context.getId(),
                    options.getTimeout(),
                    () -> handleTimeout(context)
                );
            }
            
            // Submit task
            return threadManager.submit(
                () -> executeWithContext(task, context),
                taskName
            ).whenComplete((result, error) -> {
                cleanupExecution(context);
            });
            
        } finally {
            MDC.remove("correlationId");
        }
    }
}
```

### Phase 6: Refactor Controller to Orchestrator (Day 6)

1. Refactor `ExecutionController` to be a thin orchestrator:

```java
package io.github.jspinak.brobot.runner.execution.controller;

@RestController
@RequestMapping("/api/execution")
@RequiredArgsConstructor
@Slf4j
public class ExecutionController {
    private final ExecutionService executionService;
    private final ExecutionDiagnostics diagnostics;
    
    @PostMapping("/start")
    public ResponseEntity<ExecutionResponse> startExecution(
            @RequestBody @Valid ExecutionRequest request) {
        
        log.info("Received execution request: {}", request);
        
        try {
            CompletableFuture<ExecutionResult> future = executionService.execute(
                request.getTaskName(),
                () -> loadAndExecuteTask(request),
                request.getOptions()
            );
            
            String executionId = UUID.randomUUID().toString();
            
            return ResponseEntity.ok(
                ExecutionResponse.started(executionId, future)
            );
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ExecutionResponse.error("Execution already in progress"));
        }
    }
    
    @GetMapping("/status/{executionId}")
    public ResponseEntity<ExecutionStatus> getStatus(@PathVariable String executionId) {
        return executionService.getStatus(executionId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/pause/{executionId}")
    public ResponseEntity<Void> pauseExecution(@PathVariable String executionId) {
        executionService.pause(executionId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/diagnostics")
    public DiagnosticReport getExecutionDiagnostics() {
        return diagnostics.generateFullReport();
    }
}
```

### Phase 7: Integration Testing (Day 7)

1. Create integration tests:

```java
@SpringBootTest
@AutoConfigureMockMvc
class ExecutionControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ExecutionService executionService;
    
    @Test
    void testCompleteExecutionFlow() throws Exception {
        // Given
        ExecutionRequest request = ExecutionRequest.builder()
            .taskName("test-task")
            .options(ExecutionOptions.builder()
                .timeout(Duration.ofSeconds(30))
                .build())
            .build();
        
        // When - Start execution
        MvcResult result = mockMvc.perform(post("/api/execution/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();
        
        ExecutionResponse response = objectMapper.readValue(
            result.getResponse().getContentAsString(), 
            ExecutionResponse.class
        );
        
        // Then - Check diagnostics
        mockMvc.perform(get("/api/execution/diagnostics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.components.ExecutionService").exists())
            .andExpect(jsonPath("$.components.ExecutionThreadManager").exists());
    }
}
```

### Phase 8: Migration and Deployment (Day 8-9)

1. **Parallel Running Strategy**:
   - Keep old `ExecutionController` as `LegacyExecutionController`
   - Use feature flag to switch between implementations
   - Monitor both implementations in production

2. **Feature Flag Implementation**:

```java
@Component
public class ExecutionControllerRouter {
    @Value("${features.new-execution-controller:false}")
    private boolean useNewController;
    
    @Autowired
    private ExecutionController newController;
    
    @Autowired
    private LegacyExecutionController legacyController;
    
    public ResponseEntity<ExecutionResponse> startExecution(ExecutionRequest request) {
        if (useNewController) {
            return newController.startExecution(request);
        } else {
            return legacyController.startExecution(request);
        }
    }
}
```

3. **Gradual Rollout**:
   - Week 1: 10% of traffic to new implementation
   - Week 2: 50% of traffic
   - Week 3: 100% of traffic
   - Week 4: Remove legacy code

### Phase 9: Cleanup and Documentation (Day 10)

1. **Remove Legacy Code**:
   - Delete `LegacyExecutionController`
   - Remove feature flags
   - Clean up unused dependencies

2. **Update Documentation**:
   - Update API documentation
   - Create architecture diagrams
   - Document new diagnostic endpoints

3. **Performance Validation**:
   - Compare metrics before/after
   - Ensure no performance regression
   - Validate memory usage

## Rollback Plan

If issues arise:

1. **Immediate Rollback** (< 1 hour):
   - Toggle feature flag to use legacy controller
   - No code deployment needed

2. **Code Rollback** (< 1 day):
   - Revert git commits
   - Deploy previous version
   - Restore legacy controller

3. **Data/State Issues**:
   - Execution state is transient
   - No data migration needed
   - Active executions will complete

## Success Criteria

- [ ] All unit tests passing (100% of existing tests)
- [ ] New test coverage > 80% for new classes
- [ ] No performance regression (< 5% difference)
- [ ] All diagnostic endpoints functional
- [ ] Zero production incidents during migration
- [ ] Team trained on new architecture

## Post-Migration Benefits

1. **Maintainability**: 6 focused classes vs 1 large class
2. **Testability**: Each component independently testable
3. **Diagnostics**: Full visibility into execution pipeline
4. **Extensibility**: Easy to add new features
5. **AI-Friendly**: Clear structure for AI debugging

This migration transforms a monolithic controller into a clean, modular architecture that follows SOLID principles and is optimized for both human and AI development.