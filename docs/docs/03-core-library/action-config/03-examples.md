---
sidebar_position: 3
---

# ActionConfig Code Examples

This page provides practical examples for using each ActionConfig class in real-world scenarios.

> **New to ActionConfig?** Start with the [ActionConfig Overview](./01-overview.md) for concepts and patterns.
>
> **Migrating from ActionOptions?** See the [Upgrading to Latest](../migration/upgrading-to-latest.md) guide.
>
> **Looking for simpler alternatives?** Check out [Convenience Methods](./18-convenience-methods.md) for direct action calls.

## Setup for Examples

All examples assume the following setup:

```java
import org.springframework.beans.factory.annotation.Autowired;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.histogram.HistogramFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.*;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.VerificationOptions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.movement.Movement;
import io.github.jspinak.brobot.datatypes.primitives.match.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.manageStates.mouse.MouseButton;
import org.sikuli.script.Pattern;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;

@Autowired
private Action action;

// Example StateImage objects used in examples
StateImage loginButton = new StateImage.Builder()
    .withPattern(new Pattern("login-button.png"))
    .build();

StateImage icon = new StateImage.Builder()
    .withPattern(new Pattern("icon.png"))
    .build();

// Example data class for form automation
public class UserData {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    public UserData(String firstName, String lastName, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }
}
```

## Find Actions

### Basic Pattern Finding

```java
// Simple find with default settings
PatternFindOptions find = new PatternFindOptions.Builder()
    .setSimilarity(0.85)
    .build();

ActionResult result = action.perform(find, loginButton);
```

### Find All Matches

```java
// Find all instances of an icon
PatternFindOptions findAll = new PatternFindOptions.Builder()
    .setSimilarity(0.8)
    .setMaxMatchesToActOn(-1)  // Find all matches
    .setCaptureImage(true)     // Capture screenshot for debugging
    .build();

ActionResult result = action.perform(findAll, icon);
System.out.println("Found " + result.getMatchList().size() + " matches");
```

### Find in Specific Region

```java
Region searchArea = new Region(100, 100, 400, 300);

PatternFindOptions findInRegion = new PatternFindOptions.Builder()
    .setSimilarity(0.9)
    .setSearchRegions(new SearchRegions(searchArea))
    .build();

StateImage targetImage = new StateImage.Builder()
    .withPattern(new Pattern("target.png"))
    .build();

ActionResult result = action.perform(findInRegion, targetImage);
```

### Histogram-Based Finding

```java
// Find by color distribution
HistogramFindOptions colorFind = new HistogramFindOptions.Builder()
    .setHueBins(30)
    .setSaturationBins(32)
    .setValueBins(32)
    .build();

StateImage colorfulImage = new StateImage.Builder()
    .withPattern(new Pattern("colorful-element.png"))
    .build();

ActionResult result = action.perform(colorFind, colorfulImage);
```

