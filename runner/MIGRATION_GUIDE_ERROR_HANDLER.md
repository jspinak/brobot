# ErrorHandler Refactoring Migration Guide

## Overview

This guide documents the refactoring of ErrorHandler (488 lines) from a monolithic error management class into a distributed service architecture following the Single Responsibility Principle.

## Current State Analysis

### Identified Responsibilities

1. **Error Processing** - Executing error processors in sequence
2. **Strategy Management** - Registering and finding appropriate error strategies
3. **Error History** - Recording and managing error history
4. **Context Enrichment** - Adding system state to error context
5. **Statistics Tracking** - Maintaining error metrics and counts
6. **Recovery Execution** - Running recovery actions
7. **Error Categorization** - Determining error categories
8. **Default Strategies** - Providing built-in error handling strategies

### Current Dependencies

- EventBus - For publishing error events
- ErrorHistory - Internal class for tracking errors
- IErrorProcessor - Interface for error processors
- IErrorStrategy - Interface for error strategies
- ErrorContext - Error context information
- ErrorResult - Error handling results

### Complexity Metrics

- Total Lines: 488
- Methods: 11 public, 8 private
- Inner Classes: 11 (2 processors, 9 strategies)
- Responsibilities: 8+ distinct concerns

## Target Architecture

```
┌──────────────────────────────────────────────┐
│         ErrorHandler (Orchestrator)          │
│               ~100-150 lines                 │
└─────────────────┬────────────────────────────┘
                  │ delegates to
     ┌────────────┴────────────┬────────────┬────────────┬────────────┐
     ▼                         ▼            ▼            ▼            ▼
┌─────────────┐   ┌─────────────────┐  ┌──────────┐  ┌──────────┐  ┌─────────────┐
│ Processing  │   │    Strategy     │  │ History  │  │Enrichment│  │ Statistics  │
│  Service    │   │    Service      │  │ Service  │  │ Service  │  │  Service    │
│  ~80 lines  │   │   ~150 lines    │  │ ~100 lines│ │ ~80 lines│  │  ~100 lines │
└─────────────┘   └─────────────────┘  └──────────┘  └──────────┘  └─────────────┘
```

## Migration Steps

### Phase 1: Create Service Infrastructure

#### 1.1 Create Common Package Structure
```
errorhandling/
├── ErrorHandler.java (orchestrator)
├── processing/
│   └── ErrorProcessingService.java
├── strategy/
│   ├── ErrorStrategyService.java
│   └── strategies/
│       ├── DefaultErrorStrategy.java
│       ├── ApplicationExceptionStrategy.java
│       └── ... (other strategies)
├── history/
│   └── ErrorHistoryService.java
├── enrichment/
│   └── ErrorEnrichmentService.java
└── statistics/
    └── ErrorStatisticsService.java
```

#### 1.2 Create Diagnostic Infrastructure
All services will implement `DiagnosticCapable` for consistent monitoring.

### Phase 2: Extract Services

#### 2.1 ErrorProcessingService
**Responsibilities:**
- Execute error processors in sequence
- Handle processor failures gracefully
- Manage processor registration

**Key Methods:**
```java
- processError(Throwable error, ErrorContext context)
- registerProcessor(IErrorProcessor processor)
- getProcessors(): List<IErrorProcessor>
```

#### 2.2 ErrorStrategyService
**Responsibilities:**
- Register error strategies by exception type
- Find appropriate strategy for an error
- Provide default strategies
- Execute strategies and handle results

**Key Methods:**
```java
- registerStrategy(Class<? extends Throwable> errorType, IErrorStrategy strategy)
- findStrategy(Throwable error): IErrorStrategy
- executeStrategy(Throwable error, ErrorContext context): ErrorResult
- registerDefaultStrategies()
```

#### 2.3 ErrorHistoryService
**Responsibilities:**
- Record error occurrences
- Track error frequency
- Provide recent error history
- Clear history when needed

**Key Methods:**
```java
- record(Throwable error, ErrorContext context)
- getRecentErrors(int count): List<ErrorRecord>
- getMostFrequentErrors(int count): List<ErrorFrequency>
- clear()
```

