# SessionManager Refactoring Migration Guide

## Overview

This guide provides step-by-step instructions for refactoring SessionManager (544 lines) following the successful pattern established with ExecutionController.

## Current State Analysis

### Identified Responsibilities
1. **Session Lifecycle** - Starting, ending, and managing session states
2. **Persistence** - Saving/loading sessions to/from disk
3. **State Management** - Capturing and restoring application state
4. **Autosave** - Periodic automatic saving
5. **Discovery** - Finding and listing available sessions
6. **Event Publishing** - Notifying listeners of session changes
7. **JSON Handling** - Serialization/deserialization
8. **Resource Management** - AutoCloseable implementation

### Current Dependencies
- AutomationProjectManager
- EventBus
- ObjectMapper (Jackson)
- ScheduledExecutorService
- File I/O operations

## Target Architecture

```
┌──────────────────────────────────────────────┐
│         SessionManager (Orchestrator)        │
│                 ~100-150 lines               │
└─────────────────┬────────────────────────────┘
                  │ delegates to
     ┌────────────┴────────────┬────────────┬────────────┬────────────┐
     ▼                         ▼            ▼            ▼            ▼
┌─────────────┐   ┌─────────────────┐  ┌──────────┐  ┌──────────┐  ┌─────────────┐
│ Lifecycle   │   │  Persistence    │  │  State   │  │ Autosave │  │ Discovery   │
│  Service    │   │    Service      │  │ Service  │  │ Service  │  │  Service    │
│  ~100 lines │   │   ~120 lines    │  │ ~80 lines│  │ ~80 lines│  │  ~100 lines │
└─────────────┘   └─────────────────┘  └──────────┘  └──────────┘  └─────────────┘
```

## Migration Steps

### Phase 1: Create Common Infrastructure

#### 1.1 Create Session Context
```java
package io.github.jspinak.brobot.runner.session.context;

@Builder
@Getter
public class SessionContext {
    private final String sessionId;
    private final String sessionName;
    private final LocalDateTime startTime;
    private final SessionOptions options;
    private final Map<String, Object> metadata;
}
```

#### 1.2 Create Session Options
```java
package io.github.jspinak.brobot.runner.session.context;

@Builder
@Getter
public class SessionOptions {
    @Builder.Default
    private final boolean autosaveEnabled = true;
    
    @Builder.Default
    private final Duration autosaveInterval = Duration.ofMinutes(5);
    
    @Builder.Default
    private final int maxSessionHistory = 10;
    
    @Builder.Default
    private final boolean captureScreenshots = true;
}
```

### Phase 2: Extract Services

#### 2.1 SessionLifecycleService
```java
package io.github.jspinak.brobot.runner.session.lifecycle;

@Service
@Slf4j
public class SessionLifecycleService implements DiagnosticCapable {
    
    public SessionContext startSession(String sessionName, SessionOptions options) {
        // Extract session starting logic
    }
    
    public void endSession(String sessionId) {
        // Extract session ending logic
    }
    
    public boolean isSessionActive(String sessionId) {
        // Extract session status check
    }
    
    public SessionTransition transitionTo(String sessionId, SessionState newState) {
        // Extract state transition logic
    }
}
```

#### 2.2 SessionPersistenceService
```java
package io.github.jspinak.brobot.runner.session.persistence;

@Service
@Slf4j
public class SessionPersistenceService implements DiagnosticCapable {
    
    public void saveSession(Session session, Path targetPath) {
        // Extract save logic
    }
    
    public Session loadSession(Path sessionPath) {
        // Extract load logic
    }
    
    public void deleteSession(String sessionId) {
        // Extract delete logic
    }
    
    public Path getSessionPath(String sessionId) {
        // Extract path resolution logic
    }
}
```

#### 2.3 SessionStateService
```java
package io.github.jspinak.brobot.runner.session.state;

@Service
@Slf4j
public class SessionStateService implements DiagnosticCapable {
    
    public ApplicationState captureCurrentState() {
        // Extract state capture logic
    }
    
    public void restoreState(ApplicationState state) {
        // Extract state restoration logic
    }
    
    public StateSnapshot createSnapshot(String description) {
        // Extract snapshot creation
    }
}
```

