# Concrete Implementation Examples for Brobot Runner Refactoring

This document provides concrete, working examples of the patterns described in the refactoring strategy. Each example is designed to be AI-friendly and demonstrates best practices for testability and debugging.

## 1. ExecutionController Refactoring Example

### Current State (Violates SRP)
```java
public class ExecutionController implements AutoCloseable {
    // Too many responsibilities: threading, execution, state, timeout, safety, logging
    private final ExecutorService executorService;
    private final ScheduledExecutorService timeoutScheduler;
    private final SafetyManager safetyManager;
    private final ExecutionStatusManager statusManager;
    private volatile boolean paused = false;
    // ... 380+ lines of mixed concerns
}
```

### Refactored Implementation

#### 1.1 Core Interfaces
```java
// Base diagnostic interface for AI debugging
public interface DiagnosticCapable {
    DiagnosticInfo getDiagnosticInfo();
    void enableDiagnosticMode(boolean enabled);
}

// Behavioral contract interface
public interface BehavioralContract {
    List<String> getInvariants();
    Map<String, List<String>> getStateTransitions();
    List<String> getPreconditions();
    List<String> getPostconditions();
}
```

#### 1.2 ExecutionService (Core Logic)
```java
@Service
@Slf4j
public class ExecutionService implements DiagnosticCapable, BehavioralContract {
    private static final String EXECUTION_TIMEOUT_REASON = "Execution timeout prevents infinite loops";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(30);
    
    private final AtomicReference<ExecutionContext> currentContext = new AtomicReference<>();
    private final CorrelationIdProvider correlationIdProvider;
    
    /**
     * Behavioral Contract:
     * - Only one execution can be active at a time
     * - Executions must complete or timeout
     * - All executions are traced with correlation IDs
     */
    @Override
    public List<String> getInvariants() {
        return List.of(
            "At most one active execution",
            "Every execution has a unique correlation ID",
            "Completed executions cannot be restarted"
        );
    }
    
    @Traceable
    @Timed(value = "execution.duration", description = "Execution duration")
    public CompletableFuture<ExecutionResult> execute(
            String taskName, 
            Supplier<Object> task,
            ExecutionOptions options) {
        
        String correlationId = correlationIdProvider.generate();
        MDC.put("correlationId", correlationId);
        
        log.info("Starting execution: task={}, correlationId={}, timeout={}", 
                taskName, correlationId, options.getTimeout());
        
        // Verification point for AI debugging
        Verify.state(currentContext.get() == null, 
                "Cannot start execution: another execution is active");
        
        ExecutionContext context = ExecutionContext.builder()
                .taskName(taskName)
                .correlationId(correlationId)
                .startTime(Instant.now())
                .options(options)
                .build();
        
        currentContext.set(context);
        
        try {
            return doExecute(task, context);
        } finally {
            currentContext.set(null);
            MDC.remove("correlationId");
        }
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        ExecutionContext context = currentContext.get();
        return DiagnosticInfo.builder()
                .component("ExecutionService")
                .state("hasActiveExecution", context != null)
                .state("activeTask", context != null ? context.getTaskName() : "none")
                .state("correlationId", context != null ? context.getCorrelationId() : "none")
                .state("runningTime", context != null ? 
                        Duration.between(context.getStartTime(), Instant.now()) : null)
                .timestamp(Instant.now())
                .build();
    }
}
```

