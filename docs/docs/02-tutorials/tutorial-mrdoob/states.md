---
sidebar_position: 4
---

# States

:::info Version Note
This tutorial was originally created for an earlier version of Brobot but has been updated for version 1.1.0 with the new annotation system.
:::

Now we can define our states using the new annotation system. Each state is automatically registered with Brobot using the `@State` annotation.

Here are our 3 state classes:

## Homepage State

```java
package com.example.mrdoob.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@State(initial = true)
@Component
@Getter
@Slf4j
public class Homepage {
    
    private final StateImage harmony;
    
    public Homepage() {
        harmony = new StateImage.Builder()
                .addPattern("harmonyIcon")
                .build();
        log.info("Homepage state created");
    }
}
```

## Harmony State

```java
package com.example.mrdoob.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@State
@Component
@Getter
@Slf4j
public class Harmony {
    
    private final StateImage about;
    
    public Harmony() {
        about = new StateImage.Builder()
                .addPattern("aboutButton")
                .build();
        log.info("Harmony state created");
    }
}
```

## About State

```java
package com.example.mrdoob.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@State
@Component
@Getter
@Slf4j
public class About {
    
    private final StateImage aboutText;
    
    public About() {
        aboutText = new StateImage.Builder()
                .addPattern("aboutText")
                .build();
        log.info("About state created");
    }
}
```

## Key Improvements with Annotations

### Before (Manual Registration)
```java
@Component
@Getter
public class Homepage {
    private State state = new State.Builder("homepage")
            .withImages(harmony)
            .build();
    
    public Homepage(AllStatesInProjectService stateService) {
        stateService.save(state);  // Manual registration
    }
}
```

### After (Automatic Registration)
```java
@State(initial = true)
@Getter
@Slf4j
public class Homepage {
    // No manual registration needed!
    // No State.Builder required!
}
```

## Benefits

1. **Automatic Registration**: The `@State` annotation handles all registration
2. **Less Boilerplate**: No need for State.Builder or constructor injection
3. **Clear Initial State**: Use `@State(initial = true)` to mark the starting state
4. **Better Focus**: State classes focus on their UI elements, not framework setup
5. **Standard Annotations**: Use `@Getter` and `@Slf4j` for clean, consistent code

## Best Practices

1. Always include these annotations:
   ```java
   @State              // Brobot state registration
   @Component          // Spring component registration
   @Getter             // Lombok getter generation
   @Slf4j              // Lombok logging
   ```

2. Mark your initial state:
   ```java
   @State(initial = true)  // This is where the automation starts
   ```

3. Use descriptive names for your StateImage fields - they should match the UI elements they represent

4. Initialize StateImages in the constructor for clarity

With our states defined, we can now create transitions between them using the `@Transition` annotation.