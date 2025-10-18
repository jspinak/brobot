---
sidebar_position: 3
title: CI/CD Testing Guide
description: Best practices for Brobot testing in continuous integration and headless environments
---

# CI/CD Testing Guide for Brobot

:::info Document Purpose & Audience
This document serves as a **historical record and framework development reference** documenting a 2024/2025 test migration analysis.

**Primary Audience:** Brobot framework developers and contributors
**Document Type:** Architectural decision record (ADR) + test migration analysis

**This document provides:**
1. **Historical Context** - Why certain tests were removed from CI/CD pipelines
2. **Decision Rationale** - CI/CD compatibility analysis methodology
3. **Framework Evolution** - Test suite modernization decisions
4. **Agent-Based Review** - Internal review process documentation

**For practical CI/CD testing guidance**, see:
- **[Testing Introduction](../testing-intro.md)** - Complete testing overview for application developers
- **[Mock Mode Guide](../mock-mode-guide.md)** - Mock mode configuration
- **[Unit Testing Guide](../unit-testing.md)** - Writing tests with BrobotTestBase
:::

## Background: Core Action Tests Analysis

### Historical Context (2024/2025 Test Migration)

### Summary
As Agent 1, I focused on Core Action Tests with emphasis on CI/CD compatibility. The key finding is that many mouse/click action tests cannot provide meaningful value in headless CI/CD environments since they don't actually perform operations in mock mode.

### Tests Removed or Disabled (Not CI/CD Compatible)

:::caution Status Update Required
The following status claims are from a historical analysis. Current verification shows:
- ✅ **Actually Removed** (6 tests): MouseDownTest, MouseUpTest, RightClickTest, ClickActionTest, MoveMouseTest, FindTest
- ⚠️ **Still Active** (3 tests): ClickTest.java (520 lines), ClickUntilTest.java (620 lines), DoubleClickTestUpdated.java
:::

**Tests that were removed:**
- `MouseDownTest.java` - Tests mouse button press without release
- `MouseUpTest.java` - Tests mouse button release
- `RightClickTest.java` - Tests right mouse button clicks
- `ClickActionTest.java` - Tests click action implementation
- `MoveMouseTest.java` - Tests mouse movement
- `FindTest.java` - Outdated API with too many incompatibilities

**Tests still active (marked @DisabledInCI):**
- `ClickTest.java` - Modern implementation with comprehensive test coverage (brobot/library/src/test/java/io/github/jspinak/brobot/action/basic/click/ClickTest.java:1)
- `ClickUntilTest.java` - Active tests for repetitive clicking (brobot/library/src/test/java/io/github/jspinak/brobot/action/composite/repeat/ClickUntilTest.java:1)
- `DoubleClickTestUpdated.java` - Integration test in library-test module

### Rationale for Removal
These tests were removed because:
1. **No Headless Value**: Mouse operations in mock mode don't actually move or click anything
2. **Already Covered**: Logic is tested through higher-level integration tests
3. **Outdated APIs**: Many tests used deprecated constructors and methods
4. **False Positives**: Tests would pass in CI/CD without actually testing functionality

### CI/CD Testing Best Practices

#### 1. Always Use BrobotTestBase
All tests MUST extend `BrobotTestBase` to ensure:
- Automatic mock mode activation
- Headless environment compatibility
- Fast execution times (0.01-0.04s per operation)
- CI/CD pipeline support

```java
package io.github.jspinak.brobot.test.examples;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;

public class MyTest extends BrobotTestBase {

    @Test
    public void testFeature() {
        // Mock mode is automatically enabled by BrobotTestBase
        // Test will work in headless CI/CD environments
    }
}
```

See brobot/library/src/test/java/io/github/jspinak/brobot/test/BrobotTestBase.java:1 for implementation details.

**Note on Headless Detection**: Brobot no longer auto-detects headless environments. For CI/CD pipelines, explicitly set:
```properties
# application-ci.properties or application-test.properties
brobot.mock=true           # Enable mock mode for testing (top-level property)
brobot.core.headless=true  # Explicitly declare headless environment (nested property)
```

