# Migration Guide: SessionManager Refactoring

This guide provides detailed instructions for refactoring the SessionManager from a monolithic class handling 10+ responsibilities into a clean, modular architecture with clear separation of concerns.

## Overview

### Current State
- **File**: `SessionManager.java`
- **Lines**: ~543
- **Responsibilities**: Session lifecycle, persistence, state capture, scheduling, events, resource management, file I/O, JSON serialization, autosaving, history management

### Target State
- 6 focused components following SRP
- Repository pattern for persistence
- Event-driven architecture
- Full diagnostic capabilities
- Testable components with clear boundaries

## Architecture Overview

```
session/
├── core/
│   ├── SessionService.java (Business logic)
│   ├── Session.java (Domain model)
│   └── SessionFactory.java (Session creation)
├── repository/
│   ├── SessionRepository.java (Interface)
│   ├── FileSessionRepository.java (File implementation)
│   └── SessionSerializer.java (JSON handling)
├── state/
│   ├── SessionStateCapture.java (State management)
│   └── ApplicationStateProvider.java (Interface)
├── scheduling/
│   ├── SessionScheduler.java (Autosave)
│   └── ScheduledTask.java
├── events/
│   ├── SessionEventPublisher.java
│   └── SessionEvent.java
└── diagnostics/
    └── SessionDiagnostics.java
```

## Migration Steps

### Phase 1: Preparation (Day 1)

1. **Create Feature Branch**
```bash
git checkout -b feature/refactor-session-manager
```

2. **Analyze Current Usage**
```java
// Find all usages of SessionManager
// Document public API that needs to be maintained
// Identify all state that needs to be preserved
```

3. **Create Temporary Facade**
```java
/**
 * Temporary facade to maintain backward compatibility during migration
 */
@Component
@Deprecated
public class SessionManagerFacade {
    private final SessionService sessionService;
    private final SessionRepository repository;
    private final SessionScheduler scheduler;
    
    // Delegate all methods to new components
    public Session startNewSession(String projectName) {
        return sessionService.startSession(projectName, Collections.emptyMap());
    }
}
```

### Phase 2: Extract Domain Model (Day 2)

1. **Create Clean Domain Model**
```java
package io.github.jspinak.brobot.runner.session.core;

@Value
@Builder(toBuilder = true)
public class Session {
    String id;
    String projectName;
    SessionStatus status;
    Instant startTime;
    Instant endTime;
    Map<String, Object> context;
    String correlationId;
    
    // Business invariants
    public boolean isActive() {
        return status == SessionStatus.ACTIVE;
    }
    
    public boolean isExpired() {
        return status == SessionStatus.EXPIRED || 
               (endTime != null && endTime.isBefore(Instant.now()));
    }
    
    public Duration getDuration() {
        Instant end = endTime != null ? endTime : Instant.now();
        return Duration.between(startTime, end);
    }
    
    // State transitions
    public Session expire() {
        if (!isActive()) {
            throw new IllegalStateException(
                "Cannot expire session in status: " + status);
        }
        return toBuilder()
            .status(SessionStatus.EXPIRED)
            .endTime(Instant.now())
            .build();
    }
}

@Builder
@Value
public class SessionSummary {
    String id;
    String projectName;
    SessionStatus status;
    Instant startTime;
    Duration duration;
}
```

2. **Create Session Factory**
```java
@Component
public class SessionFactory {
    private final IdGenerator idGenerator;
    private final Clock clock;
    
    public Session createSession(String projectName, Map<String, Object> initialContext) {
        Validate.notBlank(projectName, "Project name cannot be blank");
        
        return Session.builder()
            .id(idGenerator.generateId())
            .projectName(projectName)
            .status(SessionStatus.ACTIVE)
            .startTime(clock.instant())
            .context(new HashMap<>(initialContext))
            .correlationId(MDC.get("correlationId"))
            .build();
    }
}
```

### Phase 3: Extract Repository Layer (Day 3)

1. **Define Repository Interface**
```java
package io.github.jspinak.brobot.runner.session.repository;

public interface SessionRepository {
    CompletableFuture<Void> save(Session session);
    CompletableFuture<Optional<Session>> findById(String sessionId);
    CompletableFuture<List<SessionSummary>> findAllSummaries();
    CompletableFuture<List<Session>> findByStatus(SessionStatus status);
    CompletableFuture<Boolean> delete(String sessionId);
    CompletableFuture<Void> deleteExpiredSessions(Duration olderThan);
}
```

