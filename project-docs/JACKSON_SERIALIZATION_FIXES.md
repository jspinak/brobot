# Jackson Serialization Fixes for Brobot Tests

## Overview
This document provides solutions for common Jackson serialization/deserialization issues in Brobot tests, particularly with complex objects containing OpenCV Mat, BufferedImage, and SikuliX classes.

## Common Issues and Solutions

### 1. Cannot Construct Instance / No Default Constructor
**Problem**: Classes with Lombok `@Builder` but no default constructor fail to deserialize.

**Solution**: Use Jackson mix-ins or add `@JsonDeserialize` annotation with builder support.

### 2. OpenCV Mat Serialization Failures
**Problem**: `org.bytedeco.opencv.opencv_core.Mat` cannot be serialized/deserialized.

**Solution**: Register custom serializer that handles Mat as null or converts to simple representation.

### 3. BufferedImage Serialization
**Problem**: `java.awt.image.BufferedImage` causes infinite recursion or serialization errors.

**Solution**: Serialize as dimensions only, deserialize as dummy 1x1 image for tests.

### 4. SikuliX Objects (Pattern, Region, Match, Location)
**Problem**: SikuliX objects lack proper Jackson annotations and constructors.

**Solution**: Use custom serializers/deserializers that handle essential properties only.

## Implementation

### 1. Test Configuration Classes

#### BrobotJacksonTestConfig.java
Provides properly configured ObjectMapper for all tests:
- Handles OpenCV/JavaCV types
- Manages SikuliX objects
- Configures date/time serialization
- Sets appropriate features for test compatibility

#### BrobotJacksonMixins.java
Provides Jackson mix-in classes for model objects:
- Adds `@JsonDeserialize` annotations without modifying originals
- Configures builder support for Lombok classes
- Adds `@JsonIgnoreProperties(ignoreUnknown = true)` for forward compatibility

### 2. Updated BrobotTestBase
All test classes extending `BrobotTestBase` now have access to:
```java
protected ObjectMapper testObjectMapper;
```
This mapper is pre-configured with all necessary serializers and mix-ins.

### 3. Usage in Tests

```java
public class MySerializationTest extends BrobotTestBase {
    
    @Test
    public void testSerialization() throws Exception {
        // Create object with all required fields
        ActionRecord record = new ActionRecord();
        record.setTimeStamp(LocalDateTime.now()); // Required!
        record.setActionConfig(new PatternFindOptions.Builder().build());
        
        // Serialize using the configured mapper
        String json = testObjectMapper.writeValueAsString(record);
        
        // Deserialize
        ActionRecord deserialized = testObjectMapper.readValue(json, ActionRecord.class);
    }
}
```

## Key Points for Test Authors

### 1. Always Set Required Fields
Many Brobot objects expect certain fields to be non-null:
- `ActionRecord`: Set `timeStamp`, `actionConfig`, `matchList`
- `Match`: Set `region`, `score`
- `Pattern`: Use correct field names (`imgpath` not `filename`)

### 2. Use Builder Pattern Correctly
For classes with builders, ensure proper construction:
```java
PatternFindOptions options = new PatternFindOptions.Builder()
    .setSimilarity(0.8)
    .build(); // Don't forget build()!
```

### 3. Handle Complex Types
Some fields are complex objects, not primitives:
- `ActionResult.duration` is `Duration`, not `double`
- `ActionResult.text` is `Text`, not `String`
- Use appropriate getter/setter methods

### 4. Mock Mode for Tests
All tests extending `BrobotTestBase` run in mock mode:
- No actual screen capture
- No GUI interactions
- Fast execution times
- Predictable results

## Troubleshooting

### Issue: "Cannot deserialize from Object value"
**Fix**: Ensure the class has either:
- A default constructor
- `@JsonDeserialize(builder = ClassName.Builder.class)` annotation
- A registered mix-in that provides these

### Issue: "UnrecognizedPropertyException"
**Fix**: The ObjectMapper is configured with:
```java
mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
```
This should handle unknown properties gracefully.

### Issue: "No serializer found for class"
**Fix**: Check if the class needs:
- `@JsonIgnoreProperties` annotation
- Custom serializer registration
- `@JsonIgnore` on problematic fields

### Issue: Test hangs or throws AWTException
**Fix**: Ensure test extends `BrobotTestBase` which:
- Enables mock mode
- Sets headless properties
- Configures fast mock timings

## Benefits

1. **Consistent Serialization**: All tests use the same configuration
2. **Handles Complex Types**: OpenCV, SikuliX, BufferedImage all work
3. **Forward Compatible**: Unknown properties don't break tests
4. **Mock-Friendly**: Works in headless/CI environments
5. **Maintainable**: Centralized configuration in one place

## Migration Guide

For existing tests with serialization issues:

1. Extend `BrobotTestBase`
2. Use `testObjectMapper` instead of creating your own
3. Ensure all required fields are set
4. Use correct field/method names from actual classes
5. Handle complex types appropriately

## Example Test File

See `/library/src/test/java/io/github/jspinak/brobot/test/jackson/SerializationTestExample.java` for comprehensive examples of proper serialization testing.