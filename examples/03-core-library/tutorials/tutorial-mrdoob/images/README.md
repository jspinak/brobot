# Images Directory - Mr.doob Tutorial

This directory contains screenshot images for pattern matching in the Mr.doob web automation tutorial.

## Required Images

This tutorial requires **3 image files** to navigate through the Mr.doob website:

### 1. harmonyIcon.png
**Purpose**: Homepage state verification and navigation
**Location**: Mr.doob homepage (https://mrdoob.com)
**Element**: The clickable "Harmony" icon/button

**What to capture**:
- The Harmony icon as it appears on the homepage
- Include the icon with a small margin (5-10 pixels)
- Typical size: 100x100 to 200x200 pixels

**Used by**: `Homepage` state class to identify and click the harmony icon

---

### 2. aboutButton.png
**Purpose**: Navigation from Harmony to About page
**Location**: Harmony drawing application page
**Element**: The "About" button or link

**What to capture**:
- The About button/link from the Harmony page
- Crop tightly around the clickable element
- Typical size: 80x30 to 150x50 pixels

**Used by**: `Harmony` state class to navigate to the About page

---

### 3. aboutText.png
**Purpose**: About page verification
**Location**: About page on Mr.doob website
**Element**: Distinctive text that confirms you're on the About page

**What to capture**:
- Unique text or header from the About page
- Choose text that won't appear on other pages
- Typical size: 150x40 to 300x80 pixels

**Used by**: `About` state class to verify successful navigation

---

## How to Capture Images

### Step-by-Step Process:

1. **Open the Website**:
   ```
   Navigate to https://mrdoob.com in your web browser
   ```

2. **Capture Homepage (harmonyIcon.png)**:
   - Locate the Harmony icon on the homepage
   - Use a screenshot tool (see options below)
   - Crop to just the icon with a small margin
   - Save as `harmonyIcon.png` in this directory

3. **Capture Harmony Page (aboutButton.png)**:
   - Click on the Harmony icon to enter the drawing application
   - Find the About button/link
   - Capture just that button
   - Save as `aboutButton.png`

4. **Capture About Page (aboutText.png)**:
   - Click the About button
   - Find distinctive text on the About page
   - Capture that text element
   - Save as `aboutText.png`

### Screenshot Tools:

**Windows:**
- Snipping Tool (Windows + Shift + S)
- Snip & Sketch
- ShareX (free, advanced)

**macOS:**
- Command + Shift + 4 (built-in)
- Command + Shift + 5 (advanced options)

**Linux:**
- gnome-screenshot
- Flameshot
- Spectacle (KDE)

**Cross-Platform:**
- Browser extensions (e.g., "Awesome Screenshot")
- Greenshot (free, open source)

---

## Image Requirements

### Format and Quality

| Property | Requirement | Why |
|----------|-------------|-----|
| **Format** | PNG (not JPG) | Lossless compression for accurate matching |
| **Size** | As small as possible while clear | Faster pattern matching |
| **Contrast** | High | Better recognition accuracy |
| **Background** | Include minimal extra content | Reduce false matches |
| **Resolution** | Match your display | Ensures pixel-perfect matching |

### Naming Convention

⚠️ **Filenames are case-sensitive!** Use exact names:
- ✅ `harmonyIcon.png`
- ❌ `HarmonyIcon.png` or `harmony-icon.png`

### Size Guidelines

| Image | Recommended Size | Max Size |
|-------|-----------------|----------|
| harmonyIcon.png | 100x100 - 200x200 | 400x400 |
| aboutButton.png | 80x30 - 150x50 | 300x100 |
| aboutText.png | 150x40 - 300x80 | 600x150 |

---

## Similarity Threshold

The tutorial is configured with:
```yaml
brobot:
  find:
    similarity: 0.8  # 80% match required
```

**What this means**:
- Images must match 80% or better to be recognized
- Lower values (0.7) = more permissive (may cause false positives)
- Higher values (0.9) = more strict (may miss variations)

### If Images Aren't Matching:

1. **Too many false positives** (matching wrong elements):
   - Increase similarity to 0.85 or 0.9
   - Capture more distinctive portions of elements

2. **Not matching when they should**:
   - Lower similarity to 0.75 or 0.7
   - Recapture images with better contrast
   - Ensure no compression artifacts

---

## Configuration Location

Image pattern matching configuration is in `src/main/resources/application.yml`:

```yaml
brobot:
  core:
    image-path: images/  # This directory
  find:
    similarity: 0.8      # Match threshold
    parallel: true       # Multi-core searching
  screenshot:
    save-history: true   # Debug screenshots
    path: screenshots/   # Debug output location
```

---

## Automation Flow

The tutorial uses these images in sequence:

```
1. Homepage State
   └─ Looks for harmonyIcon.png
      └─ If found: Click it
         ↓
2. Harmony State
   └─ Looks for aboutButton.png
      └─ If found: Click it
         ↓
3. About State
   └─ Looks for aboutText.png
      └─ If found: Success! ✅
```

---

## Troubleshooting

### Problem: "Pattern not found" errors

**Possible Causes**:
1. Image file doesn't exist or has wrong name
2. Website layout has changed
3. Similarity threshold too high
4. Image was captured at different resolution
5. Browser zoom level different

**Solutions**:
1. Verify all 3 image files are present
2. Check filenames are exact (case-sensitive)
3. Recapture images at current website design
4. Lower similarity in application.yml (try 0.75)
5. Capture images at same browser zoom (usually 100%)

### Problem: Clicking wrong elements

**Possible Causes**:
1. Similar-looking elements on page
2. Similarity threshold too low
3. Image captured includes too much context

**Solutions**:
1. Recapture with tighter cropping
2. Increase similarity to 0.85 or 0.9
3. Choose more unique elements

### Problem: Slow performance

**Possible Causes**:
1. Images too large
2. Parallel search disabled
3. Full-screen searches

**Solutions**:
1. Crop images smaller (while maintaining clarity)
2. Verify `brobot.find.parallel: true` in config
3. Consider using search regions (advanced)

---

## Testing Your Images

### Manual Verification:

1. **Place all 3 images in this directory**
2. **Check filenames**:
   ```bash
   ls -la images/
   # Should show: harmonyIcon.png, aboutButton.png, aboutText.png
   ```
3. **Run the tutorial**:
   ```bash
   cd /path/to/tutorial-mrdoob
   ./gradlew bootRun
   ```
4. **Watch the console output** for pattern matching success/failure

### Debug Mode:

Enable detailed logging to see pattern matching attempts:

```yaml
logging:
  level:
    io.github.jspinak.brobot.action: DEBUG
```

Screenshots of each find operation will be saved to `screenshots/` directory.

---

## State Class References

### Homepage.java
```java
@State(initial = true)
@Getter
@Slf4j
public class Homepage {
    private final StateImage harmony;

    public Homepage() {
        harmony = new StateImage.Builder()
            .addPattern("harmonyIcon")  // → images/harmonyIcon.png
            .build();
    }
}
```

### Harmony.java
```java
@State
@Getter
@Slf4j
public class Harmony {
    private final StateImage about;

    public Harmony() {
        about = new StateImage.Builder()
            .addPattern("aboutButton")  // → images/aboutButton.png
            .build();
    }
}
```

### About.java
```java
@State
@Getter
@Slf4j
public class About {
    private final StateImage aboutText;

    public About() {
        aboutText = new StateImage.Builder()
            .addPattern("aboutText")  // → images/aboutText.png
            .build();
    }
}
```

---

## Advanced: Creating Mock Images

If you don't want to capture real screenshots, you can create simple PNG files for testing pattern matching:

```bash
# Create 100x100 colored rectangles (requires ImageMagick)
convert -size 100x100 xc:blue harmonyIcon.png
convert -size 150x50 xc:green aboutButton.png
convert -size 200x60 xc:red aboutText.png
```

**Note**: These won't work with the real website, but are useful for:
- Testing the application structure
- Verifying image loading
- Debugging configuration issues

---

## Related Documentation

- **Main Tutorial README**: `../README.md`
- **State Management**: `/docs/docs/01-getting-started/states.md`
- **Pattern Matching**: `/docs/docs/02-core-concepts/pattern-matching.md`
- **Configuration Guide**: `/docs/docs/05-guides/configuration.md`

---

## Quick Reference

| File | Size | Used In | Purpose |
|------|------|---------|---------|
| `harmonyIcon.png` | ~150x150 | Homepage | Navigate to Harmony |
| `aboutButton.png` | ~100x40 | Harmony | Navigate to About |
| `aboutText.png` | ~250x60 | About | Verify arrival |

---

## Support

If you encounter issues with image capture or pattern matching:

1. **Check existing images**: Example images may be in project screenshots directory
2. **Consult documentation**: See `/docs/` for comprehensive guides
3. **Enable debug mode**: Set log level to DEBUG in application.yml
4. **Review screenshots**: Check `screenshots/` directory for debug output
5. **File an issue**: https://github.com/jspinak/brobot/issues

---

**Last Updated**: Compatible with Brobot 1.1.0 and Spring Boot 3.3.0