:::info Property Configuration
Brobot supports both configuration styles:
- **System Properties**: `brobot.mock=true` (checked by MockModeManager)
- **Application Properties**: `brobot.core.headless=true` (mapped to BrobotProperties)

See [Mock Mode Guide](../mock-mode-guide.md) for detailed configuration.
:::

#### 2. Focus on Logic, Not Physical Operations
Good CI/CD tests should focus on:
- **Configuration validation**: Testing builder patterns and options
- **State management**: Testing state transitions and detection logic
- **Data processing**: Testing match filtering, scoring, and fusion
- **Error handling**: Testing exception cases and recovery
- **API contracts**: Testing method signatures and return values

Avoid tests that require:
- Physical mouse movement
- Actual screen captures
- GUI interaction
- Display availability

#### 3. Use Brobot's Action API (Never Mock SikuliX Directly)

:::danger ANTI-PATTERN
**DO NOT** mock SikuliX classes like `Mouse` or `Screen` directly. This violates Brobot's API design principles.

❌ **WRONG**:
```java
try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
    // This breaks Brobot abstraction
}
```
:::

**✅ CORRECT APPROACH** - Use Brobot's Action API with mock mode:
```java
package io.github.jspinak.brobot.test.examples;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MockActionTest extends BrobotTestBase {

    @Autowired
    private Action action;

    @Test
    public void testWithBrobotAPI() {
        // Mock mode is automatically enabled by BrobotTestBase
        ObjectCollection testCollection = new ObjectCollection.Builder()
            .withStrings("test-data")
            .build();

        // Action API works correctly in mock mode
        ActionResult result = action.find(testCollection);
        assertTrue(result.isSuccess());
    }
}
```

See CLAUDE.md in the repository root for complete API usage rules.
<!-- /CLAUDE.md is a root file, not included in /docs/ directory -->

#### 4. Test Categories for CI/CD

**High Value Tests** (Keep/Create):
- State management and transitions
- Pattern configuration and options
- Action result processing
- Match scoring and filtering
- Error handling and recovery
- Serialization/deserialization

**Low Value Tests** (Remove):
- Physical mouse operations
- Keyboard input simulation
- Screen capture operations
- Window focus management
- Display-dependent features

**Conditional Tests** (Platform-specific):

:::info Spring Context Required
HeadlessDetector requires Spring context (@SpringBootTest or ApplicationContextRunner). For unit tests without Spring, use `@DisabledInCI` annotation instead.
:::

```java
package io.github.jspinak.brobot.test.examples;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.config.environment.HeadlessDetector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@SpringBootTest
public class PlatformSpecificTest extends BrobotTestBase {

    @Autowired
    private HeadlessDetector headlessDetector;

    @Test
    public void testRequiringDisplay() {
        assumeFalse(System.getenv("CI") != null, "Skipping in CI");
        assumeFalse(headlessDetector.isHeadless(), "Skipping in configured headless mode");

        // Platform-specific test logic that requires display
    }
}
```

**For unit tests without Spring context**, use `@DisabledInCI` annotation:
```java
package io.github.jspinak.brobot.test.examples;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;
import org.junit.jupiter.api.Test;

@DisabledInCI // Automatically skips in CI environments
public class DisplayRequiredTest extends BrobotTestBase {

    @Test
    public void testWithRealDisplay() {
        // Test requiring actual display
    }
}
```

**Important**: Use `HeadlessDetector.isHeadless()` instead of `GraphicsEnvironment.isHeadless()` for consistent behavior. The HeadlessDetector uses the configured `brobot.core.headless` property value. See brobot/library/src/test/java/io/github/jspinak/brobot/test/DisabledInCI.java:1 and brobot/library/src/main/java/io/github/jspinak/brobot/config/environment/HeadlessDetector.java:1.

### Test Categories for Further Analysis

