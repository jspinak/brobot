---
sidebar_position: 4
title: 'States'
---

# States

<!-- Paper Reference: Section 5.3 - State Structure, Section 6.2 - State Management -->

## Introduction

In model-based GUI automation, the environment is represented by a **state structure**, which provides a complete map of the problem space. States are a fundamental component of this structure, representing conceptually cohesive sections of the graphical user interface. This approach moves away from fragile, sequential scripts and toward a more robust, explicit model of the GUI environment.

> **From the Paper**: "The GUI environment, the set of all possible screens during an activity, is represented in the model by the set of GUI elements (E) organized into GUI states (S). The current scene (Ξ), the screen at a specific time, provides the real-time data necessary for the automation system to perceive, interpret, and interact with the GUI."

Since the GUI environment is conceptual and must be abstracted to finite sets, **only its proxies—elements, states, and scenes—are included in the model**. This abstraction enables formal reasoning about GUI automation while maintaining practical applicability.

## Building on the Overall Model

In [overall-model.md](./overall-model.md), we saw that the State Structure **Ω = (E, S, T)** represents the complete environment map, as part of the overall six-tuple (Ξ, Ω, a, M, τ, §). This document focuses specifically on **S** (states) and how they relate to elements (E) and the visible GUI (Ξ).

**Key Concept**: States are **subsets of elements**. Each state s ∈ S is defined by which GUI elements it contains from the global element set E.

**Navigation Analogy**: Think of states as cities on a map, elements as landmarks that identify each city, and the current screen (Ξ) as your current view. You know you're in a particular "city" (state) when you can see its characteristic "landmarks" (elements).

---

## The State Structure (Ω)

The formal model of the GUI environment is defined by the state structure **Ω = (E, S, T)**.

### Components

**E = {e₁, e₂, ..., eₙ}**: The set of all GUI elements selected to model the environment
- Examples: images, regions, locations, text patterns
- Finite set of visual features used to identify states
- These are the "landmarks" that define your GUI map
- In Brobot: `StateImage`, `StateRegion`, `StateLocation`, `StateString`, `StateText`

**S**: The set of all GUI states
- Each state s ∈ S is a **subset of E** (formally: s ⊆ E)
- A state is a collection of related GUI elements
- **Multiple states can be active simultaneously**: S_Ξ ⊆ S
- States form a subset of the power set of E: S ⊆ P(E)
- In Brobot: Defined using `@State` annotation or `State.Builder`

**T**: The set of all transitions between states
- Transitions are sequences of actions that change the set of active states
- Covered in detail in [transitions.md](./transitions.md)
- In Brobot: Defined using `@Transition` annotations

### Visible Elements and Active States

The relationship between what's on screen and which states are active involves two key concepts:

**E_Ξ = f(Ξ) ⊆ E**: The set of visible GUI elements
- **Ξ** is the visible GUI (the current pixel output of the screen)
- **f(Ξ)** is the element extraction function that identifies which elements from E are currently visible
- **E_Ξ** is the resulting set of visible elements

**The Element Extraction Function f(Ξ)**:
In Brobot, this function is implemented through pattern matching and computer vision:
```
f: Screen → P(E)
f(Ξ) = {e ∈ E | pattern_match(e, Ξ) > threshold}
```

Where `pattern_match(e, Ξ)` uses image recognition algorithms (OpenCV, SikuliX) to determine if element e is visible in the current screen Ξ with sufficient confidence (typically 85-95% similarity).

**Implementation**: `BrobotScreenCapture.java` captures Ξ, then `PatternFinder.java` applies f(Ξ) to extract E_Ξ.

**S_Ξ ⊆ S**: The set of currently active states
- Maintained by the State Management System (M)
- Updated dynamically as elements appear and disappear
- Can contain multiple states simultaneously
- Implementation: `StateMemory.java` (line 74: `private Set<Long> activeStates`)

---

## State Activation: The Formal Condition

The most important formal definition for understanding states is the **activation condition**:

### Formal Definition

A state **s** is active (s ∈ S_Ξ) if and only if at least one of its elements is visible in the GUI:

**s ∈ S_Ξ ⟺ s ∩ E_Ξ ≠ ∅**

where:
- **s ∈ S_Ξ** means state s is active
- **s ∩ E_Ξ** is the intersection of the state's elements with visible elements
- **≠ ∅** means "is not empty" (at least one element is visible)

