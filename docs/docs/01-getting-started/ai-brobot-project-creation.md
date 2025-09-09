# Brobot Documentation Guide for AI/LLMs

## Overview

Brobot is a Java-based GUI automation framework that provides model-based automation with states and transitions. It builds on SikuliX for pattern matching while adding Spring Boot integration, mock testing capabilities, and a clean API.

**Current Version**: 1.1.0  
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
public class HomeState {
    private final StateImage button;
    
    public HomeState() {
        button = new StateImage.Builder()
            .addPatterns("home/button")
            .setName("Button")
            .build();
    }
}
```

#### Transition Definition
```java
@Transition(from = HomeState.class, to = NextState.class)
@RequiredArgsConstructor
@Slf4j
public class HomeToNextTransition {
    private final HomeState homeState;
    private final Action action;
    
    public boolean execute() {
        return action.click(homeState.getButton()).isSuccess();
    }
}
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
3. **Enable mock mode in test properties**: `brobot.core.mock=true`
4. **Use @ActiveProfiles("test")** in Spring tests

## Important Notes for AI/LLMs

### Critical Points to Remember

1. **Brobot does NOT call SikuliX methods directly** - Always use Brobot's Action API
2. **@State and @Transition include @Component** - No need to add @Component separately
3. **ActionHistory is REQUIRED for mock mode** - Patterns won't be found without it
4. **Use ActionConfig classes, NOT ActionOptions** - ActionOptions is deprecated
5. **Configure via properties files** - Don't set FrameworkSettings fields directly

### Common Pitfalls to Avoid

- ❌ Using `org.sikuli.script.Screen` directly
- ❌ Adding `@Component` to `@State` or `@Transition` classes
- ❌ Using `ActionOptions` instead of specific config classes
- ❌ Setting `FrameworkSettings.mock = true` directly
- ❌ Forgetting ActionHistory in mock mode tests

### When Helping Users

1. **Check the version** - This guide is for Brobot 1.1.0+
2. **Use modern patterns** - @State/@Transition annotations, ActionConfig classes
3. **Link to specific docs** - Use the links above for detailed information
4. **Test with mock mode** - Ensure ActionHistory is configured
5. **Follow Spring Boot conventions** - Dependency injection, profiles, etc.

## Additional Resources

- **GitHub Repository**: [github.com/jspinak/brobot](https://github.com/jspinak/brobot)
- **Issue Tracker**: [GitHub Issues](https://github.com/jspinak/brobot/issues)
- **Brobot Name Origin**: [Why "Brobot"?](../05-theoretical-foundations/background/brobot-name-origin.md)

---

*This guide is optimized for AI/LLM consumption. For human-readable documentation, start with the [Getting Started Guide](01-getting-started/quick-start.md).*