#### 2.4 ErrorEnrichmentService
**Responsibilities:**
- Add system state to error context
- Capture CPU, memory, thread information
- Categorize errors
- Generate error IDs

**Key Methods:**
```java
- enrichContext(ErrorContext context): ErrorContext
- categorizeError(Throwable error): ErrorCategory
- captureSystemState(): SystemState
```

#### 2.5 ErrorStatisticsService
**Responsibilities:**
- Track total error counts
- Maintain category-wise statistics
- Generate statistical reports
- Reset statistics

**Key Methods:**
```java
- recordError(ErrorContext context)
- getStatistics(): ErrorStatistics
- getErrorsByCategory(): Map<ErrorCategory, Long>
- reset()
```

### Phase 3: Refactor ErrorHandler

Transform ErrorHandler into a thin orchestrator that:
1. Coordinates service interactions
2. Implements the error handling workflow
3. Manages recovery action execution
4. Provides backward-compatible API

## Implementation Pattern

### Service Template
```java
@Slf4j
@Service
public class Error[X]Service implements DiagnosticCapable {
    
    // Thread-safe collections
    private final ConcurrentHashMap<...> data = new ConcurrentHashMap<>();
    
    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    // Core functionality methods
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        // Return service state
    }
    
    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }
    
    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);
    }
}
```

### Orchestrator Pattern
```java
@Component
public class ErrorHandler implements DiagnosticCapable {
    
    private final ErrorProcessingService processingService;
    private final ErrorStrategyService strategyService;
    private final ErrorHistoryService historyService;
    private final ErrorEnrichmentService enrichmentService;
    private final ErrorStatisticsService statisticsService;
    
    public ErrorResult handleError(Throwable error, ErrorContext context) {
        // 1. Enrich context
        ErrorContext enriched = enrichmentService.enrichContext(context);
        
        // 2. Record statistics
        statisticsService.recordError(enriched);
        
        // 3. Record history
        historyService.record(error, enriched);
        
        // 4. Process through processors
        processingService.processError(error, enriched);
        
        // 5. Execute strategy
        ErrorResult result = strategyService.executeStrategy(error, enriched);
        
        // 6. Handle recovery if needed
        if (result.isRecoverable() && result.getRecoveryAction() != null) {
            executeRecovery(result, enriched);
        }
        
        return result;
    }
}
```

## Migration Checklist

- [ ] Create service package structure
- [ ] Extract ErrorProcessingService
- [ ] Extract ErrorStrategyService
- [ ] Extract ErrorHistoryService
- [ ] Extract ErrorEnrichmentService
- [ ] Extract ErrorStatisticsService
- [ ] Move strategy implementations to separate files
- [ ] Refactor ErrorHandler to orchestrator
- [ ] Update Spring configuration
- [ ] Write unit tests for each service
- [ ] Write integration tests
- [ ] Update documentation

## Testing Strategy

### Unit Tests
Each service should have comprehensive unit tests covering:
- Normal operation
- Error cases
- Thread safety
- Diagnostic capabilities

### Integration Tests
- Full error handling workflow
- Recovery action execution
- Event publishing
- Statistics accuracy

## Benefits

1. **Single Responsibility**: Each service has one clear purpose
2. **Testability**: Services can be tested in isolation
3. **Maintainability**: Changes are localized to relevant service
4. **Extensibility**: Easy to add new processors or strategies
5. **Thread Safety**: Each service manages its own concurrency
6. **Monitoring**: Built-in diagnostics for each service

## Risks and Mitigation

1. **Risk**: Breaking existing error handling
   - **Mitigation**: Maintain API compatibility in orchestrator

2. **Risk**: Performance impact from service calls
   - **Mitigation**: Services are lightweight, no remote calls

3. **Risk**: Complex dependency injection
   - **Mitigation**: Spring handles injection automatically

## Success Criteria

- All existing tests pass
- Each service under 150 lines
- 80%+ test coverage per service
- No performance degradation
- Improved error handling visibility through diagnostics