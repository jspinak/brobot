---
sidebar_position: 5
---

# Transitions

:::info Version Note
This tutorial has been updated for Brobot 1.2.0+ with the new @TransitionSet annotation system.
:::
 
Transitions define how to navigate between states. With the new @TransitionSet annotation system, all transitions for a state are grouped together in a single class, providing better organization and clearer intent.

## Modern Approach: Unified Transition Classes

### HomepageTransitions.java

```java
package com.example.mrdoob.transitions;

import org.springframework.stereotype.Component;
import com.example.mrdoob.states.Homepage;
import com.example.mrdoob.states.Harmony;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.FromTransition;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All transitions for the Homepage state.
 * Contains FromTransitions from other states TO Homepage,
 * and a IncomingTransition to verify arrival at Homepage.
 */
@TransitionSet(state = Homepage.class, description = "MrDoob Homepage transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class HomepageTransitions {
    
    private final Homepage homepage;
    private final Harmony harmony;
    private final Action action;
    
    /**
     * Navigate from Harmony back to Homepage.
     * This might involve clicking a home/back button.
     */
    @FromTransition(from = Harmony.class, priority = 1, description = "Return from Harmony to Homepage")
    public boolean fromHarmony() {
        log.info("Navigating from Harmony to Homepage");
        
        // Mock mode support for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        
        // Assuming there's a home button or back navigation in Harmony
        return action.click(harmony.getHomeButton()).isSuccess();
    }
    
    /**
     * Verify that we have successfully arrived at the Homepage state.
     * Checks for the presence of homepage-specific elements.
     */
    @IncomingTransition(description = "Verify arrival at Homepage", required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at Homepage");
        
        // Mock mode support for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful verification");
            return true;
        }
        
        // Check for presence of homepage-specific elements
        boolean foundHarmonyLink = action.find(homepage.getHarmony()).isSuccess();
        
        if (foundHarmonyLink) {
            log.info("Successfully confirmed Homepage is active");
            return true;
        } else {
            log.error("Failed to confirm Homepage - harmony link not found");
            return false;
        }
    }
}
```

### HarmonyTransitions.java

```java
package com.example.mrdoob.transitions;

import org.springframework.stereotype.Component;
import com.example.mrdoob.states.Homepage;
import com.example.mrdoob.states.Harmony;
import com.example.mrdoob.states.About;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.FromTransition;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All transitions for the Harmony state.
 * Contains FromTransitions from other states TO Harmony,
 * and a IncomingTransition to verify arrival at Harmony.
 */
@TransitionSet(state = Harmony.class, description = "MrDoob Harmony page transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class HarmonyTransitions {
    
    private final Homepage homepage;
    private final Harmony harmony;
    private final About about;
    private final Action action;
    
    /**
     * Navigate from Homepage to Harmony by clicking the harmony link.
     */
    @FromTransition(from = Homepage.class, priority = 1, description = "Navigate from Homepage to Harmony")
    public boolean fromHomepage() {
        log.info("Navigating from Homepage to Harmony");
        
        // Mock mode support for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        
        return action.click(homepage.getHarmony()).isSuccess();
    }
    
    /**
     * Navigate from About page back to Harmony.
     */
    @FromTransition(from = About.class, priority = 2, description = "Return from About to Harmony")
    public boolean fromAbout() {
        log.info("Navigating from About to Harmony");
        
        // Mock mode support for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        
        // Assuming there's a back button or harmony link in About page
        return action.click(about.getBackButton()).isSuccess();
    }
    
    /**
     * Verify that we have successfully arrived at the Harmony state.
     * Checks for the presence of harmony-specific elements.
     */
    @IncomingTransition(description = "Verify arrival at Harmony", required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at Harmony");
        
        // Mock mode support for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful verification");
            return true;
        }
        
        // Check for presence of harmony-specific elements
        boolean foundAboutLink = action.find(harmony.getAbout()).isSuccess();
        boolean foundCanvas = action.find(harmony.getHarmonyCanvas()).isSuccess();
        
        if (foundAboutLink || foundCanvas) {
            log.info("Successfully confirmed Harmony state is active");
            return true;
        } else {
            log.error("Failed to confirm Harmony state - expected elements not found");
            return false;
        }
    }
}
```

### AboutTransitions.java