#### 1.3 ExecutionThreadManager
```java
@Component
@Slf4j
public class ExecutionThreadManager implements DiagnosticCapable, AutoCloseable {
    private static final int CORE_POOL_SIZE = 1;
    private static final String THREAD_NAME_PREFIX = "Brobot-Execution-";
    
    private final ExecutorService executorService;
    private final ThreadPoolMetrics metrics;
    
    public ExecutionThreadManager() {
        this.executorService = createExecutorService();
        this.metrics = new ThreadPoolMetrics(executorService);
    }
    
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
        log.debug("Submitting task: {}", taskDescription);
        
        CompletableFuture<T> future = new CompletableFuture<>();
        
        executorService.submit(() -> {
            String originalThreadName = Thread.currentThread().getName();
            try {
                // Add task context to thread name for debugging
                Thread.currentThread().setName(originalThreadName + " [" + taskDescription + "]");
                T result = task.call();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(new ExecutionException(
                        String.format("Task '%s' failed: %s", taskDescription, e.getMessage()), e));
            } finally {
                Thread.currentThread().setName(originalThreadName);
            }
        });
        
        return future;
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        return DiagnosticInfo.builder()
                .component("ExecutionThreadManager")
                .state("poolSize", metrics.getPoolSize())
                .state("activeThreads", metrics.getActiveCount())
                .state("queuedTasks", metrics.getQueueSize())
                .state("completedTasks", metrics.getCompletedTaskCount())
                .state("isShutdown", executorService.isShutdown())
                .build();
    }
    
    // Custom thread factory for better debugging
    private static class DiagnosticThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger counter = new AtomicInteger();
        
        DiagnosticThreadFactory(String prefix) {
            this.prefix = prefix;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(prefix + counter.incrementAndGet());
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t, e) -> 
                    log.error("Uncaught exception in thread {}: {}", t.getName(), e.getMessage(), e));
            return thread;
        }
    }
}
```

#### 1.4 ExecutionController (Thin Orchestrator)
```java
@RestController
@RequestMapping("/api/execution")
@RequiredArgsConstructor
@Slf4j
public class ExecutionController {
    private final ExecutionService executionService;
    private final ExecutionStateManager stateManager;
    private final ExecutionDiagnostics diagnostics;
    
    @PostMapping("/start")
    public ResponseEntity<ExecutionResponse> startExecution(@RequestBody ExecutionRequest request) {
        log.info("Received execution request: {}", request);
        
        try {
            CompletableFuture<ExecutionResult> future = executionService.execute(
                    request.getTaskName(),
                    () -> executeTask(request),
                    request.getOptions()
            );
            
            String executionId = UUID.randomUUID().toString();
            stateManager.trackExecution(executionId, future);
            
            return ResponseEntity.ok(ExecutionResponse.started(executionId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ExecutionResponse.error("Execution already in progress"));
        }
    }
    
    @GetMapping("/diagnostics")
    public DiagnosticReport getExecutionDiagnostics() {
        return diagnostics.generateFullReport();
    }
}
```

## 2. SessionManager Refactoring Example

### Current State (Mixed Responsibilities)
```java
public class SessionManager {
    // Mixes: lifecycle, persistence, state capture, scheduling, events
    private Session currentSession;
    private final ScheduledExecutorService scheduler;
    private void saveSession() { /* File I/O */ }
    private void loadSession() { /* JSON parsing */ }
    private void captureState() { /* State management */ }
    // ... 543+ lines
}
```

### Refactored Implementation

