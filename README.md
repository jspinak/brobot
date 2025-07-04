<div align="center">
<a href="https://jspinak.github.io/brobot">
  <img src="https://jspinak.github.io/brobot/img/brobot-landscape4.png" alt="Brobot - Visual Automation Framework" width="600">
</a>

<h3>Build Intelligent Visual Automations with Java</h3>

<p>
  <a href="https://jspinak.github.io/brobot/docs/getting-started/quick-start"><img src="https://img.shields.io/badge/docs-quickstart-blue" alt="Quickstart"></a>
  <a href="https://maven-badges.herokuapp.com/maven-central/io.github.jspinak/brobot"><img src="https://img.shields.io/maven-central/v/io.github.jspinak/brobot.svg?label=Maven%20Central" alt="Maven Central"></a>
  <a href="./LICENSE"><img src="https://img.shields.io/badge/license-MIT-green.svg" alt="License: MIT"></a>
  <a href="https://github.com/jspinak/brobot/actions"><img src="https://img.shields.io/github/actions/workflow/status/jspinak/brobot/ci.yml?branch=main" alt="Build Status"></a>
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
stateTransitions.navigateTo(LoginState.class);
// Brobot automatically finds the best path, handles errors, and recovers from failures
```

## 🚀 Quick Start

### Installation

#### Maven
```xml
<dependency>
    <groupId>io.github.jspinak</groupId>
    <artifactId>brobot</artifactId>
    <version>1.0.7</version>
</dependency>
```

#### Gradle
```groovy
implementation 'io.github.jspinak:brobot:1.0.7'
```

### Your First Automation

```java
@Component
public class SimpleAutomation {
    
    @Autowired
    private ActionService actionService;
    
    public void clickButton() {
        // Define what to look for
        StateImage button = new StateImage.Builder()
                .setName("button.png")
                .build();
        
        // Configure the search
        PatternFindOptions findOptions = PatternFindOptions.forPreciseSearch();
        
        // Find and click
        ActionResult result = performAction(findOptions, button);
        if (result.isSuccess()) {
            performClick(result.getMatchList());
        }
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
- [JavaDoc (v1.0.6)](https://jspinak.github.io/brobot/api/1.0.6/)
- [Examples](https://github.com/jspinak/brobot/tree/main/examples)

</td>
</tr>
</table>

## 🎮 Use Cases

### Game Automation
Build sophisticated game bots that understand game states and react intelligently:
```java
// Brobot understands the game state and makes decisions
if (stateService.isActive(BattleState.class)) {
    combatAI.executeOptimalStrategy();
} else {
    navigation.moveToObjective();
}
```

### Visual Testing
Create maintainable visual regression tests:
```java
@Test
void loginPageShouldDisplayCorrectly() {
    stateTransitions.navigateTo(LoginState.class);
    assertTrue(visualValidator.matches(LoginState.class));
}
```

### Process Automation
Automate complex workflows across multiple applications:
```java
// Brobot handles window switching, popups, and unexpected dialogs
workflow.extractDataFromApp1()
        .processInApp2()
        .uploadToApp3();
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