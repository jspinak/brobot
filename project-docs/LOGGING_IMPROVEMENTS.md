# Brobot Find Operation Logging Improvements

## Overview
The logging for Find operations has been significantly improved to be more concise and reduce repetitive information. This addresses the issue of verbose, redundant logging when searching for patterns multiple times during a find operation.

## Key Improvements

### 1. Deduplication of Pattern Information
- **Before**: Pattern details (name, size, similarity) were logged every time a pattern was searched
- **After**: Pattern details are logged only once per session, subsequent searches show minimal info

### 2. Session-Based Tracking
- Find operations are now tracked as "sessions" with start/end summaries
- Session summary shows: total patterns searched, total attempts, duration, and match count

### 3. Concise Output Formats
- **Pattern info**: `[SEARCH] pattern-name (293x83) sim=0.90 scene=1536x864`
- **Constrained search**: `[SEARCH] pattern-name (293x83) sim=0.90 in [0,432 768x432]`
- **Screen capture**: `[CAPTURE] 768x432 region at (0,432) -> 768x432 RGB`
- **Repeats**: `[RE-SEARCH #2] pattern-name` 
- **Similarity change**: `[RE-SEARCH #2] pattern-name (sim 0.90→0.85)`
- **Results**: `[FOUND] pattern-name: 3 matches, best=0.95`
- **Session**: `[FIND SUCCESS] 3 patterns searched 6 times in 500ms, 2 matches found`

### 4. Eliminated Redundancies
- Search region is now logged only once (not as both OPTIMIZED and CONSTRAINED)
- When searching in a constrained region, scene size is omitted (implied by region)
- Screen capture logging condensed from 4 lines to 1 line
- Repeated searches show count and only notable changes

### 5. Intelligent Verbosity Levels
- **QUIET**: Minimal output (✓/✗ symbols only)
- **NORMAL**: Concise deduplicated logging (default)
- **VERBOSE**: Full details with additional diagnostics

## Implementation

### New Classes
1. **ConciseFindLogger** (`io.github.jspinak.brobot.logging.ConciseFindLogger`)
   - Manages session-based tracking
   - Implements deduplication logic
   - Provides concise formatting

### Modified Classes
1. **ScenePatternMatcher** - Integrated concise logger
2. **FindPipeline** - Added session management

## Usage Example

### Before (50+ lines of repetitive output):
```
11:32:16 DEBUG [OBSERVE] [REGION] 'claude-prompt-1': fixed @ R[0.432.768.432]
11:32:16 DEBUG [OBSERVE] [SIMILARITY] Updated 'claude-prompt-1' to 0,90
11:32:16 DEBUG [OBSERVE] [SEARCH] Pattern: 'claude-prompt-1' (293x83) | Similarity: 0,90 | Scene: 1536x864
11:32:16 DEBUG [OBSERVE] [OPTIMIZED SEARCH] Constraining search to region: R[0.432.768.432]
11:32:16 DEBUG [OBSERVE] [CONSTRAINED] Searching in sub-region: 0,432 768x432
11:32:16 DEBUG [OBSERVE] [FINDER] Searching with similarity threshold: 0.9
11:32:16 DEBUG [OBSERVE]     [IMAGE ANALYSIS]
11:32:16 DEBUG [OBSERVE]       Pattern: 293x83 type=Type6 bytes=94KB
11:32:16 DEBUG [OBSERVE]       Pattern content: 0,0% black, 0,0% white, avg RGB=(24,24,24)
11:32:16 DEBUG [OBSERVE]       Scene: 1536x864 type=RGB bytes=5MB
11:32:16 DEBUG [OBSERVE]       Scene content: 0,0% black, 0,0% white, avg RGB=(60,60,60)
11:32:16 DEBUG [OBSERVE]     [SIMILARITY DEBUG] Testing pattern 'claude-prompt-1'
11:32:16 DEBUG [OBSERVE]     [SIMILARITY DEBUG] Original MinSimilarity: 0.9
11:32:16 DEBUG [OBSERVE]     [SIMILARITY DEBUG] Pattern type before conversion: Type6
11:32:16 DEBUG [OBSERVE]     [SIMILARITY DEBUG] Scene type: Type1
11:32:17 DEBUG [OBSERVE]     [SIMILARITY ANALYSIS]
11:32:17 DEBUG [OBSERVE]       Threshold 0,4: FOUND with score 0,500
... (repeated 3x for each pattern, multiple times)
```

### After (~10 lines of concise output):
```
[SEARCH] claude-prompt-1 (293x83) sim=0.90 in [0,432 768x432]
  [NO MATCH] claude-prompt-1
[SEARCH] claude-prompt-2 (205x81) sim=0.90 in [0,432 768x432]
  [NO MATCH] claude-prompt-2 - exists at lower similarity (0.453)
[SEARCH] claude-prompt-3 (195x80) sim=0.90 in [0,432 768x432]
  [NO MATCH] claude-prompt-3
[RE-SEARCH #2] claude-prompt-1
[RE-SEARCH #2] claude-prompt-2
[RE-SEARCH #2] claude-prompt-3
[FIND FAILED] 3 patterns searched 6 times in 5600ms, 0 matches found
```

In VERBOSE mode, additional diagnostics appear only when needed:
```
[SEARCH REGION] Constrained to: 0,432 768x432
[IMG] Pattern 195x80, Scene 1536x864, Types: Type6 vs RGB  
[WARNING] Pattern is BLACK
[SIM] Found at 0.4 with score 0.411
```

## Benefits

1. **Reduced Log Volume**: ~80% reduction in log lines for typical find operations
2. **Better Readability**: Important information is easier to spot
3. **Performance**: Less string concatenation and I/O operations
4. **Debugging**: Still provides all necessary information for troubleshooting
5. **Backward Compatible**: Old logging still available via DiagnosticLogger

## Configuration

The logging behavior can be configured via application properties:

```properties
# Set logging verbosity level
brobot.logging.verbosity=NORMAL  # Options: QUIET, NORMAL, VERBOSE

# Enable/disable specific features
brobot.logging.low-score-threshold=0.50
brobot.logging.max-detailed-matches=10
```

## Migration Notes

- The new `ConciseFindLogger` is optional and autowired with `required=false`
- If not present, the system falls back to the existing `DiagnosticLogger`
- No changes required to existing code to benefit from improvements