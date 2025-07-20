# AtlantaFX Theme Integration

## Overview
The Brobot Runner has been updated to use AtlantaFX, a modern JavaFX theme collection that provides flat, web-inspired interfaces. AtlantaFX offers professional themes with comprehensive styling for all JavaFX controls.

## Available Themes

### Light Themes
1. **Primer Light** - GitHub-inspired clean design
2. **Nord Light** - Nordic-inspired pastel colors
3. **Cupertino Light** - macOS-inspired interface

### Dark Themes
1. **Primer Dark** - GitHub dark mode
2. **Nord Dark** - Nordic dark palette
3. **Cupertino Dark** - macOS dark mode
4. **Dracula** - Popular dark theme with vibrant colors

## Implementation Details

### Dependencies
Added to `build.gradle`:
```gradle
implementation 'io.github.mkpaz:atlantafx-base:2.0.1'
```

### Key Components

1. **AtlantaFXThemeManager** (`/ui/theme/AtlantaFXThemeManager.java`)
   - Manages theme switching and application
   - Provides theme change notifications
   - Supports custom CSS overlays
   - Auto-applies label overlap fixes

2. **ThemeSelector** (`/ui/components/ThemeSelector.java`)
   - UI component for theme selection
   - ComboBox with all available themes
   - Dark mode toggle button
   - Can be embedded in settings panels

3. **ThemeConfiguration** (`/config/ThemeConfiguration.java`)
   - Spring configuration for theme management
   - Provides adapter for backward compatibility
   - Ensures smooth migration from old theme system

## Usage

### Basic Theme Application
```java
// In your JavaFX Application class
AtlantaFXThemeManager themeManager = new AtlantaFXThemeManager();
themeManager.setTheme(AtlantaTheme.PRIMER_LIGHT);
themeManager.registerScene(scene);
```

### Adding Theme Selector to UI
```java
// In your settings panel
ThemeSelector themeSelector = new ThemeSelector(themeManager);
settingsPane.getChildren().add(themeSelector);
```

### Programmatic Theme Switching
```java
// Switch to dark mode
themeManager.setTheme(AtlantaTheme.PRIMER_DARK);

// Toggle between light/dark
themeManager.toggleLightDark();

// Check if dark theme
if (themeManager.isDarkTheme()) {
    // Adjust UI accordingly
}
```

## Benefits

1. **Professional Appearance** - Modern, flat design inspired by web frameworks
2. **Comprehensive Coverage** - All JavaFX controls are styled
3. **Dark Mode Support** - Each theme has light and dark variants
4. **Accessibility** - Based on GitHub Primer design system with good contrast
5. **Customizable** - Can overlay custom CSS for specific needs
6. **Active Maintenance** - AtlantaFX is actively maintained with regular updates

## CSS Variables

AtlantaFX uses CSS variables for colors, making customization easy:
- `-color-bg-default` - Default background
- `-color-fg-default` - Default foreground (text)
- `-color-accent-emphasis` - Primary accent color
- `-color-success-fg` - Success state color
- `-color-danger-fg` - Error/danger state color
- And many more...

## Migration from Old Theme System

The old theme system is still supported through the `ThemeConfiguration` adapter. This ensures:
- Existing code continues to work
- Gradual migration is possible
- No breaking changes for users

## Testing

Run the theme demo to see all themes in action:
```bash
cd /home/jspinak/brobot-parent-directory/brobot/runner
./gradlew test --tests AtlantaFXThemeDemo
```

Or run it as a standalone application to interact with the themes.

## Custom Styling

To add custom styles on top of AtlantaFX themes:

1. Create your CSS file in `/resources/css/`
2. Add it to the theme manager:
   ```java
   themeManager.addCustomCss(getClass().getResource("/css/my-custom.css").toExternalForm());
   ```

## Next Steps

1. **Remove old theme CSS files** - Once migration is complete, remove:
   - `/css/modern-theme.css`
   - `/css/themes/light-theme.css`
   - `/css/themes/dark-theme.css`
   - `/css/themes/theme.css`

2. **Update UI components** - Review and update components to use AtlantaFX style classes:
   - `.accent` for primary buttons
   - `.success` for success states
   - `.danger` for destructive actions
   - `.flat` for flat buttons

3. **Theme Persistence** - Add user preference storage to remember selected theme

4. **Theme Preview** - Add theme preview in settings panel

## Troubleshooting

### Label Overlap Issues
The AtlantaFXThemeManager automatically includes `label-overlap-fix.css`. If issues persist:
1. Check that the CSS is being loaded
2. Verify no conflicting styles are overriding it
3. Use the browser-style inspector in ScenicView for debugging

### Theme Not Applying
1. Ensure `Application.setUserAgentStylesheet()` is called
2. Check that scenes are registered with the theme manager
3. Verify AtlantaFX dependency is properly included

### Performance
AtlantaFX themes are optimized for performance. If you experience issues:
1. Avoid frequent theme switching
2. Register scenes once, not repeatedly
3. Use the built-in CSS variables instead of custom overrides when possible