:::note Agent-Based Review Methodology
This document uses an "agent-based review" process where different team members (Agents 1-5) were assigned specific domain areas. This section documents planned review assignments for the test migration project.

**Status Update (2025):** Many of these tests are now active and maintained:
- ✅ `ColorClusterTest.java`, `ColorInfoTest.java`, `DynamicPixelFinderTest.java` - **ACTIVE** in library module
- ✅ `StateDetectorTest.java` and state management tests - **ACTIVE** with modern patterns
- ⚠️ Some tests remain in library-test/disabled-tests/ as archived historical tests
:::

**Domain 1: Pattern Matching & Color Analysis**
Originally assigned for mock mode compatibility review:
- `ColorClusterTest.java` - **ACTIVE** (brobot/library/src/test/java/io/github/jspinak/brobot/model/analysis/color/ColorClusterTest.java:1)
- `ColorInfoTest.java` - **ACTIVE** (brobot/library/src/test/java/io/github/jspinak/brobot/model/analysis/color/ColorInfoTest.java:1)
- `DynamicPixelFinderTest.java` - **ACTIVE** (brobot/library/src/test/java/io/github/jspinak/brobot/analysis/motion/DynamicPixelFinderTest.java:1)

**Status:** These tests successfully use mock mode and run in CI/CD environments.

**Domain 2: State Management**
Tests requiring modern API updates:
- `StateDetectorTest.java`
- `InitialStatesTest.java`
- `ProvisionalStateTest.java`
- `StateMemoryTest.java`
- `PathFinderTest.java`

**Status:** Most state management tests are now **ACTIVE** with updated State API patterns.

**Domain 3: Analysis & Utilities**
Serialization and utility tests:
- `SceneAnalysisTest.java`
- `SceneCombinationGeneratorTest.java`
- `JsonUtilsTest.java` (and related JSON utility tests)
- `PhysicalScreenTest.java`

**Status:** JSON utility tests updated for Jackson serialization patterns.

**Domain 4: Integration Tests & Archived Tests**
- **Archived tests:** 65 tests in `library-test/disabled-tests/` directory
  - These are historical tests outside normal test execution flow
  - Use special package naming (`runner.disabled`)
  - Preserved for reference but not actively maintained
- **Coverage:** JaCoCo coverage reporting configured
- **CI/CD:** GitHub Actions and local test runners operational

### Recommendations

1. **Prioritize Logic Tests**: Focus on testing business logic, not physical operations
2. **Use Mock Mode**: Leverage BrobotTestBase for all tests
3. **Document Skipped Tests**: Use `assumeFalse` with clear messages
4. **Batch Test Execution**: Run related tests together for efficiency
5. **Monitor Coverage**: Track coverage for logic, not UI operations

### Current Status

:::info Verified Test Counts (2025)
- **Total Test Files**: 454 (299 in library, 155 in library-test)
- **Tests with @DisabledInCI**: 40 in library module
- **Archived Tests**: 65 in library-test/disabled-tests/ directory
- **Actually Removed**: 6 mouse/click action tests
- **Still Active**: ClickTest.java, ClickUntilTest.java, DoubleClickTestUpdated.java
:::

**Mouse/Click Test Coverage**: Existing tests provide adequate coverage through:
- `ClickTest.java` - Modern implementation with comprehensive test coverage (520 lines, brobot/library/src/test/java/io/github/jspinak/brobot/action/basic/click/ClickTest.java:1)
- `ClickUntilTest.java` - Active tests for repetitive clicking (620 lines, brobot/library/src/test/java/io/github/jspinak/brobot/action/composite/repeat/ClickUntilTest.java:1)
- `FindAndClickTest.java` - Integration test for click configuration
- Action chain tests that verify mouse operations in context

## CI/CD Pipeline Configuration Examples

This section provides practical CI/CD pipeline configurations for running Brobot tests in various environments.

### GitHub Actions Workflow

