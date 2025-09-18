# Brobot AI Implementation Guide - Complete Reference

## 🔥 QUICK REFERENCE CARD

### Do's and Don'ts at a Glance

| ✅ DO | ❌ DON'T |
|-------|---------|
| Use `Navigation.openState("Menu")` | Create a TransitionManager class |
| States: Objects only (StateImage, StateString) | States: Add methods like `clickButton()` or `isActive()` |
| Transitions: Methods only | Transitions: Create StateImage objects |
| One TransitionSet per State | Multiple transition classes per state |
| Use `ClickOptions.setPauseAfterEnd(2.0)` | Use `Thread.sleep(2000)` |
| State class: `MenuState` | State class: `Menu` |
| Navigate with: `openState("Menu")` | Navigate with: `openState("MenuState")` |
| `@State` annotation only | `@State` + `@Component` |
| Use Brobot's `Action` service | Use SikuliX directly or java.awt.Robot |

## FUNDAMENTAL PRINCIPLES

### The Brobot Architecture Philosophy

Brobot follows a strict separation of concerns:

| Component | Purpose | What it Contains | What it NEVER Contains |
|-----------|---------|------------------|------------------------|
| **State Classes** | Data containers | StateImage/StateString objects only | Methods, business logic, actions |
| **TransitionSet Classes** | Navigation logic | Methods for state transitions | StateImage objects, state data |
| **Navigation Service** | Orchestrates transitions | Path finding and execution | Direct state manipulation |
| **Action Service** | Performs UI operations | Click, type, find methods | Thread.sleep(), direct SikuliX calls |

### Key Architecture Rules

1. **States are Pure Data** - Think of them as structs or POJOs. They hold UI element definitions, nothing else.
2. **One TransitionSet Per State** - Each state has exactly ONE TransitionSet class managing ALL its transitions.
3. **Never Create a TransitionManager** - Use Navigation service. A central TransitionManager is an anti-pattern.
4. **State Names are Automatic** - Derived from class name minus "State" suffix. No StateEnum field needed.
5. **@State Includes @Component** - Never add @Component to State classes.
6. **ActionHistory is Optional** - Only needed for mock mode testing, not required for production.

## CRITICAL RULES - NEVER VIOLATE THESE

### Rule 1: NEVER Use External Functions
**These will BREAK the entire model-based automation system:**

```java
// Note: BrobotProperties must be injected as a dependency
@Autowired
private BrobotProperties brobotProperties;

// ❌ ABSOLUTELY FORBIDDEN - These break everything:
Thread.sleep(2000);                          // Breaks mock testing completely
action.pause(2.0);                           // This method DOES NOT EXIST in Brobot
java.awt.Robot robot = new Robot();          // Circumvents automation model
org.sikuli.script.Screen.wait(pattern, 5);   // Bypasses wrapper functions
org.sikuli.script.Mouse.move(location);      // Direct SikuliX calls break mocking

// ✅ CORRECT - Always use Brobot's ActionConfig options:
ClickOptions clickWithPause = new ClickOptions.Builder()
    .setPauseBeforeBegin(1.0)  // Wait 1 second before clicking
    .setPauseAfterEnd(2.0)      // Wait 2 seconds after clicking
    .build();
action.click(stateImage, clickWithPause);

PatternFindOptions findWithPause = new PatternFindOptions.Builder()
    .setPauseBeforeBegin(0.5)
    .setPauseAfterEnd(1.0)
    .setWaitTime(5.0)  // Wait up to 5 seconds for pattern to appear
    .build();
action.find(stateImage, findWithPause);
```

### Rule 2: NEVER Call Transitions Directly

```java
// ❌ WRONG - Never call transition methods directly:
@Component
public class WrongApplication {
    @Autowired
    private MenuToPricingTransition transition;

    public void run() {
        transition.execute();        // ❌ NEVER DO THIS
        transition.fromMenu();       // ❌ NEVER DO THIS
        transition.verifyArrival();  // ❌ NEVER DO THIS
    }
}

// ✅ CORRECT - Always use Navigation service:
@Component
@RequiredArgsConstructor
public class CorrectApplication {
    private final Navigation navigation;
    private final Action action;
    private final PricingState pricingState;

    public void run() {
        // Navigate using state name (WITHOUT "State" suffix)
        navigation.openState("Pricing");  // ✅ CORRECT

        // Then perform actions on the state
        action.click(pricingState.getStartButton());
    }
}
```

