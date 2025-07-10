# AI-Assisted Testing Framework for Brobot Runner

## Overview

This framework defines a comprehensive testing approach optimized for AI assistants to effectively test, debug, and validate the Brobot Runner codebase. It emphasizes explicit context, traceable execution, and diagnostic-rich test patterns.

## Core Principles

### 1. Test Discoverability for AI

```java
/**
 * Test Naming Convention:
 * - Test class: [ClassUnderTest]Test
 * - Test method: test[Scenario]_[Condition]_[ExpectedResult]
 * 
 * Example:
 * - Class: SessionServiceTest
 * - Method: testStartSession_WhenNoActiveSession_ShouldCreateAndPersist
 */
```

### 2. Self-Documenting Test Structure

```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("SessionService - Core session lifecycle management")
class SessionServiceTest {
    
    /**
     * Test Metadata for AI:
     * - Component: SessionService
     * - Scenario: Starting new session
     * - Dependencies: SessionRepository, SessionStateCapture
     * - Test Data: Uses SessionTestDataBuilder
     * - Verification: State changes, persistence, events
     */
    @Test
    @Order(1)
    @DisplayName("GIVEN no active session WHEN startSession called THEN new session created and persisted")
    @TestScenario(
        given = "No active session exists",
        when = "startSession is called with valid project name",
        then = "New session is created, persisted, and events are published"
    )
    void testStartSession_WhenNoActiveSession_ShouldCreateAndPersist() {
        // Test implementation
    }
}
```

## Test Framework Components

### 1. Test Data Builder Registry

```java
@Component
public class TestDataBuilderRegistry {
    private final Map<Class<?>, TestDataBuilder<?>> builders = new HashMap<>();
    
    @PostConstruct
    public void registerBuilders() {
        register(Session.class, new SessionTestDataBuilder());
        register(ExecutionContext.class, new ExecutionContextTestDataBuilder());
        register(Configuration.class, new ConfigurationTestDataBuilder());
    }
    
    public <T> TestDataBuilder<T> getBuilder(Class<T> type) {
        return (TestDataBuilder<T>) builders.get(type);
    }
    
    // AI-friendly factory methods
    public Session aSession() {
        return getBuilder(Session.class).build();
    }
    
    public Session anExpiredSession() {
        return getBuilder(Session.class)
            .with("status", SessionStatus.EXPIRED)
            .with("endTime", Instant.now().minus(1, ChronoUnit.HOURS))
            .build();
    }
}

// Base test data builder
public abstract class TestDataBuilder<T> {
    protected final Map<String, Object> values = new HashMap<>();
    
    public TestDataBuilder<T> with(String property, Object value) {
        values.put(property, value);
        return this;
    }
    
    public abstract T build();
    
    // AI helper - describe what this builder creates
    public abstract String describe();
}
```

### 2. Diagnostic Test Runner

```java
@Component
public class DiagnosticTestRunner {
    private final List<DiagnosticCapable> components;
    private final TestExecutionRecorder recorder;
    
    /**
     * Captures diagnostic information before and after test execution
     */
    public TestResult runWithDiagnostics(Runnable test, String testName) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        MDC.put("testName", testName);
        
        try {
            // Capture pre-test state
            Map<String, DiagnosticInfo> preTestDiagnostics = captureDiagnostics();
            recorder.recordPreTest(testName, preTestDiagnostics);
            
            // Execute test
            Instant startTime = Instant.now();
            Throwable error = null;
            
            try {
                test.run();
            } catch (Throwable t) {
                error = t;
            }
            
            Instant endTime = Instant.now();
            
            // Capture post-test state
            Map<String, DiagnosticInfo> postTestDiagnostics = captureDiagnostics();
            recorder.recordPostTest(testName, postTestDiagnostics);
            
            // Generate test result
            return TestResult.builder()
                .testName(testName)
                .correlationId(correlationId)
                .startTime(startTime)
                .endTime(endTime)
                .duration(Duration.between(startTime, endTime))
                .success(error == null)
                .error(error)
                .preTestDiagnostics(preTestDiagnostics)
                .postTestDiagnostics(postTestDiagnostics)
                .stateDiff(calculateStateDiff(preTestDiagnostics, postTestDiagnostics))
                .build();
                
        } finally {
            MDC.clear();
        }
    }
    
    private Map<String, DiagnosticInfo> captureDiagnostics() {
        return components.stream()
            .collect(Collectors.toMap(
                component -> component.getClass().getSimpleName(),
                DiagnosticCapable::getDiagnosticInfo
            ));
    }
}
```

