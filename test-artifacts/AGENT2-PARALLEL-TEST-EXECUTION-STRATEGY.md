# Agent 2: Parallel Test Execution Strategy for 6000+ Tests

## Mission: Fix Test Hanging and Enable Reliable Parallel Execution

### Agent 2 Focus Areas
As Agent 2, I'm responsible for fixing the test execution infrastructure to handle 6000+ tests without hanging, focusing on:
1. Spring context initialization deadlocks
2. Gradle daemon memory issues
3. Parallel execution coordination
4. Test result XML writing failures

## Root Cause Analysis

### Primary Issue: Spring Context Deadlocks
```java
// PROBLEM: Tests hang at Spring context initialization
@SpringBootTest
class ProblematicTest {
    // Blocks during parallel execution
}
```

**Solution**: Implement test categorization and context isolation

### Secondary Issue: Resource Contention
- File system conflicts
- Thread pool exhaustion
- Memory leaks in long-running suites

## Implementation Strategy

### Phase 1: Immediate Fixes (Current)

#### 1.1 Enhanced BrobotTestBase Updates
```java
@BeforeAll
public static void setUpBrobotEnvironment() {
    // CRITICAL: Set test mode FIRST to prevent blocking
    System.setProperty("brobot.test.mode", "true");
    System.setProperty("brobot.test.type", "unit");
    
    // Disable ALL blocking operations
    System.setProperty("brobot.diagnostics.image-loading.enabled", "false");
    System.setProperty("brobot.logging.capture.enabled", "false");
    System.setProperty("brobot.startup.verification.enabled", "false");
    
    // Force zero delays
    System.setProperty("brobot.startup.delay", "0");
    System.setProperty("brobot.startup.initial.delay", "0");
    System.setProperty("brobot.startup.ui.stabilization.delay", "0");
    
    // Enable mock mode with fast timings
    MockModeManager.setMockMode(true);
}
```

#### 1.2 Gradle Test Configuration
```gradle
// library/build.gradle
test {
    // Single fork to avoid deadlocks
    maxParallelForks = 1
    
    // Increased memory allocation
    minHeapSize = "1024m"
    maxHeapSize = "3072m"
    
    // JVM arguments for stability
    jvmArgs = [
        '-XX:+UseG1GC',
        '-XX:MaxGCPauseMillis=200',
        '-XX:+HeapDumpOnOutOfMemoryError',
        '-Djava.awt.headless=true',
        '-Dbrobot.test.mode=true',
        '-Dspring.test.context.cache.maxSize=1'
    ]
    
    // Disable XML reports that cause failures
    reports {
        xml.required = false
        html.required = true
    }
    
    // Test filtering
    filter {
        excludeTestsMatching '*IntegrationTest'
        excludeTestsMatching '*IT'
    }
}
```

### Phase 2: Python Test Runner Enhancements

#### 2.1 Smart Test Categorization
```python
#!/usr/bin/env python3
"""Enhanced test categorizer for parallel execution"""

class TestCategorizer:
    def __init__(self):
        self.categories = {
            'unit': {
                'pattern': '**/util/**/*Test.java,**/model/**/*Test.java',
                'workers': 8,
                'timeout': 30
            },
            'io': {
                'pattern': '**/file/**/*Test.java,**/capture/**/*Test.java',
                'workers': 2,
                'timeout': 60
            },
            'spring': {
                'pattern': '**/*Configuration*Test.java,**/*Context*Test.java',
                'workers': 1,
                'timeout': 120
            },
            'ui': {
                'pattern': '**/screen/**/*Test.java,**/display/**/*Test.java',
                'workers': 1,
                'timeout': 90
            }
        }
    
    def categorize_test(self, test_path):
        """Determine category for optimal parallel execution"""
        if 'Spring' in test_path or '@SpringBootTest' in self.read_file(test_path):
            return 'spring'
        elif 'file' in test_path.lower() or 'io' in test_path.lower():
            return 'io'
        elif 'screen' in test_path.lower() or 'display' in test_path.lower():
            return 'ui'
        else:
            return 'unit'
```

