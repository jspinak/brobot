# Brobot Runner Troubleshooting Guide

## Overview

This guide helps you diagnose and resolve common issues with Brobot Runner. Each section covers specific problem categories with symptoms, causes, and solutions.

## Quick Diagnostics

Before troubleshooting specific issues, run the built-in diagnostic tool:

1. **From UI**: Tools > Diagnostic Tool > Run Diagnostics
2. **From Terminal**: `brobot-runner --diagnostics`

This will check:
- System requirements
- Java version and configuration
- File permissions
- Network connectivity
- Memory and disk space

## Common Issues

### 1. Application Won't Start

#### Symptoms
- Double-clicking the icon does nothing
- Application crashes immediately
- Error dialog appears briefly

#### Possible Causes and Solutions

**Java Version Issues**
```bash
# Check Java version
java -version
```
- **Solution**: Install Java 21 or later from [Adoptium](https://adoptium.net/)

**Corrupted Installation**
- **Solution**: Reinstall Brobot Runner
- **Windows**: Uninstall via Control Panel, then reinstall
- **macOS**: Delete app from Applications, empty trash, reinstall
- **Linux**: `sudo apt-get remove --purge brobot-runner` then reinstall

**Permission Issues**
- **Windows**: Run as Administrator (right-click > Run as administrator)
- **macOS**: Check Security & Privacy settings
- **Linux**: Check file permissions: `ls -la /opt/brobot-runner`

**Missing Dependencies**
- **Linux**: Install required libraries
  ```bash
  sudo apt-get install libgtk-3-0 libglib2.0-0 libjavafx-* openjfx
  ```

### 2. Configuration File Issues

#### Cannot Open Configuration Files

**Symptoms**
- "File not found" errors
- "Invalid configuration" messages
- Files appear corrupted

**Solutions**
1. **Verify file path**: Ensure no special characters or spaces
2. **Check file permissions**: Right-click > Properties > Security
3. **Validate JSON syntax**:
   ```bash
   # Using jq tool
   jq . config.json
   ```
4. **Try opening in text editor** to check for corruption

#### Configuration Validation Errors

**Common validation errors and fixes:**

| Error | Fix |
|-------|-----|
| "Missing required field: name" | Add `"name": "YourConfigName"` to root |
| "Invalid type for property" | Check data types match schema |
| "Duplicate state name" | Ensure all state names are unique |
| "Image file not found" | Verify image paths are relative to config |

### 3. Performance Issues

#### Slow Application Startup

**Symptoms**
- Takes >30 seconds to start
- Shows "Loading..." for extended time
- UI freezes during startup

**Solutions**
1. **Increase memory allocation**:
   - Edit `brobot-runner.vmoptions`:
   ```
   -Xms512m
   -Xmx2048m
   ```

2. **Disable startup features**:
   - Settings > Performance > Disable "Check for updates on startup"
   - Settings > Performance > Disable "Load recent files"

3. **Clear cache**:
   - Windows: `%APPDATA%\BrobotRunner\cache`
   - macOS: `~/Library/Caches/BrobotRunner`
   - Linux: `~/.cache/brobot-runner`

#### UI Lag and Freezing

**Symptoms**
- Delayed response to clicks
- Stuttering when scrolling
- Progress bars freeze

**Solutions**
1. **Reduce UI effects**:
   - Settings > Appearance > Disable animations
   - Settings > Editor > Disable syntax highlighting

2. **Close unnecessary panels**:
   - View > Hide Output Panel
   - View > Hide Navigator Panel

3. **Process large files differently**:
   - Split large configurations into modules
   - Use lazy loading for images

### 4. Execution Problems

#### Actions Not Working

**Symptoms**
- Clicks miss targets
- Text not typed correctly
- Automation stops unexpectedly

**Solutions**
1. **Adjust recognition settings**:
   ```json
   {
     "settings": {
       "recognition": {
         "defaultSimilarity": 0.8,  // Lower from 0.9
         "searchMethod": "accurate"  // Change from "fast"
       }
     }
   }
   ```

2. **Add delays between actions**:
   ```json
   {
     "settings": {
       "execution": {
         "delay": 0.5  // Increase from default
       }
     }
   }
   ```

3. **Check screen resolution**:
   - Ensure screen resolution matches when images were captured
   - Recapture images at current resolution

#### Image Recognition Failures

**Symptoms**
- "Image not found" errors
- Wrong elements clicked
- Inconsistent recognition

**Solutions**
1. **Improve image quality**:
   - Use PNG format (not JPEG)
   - Capture at 100% zoom
   - Include unique elements

2. **Adjust search regions**:
   ```json
   {
     "searchRegion": {
       "x": 100,
       "y": 100,
       "width": 800,
       "height": 600
     }
   }
   ```

3. **Use multiple images**:
   ```json
   {
     "images": [
       {"name": "button_normal", "path": "button1.png"},
       {"name": "button_hover", "path": "button2.png"}
     ]
   }
   ```

### 5. Network and Connectivity

#### Cannot Download Remote Resources

**Symptoms**
- "Connection timeout" errors
- "Unable to reach server" messages
- Progress stuck at 0%

**Solutions**
1. **Configure proxy settings**:
   - Settings > Network > HTTP Proxy
   - Enter proxy server and port
   - Add authentication if required

2. **Check firewall**:
   - Windows: Allow through Windows Defender Firewall
   - macOS: System Preferences > Security & Privacy > Firewall
   - Linux: Check iptables/ufw rules

3. **Test connectivity**:
   ```bash
   # Test specific URL
   curl -I https://example.com
   
   # Test with proxy
   curl -x proxy:port -I https://example.com
   ```

### 6. Memory and Resource Issues

#### Out of Memory Errors

**Symptoms**
- "OutOfMemoryError" in logs
- Application crashes during execution
- "Heap space" error messages

**Solutions**
1. **Increase heap size**:
   - Windows: Edit `BrobotRunner.l4j.ini`
   - macOS: Edit Info.plist VMOptions
   - Linux: Edit `/usr/share/brobot-runner/brobot-runner.vmoptions`
   
   Add or modify:
   ```
   -Xmx4096m
   ```

2. **Optimize configuration**:
   - Reduce image sizes
   - Limit concurrent operations
   - Enable image caching

3. **Monitor memory usage**:
   - View > Memory Monitor
   - Tools > Performance Profiler

#### High CPU Usage

**Symptoms**
- Fan runs constantly
- System becomes sluggish
- CPU at 100% in Task Manager

**Solutions**
1. **Limit thread usage**:
   ```json
   {
     "settings": {
       "performance": {
         "maxThreads": 2  // Reduce from default
       }
     }
   }
   ```

2. **Disable GPU acceleration**:
   - Settings > Performance > Disable GPU acceleration

3. **Check for infinite loops**:
   - Review loop conditions in configuration
   - Add maximum iteration limits

### 7. File and Permission Issues

#### Cannot Save Files

**Symptoms**
- "Permission denied" errors
- "Read-only file system" messages
- Changes not persisting

**Solutions**
1. **Check file permissions**:
   ```bash
   # Linux/macOS
   ls -la /path/to/file
   chmod 644 file.json
   
   # Windows (as admin)
   icacls "C:\path\to\file" /grant Users:F
   ```

2. **Check disk space**:
   ```bash
   # Linux/macOS
   df -h
   
   # Windows
   wmic logicaldisk get size,freespace,caption
   ```

3. **Disable antivirus scanning**:
   - Add Brobot Runner to exclusions
   - Exclude configuration directories

### 8. Crash Recovery

#### Recovering from Crashes

**When Brobot Runner crashes:**

1. **On next launch**:
   - Click "Recover Session" when prompted
   - Review recovered files
   - Save important changes immediately

2. **Manual recovery**:
   - Check auto-save directory:
     - Windows: `%APPDATA%\BrobotRunner\autosave`
     - macOS: `~/Library/Application Support/BrobotRunner/autosave`
     - Linux: `~/.config/brobot-runner/autosave`

3. **Generate crash report**:
   ```bash
   brobot-runner --generate-report
   ```

### 9. Update and Installation Issues

#### Update Failures

**Symptoms**
- "Update failed" messages
- Partial updates causing issues
- Version conflicts

**Solutions**
1. **Manual update**:
   - Download installer from website
   - Run installer over existing installation

2. **Clean installation**:
   - Backup configurations
   - Uninstall completely
   - Install fresh copy
   - Restore configurations

3. **Reset update cache**:
   ```bash
   # Remove update cache
   rm -rf ~/.brobot-runner/updates
   ```

## Advanced Troubleshooting

### Enable Debug Logging

1. **Via UI**:
   - Settings > Logging > Set Level to "Debug"
   - Settings > Logging > Enable "Write to file"

2. **Via Command Line**:
   ```bash
   brobot-runner --log-level=debug --log-file=debug.log
   ```

3. **Via Environment Variable**:
   ```bash
   export BROBOT_LOG_LEVEL=debug
   brobot-runner
   ```

### Collect Support Information

Generate a support bundle:

```bash
brobot-runner --support-bundle
```

This creates a ZIP file containing:
- System information
- Configuration files (sanitized)
- Recent logs
- Diagnostic results
- Error reports

### Reset to Defaults

If all else fails, reset Brobot Runner:

1. **Reset preferences**:
   ```bash
   brobot-runner --reset-preferences
   ```

2. **Reset entire application**:
   - Windows: `%APPDATA%\BrobotRunner` - Delete folder
   - macOS: `~/Library/Application Support/BrobotRunner` - Delete folder
   - Linux: `~/.config/brobot-runner` - Delete folder

## Getting Additional Help

### Before Contacting Support

1. **Check the logs**:
   - View > Logs > Show Log File
   - Look for ERROR or WARN entries

2. **Try safe mode**:
   ```bash
   brobot-runner --safe-mode
   ```

3. **Update to latest version**:
   - Help > Check for Updates

### Contacting Support

When reporting issues:

1. **Include**:
   - Brobot Runner version (Help > About)
   - Operating system and version
   - Steps to reproduce
   - Error messages (exact text or screenshots)
   - Support bundle if possible

2. **Report via**:
   - GitHub Issues: [https://github.com/jspinak/brobot/issues](https://github.com/jspinak/brobot/issues)
   - Community Forum: [https://github.com/jspinak/brobot/discussions](https://github.com/jspinak/brobot/discussions)

## Preventive Measures

### Regular Maintenance

1. **Weekly**:
   - Clear cache if performance degrades
   - Check for updates

2. **Monthly**:
   - Review and clean old configurations
   - Archive completed projects
   - Check disk space

3. **Before Major Projects**:
   - Update to latest version
   - Test in safe mode
   - Backup configurations

### Best Practices

1. **Save frequently**: Enable auto-save with 5-minute intervals
2. **Use version control**: Commit configurations to Git
3. **Test incrementally**: Validate sections before full runs
4. **Monitor resources**: Keep Memory Monitor visible
5. **Document issues**: Note any workarounds for future reference

---

Remember: Most issues can be resolved by:
1. Updating to the latest version
2. Checking file permissions
3. Validating configurations
4. Reviewing error logs

If problems persist after trying these solutions, please report the issue with detailed information for assistance.