#### 2.1 Session Repository Pattern
```java
public interface SessionRepository {
    CompletableFuture<Void> save(Session session);
    CompletableFuture<Optional<Session>> load(String sessionId);
    CompletableFuture<List<SessionSummary>> listSessions();
    CompletableFuture<Boolean> delete(String sessionId);
}

@Repository
@Slf4j
public class FileSessionRepository implements SessionRepository, DiagnosticCapable {
    private static final String SESSION_FILE_EXTENSION = ".session.json";
    private static final Path SESSIONS_DIR = Paths.get("sessions");
    
    private final ObjectMapper objectMapper;
    private final SessionValidator validator;
    
    @Override
    public CompletableFuture<Void> save(Session session) {
        return CompletableFuture.runAsync(() -> {
            String correlationId = MDC.get("correlationId");
            log.debug("[{}] Saving session: {}", correlationId, session.getId());
            
            try {
                // Validate before saving
                ValidationResult validation = validator.validate(session);
                if (!validation.isValid()) {
                    throw new SessionValidationException(
                            "Session validation failed", validation.getErrors());
                }
                
                Path sessionFile = SESSIONS_DIR.resolve(session.getId() + SESSION_FILE_EXTENSION);
                Files.createDirectories(SESSIONS_DIR);
                
                String json = objectMapper.writeValueAsString(session);
                Files.writeString(sessionFile, json, StandardOpenOption.CREATE, 
                        StandardOpenOption.TRUNCATE_EXISTING);
                
                log.info("[{}] Session saved successfully: {} ({} bytes)", 
                        correlationId, session.getId(), json.length());
                
            } catch (IOException e) {
                throw new SessionPersistenceException(
                        String.format("Failed to save session %s to %s: %s", 
                                session.getId(), SESSIONS_DIR, e.getMessage()), e);
            }
        });
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        try {
            long sessionCount = Files.list(SESSIONS_DIR)
                    .filter(p -> p.toString().endsWith(SESSION_FILE_EXTENSION))
                    .count();
            
            long totalSize = Files.list(SESSIONS_DIR)
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0;
                        }
                    }).sum();
            
            return DiagnosticInfo.builder()
                    .component("FileSessionRepository")
                    .state("sessionsDirectory", SESSIONS_DIR.toAbsolutePath())
                    .state("sessionCount", sessionCount)
                    .state("totalSizeBytes", totalSize)
                    .state("isWritable", Files.isWritable(SESSIONS_DIR))
                    .build();
        } catch (IOException e) {
            return DiagnosticInfo.error("FileSessionRepository", e);
        }
    }
}
```

#### 2.2 Session Service (Business Logic)
```java
@Service
@Slf4j
@RequiredArgsConstructor
public class SessionService implements DiagnosticCapable {
    private final SessionRepository repository;
    private final SessionStateCapture stateCapture;
    private final SessionEventPublisher eventPublisher;
    private final AtomicReference<Session> currentSession = new AtomicReference<>();
    
    /**
     * Creates a new session with full traceability.
     * 
     * Preconditions:
     * - No active session exists
     * - Project name is not null or empty
     * 
     * Postconditions:
     * - New session is active
     * - Session is persisted
     * - Session started event is published
     */
    public Session startSession(String projectName, Map<String, Object> initialContext) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        try {
            log.info("[{}] Starting new session for project: {}", correlationId, projectName);
            
            // Precondition checks with helpful error messages
            Verify.state(currentSession.get() == null, 
                    "Cannot start session: Active session exists with ID: %s", 
                    currentSession.get() != null ? currentSession.get().getId() : "unknown");
            
            Verify.notBlank(projectName, "Project name cannot be null or empty");
            
            Session session = Session.builder()
                    .id(UUID.randomUUID().toString())
                    .projectName(projectName)
                    .startTime(Instant.now())
                    .status(SessionStatus.ACTIVE)
                    .context(initialContext)
                    .correlationId(correlationId)
                    .build();
            
            // Capture initial state
            stateCapture.captureState(session);
            
            // Save to repository
            repository.save(session).join();
            
            // Update current session
            currentSession.set(session);
            
            // Publish event
            eventPublisher.publishSessionStarted(session);
            
            log.info("[{}] Session started successfully: {}", correlationId, session.getId());
            
            return session;
            
        } finally {
            MDC.remove("correlationId");
        }
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Session current = currentSession.get();
        return DiagnosticInfo.builder()
                .component("SessionService")
                .state("hasActiveSession", current != null)
                .state("sessionId", current != null ? current.getId() : null)
                .state("projectName", current != null ? current.getProjectName() : null)
                .state("sessionAge", current != null ? 
                        Duration.between(current.getStartTime(), Instant.now()) : null)
                .state("sessionStatus", current != null ? current.getStatus() : null)
                .build();
    }
}
```

## 3. UI Panel Refactoring with View Model