### 3. Test Scenario DSL

```java
// Annotation for describing test scenarios
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestScenario {
    String given();
    String when();
    String then();
    String[] verifies() default {};
    String[] dependencies() default {};
}

// Test scenario builder for complex scenarios
public class TestScenarioBuilder {
    private final List<TestStep> steps = new ArrayList<>();
    
    public static TestScenarioBuilder scenario(String name) {
        return new TestScenarioBuilder(name);
    }
    
    public TestScenarioBuilder given(String description, Runnable setup) {
        steps.add(new TestStep(StepType.GIVEN, description, setup));
        return this;
    }
    
    public TestScenarioBuilder when(String description, Runnable action) {
        steps.add(new TestStep(StepType.WHEN, description, action));
        return this;
    }
    
    public TestScenarioBuilder then(String description, Runnable assertion) {
        steps.add(new TestStep(StepType.THEN, description, assertion));
        return this;
    }
    
    public TestScenarioBuilder andThen(String description, Runnable assertion) {
        steps.add(new TestStep(StepType.THEN, description, assertion));
        return this;
    }
    
    public void execute() {
        TestContext context = new TestContext();
        
        for (TestStep step : steps) {
            log.info("{}: {}", step.getType(), step.getDescription());
            
            try {
                step.execute(context);
            } catch (Throwable t) {
                throw new TestScenarioException(
                    String.format("Test failed at %s step: %s", 
                        step.getType(), step.getDescription()), t);
            }
        }
    }
}
```

### 4. AI-Friendly Assertions

```java
public class AIAssertions {
    
    /**
     * Assertion with detailed failure context for AI debugging
     */
    public static <T> void assertWithContext(T actual, T expected, String context) {
        if (!Objects.equals(actual, expected)) {
            throw new AssertionError(String.format(
                "Assertion failed in context: %s\n" +
                "Expected: %s (type: %s)\n" +
                "Actual: %s (type: %s)\n" +
                "Difference: %s",
                context,
                expected, expected != null ? expected.getClass().getSimpleName() : "null",
                actual, actual != null ? actual.getClass().getSimpleName() : "null",
                describeDifference(expected, actual)
            ));
        }
    }
    
    /**
     * State validation with diagnostics
     */
    public static void assertStateValid(StateInspectable component, 
                                       StateValidator validator) {
        Map<String, Object> state = component.inspectState();
        ValidationResult result = validator.validate(state);
        
        if (!result.isValid()) {
            DiagnosticInfo diagnostics = component.getDiagnosticInfo();
            
            throw new AssertionError(String.format(
                "State validation failed for %s\n" +
                "Validation errors:\n%s\n" +
                "Current state:\n%s\n" +
                "Diagnostics:\n%s",
                component.getClass().getSimpleName(),
                formatValidationErrors(result.getErrors()),
                formatState(state),
                formatDiagnostics(diagnostics)
            ));
        }
    }
    
    /**
     * Event assertion with timeline
     */
    public static void assertEventOccurred(EventRecorder recorder, 
                                          Class<? extends Event> eventType,
                                          Duration within) {
        List<Event> events = recorder.getEvents(eventType);
        
        if (events.isEmpty()) {
            throw new AssertionError(String.format(
                "Expected event %s did not occur within %s\n" +
                "Recorded events timeline:\n%s",
                eventType.getSimpleName(),
                within,
                formatEventTimeline(recorder.getAllEvents())
            ));
        }
    }
}
```

### 5. Test Execution Patterns