2. **Implement File-based Repository**
```java
@Repository
@Slf4j
public class FileSessionRepository implements SessionRepository, DiagnosticCapable {
    private static final String SESSION_DIR = "sessions";
    private static final String SESSION_EXTENSION = ".session.json";
    
    private final Path sessionsPath;
    private final SessionSerializer serializer;
    private final FileSystemWatcher watcher;
    
    @PostConstruct
    public void initialize() {
        try {
            Files.createDirectories(sessionsPath);
            watcher.watch(sessionsPath, this::onFileChange);
            log.info("Session repository initialized at: {}", sessionsPath);
        } catch (IOException e) {
            throw new RepositoryInitializationException(
                "Failed to initialize session repository", e);
        }
    }
    
    @Override
    public CompletableFuture<Void> save(Session session) {
        return CompletableFuture.runAsync(() -> {
            String fileName = session.getId() + SESSION_EXTENSION;
            Path filePath = sessionsPath.resolve(fileName);
            
            try {
                // Validate session before saving
                ValidationResult validation = validator.validate(session);
                if (!validation.isValid()) {
                    throw new ValidationException(
                        "Session validation failed: " + validation.getErrors());
                }
                
                // Serialize to JSON
                String json = serializer.serialize(session);
                
                // Atomic write with temp file
                Path tempFile = filePath.resolveSibling(fileName + ".tmp");
                Files.writeString(tempFile, json, 
                    StandardOpenOption.CREATE, 
                    StandardOpenOption.TRUNCATE_EXISTING);
                Files.move(tempFile, filePath, 
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
                
                log.debug("Session saved: {} ({} bytes)", session.getId(), json.length());
                
            } catch (IOException e) {
                throw new RepositoryException(
                    String.format("Failed to save session %s: %s", 
                        session.getId(), e.getMessage()), e);
            }
        });
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        try {
            long sessionCount = Files.list(sessionsPath)
                .filter(p -> p.toString().endsWith(SESSION_EXTENSION))
                .count();
                
            long totalSize = Files.walk(sessionsPath)
                .filter(Files::isRegularFile)
                .mapToLong(this::getFileSize)
                .sum();
                
            return DiagnosticInfo.builder()
                .component("FileSessionRepository")
                .state("directory", sessionsPath.toAbsolutePath())
                .state("sessionCount", sessionCount)
                .state("totalSizeBytes", totalSize)
                .state("totalSizeMB", totalSize / (1024.0 * 1024.0))
                .state("writable", Files.isWritable(sessionsPath))
                .build();
                
        } catch (IOException e) {
            return DiagnosticInfo.error("FileSessionRepository", e);
        }
    }
}
```

3. **Create Session Serializer**
```java
@Component
public class SessionSerializer {
    private final ObjectMapper objectMapper;
    
    public SessionSerializer() {
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);
    }
    
    public String serialize(Session session) throws JsonProcessingException {
        return objectMapper.writeValueAsString(session);
    }
    
    public Session deserialize(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, Session.class);
    }
}
```

### Phase 4: Extract State Management (Day 4)