#### 2.4 SessionAutosaveService
```java
package io.github.jspinak.brobot.runner.session.autosave;

@Service
@Slf4j
public class SessionAutosaveService implements DiagnosticCapable {
    
    public void enableAutosave(SessionContext context, Consumer<Session> saveHandler) {
        // Extract autosave scheduling
    }
    
    public void disableAutosave(String sessionId) {
        // Extract autosave cancellation
    }
    
    public AutosaveStatus getStatus(String sessionId) {
        // Extract status checking
    }
}
```

#### 2.5 SessionDiscoveryService
```java
package io.github.jspinak.brobot.runner.session.discovery;

@Service
@Slf4j
public class SessionDiscoveryService implements DiagnosticCapable {
    
    public List<SessionSummary> listAvailableSessions() {
        // Extract session listing
    }
    
    public Optional<SessionSummary> findSession(String sessionId) {
        // Extract session search
    }
    
    public List<SessionSummary> findSessionsByDate(LocalDate date) {
        // Extract date-based search
    }
}
```

### Phase 3: Refactor SessionManager

```java
@Component
@Slf4j
public class SessionManager implements AutoCloseable, DiagnosticCapable {
    
    private final SessionLifecycleService lifecycleService;
    private final SessionPersistenceService persistenceService;
    private final SessionStateService stateService;
    private final SessionAutosaveService autosaveService;
    private final SessionDiscoveryService discoveryService;
    private final EventBus eventBus;
    
    // Current session tracking
    private final AtomicReference<SessionContext> currentSession = new AtomicReference<>();
    
    @Autowired
    public SessionManager(/* inject all services */) {
        // Initialize
    }
    
    public SessionContext startNewSession(String sessionName) {
        SessionContext context = lifecycleService.startSession(sessionName, SessionOptions.defaultOptions());
        currentSession.set(context);
        
        if (context.getOptions().isAutosaveEnabled()) {
            autosaveService.enableAutosave(context, this::saveCurrentSession);
        }
        
        eventBus.publish(new SessionStartedEvent(context));
        return context;
    }
    
    public void endCurrentSession() {
        SessionContext context = currentSession.get();
        if (context != null) {
            saveCurrentSession();
            autosaveService.disableAutosave(context.getSessionId());
            lifecycleService.endSession(context.getSessionId());
            currentSession.set(null);
            eventBus.publish(new SessionEndedEvent(context));
        }
    }
    
    // Other thin orchestration methods...
}
```

### Phase 4: Add Tests

1. **SessionLifecycleServiceTest** - Test state transitions, lifecycle events
2. **SessionPersistenceServiceTest** - Test save/load operations, file handling
3. **SessionStateServiceTest** - Test state capture/restore
4. **SessionAutosaveServiceTest** - Test scheduling, cancellation
5. **SessionDiscoveryServiceTest** - Test search functionality
6. **SessionManagerTest** - Test orchestration, integration

### Phase 5: Migration Execution

1. **Create feature branch**: `feature/refactor-session-manager`
2. **Implement services**: One at a time, with tests
3. **Update SessionManager**: Gradually delegate to services
4. **Ensure compatibility**: Keep public API unchanged
5. **Run all tests**: Verify nothing breaks
6. **Performance test**: Ensure no degradation
7. **Code review**: Get team feedback
8. **Merge**: When all tests pass

## Benefits

1. **Reduced Complexity**: From 544 lines to ~100-150 lines
2. **Single Responsibility**: Each service has one clear purpose
3. **Improved Testability**: Focused unit tests for each service
4. **Better Maintainability**: Changes isolated to relevant service
5. **Enhanced Reusability**: Services can be used independently

## Risks and Mitigation

1. **Risk**: Breaking existing functionality
   - **Mitigation**: Comprehensive tests before and after

2. **Risk**: Performance degradation
   - **Mitigation**: Benchmark before and after

3. **Risk**: Increased complexity from more classes
   - **Mitigation**: Clear documentation and naming

## Success Metrics

- [ ] All existing tests pass
- [ ] New tests provide >90% coverage
- [ ] Cyclomatic complexity reduced by >50%
- [ ] No performance degradation
- [ ] Team approval in code review