```java
@TestConfiguration
public class TestPatternConfiguration {
    
    /**
     * Pattern: Isolated Component Testing
     */
    @Bean
    @Primary
    public TestPattern isolatedComponentPattern() {
        return new IsolatedComponentTestPattern();
    }
    
    /**
     * Pattern: Integration Flow Testing
     */
    @Bean
    public TestPattern integrationFlowPattern() {
        return new IntegrationFlowTestPattern();
    }
    
    /**
     * Pattern: State Transition Testing
     */
    @Bean
    public TestPattern stateTransitionPattern() {
        return new StateTransitionTestPattern();
    }
}

// Example pattern implementation
public class StateTransitionTestPattern implements TestPattern {
    
    @Override
    public void test(TestContext context) {
        StateMachine stateMachine = context.getComponent(StateMachine.class);
        
        // Define state transition test
        StateTransitionTest test = StateTransitionTest.builder()
            .stateMachine(stateMachine)
            .initialState(State.IDLE)
            .transitions(List.of(
                transition(State.IDLE, Event.START, State.RUNNING),
                transition(State.RUNNING, Event.PAUSE, State.PAUSED),
                transition(State.PAUSED, Event.RESUME, State.RUNNING),
                transition(State.RUNNING, Event.STOP, State.STOPPED)
            ))
            .invalidTransitions(List.of(
                transition(State.STOPPED, Event.PAUSE, null),
                transition(State.IDLE, Event.RESUME, null)
            ))
            .build();
        
        // Execute test
        test.execute();
    }
}
```

### 6. Test Environment Setup

```java
@TestConfiguration
public class AITestEnvironmentConfiguration {
    
    @Bean
    public TestEnvironment testEnvironment() {
        return TestEnvironment.builder()
            .withDiagnostics(true)
            .withTracing(true)
            .withEventRecording(true)
            .withStateCapture(true)
            .build();
    }
    
    @Bean
    public TestDataGenerator testDataGenerator() {
        return new TestDataGenerator()
            .withFaker(new Faker())
            .withCustomGenerators(Map.of(
                Session.class, new SessionDataGenerator(),
                Configuration.class, new ConfigurationDataGenerator()
            ));
    }
    
    @Bean
    public TestOrchestrator testOrchestrator() {
        return new TestOrchestrator()
            .withParallelExecution(true)
            .withMaxThreads(4)
            .withTimeout(Duration.ofMinutes(5))
            .withRetryStrategy(new ExponentialBackoffRetry(3));
    }
}
```

### 7. Test Result Analysis

```java
@Component
public class TestResultAnalyzer {
    
    /**
     * Analyzes test results for AI consumption
     */
    public TestAnalysisReport analyze(List<TestResult> results) {
        return TestAnalysisReport.builder()
            .summary(generateSummary(results))
            .failures(analyzeFailures(results))
            .performance(analyzePerformance(results))
            .coverage(analyzeCoverage(results))
            .recommendations(generateRecommendations(results))
            .build();
    }
    
    private TestSummary generateSummary(List<TestResult> results) {
        return TestSummary.builder()
            .totalTests(results.size())
            .passed(countPassed(results))
            .failed(countFailed(results))
            .duration(calculateTotalDuration(results))
            .successRate(calculateSuccessRate(results))
            .build();
    }
    
    private List<FailureAnalysis> analyzeFailures(List<TestResult> results) {
        return results.stream()
            .filter(r -> !r.isSuccess())
            .map(this::analyzeFailure)
            .collect(Collectors.toList());
    }
    
    private FailureAnalysis analyzeFailure(TestResult result) {
        return FailureAnalysis.builder()
            .testName(result.getTestName())
            .error(result.getError())
            .rootCause(findRootCause(result.getError()))
            .stateDiff(result.getStateDiff())
            .similarFailures(findSimilarFailures(result))
            .suggestedFixes(generateFixSuggestions(result))
            .build();
    }
}
```

### 8. Test Documentation Generator