Equivalently, the set of active states can be expressed as:

**S_Ξ = {s ∈ S | s ∩ E_Ξ ≠ ∅}**

This reads: "S_Ξ is the set of all states s in S such that s has at least one visible element."

**Intuitive Interpretation**: If you can see any of a state's defining elements on your screen, that state is currently active. It's like knowing you're in a city because you can see at least one of its famous landmarks.

### Visual Representation

```
Screen (Ξ)                    State Structure (Ω)              Active States (S_Ξ)
┌──────────────────┐
│ [Logo]           │  ───→    HomeState = {logo, menu}       HomeState
│ [Menu Button]    │                                         (logo ∈ E_Ξ)
│                  │
│ [⚠ Error Icon]   │  ───→    ErrorState = {errorIcon}      ErrorState
│ [Close Button]   │                                         (errorIcon ∈ E_Ξ)
└──────────────────┘
                              LoginState = {loginForm}        (inactive)
                                                              (loginForm ∉ E_Ξ)

Result: S_Ξ = {HomeState, ErrorState}

Mathematical verification:
  HomeState ∩ E_Ξ = {logo} ≠ ∅  →  HomeState ∈ S_Ξ ✓
  ErrorState ∩ E_Ξ = {errorIcon} ≠ ∅  →  ErrorState ∈ S_Ξ ✓
  LoginState ∩ E_Ξ = ∅  →  LoginState ∉ S_Ξ ✓
```

### Practical Example: State Activation Flow

**Scenario**: User navigates from Login to Dashboard, then a popup appears.

1. **Initial State**: LoginPage
   - **Screen shows**: Login form with username/password fields
   - E_Ξ = {loginButton, usernameField, passwordField}
   - LoginState ∩ E_Ξ = {loginButton, usernameField, passwordField} ≠ ∅
   - **S_Ξ = {LoginState}** ✓

2. **After Login Transition**: Dashboard appears
   - **Screen shows**: Dashboard with logo, menu, user profile
   - E_Ξ = {dashboardLogo, menuBar, userProfile}
   - DashboardState ∩ E_Ξ = {dashboardLogo, menuBar, userProfile} ≠ ∅
   - LoginState ∩ E_Ξ = ∅ (login elements no longer visible)
   - **S_Ξ = {DashboardState}** ✓

3. **Popup Appears**: Error dialog overlays dashboard
   - **Screen shows**: Error popup over partially visible dashboard
   - E_Ξ = {dashboardLogo, errorIcon, closeButton} (dashboard partially visible)
   - DashboardState ∩ E_Ξ = {dashboardLogo} ≠ ∅ (still active!)
   - ErrorPopupState ∩ E_Ξ = {errorIcon, closeButton} ≠ ∅ (now active!)
   - **S_Ξ = {DashboardState, ErrorPopupState}** ✓ (both active simultaneously)

4. **Close Popup**: Transition deactivates popup
   - **Screen shows**: Full dashboard (popup closed)
   - Transition explicitly marks ErrorPopupState as inactive
   - E_Ξ = {dashboardLogo, menuBar, userProfile}
   - **S_Ξ = {DashboardState}** ✓

**Key Insight**: The activation condition s ∈ S_Ξ ⟺ s ∩ E_Ξ ≠ ∅ automatically handles step 3 where two states are active. The framework doesn't need special logic for overlays—the mathematical model naturally supports compositional GUIs.

---

## State Activation and Deactivation Mechanisms

### Activation: Element Found → State Active

When an observation action (like `action.find()`) successfully locates an element, the element's parent state is marked as active.

**Formal**: ∀ e ∈ E_found : (s_e, True) ∈ S_a

This means: "For every element found, add its state to the state information with value True."

**Example**:
```java
// Action finds login button on screen
ActionResult result = action.find(loginButton);

if (result.isSuccess()) {
    // loginButton ∈ E_Ξ (element is visible)
    // LoginState ∩ E_Ξ = {loginButton} ≠ ∅ (state has visible elements)
    // LoginState ∈ S_Ξ (state becomes active)
}
```

**Implementation Details** (`StateMemory.java`):
```java
// Line 188-203: addActiveState method
public void addActiveState(Long activeState) {
    if (!activeStates.contains(activeState)) {
        activeStates.add(activeState);
        // State becomes part of S_Ξ
    }
}
```

