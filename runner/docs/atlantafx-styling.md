# AtlantaFX Styling Integration

## Overview

The Brobot Runner application has been refactored to use AtlantaFX themes for a modern, professional appearance. This document explains the styling architecture and how to work with it.

## Theme Architecture

### Theme Manager Hierarchy

1. **AtlantaFXThemeManager** - Base theme manager that provides AtlantaFX theme support
2. **BrobotThemeManager** (extends AtlantaFXThemeManager) - Adds Brobot-specific customizations
3. **OptimizedThemeManager** (extends ThemeManager) - Provides performance optimizations for theme loading

### CSS Loading Order

CSS files are loaded in the following order to ensure proper styling cascade:

1. **AtlantaFX Theme CSS** (Built-in from AtlantaFX library)
2. **atlantafx-integration.css** - Integration styles for AtlantaFX
3. **ui-fixes-atlantafx.css** - Specific UI fixes and enhancements
4. **brobot-overrides.css** - Brobot-specific customizations
5. **animations.css** - Animation effects

## Key Styling Changes

### Fixed Issues

1. **Overlapping Text**
   - Tab headers now have proper spacing and min-width
   - Status bar labels have defined spacing and separators
   - Table column headers have minimum widths to prevent overlap

2. **Header/Title Bar**
   - Uses AtlantaFX's accent color for the header
   - White text on colored background for better contrast
   - Consistent height and padding

3. **Status Bar**
   - Proper spacing between elements
   - Vertical separators between sections
   - Text overflow handling with ellipsis

4. **Buttons and Controls**
   - Consistent border radius (4px)
   - Proper padding and minimum sizes
   - Hover effects and focus states

## CSS Classes

### Layout Classes
- `.header-panel` - Main application header
- `.status-bar` - Bottom status bar
- `.card` - Card container with shadow
- `.configuration-tabs` - Tab pane for configuration sections

### Button Classes
- `.button` - Base button styling
- `.button-primary` - Primary action buttons
- `.button-success` - Success/positive action buttons
- `.button-danger` - Destructive action buttons

### Text Classes
- `.header-title` - Main header title
- `.card-title` - Card section titles
- `.status-label` - Status bar labels
- `.status-path-label` - Path information in status bar

## Available Themes

The application supports the following AtlantaFX themes:

- **Primer Light/Dark** - GitHub-inspired theme
- **Nord Light/Dark** - Nordic color palette
- **Cupertino Light/Dark** - macOS-inspired theme
- **Dracula** - Popular dark theme

## Customization

To add custom styling:

1. Create a new CSS file in `/resources/css/`
2. Add it to `BrobotThemeManager.addBrobotCustomizations()`
3. Use AtlantaFX CSS variables for consistency:
   - `-color-bg-default` - Default background
   - `-color-fg-default` - Default text color
   - `-color-accent-emphasis` - Primary accent color
   - `-color-border-default` - Default border color

## Best Practices

1. Always use AtlantaFX color variables instead of hard-coded colors
2. Maintain consistent spacing using the defined variables
3. Test with both light and dark themes
4. Use proper text overflow handling (ellipsis) for long text
5. Ensure sufficient contrast for accessibility

## Troubleshooting

If styling issues occur:

1. Check the browser developer tools (F12) for CSS conflicts
2. Verify CSS files are loading in the correct order
3. Ensure theme manager is properly initialized
4. Check for missing style classes in Java code
5. Verify AtlantaFX dependencies are properly included