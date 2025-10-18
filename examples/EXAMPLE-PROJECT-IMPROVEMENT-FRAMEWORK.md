# Example Project Improvement Framework

**Version:** 1.0
**Created:** 2025-10-16
**Status:** Production-Ready Template
**First Applied To:** quick-start example project

---

## Overview

This document provides a systematic, agent-based approach to verify, fix, and improve Brobot example projects. The framework ensures:
- ✅ 100% compilation success
- ✅ Brobot 1.1.0+ API compliance
- ✅ Complete architecture demonstration (Direct Actions + State-Based)
- ✅ Documentation-code alignment
- ✅ Production-ready code quality

---

## Framework Phases

### **Phase 1: Analysis & Discovery** (Parallel Execution)

Deploy 3 specialized agents simultaneously:

#### **Agent 1: Project Structure Analyzer**
**Type:** `Explore` agent
**Thoroughness:** `medium`

**Tasks:**
1. Inventory all Java source files with line counts
2. Check build configuration (build.gradle, dependencies, Brobot version)
3. Verify application.properties/yml configuration
4. Identify images directory structure
5. Check for README and documentation files
6. Flag any missing or incorrect files

**Deliverable:** Comprehensive structure report with:
- Complete file inventory
- Build configuration analysis
- Brobot version compliance check
- List of potential issues

#### **Agent 2: Documentation-Code Comparator**
**Type:** `Explore` agent
**Thoroughness:** `medium`

**Tasks:**
1. Find corresponding documentation file(s) in `brobot/docs/docs/`
2. Extract all Java code blocks from documentation
3. Compare each code block with actual example code
4. Identify mismatches (different APIs, imports, variable names)
5. Find code in docs that doesn't exist in example
6. Find code in example not documented

**Deliverable:** Comparison report with:
- Code block inventory from docs
- Match percentage (target: 100%)
- Specific differences with line numbers
- Missing documentation or code sections

#### **Agent 3: Compilation Tester**
**Type:** `general-purpose` agent

**Tasks:**
1. Navigate to example project directory
2. Run: `./gradlew clean build --no-daemon`
3. Capture all compilation errors and warnings
4. Categorize errors by type:
   - Deprecated API usage (e.g., ClickOptions.Type)
   - Wrong imports (e.g., wrong package for @State)
   - Missing classes
   - Syntax errors
5. Test dependency resolution
6. If successful, run tests: `./gradlew test --no-daemon`

**Deliverable:** Build report with:
- Success/failure status
- Complete error list with line numbers
- Root cause analysis
- Recommended fixes prioritized

---

### **Phase 2: Critical Fixes** (Sequential - Blocking)

**Prerequisites:** Phase 1 complete

Fix compilation errors FIRST before proceeding:

#### **Step 1: API Compatibility Fixes**

**Common Issues to Fix:**

1. **ClickOptions.Type (Deprecated in 1.1.0)**
   ```java
   // ❌ OLD (broken):
   ClickOptions.Type.LEFT
   ClickOptions.Type.DOUBLE_LEFT
   ClickOptions.Type.RIGHT

   // ✅ NEW (correct):
   new ClickOptions.Builder().build()  // LEFT is default
   new ClickOptions.Builder().setNumberOfClicks(2).build()  // double-click
   new ClickOptions.Builder()
       .setPressOptions(MousePressOptions.builder()
           .setButton(MouseButton.RIGHT)
           .build())
       .build()  // right-click
   ```

2. **Required Import Additions**
   ```java
   import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
   import io.github.jspinak.brobot.model.action.MouseButton;
   ```

3. **Annotation Package Corrections**
   ```java
   // ❌ WRONG:
   import io.github.jspinak.brobot.model.state.State;
   import io.github.jspinak.brobot.model.state.transition.*;
   import io.github.jspinak.brobot.navigation.StateNavigator;

   // ✅ CORRECT:
   import io.github.jspinak.brobot.annotations.State;
   import io.github.jspinak.brobot.annotations.*;
   import io.github.jspinak.brobot.navigation.transition.StateNavigator;
   ```

#### **Step 2: Verify Compilation**
```bash
cd <example-project-path>
./gradlew clean build --no-daemon
```

**Gate:** Must pass before Phase 3. If fails, return to Step 1.

---

### **Phase 3: Architecture Enhancement** (Parallel Execution)

**Prerequisites:** Compilation successful

Deploy 4 agents simultaneously to implement state-based architecture:

