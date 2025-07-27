---
sidebar_position: 3
---

# Unit Testing

Unit testing in Brobot ensures reproducible results by using static screenshots instead of live environments. This approach provides deterministic testing conditions where Find operations execute against known screen states while other actions are mocked.

## Overview

Brobot unit testing combines:
- **Real Find operations** on static screenshots
- **Mocked actions** (click, drag, type, etc.) for safety and speed
- **Deterministic results** for reliable test assertions

## Configuration

### Configuration via Properties

Configure testing through `application.properties` or `application.yml`:

```properties
# Enable mock mode for unit testing
brobot.core.mock=true

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
  core:
    mock: true
  screenshot:
    path: screenshots/
    filename: screen
  testing:
    iteration: 1
    send-logs: true
```

### Test Configuration

Configure tests using Spring Boot's property system:

```java
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.core.mock=true",
    "brobot.screenshot.path=src/test/resources/screenshots/"
})
class UnitTest {
    // Configuration is handled automatically by Spring
}
```

## Test Structure

### Basic Unit Test Example

```java
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.core.mock=true",
    "brobot.screenshot.test-path=src/test/resources/screenshots/"
})
class LoginAutomationTest {

    @Autowired
    private Action action;
    
    @Autowired
    private StateImageRepository stateImageRepo;
    
    // No @BeforeEach needed - configuration handled by properties
    
    @Test
    void testSuccessfulLogin() {
        // Arrange - Get state objects
        StateImage usernameField = stateImageRepo.get("username_field");
        StateImage passwordField = stateImageRepo.get("password_field");
        StateImage loginButton = stateImageRepo.get("login_button");
        
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
        action.perform(typeOptions, "testuser");
        
        // Find and click password field
        ActionResult passwordResult = action.perform(findOptions, passwordField);
        action.perform(typeOptions, "testpass");
        
        // Click login button
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setClickType(ClickOptions.Type.LEFT)
            .build();
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
    StateImage loginButton = stateImageRepo.get("login_button");
    ActionResult loginResult = action.perform(findOptions, loginButton);
    action.perform(new ClickOptions.Builder().build(), loginButton);
    
    // Step 2: Navigate to dashboard
    StateImage dashboardLink = stateImageRepo.get("dashboard_link");
    ActionResult dashboardResult = action.perform(findOptions, dashboardLink);
    action.perform(new ClickOptions.Builder().build(), dashboardLink);
    
    // Step 3: Open settings
    StateImage settingsIcon = stateImageRepo.get("settings_icon");
    ActionResult settingsResult = action.perform(findOptions, settingsIcon);
    action.perform(new ClickOptions.Builder().build(), settingsIcon);
    
    // Verify each step
    assertTrue(loginResult.isSuccess());
    assertTrue(dashboardResult.isSuccess());
    assertTrue(settingsResult.isSuccess());
}
```

## Working with ActionResult

The modern API uses `ActionResult` instead of the deprecated `MatchSnapshot`:

```java
@Test
void testFindOperations() {
    // Create find configuration
    PatternFindOptions findOptions = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.7)
        .build();
    
    // Perform find action
    StateImage submitButton = stateImageRepo.get("submit_button");
    ActionResult result = action.perform(findOptions, submitButton);
    
    // Test result properties
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    
    // Access best match
    Optional<Match> bestMatch = result.getBestMatch();
    assertTrue(bestMatch.isPresent());
    assertTrue(bestMatch.get().getSimScore() > 0.8);
    
    // Test specific regions
    List<Region> regions = result.getMatchRegions();
    assertThat(regions).hasSize(2);
    
    // Test filtering
    ActionResult highScoreMatches = new ActionResult();
    result.getMatchList().stream()
        .filter(match -> match.getSimScore() > 0.9)
        .forEach(highScoreMatches::add);
    assertTrue(highScoreMatches.size() > 0);
}
```

## Mock Behavior Verification

```java
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
    
    StateImage button = stateImageRepo.get("button");
    ActionResult findResult = action.perform(findOptions, button);
    
    ClickOptions clickOptions = new ClickOptions.Builder().build();
    ActionResult clickResult = action.perform(clickOptions, button);
    
    long duration = System.currentTimeMillis() - startTime;
    
    // Verify mock timing (should be approximately 300ms)
    assertTrue(duration >= 250 && duration <= 350);
}
```

## Advanced Testing Patterns

### State-Based Testing

```java
@Test
@TestPropertySource(properties = {
    "brobot.screenshot.path=src/test/resources/state-screenshots/"
})
void testStateTransitions() {
    // Screenshots: initial_state.png, target_state.png in configured path
    
    // Get state transition objects
    State initialState = stateRepository.get("INITIAL_STATE");
    State targetState = stateRepository.get("TARGET_STATE");
    
    // Find element that triggers transition
    StateImage transitionButton = initialState.getStateImages().get(0);
    PatternFindOptions findOptions = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.BEST)
        .build();
    
    ActionResult findResult = action.perform(findOptions, transitionButton);
    assertTrue(findResult.isSuccess());
    
    // Click to trigger transition
    ClickOptions clickOptions = new ClickOptions.Builder().build();
    ActionResult clickResult = action.perform(clickOptions, transitionButton);
    
    assertTrue(clickResult.isSuccess());
    // In mock mode, state transitions are simulated based on probabilities
}
```

### Custom Assertions

```java
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
            result.getBestMatch().get().getSimScore() >= minScore,
            String.format("Best match score %.3f below minimum %.3f", 
                result.getBestMatch().get().getSimScore(), minScore)
        );
    }
}
```

## API Migration Notes

**Important**: The Brobot testing framework is transitioning from legacy `ActionOptions` to modern `ActionConfig` classes. While the examples above show the recommended modern approach:

- Use `PatternFindOptions`, `ClickOptions`, etc. instead of `ActionOptions`
- Configure via properties files rather than programmatic setup
- Some internal components (like `ActionHistory`) still use legacy APIs during the transition

This migration ensures better type safety, clearer separation of concerns, and more maintainable test code.

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
- **Unexpected results**: Check mock mode is enabled (`brobot.core.mock=true`)
- **Slow tests**: Adjust mock timings in properties for faster execution
- **Flaky tests**: Ensure screenshots represent stable UI states
- **API conflicts**: Use ActionConfig classes instead of deprecated ActionOptions