1. **Create State Capture Service**
```java
@Component
@Slf4j
public class SessionStateCapture implements DiagnosticCapable {
    private final List<ApplicationStateProvider> stateProviders;
    private final StateValidator validator;
    
    public SessionStateCapture(List<ApplicationStateProvider> stateProviders) {
        this.stateProviders = stateProviders;
        this.validator = new StateValidator();
    }
    
    public void captureState(Session session) {
        log.debug("Capturing state for session: {}", session.getId());
        
        Map<String, Object> capturedState = new HashMap<>();
        List<StateCaptureProblem> problems = new ArrayList<>();
        
        for (ApplicationStateProvider provider : stateProviders) {
            try {
                String stateKey = provider.getStateKey();
                Object state = provider.captureState();
                
                if (validator.isValid(state)) {
                    capturedState.put(stateKey, state);
                    log.trace("Captured state for {}: {} bytes", 
                        stateKey, estimateSize(state));
                } else {
                    problems.add(new StateCaptureProblem(stateKey, "Invalid state"));
                }
                
            } catch (Exception e) {
                log.warn("Failed to capture state from provider: {}", 
                    provider.getClass().getSimpleName(), e);
                problems.add(new StateCaptureProblem(
                    provider.getStateKey(), e.getMessage()));
            }
        }
        
        // Update session context
        session.getContext().put("capturedState", capturedState);
        session.getContext().put("captureTimestamp", Instant.now());
        
        if (!problems.isEmpty()) {
            session.getContext().put("captureProblems", problems);
        }
    }
    
    public void restoreState(Session session) {
        Map<String, Object> capturedState = 
            (Map<String, Object>) session.getContext().get("capturedState");
            
        if (capturedState == null) {
            log.warn("No captured state found for session: {}", session.getId());
            return;
        }
        
        for (ApplicationStateProvider provider : stateProviders) {
            String stateKey = provider.getStateKey();
            Object state = capturedState.get(stateKey);
            
            if (state != null) {
                try {
                    provider.restoreState(state);
                    log.debug("Restored state for: {}", stateKey);
                } catch (Exception e) {
                    log.error("Failed to restore state for: {}", stateKey, e);
                }
            }
        }
    }
}

// Interface for state providers
public interface ApplicationStateProvider {
    String getStateKey();
    Object captureState();
    void restoreState(Object state);
    boolean supportsStateType(Class<?> stateType);
}
```

### Phase 5: Extract Scheduling (Day 5)

1. **Create Session Scheduler**
```java
@Component
@Slf4j
public class SessionScheduler implements DiagnosticCapable, AutoCloseable {
    private final ScheduledExecutorService scheduler;
    private final SessionRepository repository;
    private final Map<String, ScheduledTask> scheduledTasks;
    
    public SessionScheduler(SessionRepository repository) {
        this.repository = repository;
        this.scheduler = Executors.newScheduledThreadPool(1, 
            new ThreadFactoryBuilder()
                .setNameFormat("session-scheduler-%d")
                .setDaemon(true)
                .build());
        this.scheduledTasks = new ConcurrentHashMap<>();
    }
    
    public void scheduleAutosave(Session session, Duration interval) {
        String taskId = "autosave-" + session.getId();
        
        // Cancel existing task if any
        cancelTask(taskId);
        
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(
            () -> autosaveSession(session),
            interval.toMillis(),
            interval.toMillis(),
            TimeUnit.MILLISECONDS
        );
        
        scheduledTasks.put(taskId, new ScheduledTask(taskId, future, "Autosave"));
        log.info("Scheduled autosave for session {} every {}", 
            session.getId(), interval);
    }
    
    private void autosaveSession(Session session) {
        try {
            MDC.put("sessionId", session.getId());
            log.trace("Autosaving session");
            
            repository.save(session)
                .exceptionally(throwable -> {
                    log.error("Autosave failed for session: {}", 
                        session.getId(), throwable);
                    return null;
                })
                .join();
                
        } finally {
            MDC.remove("sessionId");
        }
    }
    
    public void scheduleExpiration(Session session, Duration timeout) {
        String taskId = "expire-" + session.getId();
        
        ScheduledFuture<?> future = scheduler.schedule(
            () -> expireSession(session),
            timeout.toMillis(),
            TimeUnit.MILLISECONDS
        );
        
        scheduledTasks.put(taskId, new ScheduledTask(taskId, future, "Expiration"));
        log.info("Scheduled expiration for session {} after {}", 
            session.getId(), timeout);
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        return DiagnosticInfo.builder()
            .component("SessionScheduler")
            .state("scheduledTasks", scheduledTasks.size())
            .state("taskDetails", scheduledTasks.values().stream()
                .map(task -> Map.of(
                    "id", task.getId(),
                    "type", task.getType(),
                    "isDone", task.getFuture().isDone(),
                    "isCancelled", task.getFuture().isCancelled()
                ))
                .collect(Collectors.toList()))
            .state("threadPoolSize", 
                ((ThreadPoolExecutor) scheduler).getPoolSize())
            .build();
    }
}
```

### Phase 6: Create Session Service (Day 6)