### Deactivation: Implementation Choice in Brobot

**Important**: In Brobot's implementation, states are **not automatically deactivated** when elements are not found. This is a deliberate design choice, not a theoretical requirement.

> **From the Paper**: "A state is considered active if at least one of its elements is found. It is not marked as inactive based on missing elements... Individual images can reliably signal a state's existence but not its absence; therefore, Brobot relies on transitions to deactivate states."

**Rationale**: Individual images can reliably signal a state's existence but not its absence. A failed search might mean:
- The element moved to a different location
- Timing issue (element not loaded yet)
- Search region was too narrow
- Element genuinely disappeared

**Brobot's Solution**: States are deactivated **explicitly through transitions**, which provides more reliable state management:

**Formal**: ∀ e ∈ E_a \ E_found : (s_e, False) ∉ S_a

This means: "For elements not found, do NOT add their states to S_a with value False." Only transitions can deactivate.

**State Management Function** (from Paper Section 6.2):

```
f_M(S_Ξ, S_a, S_t) = (S_Ξ ∪ {s ∈ S | (s, True) ∈ S_a ∪ S_t}) \ {s ∈ S | (s, False) ∈ S_t}
```

where:
1. States are added to S_Ξ when marked active in either S_a or S_t
2. States are only removed from S_Ξ when explicitly marked inactive in S_t (transition-based updates)
3. No state is removed based solely on action results (S_a never contains pairs with False)

**Example**:
```java
// Transition from Login to Dashboard
@Transition(fromState = LoginState.class, toState = DashboardState.class)
public boolean login() {
    action.type(usernameField, "user");
    action.type(passwordField, "pass");
    boolean success = action.click(loginButton).isSuccess();

    // If successful, transition explicitly:
    // - Deactivates LoginState: (LoginState, False) ∈ S_t
    // - Activates DashboardState: (DashboardState, True) ∈ S_t

    return success;
}
```

**Theoretical Generalization**: The theoretical model doesn't mandate this approach. Alternative implementations could use probabilistic deactivation, timeout-based deactivation, or explicit confirmation searches. Brobot chooses transition-based deactivation for reliability.

---

## Formal State Properties

Given the state structure Ω = (E, S, T), the following formal properties hold:

1. **State-Element Relationship**: Each state is a subset of elements
   - **∀s ∈ S: s ⊆ E**
   - Consequence: A state cannot contain elements not in E
   - Example: If E = {logo, button, field}, then s = {logo, button} is valid, but s = {logo, unknownElement} is invalid

2. **Power Set Constraint**: States form a subset of the power set of E
   - **S ⊆ P(E)** where P(E) is the power set of E
   - This means: not every possible subset of E needs to be a state
   - Example: With E = {e₁, e₂, e₃}, we have 8 possible subsets, but might only define 3 as states

3. **Simultaneous Active States**: Multiple states may be active at once
   - **S_Ξ ⊆ S** where |S_Ξ| ≥ 0
   - This distinguishes model-based automation from traditional finite state machines
   - Enables compositional GUI representation (base + overlays)

4. **Activation Condition**: State activation depends on element visibility
   - **s ∈ S_Ξ ⟺ s ∩ E_Ξ ≠ ∅**
   - This is the fundamental theorem of state activation
   - Bidirectional implication: necessary AND sufficient condition

5. **Finiteness**: Both element and state sets are finite
   - **|E| < ∞** and **|S| < ∞**
   - Practical necessity: infinite sets cannot be implemented
   - Enables algorithmic path finding and state management

6. **Active States Subset**: Active states are always a subset of all states
   - **S_Ξ ⊆ S** at all times
   - Invariant: S_Ξ can never contain states not in S
   - Maintained by State Management System (M)

7. **Empty Intersection Property**: Inactive states have no visible elements
   - **s ∉ S_Ξ ⟹ s ∩ E_Ξ = ∅**
   - Contrapositive of the activation condition
   - If a state is not active, none of its elements are visible

8. **Non-Empty States**: States must contain at least one element
   - **∀s ∈ S: s ≠ ∅**
   - Practical constraint: empty states cannot be activated
   - In Brobot: State.Builder requires at least one element

---

## States in Brobot

### State Definition

