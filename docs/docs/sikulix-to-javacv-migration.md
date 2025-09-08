# SikuliX to JavaCV Migration Strategy

## Executive Summary

Brobot can completely replace SikuliX with JavaCV, providing a more modern, efficient, and maintainable solution. The migration would reduce dependencies, improve performance, and provide better control over core functionality.

## Current Architecture Analysis

### SikuliX Dependencies (What We Use)

1. **Pattern Matching** (`org.sikuli.script.Finder`)
   - Template matching for finding images
   - ~60 files depend on this

2. **OCR** (`org.sikuli.script.OCR`)
   - Text recognition via Tesseract
   - Used for text-based automation

3. **Input Control** (`org.sikuli.script.Mouse`, `org.sikuli.script.Key`)
   - Mouse and keyboard simulation
   - Cross-platform input handling

4. **Geometric Classes** (`org.sikuli.script.Region`, `org.sikuli.script.Location`)
   - Rectangle and point abstractions
   - Used throughout for positioning

5. **Image Management** (`org.sikuli.script.ImagePath`)
   - Pattern loading and caching
   - Bundle management

## JavaCV Replacement Architecture

### Direct Replacements Available

| SikuliX Component | JavaCV Replacement | Status |
|-------------------|-------------------|---------|
| Screen Capture | ✅ JavaCVFFmpegProvider | **Implemented & Tested** |
| Pattern Matching | JavaCVPatternMatcher | **Proof of Concept Ready** |
| OCR | JavaCVOCRService | **Proof of Concept Ready** |
| Mouse/Keyboard | Java Robot API | **Already in RobotProvider** |
| Region/Location | Java AWT Rectangle/Point | **Simple replacement** |
| Image Loading | Java ImageIO + JavaCV | **Straightforward** |

## Benefits of Migration

### 1. Performance Improvements
- **Direct OpenCV calls** - No abstraction overhead
- **Native memory management** - Better control over resources
- **Optimized algorithms** - Can choose specific OpenCV methods
- **Parallel processing** - JavaCV supports GPU acceleration

### 2. Reduced Dependencies
- **Remove 60MB+ SikuliX JAR**
- **Single ecosystem** - JavaCV for both capture and matching
- **Fewer version conflicts** - One OpenCV version

### 3. Better Maintenance
- **Active development** - JavaCV is actively maintained
- **Modern API** - Better documentation and examples
- **Direct debugging** - No black box abstractions

### 4. Proven Success
- **FFmpeg capture** - Already achieving 100% match with Windows
- **Production ready** - JavaCV used in many production systems

## Implementation Plan

### Phase 1: Parallel Implementation (Low Risk)
**Duration: 2-4 weeks**

1. **Complete JavaCV implementations**
   ```java
   @Component
   public class AdaptivePatternMatcher {
       @Value("${brobot.matcher:SIKULIX}")
       private String provider;
       
       public List<Match> find(BufferedImage screen, Pattern pattern) {
           return "JAVACV".equals(provider) 
               ? javaCVMatcher.find(screen, pattern)
               : sikuliMatcher.find(screen, pattern);
       }
   }
   ```

2. **Add configuration switches**
   ```properties
   # application.properties
   brobot.matcher.provider=JAVACV  # or SIKULIX
   brobot.ocr.provider=JAVACV      # or SIKULIX
   brobot.input.provider=ROBOT     # or SIKULIX
   ```

3. **Compatibility adapters**
   - Convert between SikuliX and JavaCV result formats
   - Maintain existing APIs

### Phase 2: Testing & Validation (Critical)
**Duration: 2-3 weeks**

1. **Performance benchmarks**
   ```java
   @Test
   public void compareMatchingPerformance() {
       // Test 1000 pattern matches
       long sikulixTime = timeMatching(sikulixMatcher);
       long javacvTime = timeMatching(javaCVMatcher);
       
       // Expect JavaCV to be faster
       assertTrue(javacvTime < sikulixTime);
   }
   ```