### Rule 3: State Naming Convention Is Mandatory

```java
// Class names MUST end with "State"
public class MenuState { }       // ✅ CORRECT
public class PricingState { }    // ✅ CORRECT
public class Menu { }             // ❌ WRONG

// Navigation uses name WITHOUT "State"
navigation.openState("Menu");     // ✅ CORRECT - for MenuState class
navigation.openState("Pricing");  // ✅ CORRECT - for PricingState class
navigation.openState("MenuState"); // ❌ WRONG - don't include "State"
```

### Rule 4: State Classes Have Objects, Not Methods

**CRITICAL: State classes are pure data containers**

```java
// ✅ CORRECT State class - ONLY objects, NO methods
@State  // @State includes @Component, don't add it separately
@Getter
public class MenuState {
    // ONLY StateImage/StateString objects
    private final StateImage logo;
    private final StateImage pricingButton;

    public MenuState() {
        // Initialize objects in constructor
        logo = new StateImage.Builder()
            .addPatterns("menu/menu-logo")
            .setName("Menu Logo")
            .build();

        pricingButton = new StateImage.Builder()
            .addPatterns("menu/menu-pricing")
            .setName("Pricing Button")
            .build();
    }
    // NO ACTION METHODS HERE!
}

// ❌ WRONG State class - has methods
@State
public class MenuState {
    private final StateImage logo;

    // ❌ WRONG - States should NOT have action methods
    public void clickLogo() {
        action.click(logo);
    }

    // ❌ WRONG - States should NOT have business logic
    public boolean isActive() {
        return action.find(logo).isSuccess();
    }
}
```

### Rule 5: Transition Classes Have Methods, Not State Objects

**CRITICAL: Transition classes contain navigation logic only**

```java
// ✅ CORRECT Transition class - methods only, receives state via DI
@TransitionSet(state = MenuState.class)
@RequiredArgsConstructor
public class MenuTransitions {
    // Only inject the state and action service
    private final MenuState menuState;  // The state this TransitionSet manages
    private final Action action;

    // Methods for navigation
    @OutgoingTransition(to = PricingState.class)
    public boolean toPricing() {
        return action.click(menuState.getPricingButton()).isSuccess();
    }

    @IncomingTransition
    public boolean verifyArrival() {
        return action.find(menuState.getLogo()).isSuccess();
    }
}

// ❌ WRONG Transition class - has StateImage objects
@TransitionSet(state = MenuState.class)
public class MenuTransitions {
    // ❌ WRONG - Don't define StateImages in transitions
    private final StateImage pricingButton;

    // ❌ WRONG - Don't initialize objects here
    public MenuTransitions() {
        pricingButton = new StateImage.Builder()...
    }
}
```

## COMPLETE PROJECT STRUCTURE

```
my-automation-project/
├── images/
│   ├── menu/
│   │   ├── menu-logo.png
│   │   ├── menu-pricing.png
│   │   └── menu-home.png
│   ├── pricing/
│   │   ├── pricing-start_for_free.png
│   │   └── pricing-header.png
│   └── homepage/
│       ├── start_for_free_big.png
│       └── enter_your_email.png
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/automation/
│   │   │       ├── Application.java           # Spring Boot main class
│   │   │       ├── states/
│   │   │       │   ├── MenuState.java
│   │   │       │   ├── PricingState.java
│   │   │       │   └── HomepageState.java
│   │   │       ├── transitions/
│   │   │       │   ├── MenuTransitions.java      # ALL transitions for Menu
│   │   │       │   ├── PricingTransitions.java   # ALL transitions for Pricing
│   │   │       │   └── HomepageTransitions.java  # ALL transitions for Homepage
│   │   │       └── runner/
│   │   │           └── AutomationRunner.java
│   │   └── resources/
│   │       ├── application.properties
│   └── test/
│       └── java/
│           └── com/example/automation/
│               └── MockAutomationTest.java
├── build.gradle
└── pom.xml  # If using Maven instead of Gradle
```

