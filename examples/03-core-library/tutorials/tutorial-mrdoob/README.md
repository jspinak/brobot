# Tutorial: Mr.doob - Brobot 1.1.0

This example demonstrates how to create a simple automation application using Brobot 1.1.0 with the new annotation system. The tutorial shows how to interact with the Mr.doob website (https://mrdoob.com), specifically navigating through the homepage, harmony drawing application, and about page.

## Overview

This tutorial demonstrates:
- Manual state structure creation using `@State` annotations
- Basic navigation between states using `@Transition` annotations  
- Handling dynamic web content
- Live automation execution with Brobot 1.1.0

## Prerequisites

- Java 21 or higher
- Spring Boot 3.3.0
- Brobot 1.1.0
- Basic knowledge of Java and Spring Boot

## Project Structure

```
tutorial-mrdoob/
├── build.gradle                 # Project dependencies and build configuration
├── settings.gradle              # Project settings
├── README.md                   # This file
├── images/                     # Screenshots for image matching
│   ├── README.md              # Instructions for capturing images
│   ├── harmonyIcon.png        # Screenshot of harmony icon from homepage
│   ├── aboutButton.png        # Screenshot of about button from harmony page
│   └── aboutText.png          # Screenshot of about text from about page
└── src/main/
    ├── java/com/example/mrdoob/
    │   ├── MrdoobApplication.java           # Main Spring Boot application
    │   ├── AutomationInstructions.java     # Automation workflow definition
    │   ├── states/                         # State definitions
    │   │   ├── Homepage.java               # Homepage state
    │   │   ├── Harmony.java                # Harmony drawing page state
    │   │   └── About.java                  # About page state
    │   └── transitions/                    # State transition definitions
    │       ├── HomepageToHarmonyTransition.java
    │       └── HarmonyToAboutTransition.java
    └── resources/
        └── application.yml                  # Application configuration
```

## Key Features

### New Annotation System (Brobot 1.1.0)

This tutorial showcases the improved annotation-based approach:

**State Definition:**
```java
@State(initial = true)  // Marks the starting state
@Getter
@Slf4j
public class Homepage {
    private final StateImage harmony;
    // No manual State.Builder or registration needed!
}
```

**Transition Definition:**
```java
@Transition(from = Homepage.class, to = Harmony.class)
@RequiredArgsConstructor
@Slf4j
public class HomepageToHarmonyTransition {
    public boolean execute() {
        return action.perform(CLICK, homepage.getHarmony()).isSuccess();
    }
}
```

### Benefits of the New System

1. **Automatic Registration**: No manual state or transition registration
2. **Less Boilerplate**: Cleaner, more focused code
3. **Declarative**: Clear intent with `@State` and `@Transition` annotations
4. **Type Safety**: Compile-time checking of state relationships

## How to Run

### 1. Prepare Images

Before running the automation, you need to capture actual screenshots of the Mr.doob website UI elements:

1. Navigate to https://mrdoob.com
2. Take screenshots of:
   - The harmony icon on the homepage → save as `images/harmonyIcon.png`
   - The about button/link on the harmony page → save as `images/aboutButton.png`
   - The about text on the about page → save as `images/aboutText.png`

### 2. Build and Run

```bash
# Build the project
./gradlew build

# Run the automation
./gradlew bootRun
```

### 3. Expected Behavior

The automation will:
1. Start from the Mr.doob homepage
2. Click on the Harmony icon to navigate to the drawing application
3. Click on the About button to navigate to the about page
4. Successfully reach the about page and verify it loaded

## Configuration

The application is configured via `application.yml`:
- **Mock Mode**: Set to `false` for live automation
- **Verbose Logging**: Enabled to show automation progress
- **Auto-scan**: Automatically discovers `@State` and `@Transition` classes
- **Timeouts**: Configured for web automation delays

## State Model

The automation models the Mr.doob website as a simple state machine:

```
[Homepage] --click harmony--> [Harmony] --click about--> [About]
```

Each state contains the UI elements needed for:
- **State Identification**: Recognizing when the state is active
- **Navigation**: Moving to other states

## Learning Objectives

By studying this example, you will learn:
- How to define states using the new `@State` annotation
- How to create transitions using the new `@Transition` annotation  
- How to structure a Brobot 1.1.0 project
- How to handle web automation with image matching
- How to use dependency injection with Brobot components

## Troubleshooting

**Image Matching Issues:**
- Ensure images are high contrast and clearly show the UI elements
- Adjust similarity thresholds in `application.yml` if needed
- Check that the website hasn't changed since capturing images

**Build Issues:**
- Verify Java 21+ is installed
- Ensure proper dependencies in `build.gradle`

**Runtime Issues:**
- Check that the Mr.doob website is accessible
- Verify screen resolution matches captured images
- Review logs for specific error details

## Related Documentation

- [Tutorial Documentation](../../../../docs/docs/03-core-library/tutorials/tutorial-mrdoob/)
- [Brobot State Management](https://brobot.jspinak.io/docs/core-library/state-management)
- [Action System](https://brobot.jspinak.io/docs/core-library/actions)

## Version

This tutorial uses **Brobot 1.1.0** with the new annotation system. For the previous manual registration approach, see documentation versions 1.0.6 and 1.0.7.