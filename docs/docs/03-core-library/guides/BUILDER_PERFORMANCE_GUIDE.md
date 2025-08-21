# Builder Pattern Performance Optimization Guide

## Overview
This guide provides best practices for optimizing builder pattern usage in Brobot for maximum performance.

## Performance Considerations

### 1. Builder Instance Reuse

#### ❌ Inefficient: Creating new builder for each modification
```java
for (int i = 0; i < 1000; i++) {
    ClickOptions options = ClickOptions.builder()
        .setNumberOfClicks(i)
        .build();
    action.perform(options, collection);
}
```

#### ✅ Efficient: Reusing builder with toBuilder()
```java
ClickOptions baseOptions = ClickOptions.builder()
    .setPressOptions(commonPressOptions)
    .build();

for (int i = 0; i < 1000; i++) {
    ClickOptions options = baseOptions.toBuilder()
        .setNumberOfClicks(i)
        .build();
    action.perform(options, collection);
}
```

### 2. Lazy Initialization

#### ❌ Eager initialization of all fields
```java
public class ExpensiveOptions {
    private final ExpensiveObject expensive = new ExpensiveObject(); // Always created
    
    public static class Builder {
        private ExpensiveObject expensive = new ExpensiveObject(); // Created even if not used
    }
}
```

#### ✅ Lazy initialization
```java
public class ExpensiveOptions {
    private final Supplier<ExpensiveObject> expensiveSupplier;
    private ExpensiveObject expensive;
    
    public ExpensiveObject getExpensive() {
        if (expensive == null) {
            expensive = expensiveSupplier.get();
        }
        return expensive;
    }
    
    public static class Builder {
        private Supplier<ExpensiveObject> expensiveSupplier = ExpensiveObject::new;
        
        public Builder setExpensiveSupplier(Supplier<ExpensiveObject> supplier) {
            this.expensiveSupplier = supplier;
            return this;
        }
    }
}
```

### 3. Immutable Object Caching

#### ✅ Cache commonly used configurations
```java
public class CommonConfigurations {
    // Singleton instances for common configurations
    public static final ClickOptions SINGLE_LEFT_CLICK = ClickOptions.builder()
        .setNumberOfClicks(1)
        .build();
    
    public static final ClickOptions DOUBLE_CLICK = ClickOptions.builder()
        .setNumberOfClicks(2)
        .build();
    
    public static final ClickOptions RIGHT_CLICK = ClickOptions.builder()
        .setPressOptions(MousePressOptions.builder()
            .setButton(MouseButton.RIGHT)
            .build())
        .build();
    
    public static final PatternFindOptions QUICK_FIND = PatternFindOptions.builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .setSimilarity(0.7)
        .setCaptureImage(false)
        .build();
    
    // Thread-safe cache for dynamic configurations
    private static final Map<String, ActionConfig> configCache = 
        new ConcurrentHashMap<>();
    
    public static ActionConfig getCachedConfig(String key, Supplier<ActionConfig> builder) {
        return configCache.computeIfAbsent(key, k -> builder.get());
    }
}
```

### 4. Builder Pool Pattern

#### ✅ For high-frequency builder usage
```java
public class BuilderPool<T> {
    private final Queue<T> pool = new ConcurrentLinkedQueue<>();
    private final Supplier<T> factory;
    private final Consumer<T> resetter;
    private final int maxSize;
    
    public BuilderPool(Supplier<T> factory, Consumer<T> resetter, int maxSize) {
        this.factory = factory;
        this.resetter = resetter;
        this.maxSize = maxSize;
    }
    
    public T acquire() {
        T builder = pool.poll();
        return builder != null ? builder : factory.get();
    }
    
    public void release(T builder) {
        if (pool.size() < maxSize) {
            resetter.accept(builder);
            pool.offer(builder);
        }
    }
}

// Usage
public class ClickOptionsBuilderPool {
    private static final BuilderPool<ClickOptions.Builder> POOL = 
        new BuilderPool<>(
            ClickOptions::builder,
            builder -> {
                // Reset to defaults
                builder.setNumberOfClicks(1);
                builder.setPressOptions(null);
                builder.setVerification(null);
            },
            100 // max pool size
        );
    
    public static ClickOptions.Builder acquire() {
        return POOL.acquire();
    }
    
    public static void release(ClickOptions.Builder builder) {
        POOL.release(builder);
    }
}
```

## Memory Optimization

### 1. Null-Safe Defaults

#### ❌ Creating unnecessary objects
```java
public static class Builder {
    private VerificationOptions verification = VerificationOptions.builder().build(); // Unnecessary object
    private RepetitionOptions repetition = RepetitionOptions.builder().build(); // Unnecessary object
}
```

#### ✅ Lazy creation only when needed
```java
public static class Builder {
    private VerificationOptions.VerificationOptionsBuilder verification;
    private RepetitionOptions.RepetitionOptionsBuilder repetition;
    
    public Builder setVerification(VerificationOptions.VerificationOptionsBuilder verification) {
        this.verification = verification;
        return this;
    }
    
    public ClickOptions build() {
        return new ClickOptions(
            verification != null ? verification.build() : null,
            repetition != null ? repetition.build() : null
        );
    }
}
```

### 2. Primitive vs Object Fields

#### ✅ Use primitives where possible
```java
public static class Builder {
    private int numberOfClicks = 1; // primitive
    private double pauseAfter = 0.0; // primitive
    private boolean captureImage = false; // primitive
    
    // Instead of
    // private Integer numberOfClicks = 1; // boxed
    // private Double pauseAfter = 0.0; // boxed
    // private Boolean captureImage = false; // boxed
}
```

## JVM Optimizations

### 1. Method Inlining

