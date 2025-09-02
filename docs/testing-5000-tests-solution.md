# Running 5000+ Tests in Brobot Library

## Problem Statement
The Brobot library contains **5,296 tests** across 285 test files, but running them all together with `./gradlew library:test` causes timeouts and failures. Individual packages run successfully, but the full suite hangs indefinitely.

## Root Causes

1. **Resource Exhaustion**: Loading 5000+ test classes simultaneously exhausts JVM memory and resources
2. **Spring Context Conflicts**: Tests expecting Spring Boot context in a plain library module
3. **SikuliX Initialization**: Attempting to initialize GUI components in headless environments
4. **Missing Mock Configuration**: Tests not properly configured for mock mode execution
5. **Test Interdependencies**: Static state and singletons causing conflicts between tests

## Solution Architecture

### 1. Enhanced Gradle Configuration
Updated `build.gradle` with proper forking and memory settings:

```gradle
test {
    maxHeapSize = '4g'
    minHeapSize = '1g'
    
    // Fork new JVM every 100 tests to prevent resource buildup
    forkEvery = 100
    maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1
    
    // Timeout to prevent hanging
    timeout = Duration.ofMinutes(10)
    
    // System properties for headless execution
    systemProperties = [
        'java.awt.headless': 'true',
        'brobot.mock': 'true',
        'sikuli.Debug': '0'
    ]
}
```

### 2. Test Suite Organization
Created `test-suites.gradle` to run tests in manageable batches:

- **unitTests**: Model and utility tests (fast, no dependencies)
- **integrationTests**: Action and navigation tests (slower, need mocking)
- **analysisTests**: Analysis and tool tests
- **aspectTests**: Aspect and logging tests

### 3. Improved BrobotTestBase
Enhanced base test class with proper mock initialization:

```java
@BeforeAll
public static void setUpBrobotEnvironment() {
    System.setProperty("java.awt.headless", "true");
    System.setProperty("brobot.mock", "true");
    System.setProperty("sikuli.Debug", "0");
    // Mock timings for fast execution
    System.setProperty("brobot.mock.time.find.first", "0.01");
}
```

### 4. Test Separation Strategy
- Moved `@SpringBootTest` tests to `library-test` module
- Library module contains only unit tests without Spring dependencies
- Integration tests requiring Spring context belong in library-test

### 5. Batch Test Runner Script
Created `run-all-tests.sh` to execute tests package by package:

```bash
# Runs each package with 60-second timeout
# Tracks success/failure for each package
# Provides detailed summary at completion
```

## Test Distribution

| Package Category | Test Count | Execution Strategy |
|-----------------|------------|-------------------|
| Model Classes | 1,382 | Fast, parallel execution |
| Utilities | 814 | Fast, parallel execution |
| Actions | 1,026 | Sequential, forked JVMs |
| Navigation | 478 | Sequential, mocked |
| Tools/Analysis | 892 | Parallel, isolated |
| Integration | 704 | Sequential, heavy mocking |

## Running the Tests

### Run All Tests (Recommended)
```bash
./library/run-all-tests.sh
```

### Run Specific Package
```bash
./gradlew library:test --tests "io.github.jspinak.brobot.model.*"
```

### Run Test Suites
```bash
./gradlew library:unitTests       # Fast unit tests
./gradlew library:integrationTests # Slower integration tests
./gradlew library:testAll         # All suites sequentially
```

### Count Tests by Package
```bash
./gradlew library:countTests
```

## Results

- **Before**: Only 25-443 tests running, timeouts, Spring conflicts
- **After**: All 5,296 tests discoverable and runnable in batches
- **Coverage**: Improved from <1% to measurable coverage per package
- **Execution Time**: ~5 minutes for full suite in batches vs. timeout

## Key Learnings

1. **Library modules shouldn't contain Spring Boot tests** - these belong in separate test modules
2. **Large test suites need proper resource management** - forking and memory limits are critical
3. **Mock mode must be initialized early** - system properties before any component creation
4. **Batch execution prevents resource exhaustion** - running 5000+ tests needs segmentation
5. **Test isolation is critical** - static state and singletons cause cascading failures

## Maintenance

- Keep Spring Boot tests in `library-test` module
- Ensure all tests extend `BrobotTestBase` for proper mocking
- Use `@Tag` annotations for test categorization
- Monitor test execution times and adjust fork settings as needed
- Run `countTests` periodically to track test growth