1. **Implement Core Session Service**
```java
@Service
@Slf4j
@RequiredArgsConstructor
public class SessionService implements DiagnosticCapable {
    private final SessionFactory sessionFactory;
    private final SessionRepository repository;
    private final SessionStateCapture stateCapture;
    private final SessionScheduler scheduler;
    private final SessionEventPublisher eventPublisher;
    
    private final AtomicReference<Session> currentSession = new AtomicReference<>();
    
    /**
     * Starts a new session following these steps:
     * 1. Verify no active session exists
     * 2. Create new session
     * 3. Capture initial state
     * 4. Save to repository
     * 5. Schedule autosave
     * 6. Publish session started event
     */
    public Session startSession(String projectName, Map<String, Object> initialContext) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        try {
            log.info("[{}] Starting new session for project: {}", 
                correlationId, projectName);
            
            // Verify preconditions
            Session existing = currentSession.get();
            if (existing != null && existing.isActive()) {
                throw new SessionAlreadyActiveException(
                    String.format("Cannot start new session. Active session exists: %s (started %s)",
                        existing.getId(), existing.getStartTime())
                );
            }
            
            // Create session
            Session session = sessionFactory.createSession(projectName, initialContext);
            
            // Capture initial state
            stateCapture.captureState(session);
            
            // Save to repository
            repository.save(session)
                .exceptionally(throwable -> {
                    throw new SessionPersistenceException(
                        "Failed to save new session", throwable);
                })
                .join();
            
            // Update current session
            currentSession.set(session);
            
            // Schedule autosave
            Duration autosaveInterval = Duration.ofMinutes(5);
            scheduler.scheduleAutosave(session, autosaveInterval);
            
            // Schedule expiration if configured
            Duration sessionTimeout = getSessionTimeout();
            if (sessionTimeout != null) {
                scheduler.scheduleExpiration(session, sessionTimeout);
            }
            
            // Publish event
            eventPublisher.publishSessionStarted(session);
            
            log.info("[{}] Session started successfully: {}", 
                correlationId, session.getId());
            
            return session;
            
        } catch (Exception e) {
            log.error("[{}] Failed to start session", correlationId, e);
            throw new SessionException("Failed to start session: " + e.getMessage(), e);
            
        } finally {
            MDC.remove("correlationId");
        }
    }
    
    public Optional<Session> getCurrentSession() {
        return Optional.ofNullable(currentSession.get());
    }
    
    public void endSession() {
        Session session = currentSession.get();
        if (session == null) {
            log.warn("No active session to end");
            return;
        }
        
        try {
            log.info("Ending session: {}", session.getId());
            
            // Update session status
            Session endedSession = session.toBuilder()
                .status(SessionStatus.COMPLETED)
                .endTime(Instant.now())
                .build();
            
            // Final state capture
            stateCapture.captureState(endedSession);
            
            // Save final state
            repository.save(endedSession).join();
            
            // Cancel scheduled tasks
            scheduler.cancelSessionTasks(session.getId());
            
            // Clear current session
            currentSession.set(null);
            
            // Publish event
            eventPublisher.publishSessionEnded(endedSession);
            
        } catch (Exception e) {
            log.error("Error ending session", e);
            throw new SessionException("Failed to end session properly", e);
        }
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Session current = currentSession.get();
        
        Map<String, Object> states = new HashMap<>();
        states.put("hasActiveSession", current != null);
        
        if (current != null) {
            states.put("sessionId", current.getId());
            states.put("projectName", current.getProjectName());
            states.put("status", current.getStatus());
            states.put("duration", current.getDuration());
            states.put("isExpired", current.isExpired());
        }
        
        return DiagnosticInfo.builder()
            .component("SessionService")
            .states(states)
            .build();
    }
}
```

### Phase 7: Testing Strategy (Day 7)

1. **Unit Tests for Each Component**
```java
@ExtendWith(MockitoExtension.class)
class SessionServiceTest {
    @Mock
    private SessionRepository repository;
    
    @Mock
    private SessionStateCapture stateCapture;
    
    @Mock
    private SessionScheduler scheduler;
    
    @InjectMocks
    private SessionService sessionService;
    
    @Test
    @DisplayName("Should start new session when no active session exists")
    void testStartNewSession() {
        // Given
        String projectName = "test-project";
        when(repository.save(any())).thenReturn(CompletableFuture.completedFuture(null));
        
        // When
        Session session = sessionService.startSession(projectName, Map.of());
        
        // Then
        assertThat(session).isNotNull();
        assertThat(session.getProjectName()).isEqualTo(projectName);
        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        
        verify(repository).save(session);
        verify(stateCapture).captureState(session);
        verify(scheduler).scheduleAutosave(eq(session), any());
    }
    
    @Test
    @DisplayName("Should throw exception when active session exists")
    void testStartSessionWithActiveSession() {
        // Given
        Session activeSession = SessionTestDataBuilder.anActiveSession().build();
        sessionService.setCurrentSession(activeSession);
        
        // When/Then
        assertThatThrownBy(() -> 
            sessionService.startSession("new-project", Map.of()))
            .isInstanceOf(SessionAlreadyActiveException.class)
            .hasMessageContaining(activeSession.getId());
    }
}
```

