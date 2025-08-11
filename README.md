<div align="center">
<a href="https://jspinak.github.io/brobot">
  <img src="https://jspinak.github.io/brobot/img/brobot-landscape4.png" alt="Brobot - Visual Automation Framework" width="600">
</a>

<h3>Build Intelligent Visual Automations with Java</h3>

<p>
  <a href="https://jspinak.github.io/brobot/docs/getting-started/quick-start"><img src="https://img.shields.io/badge/docs-quickstart-blue" alt="Quickstart"></a>
  <a href="https://maven-badges.herokuapp.com/maven-central/io.github.jspinak/brobot"><img src="https://img.shields.io/maven-central/v/io.github.jspinak/brobot.svg?label=Maven%20Central" alt="Maven Central"></a>
  <a href="./LICENSE"><img src="https://img.shields.io/badge/license-MIT-green.svg" alt="License: MIT"></a>
  <a href="https://github.com/jspinak/brobot/actions"><img src="https://img.shields.io/github/actions/workflow/status/jspinak/brobot/main.yml?branch=main" alt="Build Status"></a>
  <a href="https://jspinak.github.io/brobot"><img src="https://img.shields.io/badge/website-live-brightgreen" alt="Website"></a>
</p>

<p>
  <a href="#-why-brobot">Why Brobot</a> •
  <a href="#-quick-start">Quick Start</a> •
  <a href="#-features">Features</a> •
  <a href="#-use-cases">Use Cases</a> •
  <a href="#-documentation">Documentation</a> •
  <a href="#-contributing">Contributing</a>
</p>

</div>

---

## 🤖 Why Brobot?

**Brobot** is a powerful Java framework that combines SikuliX and OpenCV to create intelligent visual automations. Unlike traditional GUI automation tools, Brobot uses a **state-based approach** that makes your automations resilient, maintainable, and truly intelligent.

### 🎯 Perfect for:
- **🎮 Game Automation** - Build bots that can play games autonomously
- **🧪 Visual Testing** - Create robust image-based test suites
- **🔄 Process Automation** - Automate repetitive visual tasks
- **🔬 Research** - Develop visual APIs for AI/ML experiments

### 💡 What makes Brobot different?

```java
// Traditional automation: brittle and sequential
click(100, 200);
wait(2);
type("username");
click(100, 250);
wait(2);
type("password");

// Brobot: intelligent and state-aware
stateNavigator.openState("login");
// Brobot automatically finds the best path, handles errors, and recovers from failures
```

## 🚀 Quick Start

### Installation

#### Maven
```xml
<dependency>
    <groupId>io.github.jspinak</groupId>
    <artifactId>brobot</artifactId>
    <version>1.1.0</version>
</dependency>
```

#### Gradle
```groovy
implementation 'io.github.jspinak:brobot:1.1.0'
```

### Your First Automation

```java
@Component
public class SimpleAutomation {
    
    @Autowired
    private Action action;
    
    public void actOnButton() {
        // Define what to look for
        StateImage button = new StateImage.Builder()
                .setName("button.png")
                .build();
        
        // Configure the search
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSimilarity(0.7)
                .build();
        
        // Find 
        ActionResult findResult = action.perform(findOptions, button);

        if (findResult.isSuccess()) {
        // Get the first match
        Match firstMatch = findResult.getFirstMatch();
    
        // Get all matches
        List<Match> allMatches = findResult.getMatchList();
    
        // Work with each match
        for (Match match : allMatches) {
            // Highlight each found instance
            action.perform(ActionType.HIGHLIGHT, match.getRegion());
        
            // Click each one
            action.perform(ActionType.CLICK, match.getRegion());
    }
}
```