#### **Agent 4: State Classes Creator**
**Type:** `general-purpose` agent

**Tasks:**
1. Create primary state class (e.g., `LoginState.java`)
2. Create target state class (e.g., `DashboardState.java`)
3. Use correct `@State` annotation from `io.github.jspinak.brobot.annotations`
4. Define StateImages for each UI element
5. Use `@Getter` for field access

**Template:**
```java
package com.example.<project>;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;

@State
@Getter
public class <StateName>State {
    private final StateImage element1 = new StateImage.Builder()
            .setName("element1")
            .addPatterns("element1")
            .build();

    // Add more StateImages as needed
}
```

#### **Agent 5: Transitions Implementer**
**Type:** `general-purpose` agent

**Tasks:**
1. Create transitions class (e.g., `LoginTransitions.java`)
2. Use `@TransitionSet(state = <State>.class)`
3. Implement `@IncomingTransition` for verification
4. Implement `@OutgoingTransition(activate = {TargetState.class})` for navigation
5. Add comprehensive error handling and logging

**Template:**
```java
package com.example.<project>;

import org.springframework.stereotype.Component;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@TransitionSet(state = <State>.class)
@RequiredArgsConstructor
@Slf4j
public class <State>Transitions {

    private final Action action;
    private final <State> state;

    @IncomingTransition(description = "Verify <state> is visible")
    public boolean verify() {
        log.info("Verifying <state> visibility");
        boolean found = action.find(state.getKeyElement()).isSuccess();
        log.info("Verification: {}", found ? "VISIBLE" : "NOT FOUND");
        return found;
    }

    @OutgoingTransition(activate = {<TargetState>.class})
    public boolean navigateTo<Target>() {
        log.info("Navigating to <target>");

        // Implement navigation logic with error handling
        boolean success = action.click(state.getNavigationElement()).isSuccess();

        if (success) {
            log.info("Navigation successful");
        } else {
            log.error("Navigation failed");
        }

        return success;
    }
}
```

#### **Agent 6: StateNavigator Demo Creator**
**Type:** `general-purpose` agent

**Tasks:**
1. Create automation class (e.g., `<Feature>Automation.java`)
2. Inject `StateNavigator` from correct package
3. Demonstrate automatic pathfinding
4. Add comprehensive documentation comments

**Template:**
```java
package com.example.<project>;

import org.springframework.stereotype.Component;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Demonstrates automatic navigation using StateNavigator.
 *
 * StateNavigator automatically:
 * 1. Determines current state via @IncomingTransition checks
 * 2. Finds path to target state via @OutgoingTransition graph
 * 3. Executes necessary transitions in sequence
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class <Feature>Automation {

    private final StateNavigator navigator;

    /**
     * Navigate to target state automatically.
     */
    public boolean navigateTo<Target>() {
        log.info("Attempting automatic navigation to <Target>");

        // Brobot automatically figures out the path!
        boolean success = navigator.openState("<Target>");

        if (success) {
            log.info("Successfully navigated to <Target>");
        } else {
            log.error("Failed to navigate to <Target>");
        }

        return success;
    }
}
```

#### **Agent 7: Runner Enhancer**
**Type:** `general-purpose` agent

**Tasks:**
1. Locate existing ApplicationRunner or CommandLineRunner
2. Add dependency injection for new automation classes
3. Restructure run() method to show both approaches:
   - **Part A:** Direct Action API examples
   - **Part B:** State-Based Architecture examples
4. Add clear section dividers and summary