2. **Accuracy comparison**
   - Match score differences
   - False positive/negative rates
   - OCR accuracy metrics

3. **Memory profiling**
   - Heap usage comparison
   - Native memory tracking
   - Leak detection

### Phase 3: Gradual Migration (Safe)
**Duration: 4-6 weeks**

1. **Module by module conversion**
   - Start with non-critical modules
   - Run both in parallel initially
   - Switch based on success metrics

2. **User opt-in**
   ```properties
   # Users can choose
   brobot.experimental.javacv=true
   ```

3. **Fallback mechanism**
   ```java
   try {
       return javaCVMatcher.find(screen, pattern);
   } catch (Exception e) {
       log.warn("JavaCV failed, falling back to SikuliX", e);
       return sikulixMatcher.find(screen, pattern);
   }
   ```

### Phase 4: Complete Migration (Final)
**Duration: 2-3 weeks**

1. **Remove SikuliX dependencies**
2. **Update documentation**
3. **Migration tools for users**

## Code Examples

### Pattern Matching Comparison

#### Current (SikuliX)
```java
Finder finder = new Finder(screenImage);
finder.findAll(pattern.sikuli());
while (finder.hasNext()) {
    Match match = finder.next();
    // Process match
}
finder.destroy();
```

#### New (JavaCV)
```java
JavaCVPatternMatcher matcher = new JavaCVPatternMatcher();
List<MatchResult> matches = matcher.findPatterns(
    screenImage, pattern, options);
for (MatchResult match : matches) {
    // Process match
}
// No explicit cleanup needed - try-with-resources in implementation
```

### OCR Comparison

#### Current (SikuliX)
```java
List<Match> words = OCR.readWords(image);
```

#### New (JavaCV)
```java
JavaCVOCRService ocr = new JavaCVOCRService();
List<WordMatch> words = ocr.findWords(image);
```

## Risk Mitigation

### Compatibility Risks
- **Solution**: Adapter pattern to maintain API compatibility
- **Testing**: Comprehensive test suite comparing results

### Performance Risks
- **Solution**: Benchmark before full migration
- **Fallback**: Keep SikuliX as fallback option

### User Impact
- **Solution**: Gradual rollout with opt-in
- **Documentation**: Clear migration guides

## Success Metrics

1. **Performance**
   - ✅ Pattern matching 20% faster
   - ✅ Memory usage reduced by 30%
   - ✅ Startup time improved by 2 seconds

2. **Reliability**
   - ✅ Same or better match accuracy
   - ✅ No increase in false positives
   - ✅ OCR accuracy maintained

3. **Adoption**
   - ✅ 50% of users on JavaCV within 3 months
   - ✅ No critical bugs reported
   - ✅ Positive performance feedback

## Recommendation

**Proceed with Phase 1 immediately**. The proof of concepts show JavaCV is viable and the benefits are substantial:

1. **Already proven** - FFmpeg capture shows JavaCV works perfectly
2. **Low risk** - Parallel implementation allows easy rollback
3. **High reward** - Significant performance and maintenance benefits
4. **Future-proof** - JavaCV is actively developed, SikuliX development has slowed

## Next Steps

1. **Complete JavaCVPatternMatcher** - Add remaining features
2. **Integration tests** - Ensure drop-in compatibility
3. **Performance benchmarks** - Quantify improvements
4. **User documentation** - Prepare migration guides
5. **Beta program** - Get early user feedback

## Conclusion

Moving from SikuliX to JavaCV is not just feasible but highly recommended. The successful implementation of JavaCV FFmpeg capture (100% match with Windows) proves the approach works. A complete migration would modernize Brobot's architecture, improve performance, and reduce technical debt.

The phased approach ensures minimal risk while maximizing the benefits of a modern, efficient pattern matching and OCR solution.