> For more find strategies and options, see the [Find Options Reference](./05-reference.md#find-options).

## Click Actions

### Simple Click

```java
ClickOptions click = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .build();

StateImage button = new StateImage.Builder()
    .withPattern(new Pattern("button.png"))
    .build();

action.perform(click, button);
```

### Double Click with Timing

```java
ClickOptions doubleClick = new ClickOptions.Builder()
    .setNumberOfClicks(2)
    .setPressOptions(MousePressOptions.builder()
        .setPauseBetweenClicks(0.1)
        .setPauseBeforeMouseDown(0.2)
        .setPauseAfterMouseUp(0.2)
        .build())
    .build();

StateImage fileIcon = new StateImage.Builder()
    .withPattern(new Pattern("file-icon.png"))
    .build();

action.perform(doubleClick, fileIcon);
```

### Right Click Menu

```java
ClickOptions rightClick = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .setPressOptions(MousePressOptions.builder()
        .setButton(MouseButton.RIGHT)
        .build())
    .build();

StateImage targetElement = new StateImage.Builder()
    .withPattern(new Pattern("target-element.png"))
    .build();

action.perform(rightClick, targetElement);
```

### Click with Verification

```java
// Click and verify result appears
StateImage expectedPopup = new StateImage.Builder()
    .withPattern(new Pattern("expected-popup.png"))
    .build();

ObjectCollection popupCollection = new ObjectCollection.Builder()
    .withStateImages(expectedPopup)
    .build();

ClickOptions verifiedClick = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .setVerificationOptions(VerificationOptions.builder()
        .setEvent(VerificationOptions.Event.OBJECTS_APPEAR)
        .setObjectCollection(popupCollection)
        .build())
    .build();

StateImage submitButton = new StateImage.Builder()
    .withPattern(new Pattern("submit-button.png"))
    .build();

action.perform(verifiedClick, submitButton);
```

## Type Actions

### Simple Text Entry

```java
TypeOptions typeText = new TypeOptions.Builder()
    .setText("user@example.com")
    .build();

action.perform(typeText);
```

### Type with Delays

```java
TypeOptions slowType = new TypeOptions.Builder()
    .setText("Important message")
    .setPauseBeforeKeyDown(0.1)
    .setPauseAfterKeyUp(0.1)
    .setTypeDelay(0.05)
    .build();

action.perform(slowType);
```

### Keyboard Shortcuts

```java
// Ctrl+A (Select All)
TypeOptions selectAll = new TypeOptions.Builder()
    .setText("a")
    .setModifierKeys("ctrl")
    .build();

action.perform(selectAll);

// Ctrl+Shift+S (Save As)
TypeOptions saveAs = new TypeOptions.Builder()
    .setText("s")
    .setModifierKeys("ctrl", "shift")
    .build();

action.perform(saveAs);
```

### Special Keys

```java
// Navigate with Tab and Enter using string notation
TypeOptions navigate = new TypeOptions.Builder()
    .setText("{TAB}{TAB}{ENTER}")
    .setPauseBetweenKeys(0.3)
    .build();

action.perform(navigate);
```

## Mouse Actions

### Move to Location

```java
Location targetPos = new Location(500, 300);

MouseMoveOptions moveTo = new MouseMoveOptions.Builder()
    .setLocation(targetPos)
    .setMoveTime(1.0)  // 1 second movement
    .build();

action.perform(moveTo);
```

### Custom Drag Operation

```java
// Manual drag using mouse down/move/up
StateImage dragSource = new StateImage.Builder()
    .withPattern(new Pattern("drag-source.png"))
    .build();

Location dropLocation = new Location(800, 400);

MouseDownOptions startDrag = new MouseDownOptions.Builder()
    .setPressOptions(MousePressOptions.builder()
        .setButton(MouseButton.LEFT)
        .setPauseAfterMouseDown(0.5)
        .build())
    .build();

MouseMoveOptions dragTo = new MouseMoveOptions.Builder()
    .setLocation(dropLocation)
    .setMoveTime(2.0)  // Slow drag
    .build();

MouseUpOptions endDrag = new MouseUpOptions.Builder()
    .setPressOptions(MousePressOptions.builder()
        .setButton(MouseButton.LEFT)
        .build())
    .build();

// Execute drag sequence
action.perform(startDrag, dragSource);
action.perform(dragTo);
action.perform(endDrag);
```

### Scroll

```java
ScrollOptions scrollDown = new ScrollOptions.Builder()
    .setDirection(ScrollOptions.Direction.DOWN)
    .setClicks(5)
    .setPauseBetweenScrolls(0.1)
    .build();

StateImage scrollableArea = new StateImage.Builder()
    .withPattern(new Pattern("scrollable-area.png"))
    .build();

action.perform(scrollDown, scrollableArea);
```

## Visual Actions

### Define Region

```java
// Define a region relative to a found anchor image
DefineRegionOptions defineArea = new DefineRegionOptions.Builder()
    .setOffsetX(50)
    .setOffsetY(100)
    .setWidth(300)
    .setHeight(200)
    .build();

StateImage anchorImage = new StateImage.Builder()
    .withPattern(new Pattern("anchor.png"))
    .build();

ActionResult result = action.perform(defineArea, anchorImage);
Region definedRegion = result.getDefinedRegion();
```

### Highlight

```java
// Highlight important area
HighlightOptions highlight = new HighlightOptions.Builder()
    .setDuration(3.0)
    .setColor(Color.RED)
    .setLineWidth(3)
    .build();

Region importantArea = new Region(200, 200, 400, 300);
ObjectCollection highlightCollection = new ObjectCollection.Builder()
    .withRegions(importantArea)
    .build();

action.perform(highlight, highlightCollection);
```

## Composite Actions

### Drag and Drop

```java
StateImage sourceImage = new StateImage.Builder()
    .withPattern(new Pattern("drag-source.png"))
    .build();

StateImage targetImage = new StateImage.Builder()
    .withPattern(new Pattern("drop-target.png"))
    .build();

ObjectCollection sourceCollection = new ObjectCollection.Builder()
    .withStateImages(sourceImage)
    .build();

ObjectCollection targetCollection = new ObjectCollection.Builder()
    .withStateImages(targetImage)
    .build();

DragOptions dragDrop = new DragOptions.Builder()
    .setFromOptions(new PatternFindOptions.Builder()
        .setSimilarity(0.9)
        .build())
    .setToOptions(new PatternFindOptions.Builder()
        .setSimilarity(0.9)
        .build())
    .setHoldTime(0.5)
    .build();

ActionResult result = action.perform(
    dragDrop,
    sourceCollection,
    targetCollection
);

// Get the movement performed
Movement movement = result.getMovement().orElse(null);
```

### Click Until Condition Met

```java
// Keep clicking refresh until loading spinner disappears
StateImage loadingSpinner = new StateImage.Builder()
    .withPattern(new Pattern("loading-spinner.png"))
    .build();

StateImage refreshButton = new StateImage.Builder()
    .withPattern(new Pattern("refresh-button.png"))
    .build();

ObjectCollection spinnerCollection = new ObjectCollection.Builder()
    .withStateImages(loadingSpinner)
    .build();

// Use RepeatUntilOptions with VerificationOptions
ClickOptions refreshUntilLoaded = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .setVerificationOptions(VerificationOptions.builder()
        .setEvent(VerificationOptions.Event.OBJECTS_VANISH)
        .setObjectCollection(spinnerCollection)
        .setMaxWait(30.0)  // Wait up to 30 seconds
        .build())
    .setPauseAfterEnd(2.0)  // Delay between click attempts
    .build();

// Perform the action repeatedly until verification succeeds
ActionResult result = action.perform(refreshUntilLoaded, refreshButton);
```

> **Note:** For complex repeat-until patterns, see [Conditional Actions](./09-conditional-actions.md).

## Action Chaining

For detailed information on chaining strategies and advanced patterns, see the [Action Chaining Guide](./07-action-chaining.md).

### Login Sequence

```java
StateImage usernameField = new StateImage.Builder()
    .withPattern(new Pattern("username-field.png"))
    .build();

StateImage passwordField = new StateImage.Builder()
    .withPattern(new Pattern("password-field.png"))
    .build();

StateImage loginBtn = new StateImage.Builder()
    .withPattern(new Pattern("login-button.png"))
    .build();

// Complete login flow
ActionConfig loginFlow = new PatternFindOptions.Builder()
    .setSimilarity(0.9)
    .then(new ClickOptions.Builder()
        .setNumberOfClicks(1)
        .then(new TypeOptions.Builder()
            .setText("username@example.com")
            .then(new TypeOptions.Builder()
                .setText("{TAB}")
                .then(new TypeOptions.Builder()
                    .setText("password123")
                    .then(new PatternFindOptions.Builder()
                        .setSimilarity(0.9)
                        .then(new ClickOptions.Builder()
                            .setNumberOfClicks(1)
                            .build())
                        .build())
                    .build())
                .build())
            .build())
        .build())
    .build();

ActionResult result = action.perform(
    loginFlow,
    usernameField,
    passwordField,
    loginBtn
);
```

### Form Filling

```java
StateImage nameField = new StateImage.Builder()
    .withPattern(new Pattern("name-field.png"))
    .build();

// Fill multiple form fields
ActionConfig fillForm = new PatternFindOptions.Builder()
    .setSimilarity(0.85)
    .then(new ClickOptions.Builder()
        .then(new TypeOptions.Builder()
            .setText("John Doe")
            .then(new TypeOptions.Builder()
                .setText("{TAB}")
                .then(new TypeOptions.Builder()
                    .setText("john@example.com")
                    .then(new TypeOptions.Builder()
                        .setText("{TAB}")
                        .then(new TypeOptions.Builder()
                            .setText("555-1234")
                            .build())
                        .build())
                    .build())
                .build())
            .build())
        .build())
    .build();

action.perform(fillForm, nameField);
```

## Error Handling

For more details on ActionResult components, see [ActionResult Components](./17-actionresult-components.md).

### Check Action Success

```java
ClickOptions clickOptions = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .build();

StateImage target = new StateImage.Builder()
    .withPattern(new Pattern("target.png"))
    .build();

ActionResult result = action.perform(clickOptions, target);

if (!result.isSuccess()) {
    System.err.println("Action failed: " + result.getFailureReason());
    // Handle failure
} else {
    System.out.println("Successfully clicked at: " +
        result.getFirstMatch().getLocation());
}
```

### Custom Success Criteria

```java
StateImage repeatingIcon = new StateImage.Builder()
    .withPattern(new Pattern("repeating-icon.png"))
    .build();

PatternFindOptions findMultiple = new PatternFindOptions.Builder()
    .setSimilarity(0.8)
    .setSuccessCriteria(result ->
        result.getMatchList().size() >= 3)  // Need at least 3 matches
    .build();

ActionResult result = action.perform(findMultiple, repeatingIcon);
```

### Handle Execution History

```java
// Execute complex chain
ActionConfig complexChain = new PatternFindOptions.Builder()
    .setSimilarity(0.9)
    .then(new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().setText("test").build())
    .build();

StateImage[] objects = {loginButton, icon};

ActionResult result = action.perform(complexChain, objects);

// Check each step
for (int i = 0; i < result.getExecutionHistory().size(); i++) {
    var record = result.getExecutionHistory().get(i);
    if (!record.isActionSuccess()) {
        System.err.println("Failed at step " + i);
        System.err.println("Duration: " + record.getDuration() + "s");
        break;
    }
}
```

## Complete Workflow Examples

Real-world automation scenarios combining multiple ActionConfig patterns.

### E-Commerce Checkout Flow

```java
import org.springframework.stereotype.Component;

@Component
public class CheckoutAutomation {
    @Autowired
    private Action action;

    public boolean completeCheckout(String productName, String quantity) {
        // 1. Search for product
        StateImage searchBox = new StateImage.Builder()
            .withPattern(new Pattern("search-box.png"))
            .build();

        ActionConfig searchFlow = new PatternFindOptions.Builder()
            .setSimilarity(0.85)
            .then(new ClickOptions.Builder().build())
            .then(new TypeOptions.Builder()
                .setText(productName + "{ENTER}")
                .build())
            .build();

        ActionResult searchResult = action.perform(searchFlow, searchBox);
        if (!searchResult.isSuccess()) {
            return false;
        }

        // 2. Add to cart
        StateImage addToCartBtn = new StateImage.Builder()
            .withPattern(new Pattern("add-to-cart.png"))
            .build();

        StateImage cartIcon = new StateImage.Builder()
            .withPattern(new Pattern("cart-icon-filled.png"))
            .build();

        ObjectCollection cartVerification = new ObjectCollection.Builder()
            .withStateImages(cartIcon)
            .build();

        ClickOptions addToCart = new ClickOptions.Builder()
            .setNumberOfClicks(1)
            .setVerificationOptions(VerificationOptions.builder()
                .setEvent(VerificationOptions.Event.OBJECTS_APPEAR)
                .setObjectCollection(cartVerification)
                .setMaxWait(5.0)
                .build())
            .build();

        ActionResult cartResult = action.perform(addToCart, addToCartBtn);
        if (!cartResult.isSuccess()) {
            return false;
        }

        // 3. Proceed to checkout
        StateImage checkoutBtn = new StateImage.Builder()
            .withPattern(new Pattern("checkout-button.png"))
            .build();

        action.perform(new ClickOptions.Builder().build(), checkoutBtn);

        return true;
    }
}
```

### Data Entry Automation with Validation

```java
@Component
public class FormAutomation {
    @Autowired
    private Action action;

    public ActionResult fillRegistrationForm(UserData userData) {
        StateImage firstNameField = new StateImage.Builder()
            .withPattern(new Pattern("first-name-field.png"))
            .build();

        // Build form filling chain with validation
        ActionConfig formChain = new PatternFindOptions.Builder()
            .setSimilarity(0.9)
            .setMaxWait(10.0)
            .then(new ClickOptions.Builder()
                .then(new TypeOptions.Builder()
                    .setText(userData.getFirstName())
                    .setTypeDelay(0.05)  // Human-like typing
                    .then(new TypeOptions.Builder()
                        .setText("{TAB}")
                        .then(new TypeOptions.Builder()
                            .setText(userData.getLastName())
                            .then(new TypeOptions.Builder()
                                .setText("{TAB}")
                                .then(new TypeOptions.Builder()
                                    .setText(userData.getEmail())
                                    .then(new TypeOptions.Builder()
                                        .setText("{TAB}")
                                        .then(new TypeOptions.Builder()
                                            .setText(userData.getPhone())
                                            .build())
                                        .build())
                                    .build())
                                .build())
                            .build())
                        .build())
                    .build())
                .build())
            .build();

        return action.perform(formChain, firstNameField);
    }
}
```

## Performance Considerations

Optimize ActionConfig usage for large-scale automation.

### Timing and Delays

```java
// Fast automation for trusted environments
PatternFindOptions fastFind = new PatternFindOptions.Builder()
    .setSimilarity(0.95)  // Higher similarity = faster matching
    .setMaxWait(5.0)      // Shorter timeout
    .build();

// Reliable automation for unstable environments
PatternFindOptions reliableFind = new PatternFindOptions.Builder()
    .setSimilarity(0.80)  // Lower similarity = more forgiving
    .setMaxWait(30.0)     // Longer timeout for slow systems
    .setPauseAfterEnd(0.5) // Allow UI to settle
    .build();
```

### Search Region Optimization

```java
// Poor: Search entire screen (slow)
PatternFindOptions slowFind = new PatternFindOptions.Builder()
    .setSimilarity(0.85)
    .build();

// Good: Limit search area (fast)
Region knownArea = new Region(100, 100, 400, 300);
PatternFindOptions fastFind = new PatternFindOptions.Builder()
    .setSimilarity(0.85)
    .setSearchRegions(new SearchRegions(knownArea))
    .build();

// Performance tip: Limit search to 25% of screen can be 4x faster
```

### Image Capture Settings

```java
// Development: Capture screenshots for debugging (slower)
PatternFindOptions debugMode = new PatternFindOptions.Builder()
    .setSimilarity(0.85)
    .setCaptureImage(true)  // Saves screenshot on match
    .build();

// Production: Skip captures for performance (faster)
PatternFindOptions productionMode = new PatternFindOptions.Builder()
    .setSimilarity(0.85)
    .setCaptureImage(false)  // Skip screenshot capture
    .build();
```

### Batch Operations

```java
// Define a list of buttons to click
List<StateImage> buttonList = Arrays.asList(
    new StateImage.Builder().withPattern(new Pattern("button1.png")).build(),
    new StateImage.Builder().withPattern(new Pattern("button2.png")).build(),
    new StateImage.Builder().withPattern(new Pattern("button3.png")).build()
);

// Efficient: Reuse options for multiple operations
ClickOptions standardClick = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .build();

for (StateImage button : buttonList) {
    action.perform(standardClick, button);  // Reuse same options
}

// Inefficient: Creating new options each time
for (StateImage button : buttonList) {
    ClickOptions click = new ClickOptions.Builder().build();  // Wasteful
    action.perform(click, button);
}
```

## Troubleshooting Common Issues

Solutions to frequent ActionConfig problems.

### Find Action Fails

**Problem:** Pattern not found even though it's visible on screen.

```java
// Solution 1: Lower similarity threshold
PatternFindOptions lenientFind = new PatternFindOptions.Builder()
    .setSimilarity(0.70)  // Try 0.70-0.80 instead of 0.90
    .build();

// Solution 2: Increase wait time
PatternFindOptions patientFind = new PatternFindOptions.Builder()
    .setSimilarity(0.85)
    .setMaxWait(15.0)  // Wait longer for slow UI
    .build();

// Solution 3: Use histogram finding for dynamic content
HistogramFindOptions colorFind = new HistogramFindOptions.Builder()
    .setHueBins(30)
    .setSaturationBins(32)
    .build();
```

### Click in Wrong Location

**Problem:** Click occurs but at incorrect coordinates.

```java
// Solution: Add pause before action
ClickOptions stableClick = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .setPauseBeforeBegin(0.5)  // Wait for UI to stabilize
    .setPressOptions(MousePressOptions.builder()
        .setPauseBeforeMouseDown(0.2)
        .build())
    .build();
```

### Type Action Misses Keys

**Problem:** Typed text is incomplete or incorrect.

```java
// Solution: Add delays between keystrokes
TypeOptions reliableType = new TypeOptions.Builder()
    .setText("Important Data")
    .setTypeDelay(0.1)           // Delay between characters
    .setPauseBeforeKeyDown(0.05)
    .setPauseAfterKeyUp(0.05)
    .build();
```

### Verification Times Out

**Problem:** VerificationOptions fails even though condition is met.

```java
// Solution: Increase maxWait and check event type
ClickOptions verifiedClick = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .setVerificationOptions(VerificationOptions.builder()
        .setEvent(VerificationOptions.Event.OBJECTS_APPEAR)
        .setObjectCollection(expectedObjects)
        .setMaxWait(30.0)  // Increase from default 10s
        .build())
    .setPauseAfterEnd(1.0)  // Allow time for verification
    .build();
```

### Chain Breaks Midway

**Problem:** Action chain fails partway through execution.

```java
// Define the objects for the chain
StateImage[] objects = {
    new StateImage.Builder().withPattern(new Pattern("step1.png")).build(),
    new StateImage.Builder().withPattern(new Pattern("step2.png")).build()
};

// Solution: Add error handling at each critical step
ActionConfig robustChain = new PatternFindOptions.Builder()
    .setSimilarity(0.85)
    .setMaxWait(10.0)
    .setSuccessCriteria(result -> result.isSuccess())  // Explicit check
    .then(new ClickOptions.Builder()
        .setPauseBeforeBegin(0.5)  // Stabilization
        .then(new TypeOptions.Builder()
            .setText("data")
            .setTypeDelay(0.05)
            .build())
        .build())
    .build();

// Check execution history for failures
ActionResult result = action.perform(robustChain, objects);
if (!result.isSuccess()) {
    for (var record : result.getExecutionHistory()) {
        if (!record.isActionSuccess()) {
            System.err.println("Failed at: " + record.getActionType());
            System.err.println("Reason: " + result.getFailureReason());
            break;
        }
    }
}
```

For more troubleshooting guidance, see [Troubleshooting Action Chains](./troubleshooting-chains.md).

## Best Practices

For comprehensive API documentation, refer to the [ActionConfig Reference](./05-reference.md).

### 1. Use Specific Options Classes

```java
// Good: Type-safe and clear
ClickOptions click = new ClickOptions.Builder().build();

// Avoid: Using generic ActionConfig reference
ActionConfig genericClick = new ClickOptions.Builder().build();
```

### 2. Configure Timing Appropriately

```java
// Good: Explicit timing for reliability
TypeOptions reliableType = new TypeOptions.Builder()
    .setText("important data")
    .setPauseBeforeKeyDown(0.1)
    .setPauseAfterKeyUp(0.1)
    .setTypeDelay(0.05)
    .build();
```

### 3. Use Action Chaining with then()

```java
// Good: Readable and maintainable using ActionConfig's then() method
ActionConfig workflow = new PatternFindOptions.Builder()
    .setSimilarity(0.9)
    .then(new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().setText("Hello").build())
    .then(new PatternFindOptions.Builder().setSimilarity(0.9).build())
    .then(new ClickOptions.Builder().build())
    .build();
```

For more chaining patterns, see the [Action Chaining](./07-action-chaining.md) guide.

### 4. Handle All Results

```java
// Good: Check results and handle failures
StateImage button = new StateImage.Builder()
    .withPattern(new Pattern("button.png"))
    .build();

ClickOptions options = new ClickOptions.Builder().build();

ActionResult result = action.perform(options, button);
if (result.isSuccess()) {
    System.out.println("Action succeeded");
} else {
    System.err.println("Action failed: " + result.getFailureReason());
}
```

### 5. Optimize Search Regions

```java
// Good: Define search areas to improve performance
Region headerArea = new Region(0, 0, 1920, 200);
PatternFindOptions headerFind = new PatternFindOptions.Builder()
    .setSimilarity(0.85)
    .setSearchRegions(new SearchRegions(headerArea))
    .build();
```

### 6. Reuse Configuration Objects

```java
// Good: Create once, use many times
ClickOptions standardClick = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .build();

action.perform(standardClick, button1);
action.perform(standardClick, button2);
action.perform(standardClick, button3);
```

## See Also

### Core Documentation
- [ActionConfig Overview](./01-overview.md) - Concepts and introduction
- [ActionConfig API Reference](./05-reference.md) - Complete API documentation
- [Upgrading to Latest](../migration/upgrading-to-latest.md) - Migrating from ActionOptions

### Advanced Topics
- [Action Chaining](./07-action-chaining.md) - Complex multi-step workflows and chaining patterns
- [Conditional Actions](./09-conditional-actions.md) - Repeat-until and conditional patterns

### Alternatives & Troubleshooting
- [Convenience Methods](./18-convenience-methods.md) - Simpler API for common operations
- [Troubleshooting Action Chains](./troubleshooting-chains.md) - Common issues and solutions
- [ActionResult Components](./17-actionresult-components.md) - Understanding action results