2. **Integration Tests**
```java
@SpringBootTest
@Testcontainers
class SessionIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14");
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private SessionRepository repository;
    
    @Test
    void testCompleteSessionLifecycle() {
        // Start session
        Session session = sessionService.startSession("integration-test", Map.of());
        
        // Verify persisted
        Optional<Session> loaded = repository.findById(session.getId()).join();
        assertThat(loaded).isPresent();
        
        // End session
        sessionService.endSession();
        
        // Verify updated
        Session ended = repository.findById(session.getId()).join().orElseThrow();
        assertThat(ended.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(ended.getEndTime()).isNotNull();
    }
}
```

### Phase 8: Migration Execution (Day 8-9)

1. **Parallel Implementation**
```java
@Configuration
public class SessionConfiguration {
    @Bean
    @ConditionalOnProperty(name = "session.use-legacy", havingValue = "true")
    public SessionManager legacySessionManager() {
        return new SessionManager();
    }
    
    @Bean
    @ConditionalOnProperty(name = "session.use-legacy", havingValue = "false", matchIfMissing = true)
    public SessionManagerFacade sessionManagerFacade(
            SessionService sessionService,
            SessionRepository repository,
            SessionScheduler scheduler) {
        return new SessionManagerFacade(sessionService, repository, scheduler);
    }
}
```

2. **Data Migration** (if needed)
```java
@Component
@Slf4j
public class SessionDataMigration {
    
    @EventListener(ApplicationReadyEvent.class)
    public void migrateExistingSessions() {
        if (!needsMigration()) {
            return;
        }
        
        log.info("Starting session data migration");
        
        // Load legacy sessions
        List<LegacySession> legacySessions = loadLegacySessions();
        
        // Convert and save
        for (LegacySession legacy : legacySessions) {
            try {
                Session session = convertToNewFormat(legacy);
                repository.save(session).join();
                log.debug("Migrated session: {}", session.getId());
            } catch (Exception e) {
                log.error("Failed to migrate session: {}", legacy.getId(), e);
            }
        }
        
        markMigrationComplete();
    }
}
```

### Phase 9: Monitoring and Validation (Day 10)

1. **Add Metrics**
```java
@Component
public class SessionMetrics {
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void onSessionStarted(SessionStartedEvent event) {
        meterRegistry.counter("sessions.started", 
            "project", event.getSession().getProjectName()).increment();
    }
    
    @EventListener
    public void onSessionEnded(SessionEndedEvent event) {
        Session session = event.getSession();
        
        meterRegistry.counter("sessions.ended",
            "project", session.getProjectName(),
            "status", session.getStatus().toString()).increment();
            
        meterRegistry.timer("sessions.duration",
            "project", session.getProjectName())
            .record(session.getDuration());
    }
}
```

2. **Validation Checklist**
- [ ] All session operations work correctly
- [ ] Autosave functioning
- [ ] Session expiration working
- [ ] State capture/restore validated
- [ ] No performance degradation
- [ ] All tests passing

## Rollback Strategy

1. **Feature Flag Control**
   - Toggle `session.use-legacy=true`
   - No deployment needed

2. **Data Compatibility**
   - New format is backward compatible
   - Legacy reader can read new format

3. **Emergency Procedures**
   - Keep legacy code for 30 days
   - Monitor error rates
   - Have hotfix ready

## Benefits After Migration

1. **Separation of Concerns**: Each class has one responsibility
2. **Testability**: 90%+ test coverage achievable
3. **Extensibility**: Easy to add new storage backends
4. **Diagnostics**: Full visibility into session state
5. **Performance**: Async operations, better resource usage

This migration transforms the monolithic SessionManager into a clean, modular architecture that's easier to understand, test, and maintain.