## COMPLETE WORKING EXAMPLES

### 1. Complete State Class Example

```java
package com.example.automation.states;

import io.github.jspinak.brobot.state.annotations.State;
import io.github.jspinak.brobot.stateStructure.model.state.StateImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * State classes are pure data containers - ONLY objects, NO methods
 * The state name is automatically derived from class name minus "State" suffix
 * @State annotation includes @Component - don't add @Component separately
 */
@State(initial = true)  // Mark as initial state if this is where automation starts
@Getter
@Slf4j
public class MenuState {
    // NO StateEnum field needed - state name is derived from class name

    // All UI elements in this state - ONLY StateImage/StateString objects
    private final StateImage logo;
    private final StateImage pricingButton;
    private final StateImage homeButton;
    private final StateImage searchBox;

    public MenuState() {
        log.info("Initializing MenuState");

        // Initialize each UI element with proper configuration
        // ActionHistory is OPTIONAL - only needed if you want to use mock mode
        logo = new StateImage.Builder()
            .addPatterns("menu/menu-logo")  // Path relative to images/ folder
            .setName("Menu Logo")
            .build();

        pricingButton = new StateImage.Builder()
            .addPatterns("menu/menu-pricing")
            .setName("Pricing Button")
            .build();

        homeButton = new StateImage.Builder()
            .addPatterns("menu/menu-home")
            .setName("Home Button")
            .build();

        searchBox = new StateImage.Builder()
            .addPatterns("menu/menu-search")
            .setName("Search Box")
            .build();
    }
    // NO METHODS HERE - States are data containers only!
}
```

### 2. Complete TransitionSet Class Example

```java
package com.example.automation.transitions;

import com.example.automation.states.*;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.state.annotations.FromTransition;
import io.github.jspinak.brobot.state.annotations.IncomingTransition;
import io.github.jspinak.brobot.state.annotations.TransitionSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@TransitionSet(state = PricingState.class)  // This class handles ALL transitions for PricingState
@RequiredArgsConstructor
@Slf4j
public class PricingTransitions {

    // Only needs its own state as dependency
    private final PricingState pricingState;
    private final Action action;

    /**
     * Navigate FROM Pricing TO Menu
     * Priority determines order when multiple paths exist
     */
    @OutgoingTransition(to = MenuState.class, priority = 1)
    public boolean toMenu() {
        log.info("Navigating from Pricing to Menu");

        // Add pause before clicking
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setPauseBeforeBegin(0.5)
            .setPauseAfterEnd(1.0)
            .build();

        // Click the menu button in pricing page
        return action.click(pricingState.getMenuButton(), clickOptions).isSuccess();
    }

    /**
     * Navigate FROM Pricing TO Homepage
     */
    @OutgoingTransition(to = HomepageState.class, priority = 2)
    public boolean toHomepage() {
        log.info("Navigating from Pricing to Homepage");

        // Click the home/logo button
        return action.click(pricingState.getHomeButton()).isSuccess();
    }

    /**
     * Verify arrival at Pricing state
     * This method is called after navigation to confirm arrival succeeded
     * ONLY ONE @IncomingTransition per TransitionSet class
     */
    @IncomingTransition
    public boolean verifyArrival() {
        log.info("Verifying arrival at Pricing state");

        // Wait for unique element with timeout
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setWaitTime(5.0)  // Wait up to 5 seconds
            .setPauseAfterEnd(0.5)
            .build();

        boolean found = action.find(pricingState.getUniqueElement(), findOptions).isSuccess();

        if (found) {
            log.info("Successfully arrived at Pricing state");
        } else {
            log.error("Failed to verify arrival at Pricing state");
        }

        return found;
    }
}
```

### 3. Complete Spring Boot Application Class

```java
package com.example.automation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.automation",
    "io.github.jspinak.brobot"  // REQUIRED: Scan Brobot components
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 4. Complete Automation Runner Class

```java
package com.example.automation.runner;

