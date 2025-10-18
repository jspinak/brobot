---
sidebar_position: 3
---

# Unit Testing

Unit testing in Brobot ensures reproducible results by using static screenshots instead of live environments. This approach provides deterministic testing conditions where Find operations execute against known screen states while other actions are mocked.

## Overview

Brobot unit testing combines:
- **Centralized mock mode management** via `MockModeManager`
- **Real Find operations** on static screenshots
- **Mocked actions** (click, drag, type, etc.) for safety and speed
- **Deterministic results** for reliable test assertions

## Base Test Class

All Brobot unit tests should extend `BrobotTestBase` for automatic mock mode configuration:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;

public class MyUnitTest extends BrobotTestBase {
    
    @Test
    public void testFeature() {
        // Mock mode is automatically enabled
        // Test runs safely in headless environments
    }
}
```

`BrobotTestBase` automatically:
- Enables mock mode via `MockModeManager`
- Configures fast mock timings (0.01-0.04s)
- Ensures headless compatibility
- Prevents AWTException errors

## Configuration

### Automatic Mock Mode Configuration

When extending `BrobotTestBase`, mock mode is automatically configured via `MockModeManager`:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.config.mock.MockModeManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginTest extends BrobotTestBase {
    // No manual mock configuration needed!

    @Test
    public void testLogin() {
        // Mock mode is already enabled
        assertTrue(MockModeManager.isMockMode());
    }
}
```

### Manual Configuration via Properties

If not using `BrobotTestBase`, configure through `application.properties`:

```properties
# Enable mock mode for unit testing
brobot.mock=true

# Screenshot configuration
brobot.screenshot.path=screenshots/
brobot.screenshot.filename=screen

# Testing settings
brobot.testing.iteration=1
brobot.testing.send-logs=true
```

Or using YAML:

```yaml
brobot:
  mock: true
  screenshot:
    path: screenshots/
    filename: screen
  testing:
    iteration: 1
    send-logs: true
```

### Test Configuration

For Spring Boot tests, extend `BrobotTestBase` and use property configuration:

```java
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.screenshot.path=src/test/resources/screenshots/"
})
class UnitTest extends BrobotTestBase {
    // Mock mode is automatically enabled by BrobotTestBase
    // Additional properties are handled by Spring
}
```

For non-Spring tests:

```java
public class SimpleUnitTest extends BrobotTestBase {
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest(); // Enables mock mode via MockModeManager
        // Add any additional setup
    }
}```

## Test Structure

### Basic Unit Test Example

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.methods.basicactions.find.PatternFindOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.ClickOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.TypeOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.reports.ActionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "brobot.screenshot.test-path=src/test/resources/screenshots/"
})
class LoginAutomationTest extends BrobotTestBase {

    @Autowired
    private Action action;
    
    // Mock mode is automatically enabled by BrobotTestBase
    