**Template Enhancement:**
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class <Project>Runner implements ApplicationRunner {

    private final <DirectAction>Examples directExamples;
    private final <Feature>Automation stateBasedAutomation;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Brobot <Project> Examples ===");
        log.info("This demo showcases two approaches:");
        log.info("  A. Direct Action API");
        log.info("  B. State-Based Architecture\n");

        // Part A: Direct Actions
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("PART A: DIRECT ACTION API EXAMPLES");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Run existing examples...

        // Part B: State-Based
        log.info("\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("PART B: STATE-BASED ARCHITECTURE (RECOMMENDED)");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        boolean success = stateBasedAutomation.navigateToTarget();
        log.info("Result: {}", success ? "✓ SUCCESS" : "✗ FAILED");

        // Summary
        printSummary();
    }

    private void printSummary() {
        log.info("\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("SUMMARY");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("\n✓ Examples Complete!");
        log.info("\nKey Principles:");
        log.info("  • Use type-safe configuration builders");
        log.info("  • Never use Thread.sleep() - use pause options");
        log.info("  • Check ActionResult.isSuccess() for errors");
        log.info("  • Leverage convenience methods");
        log.info("  • Use @State and @Transition for complex apps");
        log.info("\nRecommendation:");
        log.info("  → Direct Actions for quick scripts");
        log.info("  → State-Based for complex applications");
    }
}
```

---

### **Phase 4: Documentation Sync** (Parallel Execution)

**Prerequisites:** Architecture complete

Deploy 2 agents simultaneously:

#### **Agent 8: Documentation API Updater**
**Type:** `general-purpose` agent

**Tasks:**
1. Locate corresponding markdown file(s) in `brobot/docs/docs/`
2. Find all instances of deprecated API usage:
   - `ClickOptions.Type.*` patterns
   - Wrong import statements
   - Outdated method calls
3. Replace with Brobot 1.1.0 API patterns
4. Add API change callout boxes:
   ```markdown
   :::note API Change in Brobot 1.1.0
   The `ClickOptions.Type` enum has been replaced. Use:
   - `MousePressOptions.builder().setButton(MouseButton.RIGHT)` for button selection
   - `setNumberOfClicks(2)` for double-clicks
   - LEFT button is default
   :::
   ```
5. Verify all code blocks compile
6. Add missing import statements to examples

#### **Agent 9: Documentation Coverage Validator**
**Type:** `general-purpose` agent

**Tasks:**
1. Create mapping document showing which files demonstrate which concepts
2. Verify all new classes are mentioned in documentation
3. Add missing examples if needed
4. Ensure README.md is updated with new architecture
5. Document both approaches (Direct vs State-Based)

**Deliverable:**
- Updated documentation with 100% code accuracy
- Clear guidance on when to use each approach
- All examples compile-tested

---

### **Phase 5: Polish & Validation** (Sequential)

#### **Step 1: Project Structure Enhancement**

**Tasks:**
1. Create `images/` directory if missing
2. Add subdirectories as needed (e.g., `images/form/`)
3. Create `images/README.md` explaining:
   - Expected image file structure
   - Mock mode vs real mode operation
   - How to capture screenshots
4. Add `.gitkeep` files to preserve structure

**Template for images/README.md:**
```markdown
# Images Directory

Place your screenshot images here for pattern matching.

## Expected Structure:
\`\`\`
images/
├── element1.png
├── element2.png
└── subdirectory/
    └── element3.png
\`\`\`

## Note on Mock Mode
This example runs in mock mode by default (see application.yml).
In mock mode, Brobot simulates pattern matching without actual image files.

To use real GUI automation:
1. Set \`brobot.core.mock: false\` in application.yml
2. Capture screenshots of your target UI elements
3. Save them with the filenames shown above
4. Run the application
```

#### **Step 2: Code Quality Review**

**Checklist:**
- [ ] All files have proper package declarations
- [ ] All imports are from correct packages
- [ ] No unused imports
- [ ] Consistent code formatting
- [ ] Comprehensive JavaDoc comments on public methods
- [ ] All @Slf4j loggers use appropriate levels (INFO, ERROR, DEBUG)
- [ ] No System.out.println() statements
- [ ] No Thread.sleep() usage
- [ ] All ActionResult checks use .isSuccess()

#### **Step 3: Final Build Verification**

```bash
cd <example-project-path>

# Clean build
./gradlew clean build --no-daemon

# Verify success
echo $?  # Should be 0

# Check for warnings (acceptable: Jackson annotations)
# Check for errors (none acceptable)
```

**Acceptance Criteria:**
- ✅ BUILD SUCCESSFUL
- ✅ Zero compilation errors
- ✅ Only acceptable warnings (Jackson transitive dependencies)
- ✅ All tests pass (if present)

#### **Step 4: Integration Test** (Optional but Recommended)

If time permits, run the actual example:
```bash
./gradlew bootRun
```

Verify:
- Application starts without errors
- Both Part A and Part B examples execute
- Logging output is clear and informative
- Application terminates cleanly

---

## Common API Issues & Fixes Reference

### Issue 1: ClickOptions.Type Enum (DEPRECATED)

**Symptoms:**
```
error: cannot find symbol: variable Type
  location: class ClickOptions
```

**Fix:**
```java
// Add imports
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.MouseButton;

// Replace code:
// OLD: new ClickOptions.Builder().setClickType(ClickOptions.Type.LEFT).build()
new ClickOptions.Builder().build()  // LEFT is default

// OLD: new ClickOptions.Builder().setClickType(ClickOptions.Type.DOUBLE_LEFT).build()
new ClickOptions.Builder().setNumberOfClicks(2).build()

// OLD: new ClickOptions.Builder().setClickType(ClickOptions.Type.RIGHT).build()
new ClickOptions.Builder()
    .setPressOptions(MousePressOptions.builder()
        .setButton(MouseButton.RIGHT)
        .build())
    .build()
```

### Issue 2: Wrong @State Package

**Symptoms:**
```
error: incompatible types: State cannot be converted to Annotation
```

**Fix:**
```java
// OLD (wrong):
import io.github.jspinak.brobot.model.state.State;

// NEW (correct):
import io.github.jspinak.brobot.annotations.State;
```

### Issue 3: Wrong Transition Annotations Package

**Symptoms:**
```
error: package io.github.jspinak.brobot.model.state.transition does not exist
```

**Fix:**
```java
// OLD (wrong):
import io.github.jspinak.brobot.model.state.transition.*;

// NEW (correct):
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
```

### Issue 4: Wrong StateNavigator Package

**Symptoms:**
```
error: package io.github.jspinak.brobot.navigation does not exist
```

**Fix:**
```java
// OLD (wrong):
import io.github.jspinak.brobot.navigation.StateNavigator;

// NEW (correct):
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
```

### Issue 5: StateNavigator.openState() Method

**Note:** StateNavigator does NOT support `openState(Class<?>)` method.

**Supported signatures:**
```java
boolean openState(String stateName)
boolean openState(StateEnum stateEnum)
boolean openState(Long stateId)
```

**Fix if using Class:**
```java
// OLD (doesn't exist):
navigator.openState(DashboardState.class)

// NEW (correct):
navigator.openState("Dashboard")  // Use state name
```

### Issue 6: MousePressOptions Builder Method

**Symptoms:**
```
error: cannot find symbol: method button(MouseButton)
```

**Fix:**
```java
// OLD (wrong):
MousePressOptions.builder().button(MouseButton.RIGHT)

// NEW (correct):
MousePressOptions.builder().setButton(MouseButton.RIGHT)
```

---

## Brobot 1.1.0 API Compliance Checklist

Use this checklist to verify example projects:

### Build Configuration
- [ ] Brobot version is 1.1.0 or higher
- [ ] Spring Boot version is compatible (3.2.0+)
- [ ] Java version is 11+ (21 recommended)
- [ ] Lombok is properly configured
- [ ] No redundant dependencies (Brobot includes Spring, Lombok transitive)

### Annotations
- [ ] @State from `io.github.jspinak.brobot.annotations`
- [ ] @TransitionSet from `io.github.jspinak.brobot.annotations`
- [ ] @IncomingTransition from `io.github.jspinak.brobot.annotations`
- [ ] @OutgoingTransition from `io.github.jspinak.brobot.annotations`

### Action API
- [ ] Uses Action class, not direct SikuliX methods
- [ ] Uses ActionConfig classes (PatternFindOptions, ClickOptions, etc.)
- [ ] NOT using deprecated ActionOptions
- [ ] Checks ActionResult.isSuccess()
- [ ] Uses convenience methods (action.click, action.find, action.type)

### Click Configuration
- [ ] No usage of ClickOptions.Type enum
- [ ] Uses MousePressOptions for non-default buttons
- [ ] Uses setNumberOfClicks() for double-clicks
- [ ] Uses MouseButton enum (LEFT, RIGHT, MIDDLE)

### Pause Handling
- [ ] No Thread.sleep() calls
- [ ] Uses setPauseBeforeBegin/setPauseAfterEnd
- [ ] Pauses configured in ActionConfig options

### State Management (if applicable)
- [ ] State classes use @State annotation
- [ ] StateImages use .Builder() pattern
- [ ] Transition classes use @TransitionSet
- [ ] One @IncomingTransition per state
- [ ] @OutgoingTransition specifies activate states
- [ ] StateNavigator used correctly

### Spring Integration
- [ ] @SpringBootApplication with correct @ComponentScan
- [ ] Includes both application and Brobot packages
- [ ] Uses @Component for state/transition classes
- [ ] Uses @Autowired or @RequiredArgsConstructor for DI

### Logging
- [ ] Uses SLF4J via @Slf4j annotation
- [ ] Configured in application.properties/yml
- [ ] Appropriate log levels (INFO, ERROR, DEBUG)
- [ ] No System.out.println()

### Code Quality
- [ ] Proper error handling with try-catch
- [ ] Meaningful variable names
- [ ] Comprehensive JavaDoc on public methods
- [ ] No unused imports
- [ ] Consistent formatting

---

## Success Metrics

Track these metrics for each example project:

| Metric | Target | Measurement |
|--------|--------|-------------|
| Compilation | ✅ Success | `./gradlew build` exit code 0 |
| API Compliance | 100% | All patterns from checklist |
| Architecture Coverage | 100% | Both Direct + State-Based shown |
| Doc-Code Alignment | 100% | All doc code blocks match project |
| Code Quality | Excellent | All checklist items pass |

---

## Project Improvement Prompt Template

Use this prompt to apply the framework to any example project:

```
Apply the Example Project Improvement Framework to the [PROJECT_NAME] example project
located at brobot/examples/[PATH]/[PROJECT_NAME]/.

Execute all phases using maximum parallelization:
- Phase 1: Deploy 3 agents in parallel for analysis
- Phase 2: Fix compilation errors sequentially
- Phase 3: Deploy 4 agents in parallel for architecture enhancement
- Phase 4: Deploy 2 agents in parallel for documentation sync
- Phase 5: Execute validation steps sequentially

Goals:
1. Achieve 100% compilation success
2. Ensure Brobot 1.1.0+ API compliance
3. Demonstrate both Direct Action and State-Based architectures
4. Align documentation with example code
5. Produce production-ready, reference-quality code

Report progress using the TodoWrite tool and provide a comprehensive summary upon completion.
```

---

## Example Projects Queue

Apply this framework to the following projects in order:

### 01-getting-started/
1. ✅ **quick-start** - COMPLETE (Template project)
2. ⏳ **pure-actions-quickstart** - Ready for framework
3. ⏳ **action-hierarchy** - Ready for framework

### 03-core-library/guides/
4. ⏳ **automatic-action-logging** - Ready for framework
5. ⏳ **finding-objects/using-color** - Ready for framework
6. ⏳ **finding-objects/combining-finds** - Ready for framework
7. ⏳ **finding-objects/movement** - Ready for framework
8. ⏳ **dynamic-transitions/special-states-example** - Ready for framework

### 03-core-library/tutorials/
9. ⏳ **tutorial-basics** - Ready for framework
10. ⏳ **tutorial-claude-automator** - Ready for framework
11. ⏳ **tutorial-mrdoob** - Ready for framework

### 03-core-library/action-config/
12. ⏳ **examples** - Ready for framework
13. ⏳ **conditional-chains-examples** - Ready for framework

---

## Lessons Learned (Updated with each project)

### From quick-start (2025-10-16):

1. **Always verify annotation packages** - @State and transitions are in `annotations`, not `model.state.transition`
2. **StateNavigator location** - Package is `navigation.transition`, not just `navigation`
3. **StateNavigator API limitation** - No `openState(Class<?>)` method exists, only String/StateEnum/Long
4. **MousePressOptions builder** - Uses `setButton()` not `button()`
5. **Agent efficiency** - Parallel execution in Phases 1, 3, 4 saves significant time
6. **Jackson warnings acceptable** - Transitive dependency warnings are expected and safe to ignore
7. **Import verification critical** - Always read files before editing to ensure correct imports
8. **Documentation updates essential** - Code-doc alignment is as important as compilation

### Add new lessons here as more projects are completed...

---

## Framework Maintenance

**Version History:**
- v1.0 (2025-10-16): Initial framework based on quick-start project success

**When to Update:**
- New Brobot version releases with API changes
- Discovery of common issues not covered
- New best practices identified
- Agent performance improvements

**Review Schedule:** After every 5 example projects completed

---

## Contact & Support

**Framework Author:** Claude (Anthropic)
**Framework Location:** `/home/jspinak/brobot_parent/brobot/examples/EXAMPLE-PROJECT-IMPROVEMENT-FRAMEWORK.md`
**Example Projects Location:** `/home/jspinak/brobot_parent/brobot/examples/`
**Brobot Documentation:** `/home/jspinak/brobot_parent/brobot/docs/docs/`

For questions or framework improvements, update this document and increment the version number.