import com.example.automation.states.*;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.navigation.Navigation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutomationRunner implements CommandLineRunner {

    private final Navigation navigation;
    private final Action action;
    private final MenuState menuState;
    private final PricingState pricingState;
    private final HomepageState homepageState;

    @Override
    public void run(String... args) {
        log.info("Starting automation");

        try {
            // Step 1: Navigate to Pricing page
            log.info("Step 1: Navigating to Pricing");
            navigation.openState("Pricing");  // Note: "Pricing" not "PricingState"

            // Step 2: Click on "Start for Free" button
            log.info("Step 2: Clicking Start for Free");
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setPauseBeforeBegin(1.0)
                .setPauseAfterEnd(2.0)
                .build();

            if (!action.click(pricingState.getStartForFreeButton(), clickOptions).isSuccess()) {
                log.error("Failed to click Start for Free button");
                return;
            }

            // Step 3: Navigate to Homepage
            log.info("Step 3: Navigating to Homepage");
            navigation.openState("Homepage");

            // Step 4: Type email address
            log.info("Step 4: Entering email address");

            // First click the email field
            action.click(homepageState.getEmailField());

            // Then type the email
            TypeOptions typeOptions = new TypeOptions.Builder()
                .setPauseBeforeBegin(0.5)
                .setPauseAfterEnd(1.0)
                .setModifiers(new Key[]{})  // No modifier keys
                .build();

            action.type("user@example.com", typeOptions);

            // Step 5: Submit
            log.info("Step 5: Submitting form");
            action.click(homepageState.getSubmitButton());

            log.info("Automation completed successfully");

        } catch (Exception e) {
            log.error("Automation failed", e);
        }
    }
}
```

### 5. Complete application.properties Configuration

```properties
# Spring Configuration
spring.application.name=my-automation-project
spring.main.banner-mode=off

# Brobot Core Configuration
brobot.core.image-path=images/
brobot.core.mock=false
brobot.core.allow-manual-override=true

# Screenshot Configuration
brobot.screenshot.save-history=true
brobot.screenshot.history-path=history/
brobot.screenshot.include-timestamp=true
brobot.screenshot.format=png

# Logging Configuration
brobot.logging.verbosity=VERBOSE
brobot.console.actions.enabled=true
brobot.console.actions.level=VERBOSE
brobot.console.state-transitions.enabled=true
brobot.console.matches.enabled=true

# Pattern Matching Configuration
brobot.pattern.default-similarity=0.85
brobot.pattern.wait-time=5.0
brobot.pattern.scan-rate=0.3

# Mock Mode Configuration (for testing)
brobot.mock.enabled=false
brobot.mock.success-probability=0.9
brobot.mock.find-duration=0.02
brobot.mock.click-duration=0.01
brobot.mock.type-duration=0.03

# Action Timing Defaults
brobot.action.pause-before-begin=0.0
brobot.action.pause-after-end=0.0
brobot.action.move-mouse-delay=0.5
```

### 6. Complete State Class with Mock Mode Support (OPTIONAL)

```java
package com.example.automation.states;

import io.github.jspinak.brobot.state.annotations.State;
import io.github.jspinak.brobot.stateStructure.model.state.StateImage;
import io.github.jspinak.brobot.primatives.region.Region;
import io.github.jspinak.brobot.tools.testing.mock.history.MockActionHistoryFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Example of State with mock mode support
 * ActionHistory is ONLY needed if you want to test without real GUI
 * If you're only running against real application, ActionHistory is not necessary
 */
@State(initial = true)
@Getter
@Slf4j
public class MenuState {
    // State classes have ONLY objects, NO methods

    private final StateImage logo;
    private final StateImage pricingButton;
    private final StateImage homeButton;

