# Brobot Test Patterns Guide
*Established by Agent 2 - Tools & History Team*

## Quick Reference Test Patterns

### Pattern 1: Simple Unit Test (No Spring)
```java
public class MyComponentTest extends SimpleTestBase {
    
    @Mock
    private DependencyA mockDependency;
    
    private MyComponent component;
    
    @BeforeEach
    public void setup() {
        super.setupMockMode();
        MockitoAnnotations.openMocks(this);
        component = new MyComponent(mockDependency);
    }
    
    @Test
    public void testFeature() {
        // Given
        when(mockDependency.method()).thenReturn(expectedValue);
        
        // When
        Result result = component.doSomething();
        
        // Then
        assertNotNull(result);
        verify(mockDependency).method();
    }
}
```

### Pattern 2: Integration Test (With Spring)
```java
@SpringBootTest
@ActiveProfiles("test")
public class MyIntegrationTest extends BrobotIntegrationTestBase {
    
    @Autowired
    private MyService service;
    
    @Test
    public void testIntegration() {
        // Test with full Spring context
    }
}
```

### Pattern 3: Testing with Temporary Files
```java
public class FileOperationTest extends SimpleTestBase {
    
    @TempDir
    Path tempDir;
    
    @Test
    public void testFileOperation() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.json");
        Files.writeString(testFile, "{}");
        
        // Act
        MyFileProcessor processor = new MyFileProcessor();
        processor.process(testFile.toString());
        
        // Assert
        assertTrue(Files.exists(testFile));
    }
}
```

### Pattern 4: Testing Action Results
```java
@Test
public void testActionResult() {
    // Create ActionResult
    ActionResult result = new ActionResult();
    result.setSuccess(true);
    result.setDuration(Duration.ofMillis(100));
    
    // Add matches
    result.add(new Match.Builder()
        .setRegion(new Region(10, 10, 50, 50))
        .setSimScore(0.95)  // Note: Use setSimScore, not setScore
        .build());
    
    // Assertions
    assertTrue(result.isSuccess());
    assertEquals(1, result.getMatchList().size());
}
```

### Pattern 5: Testing with Mock Builders
```java
@Test
public void testWithMockData() {
    // Create test data using builders
    ActionRecord record = new ActionRecord.Builder()
        .setActionConfig(new PatternFindOptions.Builder()
            .setSimilarity(0.9)
            .build())
        .setActionSuccess(true)
        .setDuration(100L)
        .setMatchList(Arrays.asList(mockMatch))
        .build();
    // Note: timestamp is auto-set by builder
    
    // Use in tests
    assertNotNull(record.getTimeStamp());
}
```

### Pattern 6: Testing Collections and Batches
```java
@Test
public void testBatchOperation() {
    // Arrange
    List<Item> batch = IntStream.range(0, 100)
        .mapToObj(i -> createTestItem(i))
        .collect(Collectors.toList());
    
    // Act
    BatchProcessor processor = new BatchProcessor();
    BatchResult result = processor.processBatch(batch);
    
    // Assert
    assertEquals(100, result.getProcessedCount());
    assertTrue(result.getDuration() < Duration.ofSeconds(5));
}
```

### Pattern 7: Async Testing
```java
@Test
public void testAsyncOperation() {
    // Arrange
    CompletableFuture<String> future = service.asyncOperation();
    
    // Act & Assert
    assertTimeout(Duration.ofSeconds(2), () -> {
        String result = future.get();
        assertEquals("expected", result);
    });
}
```

### Pattern 8: Exception Testing
```java
@Test
public void testExceptionHandling() {
    // Arrange
    InvalidInput input = new InvalidInput();
    
    // Act & Assert
    assertThrows(ValidationException.class, () -> {
        validator.validate(input);
    });
}
```

## Common Pitfalls & Solutions

### Pitfall 1: Wrong Match Score Method
❌ **Wrong**: `new Match.Builder().setScore(0.95)`
✅ **Correct**: `new Match.Builder().setSimScore(0.95)`

### Pitfall 2: Manual Timestamp Setting
❌ **Wrong**: `record.setTimeStamp(LocalDateTime.now())`
✅ **Correct**: Let the builder handle it automatically

### Pitfall 3: Using ActionOptions
❌ **Wrong**: `new ActionOptions()`
✅ **Correct**: Use specific options like `PatternFindOptions`, `ClickOptions`, etc.

### Pitfall 4: Console Logging Level
❌ **Wrong**: `brobot.console.actions.level=OFF`
✅ **Correct**: `brobot.console.actions.level=QUIET`

### Pitfall 5: Direct Spring Injection in Tests
❌ **Wrong**: 
```java
@Autowired
private MyService service; // In non-Spring test
```
✅ **Correct**:
```java
private MyService service = new MyService();
```

## Test Execution Commands

### Run Specific Test Class
```bash
./gradlew :library-test:test --tests "MyTest"
```

### Run Tests Matching Pattern
```bash
./gradlew :library-test:test --tests "*History*"
```

### Run with Detailed Output
```bash
./gradlew :library-test:test --info
```

### Force Re-run (Skip Cache)
```bash
./gradlew :library-test:test --rerun-tasks
```

### Run with Coverage Report
```bash
./gradlew :library-test:test :library-test:jacocoTestReport
```

## Test Data Helpers

### Create Mock Match
```java
private Match createMockMatch(int x, int y, int w, int h) {
    return new Match.Builder()
        .setRegion(new Region(x, y, w, h))
        .setSimScore(0.95)
        .build();
}
```

### Create Test ActionHistory
```java
private ActionHistory createTestHistory(int recordCount) {
    ActionHistory history = new ActionHistory();
    history.setTimesSearched(recordCount * 2);
    history.setTimesFound(recordCount);
    
    List<ActionRecord> records = IntStream.range(0, recordCount)
        .mapToObj(i -> new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder().build())
            .setActionSuccess(true)
            .setDuration((long)(100 + i * 10))
            .setMatchList(new ArrayList<>())
            .build())
        .collect(Collectors.toList());
    
    history.setSnapshots(records);
    return history;
}
```

## Debugging Failed Tests

### Check Mock Mode
```java
@BeforeEach
public void verifyMockMode() {
    System.out.println("Mock mode: " + System.getProperty("brobot.mock.mode"));
}
```

### Capture Mock Interactions
```java
ArgumentCaptor<ObjectCollection> captor = ArgumentCaptor.forClass(ObjectCollection.class);
verify(action).perform(captor.capture());
ObjectCollection captured = captor.getValue();
// Examine captured value
```

### Verbose Test Output
```java
@Test
public void debugTest() {
    // Enable detailed logging for this test
    Logger logger = LoggerFactory.getLogger(MyClass.class);
    Level originalLevel = logger.getLevel();
    logger.setLevel(Level.DEBUG);
    
    try {
        // Test code
    } finally {
        logger.setLevel(originalLevel);
    }
}
```

---
*This guide contains patterns established during the test implementation for Brobot library.*
*Use these patterns to maintain consistency across all test implementations.*