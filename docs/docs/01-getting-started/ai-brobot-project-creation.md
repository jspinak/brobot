# Brobot Documentation Guide for AI/LLMs

## Overview

Brobot is a Java-based GUI automation framework that provides model-based automation with states and transitions. It builds on SikuliX for pattern matching while adding Spring Boot integration, mock testing capabilities, and a clean API.

**Current Version**: 1.2.0  
**Java Version**: 21+  
**Spring Boot**: 3.2.0+

## Quick Navigation for Common Tasks

### 🚀 Getting Started
- **Installation & Setup**: [Installation Guide](01-getting-started/installation.md)
- **Quick Start Tutorial**: [Quick Start](01-getting-started/quick-start.md)
- **Core Concepts**: [Core Concepts](01-getting-started/core-concepts.md)
- **First Project Setup**: [Preparing Folder for AI Project Creation](preparing-folder-for-ai-project-creation.md)

### 📦 Core Components

#### States & Transitions
- **State Definition with @State**: [States Tutorial](03-core-library/tutorials/tutorial-basics/states.md)
- **Transition Creation with @Transition**: [Transitions Tutorial](03-core-library/tutorials/tutorial-basics/transitions.md)
- **State Management**: [States Overview](01-getting-started/states.md)
- **Transition Patterns**: [Transitions Overview](01-getting-started/transitions.md)
- **Initial State Handling**: [Initial State Architecture](03-core-library/architecture/initial-state-handling.md)

#### Actions & Automation
- **Action Hierarchy**: [Action Hierarchy](01-getting-started/action-hierarchy.md)
- **Action Configuration**: [Action Config Overview](03-core-library/action-config/01-overview.md)
- **Fluent API Reference**: [Fluent API](03-core-library/action-config/04-fluent-api.md)
- **Action Chaining**: [Action Chaining](03-core-library/action-config/07-action-chaining.md)
- **Pure Actions API**: [Pure Actions Quickstart](01-getting-started/pure-actions-quickstart.md)

#### Conditional Actions & Complex Workflows
- **ConditionalActionChain Examples**: [Conditional Chains Examples](03-core-library/action-config/15-conditional-chains-examples.md)
- **Conditional Actions Guide**: [Conditional Actions](03-core-library/action-config/09-conditional-actions.md)
- **ConditionalActionWrapper**: [Conditional Action Wrapper](03-core-library/action-config/16-conditional-action-wrapper.md)
- **Complex Workflows**: [Complex Workflows](03-core-library/action-config/08-complex-workflows.md)
- **Form Automation**: [Form Automation](03-core-library/action-config/10-form-automation.md)
- **Troubleshooting Chains**: [Troubleshooting Guide](03-core-library/action-config/troubleshooting-chains.md)

### 🧪 Testing & Mocking

#### Mock Mode & Testing
- **Testing Introduction**: [Testing Intro](04-testing/testing-intro.md)
- **Mock Mode Guide**: [Mock Mode Guide](04-testing/mock-mode-guide.md)
- **Unit Testing**: [Unit Testing](04-testing/unit-testing.md)
- **Integration Testing**: [Integration Testing](04-testing/integration-testing.md)
- **Profile-Based Testing**: [Profile-Based Testing](04-testing/profile-based-testing.md)

#### ActionHistory (Critical for Mock Testing)
- **ActionHistory Mock Snapshots**: [Mock Snapshots Guide](04-testing/actionhistory-mock-snapshots.md)
- **ActionHistory Integration Testing**: [Integration Testing](04-testing/actionhistory-integration-testing.md)
- **Mixed Mode Execution**: [Mixed Mode](04-testing/mixed-mode-execution.md)

### 🎯 Pattern Matching & Screen Capture

#### Screen Capture
- **Modular Capture System**: [Capture System](03-core-library/capture/modular-capture-system.md)
- **DPI Resolution Guide**: [DPI Guide](03-core-library/capture/dpi-resolution-guide.md)
- **Capture Quick Reference**: [Capture Quick Reference](03-core-library/capture/capture-quick-reference.md)
- **Quick Start Capture Setup**: [Capture Setup](../03-core-library/tools/quick-start-capture-setup.md)

#### Region Management
- **Screen-Adaptive Regions**: [Adaptive Regions](03-core-library/guides/screen-adaptive-regions.md)
- **Declarative Region Definition**: [Declarative Regions](03-core-library/guides/declarative-region-definition.md)
- **Search Regions & Fixed Locations**: [Search Regions](03-core-library/guides/search-regions-and-fixed-locations.md)