#### ✅ Keep setter methods simple for JVM inlining
```java
public Builder setNumberOfClicks(int clicks) {
    this.numberOfClicks = clicks;
    return this;
}
```

#### ❌ Complex setters prevent inlining
```java
public Builder setNumberOfClicks(int clicks) {
    if (clicks < 0) {
        logger.warn("Negative clicks: " + clicks);
        this.numberOfClicks = 1;
    } else if (clicks > 10) {
        logger.warn("Too many clicks: " + clicks);
        this.numberOfClicks = 10;
    } else {
        this.numberOfClicks = clicks;
    }
    logMetrics(clicks);
    validateState();
    return this;
}
```

### 2. Final Fields

#### ✅ Mark fields final in immutable objects
```java
public final class ClickOptions {
    private final int numberOfClicks;
    private final MousePressOptions mousePressOptions;
    
    // JVM can optimize final fields better
}
```

## Benchmarking

### Performance Test Template
```java
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BuilderBenchmark {
    
    @Benchmark
    public ClickOptions directBuilder() {
        return ClickOptions.builder()
            .setNumberOfClicks(2)
            .build();
    }
    
    @Benchmark
    public ClickOptions cachedBuilder() {
        return CommonConfigurations.DOUBLE_CLICK;
    }
    
    @Benchmark
    public ClickOptions pooledBuilder() {
        ClickOptions.Builder builder = ClickOptionsBuilderPool.acquire();
        try {
            return builder.setNumberOfClicks(2).build();
        } finally {
            ClickOptionsBuilderPool.release(builder);
        }
    }
}
```

### Expected Results
```
Benchmark                          Mode  Cnt    Score   Error  Units
BuilderBenchmark.directBuilder     avgt   10  145.234 ± 3.456  ns/op
BuilderBenchmark.cachedBuilder     avgt   10    2.345 ± 0.123  ns/op
BuilderBenchmark.pooledBuilder     avgt   10   45.678 ± 2.345  ns/op
```

## Anti-Patterns to Avoid

### 1. Builder Mutation After Build
```java
// ❌ WRONG - Builder should not be used after build()
ClickOptions.Builder builder = ClickOptions.builder();
ClickOptions options1 = builder.setNumberOfClicks(1).build();
ClickOptions options2 = builder.setNumberOfClicks(2).build(); // Unsafe!
```

### 2. Excessive Builder Nesting
```java
// ❌ Too much nesting reduces readability
ClickOptions options = ClickOptions.builder()
    .setPressOptions(MousePressOptions.builder()
        .setButton(MouseButton.LEFT)
        .setPauseBeforeMouseDown(0.1)
        .setPauseAfterMouseDown(0.1)
        .setPauseBeforeMouseUp(0.1)
        .setPauseAfterMouseUp(0.1)
        .build())
    .setVerification(VerificationOptions.builder()
        .setEvent(Event.TEXT_APPEARS)
        .setText("Success")
        .setObjectCollection(ObjectCollection.builder()
            .addStateImage(StateImage.builder()
                .setName("image1")
                .build())
            .build())
        .build())
    .build();

// ✅ Better - Extract complex builders
MousePressOptions pressOptions = createPressOptions();
VerificationOptions verification = createVerification();

ClickOptions options = ClickOptions.builder()
    .setPressOptions(pressOptions)
    .setVerification(verification)
    .build();
```

### 3. Thread Safety Issues
```java
// ❌ Shared mutable builder
public class Service {
    private final ClickOptions.Builder sharedBuilder = ClickOptions.builder(); // NOT thread-safe
    
    public void performClick(int clicks) {
        ClickOptions options = sharedBuilder
            .setNumberOfClicks(clicks) // Race condition!
            .build();
    }
}

// ✅ Thread-safe approach
public class Service {
    public void performClick(int clicks) {
        ClickOptions options = ClickOptions.builder() // New builder per invocation
            .setNumberOfClicks(clicks)
            .build();
    }
}
```

## Memory Profiling

### Using JVM Flags
```bash
# Track object allocation
-XX:+PrintGC -XX:+PrintGCDetails

# Memory usage analysis
-XX:+UseG1GC -XX:MaxGCPauseMillis=200

# Heap dump on OOM
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heap.hprof
```

### Profiling Code
```java
@Test
public void profileBuilderMemoryUsage() {
    Runtime runtime = Runtime.getRuntime();
    
    // Warm-up
    for (int i = 0; i < 1000; i++) {
        ClickOptions.builder().setNumberOfClicks(i).build();
    }
    
    System.gc();
    long memBefore = runtime.totalMemory() - runtime.freeMemory();
    
    // Test
    List<ClickOptions> options = new ArrayList<>();
    for (int i = 0; i < 10000; i++) {
        options.add(ClickOptions.builder()
            .setNumberOfClicks(i % 10)
            .build());
    }
    
    long memAfter = runtime.totalMemory() - runtime.freeMemory();
    long memUsed = memAfter - memBefore;
    
    System.out.printf("Memory used: %.2f MB for %d objects (%.2f bytes/object)%n",
        memUsed / 1024.0 / 1024.0,
        options.size(),
        (double) memUsed / options.size());
}
```

## Best Practices Summary

1. **Cache immutable configurations** that are used frequently
2. **Use toBuilder()** for variations of existing configurations
3. **Avoid unnecessary object creation** in builders
4. **Use primitives** instead of boxed types where possible
5. **Keep setter methods simple** for JVM inlining
6. **Extract complex builders** to improve readability
7. **Never share mutable builders** between threads
8. **Profile memory usage** in performance-critical paths
9. **Use builder pools** for high-frequency scenarios
10. **Document performance characteristics** in JavaDoc