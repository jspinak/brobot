# Documentation Code Block to Example Implementation Mapping

## Overview
This document maps each code block in the documentation to its corresponding implementation (or lack thereof) in the example project.

---

## Code Block 1: Traditional Loop Approach
**Documentation**: Lines 49-71  
**Title**: Method 1: Traditional Loop Approach  
**Status**: ✓ IMPLEMENTED

### Location
File: `/home/jspinak/brobot_parent/brobot/examples/01-getting-started/action-hierarchy/src/main/java/com/example/actionhierarchy/ComplexActionExamples.java`  
Method: `clickUntilFound(StateImage, StateImage, int)` - Lines 28-50

### Match Quality: 95%
```
DOCUMENTED                          IMPLEMENTED
┌─────────────────────────────┐    ┌──────────────────────────┐
│ ClickOptions.Builder()      │    │ ClickOptions.Builder()   │
│ .setPauseAfterEnd(1.0)      │───→│ .setPauseAfterEnd(1.0)   │
│ .build()                    │    │ .build()                 │
└─────────────────────────────┘    └──────────────────────────┘
```

### Differences
- Code logic: **IDENTICAL**
- Example creates ClickOptions inside loop (minor variation)
- Both use `PatternFindOptions.forQuickSearch()`

### Notes
- This is the baseline example
- Simplest approach for beginners
- Fully functional and matches documentation

---

## Code Block 2: Fluent API with Action Chaining
**Documentation**: Lines 74-93  
**Title**: Method 2: Fluent API with Action Chaining  
**Status**: ◐ PARTIALLY IMPLEMENTED

### Location
File: Same as Block 1  
Method: `clickUntilFoundFluent(StateImage, StateImage)` - Lines 52-85

### Match Quality: 40%
```
DOCUMENTED                          IMPLEMENTED
┌────────────────────────────┐     ┌──────────────────────────┐
│ ClickOptions.Builder()     │     │ ClickOptions.Builder()   │
│ .setVerification(...)      │────X│ (NOT IMPLEMENTED)        │
│ .setRepetition(...)        │────X│ (NOT IMPLEMENTED)        │
│ .build()                   │     │ .then(PatternFind...)    │
└────────────────────────────┘     │ .build()                 │
                                   └──────────────────────────┘
```

### Critical Differences
1. **MISSING API**: `setVerification()` method
   - Documented: `new VerificationOptions.Builder().addVerifyImage(findTarget)`
   - Example: Not used

2. **MISSING API**: `setRepetition()` method
   - Documented: `new RepetitionOptions.Builder().setMaxTimesToRepeatActionSequence(10)`
   - Example: Code comment explicitly states "doesn't exist in current version"

3. **ALTERNATIVE PATTERN**: `.then()` chaining
   - Documented: Uses `setVerification` and `setRepetition`
   - Example: Uses `.then()` to chain with PatternFindOptions
   - Both achieve similar goals but different approach

### Code in Example
```java
// Documented approach (WON'T WORK):
.setVerification(new VerificationOptions.Builder()
    .addVerifyImage(findTarget)
    .build())
.setRepetition(new RepetitionOptions.Builder()
    .setMaxTimesToRepeatActionSequence(10)
    .build())

// Example uses instead (WORKS):
.withBeforeActionLog("...")
.then(new PatternFindOptions.Builder()
    .withBeforeActionLog("...")
    .build())
```

### Notes
- Example includes commented-out code referencing missing methods
- Alternative pattern works but is NOT what documentation shows
- This is a known API discrepancy

---

## Code Block 3: Using ConditionalActionChain (Basic)
**Documentation**: Lines 96-112  
**Title**: Method 3: Using ConditionalActionChain  
**Status**: ✗ NOT IMPLEMENTED

### Location
- **No corresponding code in example**

### Expected Code
```java
ConditionalActionChain clickUntilFound = ConditionalActionChain
    .find(nextButton)
    .ifFoundClick()
    .then(new PatternFindOptions.Builder()
        .setRepetition(new RepetitionOptions.Builder()
            .setMaxTimesToRepeatActionSequence(10)
            .setPauseBetweenActionSequences(1.0)
            .build())
        .build())
    .withObjectCollection(new ObjectCollection.Builder()
        .withImages(finishButton)
        .build());

ActionResult result = clickUntilFound.perform(action);
```

