# Enhanced Mocking Example

This example demonstrates conceptual designs for Brobot's advanced mock testing capabilities. Due to the complexity of the enhanced mocking system, this example focuses on illustrating the key concepts and patterns rather than providing a fully functional implementation.

## Overview

The Enhanced Mocking system design provides sophisticated tools for creating realistic test scenarios:

- **Scenario-Based Testing**: Configure complex test scenarios with failure patterns, temporal conditions, and cascading behaviors
- **Behavior Verification**: Verify action sequences, timing, state transitions, and recovery behaviors
- **Structured Test Data**: Build and manage test data with variations and transformations
- **Temporal Conditions**: Define time-based behaviors and state changes
- **Failure Patterns**: Configure realistic failure scenarios with recovery mechanisms

**Note**: This example contains conceptual code demonstrating the architecture and design patterns. For a working implementation, you would need to integrate with the full Brobot library.

## Project Structure

```
enhanced-mocking/
├── src/main/java/com/example/mocking/
│   ├── EnhancedMockingApplication.java    # Main Spring Boot application
│   ├── config/
│   │   ├── MockScenarioConfig.java        # Scenario configuration
│   │   ├── FailurePattern.java            # Failure pattern definitions
│   │   └── TemporalConditions.java        # Time-based conditions
│   ├── MockScenarioManager.java           # Scenario lifecycle management
│   ├── verification/
│   │   ├── MockBehaviorVerifier.java      # Behavior verification system
│   │   ├── StateTransitionVerification.java # State transition tracking
│   │   ├── Verification.java              # Verification interface
│   │   ├── VerificationResult.java        # Verification result enum
│   │   └── VerificationReport.java        # Verification reporting
│   ├── data/
│   │   ├── TestDataBuilder.java           # Test data builder
│   │   ├── TestScenario.java              # Test scenario structure
│   │   └── TestVariation.java             # Scenario variations
│   └── examples/
│       ├── ScenarioBasedTestExample.java  # Scenario testing examples
│       ├── BehaviorVerificationExample.java # Verification examples
│       ├── StructuredTestDataExample.java # Test data examples
│       └── EnhancedMockingExampleRunner.java # Example runner
└── src/main/resources/
    └── application.yml                     # Application configuration
```

## Key Features

### 1. Scenario-Based Testing

Create complex test scenarios with multiple conditions:

```java
MockScenarioConfig config = MockScenarioConfig.builder()
    .scenarioName("degrading_performance")
    .description("System performance degrades over time")
    .maxDuration(Duration.ofMinutes(5))
    .build();

// Configure temporal conditions
TemporalConditions perfConditions = TemporalConditions.builder()
    .conditionName("performance_degradation")
    .activateAfter(Duration.ofMinutes(1))
    .build();

// Add failure patterns
FailurePattern slowClicks = FailurePattern.builder()
    .patternName("slow_clicks")
    .failureRate(0.3)
    .delayBeforeFailure(Duration.ofSeconds(2))
    .cascadesToActions(Map.of(
        ActionType.TYPE, 0.5
    ))
    .build();
```

### 2. Behavior Verification

Verify complex action sequences and behaviors:

```java
// Verify action sequences
behaviorVerifier.expectActionSequence(Arrays.asList(
    ActionType.CLICK,
    ActionType.TYPE,
    ActionType.CLICK
)).within(Duration.ofSeconds(10));

// Verify timing constraints
behaviorVerifier.expectActionTiming(ActionType.CLICK)
    .maxDuration(Duration.ofMillis(500))
    .forNextActions(3);

// Verify conditional behaviors
behaviorVerifier.expectConditionalBehavior()
    .when(() -> clickCount.get() < 3)
    .thenExpect(ActionType.CLICK)
    .toSucceed();
```

### 3. Structured Test Data

Build and manage test data with variations:

```java
TestScenario checkoutScenario = testDataBuilder.scenario("checkout")
    .withDescription("E-commerce checkout flow")
    .withStateImage("cart_icon", "shopping_cart")
    .withStateImage("checkout_button", "checkout_btn")
    .withRegion("checkout_form", new Region(400, 150, 1120, 700))
    .withTag("e-commerce")
    
    // Add mobile variation
    .withVariation("mobile")
        .withTransformation("scale_regions", 
            TestVariation.Transformations.scaleRegion(0.5))
    .endVariation()
    
    // Add international variation
    .withVariation("international")
        .withTransformation("replace_currency",
            TestVariation.Transformations.replaceString("$", "€"))
    .endVariation()
    
    .build();

// Use variations
TestScenario mobileCheckout = checkoutScenario.withVariation("mobile");
TestScenario internationalCheckout = checkoutScenario.withVariation("international");
```