    public MenuState() {
        log.info("Initializing MenuState");

        // Define regions where elements appear (for mock mode)
        Region logoRegion = new Region(100, 50, 150, 60);
        Region pricingRegion = new Region(300, 50, 100, 40);
        Region homeRegion = new Region(200, 50, 100, 40);

        // Create StateImages with ActionHistory for mock mode
        // WITHOUT ActionHistory, patterns will NEVER be found in mock mode!
        // But ActionHistory is OPTIONAL - only add it if you need mock testing
        logo = new StateImage.Builder()
            .addPatterns("menu/menu-logo")
            .setName("Menu Logo")
            // ActionHistory is OPTIONAL - only for mock mode testing
            .withActionHistory(MockActionHistoryFactory.reliableButton(logoRegion))
            .build();

        pricingButton = new StateImage.Builder()
            .addPatterns("menu/menu-pricing")
            .setName("Pricing Button")
            // ActionHistory determines where/how pattern is "found" in mock
            .withActionHistory(MockActionHistoryFactory.reliableButton(pricingRegion))
            .build();

        homeButton = new StateImage.Builder()
            .addPatterns("menu/menu-home")
            .setName("Home Button")
            .withActionHistory(MockActionHistoryFactory.reliableButton(homeRegion))
            .build();
    }
    // NO METHODS in State classes - they are data containers only
}
```

### 7. Running in Mock Mode

Mock mode runs the SAME production code but simulates GUI interactions using ActionHistory from StateImages. No separate test classes or @SpringBootTest needed!

#### Enabling Mock Mode

Simply set these properties in `application.properties`:

```properties
# Enable mock mode - this is the ONLY required setting
brobot.core.mock=true

# Optional: Control action success probability (default is 1.0 = 100%)
brobot.mock.action.success.probability=1.0
```

#### Running Your Automation in Mock Mode

Your existing runner class works for both live and mock mode:

```java
package com.example.automation.runner;

import com.example.automation.states.*;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.navigation.Navigation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutomationRunner implements CommandLineRunner {

    private final Navigation navigation;
    private final Action action;
    private final MenuState menuState;
    private final PricingState pricingState;
    private final HomepageState homepageState;

    @Override
    public void run(String... args) {
        // This code runs in BOTH live and mock mode
        // In mock mode, actions use ActionHistory instead of real GUI

        log.info("Starting automation (Mock mode: {})",
                io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock());

        // Navigate to Pricing
        log.info("Navigating to Pricing page");
        navigation.openState("Pricing");

        // Click Start for Free
        log.info("Clicking Start for Free button");
        var result = action.click(pricingState.getStartForFreeButton());
        log.info("Click result: {}", result.isSuccess());

        // Navigate to Homepage
        log.info("Navigating to Homepage");
        navigation.openState("Homepage");

        // Enter email
        log.info("Entering email address");
        action.click(homepageState.getEmailField());
        action.type("user@example.com");
        action.click(homepageState.getSubmitButton());

        log.info("Automation completed successfully");
    }
}
```

#### Different Configurations for Live vs Mock

Use Spring profiles or separate property files:

**application.properties** (Live/Production):

```properties
# Live mode - interact with real GUI
brobot.core.mock=false

# Other production settings
brobot.screenshot.save-history=true
brobot.logging.verbosity=VERBOSE
```

**application-mock.properties** (Mock mode):

```properties
# Enable mock mode - uses ActionHistory instead of real GUI
brobot.core.mock=true

# Optional: Control success probability (default 1.0 = 100%)
brobot.mock.action.success.probability=1.0

# Fast mock timings
brobot.mock.find-duration=0.01
brobot.mock.click-duration=0.01
brobot.mock.type-duration=0.01

# Disable screenshots in mock mode
brobot.screenshot.save-history=false
```

#### Running with Different Profiles

```bash
# Run in live mode (default)
java -jar my-automation.jar

# Run in mock mode
java -jar my-automation.jar --spring.profiles.active=mock

# Or set environment variable
export SPRING_PROFILES_ACTIVE=mock
java -jar my-automation.jar
```

#### Key Points About Mock Mode

1. **Same Code**: Your automation code is identical for live and mock mode
2. **ActionHistory Required**: StateImages MUST have ActionHistory or patterns won't be found
3. **No Test Framework**: No JUnit, no @SpringBootTest, no separate test classes
4. **Property-Driven**: Switch between modes with just `brobot.core.mock=true/false`
5. **Fast Execution**: Mock actions complete in milliseconds (0.01s vs 1-2s for real)
6. **CI/CD Ready**: Works in headless environments without displays

The mock mode is designed to validate your automation logic and flow without requiring access to the actual GUI.

## GRADLE BUILD CONFIGURATION

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.0'
}

group = 'com.example'
version = '1.0.0'
sourceCompatibility = '21'

repositories {
    mavenCentral()
}

dependencies {
    // Brobot Framework
    implementation 'io.github.jspinak:brobot:1.2.0'

    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter'

    // Lombok
    compileOnly 'org.projectlombok:lombok:1.18.32'
    annotationProcessor 'org.projectlombok:lombok:1.18.32'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
}

test {
    useJUnitPlatform()
}
```