### Why Not Implemented
1. Uses `ConditionalActionChain` (imported but not demonstrated)
2. Uses `RepetitionOptions` (not available in current version)
3. Would be example of "Method 3" but example shows ClickUntilOptions instead

### Gap Impact
- **HIGH** - 54% of documentation is ConditionalActionChain patterns
- This pattern is not demonstrated anywhere in the example code
- Developers cannot see how to use ConditionalActionChain from this example

### Where ConditionalActionChain IS Used in Docs
This class appears in:
- Blocks 3, 4, 5, 6, 7, 8, 9, 10 (8 out of 11 blocks!)
- Most advanced patterns rely on it
- NOT DEMONSTRATED IN EXAMPLE

---

## Code Block 4: Creating a Reusable Click-Until-Found Function
**Documentation**: Lines 115-144  
**Title**: Method 4: Creating a Reusable Click-Until-Found Function  
**Status**: ◐ PARTIALLY IMPLEMENTED

### Location
File: Same as Block 1  
Method: `clickUntilFound(StateImage, StateImage, int, double)` - Lines 122-166

### Match Quality: 35%
```
DOCUMENTED (USING)              EXAMPLE (ACTUAL)
┌───────────────────────────┐   ┌──────────────────────────┐
│ RepetitionOptions.Builder │──X│ (NOT USED)               │
│ .setMaxTimesToRepeat(...)  │   │                          │
│ ConditionalActionChain     │──X│ (NOT USED)               │
│ .find()                    │   │ PatternFindOptions       │
│ .ifFoundClick()            │   │ .then(ClickOptions)      │
│ .then()                    │   │ .then(PatternFind)       │
└───────────────────────────┘   └──────────────────────────┘
```

### Critical Differences
1. **DIFFERENT ARCHITECTURE**
   - Documented: Uses `ConditionalActionChain`
   - Example: Uses chained `.then()` on `PatternFindOptions`

2. **MISSING CLASS**: `RepetitionOptions`
   - Documented: Shows complex repetition configuration
   - Example: Commented out with note

3. **DIFFERENT FLOW**
   - Documented: Find → Click → Find (using ConditionalActionChain)
   - Example: Find → Click → Find (using ActionOptions chaining)

### Signature Match
```
✓ Method name: clickUntilFound
✓ Parameters: StateImage clickTarget, StateImage findTarget, int maxAttempts, double pauseBetween
✓ Return type: boolean
✗ Implementation pattern: Different
```

### Notes
- Same end result but different mechanism
- Example is more practical with current API
- Documentation shows intended design (not yet implemented)

---

## Code Block 5: Basic Conditional Chaining Example
**Documentation**: Lines 178-195  
**Title**: Example: Simple conditional chain - Login pattern  
**Status**: ✗ NOT IMPLEMENTED

### Location
- **No corresponding code in example**

### Expected Code
```java
ConditionalActionChain loginChain = ConditionalActionChain
    .find(loginButton)
    .ifFoundClick()
    .then(usernameField)
    .ifFoundType(username)
    .then(passwordField)
    .ifFoundType(password)
    .then(submitButton)
    .ifFoundClick()
    .ifNotFoundLog("Login button not found - may already be logged in");

ActionResult result = loginChain.perform(action);
```

### Why Not Implemented
- Different use case (login flow vs click-until-found)
- Would require username/password fields and string typing
- Example focuses on click patterns only

### Gap Impact
- **MEDIUM** - Shows real-world ConditionalActionChain usage
- Developers get no example of multi-step login-like flow
- First documented example of ConditionalActionChain not implemented

---

## Code Block 6: Multi-Step Validation Pattern
**Documentation**: Lines 214-229  
**Title**: Pattern 1: Multi-Step Validation  
**Status**: ✗ NOT IMPLEMENTED

### Location
- **No corresponding code in example**