```java
@Component
public class TestDocumentationGenerator {
    
    /**
     * Generates AI-readable test documentation
     */
    public TestDocumentation generate(Class<?> testClass) {
        TestClassInfo classInfo = analyzeTestClass(testClass);
        
        return TestDocumentation.builder()
            .className(testClass.getSimpleName())
            .description(classInfo.getDescription())
            .scenarios(extractScenarios(testClass))
            .dependencies(classInfo.getDependencies())
            .testData(classInfo.getTestDataTypes())
            .assertions(classInfo.getAssertionTypes())
            .build();
    }
    
    private List<TestScenarioDoc> extractScenarios(Class<?> testClass) {
        return Arrays.stream(testClass.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(Test.class))
            .map(this::documentScenario)
            .collect(Collectors.toList());
    }
    
    private TestScenarioDoc documentScenario(Method method) {
        TestScenario scenario = method.getAnnotation(TestScenario.class);
        
        return TestScenarioDoc.builder()
            .methodName(method.getName())
            .displayName(getDisplayName(method))
            .given(scenario != null ? scenario.given() : extractGiven(method))
            .when(scenario != null ? scenario.when() : extractWhen(method))
            .then(scenario != null ? scenario.then() : extractThen(method))
            .verifies(scenario != null ? Arrays.asList(scenario.verifies()) : 
                     extractVerifications(method))
            .build();
    }
}
```

## Test Patterns for Specific Components

### 1. Service Layer Testing

```java
@SpringBootTest
@TestPropertySource(properties = {
    "diagnostics.enabled=true",
    "testing.ai-mode=true"
})
class ServiceLayerTestPattern {
    
    @Autowired
    private TestDataBuilderRegistry testData;
    
    @Autowired
    private DiagnosticTestRunner testRunner;
    
    @Test
    void testServiceLayerPattern() {
        testRunner.runWithDiagnostics(() -> {
            // Given - Setup test data
            Session session = testData.aSession()
                .with("projectName", "test-project")
                .build();
            
            // When - Execute service method
            SessionService service = getBean(SessionService.class);
            Session result = service.startSession(
                session.getProjectName(), 
                Map.of("test", true)
            );
            
            // Then - Verify results
            AIAssertions.assertWithContext(
                result.getStatus(), 
                SessionStatus.ACTIVE,
                "New session should be active"
            );
            
            // And - Verify side effects
            verify(repository).save(any(Session.class));
            verify(eventPublisher).publishSessionStarted(any());
            
        }, "testServiceLayerPattern");
    }
}
```

### 2. Controller Layer Testing

```java
@WebMvcTest(ExecutionController.class)
@Import({DiagnosticTestRunner.class, TestDataBuilderRegistry.class})
class ControllerLayerTestPattern {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testControllerEndpoint() throws Exception {
        // Given
        ExecutionRequest request = ExecutionRequest.builder()
            .taskName("test-task")
            .options(ExecutionOptions.builder()
                .timeout(Duration.ofSeconds(30))
                .build())
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/execution/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.executionId").exists())
            .andExpect(jsonPath("$.status").value("STARTED"))
            .andDo(result -> {
                // Capture response for AI analysis
                String response = result.getResponse().getContentAsString();
                log.info("Controller response: {}", response);
            });
    }
}
```

## AI Testing Commands

### 1. Test Discovery

```java
// Find all tests for a component
AITestCommands.findTestsFor("SessionService");

// Find tests by scenario
AITestCommands.findTestsWithScenario("expired session");

// Find integration tests
AITestCommands.findIntegrationTests();
```

### 2. Test Execution

```java
// Run tests with full diagnostics
AITestCommands.runWithDiagnostics("SessionServiceTest");

// Run specific scenario
AITestCommands.runScenario("Session timeout recovery");

// Run and analyze
AITestCommands.runAndAnalyze("io.github.jspinak.brobot.runner");
```

### 3. Test Generation

```java
// Generate test for uncovered code
AITestCommands.generateTestFor(SessionService.class, "recoverSession");

// Generate integration test
AITestCommands.generateIntegrationTest(
    "User starts session, executes task, session expires"
);
```

## Summary

This AI-Assisted Testing Framework provides:

1. **Clear Test Structure**: Consistent patterns for test organization
2. **Rich Context**: Extensive metadata and documentation
3. **Diagnostic Integration**: Full system state visibility
4. **Failure Analysis**: Detailed error context and suggestions
5. **Test Generation**: Patterns for creating new tests
6. **Traceability**: Correlation IDs and event timelines

The framework enables AI assistants to effectively understand, execute, and analyze tests, making the debugging process more efficient and accurate.