## MAVEN POM CONFIGURATION (Alternative to Gradle)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>my-automation-project</artifactId>
    <version>1.0.0</version>

    <properties>
        <java.version>21</java.version>
        <brobot.version>1.2.0</brobot.version>
        <lombok.version>1.18.32</lombok.version>
    </properties>

    <dependencies>
        <!-- Brobot Framework -->
        <dependency>
            <groupId>io.github.jspinak</groupId>
            <artifactId>brobot</artifactId>
            <version>${brobot.version}</version>
        </dependency>

        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## COMMON ACTION PATTERNS

### Adding Pauses to Actions

```java
// Click with pauses
ClickOptions clickWithPause = new ClickOptions.Builder()
    .setPauseBeforeBegin(1.0)  // Wait 1 second before clicking
    .setPauseAfterEnd(2.0)      // Wait 2 seconds after clicking
    .setNumberOfClicks(2)       // Double-click
    .build();
action.click(stateImage, clickWithPause);

// Find with timeout and pauses
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setWaitTime(10.0)          // Wait up to 10 seconds
    .setPauseBeforeBegin(0.5)   // Pause before searching
    .setPauseAfterEnd(1.0)      // Pause after finding
    .setSimilarity(0.95)        // 95% match required
    .build();
action.find(stateImage, findOptions);

// Type with pauses
TypeOptions typeOptions = new TypeOptions.Builder()
    .setPauseBeforeBegin(0.5)
    .setPauseAfterEnd(1.0)
    .setPauseBetweenKeys(0.1)  // Pause between each keystroke
    .build();
action.type("text to type", typeOptions);

// Drag with pauses
DragOptions dragOptions = new DragOptions.Builder()
    .setPauseBeforeBegin(1.0)
    .setPauseAfterEnd(2.0)
    .setPauseBeforeMouseUp(0.5)  // Pause before releasing
    .build();
action.drag(fromImage, toImage, dragOptions);
```

### Clean Logging with ActionConfig Options

All ActionConfig builders support embedded logging messages for cleaner, more maintainable code:

```java
// PatternFindOptions with embedded logging
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .withBeforeActionLog("Searching for submit button...")
    .withSuccessLog("Submit button found at {location}")
    .withFailureLog("Submit button not found - check if page loaded")
    .setWaitTime(5.0)
    .setSimilarity(0.85)
    .build();

// ClickOptions with embedded logging
ClickOptions clickOptions = new ClickOptions.Builder()
    .withBeforeActionLog("Clicking login button...")
    .withSuccessLog("Login button clicked successfully")
    .withFailureLog("Failed to click login button")
    .setPauseAfterEnd(1.0)
    .build();

// TypeOptions with embedded logging
TypeOptions typeOptions = new TypeOptions.Builder()
    .withBeforeActionLog("Entering username...")
    .withSuccessLog("Username entered")
    .withFailureLog("Failed to enter username")
    .build();
```

#### Best Practices for Logging in Transitions

```java
@IncomingTransition
public boolean verifyArrival() {
    // Embed all logging directly in the options - no separate log statements needed
    PatternFindOptions findOptions = new PatternFindOptions.Builder()
        .withBeforeActionLog("Verifying arrival at " + stateName + "...")
        .withSuccessLog("Successfully arrived at " + stateName)
        .withFailureLog("Failed to verify arrival at " + stateName)
        .setWaitTime(5.0)
        .build();

    return action.find(stateIdentifier, findOptions).isSuccess();
}

@OutgoingTransition(to = TargetState.class)
public boolean toTarget() {
    // All logging handled by the options configuration
    ClickOptions clickOptions = new ClickOptions.Builder()
        .withBeforeActionLog("Navigating to Target...")
        .withSuccessLog("Navigation successful")
        .withFailureLog("Navigation failed - element not found")
        .setPauseBeforeBegin(0.5)
        .setPauseAfterEnd(1.0)
        .build();

    return action.click(navigationButton, clickOptions).isSuccess();
}
```