    @Test
    void testSuccessfulLogin() {
        // Arrange - Create state objects
        StateImage usernameField = new StateImage.Builder()
            .addPattern("username_field")
            .build();
        StateImage passwordField = new StateImage.Builder()
            .addPattern("password_field")
            .build();
        StateImage loginButton = new StateImage.Builder()
            .addPattern("login_button")
            .build();
        
        // Act - Perform actions
        // Find and click username field
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
        ActionResult usernameResult = action.perform(findOptions, usernameField);
        
        // Type username
        TypeOptions typeOptions = new TypeOptions.Builder()
            .setTypeDelay(0.05)
            .build();
        action.perform(typeOptions, new ObjectCollection.Builder()
            .withStrings("testuser")
            .build());
        
        // Find and click password field
        ActionResult passwordResult = action.perform(findOptions, passwordField);
        action.perform(typeOptions, new ObjectCollection.Builder()
            .withStrings("testpass")
            .build());
        
        // Click login button
        ClickOptions clickOptions = new ClickOptions.Builder()
            .build();  // LEFT button is default
        ActionResult loginResult = action.perform(clickOptions, loginButton);
        
        // Assert
        assertTrue(usernameResult.isSuccess());
        assertTrue(passwordResult.isSuccess());
        assertTrue(loginResult.isSuccess());
        assertEquals(1, loginResult.size());
        assertThat(loginResult.getBestMatch()).isPresent();
    }
}
```

### Testing with Multiple Screenshots

```java
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.methods.basicactions.find.PatternFindOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.ClickOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.reports.ActionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Test
void testNavigationFlow() {
    // Screenshots configured via properties file
    // brobot.screenshot.path=src/test/resources/screenshots/
    // Place files: step1_login.png, step2_dashboard.png, step3_settings.png

    // Create find options for navigation
    PatternFindOptions findOptions = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.BEST)
        .setSimilarity(0.8)
        .build();
    
    // Test navigation sequence
    // Step 1: Login
    StateImage loginButton = new StateImage.Builder()
        .addPattern("login_button.png")
        .build();
    ActionResult loginResult = action.perform(findOptions, loginButton);
    action.perform(new ClickOptions.Builder().build(), loginButton);
    
    // Step 2: Navigate to dashboard
    StateImage dashboardLink = new StateImage.Builder()
        .addPattern("dashboard_link")
        .build();
    ActionResult dashboardResult = action.perform(findOptions, dashboardLink);
    action.perform(new ClickOptions.Builder().build(), dashboardLink);
    
    // Step 3: Open settings
    StateImage settingsIcon = new StateImage.Builder()
        .addPattern("settings_icon.png")
        .build();
    ActionResult settingsResult = action.perform(findOptions, settingsIcon);
    action.perform(new ClickOptions.Builder().build(), settingsIcon);
    
    // Verify each step
    assertTrue(loginResult.isSuccess());
    assertTrue(dashboardResult.isSuccess());
    assertTrue(settingsResult.isSuccess());
}
```

## Working with ActionResult

`ActionResult` is returned by action executions and provides comprehensive information about matches found:

```java
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.methods.basicactions.find.PatternFindOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.reports.ActionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@Test
void testFindOperations() {
    // Create find configuration
    PatternFindOptions findOptions = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.7)
        .build();
    
    // Perform find action
    StateImage submitButton = new StateImage.Builder()
        .addPattern("submit_button.png")
        .build();
    ActionResult result = action.perform(findOptions, submitButton);
    
    // Test result properties
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    
    // Access best match
    Optional<Match> bestMatch = result.getBestMatch();
    assertTrue(bestMatch.isPresent());
    assertTrue(bestMatch.get().getScore() > 0.8);
    
    // Test specific regions
    List<Region> regions = result.getMatchRegions();
    assertThat(regions).hasSize(2);
    
    // Test filtering
    ActionResult highScoreMatches = new ActionResult();
    result.getMatchList().stream()
        .filter(match -> match.getScore() > 0.9)
        .forEach(highScoreMatches::add);
    assertTrue(highScoreMatches.size() > 0);
}
```

## Mock Behavior Verification

```java
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.methods.basicactions.find.PatternFindOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.ClickOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.reports.ActionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Test
@TestPropertySource(properties = {
    "brobot.mock.time-click=0.1",
    "brobot.mock.time-find-first=0.2"
})
void testMockTimings() {
    long startTime = System.currentTimeMillis();
    
    // Perform mocked actions
    PatternFindOptions findOptions = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .build();
    
    StateImage button = new StateImage.Builder()
        .addPattern("button.png")
        .build();
    ActionResult findResult = action.perform(findOptions, button);
    
    ClickOptions clickOptions = new ClickOptions.Builder().build();
    ActionResult clickResult = action.perform(clickOptions, button);
    
    long duration = System.currentTimeMillis() - startTime;
    
    // Verify mock timing (should be approximately 300ms)
    assertTrue(duration >= 250 && duration <= 350);
}
```

## Advanced Testing Patterns

### Pattern-Based Testing

```java
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.methods.basicactions.find.PatternFindOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.reports.ActionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Test
void testPatternMatching() {
    // Test with different similarity thresholds
    PatternFindOptions strictFind = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.BEST)
        .setSimilarity(0.95)
        .build();
    
    PatternFindOptions relaxedFind = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.70)
        .build();
    
    StateImage targetPattern = new StateImage.Builder()
        .addPattern("target_button.png")
        .build();
    
    // Test strict matching
    ActionResult strictResult = action.perform(strictFind, targetPattern);
    assertTrue(strictResult.size() <= 1, "Strict matching should find at most one match");
    
    // Test relaxed matching
    ActionResult relaxedResult = action.perform(relaxedFind, targetPattern);
    assertTrue(relaxedResult.size() >= strictResult.size(), 
        "Relaxed matching should find at least as many matches");
}
```

### Custom Assertions

```java
import io.github.jspinak.brobot.reports.ActionResult;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BrobotAssertions {

    public static void assertFoundInRegion(ActionResult result, Region expectedRegion) {
        assertTrue(result.isSuccess(), "Expected to find matches");
        
        boolean foundInRegion = result.getMatchList().stream()
            .anyMatch(match -> expectedRegion.contains(match.getRegion()));
            
        assertTrue(foundInRegion, "No matches found in expected region");
    }
    
    public static void assertMinimumScore(ActionResult result, double minScore) {
        assertTrue(result.getBestMatch().isPresent(), "No matches found");
        assertTrue(
            result.getBestMatch().get().getScore() >= minScore,
            String.format("Best match score %.3f below minimum %.3f", 
                result.getBestMatch().get().getScore(), minScore)
        );
    }
}
```

## API Migration Notes

**Important**: Brobot 1.1.0+ uses modern API patterns. When migrating from Brobot 1.0.x:

### ActionOptions → ActionConfig Migration
- Use `PatternFindOptions`, `ClickOptions`, `TypeOptions`, etc. instead of legacy `ActionOptions`
- Configure via properties files (`brobot.mock=true`) rather than programmatic setup
- ActionConfig classes provide better type safety and clearer separation of concerns

### ActionRecord for Test History
- `ActionRecord` is the class for recording action history and mock data
- Use `ActionRecord` for creating test snapshots and mock responses
- ActionRecord replaced the legacy MatchSnapshot class from Brobot 1.0.x

### Modern Property Names
- Use `brobot.mock=true` (NOT `brobot.core.mock` or `brobot.mock.mode`)
- Use `brobot.screenshot.path` for screenshot configuration
- Use `brobot.mock.time-*` properties for mock timing configuration

See [Upgrading to Latest](../03-core-library/migration/upgrading-to-latest.md) for complete migration guide.

## Best Practices

1. **Modern API Usage**
   - Use ActionConfig subclasses (PatternFindOptions, ClickOptions) instead of ActionOptions
   - Configure tests through properties files
   - Leverage action chaining with `.then()` for complex test scenarios

2. **Screenshot Management**
   - Use descriptive filenames (e.g., `login_page_chrome.png`)
   - Keep screenshots in `src/test/resources/screenshots/`
   - Version control your test screenshots

3. **Test Isolation**
   - Configure screenshots via properties rather than programmatic setup
   - Use `@TestPropertySource` for test-specific settings
   - Avoid manual configuration in `@BeforeEach` methods

4. **Assertions**
   - Test both positive and negative cases
   - Verify match scores and regions using ActionResult methods
   - Use custom assertion methods for common patterns

5. **Mock Configuration**
   - Set realistic mock timings via properties
   - Test timeout scenarios
   - Verify mock behavior in CI environments

## Troubleshooting

- **No matches found**: Verify screenshot path configuration in properties
- **Unexpected results**: Check mock mode is enabled (`brobot.mock=true`)
- **Slow tests**: Adjust mock timings in properties for faster execution
- **Flaky tests**: Ensure screenshots represent stable UI states
- **API conflicts**: Use ActionConfig classes instead of deprecated ActionOptions

## Related Documentation

### Essential Testing Guides
- **[Testing Introduction](./testing-intro.md)** - Overview of all Brobot testing approaches
- **[Integration Testing](./integration-testing.md)** - Spring Boot integration testing patterns
- **[Profile-Based Testing](./profile-based-testing.md)** - Test profiles and configuration isolation
- **[Test Utilities](./test-utilities.md)** - BrobotTestBase and testing helper classes
- **[Testing Strategy](./testing-strategy.md)** - Comprehensive testing strategy and best practices

### Mock Mode & Testing
- **[Mock Mode Guide](./mock-mode-guide.md)** - Comprehensive mock testing framework
- **[Mock Mode Manager](./mock-mode-manager.md)** - Centralized mock configuration
- **[Mock Stochasticity](./mock-stochasticity.md)** - Probabilistic testing patterns
- **[ActionHistory Mock Snapshots](./actionhistory-mock-snapshots.md)** - Creating mock test data
- **[Action Recording](./action-recording.md)** - Recording actions for test creation

### ActionConfig System
- **[ActionConfig Overview](../03-core-library/action-config/01-overview.md)** - ActionConfig system introduction
- **[ActionConfig Examples](../03-core-library/action-config/03-examples.md)** - Practical testing examples
- **[ActionConfig Reference](../03-core-library/action-config/05-reference.md)** - Complete API reference
- **[Upgrading to Latest](../03-core-library/migration/upgrading-to-latest.md)** - Migration guide from 1.0.x

### Configuration & Setup
- **[BrobotProperties Usage](../03-core-library/configuration/brobot-properties-usage.md)** - Configuration guide
- **[Properties Reference](../03-core-library/configuration/properties-reference.md)** - Complete properties documentation
- **[Headless Configuration](../03-core-library/configuration/headless-configuration.md)** - Headless environment setup
- **[Auto Configuration](../03-core-library/configuration/auto-configuration.md)** - Spring Boot auto-configuration

### Advanced Testing
- **[Enhanced Mock Testing System](./advanced/enhanced-mocking.md)** - Advanced mock scenarios
- **[Mat Testing Utilities](./mat-testing-utilities.md)** - OpenCV Mat testing utilities
- **[Debugging Pattern Matching](./debugging-pattern-matching.md)** - Troubleshooting pattern matching
- **[CI/CD Testing](./advanced/ci-cd-testing.md)** - Continuous integration patterns
