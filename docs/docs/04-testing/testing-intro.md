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
- **Optimized execution** with parallel testing and shared contexts

**Performance Features**:
- Parallel test execution using available CPU cores
- Shared Spring contexts via `OptimizedIntegrationTestBase`
- Ultra-fast mock timings (0.005-0.04s per operation in test profile)
- JVM forking (every 20 tests) to prevent memory issues
- Test result caching for faster re-runs

**Best for**: End-to-end workflow validation, reliability testing, CI/CD pipeline integration

For comprehensive integration testing patterns with Spring Boot, see the [Integration Testing Guide](./integration-testing.md).

### Unit Testing
**Purpose**: Test individual components with deterministic, reproducible results

- **Isolated component testing** using static screenshots
- **Deterministic results** with known screen states
- **Fast execution** with mocked actions
- **Regression detection** for code changes

**Best for**: Component validation, regression testing, development workflows

For detailed unit testing patterns with BrobotTestBase, see the [Unit Testing Guide](./unit-testing.md).

### Action Recording
**Purpose**: Visual validation and debugging of automation behavior

- **Visual verification** of action execution
- **Interactive debugging** and development
- **Screenshot-based validation** with real-time feedback
- **Manual testing support** for complex scenarios

> **💡 Visual Debugging**: Brobot's highlighting feature provides real-time visual feedback during automation. See the [Highlighting Feature Guide](/docs/core-library/guides/user-guides/highlighting-feature.md) for details on visual debugging and validation techniques.

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
brobot.mock=true
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
  mock: true
  core:
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

For applications migrating from Brobot 1.0.x, the following APIs have been modernized:

| Legacy (1.0.x) | Modern (1.1.0+) | Notes |
|----------------|-----------------|-------|
| Direct settings manipulation | `brobot.mock=true` | Use properties instead of code configuration |
| Manual screenshot configuration | `brobot.screenshot.path` | Configure via properties |
| `ActionOptions` classes | `ActionConfig` classes (e.g., `PatternFindOptions`, `ClickOptions`) | New fluent builder API |

See [ActionOptions to ActionConfig Migration](../03-core-library/migration/actionoptions-to-actionconfig.md) for complete migration guide.

## Testing Workflow

### 1. Configuration Setup
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.manageStates.StateService;

@SpringBootTest
@TestPropertySource(properties = {
    "brobot.mock=true",
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.reports.ActionResult;
import io.github.jspinak.brobot.actions.actionConfigs.PatternFindOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;
import java.util.List;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.reports.ActionResult;
import io.github.jspinak.brobot.actions.actionConfigs.PatternFindOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;

@Test
void validateResults() {
    // Perform find action
    PatternFindOptions findOptions = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .build();

    // Create StateImage for testing
    StateImage buttonsImage = new StateImage.Builder()
        .addPattern("buttons")
        .setName("buttons")
        .build();

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

#### Profile-Based Configuration (Recommended)
For conflict-free test configuration, use [Profile-Based Testing](/docs/testing/profile-based-testing):

```java
@SpringBootTest(classes = IntegrationTestMinimalConfig.class)
@ActiveProfiles("integration-minimal")
@TestPropertySource(locations = "classpath:application-integration.properties")
class IntegrationTest extends IntegrationTestBase {
    // Isolated test configuration without bean conflicts
}
```

#### Standard Configuration
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

## Quick Performance Setup

To enable optimized test execution for integration tests:

1. **Add to `library-test/gradle.properties`**:
```properties
org.gradle.parallel=true
org.gradle.caching=true
systemProp.junit.jupiter.execution.parallel.enabled=true
```

2. **Update `library-test/build.gradle`**:
```gradle
test {
    timeout = Duration.ofMinutes(10)
    maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2)
    forkEvery = 20
}
```

3. **Use optimized base class**:
```java
public class MyTest extends OptimizedIntegrationTestBase {
    // Automatic performance optimizations
}
```

For detailed examples and advanced patterns, see the specific testing type documentation.

## Related Documentation

### Essential Testing Guides
- **[Testing Strategy](./testing-strategy.md)** - Comprehensive testing strategy, tag-based organization, and best practices
- **[Unit Testing](./unit-testing.md)** - Unit testing patterns with BrobotTestBase
- **[Integration Testing](./integration-testing.md)** - Spring Boot integration testing patterns
- **[Profile-Based Testing](./profile-based-testing.md)** - Test profiles and configuration isolation

### Mock Mode & Testing
- **[Mock Mode Guide](./mock-mode-guide.md)** - Comprehensive mock testing framework
- **[Mock Mode Manager](./mock-mode-manager.md)** - Centralized mock configuration
- **[Mock Stochasticity](./mock-stochasticity.md)** - Probabilistic testing patterns
- **[ActionHistory Integration Testing](./actionhistory-integration-testing.md)** - Testing with action histories
- **[ActionHistory Mock Snapshots](./actionhistory-mock-snapshots.md)** - Creating mock test data
- **[Action Recording](./action-recording.md)** - Recording actions for test creation

### Testing Utilities
- **[Test Utilities](./test-utilities.md)** - BrobotTestBase and testing helper classes
- **[Mat Testing Utilities](./mat-testing-utilities.md)** - OpenCV Mat testing utilities
- **[Debugging Pattern Matching](./debugging-pattern-matching.md)** - Troubleshooting pattern matching

### Configuration & Setup
- **[BrobotProperties Usage](../03-core-library/configuration/brobot-properties-usage.md)** - Configuration guide
- **[Properties Reference](../03-core-library/configuration/properties-reference.md)** - Complete properties documentation
- **[Headless Configuration](../03-core-library/configuration/headless-configuration.md)** - Headless environment setup
- **[Auto Configuration](../03-core-library/configuration/auto-configuration.md)** - Spring Boot auto-configuration

### ActionConfig System
- **[ActionConfig Overview](../03-core-library/action-config/01-overview.md)** - ActionConfig system introduction
- **[ActionConfig Examples](../03-core-library/action-config/03-examples.md)** - Practical testing examples
- **[ActionConfig Reference](../03-core-library/action-config/05-reference.md)** - Complete API reference
- **[ActionOptions to ActionConfig Migration](../03-core-library/migration/actionoptions-to-actionconfig.md)** - Migration guide from 1.0.x

### Advanced Testing Features
- **[Enhanced Mock Testing System](./advanced/enhanced-mocking.md)** - Scenario-based mock testing
- **[CI/CD Testing](./advanced/ci-cd-testing.md)** - Continuous integration patterns  