---
sidebar_position: 10
title: 'Mock Mode Migration Guide'
description: 'Migrating existing tests to use centralized MockModeManager'
---

# Mock Mode Migration Guide

This guide helps you migrate existing Brobot tests to use the new centralized `MockModeManager` for consistent mock mode configuration.

## Overview of Changes

The introduction of `MockModeManager` simplifies mock mode management by providing:
- **Single source of truth** for mock mode status
- **Automatic synchronization** across all components
- **Simplified test configuration** via `BrobotTestBase`

## Quick Migration Steps

### Step 1: Update Test Base Class

Ensure your tests extend `BrobotTestBase`:

**Before:**
```java
// Note: BrobotProperties must be injected as a dependency
@Autowired
private BrobotProperties brobotProperties;

public class MyTest {
    @BeforeEach
    public void setup() {
        System.setProperty("brobot.mock", "true");
        // Mock mode is now configured via application.properties:
// brobot.core.mock=true;
    }
}
```

**After:**
```java
import io.github.jspinak.brobot.test.BrobotTestBase;

public class MyTest extends BrobotTestBase {
    // Mock mode is automatically configured!
}
```

### Step 2: Replace Mock Mode Checks

Update code that checks mock mode status:

**Before:**
```java
if (brobotProperties.getCore().isMock()) {
    // mock logic
}

// OR
if ("true".equals(System.getProperty("brobot.mock.mode"))) {
    // mock logic
}

// OR
if (ExecutionEnvironment.getInstance().isMockMode()) {
    // mock logic
}
```

**After:**
```java
import io.github.jspinak.brobot.config.MockModeManager;

if (brobotProperties.getCore().isMock()) {
    // mock logic
}
```

### Step 3: Replace Mock Mode Settings

Update code that sets mock mode:

**Before:**
```java
// Multiple places to set
System.setProperty("brobot.mock", "true");
System.setProperty("brobot.mock.mode", "true");
// Mock mode is now configured via application.properties:
// brobot.core.mock=true;

ExecutionEnvironment env = ExecutionEnvironment.builder()
    .mockMode(true)
    .build();
ExecutionEnvironment.setInstance(env);
```

**After:**
```java
// Single method call
MockModeManager.setMockMode(true);
```

## Common Migration Scenarios

### Scenario 1: Spring Boot Tests

**Before:**
```java
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.core.mock=true",
    "brobot.core.mock=true"
})
public class IntegrationTest {
    @BeforeEach
    public void setup() {
        // Mock mode is now configured via application.properties:
// brobot.core.mock=true;
    }
}
```

**After:**
```java
@SpringBootTest
public class IntegrationTest extends BrobotTestBase {
    // Mock mode automatically configured
    // Properties are synchronized by MockModeManager
}
```

### Scenario 2: Custom Test Setup

**Before:**
```java
public abstract class CustomTestBase {
    @BeforeEach
    public void setupMockMode() {
        System.setProperty("brobot.mock.mode", "true");
        try {
            Field mockField = BrobotProperties.class.getField("mock");
            mockField.set(null, true);
        } catch (Exception e) {
            // Handle error
        }
    }
}
```

**After:**
```java
import io.github.jspinak.brobot.test.BrobotTestBase;

public abstract class CustomTestBase extends BrobotTestBase {
    // Mock mode handled by parent class
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest(); // Ensures MockModeManager configuration
        // Your custom setup here
    }
}
```

### Scenario 3: Conditional Mock Mode

**Before:**
```java
@Test
public void testWithConditionalMock() {
    boolean useMock = System.getenv("CI") != null;
    
    if (useMock) {
        System.setProperty("brobot.mock", "true");
        // Mock mode is now configured via application.properties:
// brobot.core.mock=true;
    }
    
    // Test logic
}
```

**After:**
```java
@Test
public void testWithConditionalMock() {
    boolean useMock = System.getenv("CI") != null;
    
    if (useMock) {
        MockModeManager.setMockMode(true);
    }
    
    // Test logic
}
```

### Scenario 4: Mode Switching During Test

**Before:**
```java
@Test
public void testModeSwitch() {
    // Start with mock
    // Mock mode is now configured via application.properties:
// brobot.core.mock=true;
    // ... mock tests ...
    
    // Switch to real
    // Mock mode is now configured via application.properties:
// brobot.core.mock=false;
    System.setProperty("brobot.mock", "false");
    // ... real tests ...
    
    // Back to mock
    // Mock mode is now configured via application.properties:
// brobot.core.mock=true;
    System.setProperty("brobot.mock", "true");
}
```

**After:**
```java
@Test
public void testModeSwitch() {
    // Start with mock
    MockModeManager.setMockMode(true);
    // ... mock tests ...
    
    // Switch to real
    MockModeManager.setMockMode(false);
    try {
        // ... real tests ...
    } finally {
        // Always restore mock mode
        MockModeManager.setMockMode(true);
    }
}
```

## Debugging Migration Issues

### Verify Mock Mode State

If you're experiencing issues after migration, use the debug logging:

```java
@Test
public void debugMockState() {
    // Log complete mock mode state
    MockModeManager.logMockModeState();
    
    // This shows:
    // - System properties
    // - ExecutionEnvironment state
    // - FrameworkSettings value
}
```

### Common Issues and Solutions

#### Issue 1: Mock Mode Not Enabled

**Symptom:** Tests fail with `HeadlessException` or try to capture real screens

**Solution:**
```java
public class MyTest extends BrobotTestBase {
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest(); // MUST call parent setup!
        // Your setup here
    }
}
```

#### Issue 2: Inconsistent Mock Behavior

**Symptom:** Some components use mock mode, others don't

**Solution:**
```java
// Debug the state
MockModeManager.logMockModeState();

// Ensure using MockModeManager everywhere
if (brobotProperties.getCore().isMock()) { // Not brobotProperties.getCore().isMock()
    // mock logic
}
```

#### Issue 3: Legacy Code Interference

**Symptom:** Mock mode changes unexpectedly

**Solution:** Search for and replace all direct mock settings:
```bash
# Find direct brobotProperties.getCore().isMock() assignments
grep -r "brobotProperties.getCore().isMock()\s*=" .

# Find system property settings
grep -r "setProperty.*mock" .

# Replace with MockModeManager.setMockMode()
```

## Benefits After Migration

### Cleaner Test Code

**Before:** Multiple setup lines
**After:** Single base class extension

### Consistent Behavior

All components synchronized automatically - no more partial mock states

### Better Debugging

Single logging method shows complete mock state

### Easier Maintenance

Changes to mock configuration only need updates in one place

## Checklist

- [ ] All test classes extend `BrobotTestBase`
- [ ] Replaced all `brobotProperties.getCore().isMock()` checks with `brobotProperties.getCore().isMock()`
- [ ] Replaced all mock mode settings with `MockModeManager.setMockMode()`
- [ ] Removed redundant system property settings
- [ ] Verified tests pass in both local and CI environments
- [ ] Added `super.setupTest()` call in overridden setup methods

## Need Help?

If you encounter issues during migration:
1. Use `MockModeManager.logMockModeState()` to debug
2. Check that `BrobotTestBase` is properly extended
3. Ensure no legacy code is directly setting mock flags
4. Refer to the [Mock Mode Manager](./mock-mode-manager.md) documentation