[**📖 View Full Quickstart Guide →**](https://jspinak.github.io/brobot/docs/01-getting-started/quick-start)

## ✨ Features

### 🎯 State-Based Architecture
Model your application as states and transitions, not just sequences of clicks. Brobot automatically handles navigation, error recovery, and dynamic UI changes.

### 🔍 Advanced Image Recognition
- **Multi-pattern matching** with similarity thresholds
- **Color-based detection** for dynamic content
- **Motion tracking** for moving objects
- **OCR support** for text recognition

### 🛡️ Built-in Resilience
- **Automatic error recovery** - Brobot recalculates paths when things go wrong
- **Dynamic state detection** - Knows where it is at all times
- **Smart waiting** - No more arbitrary sleep() calls

### 🧪 Testable Automations
Unlike traditional GUI automation, Brobot automations are **fully testable**:
```java
// Test your automation logic without running the GUI
BrobotSettings.mock = true;
// Your tests run with simulated responses
```

### 🔧 Modern Java API
- **Type-safe builders** for all configurations
- **Spring Boot integration** out of the box
- **Comprehensive logging** and debugging tools
- **JavaFX Desktop Runner** for visual automation development

## 📚 Documentation

<table>
<tr>
<td width="33%" valign="top">

### Getting Started
- [Quick Start Guide](https://jspinak.github.io/brobot/docs/getting-started/quick-start)
- [Installation](https://jspinak.github.io/brobot/docs/getting-started/installation)
- [Core Concepts](https://jspinak.github.io/brobot/docs/getting-started/core-concepts)
- [Your First State](https://jspinak.github.io/brobot/docs/getting-started/states)

</td>
<td width="33%" valign="top">

### Guides
- [Finding Objects](https://jspinak.github.io/brobot/docs/core-library/guides/finding-objects/combining-finds-v2)
- [State Management](https://jspinak.github.io/brobot/docs/core-library/tutorials/tutorial-basics/states-v2)
- [Color Detection](https://jspinak.github.io/brobot/docs/core-library/guides/finding-objects/using-color-v2)
- [Motion Tracking](https://jspinak.github.io/brobot/docs/core-library/guides/finding-objects/movement-v2)

</td>
<td width="33%" valign="top">

### API Reference
- [Migration Guide (v1.1.0)](https://jspinak.github.io/brobot/docs/core-library/guides/migration-guide)
- [Action Hierarchy](https://jspinak.github.io/brobot/docs/getting-started/action-hierarchy)
- [JavaDoc (v1.0.7)](https://jspinak.github.io/brobot/api/1.0.7/)
- [JavaDoc (all versions)](https://jspinak.github.io/brobot/api/)
- [Example: Login Automation](https://github.com/jspinak/brobot/blob/main/examples/LoginAutomationExample.java)

</td>
</tr>
</table>

## 🎮 Use Cases

### Game Automation
Build sophisticated game bots that understand game states and react intelligently:
```java
// Define game states
@State(initial = true)
@Getter
public class HomeBaseState {
    private final StateImage baseFlag;
    private final StateImage attackButton;
    
    public HomeBaseState() {
        baseFlag = new StateImage.Builder()
            .setName("home-flag")
            .addPatterns("game/home-flag")
            .build();
            
        attackButton = new StateImage.Builder()
            .setName("attack-button")
            .addPatterns("game/attack-button")
            .build();
    }
}

@State
@Getter
public class EnemyBaseState {
    private final StateImage enemyFlag;
    private final StateImage enemyUnits;
    
    public EnemyBaseState() {
        enemyFlag = new StateImage.Builder()
            .setName("enemy-flag")
            .addPatterns("game/enemy-flag")
            .build();
            
        enemyUnits = new StateImage.Builder()
            .setName("enemy-units")
            .addPatterns("game/enemy-units")
            .build();
    }
}

// Define transition from Home Base to Enemy Base
@Transition(from = HomeBaseState.class, to = EnemyBaseState.class)
@RequiredArgsConstructor
public class AttackEnemyBaseTransition {
    private final HomeBaseState homeBase;
    private final Action action;
    
    public boolean execute() {
        // Click attack button to navigate to enemy base
        ActionResult result = action.click(homeBase.getAttackButton());
        return result.isSuccess();
    }
}

// To go to the enemy base
stateNavigator.openState("EnemyBase");

```

### Visual Testing
Create maintainable visual regression tests:
```java
@Test
void loginPageShouldDisplayCorrectly() {
    // Define expected UI elements
    StateImage logo = new StateImage.Builder()
        .setName("company-logo")
        .addPatterns("login/logo")
        .setSimilarity(0.95)  // High similarity for visual tests
        .build();
    
    StateImage loginForm = new StateImage.Builder()
        .setName("login-form")
        .addPatterns("login/form")
        .build();
    
    // Verify all elements are present
    ActionResult logoResult = action.find(logo);
    assertTrue(logoResult.isSuccess(), "Logo should be visible");
    
    ActionResult formResult = action.find(loginForm);
    assertTrue(formResult.isSuccess(), "Login form should be visible");
}
```

### Process Automation
Automate complex workflows across multiple applications:
```java
// Define UI elements
StateImage exportButton = new StateImage.Builder()
    .addPatterns("app/export-button")
    .build();

StateImage fileNameField = new StateImage.Builder()
    .addPatterns("dialog/filename-field")
    .build();

// Complete workflow in one conditional chain
ConditionalActionChain
    .find(new PatternFindOptions.Builder().build())  // Find export button
    .ifFoundClick()     // Click it
    .then(new PatternFindOptions.Builder().build())  // Find filename field
    .ifFoundType("export_" + System.currentTimeMillis() + ".csv")  // Type filename
    .perform(action, new ObjectCollection.Builder()
        .withImages(exportButton, fileNameField)
        .build());
```

## 🏗️ Architecture

```
brobot/
├── library/          # Core Brobot framework
├── library-test/     # Integration tests with GUI
├── runner/           # JavaFX Desktop Runner (in development)
└── docs/             # Documentation website
```

### Test Organization
- **Unit tests** (`library/`) - Fast, isolated component tests
- **Integration tests** (`library-test/`) - Full GUI automation tests with Spring context

## 🤝 Contributing

We love contributions! Whether it's bug reports, feature requests, documentation improvements, or code contributions, all are welcome.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

See [CONTRIBUTING.md](./CONTRIBUTING.md) for detailed guidelines.

## 🛠️ Development Setup

```bash
# Clone the repository
git clone https://github.com/jspinak/brobot.git
cd brobot

# Build the project
./gradlew build

# Run tests
./gradlew test

# Run integration tests
./gradlew :library-test:test
```

## 📄 License

Brobot is [MIT licensed](./LICENSE). The documentation is [Creative Commons licensed](./LICENSE-docs).

## 🙏 Acknowledgments

Brobot is built on the shoulders of giants:
- [SikuliX](http://sikulix.com/) - Computer vision engine
- [OpenCV](https://opencv.org/) - Image processing
- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework

## 🌟 Star History

[![Star History Chart](https://api.star-history.com/svg?repos=jspinak/brobot&type=Date)](https://star-history.com/#jspinak/brobot&Date)

---

<div align="center">
  <sub>Built with ❤️ by <a href="https://github.com/jspinak">jspinak</a> and <a href="https://github.com/jspinak/brobot/graphs/contributors">contributors</a></sub>
</div>