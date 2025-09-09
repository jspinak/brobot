# Declarative Search Regions Test & Fix

## Problem Summary
ClaudeIcon should search ONLY near ClaudePrompt (using SearchRegionOnObject), but instead searches the entire screen.

## Root Cause
When StateImages with multiple patterns are processed:
1. Patterns are searched individually in FindAll.java
2. Individual patterns don't retain the SearchRegionOnObject configuration
3. Search regions are empty [] instead of being constrained near ClaudePrompt

## Solution Applied

### 1. Improved Dependency Ordering (FindPipeline.java)
- Images are now categorized as TARGETS, INDEPENDENTS, or DEPENDENTS
- TARGETS (e.g., ClaudePrompt) are searched first
- DEPENDENTS (e.g., ClaudeIcon) are searched last
- This ensures dependencies are found before their dependents need them

### 2. Enhanced Debugging
Added logging with these prefixes to track the issue:
- `[ORDERING]` - Shows how StateImages are ordered by dependencies
- `[ITERATIVE]` - Shows when each StateImage is processed and its search regions
- `[FINDALL_DEBUG]` - Shows when patterns are being searched
- `[PIPELINE_DEBUG]` - Shows dependency resolution process
- `[RESOLVER_DEBUG]` - Shows search region updates

## Expected Behavior
1. ClaudePrompt should be found first (in lower left quadrant)
2. Its location should be saved
3. ClaudeIcon's search regions should be updated to search near ClaudePrompt
4. ClaudeIcon should ONLY find matches near ClaudePrompt, not across entire screen

## Debug Output to Look For
```
[ORDERING] Final order:
[ORDERING]   [0] ClaudePrompt (TARGET)
[ORDERING]   [1] ClaudeIcon (DEPENDENT)

[ITERATIVE] Processing StateImage 1 of 2: 'ClaudePrompt'
[ITERATIVE]   Has SearchRegionOnObject: false
[ITERATIVE]   Found 1 matches for 'ClaudePrompt'

[ITERATIVE] Processing StateImage 2 of 2: 'ClaudeIcon'
[ITERATIVE]   Has SearchRegionOnObject: true
[ITERATIVE]   Depends on: Prompt.ClaudePrompt
[ITERATIVE]   Pattern search regions BEFORE find:
[ITERATIVE]     Pattern 0: 1 regions
[ITERATIVE]       - R[477,843,200,200]  // Near ClaudePrompt!
```

## Testing
Run your application and look for:
1. ClaudePrompt highlighted first
2. ClaudeIcon ONLY highlighted near ClaudePrompt
3. NO ClaudeIcon matches far from ClaudePrompt

If ClaudeIcon is still found everywhere, check the logs for:
- Are patterns showing empty search regions []?
- Is the ordering correct (ClaudePrompt before ClaudeIcon)?
- Is DynamicRegionResolver updating the regions?