#### Pattern Tools
- **Pattern Creation Tools**: [Pattern Tools](../03-core-library/tools/pattern-creation-tools.md)
- **Pattern Capture Tool Guide**: [Capture Tool](../03-core-library/tools/pattern-capture-tool-guide.md)
- **Debugging Pattern Matching**: [Debug Patterns](04-testing/debugging-pattern-matching.md)

### ⚙️ Configuration

#### Application Configuration
- **Auto Configuration**: [Auto Configuration](03-core-library/configuration/auto-configuration.md)
- **Initial States Configuration**: [Initial States](03-core-library/configuration/initial-states.md)
- **Monitor Configuration**: [Monitor Config](03-core-library/guides/monitor-configuration.md)

### 📊 Logging & Debugging
- **Unified Logging System**: [Logging System](03-core-library/guides/unified-logging-system.md)
- **Modular Logging Guide**: [Modular Logging](03-core-library/guides/modular-logging-guide.md)
- **Logging Architecture**: [Logging Architecture](03-core-library/architecture/logging-architecture.md)
- **Test Logging Architecture**: [Test Logging](03-core-library/testing/test-logging-architecture.md)

### 🔧 Advanced Topics

#### Color & Motion Detection
- **Using Color**: [Color Finding](03-core-library/guides/finding-objects/using-color.md)
- **Motion Detection Testing**: [Motion Testing](03-core-library/testing/motion-detection-testing.md)
- **Color Analysis**: [Color Analysis](03-core-library/guides/labeling/color-analysis.md)

#### Migration Guides
- **ActionOptions to ActionConfig**: [Migration Guide](03-core-library/migration/actionoptions-to-actionconfig.md)

### 🎓 Tutorials

#### Basic Tutorial (Dawn of Titans)
- **Introduction**: [Tutorial Intro](03-core-library/tutorials/tutorial-basics/intro.md)
- **Setup**: [Tutorial Setup](03-core-library/tutorials/tutorial-basics/setup.md)
- **States**: [Tutorial States](03-core-library/tutorials/tutorial-basics/states.md)
- **Transitions**: [Tutorial Transitions](03-core-library/tutorials/tutorial-basics/transitions.md)
- **Live Automation**: [Live Demo](03-core-library/tutorials/tutorial-basics/live-automation.mdx)

#### MrDoob Tutorial
- **Introduction**: [MrDoob Intro](03-core-library/tutorials/tutorial-mrdoob/intro.md)
- **Setup**: [MrDoob Setup](03-core-library/tutorials/tutorial-mrdoob/setup.md)
- **Model**: [MrDoob Model](03-core-library/tutorials/tutorial-mrdoob/model.md)

#### Claude Automator Tutorial
- **Introduction**: [Claude Intro](03-core-library/tutorials/tutorial-claude-automator/intro.md)
- **Project Setup**: [Claude Setup](03-core-library/tutorials/tutorial-claude-automator/project-setup.md)
- **Configuration**: [Claude Config](03-core-library/tutorials/tutorial-claude-automator/configuration.md)

### 🔌 Integrations
- **MCP Server Installation**: [MCP Installation](06-integrations/mcp-server/installation.md)
- **MCP Getting Started**: [MCP Guide](06-integrations/mcp-server/getting-started.md)
- **MCP API Reference**: [MCP API](06-integrations/mcp-server/api-reference.md)

### 📚 Theoretical Foundations
- **Introduction**: [Theory Intro](05-theoretical-foundations/introduction.md)
- **Core Concepts**: [Theory Concepts](05-theoretical-foundations/core-concepts.md)
- **Overall Model**: [Overall Model](05-theoretical-foundations/overall-model.md)
- **States Theory**: [States Theory](05-theoretical-foundations/states.md)
- **Transitions Theory**: [Transitions Theory](05-theoretical-foundations/transitions.md)

## CRITICAL: Only Use Brobot API - Never Use External Functions

### ⚠️ ABSOLUTE RULES - NO EXCEPTIONS

**NEVER use any of these:**
- ❌ `Thread.sleep()` - Breaks mock testing and model-based automation
- ❌ `org.sikuli.script.*` direct calls - Circumvents Brobot's wrapper functions
- ❌ `java.awt.Robot` - Bypasses Brobot's automation model
- ❌ Any direct system calls or native automation

