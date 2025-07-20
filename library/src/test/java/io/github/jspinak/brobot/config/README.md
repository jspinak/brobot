# Brobot Configuration Test Suite

This test suite validates the enhanced Brobot configuration system across various deployment scenarios.

## Test Coverage

### 1. DeploymentScenarioTest
Tests different deployment environments:
- Development (IDE with relative paths)
- CI/CD (headless with fast timeouts)
- JAR deployment (external image directories)
- Mock mode (testing without real images)
- Windows/WSL/Docker environments
- Configuration profiles (development, testing, production)

### 2. ImagePathManagerTest
Tests image path resolution strategies:
- Absolute path resolution
- Relative path resolution
- Classpath resolution
- JAR extraction simulation
- Multiple path management
- Path validation
- Fallback path creation

### 3. SmartImageLoaderTest
Tests intelligent image loading:
- Loading from various sources
- Image caching functionality
- Mock mode placeholders
- Fallback strategies
- Concurrent loading
- Load history and diagnostics

### 4. ConfigurationDiagnosticsTest
Tests diagnostics and troubleshooting:
- Full diagnostic reports
- Environment detection
- Configuration validation
- Common issue detection
- Suggestion generation
- Runtime capabilities

## Running the Tests

### Run all configuration tests:
```bash
./gradlew :library:test --tests "io.github.jspinak.brobot.config.*"
```

### Run specific test class:
```bash
./gradlew :library:test --tests DeploymentScenarioTest
./gradlew :library:test --tests ImagePathManagerTest
./gradlew :library:test --tests SmartImageLoaderTest
./gradlew :library:test --tests ConfigurationDiagnosticsTest
```

### Run with specific profile:
```bash
./gradlew :library:test -Dspring.profiles.active=test
```

## Test Environment Setup

The tests automatically create temporary directories and test images as needed. No manual setup required.

### Environment Variables (Optional)
- `CI=true` - Simulate CI environment
- `DISPLAY=:0` - Set display for WSL/Linux
- `BROBOT_MOCK_MODE=true` - Force mock mode
- `BROBOT_FORCE_HEADLESS=true` - Force headless mode

## Test Scenarios Covered

1. **IDE Development**
   - Relative image paths
   - Verbose logging
   - Visual debugging

2. **CI/CD Pipeline**
   - Headless execution
   - Fast timeouts
   - No screen capture

3. **Production JAR**
   - External image directories
   - JAR resource extraction
   - Multiple retry attempts

4. **Testing/Mock**
   - Placeholder images
   - No real file I/O
   - Fast execution

5. **Cross-Platform**
   - Windows detection
   - WSL handling
   - Docker awareness
   - Mac/Linux support

## Expected Results

All tests should pass in their respective environments. The configuration system should:
- Auto-detect the environment correctly
- Apply appropriate defaults
- Handle missing resources gracefully
- Provide helpful diagnostics
- Work without external configuration files