This approach eliminates the need for:
- Separate log statements before/after actions
- Manual if/else blocks for success/failure logging
- Redundant logging code across transitions

### Conditional Action Chains

```java
// Chain multiple conditional actions
ConditionalActionChain
    .find(loginButton)
    .ifFoundClick()
    .then(usernameField)
    .ifFoundType("myusername")
    .then(passwordField)
    .ifFoundType("mypassword")
    .then(submitButton)
    .ifFoundClick()
    .ifNotFoundLog("Login failed - submit button not found")
    .perform(action, objectCollection);

// With custom success handling
ConditionalActionChain
    .find(element)
    .ifFound(result -> {
        log.info("Found at: " + result.getLocation());
        return action.click(result.getLocation());
    })
    .ifNotFound(result -> {
        log.error("Element not found, trying alternative");
        return action.click(alternativeElement);
    })
    .perform(action, objectCollection);
```

### Working with Regions

```java
// Define a search region
Region searchRegion = Region.builder()
    .withX(100)
    .withY(200)
    .withWidth(500)
    .withHeight(300)
    .build();

// Search within specific region
PatternFindOptions regionSearch = new PatternFindOptions.Builder()
    .setSearchRegion(searchRegion)
    .build();
action.find(stateImage, regionSearch);

// Screen-relative regions
Region topRight = Region.builder()
    .withPosition(Positions.Name.TOPRIGHT)
    .withSize(200, 100)
    .build();

// Use Location for screen positions
Location center = new Location(Positions.Name.MIDDLEMIDDLE);
action.move(center);
```

## ERROR PATTERNS AND SOLUTIONS

### Navigation Fails
**Error**: `No path found from [CurrentState] to [TargetState]`
**Solution**: Ensure TransitionSet classes are properly annotated and scanned:
```java
@TransitionSet(state = TargetState.class)  // Must have this annotation
@RequiredArgsConstructor  // For dependency injection
public class TargetTransitions {
    @OutgoingTransition(to = NextState.class)
    public boolean toNext() { /* ... */ }

    @IncomingTransition
    public boolean verifyArrival() { /* ... */ }
}
```

### State Not Found
**Error**: `State not found: [StateName]`
**Solution**: Check state naming convention:
```java
// Class must end with "State"
@State
public class MenuState { }  // ✅ CORRECT

// Navigation uses name without "State"
navigation.openState("Menu");  // ✅ CORRECT
```

### Transition Not Executing
**Error**: Transition methods not being called
**Solution**: NEVER call transitions directly, use Navigation:
```java
// ❌ WRONG
transition.execute();

// ✅ CORRECT
navigation.openState("Target");
```

## COMMON MISTAKES TO AVOID

### ❌ Creating a TransitionManager Class
```java
// ❌ WRONG - Don't create this class!
@Component
public class TransitionManager {
    public void navigateToMenu() { ... }
    public void navigateToSettings() { ... }
}

// ✅ CORRECT - Use Navigation service
@Component
public class MyRunner {
    @Autowired
    private Navigation navigation;

    public void run() {
        navigation.openState("Menu");
    }
}
```

### ❌ Adding Methods to State Classes
```java
// ❌ WRONG - States should not have methods
@State
public class MenuState {
    private StateImage logo;

    public void clickLogo() {  // ❌ NO!
        action.click(logo);
    }

    public boolean isActive() { // ❌ NO!
        return action.find(logo).isSuccess();
    }
}

// ✅ CORRECT - States are data only
@State
public class MenuState {
    private final StateImage logo;

    public MenuState() {
        logo = new StateImage.Builder()
            .addPatterns("menu/logo")
            .build();
    }
    // NO METHODS - just getters via @Getter
}
```

### ❌ Creating StateImage Objects in Transitions
```java
// ❌ WRONG - Transitions should not create StateImages
@TransitionSet(state = MenuState.class)
public class MenuTransitions {
    private final StateImage button = new StateImage.Builder()... // ❌ NO!
}

// ✅ CORRECT - Transitions only use injected state
@TransitionSet(state = MenuState.class)
public class MenuTransitions {
    private final MenuState menuState;  // ✅ Get StateImages from here
    private final Action action;
}
```

