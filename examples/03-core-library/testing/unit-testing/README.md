# Unit Testing Example

This example demonstrates comprehensive unit testing patterns and best practices for Brobot applications.

## Overview

This project shows how to effectively test Brobot components:

- **State Testing**: Test State classes and their properties
- **Transition Testing**: Verify transition logic and validation
- **Service Testing**: Unit test business logic components
- **Integration Testing**: Test component interactions
- **Mock Mode Testing**: Leverage Brobot's mock mode for testing
- **Test Utilities**: Custom builders and assertions for cleaner tests

## Project Structure

```
unit-testing/
├── src/main/java/com/example/unittesting/
│   ├── UnitTestingApplication.java       # Main application
│   ├── states/
│   │   ├── LoginState.java              # Example login state
│   │   └── DashboardState.java          # Example dashboard state
│   ├── transitions/
│   │   └── LoginToDashboardTransition.java # State transition
│   └── services/
│       └── AuthenticationService.java    # Business logic service
├── src/test/java/com/example/unittesting/
│   ├── states/
│   │   ├── LoginStateTest.java          # State unit tests
│   │   └── DashboardStateTest.java      # State unit tests
│   ├── transitions/
│   │   └── LoginToDashboardTransitionTest.java # Transition tests
│   ├── services/
│   │   └── AuthenticationServiceTest.java # Service tests
│   ├── integration/
│   │   └── StateTransitionIntegrationTest.java # Integration tests
│   ├── mock/
│   │   └── MockModeTest.java            # Mock mode examples
│   ├── utils/
│   │   ├── TestDataBuilder.java         # Test data builders
│   │   └── TestAssertions.java          # Custom assertions
│   └── examples/
│       └── TestDataBuilderExampleTest.java # Builder usage examples
└── src/test/resources/
    └── application-test.yml              # Test configuration
```

## Key Testing Patterns

### 1. State Testing

Test your State classes thoroughly:

```java
@Test
@DisplayName("Should have all required elements")
void testHasRequiredElements() {
    assertThat(loginState.hasRequiredElements()).isTrue();
    assertThat(loginState.getUsernameField()).isNotNull();
    assertThat(loginState.getPasswordField()).isNotNull();
    assertThat(loginState.getLoginButton()).isNotNull();
}
```

### 2. Parameterized Testing

Use JUnit 5's parameterized tests for comprehensive coverage:

```java
@ParameterizedTest
@DisplayName("Should validate credentials correctly")
@CsvSource({
    "validuser, password123, true",
    "user, short, false",
    "user, 1234567, false"
})
void testCredentialValidation(String username, String password, boolean expected) {
    boolean result = transition.validateCredentials(username, password);
    assertThat(result).isEqualTo(expected);
}
```

### 3. Nested Test Organization

Group related tests with @Nested:

```java
@Nested
@DisplayName("Authentication Tests")
class AuthenticationTests {
    
    @Test
    @DisplayName("Should authenticate valid user")
    void testValidAuthentication() {
        // Test implementation
    }
    
    @Test
    @DisplayName("Should reject invalid credentials")
    void testInvalidAuthentication() {
        // Test implementation
    }
}
```

### 4. Test Data Builders

Create expressive test data:

```java
TestUser customUser = new TestDataBuilder.UserBuilder()
    .withUsername("john.doe")
    .withPassword("securePass123")
    .withRole("USER")
    .withRole("PREMIUM")
    .build();
```

### 5. Custom Assertions

Write domain-specific assertions:

```java
TestAssertions.StateAssert.assertThat(settingsState)
    .hasName("settings")
    .isActive()
    .hasStateObject("save_button")
    .hasStateObjectCount(3);
```

## Running Tests

### Run All Tests

```bash
./gradlew test
```

### Run Unit Tests Only

```bash
./gradlew unitTests
```

### Run Integration Tests

```bash
./gradlew integrationTests
```

### Run with Verbose Output

```bash
./gradlew verboseTest
```

### Generate Test Report

After running tests, view the HTML report:
```bash
open build/reports/tests/test/index.html
```

## Test Configuration

### Test Properties (application-test.yml)

```yaml
brobot:
  mock:
    enabled: true    # Always use mock mode in tests
    verbose: true    # Detailed logging for debugging
  action:
    max-wait: 1      # Faster timeouts for tests
    delay-between-actions: 0  # No delays in tests
```

## Best Practices

### 1. Test Naming

Use descriptive test names with @DisplayName:

```java
@Test
@DisplayName("Should authenticate valid user and update state")
void testValidAuthentication() { }
```

### 2. Test Independence

Each test should be independent:

```java
@BeforeEach
void setUp() {
    authService.logout(); // Ensure clean state
}
```

### 3. Test Coverage

Aim for comprehensive coverage:
- Happy path scenarios
- Edge cases
- Error conditions
- Null/empty inputs

### 4. Mock Mode Usage

Leverage Brobot's mock mode for predictable tests:

```java
@Test
void testWithMockMode() {
    // Mock mode automatically enabled via configuration
    // Actions are simulated, not executed
}
```

### 5. Integration Testing

Test component interactions:

```java
@SpringBootTest
@ActiveProfiles("test")
class StateTransitionIntegrationTest {
    @Autowired
    private LoginState loginState;
    
    @Autowired
    private AuthenticationService authService;
    
    // Test interactions between components
}
```

## Advanced Testing Techniques

### 1. Test Fixtures

Create reusable test data:

```java
public static TestState loginState() {
    return new StateBuilder()
        .withName("login")
        .withStateObject("username_field", "username_input.png")
        .withStateObject("password_field", "password_input.png")
        .build();
}
```

### 2. Mock Verification

Verify mock behavior:

```java
@Test
void testActionSequenceVerification() {
    actionRecorder.recordAction("CLICK", "menu_button", true);
    actionRecorder.recordAction("CLICK", "settings_option", true);
    
    boolean sequenceCorrect = actionRecorder.verifySequence(
        new String[]{"CLICK", "CLICK"},
        new String[]{"menu_button", "settings_option"}
    );
    
    assertThat(sequenceCorrect).isTrue();
}
```

### 3. Performance Testing

Test action performance:

```java
@Test
void testActionPerformance() {
    TestActionResult result = performAction();
    
    assertThat(result.duration)
        .as("Action should complete quickly")
        .isLessThan(100);
}
```

## Troubleshooting

### Common Issues

1. **Tests fail in CI but pass locally**
   - Check for timing dependencies
   - Ensure mock mode is enabled
   - Verify test data isolation

2. **Flaky tests**
   - Remove time-dependent assertions
   - Use deterministic test data
   - Avoid relying on external state

3. **Slow test execution**
   - Use @DirtiesContext sparingly
   - Minimize Spring context loading
   - Use unit tests over integration tests when possible

## Next Steps

- Add mutation testing with PITest
- Implement contract testing for transitions
- Add performance benchmarks
- Create test templates for common scenarios

For more testing guidance, see the [Brobot Testing Guide](../../../docs/docs/03-core-library/testing/).