## Implementation Notes

This example provides architectural patterns and design concepts for enhanced mocking. To use these patterns in your project:

1. **Adapt the Interfaces**: The interfaces and classes shown here demonstrate the design patterns. You'll need to adapt them to work with your specific Brobot setup.

2. **Integrate with Brobot**: The mock scenario manager and behavior verifier would need to integrate with Brobot's action execution system.

3. **Extend as Needed**: The patterns shown here are starting points. You can extend them with additional features like:
   - More sophisticated failure patterns
   - Complex state machine verification
   - Performance profiling and analysis
   - Integration with CI/CD pipelines

## Configuration

### Application Configuration (application.yml)

```yaml
brobot:
  mock:
    enabled: true
    verbose: true
    delay:
      min: 100
      max: 500
    success-rate: 0.95

enhanced-mock:
  scenarios:
    default-timeout: 300000  # 5 minutes
    enable-reporting: true
  verification:
    strict-mode: false
    timing-precision: milliseconds
  test-data:
    cache-scenarios: true
    enable-variations: true
```

## Use Cases

### 1. Testing System Degradation

Simulate how your automation handles performance degradation:

```java
// System starts fast, then slows down
TemporalConditions degradation = TemporalConditions.builder()
    .activateAfter(Duration.ofMinutes(1))
    .build();

FailurePattern slowness = FailurePattern.builder()
    .delayBeforeFailure(Duration.ofSeconds(3))
    .build();
```

### 2. Testing Error Recovery

Verify that your automation properly recovers from failures:

```java
behaviorVerifier.expectRecoveryBehavior()
    .afterFailureOf(ActionType.CLICK)
    .expectRetryWithin(Duration.ofSeconds(2))
    .maxRetries(3);
```

### 3. Testing Different Environments

Use test data variations for different environments:

```java
// Desktop version
TestScenario desktop = baseScenario;

// Mobile version with scaled regions
TestScenario mobile = baseScenario.withVariation("mobile");

// Different locale
TestScenario french = baseScenario.withVariation("fr_FR");
```

### 4. Testing State Machine Behavior

Verify complex state transitions:

```java
StateTransitionVerification verification = 
    new StateTransitionVerification(Arrays.asList(
        "login", "dashboard", "settings", "logout"
    ));

// Your automation runs...

if (verification.verify()) {
    log.info("State transitions followed expected path");
}
```

## Best Practices

1. **Scenario Design**
   - Keep scenarios focused on specific test objectives
   - Use meaningful names and descriptions
   - Document expected behaviors

2. **Failure Patterns**
   - Start with realistic failure rates (5-20%)
   - Use cascading failures to test error propagation
   - Include recovery delays for realistic behavior

3. **Behavior Verification**
   - Verify both positive and negative cases
   - Use timing verification to catch performance issues
   - Check state transitions for workflow validation

4. **Test Data Management**
   - Use predefined scenarios for common cases
   - Create variations for different environments
   - Keep test data versioned and documented

## Troubleshooting

### Common Issues

1. **Scenario Not Activating**
   - Check temporal conditions timing
   - Verify scenario is started before actions
   - Check logs for activation messages

2. **Verification Failures**
   - Review expected vs actual sequences
   - Check timing constraints
   - Verify mock mode is enabled

3. **Test Data Issues**
   - Ensure all referenced images exist
   - Check region coordinates
   - Verify transformations are compatible

### Debug Mode

Enable debug logging in application.yml:

```yaml
logging:
  level:
    com.example.mocking: DEBUG
    io.github.jspinak.brobot: DEBUG
```

## Next Steps

- Explore the individual example classes for detailed implementations
- Create custom scenarios for your specific testing needs
- Integrate with your CI/CD pipeline for automated testing
- Combine with Brobot's reporting features for test analysis

For more information, see the [Enhanced Mocking documentation](../../../docs/docs/03-core-library/testing/enhanced-mocking.md).