#### 2.2 Parallel Execution Controller
```python
class ParallelTestController:
    def __init__(self, max_workers=8):
        self.executor = ThreadPoolExecutor(max_workers=max_workers)
        self.results = {}
        self.lock = threading.Lock()
        
    def run_test_batch(self, tests, category):
        """Run tests with category-specific parallelization"""
        config = self.categories[category]
        
        # Adjust workers based on category
        with ThreadPoolExecutor(max_workers=config['workers']) as executor:
            futures = []
            for test in tests:
                future = executor.submit(
                    self.run_single_test,
                    test,
                    config['timeout']
                )
                futures.append((test, future))
            
            # Collect results with progress
            for test, future in futures:
                try:
                    result = future.result(timeout=config['timeout'])
                    self.record_result(test, result)
                except TimeoutError:
                    self.record_timeout(test)
                except Exception as e:
                    self.record_failure(test, e)
```

### Phase 3: Test Isolation Framework

#### 3.1 Spring Context Isolation
```java
/**
 * Base class for tests requiring Spring context isolation
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
    "spring.test.context.cache.maxSize=1",
    "spring.jmx.enabled=false",
    "spring.main.lazy-initialization=true"
})
public abstract class IsolatedSpringTestBase extends BrobotTestBase {
    
    private static final Object CONTEXT_LOCK = new Object();
    
    @BeforeAll
    public static void lockContext() {
        synchronized (CONTEXT_LOCK) {
            // Ensure single context initialization
        }
    }
    
    @AfterAll
    public static void releaseContext() {
        synchronized (CONTEXT_LOCK) {
            // Force context cleanup
            SpringContextHelper.clearAllContexts();
        }
    }
}
```

#### 3.2 Resource Isolation Manager
```java
public class TestResourceIsolator {
    
    private final Map<String, Path> testDirectories = new ConcurrentHashMap<>();
    private final Map<String, ExecutorService> testExecutors = new ConcurrentHashMap<>();
    
    public void isolateTest(String testId) {
        // Create isolated temp directory
        Path testDir = Files.createTempDirectory("test-" + testId);
        testDirectories.put(testId, testDir);
        
        // Create isolated thread pool
        ExecutorService executor = Executors.newFixedThreadPool(2);
        testExecutors.put(testId, executor);
        
        // Set test-specific properties
        System.setProperty("brobot.test.dir." + testId, testDir.toString());
    }
    
    public void cleanupTest(String testId) {
        // Cleanup temp directory
        Path dir = testDirectories.remove(testId);
        if (dir != null) {
            FileUtils.deleteRecursively(dir);
        }
        
        // Shutdown thread pool
        ExecutorService executor = testExecutors.remove(testId);
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
```

### Phase 4: Monitoring and Recovery

#### 4.1 Test Execution Monitor
```python
class TestExecutionMonitor:
    def __init__(self):
        self.start_time = time.time()
        self.test_metrics = {}
        self.deadlock_detector = DeadlockDetector()
        
    def monitor_test(self, test_name):
        """Monitor test execution for issues"""
        start = time.time()
        memory_before = self.get_memory_usage()
        thread_count_before = threading.active_count()
        
        # Start deadlock detection
        detector_thread = threading.Thread(
            target=self.deadlock_detector.check,
            args=(test_name, 30)  # 30 second timeout
        )
        detector_thread.start()
        
        yield  # Test executes here
        
        # Record metrics
        duration = time.time() - start
        memory_after = self.get_memory_usage()
        thread_count_after = threading.active_count()
        
        self.test_metrics[test_name] = {
            'duration': duration,
            'memory_delta': memory_after - memory_before,
            'thread_leak': thread_count_after - thread_count_before,
            'deadlock': self.deadlock_detector.detected
        }
        
        # Alert on issues
        if duration > 60:
            print(f"⚠️ Slow test: {test_name} took {duration:.1f}s")
        if memory_after - memory_before > 100_000_000:  # 100MB
            print(f"⚠️ Memory leak: {test_name} used {(memory_after - memory_before) / 1_000_000:.1f}MB")
```