### Current State (UI Mixed with Business Logic)
```java
public class ConfigurationPanel extends VBox {
    // Direct dependencies on services, business logic in UI
    private final ProjectManager projectManager;
    private final ValidationService validationService;
    private void loadConfiguration() { /* Business logic */ }
    private void validateAndSave() { /* More business logic */ }
}
```

### Refactored Implementation

#### 3.1 View Model Interface
```java
public interface ConfigurationViewModel extends DiagnosticCapable {
    // Observable properties for UI binding
    ReadOnlyStringProperty projectPathProperty();
    ReadOnlyStringProperty configurationStatusProperty();
    ReadOnlyBooleanProperty loadingProperty();
    ReadOnlyBooleanProperty validProperty();
    ObservableList<ValidationMessage> validationMessages();
    
    // Commands (UI actions)
    Command loadConfigurationCommand();
    Command saveConfigurationCommand();
    Command browseProjectCommand();
    Command validateCommand();
    
    // Diagnostic info for debugging
    @Override
    default DiagnosticInfo getDiagnosticInfo() {
        return DiagnosticInfo.builder()
                .component("ConfigurationViewModel")
                .state("projectPath", projectPathProperty().get())
                .state("status", configurationStatusProperty().get())
                .state("isLoading", loadingProperty().get())
                .state("isValid", validProperty().get())
                .state("validationMessageCount", validationMessages().size())
                .build();
    }
}
```

#### 3.2 View Model Implementation
```java
@Component
@Slf4j
public class ConfigurationViewModelImpl implements ConfigurationViewModel {
    // Observable properties
    private final StringProperty projectPath = new SimpleStringProperty("");
    private final StringProperty configurationStatus = new SimpleStringProperty("Not loaded");
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    private final ObservableList<ValidationMessage> validationMessages = 
            FXCollections.observableArrayList();
    
    // Dependencies (services)
    private final ConfigurationService configService;
    private final ValidationService validationService;
    private final EventBus eventBus;
    
    // Commands
    private final Command loadConfigurationCommand;
    private final Command saveConfigurationCommand;
    
    public ConfigurationViewModelImpl(
            ConfigurationService configService,
            ValidationService validationService,
            EventBus eventBus) {
        
        this.configService = configService;
        this.validationService = validationService;
        this.eventBus = eventBus;
        
        // Initialize commands with proper error handling
        this.loadConfigurationCommand = new AsyncCommand(
                this::loadConfiguration,
                loading.not(),
                "Load Configuration"
        );
        
        this.saveConfigurationCommand = new AsyncCommand(
                this::saveConfiguration,
                loading.not().and(valid),
                "Save Configuration"
        );
        
        // Setup reactive validation
        projectPath.addListener((obs, old, path) -> {
            if (!path.isEmpty()) {
                validatePath(path);
            }
        });
    }
    
    private CompletableFuture<Void> loadConfiguration() {
        String path = projectPath.get();
        log.info("Loading configuration from: {}", path);
        
        return CompletableFuture.runAsync(() -> {
            try {
                loading.set(true);
                configurationStatus.set("Loading...");
                
                Configuration config = configService.load(path);
                
                Platform.runLater(() -> {
                    configurationStatus.set("Loaded successfully");
                    eventBus.post(new ConfigurationLoadedEvent(config));
                });
                
            } catch (Exception e) {
                String errorMsg = String.format("Failed to load configuration from '%s': %s", 
                        path, e.getMessage());
                log.error(errorMsg, e);
                
                Platform.runLater(() -> {
                    configurationStatus.set("Load failed");
                    validationMessages.add(ValidationMessage.error(errorMsg));
                });
            } finally {
                Platform.runLater(() -> loading.set(false));
            }
        });
    }
}
```