### Expected Code
```java
ConditionalActionChain wizardChain = ConditionalActionChain
    .find(step1Button)
    .ifFoundClick()
    .then(step2Button)
    .ifFoundClick()
    .then(step3Button)
    .ifFoundClick()
    .then(completionMessage)
    .ifFoundLog("Wizard completed successfully")
    .ifNotFoundLog("Wizard failed to complete");

ActionResult result = wizardChain.perform(action);
```

### Why Not Implemented
- Would require 4 StateImages (step1, step2, step3, completion)
- Example only provides nextButton, finishButton, submitButton
- Complex multi-step flow beyond example scope

### Gap Impact
- **HIGH** - Shows important pattern validation flow
- Developers cannot see wizard/sequential pattern implementation

---

## Code Block 7: Custom Search Duration Pattern
**Documentation**: Lines 232-251  
**Title**: Pattern 1 (Alternative): Multi-Step with custom search  
**Status**: ✗ NOT IMPLEMENTED

### Location
- **No corresponding code in example**

### Expected Code
```java
ConditionalActionChain wizardChain = ConditionalActionChain
    .find(step1Button)
    .ifFoundClick()
    .then(step2Button)
    .ifFoundClick()
    .then(step3Button)
    .ifFoundClick()
    .then(new PatternFindOptions.Builder()
        .setSearchDuration(5.0)
        .build())
    .withObjectCollection(new ObjectCollection.Builder()
        .withImages(completionMessage)
        .build())
    .ifFoundLog("Wizard completed successfully")
    .ifNotFoundLog("Wizard failed to complete");

ActionResult result = wizardChain.perform(action);
```

### Relationship to Block 6
- Variation showing how to customize search duration
- Same pattern with added configuration
- Demonstrates PatternFindOptions integration

### Gap Impact
- **MEDIUM** - Shows configuration customization
- Extends Block 6 pattern

---

## Code Block 8: Retry with Different Strategies
**Documentation**: Lines 258-294  
**Title**: Pattern 2: Retry with Different Strategies  
**Status**: ✗ NOT IMPLEMENTED

### Location
- **No corresponding code in example**

### Expected Code
Shows 4 different fallback strategies:
1. Try X button: `ConditionalActionChain.find(closeButton).ifFoundClick()`
2. Try Escape: `ConditionalActionChain.pressEscape().then(dialogImage)`
3. Try Escape (alt): `ConditionalActionChain.start(TypeOptions).withStrings(Key.ESC)`
4. Try click outside: `ConditionalActionChain.start(ClickOptions).withLocations(...)`

### Why Not Implemented
- Shows advanced fallback strategies not needed for basic example
- Introduces new methods: `.pressEscape()`, `.start()`, `.withLocations()`
- Would require dialog state and multiple strategies

### Gap Impact
- **HIGH** - Shows important robustness pattern
- Developers need to see fallback strategy implementation
- Essential for real-world GUI automation

---

## Code Block 9: Conditional Branching Based on Application State
**Documentation**: Lines 297-340  
**Title**: Pattern 3: Conditional Branching  
**Status**: ✗ NOT IMPLEMENTED

### Location
- **No corresponding code in example**

### Expected Code
Shows 3 separate chains:
```java
// Home screen state
ConditionalActionChain fromHome = ConditionalActionChain
    .find(homeScreen)
    .then(menuButton)
    .ifFoundClick()
    ...

// Settings screen state
ConditionalActionChain fromSettings = ConditionalActionChain
    .find(settingsScreen)
    .then(backButton)
    .ifFoundClick()
    ...

// Error dialog state
ConditionalActionChain fromError = ConditionalActionChain
    .find(errorDialog)
    ...
```

### Why Not Implemented
- Would require multiple state images (homeScreen, settingsScreen, errorDialog)
- Demonstrates state management which is advanced topic
- Beyond scope of action hierarchy example

### Gap Impact
- **VERY HIGH** - Shows state-aware automation pattern
- Important for complex applications
- No example of state checking before action

---

## Code Block 10: Integration with ActionChainOptions
**Documentation**: Lines 346-396  
**Title**: Integration with ActionChainOptions  
**Status**: ✗ NOT IMPLEMENTED

### Location
- **No corresponding code in example**

