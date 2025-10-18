# Disabled Files Notice

This project contains 4 example files with the `.disabled` extension. These files are intentionally disabled and will not compile or execute.

## Disabled Files

| File | Lines | Original Purpose |
|------|-------|-----------------|
| `BasicMovementExample.java.disabled` | 255 | Mouse movement demonstrations from documentation |
| `DragDropExample.java.disabled` | 298 | Drag and drop operation examples |
| `ScrollingExample.java.disabled` | 478 | Mouse wheel scrolling examples |
| `AdvancedMovementExample.java.disabled` | 350 | Advanced movement patterns and configurations |

**Total disabled code:** 1,381 lines (88% of project code)

## Why Are These Files Disabled?

These example files were written for **mouse movement control** (MouseMoveOptions, DragOptions, ScrollOptions), but this project now focuses on **motion detection** (MotionFindOptions) which is a completely different feature.

### The Difference

**Mouse Movement Control** (what disabled files demonstrate):
- Controls your mouse cursor position
- Performs drag and drop operations
- Scrolls windows using mouse wheel
- Creates gestures and patterns

**Motion Detection** (what this project actually does):
- Finds moving objects on screen
- Tracks objects between screen captures
- Detects when animations start/stop
- Analyzes movement patterns

## What Should You Do?

### Option 1: Use Motion Detection (Current Implementation)

If you need to detect moving objects on screen, use the active code:
- `SimpleMotionExample.java` - Working motion detection examples
- Follow the updated `README.md` for motion detection documentation

### Option 2: Implement Mouse Movement Control

If you need mouse control features, you have these options:

**A. Create a separate mouse-movement project:**
- Start a new example project for MouseMoveOptions
- Implement the features from the disabled files
- Use current Brobot 1.1.0 API

**B. Re-enable and fix the disabled files:**
- Remove `.disabled` extensions
- Update to use current Brobot 1.1.0 API
- Fix any API compatibility issues
- Test and verify functionality

## API Migration Notes

The disabled files may use outdated API signatures that no longer exist in Brobot 1.1.0:

### Known API Changes

**MouseMoveOptions:**
- ❌ OLD: `setLocation(Location)` - May not exist
- ✅ NEW: `setMoveMouseDelay(float)` - Actual API
- ❌ OLD: `setMoveTime(double)` - Doesn't exist
- ❌ OLD: `setMovementPattern(MovementPattern)` - Doesn't exist

**DragOptions:**
- ❌ OLD: `setFromOptions()` / `setToOptions()` - Don't exist
- ✅ NEW: `setMousePressOptions()` - Actual API
- ❌ OLD: `setHoldTime()` - Doesn't exist
- ✅ NEW: `setDelayBetweenMouseDownAndMove()` - Actual API
- ❌ OLD: `setDragSpeed(DragSpeed)` - Doesn't exist

**ScrollOptions:**
- ✅ Actual class name (NOT ScrollMouseWheelOptions)
- ✅ NEW: `setDirection(Direction.UP/DOWN)` - Actual API
- ❌ OLD: `setClicks(int)` - Doesn't exist
- ✅ NEW: `setScrollSteps(int)` - Actual API
- ❌ OLD: `setPauseBetweenScrolls()` - Doesn't exist

### Before Re-enabling

1. Check current Brobot API documentation
2. Update imports and class names
3. Replace deprecated methods with current API
4. Test compilation before implementing logic
5. Add proper error handling

## Project Roadmap

### Short-term
- ✅ Update README to document motion detection (DONE)
- ✅ Clarify configuration file (DONE)
- ✅ Document disabled files (DONE)

### Future Possibilities
- [ ] Remove disabled files entirely (clean slate)
- [ ] Create separate mouse-movement example project
- [ ] Expand motion detection examples
- [ ] Add test coverage for motion detection

## Questions?

If you're unsure which approach to take:

**Need motion detection?**
→ Use the current active code (SimpleMotionExample.java)

**Need mouse control?**
→ Create a new mouse-movement project with current API

**Want to help?**
→ Contribute by creating a proper mouse-movement example

## Related Documentation

- [Brobot Motion Detection Guide](../../../../docs/docs/03-core-library/guides/finding-objects/movement.md)
- [Brobot Action Config Reference](../05-reference.md)
- [Current README.md](./README.md) - Motion detection documentation

---

**Last Updated:** 2025-10-16
**Status:** Disabled files preserved for reference only
