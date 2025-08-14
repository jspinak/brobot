---
sidebar_position: 20
title: 'Runtime Delegation Migration Guide'
---

# Migrating from Runtime Delegation to Profile-Based Architecture

## Overview

This guide helps you migrate from Brobot's runtime delegation pattern (checking `FrameworkSettings.mock` at runtime) to the cleaner profile-based architecture using Spring profiles and dependency injection.

## Current State: Runtime Delegation

Many Brobot components currently use runtime checks:

```java
// Example from TypeTextWrapper.java
public boolean type(String text) {
    if (FrameworkSettings.mock) {
        return true;  // Mock execution
    }
    // Live execution
    return screen.type(text) == 1;
}

// Example from SingleClickExecutor.java
public boolean click(Location location) {
    if (FrameworkSettings.mock) {
        pause(FrameworkSettings.mockTimeClick);
        return true;
    }
    // Live click implementation
    mouse.click(location);
    return true;
}
```

## Target State: Profile-Based Architecture

Replace runtime checks with profile-specific implementations:

```java
// Interface
public interface TypeTextExecutor {
    boolean type(String text);
}

// Mock implementation
@Component
@Profile("test")
public class MockTypeTextExecutor implements TypeTextExecutor {
    public boolean type(String text) {
        pause(FrameworkSettings.mockTimeType);
        return true;
    }
}

// Live implementation
@Component
@Profile("!test")
public class LiveTypeTextExecutor implements TypeTextExecutor {
    @Autowired
    private Screen screen;
    
    public boolean type(String text) {
        return screen.type(text) == 1;
    }
}
```

## Migration Steps

### Step 1: Identify Runtime Delegation Points

Find all runtime checks in your codebase:

```bash
# Find all runtime mock checks
grep -r "if.*FrameworkSettings\.mock" --include="*.java"
grep -r "FrameworkSettings\.mock\s*\?" --include="*.java"
```

Common locations:
- `TypeTextWrapper.java`
- `SingleClickExecutor.java`
- `MouseWheel.java`
- `SceneProvider.java`
- `DragCoordinateCalculator.java`

### Step 2: Extract Interfaces

For each class with runtime delegation, extract an interface:

```java
// Before: SingleClickExecutor with runtime check
public class SingleClickExecutor {
    public boolean click(Location location) {
        if (FrameworkSettings.mock) {
            return mockClick(location);
        }
        return liveClick(location);
    }
}

// After: Extract interface
public interface ClickExecutor {
    boolean click(Location location);
}
```

### Step 3: Create Profile-Specific Implementations

Split the implementation into profile-specific classes:

```java
@Component
@Profile("test")
@Slf4j
public class MockClickExecutor implements ClickExecutor {
    
    @Override
    public boolean click(Location location) {
        log.debug("Mock click at {}", location);
        pause(FrameworkSettings.mockTimeClick);
        return true;
    }
    
    private void pause(double seconds) {
        try {
            Thread.sleep((long)(seconds * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class LiveClickExecutor implements ClickExecutor {
    
    private final Mouse mouse;
    
    @Override
    public boolean click(Location location) {
        log.debug("Live click at {}", location);
        mouse.move(location);
        mouse.click();
        return true;
    }
}
```

### Step 4: Update Dependency Injection

Replace direct instantiation with dependency injection:

```java
// Before
public class Action {
    private final SingleClickExecutor clickExecutor = new SingleClickExecutor();
    
    public boolean click(Location location) {
        return clickExecutor.click(location);
    }
}

// After
@Component
@RequiredArgsConstructor
public class Action {
    private final ClickExecutor clickExecutor;  // Injected based on profile
    
    public boolean click(Location location) {
        return clickExecutor.click(location);
    }
}
```

### Step 5: Handle Complex Cases

For classes with multiple responsibilities, use composition:

```java
// Complex class with multiple mock checks
public class ActionExecutor {
    public ActionResult execute(ActionOptions options, ObjectCollection targets) {
        if (FrameworkSettings.mock) {
            // Mock: find
            if (options instanceof FindOptions) {
                return mockFind(targets);
            }
            // Mock: click
            if (options instanceof ClickOptions) {
                return mockClick(targets);
            }
        } else {
            // Live execution with multiple branches
        }
    }
}

// Refactor using Strategy pattern
@Component
@RequiredArgsConstructor
public class ActionExecutor {
    private final Map<Class<?>, ActionStrategy> strategies;
    
    @PostConstruct
    public void init() {
        // Strategies are injected based on profile
    }
    
    public ActionResult execute(ActionOptions options, ObjectCollection targets) {
        ActionStrategy strategy = strategies.get(options.getClass());
        return strategy.execute(options, targets);
    }
}
```

