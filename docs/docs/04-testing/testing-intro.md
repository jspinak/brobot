---
sidebar_position: 1
---

# Testing Overview

Brobot provides comprehensive testing capabilities designed for GUI automation reliability and maintainability. The framework supports multiple testing approaches, each serving specific purposes in the automation testing lifecycle.

:::tip Advanced Testing Features
For advanced mock testing capabilities including scenario-based configurations and failure patterns, see the [Enhanced Mock Testing System](/docs/core-library/testing/enhanced-mocking) in the Core Library documentation.
:::

## Testing Types

### Integration Testing
**Purpose**: Validate complete automation workflows and system reliability

- **Full workflow simulation** using mock execution
- **Stochastic modeling** of real-world variability and failure modes
- **State transition validation** across complex application flows
- **Performance and reliability assessment** for production readiness

**Best for**: End-to-end workflow validation, reliability testing, CI/CD pipeline integration

### Unit Testing
**Purpose**: Test individual components with deterministic, reproducible results

- **Isolated component testing** using static screenshots
- **Deterministic results** with known screen states
- **Fast execution** with mocked actions
- **Regression detection** for code changes

**Best for**: Component validation, regression testing, development workflows

### Action Recording
**Purpose**: Visual validation and debugging of automation behavior

- **Visual verification** of action execution
- **Interactive debugging** and development
- **Screenshot-based validation** with real-time feedback
- **Manual testing support** for complex scenarios

**Best for**: Development debugging, manual verification, complex scenario validation

## Configuration Architecture

### Modern Configuration System

Brobot uses Spring Boot's configuration properties system with clean architectural patterns:

:::info New Test Logging Architecture
Brobot now includes a clean test logging architecture that follows Single Responsibility Principle and eliminates circular dependencies. See the [Test Logging Architecture](/docs/core-library/testing/test-logging-architecture) guide for details on using the TestLoggerFactory and related components.
:::

### Configuration Properties

```properties
# Core testing settings
brobot.core.mock=true
brobot.core.headless=false
brobot.core.image-path=images/

# Screenshot management
brobot.screenshot.path=screenshots/
brobot.screenshot.save-snapshots=false
brobot.screenshot.save-history=true

# Mock execution timings
brobot.mock.time-find-first=0.1
brobot.mock.time-click=0.05
brobot.mock.time-drag=0.3

# Testing behavior
brobot.testing.iteration=1
brobot.testing.send-logs=true
```

Or using YAML format:

```yaml
brobot:
  core:
    mock: true
    headless: false
    image-path: images/
  screenshot:
    path: screenshots/
    save-snapshots: false
    save-history: true
  mock:
    time-find-first: 0.1
    time-click: 0.05
    time-drag: 0.3
  testing:
    iteration: 1
    send-logs: true
```

### Migration from Legacy API

| Legacy (Deprecated) | Modern Equivalent |
|--------------------|-----------------|
| `BrobotSettings.mock` | `brobot.core.mock=true` |
| `BrobotSettings.screenshotPath` | `brobot.screenshot.path` |
| `BrobotSettings.screenshots` | Configure via properties |
| `ActionOptions` | `ActionConfig` (e.g., `PatternFindOptions`) |
| `MatchSnapshot` | `ActionResult` |

## Testing Workflow

### 1. Configuration Setup
```java
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.core.mock=true",
    "brobot.screenshot.path=src/test/resources/screenshots/"
})
class AutomationTest {
    
    @Autowired
    private Action action;
    
    @Autowired
    private StateService stateService;
    
    // No manual configuration needed - handled by properties
}
```

### 2. Test Execution
```java
@Test
void testAutomationFlow() {
    // Create action configuration
    PatternFindOptions findOptions = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.BEST)
        .setSimilarity(0.85)
        .build();
    
    // Create state object
    StateImage loginButton = new StateImage.Builder()
        .addPattern("login_button")  // No .png extension needed
        .build();
    
    // Execute automation with mock/real behavior based on configuration
    ActionResult result = action.perform(findOptions, loginButton);
    
    // Modern assertion patterns
    assertTrue(result.isSuccess());
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
}
```

### 3. Result Validation
```java
@Test
void validateResults() {
    // Perform find action
    PatternFindOptions findOptions = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .build();
    
    StateImage buttonsImage = stateImageRepo.get("buttons");
    ActionResult result = action.perform(findOptions, buttonsImage);
    
    // Test ActionResult properties
    Optional<Match> bestMatch = result.getBestMatch();
    assertTrue(bestMatch.isPresent());
    assertTrue(bestMatch.get().getScore() > 0.8);
    
    // Test regions and coordinates
    List<Region> regions = result.getMatchRegions();
    assertThat(regions).hasSize(2);
}
```

## Testing Best Practices

### Test Organization
- **Separate test configurations** for different testing types
- **Use descriptive test names** that indicate the scenario being tested
- **Organize screenshots** in logical directory structures
- **Version control test assets** including screenshots and configurations

### Configuration Management
- **Use properties files** for environment-specific settings
- **Leverage Spring profiles** for different testing scenarios
- **Document configuration** requirements for each test type
- **Validate configuration** in test setup methods

### Assertion Strategies
- **Test positive and negative cases** for comprehensive coverage
- **Use appropriate assertion granularity** (component vs. workflow level)
- **Implement custom matchers** for domain-specific validations
- **Include timing and performance assertions** where relevant

## Framework Integration

### Spring Boot Integration
```java
@SpringBootTest
@ActiveProfiles("test")
class IntegrationTest {
    // Full Spring context with Brobot configuration
}
```

### JUnit 5 Support
```java
@ExtendWith(MockitoExtension.class)
class UnitTest {
    @Mock
    private SomeService mockService;
    
    // Standard JUnit 5 patterns with Brobot
}
```

### CI/CD Pipeline Support
- **Headless execution** for automated environments
- **Configurable timeouts** for different environments
- **Test result reporting** with detailed logs
- **Screenshot archival** for test failure analysis

## Getting Started

1. **Choose your testing approach** based on requirements
2. **Configure properties** for your testing environment
3. **Set up test screenshots** or mock data as needed
4. **Implement test cases** using modern APIs
5. **Validate and iterate** based on test results

For detailed examples and advanced patterns, see the specific testing type documentation.  