```yaml
# .github/workflows/brobot-tests.yml
name: Brobot Test Suite

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: 'gradle'

    - name: Configure test environment
      run: |
        echo "BROBOT_MOCK=true" >> $GITHUB_ENV
        echo "CI=true" >> $GITHUB_ENV

    - name: Run library tests
      run: |
        ./gradlew :library:test --no-daemon --info
      env:
        BROBOT_MOCK: true

    - name: Run library-test integration tests
      run: |
        ./gradlew :library-test:test --no-daemon --info
      env:
        BROBOT_MOCK: true

    - name: Upload test results
      if: always()
      uses: actions/upload-artifact@v3
      with:
        name: test-results
        path: |
          library/build/reports/tests/
          library-test/build/reports/tests/

    - name: Upload coverage reports
      if: success()
      uses: codecov/codecov-action@v3
      with:
        files: library/build/reports/jacoco/test/jacocoTestReport.xml
        flags: unittests
        name: codecov-brobot
```

### Jenkins Pipeline

```groovy
// Jenkinsfile
pipeline {
    agent any

    environment {
        BROBOT_MOCK = 'true'
        CI = 'true'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Setup') {
            steps {
                sh '''
                    chmod +x gradlew
                    ./gradlew --version
                '''
            }
        }

        stage('Test - Library') {
            steps {
                sh '''
                    ./gradlew :library:test --no-daemon --info
                '''
            }
        }

        stage('Test - Integration') {
            steps {
                sh '''
                    ./gradlew :library-test:test --no-daemon --info
                '''
            }
        }

        stage('Coverage Report') {
            steps {
                sh '''
                    ./gradlew jacocoTestReport
                '''
                jacoco(
                    execPattern: '**/build/jacoco/*.exec',
                    classPattern: '**/build/classes',
                    sourcePattern: '**/src/main/java'
                )
            }
        }
    }

    post {
        always {
            junit '**/build/test-results/test/*.xml'
            archiveArtifacts artifacts: '**/build/reports/**', allowEmptyArchive: true
        }
        failure {
            emailext(
                subject: "Build Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: "Check console output at ${env.BUILD_URL}",
                to: "${env.CHANGE_AUTHOR_EMAIL}"
            )
        }
    }
}
```

### GitLab CI

```yaml
# .gitlab-ci.yml
image: openjdk:17-jdk

variables:
  BROBOT_MOCK: "true"
  CI: "true"
  GRADLE_OPTS: "-Dorg.gradle.daemon=false"

cache:
  paths:
    - .gradle/wrapper
    - .gradle/caches

stages:
  - test
  - coverage

before_script:
  - export GRADLE_USER_HOME=`pwd`/.gradle
  - chmod +x gradlew

test:library:
  stage: test
  script:
    - ./gradlew :library:test --info
  artifacts:
    when: always
    reports:
      junit: library/build/test-results/test/TEST-*.xml
    paths:
      - library/build/reports/tests/

test:integration:
  stage: test
  script:
    - ./gradlew :library-test:test --info
  artifacts:
    when: always
    reports:
      junit: library-test/build/test-results/test/TEST-*.xml
    paths:
      - library-test/build/reports/tests/

coverage:
  stage: coverage
  script:
    - ./gradlew jacocoTestReport
  coverage: '/Total.*?([0-9]{1,3})%/'
  artifacts:
    reports:
      coverage_report:
        coverage_format: cobertura
        path: library/build/reports/jacoco/test/jacocoTestReport.xml
```

### Docker Test Environment

```dockerfile
# Dockerfile.test
FROM openjdk:17-jdk-slim

# Install required packages for headless testing
RUN apt-get update && apt-get install -y \
    xvfb \
    libxrender1 \
    libxtst6 \
    libxi6 \
    && rm -rf /var/lib/apt/lists/*

# Set up virtual display for headless testing
ENV DISPLAY=:99
ENV BROBOT_MOCK=true
ENV BROBOT_CORE_HEADLESS=true

WORKDIR /app

# Copy project files
COPY . .

# Make gradlew executable
RUN chmod +x gradlew

# Run tests with virtual display
CMD ["sh", "-c", "Xvfb :99 -screen 0 1920x1080x24 & ./gradlew test --no-daemon"]
```