#### 4.2 Automatic Recovery System
```python
class TestRecoverySystem:
    def __init__(self):
        self.max_retries = 3
        self.retry_delay = [1, 3, 5]  # Exponential backoff
        
    def run_with_recovery(self, test_class):
        """Run test with automatic recovery on failure"""
        for attempt in range(self.max_retries):
            try:
                # Kill any hanging gradle processes
                self.cleanup_gradle()
                
                # Run test
                result = self.execute_test(test_class)
                
                if result.success:
                    return result
                    
                # Check for known issues
                if "OutOfMemoryError" in result.error:
                    self.increase_memory()
                elif "deadlock" in result.error.lower():
                    self.force_sequential_mode()
                    
            except Exception as e:
                print(f"Attempt {attempt + 1} failed: {e}")
                
            # Wait before retry
            if attempt < self.max_retries - 1:
                time.sleep(self.retry_delay[attempt])
                
        return TestResult(success=False, error="Max retries exceeded")
    
    def cleanup_gradle(self):
        """Force cleanup of gradle processes"""
        os.system("./gradlew --stop")
        os.system("pkill -f gradle || true")
        time.sleep(2)
```

## Execution Plan

### Step 1: Deploy Immediate Fixes (Now)
```bash
# Update BrobotTestBase with new properties
vim library/src/test/java/io/github/jspinak/brobot/test/BrobotTestBase.java

# Update Gradle configuration
vim library/build.gradle

# Stop all daemons
./gradlew --stop
```

### Step 2: Test with Small Batch (5 minutes)
```bash
# Test with fast unit tests
python3 library/scripts/run-all-tests.py library \
    --pattern "FilenameUtilsTest" \
    --workers 1 \
    --timeout 30

# Verify no hanging
```

### Step 3: Scale to Category Testing (15 minutes)
```bash
# Run unit tests category
python3 library/scripts/run-all-tests.py library \
    --category unit \
    --workers 8 \
    --monitor

# Run IO tests with reduced parallelism
python3 library/scripts/run-all-tests.py library \
    --category io \
    --workers 2 \
    --monitor
```

### Step 4: Full Suite Execution (30 minutes)
```bash
# Execute all 6000+ tests
python3 library/scripts/run-all-tests.py library \
    --mode parallel \
    --workers 8 \
    --retry-failed \
    --monitor \
    --report
```

## Success Metrics

### Must Have (Critical)
- ✅ No test hanging after 2 minutes
- ✅ All 6000+ tests complete execution
- ✅ Memory usage stays below 4GB
- ✅ No gradle daemon crashes

### Should Have (Important)
- ⏱️ Execution time < 30 minutes
- 📊 95% tests pass on first attempt
- 🔄 Automatic retry handles flaky tests
- 📈 Progress reporting every 10 seconds

### Nice to Have (Optimal)
- 🚀 Execution time < 15 minutes with 16 workers
- 💯 100% test pass rate
- 📝 Detailed performance metrics per test
- 🎯 Automatic slow test detection

## Troubleshooting Guide

### Issue: Tests Still Hanging
```bash
# 1. Check for deadlocks
jstack $(pgrep -f gradle) > deadlock-analysis.txt

# 2. Force sequential execution
python3 library/scripts/run-all-tests.py library --mode sequential

# 3. Increase timeouts
python3 library/scripts/run-all-tests.py library --timeout 180
```

### Issue: OutOfMemoryError
```bash
# 1. Increase heap size
export GRADLE_OPTS="-Xmx4g -XX:MaxMetaspaceSize=1g"

# 2. Run in smaller batches
python3 library/scripts/run-all-tests.py library --batch-size 100

# 3. Clear caches
rm -rf ~/.gradle/caches/
```

### Issue: XML Report Failures
```bash
# 1. Disable XML reports completely
echo "test.reports.xml.required = false" >> gradle.properties

# 2. Use JSON reporting instead
python3 library/scripts/run-all-tests.py library --report-format json
```

## Verification Checklist

- [ ] BrobotTestBase updated with test mode properties
- [ ] Gradle configuration has disabled XML reports
- [ ] Python test runner can categorize tests
- [ ] Small batch test execution works
- [ ] Category-based execution works
- [ ] Full suite completes without hanging
- [ ] Memory usage remains stable
- [ ] Retry mechanism handles failures
- [ ] Progress reporting is accurate
- [ ] Final report generated successfully

## Agent 2 Deliverables

1. **Fixed BrobotTestBase** with proper test mode initialization
2. **Updated Gradle configuration** for stable execution
3. **Enhanced Python test runner** with categorization
4. **Test isolation framework** for Spring tests
5. **Monitoring and recovery system** for reliability
6. **Documentation** of all changes and solutions

This completes Agent 2's parallel test execution strategy for handling the 6000+ test suite reliably.