#### 3.3 View (Pure UI)
```java
public class ConfigurationView extends BrobotCard {
    private final ConfigurationViewModel viewModel;
    
    public ConfigurationView(ConfigurationViewModel viewModel) {
        super("Configuration");
        this.viewModel = viewModel;
        
        setupUI();
        bindToViewModel();
    }
    
    private void setupUI() {
        // Project path field
        TextField projectPathField = new TextField();
        projectPathField.setPromptText("Project configuration path");
        projectPathField.setPrefWidth(400);
        
        // Browse button
        Button browseButton = new Button("Browse", new FontIcon(Material2AL.FOLDER_OPEN));
        browseButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        
        // Load button
        Button loadButton = new Button("Load", new FontIcon(Material2AL.DOWNLOAD));
        loadButton.getStyleClass().addAll(Styles.ACCENT);
        
        // Status label
        Label statusLabel = new Label();
        statusLabel.getStyleClass().add(Styles.TEXT_MUTED);
        
        // Progress indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(16, 16);
        
        // Validation messages
        ListView<ValidationMessage> messageList = new ListView<>();
        messageList.setCellFactory(lv -> new ValidationMessageCell());
        messageList.setPrefHeight(100);
        
        // Layout
        HBox pathBox = new HBox(8, projectPathField, browseButton, loadButton);
        HBox statusBox = new HBox(8, statusLabel, progressIndicator);
        
        VBox content = new VBox(12, 
                new Label("Project Configuration"),
                pathBox,
                statusBox,
                new Separator(),
                new Label("Validation Messages"),
                messageList
        );
        
        setContent(content);
    }
    
    private void bindToViewModel() {
        // Bind UI to view model properties
        projectPathField.textProperty().bindBidirectional(viewModel.projectPathProperty());
        statusLabel.textProperty().bind(viewModel.configurationStatusProperty());
        progressIndicator.visibleProperty().bind(viewModel.loadingProperty());
        loadButton.disableProperty().bind(viewModel.loadingProperty());
        messageList.itemsProperty().bind(viewModel.validationMessages());
        
        // Bind commands
        loadButton.setOnAction(e -> viewModel.loadConfigurationCommand().execute());
        browseButton.setOnAction(e -> viewModel.browseProjectCommand().execute());
    }
}
```

## 4. Plugin Architecture Implementation

### 4.1 Plugin API
```java
public interface BrobotPlugin extends DiagnosticCapable {
    /**
     * Plugin metadata for discovery and management
     */
    PluginMetadata getMetadata();
    
    /**
     * Called when plugin is enabled
     */
    void onEnable(PluginContext context);
    
    /**
     * Called when plugin is disabled
     */
    void onDisable();
    
    /**
     * Self-test for AI debugging
     */
    default SelfTestResult selfTest() {
        try {
            // Default implementation
            DiagnosticInfo info = getDiagnosticInfo();
            return SelfTestResult.success("Plugin operational", info);
        } catch (Exception e) {
            return SelfTestResult.failure("Self-test failed", e);
        }
    }
}

@Value
@Builder
public class PluginMetadata {
    String id;
    String name;
    String version;
    String description;
    String author;
    List<String> dependencies;
    Map<String, String> configuration;
}
```

### 4.2 Example Plugin
```java
@Plugin(id = "image-analysis", name = "Image Analysis Plugin")
@Slf4j
public class ImageAnalysisPlugin implements BrobotPlugin {
    private PluginContext context;
    private ImageAnalysisService analysisService;
    
    @Override
    public PluginMetadata getMetadata() {
        return PluginMetadata.builder()
                .id("image-analysis")
                .name("Image Analysis Plugin")
                .version("1.0.0")
                .description("Provides advanced image analysis capabilities")
                .author("Brobot Team")
                .dependencies(List.of("core", "opencv"))
                .build();
    }
    
    @Override
    public void onEnable(PluginContext context) {
        this.context = context;
        log.info("Enabling Image Analysis Plugin");
        
        // Register services
        analysisService = new ImageAnalysisService();
        context.registerService(ImageAnalysisService.class, analysisService);
        
        // Register UI components
        context.registerUIModule(new ImageAnalysisUIModule(analysisService));
        
        // Subscribe to events
        context.getEventBus().subscribe(ImageLoadedEvent.class, this::onImageLoaded);
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        return DiagnosticInfo.builder()
                .component("ImageAnalysisPlugin")
                .state("enabled", context != null)
                .state("processedImages", analysisService != null ? 
                        analysisService.getProcessedCount() : 0)
                .state("activeAnalysis", analysisService != null ? 
                        analysisService.getActiveAnalysisCount() : 0)
                .build();
    }
}
```