**Usage:**
```bash
# Build test image
docker build -f Dockerfile.test -t brobot-tests .

# Run tests in container
docker run --rm brobot-tests

# Run with volume for persistent test results
docker run --rm -v $(pwd)/build:/app/build brobot-tests
```

### Local Headless Testing Setup

**Linux/WSL:**
```bash
#!/bin/bash
# setup-headless-tests.sh

# Start virtual display
Xvfb :99 -screen 0 1920x1080x24 &
export DISPLAY=:99

# Set Brobot properties
export BROBOT_MOCK=true
export BROBOT_CORE_HEADLESS=true

# Run tests
./gradlew test --no-daemon

# Cleanup
killall Xvfb
```

**macOS:**
```bash
#!/bin/bash
# macOS doesn't need Xvfb, just set mock mode

export BROBOT_MOCK=true
export BROBOT_CORE_HEADLESS=true

./gradlew test --no-daemon
```

**Windows (PowerShell):**
```powershell
# test-headless.ps1

$env:BROBOT_MOCK="true"
$env:BROBOT_CORE_HEADLESS="true"

./gradlew.bat test --no-daemon
```

### Python Test Runner (Recommended for Large Test Suites)

For the complete 6000+ test suite without hanging:

```bash
# Run all tests with parallel execution (recommended)
python3 library/scripts/run-all-tests.py library --mode parallel --workers 8

# Run with retry for flaky tests
python3 library/scripts/run-all-tests.py library --retry-failed

# Run specific test pattern
python3 library/scripts/run-all-tests.py library --pattern "Click"
```

See TEST-EXECUTION-SOLUTION.md in the repository root for complete details on running the full test suite.
<!-- /TEST-EXECUTION-SOLUTION.md is a root file, not included in /docs/ directory -->

### Troubleshooting CI/CD Test Issues

**Common Issues:**

1. **Tests hang in CI:**
   - Use `--no-daemon` flag with Gradle
   - Set timeouts in CI configuration
   - Use Python test runner for large suites

2. **Display errors in headless mode:**
   - Ensure `BROBOT_MOCK=true` is set
   - Verify `BROBOT_CORE_HEADLESS=true` configuration
   - Use Xvfb on Linux systems if needed

3. **Out of memory errors:**
   - Increase JVM heap: `GRADLE_OPTS="-Xmx4g"`
   - Run test subsets separately
   - Use test filtering: `--tests "io.github.jspinak.brobot.action.*"`

4. **Flaky tests:**
   - Use `@DisabledInCI` annotation for display-dependent tests
   - Verify mock mode is properly enabled
   - Check for static state pollution between tests

## Related Documentation

### Core Testing Guides
- **[Testing Introduction](../testing-intro.md)** - Overview of all testing approaches and workflows
- **[Unit Testing Guide](../unit-testing.md)** - Writing unit tests with BrobotTestBase
- **[Integration Testing](../integration-testing.md)** - Integration test patterns for CI/CD
- **[Mock Mode Guide](../mock-mode-guide.md)** - Mock mode configuration and usage
- **[Mock Mode Manager](../mock-mode-manager.md)** - Centralized mock mode management
- **[Test Utilities](../test-utilities.md)** - BrobotTestBase and utility classes
- **[Mat Testing Utilities](../mat-testing-utilities.md)** - OpenCV Mat testing utilities
- **[Profile-Based Testing](../profile-based-testing.md)** - Profile-based test configuration
- **[Testing Strategy](../testing-strategy.md)** - Overall testing strategy

### Advanced Testing
- **[Enhanced Mock Testing](./enhanced-mocking.md)** - Advanced mock scenarios and failure patterns

### Framework Guidelines
<!-- Root repository files not included in /docs/ directory -->
<!-- - CLAUDE.md - Brobot API usage guidelines and testing best practices -->
<!-- - TEST-EXECUTION-SOLUTION.md - Running 6000+ tests without hanging issues -->