### Step 6: Gradual Migration

You don't need to migrate everything at once. Use a hybrid approach:

```java
@Component
@RequiredArgsConstructor
public class HybridExecutor {
    
    @Autowired(required = false)
    private MockExecutor mockExecutor;  // Only exists in test profile
    
    @Autowired(required = false)
    private LiveExecutor liveExecutor;  // Only exists in non-test profile
    
    public boolean execute() {
        // Fallback to runtime check if needed
        if (mockExecutor != null) {
            return mockExecutor.execute();
        } else if (liveExecutor != null) {
            return liveExecutor.execute();
        } else {
            // Legacy runtime check as fallback
            return FrameworkSettings.mock ? 
                legacyMockExecute() : legacyLiveExecute();
        }
    }
}
```

## Migration Examples

### Example 1: TypeTextWrapper Migration

**Before:**
```java
public class TypeTextWrapper {
    public boolean type(String text) {
        if (FrameworkSettings.mock) return true;
        return screen.type(text) == 1;
    }
}
```

**After:**
```java
// Interface
public interface TextTyper {
    boolean type(String text);
}

// Implementations
@Component
@Profile("test")
public class MockTextTyper implements TextTyper {
    public boolean type(String text) {
        return true;
    }
}

@Component
@Profile("!test")
@RequiredArgsConstructor
public class LiveTextTyper implements TextTyper {
    private final Screen screen;
    
    public boolean type(String text) {
        return screen.type(text) == 1;
    }
}
```

### Example 2: SceneProvider Migration

**Before:**
```java
public class SceneProvider {
    public Scene getScene() {
        if (FrameworkSettings.mock) {
            return mockSceneRepository.getRandomScene();
        }
        return screenCapture.captureScreen();
    }
}
```

**After:**
```java
// Interface
public interface SceneProvider {
    Scene getScene();
}

// Implementations
@Component
@Profile("test")
@RequiredArgsConstructor
public class MockSceneProvider implements SceneProvider {
    private final MockSceneRepository repository;
    
    public Scene getScene() {
        return repository.getRandomScene();
    }
}

@Component
@Profile("!test")
@RequiredArgsConstructor
public class LiveSceneProvider implements SceneProvider {
    private final ScreenCapture screenCapture;
    
    public Scene getScene() {
        return screenCapture.captureScreen();
    }
}
```

## Testing the Migration

### Verify Profile Activation
```java
@Test
@ActiveProfiles("test")
public void verifyMockImplementation() {
    assertThat(executor).isInstanceOf(MockExecutor.class);
}

@Test
@ActiveProfiles("live")
public void verifyLiveImplementation() {
    assertThat(executor).isInstanceOf(LiveExecutor.class);
}
```

### Verify Behavior
```java
@SpringBootTest
@ActiveProfiles("test")
public class MigrationVerificationTest {
    
    @Autowired
    private ClickExecutor clickExecutor;
    
    @Test
    public void verifyNoRuntimeChecks() {
        // Should use mock implementation without runtime checks
        assertTrue(clickExecutor.click(new Location(0, 0)));
        
        // Verify no access to FrameworkSettings.mock
        // in the execution path
    }
}
```

## Benefits of Migration

### Performance
- **No runtime overhead**: Eliminates conditional checks
- **Better JVM optimization**: Static dispatch instead of dynamic
- **Reduced complexity**: Simpler execution paths

### Maintainability
- **Clear separation**: Test and production code separated
- **Single responsibility**: Each class has one purpose
- **Easier testing**: Mock and live implementations tested independently

### Type Safety
- **Compile-time verification**: Interface contracts enforced
- **Better IDE support**: Profile-aware code completion
- **Reduced bugs**: No accidental mock code in production

## Backward Compatibility

The migration can be done gradually while maintaining backward compatibility:

```java
@Configuration
public class MigrationConfiguration {
    
    @Bean
    @ConditionalOnMissingBean(ClickExecutor.class)
    public ClickExecutor fallbackClickExecutor() {
        // Provide legacy implementation if no profile-specific bean exists
        return new LegacyClickExecutor();
    }
}
```

## Summary

Migrating from runtime delegation to profile-based architecture:
1. **Improves performance** by eliminating runtime checks
2. **Enhances maintainability** through clear separation of concerns
3. **Increases reliability** with compile-time verification
4. **Simplifies testing** with isolated implementations

The migration can be done incrementally, maintaining backward compatibility while progressively modernizing the codebase.