**Why this matters:**
- Brobot uses wrapper functions that decide whether to mock or execute live automation
- Using external functions circumvents the model and **nullifies ALL benefits** of model-based GUI automation
- Mock testing becomes impossible when you use direct calls
- The automation cannot be tested without a real UI

### ✅ ALWAYS Use Brobot Actions Instead

```java
// ❌ WRONG - Never do this!
Thread.sleep(2000);  // Breaks mock testing!

// ✅ CORRECT - Use Brobot's wait/pause actions
action.pause(2.0);  // Works in both mock and live mode

// ❌ WRONG - Direct SikuliX call
org.sikuli.script.Screen.wait(pattern, 5);

// ✅ CORRECT - Use Brobot's Action API
action.find(stateImage);  // Respects mock/live mode

// ❌ WRONG - Direct mouse movement
Robot robot = new Robot();
robot.mouseMove(100, 200);

// ✅ CORRECT - Use Brobot's Action API
action.move(new Location(100, 200));
```

### Waiting and Timing in Brobot

```java
// For pauses between actions
action.pause(1.5);  // Pause for 1.5 seconds

// For waiting for elements (built into find operations)
PatternFindOptions options = new PatternFindOptions.Builder()
    .setWaitTime(5.0)  // Wait up to 5 seconds
    .build();
action.find(options, stateImage);

// Navigation automatically waits for state transitions
navigation.goToState("NextState");  // Includes appropriate waits
```

## Key Patterns & Best Practices

### Project Structure
```
project-root/
├── src/main/java/com/[organization]/[project]/
│   ├── states/           # @State annotated classes
│   ├── transitions/      # @Transition annotated classes
│   ├── config/          # Spring configuration
│   └── Application.java # Spring Boot main class
├── src/main/resources/
│   └── application.yml  # Configuration
├── images/              # Pattern images
│   └── [state-name]/    # Organized by state
└── build.gradle         # Dependencies
```

### Essential Gradle Dependencies
```gradle
dependencies {
    implementation 'io.github.jspinak:brobot:1.1.0'
    annotationProcessor 'org.projectlombok:lombok:1.18.32'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

### Key Annotations
- **@State**: Marks a class as a Brobot state (includes @Component)
- **@Transition**: Marks a class as a state transition (includes @Component)
- **@Getter**: Lombok annotation for getters
- **@Slf4j**: Lombok annotation for logging
- **@RequiredArgsConstructor**: Lombok for constructor injection

### Modern API Patterns

#### State Definition
```java
@State(initial = true)  // Marks as initial state
@Getter
@Slf4j
public class HomeState {  // Class name ends with "State"
    private final StateImage button;
    
    public HomeState() {
        button = new StateImage.Builder()
            .addPatterns("home/button")
            .setName("Button")
            .build();
    }
}
```

#### Transition Organization with @TransitionSet

Brobot 1.2.0+ uses a cohesive approach where ALL transitions for a state are in ONE class:

```java
@TransitionSet(state = PricingState.class)
@RequiredArgsConstructor
@Slf4j
public class PricingTransitions {
    private final MenuState menuState;
    private final HomepageState homepageState;
    private final PricingState pricingState;
    private final Action action;
    
    // FromTransition: How to get TO Pricing FROM Menu
    @FromTransition(from = MenuState.class, priority = 1)
    public boolean fromMenu() {
        log.info("Navigating from Menu to Pricing");
        return action.click(menuState.getPricingButton()).isSuccess();
    }
    
    // FromTransition: How to get TO Pricing FROM Homepage
    @FromTransition(from = HomepageState.class, priority = 2)
    public boolean fromHomepage() {
        log.info("Navigating from Homepage to Pricing");
        return action.click(homepageState.getPricingLink()).isSuccess();
    }
    
    // ToTransition: Verify we've ARRIVED at Pricing
    @ToTransition
    public boolean verifyArrival() {
        log.info("Verifying arrival at Pricing state");
        return action.find(pricingState.getUniqueElement()).isSuccess();
    }
}
```

**Key Points**: 
- **ONE class per state** containing all its transitions
- **@FromTransition methods** handle navigation FROM other states
- **@ToTransition method** verifies arrival (only ONE per class)
- **High cohesion** - easy to find all transitions for a state
- Transitions execute in sequence: FromTransition → ToTransition

#### CRITICAL: Navigation Usage
```java
// WRONG - Never call transitions directly!
@Component
public class WrongRunner {
    @Autowired
    private HomeToNextTransition transition;
    
    public void run() {
        transition.execute();  // ❌ WRONG - Don't do this!
    }
}