### ❌ Using Old State Names Without "State" Suffix
```java
// ❌ WRONG - These will cause import/compilation errors
@Autowired
private MainScreen mainScreen;        // ❌ Class doesn't exist
@Autowired
private Processing processing;        // ❌ Class doesn't exist

// ✅ CORRECT - Use proper State class names
@Autowired
private MainScreenState mainScreenState;  // ✅
@Autowired
private ProcessingState processingState;  // ✅
```

### ❌ Adding StateEnum Field
```java
// ❌ WRONG - StateEnum field is not needed
@State
public class MenuState {
    private final StateEnum stateEnum = StateEnum.MENU;  // ❌ NOT NEEDED
}

// ✅ CORRECT - State name is derived automatically
@State
public class MenuState {
    // State name "Menu" is automatically derived from class name
}
```

## CHECKLIST FOR NEW BROBOT PROJECT

- [ ] Project structure follows standard layout (states/, transitions/ folders)
- [ ] All State classes end with "State"
- [ ] State classes have ONLY objects (StateImage/StateString), NO methods
- [ ] Each state has ONE TransitionSet class with ALL its transitions
- [ ] TransitionSet classes have ONLY methods, NO StateImage objects
- [ ] NO TransitionManager class exists (use Navigation service)
- [ ] @OutgoingTransition methods navigate FROM the state TO other states
- [ ] Only ONE @IncomingTransition method per TransitionSet
- [ ] Images organized in folders by state name
- [ ] application.properties configured with brobot settings
- [ ] Spring Boot main class scans both project and brobot packages
- [ ] ActionHistory configured in StateImage.Builder ONLY if mock mode needed
- [ ] NO Thread.sleep() anywhere in code
- [ ] NO direct SikuliX calls
- [ ] NO java.awt.Robot usage
- [ ] Navigation.openState() used for all state transitions
- [ ] Pauses configured via ActionConfig options, not action.pause()

## Special Keys and Keyboard Input

When typing special keys (ENTER, ESC, TAB, etc.), use the SikuliX Key constants:

```java
import org.sikuli.script.Key;

// Special keys use SikuliX Key constants (recommended)
action.type(Key.ENTER, typeOptions);  // Press ENTER
action.type(Key.ESC, typeOptions);    // Press ESC
action.type(Key.TAB, typeOptions);    // Press TAB

// Or define as StateString for reusability
StateString enterKey = new StateString.Builder()
    .withString(Key.ENTER)  // Uses SikuliX Key constant
    .setName("Enter Key")
    .build();

// Alternative: Direct Unicode (if you prefer not to import Key)
action.type("\n", typeOptions);      // ENTER
action.type("\u001b", typeOptions);  // ESC
action.type("\t", typeOptions);      // TAB
```

**Important**: Do NOT use string literals like `"ESC"` or `"ENTER"` - these will type the letters, not press the key!

For complete special keys documentation, see: [Special Keys Guide](../03-core-library/keyboard/special-keys-guide.md)

## IMPORTANT REMINDERS

1. **Brobot wraps SikuliX** - Never call SikuliX methods directly
2. **Mock mode REQUIRES ActionHistory** - Patterns will NEVER be found without it! Use withActionHistory() in StateImage.Builder
3. **@State includes @Component** - Don't add @Component to State classes
4. **@TransitionSet includes @Component** - Don't add @Component to TransitionSet classes
5. **Navigation handles pathing** - It finds the route and executes transitions automatically
6. **State suffix is removed** - MenuState becomes "Menu" in navigation
7. **Pauses are in ActionConfig** - Use setPauseBeforeBegin/setPauseAfterEnd
8. **One TransitionSet per state** - All transitions for a state in one class
9. **OutgoingTransition + IncomingTransition** - OutgoingTransitions navigate FROM the state, IncomingTransition verifies arrival
10. **Special keys use Unicode** - Use `"\n"` for ENTER, `"\u001b"` for ESC, not string literals

---

This guide contains everything needed to create a Brobot automation project. All code examples are complete and functional. Follow the patterns exactly as shown to ensure proper operation.