# Brobot Runner User Manual

## Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [User Interface Overview](#user-interface-overview)
4. [Working with Configurations](#working-with-configurations)
5. [Execution and Monitoring](#execution-and-monitoring)
6. [Advanced Features](#advanced-features)
7. [Settings and Preferences](#settings-and-preferences)
8. [Keyboard Shortcuts](#keyboard-shortcuts)
9. [Tips and Best Practices](#tips-and-best-practices)

## Introduction

Brobot Runner is a powerful desktop application designed to create, edit, and execute GUI automation configurations for the Brobot framework. It provides an intuitive interface for managing automation workflows without requiring programming knowledge.

### Key Features
- Visual configuration editor with syntax highlighting
- Real-time validation and error checking
- Live execution monitoring with detailed logs
- Session recovery after crashes
- Performance optimization for large configurations
- Cross-platform support (Windows, macOS, Linux)

## Getting Started

### First Launch

When you launch Brobot Runner for the first time:

1. **Welcome Screen**: You'll see a welcome screen with options to:
   - Create a new configuration
   - Open an existing configuration
   - Browse example configurations
   - Access documentation

2. **Initial Setup**: The application will:
   - Create necessary directories
   - Check for required permissions
   - Initialize default settings

3. **Recovery Check**: If a previous session crashed, you'll be prompted to recover your work

### Creating Your First Configuration

1. Click **File > New Configuration** or press `Ctrl+N` (`Cmd+N` on macOS)
2. Choose a template:
   - **Basic**: Simple automation template
   - **Web Automation**: For browser-based tasks
   - **Desktop Automation**: For desktop application tasks
   - **Custom**: Start with a blank configuration
3. Save your configuration with a descriptive name

## User Interface Overview

### Main Window Components

```
┌─────────────────────────────────────────────────────────────┐
│ Menu Bar                                                    │
├─────────────────────────────────────────────────────────────┤
│ Toolbar                                                     │
├───────────────┬─────────────────────────┬──────────────────┤
│               │                         │                  │
│  Navigator    │   Editor Area           │  Properties      │
│  Panel        │                         │  Panel           │
│               │                         │                  │
├───────────────┴─────────────────────────┴──────────────────┤
│ Output/Console Area                                         │
├─────────────────────────────────────────────────────────────┤
│ Status Bar                                                  │
└─────────────────────────────────────────────────────────────┘
```

### Menu Bar

**File Menu**
- New Configuration (`Ctrl+N`)
- Open Configuration (`Ctrl+O`)
- Recent Files
- Save (`Ctrl+S`)
- Save As (`Ctrl+Shift+S`)
- Export Configuration
- Exit (`Alt+F4`)

**Edit Menu**
- Undo (`Ctrl+Z`)
- Redo (`Ctrl+Y`)
- Cut/Copy/Paste
- Find (`Ctrl+F`)
- Replace (`Ctrl+H`)
- Format JSON (`Ctrl+Alt+F`)

**View Menu**
- Show/Hide Panels
- Zoom In/Out (`Ctrl++`/`Ctrl+-`)
- Reset Layout
- Theme Selection

**Run Menu**
- Execute Configuration (`F5`)
- Debug Mode (`F6`)
- Stop Execution (`Shift+F5`)
- Run Selected Section

**Tools Menu**
- Validate Configuration
- Performance Profiler
- Diagnostic Tool
- Options/Settings

**Help Menu**
- User Manual
- Configuration Guide
- Troubleshooting
- About

### Toolbar

Quick access buttons for common actions:
- 🆕 New Configuration
- 📁 Open Configuration
- 💾 Save
- ▶️ Run
- ⏸️ Pause
- ⏹️ Stop
- 🔍 Validate
- 🛠️ Settings

### Navigator Panel

The left panel shows:
- **Configuration Tree**: Hierarchical view of your configuration
- **Outline View**: Quick navigation to sections
- **Search**: Find elements within the configuration

Right-click any item for context menu options:
- Edit
- Duplicate
- Delete
- Move Up/Down
- Add Child Element

### Editor Area

The central editor provides:
- **Syntax Highlighting**: JSON syntax with color coding
- **Auto-completion**: Intelligent suggestions as you type
- **Error Markers**: Red underlines for syntax errors
- **Code Folding**: Collapse/expand sections
- **Line Numbers**: For easy reference
- **Breadcrumb Navigation**: Shows current location in structure

### Properties Panel

The right panel displays:
- **Element Properties**: Editable fields for selected elements
- **Validation Results**: Real-time validation feedback
- **Quick Actions**: Common operations for the selected element
- **Documentation**: Context-sensitive help

### Output Area

The bottom panel contains tabs for:
- **Console**: Execution logs and messages
- **Problems**: Validation errors and warnings
- **Output**: Detailed execution results
- **Debug**: Debug information when in debug mode

## Working with Configurations

### Configuration Structure

A typical Brobot configuration consists of:

```json
{
  "name": "My Automation",
  "description": "Automates daily tasks",
  "settings": {
    "timeout": 30,
    "retryCount": 3
  },
  "states": [
    {
      "name": "InitialState",
      "images": ["start_button.png"],
      "actions": [
        {
          "type": "click",
          "target": "start_button"
        }
      ]
    }
  ]
}
```

### Creating Elements

1. **Add State**: Right-click in navigator > Add State
2. **Add Image**: Drag and drop image files or use Add Image button
3. **Add Action**: Select state > Add Action > Choose action type
4. **Add Transition**: Connect states by dragging between them

### Editing Configurations

**Visual Editing**
- Use the Properties panel to modify element attributes
- Drag and drop to reorder elements
- Double-click to rename elements

**Text Editing**
- Switch to Source view for direct JSON editing
- Use `Ctrl+Space` for auto-completion
- Format with `Ctrl+Alt+F`

### Validation

Configurations are validated in real-time:
- ✅ **Green checkmark**: Valid configuration
- ⚠️ **Yellow warning**: Non-critical issues
- ❌ **Red error**: Must be fixed before execution

Click on any validation message to jump to the problem.

## Execution and Monitoring

### Running Configurations

1. **Quick Run**: Press `F5` or click the Run button
2. **Debug Run**: Press `F6` for step-by-step execution
3. **Partial Run**: Select elements and choose "Run Selection"

### Execution Options

Before running, you can configure:
- **Execution Speed**: Normal, Slow, Fast
- **Screenshot Capture**: Enable/disable
- **Logging Level**: Error, Warning, Info, Debug
- **Breakpoints**: Set by clicking line numbers

### Monitoring Execution

During execution:
- **Progress Bar**: Shows overall progress
- **Current State**: Highlighted in navigator
- **Live Logs**: Scroll in console
- **Statistics**: Performance metrics in status bar

### Controlling Execution

- **Pause**: Temporarily halt execution
- **Resume**: Continue from pause
- **Stop**: Terminate execution
- **Step**: Execute one action (debug mode)

## Advanced Features

### Session Recovery

If Brobot Runner crashes:
1. On next launch, you'll see "Recover Previous Session?"
2. Click "Recover" to restore:
   - Open configurations
   - Unsaved changes
   - Window layout
   - Execution history

### Performance Optimization

For large configurations:
1. **Enable Lazy Loading**: Settings > Performance > Lazy Load Images
2. **Adjust Memory**: Settings > Performance > Memory Allocation
3. **Use Profiler**: Tools > Performance Profiler

### Templates and Snippets

Create reusable components:
1. **Save as Template**: File > Save as Template
2. **Insert Snippet**: Right-click > Insert Snippet
3. **Manage Templates**: Tools > Template Manager

### Version Control Integration

Work with Git repositories:
1. **Initialize Repository**: File > Version Control > Initialize
2. **Commit Changes**: `Ctrl+K` with commit message
3. **View History**: File > Version Control > History

### Export and Import

Share configurations:
- **Export Package**: Includes images and dependencies
- **Import Package**: Automatically extracts and configures
- **Export as Script**: Generate executable script

## Settings and Preferences

### General Settings

**Appearance**
- Theme: Light, Dark, Auto
- Font Size: Adjust editor font
- Icon Size: UI element sizing

**Behavior**
- Auto-save: Enable with interval
- Confirmation Dialogs: Toggle confirmations
- Startup: Choose default action

### Editor Settings

**Code Style**
- Indentation: Spaces or tabs
- Tab Size: 2, 4, or 8 spaces
- Line Wrapping: Enable/disable
- Show Whitespace: Toggle visibility

**Validation**
- Real-time Validation: Enable/disable
- Validation Severity: Customize rules
- Auto-fix: Enable automatic corrections

### Execution Settings

**Default Options**
- Timeout: Global timeout value
- Retry Count: Failed action retries
- Screenshot Path: Where to save captures
- Log Level: Default logging verbosity

**Advanced**
- Thread Pool Size: Concurrent operations
- Memory Limit: Maximum heap size
- GPU Acceleration: Enable/disable

### Network Settings

**Proxy Configuration**
- HTTP/HTTPS Proxy: Server settings
- Proxy Authentication: Username/password
- Bypass List: Excluded addresses

**Updates**
- Auto-check: Enable update checks
- Update Channel: Stable, Beta, Nightly

## Keyboard Shortcuts

### Essential Shortcuts

| Action | Windows/Linux | macOS |
|--------|--------------|-------|
| New Configuration | `Ctrl+N` | `Cmd+N` |
| Open Configuration | `Ctrl+O` | `Cmd+O` |
| Save | `Ctrl+S` | `Cmd+S` |
| Save As | `Ctrl+Shift+S` | `Cmd+Shift+S` |
| Close Tab | `Ctrl+W` | `Cmd+W` |
| Undo | `Ctrl+Z` | `Cmd+Z` |
| Redo | `Ctrl+Y` | `Cmd+Shift+Z` |
| Find | `Ctrl+F` | `Cmd+F` |
| Replace | `Ctrl+H` | `Cmd+Option+F` |
| Run | `F5` | `F5` |
| Debug | `F6` | `F6` |
| Stop | `Shift+F5` | `Shift+F5` |

### Navigation Shortcuts

| Action | Windows/Linux | macOS |
|--------|--------------|-------|
| Go to Line | `Ctrl+G` | `Cmd+L` |
| Next Tab | `Ctrl+Tab` | `Cmd+Option+→` |
| Previous Tab | `Ctrl+Shift+Tab` | `Cmd+Option+←` |
| Toggle Sidebar | `Ctrl+B` | `Cmd+B` |
| Toggle Console | `Ctrl+J` | `Cmd+J` |
| Quick Open | `Ctrl+P` | `Cmd+P` |
| Command Palette | `Ctrl+Shift+P` | `Cmd+Shift+P` |

### Editing Shortcuts

| Action | Windows/Linux | macOS |
|--------|--------------|-------|
| Duplicate Line | `Ctrl+D` | `Cmd+D` |
| Delete Line | `Ctrl+Shift+K` | `Cmd+Shift+K` |
| Move Line Up | `Alt+↑` | `Option+↑` |
| Move Line Down | `Alt+↓` | `Option+↓` |
| Comment Line | `Ctrl+/` | `Cmd+/` |
| Format Document | `Ctrl+Alt+F` | `Cmd+Option+F` |
| Expand Selection | `Shift+Alt+→` | `Shift+Option+→` |

## Tips and Best Practices

### Configuration Best Practices

1. **Use Descriptive Names**: Name states and actions clearly
2. **Organize Hierarchically**: Group related elements
3. **Add Comments**: Document complex logic
4. **Validate Regularly**: Fix issues as they appear
5. **Test Incrementally**: Run sections before full execution

### Performance Tips

1. **Optimize Images**: Use appropriate resolution
2. **Limit Scope**: Break large automations into modules
3. **Use Caching**: Enable for frequently accessed resources
4. **Monitor Resources**: Check memory and CPU usage
5. **Clean Up**: Remove unused elements and images

### Troubleshooting Tips

1. **Check Logs**: Console provides detailed information
2. **Use Debug Mode**: Step through problematic sections
3. **Validate First**: Ensure configuration is valid
4. **Test Isolation**: Run problematic parts separately
5. **Report Issues**: Use Help > Report Issue with logs

### Security Recommendations

1. **Avoid Hardcoding**: Use variables for sensitive data
2. **Secure Storage**: Encrypt sensitive configurations
3. **Access Control**: Limit file permissions
4. **Regular Updates**: Keep Brobot Runner updated
5. **Audit Logs**: Review execution history regularly

## Conclusion

Brobot Runner is designed to make GUI automation accessible and efficient. This manual covers the essential features, but the application continues to evolve. For the latest information:

- Check for updates regularly
- Visit our [documentation site](https://github.com/jspinak/brobot/wiki)
- Join our [community forum](https://github.com/jspinak/brobot/discussions)
- Report issues on [GitHub](https://github.com/jspinak/brobot/issues)

Happy automating!