// CORRECT - Use Navigation service
@Component
@RequiredArgsConstructor
public class CorrectRunner {
    private final Navigation navigation;
    private final Action action;
    private final NextState nextState;
    
    public void run() {
        // Navigate using state name (without "State" suffix)
        navigation.goToState("Next");  // ✅ CORRECT
        
        // Then perform actions on the state
        action.click(nextState.getButton());
    }
}
```

#### State Naming Convention
- **Class naming**: Always end with "State" (e.g., `MenuState`, `PricingState`)
- **@State annotation**: Automatically removes "State" suffix for the state name
- **Navigation**: Use the name WITHOUT "State":
  ```java
  // Class: MenuState → navigation.goToState("Menu")
  // Class: PricingState → navigation.goToState("Pricing")
  // Class: HomepageState → navigation.goToState("Homepage")
  ```

#### ConditionalActionChain Pattern
```java
ConditionalActionChain
    .find(element1)
    .ifFoundClick()
    .then(element2)      // Sequential composition
    .ifFoundType("text")
    .then(element3)
    .ifFoundClick()
    .perform(action, objectCollection);
```

### Configuration Best Practices

Always configure through `application.yml` or `application.properties`:

```yaml
brobot:
  core:
    image-path: images/
    mock: false
  screenshot:
    save-history: true
    history-path: history/
  logging:
    verbosity: VERBOSE
```

**Never** set FrameworkSettings fields directly (deprecated).

### Testing Best Practices

1. **Always use ActionHistory in mock mode** - Required for patterns to be found
2. **Use MockActionHistoryFactory** for common UI patterns
3. **Enable mock mode in test properties**: `brobot.mock.enabled=true`
4. **Use @ActiveProfiles("test")** in Spring tests

## Important Notes for AI/LLMs

### Critical Points to Remember

1. **NEVER use Thread.sleep(), Robot, or ANY external functions** - Only use Brobot API
2. **Brobot does NOT call SikuliX methods directly** - Always use Brobot's Action API
3. **Use @TransitionSet with method-level annotations** - All transitions for a state in ONE class
4. **@FromTransition methods** navigate TO the state FROM other states
5. **@ToTransition method** (only ONE per class) verifies arrival at the state
6. **@State and @TransitionSet include @Component** - No need to add @Component separately
7. **ActionHistory is REQUIRED for mock mode** - Patterns won't be found without it
8. **Configure via properties files** - Don't set FrameworkSettings fields directly
9. **NEVER call transitions directly** - Use `Navigation.goToState("StateName")` instead
10. **State naming convention** - Classes end with "State" but navigation uses name without "State"
    - Class: `PricingState` → Navigate: `navigation.goToState("Pricing")`

### Common Pitfalls to Avoid

- ❌ Using `Thread.sleep()` anywhere in the code - Use `action.pause()` instead
- ❌ Using `java.awt.Robot` or any system automation outside Brobot
- ❌ Using `org.sikuli.script.Screen` directly
- ❌ Scattering transitions across multiple classes instead of using @TransitionSet
- ❌ Having multiple @ToTransition methods in one @TransitionSet class
- ❌ Forgetting to annotate transition class with @TransitionSet
- ❌ Adding `@Component` to `@State` or `@TransitionSet` classes (already included)
- ❌ Using `ActionOptions` instead of specific config classes
- ❌ Setting `FrameworkSettings.mock = true` directly
- ❌ Forgetting ActionHistory in mock mode tests
- ❌ Calling transition methods directly instead of using Navigation
- ❌ Using full class name in `navigation.goToState()` (e.g., "PricingState" instead of "Pricing")

### When Helping Users

1. **Check the version** - This guide is for Brobot 1.2.0+
2. **Use modern patterns** - @State/@TransitionSet annotations, ActionConfig classes
3. **Link to specific docs** - Use the links above for detailed information
4. **Test with mock mode** - Ensure ActionHistory is configured
5. **Follow Spring Boot conventions** - Dependency injection, profiles, etc.

## Additional Resources

- **GitHub Repository**: [github.com/jspinak/brobot](https://github.com/jspinak/brobot)
- **Issue Tracker**: [GitHub Issues](https://github.com/jspinak/brobot/issues)
- **Brobot Name Origin**: [Why "Brobot"?](../05-theoretical-foundations/background/brobot-name-origin.md)

---

*This guide is optimized for AI/LLM consumption. For human-readable documentation, start with the [Getting Started Guide](01-getting-started/quick-start.md).*