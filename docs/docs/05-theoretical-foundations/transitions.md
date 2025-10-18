---
sidebar_position: 5
title: 'Transitions'
---

# Transitions

<!-- Paper Reference: Section 5.6 - The Transition Model, Section 6.3 - The Transition Implementation -->

## Building on the Overall Model

This document focuses on **Transitions (T)**, the third component of the State Structure Ω = (E, S, T). Before diving into transitions, ensure you understand:

- **[Overall Model](./overall-model.md)** - The complete formal model (Ξ, Ω, a, M, τ, §)
- **[States](./states.md)** - The State Structure (Ω) and state management (M)

Transitions complete the State Structure by defining **how to move between states**. While states represent what exists (static), transitions represent what happens (dynamic).

## What is a Transition?

### The Dynamic Counterpart to States

If states are the "nouns" of GUI automation (what exists), transitions are the "verbs" (what happens).

**The Key Distinction**:
- **States (S)**: Static configurations of the GUI - what screens/configurations exist
- **Transitions (T)**: Dynamic sequences of actions - how to change from one configuration to another

**Analogy**: Think of navigating a city:
- **States** are destinations (cities on a map)
- **Elements** are landmarks that identify each city
- **Transitions** are the roads and driving instructions to get from one city to another

Just as a road has both a physical pathway and turn-by-turn directions, a transition has both the actions to execute and knowledge about what should change when you arrive.

### Why Transitions Matter

In process-based GUI automation, you write sequential scripts:
```
1. Click button X
2. Wait 2 seconds
3. Type "username"
4. Click submit
5. ...
```

This approach has a critical flaw: **it doesn't know where it is**. If step 3 fails, the script is lost.

In model-based automation, transitions are part of a graph structure:
```
LoginState --[login transition]--> DashboardState
```

The framework knows:
- Where it is (LoginState is active)
- Where it wants to go (DashboardState)
- What paths exist (transitions in the graph)
- How to recover (if one path fails, find another)

> **From the Paper**: "The Transition Model defines how sequences of actions lead to transitions between different GUI states."

## Formal Definition

<!-- Paper Reference: Section 5.6, lines 337-349 -->

### The Transition Tuple

A transition is formally defined as a tuple:

**t = (A, S_t^def)**

where:
- **A** = (a¹, a², ..., aⁿ) is a process or sequence of actions executed as part of the transition
- **S_t^def** ∈ P(S × {True, False}) is the intended state information if the transition succeeds

**What This Means**:

1. **A (Action Sequence)**: The concrete steps that manipulate the GUI
   - Examples: click login button, type username, press Enter
   - These are the "how" - the physical actions performed

2. **S_t^def (Defined State Information)**: Explicit knowledge about state changes
   - Examples: "activate Dashboard", "deactivate LoginPage"
   - These are the "what" - the expected state transitions
   - Type: P(S × {True, False}) means "set of (state, boolean) pairs"
     - (s, True) means "activate state s"
     - (s, False) means "deactivate state s"

**Example Transition**:
```
Login Transition:
A = [click(loginButton), type(username, "user"), type(password, "pass"), click(submit)]
S_t^def = {(LoginPage, False), (Dashboard, True)}

Translation: "Execute these actions, then deactivate LoginPage and activate Dashboard"
```

### The Transition Function

The transition function processes the transition tuple:

**f_τ: (A, S_t^def) → (Ξ', r_t)**

where:
- **Ξ'** is the resulting GUI state after executing the action process
- **r_t** is the transition result, containing:
  - **success or failure status**: Boolean indicating if transition succeeded
  - **state information S_t**:
    - S_t = S_t^def if transition succeeds
    - S_t = ∅ (empty set) if transition fails

**Why This Matters**: The transition result provides **explicit feedback** about what states should be active. The State Management system (M) uses this information to update the active states (S_Ξ).

### The Process Function

Before the transition function applies state changes, the process function executes the action sequence:

**f_A: (A) → (Ξ')**

This function builds on the atomic action function f_a by **iteratively applying** each action in the sequence:
- Action a¹ operates on initial screen Ξ → produces Ξ₁
- Action a² operates on Ξ₁ → produces Ξ₂
- ...
- Action aⁿ operates on Ξₙ₋₁ → produces final Ξ' (resulting GUI)

**The final output Ξ' represents the cumulative effect of all actions.**

### The Transition Relation

The transition relation defines which transitions are accessible from which states:

**δ ⊆ S × T**

where:
- **S** is the set of all possible states
- **T** is the set of all transitions
- **(s_i, t_j) ∈ δ** means "transition t_j is accessible from state s_i"

**Important**: δ is a **relation** (⊆), not a function. This means:
- Multiple transitions can be accessible from the same state
- A transition can be accessible from multiple states

**Example**:
```
States: S = {Login, Dashboard, Settings}
Transitions: T = {t_login, t_logout, t_toSettings, t_fromSettings}

Transition Relation δ:
(Login, t_login) ∈ δ          ← Can login from Login state
(Dashboard, t_logout) ∈ δ     ← Can logout from Dashboard
(Dashboard, t_toSettings) ∈ δ ← Can go to Settings from Dashboard
(Settings, t_fromSettings) ∈ δ ← Can leave Settings
```

> **From the Paper**: "A transition is a tuple t = (A, S_t^def) comprising: A, a process or sequence of actions A = (a¹, a², ..., aⁿ) executed as part of the transition; S_t^def ∈ P(S × {True, False}), the intended state information if the transition succeeds."

## The Critical Role of S_t^def: Optimization Through Knowledge

<!-- Paper Reference: Section 5.6, lines 355-357 -->

One of the most important features of the Transition Model is **S_t^def** (defined state information). This component enables significant optimization.

### Why S_t^def Exists

> **From the Paper**: "The state information S_t provides explicit state change details that complement the information inferred from actions. Although not strictly necessary for state management, transitions can significantly streamline the process through S_t by containing known relationships between states. For example, if state A always disappears when state B is activated, S_t can include this rule directly in the transition, eliminating the need for additional actions to verify the absence of state A."

### Without S_t^def (Process-Based Approach)

In traditional automation:
```java
// Navigate to Settings
action.click(settingsButton);
Thread.sleep(2000); // Wait for transition

// Must verify Dashboard closed
if (findImage("dashboard.png") != null) {
    throw new Exception("Dashboard still visible!");
}

// Must verify Settings opened
if (findImage("settings.png") == null) {
    throw new Exception("Settings not visible!");
}
```

**Problems**:
- Extra verification actions required
- Hard-coded delays
- Manual error checking
- No explicit knowledge about state relationships

### With S_t^def (Model-Based Approach)

```java
// Transition encodes state knowledge
Transition toSettings = new Transition(
    actions: [click(settingsButton)],
    S_t^def: {(Dashboard, False), (Settings, True)}
);

// Framework automatically:
// 1. Executes actions
// 2. Applies state changes from S_t^def
// 3. Verifies expected states are active
// 4. Returns success/failure
```

**Benefits**:
- **No extra verification steps** - S_t^def encodes expected changes
- **Automatic state management** - Framework updates S_Ξ using S_t
- **Explicit knowledge** - State relationships are declared, not implicit
- **Error recovery** - Framework knows what states should be active

### Practical Example: The DoT Application

In the DoT (Dawn of Titans) application from the paper, transitions encode state relationships:

**World → Island Transition**:
```
t_goToIsland = (
    A: [click(searchButton)],
    S_t^def: {(World, True), (Island, True)}
)
```

Note: Both World and Island remain active (World, True) because the World map stays visible when viewing an Island. This knowledge is **explicitly encoded** in S_t^def, so the framework knows not to deactivate World.

**Without S_t^def**: The automation would need extra actions to verify World remains visible while Island becomes active. This would add complexity and potential for error.

**With S_t^def**: The transition explicitly states "keep World active, also activate Island." The framework handles this automatically.

## Transitions in Brobot

<!-- Paper Reference: Section 6.3 - The Transition Implementation -->

### Implementation Architecture: FromTransitions and ToTransitions

Brobot implements the transition model by separating transitions into two complementary parts:

- **FromTransition**: State-specific initial actions required to leave the current state
- **ToTransition**: Universal finalization actions that always occur when entering the target state

> **From the Paper** (Section 6.3): "In Brobot, transitions are separated into two parts: FromTransitions and ToTransitions. A FromTransition handles the initial actions for moving from the current state to another state, and a ToTransition completes the transition."

**Visual Representation**:
```
State 1 (active)  →  [FromTransition 1→6]  →  [ToTransition →6]  →  State 6 (active)
                      ↓                         ↓
                      State-specific actions    Universal entry actions
```

### Why Split Transitions?

**Reason 1: Reusability**

Multiple states might transition TO the same target:
```
State 1 → State 6:  From(1→6) + To(→6)
State 2 → State 6:  From(2→6) + To(→6)
State 5 → State 6:  From(5→6) + To(→6)
```

The **To(→6)** actions are reused across all transitions! This eliminates duplication.

**Reason 2: Composability**

The "always-execute" nature of ToTransitions ensures that no matter how you reach a state, certain actions always run:
- Initialize state-specific resources
- Verify critical elements are visible
- Set up state-specific context

**Reason 3: Separation of Concerns**

- **FromTransition**: "How do I leave my current location?"
- **ToTransition**: "What always happens when arriving at the destination?"

This separation mirrors natural decomposition of navigation tasks.

**Example**:

Going from Login → Dashboard might involve:
- **FromTransition (Login→Dashboard)**: Type credentials, click submit
- **ToTransition (→Dashboard)**: Wait for logo, initialize dashboard widgets

Going from Settings → Dashboard would have:
- **FromTransition (Settings→Dashboard)**: Click back button
- **ToTransition (→Dashboard)**: *Same as above* - wait for logo, initialize widgets

The ToTransition is shared, avoiding code duplication.

### Code Example: The DoT Application

**Note**: The following example is based on the paper's DoT application (Appendix II). Brobot also supports annotation-based transitions (see "Modern Annotation Pattern" below).

```java
// From the DoT test application in the paper (Appendix II)
public class WorldTransitions {
    // Injected dependencies
    private final Action action;
    private final WorldState world;
    private final StateTransitionsRepository stateTransitionsRepository;

    public WorldTransitions(Action action, WorldState world,
                           StateTransitionsRepository repo) {
        this.action = action;
        this.world = world;
        this.stateTransitionsRepository = repo;

        // Define transitions FROM the WORLD state
        StateTransitions transitions =
            new StateTransitions.Builder("WORLD") // State name as string
                .addTransitionFinish(this::finishTransition) // ToTransition
                .addTransition(new JavaStateTransition.Builder() // Note: JavaStateTransition, not StateTransition
                    .addToActivate("ISLAND") // Activate ISLAND state (state name as string)
                    .setFunction(this::goToIsland) // The action sequence
                    .setStaysVisibleAfterTransition(StateTransition.StaysVisible.TRUE) // WORLD remains visible
                    .build())
                .build();

        stateTransitionsRepository.add(transitions);
    }

    // ToTransition: always executed when entering WORLD state
    private boolean finishTransition() {
        ActionOptions find = new ActionOptions.Builder()
            .setMaxWait(10)
            .build();
        ObjectCollection worldImages = new ObjectCollection.Builder()
            .withAllStateImages(world.getState())
            .build();
        return action.perform(find, worldImages).isSuccess();
    }

    // FromTransition: WORLD → ISLAND action sequence
    public boolean goToIsland() {
        // The action sequence for this transition is a single click
        return action.perform(
            ActionType.CLICK, world.getSearchButton()
        ).isSuccess();
    }
}
```

**Mapping to Formal Model**:

This code implements the transition tuple t = (A, S_t^def):
- **A** = `goToIsland()` method (action sequence: click search button)
- **S_t^def** = {(ISLAND, True), (WORLD, True)} (encoded via `.addToActivate("ISLAND")` and `.setStaysVisibleAfterTransition(TRUE)`)

The ToTransition `finishTransition()` always verifies World state images are visible, ensuring the state is properly activated.

### Modern Annotation Pattern (Alternative)

Brobot also supports annotation-based transitions for cleaner code:

```java
@TransitionSet(state = WorldState.class)
@Component
public class WorldTransitions {
    @Autowired
    private Action action;

    @Autowired
    private WorldState world;

    @OutgoingTransition(
        activate = {IslandState.class},
        staysVisible = true
    )
    public boolean goToIsland() {
        return action.click(world.getSearchButton()).isSuccess();
    }

    @IncomingTransition
    public boolean finishTransition() {
        return action.find(world.getAllImages()).isSuccess();
    }
}
```

**Advantages**:
- Less boilerplate code
- Declarative state relationships
- Automatic registration with Spring context
- Type-safe state references

## Transitions in Path Traversal

<!-- Paper Reference: Section 5.7 - The Path Traversal Model -->

Transitions are the fundamental building blocks of **paths** in the state graph. Understanding how transitions compose into paths is essential for understanding navigation.

### Paths as Transition Sequences

A path is defined as ρ = (S_ρ, T_ρ) where:
- **S_ρ** = [s₀, s₁, ..., sₙ] is the sequence of states
- **T_ρ** = [t₀, t₁, ..., tₙ₋₁] is the sequence of transitions

**Path Validity Conditions**:
1. **s₀ ∈ S_Ξ** - First state must be currently active
2. **sₙ = s_target** - Last state must be the target
3. **(s_i, t_i) ∈ δ** - Each transition must be accessible from its source state
4. **(s_{i+1}, True) ∈ S_{t_i}^def** - Each transition must activate the next state

**Example Path**:
```
Active States: {Login}
Target: Settings

Valid Path:
S_ρ = [Login, Dashboard, Settings]
T_ρ = [t_login, t_toSettings]

Where:
- (Login, t_login) ∈ δ ✓
- (Dashboard, True) ∈ S_{t_login}^def ✓
- (Dashboard, t_toSettings) ∈ δ ✓
- (Settings, True) ∈ S_{t_toSettings}^def ✓
```

### Dynamic Path Selection with Transition Failures

When a transition fails, the Path Traversal Model (§) dynamically recalculates paths:

**Scenario**:
```
Initial Path: Start → [t1] → State2 → [t2] → State5 → [t3] → End
                      ✓              ✗
```

**What Happens**:
1. Transition t1 succeeds → State2 becomes active
2. Transition t2 **fails** → State5 not activated
3. Framework queries State Management: "What states are active?"
4. Answer: {Start, State2} (State2 is active, State5 is not)
5. Framework calculates **new paths** from {Start, State2} to End
6. Framework selects alternative path:
   ```
   State2 → [t4] → State6 → [t5] → End
   ```

**This is only possible because**:
- Transitions are part of an explicit graph structure (δ ⊆ S × T)
- State Management tracks active states (S_Ξ)
- Path Traversal can recalculate from current position

In process-based automation, there is no graph, no state tracking, and no recalculation. A failed step means total failure.

### Transition Costs and Path Selection

The framework can assign costs to transitions to guide path selection:

**Transition Cost Function**: c_T: T → ℝ

**Path Cost**:
```
c(ρ) = Σ[c_T(t_i) + c_S(s_{i+1})]
```

**Path Selection** (heuristic-based):
```
ρ* = arg min_{ρ ∈ P}[c(ρ)]
```

**What This Means**: Among all valid paths P, select the path ρ* with minimum total cost.

**Example Transition Costs**:
- Fast, reliable transition: cost = 1.0
- Slow transition (requires waiting): cost = 5.0
- Unreliable transition (sometimes fails): cost = 10.0

The framework will prefer paths with lower total cost, avoiding slow or unreliable transitions when alternatives exist.

## Formal Properties of Transitions

Understanding these formal properties helps reason about transition behavior:

### 1. Transition Composability

**Property**: Executing transitions in sequence composes their effects.

For path ρ with transitions T_ρ = [t₀, t₁, ..., tₙ₋₁]:
```
Result of path = f_τ(tₙ₋₁) ∘ f_τ(tₙ₋₂) ∘ ... ∘ f_τ(t₀)
```

Each transition builds on the GUI state produced by the previous transition.

### 2. Conditional Execution

**Property**: Transition t_i executes **only if** all previous transitions succeeded.

```
t_i executes ⟺ ∀j < i: r_{t_j}.success = True
```

If any transition in a path fails, subsequent transitions do not execute.

### 3. State Information Consistency

**Property**: Successful transitions guarantee their defined state changes.

```
If r_t.success = True, then S_t = S_t^def
```

The State Management system (M) applies S_t to update active states S_Ξ.

### 4. Transition Accessibility

**Property**: A transition can only be executed if accessible from an active state.

```
t can be executed ⟺ ∃s ∈ S_Ξ: (s, t) ∈ δ
```

### 5. State Activation Guarantee

**Property**: If (s, True) ∈ S_t^def and transition succeeds, then s ∈ S_Ξ after transition.

**Proof**:
1. Transition succeeds → r_t.success = True
2. By Property 3 → S_t = S_t^def
3. State Management applies S_t → s becomes active
4. Therefore → s ∈ S_Ξ

### 6. Process Function Determinism

**Property**: For fixed action sequence A and fixed starting GUI Ξ, f_A produces deterministic result.

```
f_A(A)(Ξ) is deterministic (ignoring environmental stochasticity Θ)
```

However, in practice, environmental stochasticity (network delays, rendering variations, etc.) can affect results.

## Practical Guidance for Defining Transitions

### When to Create a Transition

Create a transition when:
1. **State-to-state navigation exists**: There's a clear path from one GUI configuration to another
2. **Actions are repeatable**: The action sequence can be executed multiple times
3. **State relationship is known**: You know which states should activate/deactivate
4. **Reusability matters**: The transition might be part of multiple paths

### How Granular Should Transitions Be?

**Decision Tree**:

```
Is this a single atomic action?
├─ YES: Consider making it a transition if it changes states
└─ NO: Is it a coherent sequence with clear start/end states?
    ├─ YES: Make it a transition
    └─ NO: Break into smaller transitions
```

**Examples**:

**Too Granular** (anti-pattern):
```java
// Each click is a separate transition
transition1: click(menu)
transition2: click(submenu)
transition3: click(item)
```

**Good Granularity**:
```java
// Coherent sequence as one transition
transitionToItem: [click(menu), click(submenu), click(item)]
```

**Too Coarse** (anti-pattern):
```java
// Entire workflow as one transition
transitionCompleteWorkflow: [login, navigate, processData, logout]
```

**Good Granularity**:
```java
// Logical steps as separate transitions
transitionLogin: [enterCredentials, clickSubmit]
transitionNavigate: [clickMenu, selectOption]
transitionProcess: [loadData, transform, save]
transitionLogout: [clickLogout]
```

### FromTransition vs ToTransition: When to Use Each

**Use FromTransition when**:
- Actions depend on the **source state**
- Different source states require different actions to reach the same target
- The action is about "leaving" the current state

**Use ToTransition when**:
- Actions are **always required** when entering the target state
- The action is independent of where you came from
- The action is about "entering" or "initializing" the target state

**Example**:

Entering Dashboard from multiple states:
```java
// FromTransition (Login → Dashboard): credentials required
fromLogin() {
    type(username);
    type(password);
    click(submit);
}

// FromTransition (Settings → Dashboard): just click back
fromSettings() {
    click(backButton);
}

// ToTransition (→ Dashboard): always verify dashboard loaded
toDashboard() {
    waitFor(dashboardLogo);
    verifyWidget(summaryWidget);
}
```

## Common Mistakes and How to Avoid Them

### Mistake 1: Not Using S_t^def

**Wrong**:
```java
// Only actions, no state information
new JavaStateTransition.Builder()
    .setFunction(this::navigate)
    .build();
```

**Why It's Wrong**: Framework doesn't know which states should activate/deactivate. State management becomes implicit and error-prone.

**Correct**:
```java
new JavaStateTransition.Builder()
    .setFunction(this::navigate)
    .addToActivate("TargetState")
    .addToDeactivate("SourceState")
    .build();
```

### Mistake 2: Including Verification in Action Sequence

**Wrong**:
```java
public boolean navigate() {
    action.click(button);
    // Verification shouldn't be in action sequence
    if (!action.find(targetElement).isSuccess()) {
        return false;
    }
    return true;
}
```

**Why It's Wrong**: Verification is the framework's job via State Management. Including it here duplicates logic.

**Correct**:
```java
public boolean navigate() {
    // Just perform the action
    return action.click(button).isSuccess();
}
// Let S_t^def and State Management handle verification
```

### Mistake 3: Forgetting setStaysVisibleAfterTransition

**Wrong**:
```java
// World stays visible when Island opens, but not specified
new JavaStateTransition.Builder()
    .addToActivate("ISLAND")
    .setFunction(this::goToIsland)
    .build();
// Default behavior: World deactivates (WRONG)
```

**Why It's Wrong**: Framework will deactivate the source state by default, even though World remains visible.

**Correct**:
```java
new JavaStateTransition.Builder()
    .addToActivate("ISLAND")
    .setFunction(this::goToIsland)
    .setStaysVisibleAfterTransition(StateTransition.StaysVisible.TRUE)
    .build();
```

### Mistake 4: Using StateTransition.Builder Instead of JavaStateTransition.Builder

**Wrong**:
```java
new StateTransition.Builder() // StateTransition is an interface!
    .addToActivate("Target")
    .build();
```

**Why It's Wrong**: `StateTransition` is an interface, not a concrete class. This won't compile.

**Correct**:
```java
new JavaStateTransition.Builder() // JavaStateTransition is the implementation
    .addToActivate("Target")
    .build();
```

### Mistake 5: Passing State Objects Instead of State Names

**Wrong**:
```java
.addToActivate(DashboardState.class) // Wrong type
```

**Why It's Wrong**: The method expects state **names** (strings), not class objects.

**Correct**:
```java
.addToActivate("Dashboard") // State name as string
```

## Testing Transitions

### Unit Testing a Transition

```java
@ExtendWith(MockitoExtension.class)
class WorldTransitionsTest {
    @Mock
    private Action action;

    @Mock
    private WorldState world;

    @InjectMocks
    private WorldTransitions transitions;

    @Test
    void testGoToIsland_success() {
        // Arrange
        StateImage searchButton = mock(StateImage.class);
        when(world.getSearchButton()).thenReturn(searchButton);

        ActionResult successResult = new ActionResult();
        successResult.setSuccess(true);
        when(action.perform(ActionType.CLICK, searchButton))
            .thenReturn(successResult);

        // Act
        boolean result = transitions.goToIsland();

        // Assert
        assertTrue(result);
        verify(action).perform(ActionType.CLICK, searchButton);
    }
}
```

### Integration Testing Path Traversal

```java
@SpringBootTest
class PathTraversalIntegrationTest {
    @Autowired
    private StateNavigator navigator;

    @Test
    void testNavigateWorldToIsland() {
        // Arrange: Start at World state
        stateMemory.setActiveStates("WORLD");

        // Act: Navigate to Island
        boolean success = navigator.openState("ISLAND");

        // Assert: Both states should be active
        assertTrue(success);
        assertTrue(stateMemory.isActive("WORLD"));
        assertTrue(stateMemory.isActive("ISLAND"));
    }
}
```

## Implementation Details

**Key Files in Brobot**:

1. **StateTransition.java** (interface)
   - Location: `/library/src/main/java/io/github/jspinak/brobot/model/transition/StateTransition.java`
   - Defines transition contract

2. **JavaStateTransition.java** (implementation)
   - Location: `/library/src/main/java/io/github/jspinak/brobot/navigation/transition/JavaStateTransition.java`
   - Line 208: `.addToActivate(String... stateNames)` method
   - Line 180: `.setStaysVisibleAfterTransition(StaysVisible)` method

3. **StateTransitions.java** (container)
   - Location: `/library/src/main/java/io/github/jspinak/brobot/navigation/transition/StateTransitions.java`
   - Manages collection of transitions for a state

4. **TransitionFunction.java** (executor)
   - Location: `/library/src/main/java/io/github/jspinak/brobot/actions/methods/basicactions/transitions/TransitionFunction.java`
   - Implements f_τ function

## Key Theorems and Properties Summary

| Property | Formal Statement | Intuitive Meaning |
|----------|------------------|-------------------|
| **Transition Definition** | t = (A, S_t^def) | Transition = actions + state info |
| **Transition Function** | f_τ: (A, S_t^def) → (Ξ', r_t) | Executes transition, returns result |
| **Process Function** | f_A: (A) → (Ξ') | Executes action sequence |
| **Transition Relation** | δ ⊆ S × T | Maps states to accessible transitions |
| **State Information Type** | S_t^def ∈ P(S × {True, False}) | Set of (state, activate/deactivate) pairs |
| **Transition Result** | r_t = (success, S_t) | Contains success flag and state info |
| **State Application** | S_t = S_t^def if success, ∅ if fail | State changes applied only on success |
| **Path Validity** | (s_i, t_i) ∈ δ ∧ (s_{i+1}, True) ∈ S_{t_i}^def | Transition accessible and activates next state |

## Comparison with Finite State Machines (FSMs)

Model-based GUI automation shares concepts with FSMs but has critical differences:

| Aspect | Traditional FSM | Model-Based GUI Automation |
|--------|-----------------|---------------------------|
| **States** | Single active state | Multiple simultaneous active states |
| **Transitions** | State → State | State → {States} with explicit S_t^def |
| **Determinism** | Deterministic transitions | Non-deterministic (stochasticity Θ) |
| **Observation** | Perfect state knowledge | Probabilistic pattern matching |
| **Recovery** | No error recovery | Dynamic path recalculation |
| **State Information** | Implicit in transition | Explicit in S_t^def |

The key innovation is **S_t^def**, which allows transitions to explicitly encode state relationships that FSMs leave implicit.

## Further Reading

### Academic Foundation
- **[Academic Foundation](./academic-foundation.md)** - Research background and citations
  - Section 5.6: The Transition Model (τ)
  - Section 6.3: The Transition Implementation
  - Figure 8: FromTransition and ToTransition architecture

### Related Models
- **[Overall Model](./overall-model.md)** - Complete formal model (Ξ, Ω, a, M, τ, §)
- **[States](./states.md)** - State Structure and state management
- **[Testing the Automation](./testing-automation.md)** - Testing transitions and paths

### Practical Implementation
- **[AI Brobot Project Creation Guide](../01-getting-started/ai-brobot-project-creation.md)** - Complete API reference
- **[Getting Started](../01-getting-started/)** - Hands-on tutorials

## Appendix: Mathematical Notation Quick Reference

- **t** = A single transition
- **A** = Action sequence (a¹, a², ..., aⁿ)
- **S_t^def** = Defined state information for transition t
- **f_τ** = Transition function
- **f_A** = Process function (executes action sequence)
- **δ** = Transition relation (which transitions are accessible from which states)
- **r_t** = Transition result
- **Ξ'** = Resulting GUI state after transition
- **ρ** = Path (sequence of states and transitions)
- **S_ρ** = State sequence in path
- **T_ρ** = Transition sequence in path
- **c_T(t)** = Cost of transition t
- **P(S × {True, False})** = Power set of (state, boolean) pairs