## 5. Test Patterns for AI Debugging

### 5.1 Test Data Builder with Diagnostics
```java
public class SessionTestDataBuilder {
    public static SessionBuilder aSession() {
        return new SessionBuilder();
    }
    
    public static SessionBuilder anExpiredSession() {
        return new SessionBuilder()
                .withStatus(SessionStatus.EXPIRED)
                .withStartTime(Instant.now().minus(2, ChronoUnit.HOURS))
                .withEndTime(Instant.now().minus(1, ChronoUnit.HOURS));
    }
    
    public static class SessionBuilder {
        private String id = UUID.randomUUID().toString();
        private String projectName = "test-project";
        private SessionStatus status = SessionStatus.ACTIVE;
        private Instant startTime = Instant.now();
        private Instant endTime = null;
        private boolean diagnosticMode = false;
        
        public SessionBuilder withDiagnosticMode(boolean enabled) {
            this.diagnosticMode = enabled;
            return this;
        }
        
        public Session build() {
            Session session = new Session(id, projectName, status, startTime, endTime);
            
            if (diagnosticMode) {
                // Add diagnostic metadata
                session.addMetadata("testBuilder", this.getClass().getSimpleName());
                session.addMetadata("builtAt", Instant.now().toString());
                session.addMetadata("diagnosticMode", "true");
            }
            
            return session;
        }
    }
}
```

### 5.2 Scenario Test with AI Debugging
```java
@Nested
@DisplayName("Scenario: Session Timeout Recovery")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SessionTimeoutRecoveryScenario {
    
    @Test
    @DisplayName("Given an expired session, when recovery is attempted, then session is restored with new timeout")
    @EnableDiagnostics
    void recoverExpiredSession() {
        // Given - Clear setup with test data builder
        Session expiredSession = SessionTestDataBuilder.anExpiredSession()
                .withProjectName("critical-project")
                .withDiagnosticMode(true)
                .build();
        
        sessionRepository.save(expiredSession).join();
        
        // When - Single action with correlation tracking
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        RecoveryResult result = sessionService.recoverSession(expiredSession.getId());
        
        // Then - Clear assertions with context
        assertThat(result)
                .describedAs("Recovery result for expired session %s", expiredSession.getId())
                .isNotNull();
        
        assertThat(result.isSuccessful())
                .describedAs("Recovery should succeed for expired session")
                .isTrue();
        
        Session recoveredSession = result.getRecoveredSession();
        assertThat(recoveredSession.getStatus())
                .describedAs("Recovered session status")
                .isEqualTo(SessionStatus.ACTIVE);
        
        // Diagnostic assertions for AI debugging
        assertThat(recoveredSession.getMetadata())
                .describedAs("Session metadata should contain recovery info")
                .containsEntry("recoveryCorrelationId", correlationId)
                .containsEntry("originalSessionId", expiredSession.getId())
                .containsKey("recoveryTimestamp");
        
        // Cleanup
        MDC.remove("correlationId");
    }
}
```

## Summary

These implementation examples demonstrate:

1. **Single Responsibility**: Each class has one clear purpose
2. **AI-Friendly Patterns**: Diagnostic interfaces, correlation IDs, explicit context
3. **Testability**: Clear test data builders, scenario-based tests
4. **Modularity**: Plugin architecture, view models, clean separation
5. **Traceability**: Comprehensive logging with context

The refactored code is significantly easier for AI to understand, debug, and extend while maintaining clean architecture principles for human developers.