> **From the Paper**: "A state in model-based GUI automation is a collection of related GUI elements. State objects often are grouped spatially or appear at the same time. Objects used together in a process are likely candidates for belonging to the same state. However, these configurations are not absolute rules, and the definition of a state is subjective. A state has a meaning within the automated environment that can vary depending on the automation goals, and a state configuration should make sense in the context of the automation application."

In Brobot, a state is a collection of related GUI elements. These elements are often grouped spatially, appear together on screen, or are used together in a process. However, these groupings are not absolute rules—**the definition of a state is subjective** and should make sense for your specific automation task.

A state has meaning within the automated environment that can vary depending on automation goals. State configurations should be context-driven and aligned with your application's purpose.

### Code Example: Defining a State

**Updated Example Using Current Brobot API:**

```java
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.action.ActionRecord;

// Define the element that identifies this state
private StateImage toWorldButton = new StateImage.Builder()
    .addPatterns("toWorldButton")              // Pattern file to match
    .setFixedForAllPatterns(true)              // Element doesn't move on screen
    .withActionHistory(new ActionRecord(220, 600, 20, 20))  // Expected location
    .build();

// Define the state as a collection of elements
// In this case, Home state is defined by a single button
private State homeState = new State.Builder(Name.HOME)
    .withImages(toWorldButton)                 // Element(s) that define this state
    .build();
```

**Where `Name` is a user-defined enum:**
```java
public enum Name implements StateEnum {
    HOME, WORLD, ISLAND, SETTINGS
}
```

**Alternative: Multiple Elements Per State**:
```java
private StateImage logo = new StateImage.Builder()
    .addPatterns("login-logo")
    .build();

private StateImage usernameField = new StateImage.Builder()
    .addPatterns("username-field")
    .build();

private StateImage loginButton = new StateImage.Builder()
    .addPatterns("login-button")
    .build();

// State defined by multiple elements
private State loginState = new State.Builder(Name.LOGIN)
    .withImages(logo, usernameField, loginButton)  // s = {logo, usernameField, loginButton} ⊆ E
    .build();
```

### Formal Interpretation of the Code

The Java code above defines a state in Brobot. In terms of our formal model:

- **E_home = {toWorldButton}**: The element set for the Home state
- **s_home = E_home ⊆ E**: The HOME state is this subset of the global element set E
- **Activation**: s_home ∈ S_Ξ ⟺ toWorldButton ∈ E_Ξ

When Brobot's visual search successfully finds the `toWorldButton` pattern on screen (through pattern matching at coordinates (220, 600) with size 20×20), the element becomes part of E_Ξ, and thus HOME becomes part of S_Ξ through the activation condition.

**Data Flow**:
```
1. Screen capture → Ξ (pixel output)
2. Pattern matching → f(Ξ) extracts visible elements → E_Ξ
3. Element found → toWorldButton ∈ E_Ξ
4. Activation condition → s_home ∩ E_Ξ = {toWorldButton} ≠ ∅
5. State becomes active → s_home ∈ S_Ξ
```

---

## Practical Example from the Paper: The DoT App

> **From the Paper Section 5.3.1**: "To make the abstract components of the Overall Model (Ξ, Ω, a, M, τ, §) more concrete, I use the DoT app, an experimental application designed to automate tasks in the mobile game *Dawn of Titans*. This app will serve as a running example to illustrate how the theoretical models are realized in practice."

The DoT (Dawn of Titans) application demonstrates state definition in a real-world automation context:

**States Defined**:
- **Home State**: Identified by the `toWorldButton` element
- **World State**: Identified by elements in the world map view
- **Island State**: Identified by island-specific UI elements

**State Structure Example**:
```
Ω_DoT = (E_DoT, S_DoT, T_DoT) where:

E_DoT = {toWorldButton, worldMapRegion, islandNameRegion, ...}

S_DoT = {
  Home = {toWorldButton},
  World = {worldMapRegion, ...},
  Island = {islandNameRegion, ...}
}

T_DoT = {HomeToWorld, WorldToIsland, ...}
```

**Key Insight**: The DoT app uses a relatively simple state structure (3 main states) for its automation task. The paper notes that a more granular approach (e.g., adding a `NewIsland` state) might have improved the automation for specific use cases. This demonstrates that **state granularity should match automation needs**, not GUI complexity.

---

## Connection to State Management (M)

The **State Management System (M)** is responsible for maintaining S_Ξ, the set of active states. It continuously:

1. **Processes action results** to identify newly visible elements (E_Ξ)
2. **Applies the activation condition** s ∈ S_Ξ ⟺ s ∩ E_Ξ ≠ ∅ to determine which states should be active
3. **Processes explicit state changes** from transitions (S_t)
4. **Updates S_Ξ** using the state management function f_M

**Formal Definition** (from overall-model.md):

```
M = (S_Ξ) where S_Ξ ⊆ S is the current set of active states

f_M: (S_Ξ, S_a, S_t) → S'_Ξ

where:
- S_a: state information from actions (which states have visible elements)
- S_t: state information from transitions (which states to activate/deactivate)
- S'_Ξ: the updated set of active states
```

**Implementation Architecture**:
```
Action Execution → ActionResult
       ↓
State Information Extraction → S_a
       ↓
State Management (M) ← S_t (from transitions)
       ↓
Updated Active States → S'_Ξ
```

**Source Files**:
- `StateMemory.java` (lines 74-96): Maintains S_Ξ as `Set<Long> activeStates`
- `StateMemoryUpdater.java`: Implements f_M update logic
- `StateDetector.java`: Extracts S_a from action results

For complete details on the State Management System, see [overall-model.md](./overall-model.md#m---the-state-management-system).

---

## Best Practices for State Design

### Cohesiveness
Group elements into a state that are logically related, appear together on screen, or are used together in a process.

**Example**:
```java
// Good: LoginState contains all login-related elements
LoginState = {usernameField, passwordField, loginButton, forgotPasswordLink}

// Poor: Mixing unrelated elements from different screens
MixedState = {usernameField, dashboardLogo, settingsButton}
```

**Cohesion Test**: Ask "If I see element X, should I expect to see element Y?" If yes, they likely belong in the same state.

### Context-Driven
The definition of a state is subjective and should be tailored to your automation goals. The same GUI might be modeled differently depending on what you're trying to automate.

**Example**: For a spreadsheet application:
- **Document editing automation**: Might define states by worksheet tabs
  - States: Sheet1State, Sheet2State, Sheet3State
- **UI testing automation**: Might define states by dialog windows and ribbons
  - States: MainEditorState, FormatDialogState, ChartDialogState
- **Data extraction automation**: Might define states by visible cell ranges
  - States: HeaderVisibleState, DataRangeVisibleState

**Key Principle**: States should align with your automation's decision points, not necessarily the application's internal architecture.

### Modularity
The state-based approach allows states and transitions to be built, tested, and debugged independently. This modular design helps manage complexity and localize troubleshooting efforts.

**Benefit**: If LoginState has issues, you can:
- Test it in isolation with mock elements
- Modify its elements without affecting other states
- Update its transitions independently
- Verify activation condition in unit tests

**Testing Example**:
```java
@Test
public void testLoginStateActivation() {
    // Mock E_Ξ with login elements visible
    Set<Element> visibleElements = Set.of(loginButton, usernameField);

    // Verify activation condition
    assertTrue(loginState.hasVisibleElements(visibleElements));
    // Equivalent to: loginState ∩ E_Ξ ≠ ∅
}
```

### Granularity Balance
Finding the right number of states is key—neither too fine-grained nor too coarse.

**Too Fine-Grained** ❌:
```java
ButtonVisibleState, ButtonHoverState, ButtonClickedState, ButtonDisabledState
// Problem: Explosion of states, overly complex navigation
// Result: Hundreds of states for simple GUI
```

**Too Coarse-Grained** ❌:
```java
EntireApplicationState
// Problem: Can't detect intermediate steps, poor error recovery
// Result: Can't navigate or handle errors effectively
```

**Balanced** ✅:
```java
LoginState, DashboardState, SettingsState, ReportState
// Right level: Represents meaningful application sections
// Result: Manageable state count, effective navigation
```

**Granularity Guidelines**:
- **Start coarse**: Begin with main application sections
- **Refine as needed**: Add states when you need to distinguish scenarios
- **Test navigation**: If path finding struggles, you may need more states
- **Monitor complexity**: If you have >50 states, consider whether some can be merged

**Paper Example**: The DoT app uses a simple design with HOME, WORLD, and ISLAND states. The paper notes that a more granular `NewIsland` state might have been better for the specific automation task—demonstrating that granularity should match your automation needs.

---

## Multiple Active States: A Key Architectural Choice

One of the most important distinctions of model-based GUI automation is that **multiple states can be active simultaneously**. This sets it apart from traditional finite state machines where only one state can be active at a time.

### Why Multiple Active States?

Real GUIs are compositional:
- Base application window (DashboardState)
- Overlay dialogs (ErrorPopupState)
- Floating toolbars (ToolboxState)
- Background processes (LoadingState)

**All can be visible and active at the same time.**

**Real-World Analogy**: Think of your computer desktop. You might have:
- Main application window (like a city you're in)
- Notification popups (like street signs you see)
- System tray icons (like landmarks always visible)
- Background processes (like weather you experience)

You're experiencing all of these simultaneously—model-based GUI automation reflects this reality.

### Formal Definition

**S_Ξ ⊆ S** where |S_Ξ| ≥ 0

This allows S_Ξ to contain:
- Zero states: S_Ξ = ∅ (no states active—rare, usually startup)
- One state: S_Ξ = {DashboardState}
- Multiple states: S_Ξ = {DashboardState, ErrorPopupState, LoadingState}

**Cardinality Bounds**:
- Minimum: |S_Ξ| = 0 (empty set)
- Maximum: |S_Ξ| = |S| (all states active—theoretically possible but rare)
- Typical: 1 ≤ |S_Ξ| ≤ 5 (one base state plus a few overlays)

### Practical Impact

When finding transitions during path traversal, the framework looks for transitions available from **any** of the currently active states:

```java
// With S_Ξ = {DashboardState, ErrorPopupState}

// Framework can execute:
// - Transitions from DashboardState (navigate to Settings)
// - Transitions from ErrorPopupState (close error dialog)
// - Transitions from BOTH states (e.g., logout available from anywhere)
```

**Mathematical Formulation**:
```
Available transitions: T_available = ⋃(s ∈ S_Ξ) T_s

where T_s = {t ∈ T | (s, t) ∈ δ}
```

This compositional approach enables robust navigation in complex GUIs because:
1. **Error Recovery**: Unexpected popups don't break navigation—they just become additional active states
2. **Flexible Paths**: More starting points (S_Ξ) means more possible paths
3. **Natural Modeling**: Matches how real GUIs actually work

### Comparison with Traditional FSM

**Traditional Finite State Machine**:
```
Current State: DashboardState (single state)
Popup Appears → Must transition to PopupState
Dashboard No Longer Active → Lost context
```

**Model-Based GUI Automation**:
```
Current States: {DashboardState} (can be multiple)
Popup Appears → Add PopupState to active set
Current States: {DashboardState, PopupState} (both active)
Dashboard Context Preserved → Can resume after closing popup
```

---

## Common Mistakes When Defining States

### ❌ Mistake 1: Defining States by Actions Instead of Elements

**Wrong**:
```java
ClickingButtonState, TypingTextState, WaitingState
```

**Right**:
```java
LoginPageState (identified by login elements visible)
DashboardState (identified by dashboard elements visible)
```

States represent **what's visible**, not what you're doing.

**Why this matters**: Actions are temporary operations; states are persistent GUI configurations. If you define states by actions, you'll create states that can never be reliably detected because "clicking" isn't a visual property.

**Correct Thinking**: "What elements are on screen?" not "What am I doing?"

### ❌ Mistake 2: One-to-One Mapping with Screens

Don't assume each state = each screen. Some screens may need multiple states, and some states may span multiple screens.

**Example**: A settings dialog with tabs might be:
- **One state**: `SettingsState` (simple approach)
  - Works if: All tabs share common elements and you don't need to distinguish them
- **Multiple states**: `GeneralSettingsState`, `PrivacySettingsState`, `AdvancedSettingsState`
  - Works if: Each tab has distinct elements and your automation needs to know which tab is active

**Decision Criterion**: Do your automation tasks require distinguishing these configurations? If yes, create separate states. If no, use one state.

**Example Decision Tree**:
```
Does your automation need to:
- Navigate specifically to the Privacy tab? → Multiple states
- Just open Settings (any tab is fine)? → Single state
- Perform different actions per tab? → Multiple states
- Perform same actions regardless of tab? → Single state
```

### ❌ Mistake 3: Ignoring Overlays and Popups

**Wrong**: Only defining main application states

**Right**: Include states for:
- Error dialogs (ErrorDialogState)
- Loading screens (LoadingState)
- Tooltips (if relevant to automation) (TooltipState)
- Notification banners (NotificationState)
- Popup menus (ContextMenuState)
- Modal dialogs (ConfirmDialogState)

**Why this matters**: Popups can appear unexpectedly and block automation. If they're not modeled as states, the framework can't:
- Detect their presence
- Find transitions to close them
- Resume automation after handling them

Remember: **Multiple states can be active**, so popups are additional active states, not replacements. When a popup appears over the dashboard, both DashboardState and PopupState are active.

**Real-World Impact**: A common automation failure occurs when an unexpected error dialog appears. With proper state modeling, the framework detects the dialog state, finds the "close dialog" transition, executes it, and resumes the intended path. Without it, the automation fails.

---

## Related Documentation

### Theoretical Foundations
- **[Introduction](./introduction.md)** - Conceptual overview of model-based approach with practical examples
- **[Academic Foundation](./academic-foundation.md)** - Research background, empirical evidence, and citations
- **[Overall Model](./overall-model.md)** - Complete formal model (Ξ, Ω, a, M, τ, §) with State Structure definition
- **[Transitions](./transitions.md)** - How transitions connect states and manage state changes (T component)
- **[Testing the Automation](./testing-automation.md)** - Testing states independently using mock mode

### Practical Implementation
- **[Getting Started](../01-getting-started/)** - Hands-on tutorials for creating states in Brobot applications
- **[AI Brobot Project Creation](../../docs/ai-brobot-project-creation.md)** - Complete API reference for State and StateImage classes

### Source Code References
- **State.java** (`library/src/main/java/io/github/jspinak/brobot/model/state/State.java`) - State model implementation
- **StateMemory.java** (`library/src/main/java/io/github/jspinak/brobot/statemanagement/StateMemory.java`) - S_Ξ maintenance
- **StateImage.java** (`library/src/main/java/io/github/jspinak/brobot/model/state/StateImage.java`) - Element definition

---

## Mathematical Notation Summary

For quick reference, key symbols used in this document:

| Symbol | Definition | Description |
|--------|------------|-------------|
| **Ω** | State Structure | Tuple (E, S, T) |
| **E** | Element Set | {e₁, e₂, ..., eₙ} all GUI elements |
| **S** | State Set | All possible GUI states |
| **T** | Transition Set | All transitions between states |
| **Ξ** | Visible GUI | Current screen pixel output |
| **f(Ξ)** | Element Extraction | Function extracting visible elements from screen |
| **E_Ξ** | Visible Elements | f(Ξ) ⊆ E, elements currently on screen |
| **S_Ξ** | Active States | {s ∈ S \| s ∩ E_Ξ ≠ ∅}, currently active states |
| **s ⊆ E** | State Definition | Each state is a subset of elements |
| **s ∩ E_Ξ ≠ ∅** | Activation Condition | State is active iff it has visible elements |
| **M** | State Management | System that maintains S_Ξ |
| **P(E)** | Power Set | Set of all subsets of E |
| **S_a** | Action State Info | State information derived from actions |
| **S_t** | Transition State Info | State information from transitions |
| **δ** | Transition Relation | δ ⊆ S × T, which transitions are accessible from which states |

### Key Theorems and Properties

1. **Activation Theorem**: s ∈ S_Ξ ⟺ s ∩ E_Ξ ≠ ∅
2. **Active States Set**: S_Ξ = {s ∈ S | s ∩ E_Ξ ≠ ∅}
3. **State-Element Relationship**: ∀s ∈ S: s ⊆ E
4. **Power Set Constraint**: S ⊆ P(E)
5. **State Management**: f_M: (S_Ξ, S_a, S_t) → S'_Ξ
6. **Finiteness**: |E| < ∞ ∧ |S| < ∞

These symbols provide precise mathematical definitions. For conceptual understanding, focus on the core idea: **states are collections of elements, and a state is active when at least one of its elements is visible on screen**.

---

## Further Reading

For those interested in the theoretical foundations and empirical evidence:

- **Paper Section 5.3**: Formal definition of State Structure and components
- **Paper Section 6.2**: Brobot's implementation of state management
- **Paper Section 5.3.1**: Practical example using the DoT application
- **Figure 6** (Paper): Visual example of three simultaneous states in a GUI

For academic citations and empirical evidence of the problems model-based automation solves, see [academic-foundation.md](./academic-foundation.md).