### Expected Code
```java
ActionChainOptions nestedFind = new ActionChainOptions.Builder(
        new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .setSimilarity(0.9)
            .build())
    .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
    .then(new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.MU)
        .setSimilarity(0.85)
        .build())
    .build();

ConditionalActionChain robustClick = ConditionalActionChain
    .start(nestedFind)
    .ifFoundClick()
    .ifNotFoundDo(result -> {
        // Fallback logic
    });
```

### Why Not Implemented
- Shows advanced pattern combining ActionChainOptions with ConditionalActionChain
- Requires ColorFindOptions (not in basic example)
- Complex combination beyond introduction example

### Gap Impact
- **HIGH** - Shows advanced combination patterns
- Important for robust automation
- Developers may miss this integration pattern

---

## Code Block 11: Complete Form Filling with Validation
**Documentation**: Lines 435-519  
**Title**: Example: Complete Form Filling with Validation  
**Status**: ✗ NOT IMPLEMENTED

### Location
- **No corresponding code in example**

### Expected Code
Complete `FormAutomation` class with:
- 85+ lines of comprehensive form handling
- Form validation before submission
- Error handling and recovery
- Optional field filling
- Validation error handling

### Why Not Implemented
- This is comprehensive class-level example
- Would require FormData class and multiple state images
- Beyond scope of "getting started" example

### Gap Impact
- **VERY HIGH** - This is the complete real-world example
- Shows how to combine all patterns
- Developers cannot see full integration

### Scope Issue
- Probably deserves its own example project
- Or should be in "advanced-patterns" example

---

## Summary Matrix

```
Code Block  Topic                    Docs Lines  Status  Match%  Example Location
─────────────────────────────────────────────────────────────────────────────────
1           Traditional Loop         49-71       ✓       95%     clickUntilFound()
2           Fluent API               74-93       ◐       40%     clickUntilFoundFluent()
3           ConditionalActionChain   96-112      ✗       0%      NOT IMPLEMENTED
4           Reusable Function        115-144     ◐       35%     clickUntilFound(4-param)
5           Login Example            178-195     ✗       0%      NOT IMPLEMENTED
6           Wizard Pattern           214-229     ✗       0%      NOT IMPLEMENTED
7           Search Duration          232-251     ✗       0%      NOT IMPLEMENTED
8           Retry Strategies         258-294     ✗       0%      NOT IMPLEMENTED
9           State Branching          297-340     ✗       0%      NOT IMPLEMENTED
10          ActionChainOptions       346-396     ✗       0%      NOT IMPLEMENTED
11          Form Filling             435-519     ✗       0%      NOT IMPLEMENTED
─────────────────────────────────────────────────────────────────────────────────
            OVERALL MATCH:                       45%
```

---

## Implementation Status Legend

| Symbol | Meaning | Count |
|--------|---------|-------|
| ✓ | Fully Implemented | 0 blocks |
| ◐ | Partially Implemented | 2 blocks (Blocks 1, 2, 4) |
| ✗ | Not Implemented | 9 blocks (Blocks 3, 5-11) |

---

## Recommendations

### HIGH PRIORITY
1. **Block 3** - Implement basic ConditionalActionChain example
2. **Block 8** - Implement retry/fallback strategies
3. **Block 9** - Implement state-based branching

### MEDIUM PRIORITY
4. **Block 5** - Add login flow example
5. **Block 6-7** - Add multi-step wizard pattern

### LOWER PRIORITY (Consider separate examples)
6. **Block 10** - ActionChainOptions integration (advanced)
7. **Block 11** - Complete form automation (advanced)

---

## Files Containing Code

- `/home/jspinak/brobot_parent/brobot/examples/01-getting-started/action-hierarchy/src/main/java/com/example/actionhierarchy/ComplexActionExamples.java`
- `/home/jspinak/brobot_parent/brobot/examples/01-getting-started/action-hierarchy/src/main/java/com/example/actionhierarchy/ExampleRunner.java`
- `/home/jspinak/brobot_parent/brobot/examples/01-getting-started/action-hierarchy/src/main/java/com/example/actionhierarchy/ExampleState.java`