```java
package com.example.mrdoob.transitions;

import org.springframework.stereotype.Component;
import com.example.mrdoob.states.Harmony;
import com.example.mrdoob.states.About;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.FromTransition;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All transitions for the About state.
 * Contains FromTransitions from other states TO About,
 * and a IncomingTransition to verify arrival at About.
 */
@TransitionSet(state = About.class, description = "MrDoob About page transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class AboutTransitions {
    
    private final Harmony harmony;
    private final About about;
    private final Action action;
    
    /**
     * Navigate from Harmony to About by clicking the about link.
     */
    @FromTransition(from = Harmony.class, priority = 1, description = "Navigate from Harmony to About")
    public boolean fromHarmony() {
        log.info("Navigating from Harmony to About");
        
        // Mock mode support for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        
        return action.click(harmony.getAbout()).isSuccess();
    }
    
    /**
     * Verify that we have successfully arrived at the About state.
     * Checks for the presence of about-specific elements.
     */
    @IncomingTransition(description = "Verify arrival at About", required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at About");
        
        // Mock mode support for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful verification");
            return true;
        }
        
        // Check for presence of about-specific elements
        boolean foundAboutContent = action.find(about.getAboutContent()).isSuccess();
        
        if (foundAboutContent) {
            log.info("Successfully confirmed About state is active");
            return true;
        } else {
            log.error("Failed to confirm About state - content not found");
            return false;
        }
    }
}
```

## Key Improvements with @TransitionSet

### Comparison: Old vs New

#### Before (Individual Transition Classes - v1.1.0)
```java
// Separate file for each transition
@Transition(from = Homepage.class, to = Harmony.class)
@Component
public class HomepageToHarmonyTransition {
    private final Action action;
    private final Homepage homepage;
    
    public boolean execute() {
        return action.click(homepage.getHarmony()).isSuccess();
    }
}

// Another separate file
@Transition(from = Harmony.class, to = About.class)
@Component
public class HarmonyToAboutTransition {
    private final Action action;
    private final Harmony harmony;
    
    public boolean execute() {
        return action.click(harmony.getAbout()).isSuccess();
    }
}
```

#### After (Unified Transition Classes - v1.2.0+)
```java
// All transitions for Harmony in ONE class
@TransitionSet(state = Harmony.class)
@Component
public class HarmonyTransitions {
    
    @FromTransition(from = Homepage.class, priority = 1)
    public boolean fromHomepage() {
        if (FrameworkSettings.mock) return true;
        return action.click(homepage.getHarmony()).isSuccess();
    }
    
    @FromTransition(from = About.class, priority = 2)
    public boolean fromAbout() {
        if (FrameworkSettings.mock) return true;
        return action.click(about.getBackButton()).isSuccess();
    }
    
    @IncomingTransition(required = true)
    public boolean verifyArrival() {
        if (FrameworkSettings.mock) return true;
        return action.find(harmony.getAboutLink()).isSuccess();
    }
}
```

## File Organization

Organize your transitions alongside states:

```
src/main/java/com/example/mrdoob/
├── states/
│   ├── Homepage.java
│   ├── Harmony.java
│   └── About.java
└── transitions/
    ├── HomepageTransitions.java  # All transitions for Homepage
    ├── HarmonyTransitions.java   # All transitions for Harmony
    └── AboutTransitions.java     # All transitions for About
```

## Benefits of the New Approach

1. **Better Organization**: All transitions for a state in ONE place
2. **Clearer Intent**: FromTransitions vs IncomingTransition makes navigation flow obvious
3. **Less Boilerplate**: No need for separate classes for each transition path
4. **Mock Mode Ready**: Easy to add testing support with framework settings check
5. **Natural Structure**: File organization mirrors state structure
6. **Easier Maintenance**: Adding new transitions is straightforward

## Testing Transitions

The unified structure makes testing easier:

```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class HarmonyTransitionsTest {
    
    @Autowired
    private HarmonyTransitions harmonyTransitions;
    
    @MockBean
    private Action action;
    
    @MockBean
    private Homepage homepage;
    
    @Test
    public void testFromHomepageTransition() {
        // Given
        when(action.click(any())).thenReturn(
            new ActionResult.Builder().setSuccess(true).build()
        );
        
        // When
        boolean result = harmonyTransitions.fromHomepage();
        
        // Then
        assertTrue(result);
        verify(action).click(homepage.getHarmony());
    }
    
    @Test
    public void testVerifyArrival() {
        // Given
        when(action.find(any())).thenReturn(
            new ActionResult.Builder().setSuccess(true).build()
        );
        
        // When
        boolean arrived = harmonyTransitions.verifyArrival();
        
        // Then
        assertTrue(arrived);
    }
}
```

## Best Practices

1. **Always include mock mode support** for testing environments
2. **Use descriptive method names** like `fromHomepage()`, `fromAbout()`
3. **Add comprehensive logging** for debugging
4. **Verify critical elements** in IncomingTransition methods
5. **Handle failures gracefully** with try-catch blocks where appropriate
6. **Set appropriate priorities** when multiple paths exist to the same state

## Next Steps

With states and transitions defined using the @TransitionSet system, your entire MrDoob automation is ready to run. The framework handles all registration and wiring automatically - you just focus on the automation logic!