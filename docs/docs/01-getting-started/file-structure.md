---
sidebar_position: 5
title: 'Project File Structure'
---

# Project File Structure

## Overview

A typical Brobot project organizes files by state, with each state having its own folder containing the State class and TransitionSet class.

## Standard Project Layout

```
📁 my-automation-project/
├── 📁 images/
│   ├── 🖼️ castle.png
│   ├── 🖼️ farms.png
│   ├── 🖼️ forest.png
│   ├── 🖼️ lakes.png
│   ├── 🖼️ mines.png
│   ├── 🖼️ mountains.png
│   ├── 🖼️ searchButton.png
│   └── 🖼️ toWorldButton.png
│
├── 📁 src/
│   └── 📁 main/
│       ├── 📁 java/
│       │   └── 📁 com.example.demo/
│       │       ├── 📁 home/
│       │       │   ├── ☕ HomeState.java
│       │       │   └── ☕ HomeTransitions.java
│       │       │
│       │       ├── 📁 island/
│       │       │   ├── ☕ IslandState.java
│       │       │   ├── ☕ IslandTransitions.java
│       │       │   ├── ☕ IslandActivities.java
│       │       │   ├── ☕ IslandStatistics.java
│       │       │   └── ☕ GetIslandType.java
│       │       │
│       │       ├── 📁 world/
│       │       │   ├── ☕ WorldState.java
│       │       │   └── ☕ WorldTransitions.java
│       │       │
│       │       ├── ☕ Application.java
│       │       ├── ☕ GetLabeledDataApp.java
│       │       └── ☕ SaveLabeledImages.java
│       │
│       └── 📁 resources/
│           └── 📄 application.properties
│
├── 📄 build.gradle
└── 📄 pom.xml
```

## Organized by State Folders

The recommended approach is to organize code by state, with each state in its own package:

```
📁 src/main/java/com/example/automation/
├── 📁 states/
│   ├── 📁 menu/
│   │   ├── ☕ MenuState.java
│   │   └── ☕ MenuTransitions.java
│   │
│   ├── 📁 pricing/
│   │   ├── ☕ PricingState.java
│   │   └── ☕ PricingTransitions.java
│   │
│   └── 📁 homepage/
│       ├── ☕ HomepageState.java
│       └── ☕ HomepageTransitions.java
│
├── 📁 runner/
│   └── ☕ AutomationRunner.java
│
└── ☕ Application.java
```

## With Separate States and Transitions Folders

Alternatively, you can separate states and transitions into different folders:

```
📁 src/main/java/com/example/automation/
├── 📁 states/
│   ├── ☕ MenuState.java
│   ├── ☕ PricingState.java
│   └── ☕ HomepageState.java
│
├── 📁 transitions/
│   ├── ☕ MenuTransitions.java
│   ├── ☕ PricingTransitions.java
│   └── ☕ HomepageTransitions.java
│
├── 📁 runner/
│   └── ☕ AutomationRunner.java
│
└── ☕ Application.java
```

## Images Folder Organization

### All Images in Root

For simple projects, keep all images in the root `images/` folder:

```
📁 images/
├── 🖼️ menu-logo.png
├── 🖼️ menu-pricing.png
├── 🖼️ pricing-start-free.png
└── 🖼️ homepage-email.png
```

### Organized by State

For larger projects, organize images by state:

```
📁 images/
├── 📁 menu/
│   ├── 🖼️ logo.png
│   ├── 🖼️ pricing-button.png
│   └── 🖼️ home-button.png
│
├── 📁 pricing/
│   ├── 🖼️ header.png
│   └── 🖼️ start-free-button.png
│
└── 📁 homepage/
    ├── 🖼️ email-field.png
    └── 🖼️ submit-button.png
```

## Complete Example Project

```
📁 brobot-automation-project/
│
├── 📁 images/
│   ├── 📁 menu/
│   │   ├── 🖼️ menu-logo.png
│   │   ├── 🖼️ menu-pricing.png
│   │   └── 🖼️ menu-home.png
│   │
│   ├── 📁 pricing/
│   │   ├── 🖼️ pricing-header.png
│   │   └── 🖼️ start-for-free.png
│   │
│   └── 📁 homepage/
│       ├── 🖼️ hero-image.png
│       ├── 🖼️ email-field.png
│       └── 🖼️ submit-button.png
│
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/
│   │   │   └── 📁 com/example/automation/
│   │   │       │
│   │   │       ├── 📁 states/
│   │   │       │   ├── ☕ MenuState.java
│   │   │       │   ├── ☕ PricingState.java
│   │   │       │   └── ☕ HomepageState.java
│   │   │       │
│   │   │       ├── 📁 transitions/
│   │   │       │   ├── ☕ MenuTransitions.java
│   │   │       │   ├── ☕ PricingTransitions.java
│   │   │       │   └── ☕ HomepageTransitions.java
│   │   │       │
│   │   │       ├── 📁 runner/
│   │   │       │   └── ☕ AutomationRunner.java
│   │   │       │
│   │   │       └── ☕ Application.java
│   │   │
│   │   └── 📁 resources/
│   │       ├── 📄 application.properties
│   │       └── 📄 application-mock.properties
│   │
│   └── 📁 test/
│       └── 📁 java/
│           └── 📁 com/example/automation/
│               └── ☕ MockAutomationTest.java
│
├── 📄 build.gradle
├── 📄 pom.xml
├── 📄 README.md
└── 📄 .gitignore
```

## Key File Structure Rules

1. **Images Folder**: Place all images in `images/` at the project root
2. **State Classes**: One per screen/page, contains only UI elements (StateImage, StateString)
3. **TransitionSet Classes**: One per state, contains navigation methods
4. **Application.java**: Spring Boot main class with `@ComponentScan` for Brobot
5. **Runner Classes**: CommandLineRunner implementations to execute automation
6. **One TransitionSet per State**: Each state has exactly ONE TransitionSet class

## Icon Legend

- 📁 = Folder/Directory
- 🖼️ = Image file (.png, .jpg)
- ☕ = Java source file (.java)
- 📄 = Configuration file (.properties, .xml, .gradle, .md)

## Best Practices

- **Organize by domain**: Group related states together
- **Clear naming**: Use descriptive names for states and images
- **Consistent structure**: Follow the same pattern across all states
- **Separate concerns**: Keep states and transitions in separate files
- **Image organization**: Use subfolders for images when you have many states
