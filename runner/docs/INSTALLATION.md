# Brobot Runner Installation Guide

## Overview

Brobot Runner is a JavaFX-based desktop application that provides a graphical interface for the Brobot automation framework. This guide will help you install Brobot Runner on Windows, macOS, and Linux.

## System Requirements

### Minimum Requirements
- **Operating System**: Windows 10/11, macOS 10.14+, Linux (Ubuntu 18.04+, Fedora 30+, or equivalent)
- **Memory**: 2 GB RAM (4 GB recommended)
- **Storage**: 200 MB available disk space
- **Display**: 1024x768 resolution

### Software Requirements
- Java 21 or later (bundled with installer)
- Internet connection for downloading remote resources (optional)

## Installation Instructions

### Windows

#### Using the MSI Installer (Recommended)

1. **Download the Installer**
   - Download `BrobotRunner-1.0.0.msi` from the releases page
   - The file is digitally signed for security

2. **Run the Installer**
   - Double-click the MSI file
   - If Windows SmartScreen appears, click "More info" and then "Run anyway"
   - Follow the installation wizard:
     - Accept the license agreement
     - Choose installation directory (default: `C:\Program Files\Brobot Runner`)
     - Select whether to create desktop shortcuts
     - Choose whether to install for all users or current user only

3. **Complete Installation**
   - Click "Install" to begin installation
   - The installer will:
     - Copy application files
     - Create Start Menu shortcuts
     - Register file associations for `.json` files
     - Add Windows Defender exclusion for better performance
   - Click "Finish" when complete

4. **Launch the Application**
   - Use the desktop shortcut, or
   - Find "Brobot Runner" in the Start Menu, or
   - Double-click any `.json` configuration file

#### Manual Installation

1. Extract the ZIP archive to your desired location
2. Run `BrobotRunner.exe` from the extracted folder
3. Optionally, create shortcuts manually

### macOS

#### Using the DMG Installer (Recommended)

1. **Download the Installer**
   - Download `BrobotRunner-1.0.0.dmg` from the releases page
   - The DMG should be notarized by Apple for security

2. **Install the Application**
   - Double-click the DMG file
   - Drag the "Brobot Runner" icon to the Applications folder
   - Eject the DMG when done

3. **First Launch**
   - Open Finder and go to Applications
   - Right-click "Brobot Runner" and select "Open"
   - Click "Open" in the security dialog (first time only)
   - The app will request permissions for:
     - Automation (for GUI control)
     - File access (for configurations)

4. **Command Line Tool**
   - The installer creates a command line tool: `brobot-runner`
   - Available in `/usr/local/bin` after installation

#### Troubleshooting macOS Security

If you see "Brobot Runner can't be opened because it is from an unidentified developer":
1. Go to System Preferences > Security & Privacy
2. Click "Open Anyway" next to the Brobot Runner message
3. Or use Terminal: `xattr -cr /Applications/Brobot\ Runner.app`

### Linux

#### DEB Package (Debian/Ubuntu)

1. **Download the Package**
   - Download `brobot-runner_1.0.0_amd64.deb` from the releases page

2. **Install Using GUI**
   - Double-click the DEB file
   - Click "Install" in the Software Center
   - Enter your password when prompted

3. **Install Using Terminal**
   ```bash
   sudo dpkg -i brobot-runner_1.0.0_amd64.deb
   # If there are dependency issues:
   sudo apt-get install -f
   ```

4. **Launch the Application**
   - Find "Brobot Runner" in your applications menu
   - Or run `brobot-runner` from terminal

#### RPM Package (Fedora/RHEL/CentOS)

1. **Download the Package**
   - Download `brobot-runner-1.0.0.x86_64.rpm` from the releases page

2. **Install Using GUI**
   - Double-click the RPM file
   - Click "Install" in the Software Center

3. **Install Using Terminal**
   ```bash
   sudo rpm -i brobot-runner-1.0.0.x86_64.rpm
   # Or using dnf/yum:
   sudo dnf install brobot-runner-1.0.0.x86_64.rpm
   ```

#### Universal Installation (AppImage)

1. Download `brobot-runner-1.0.0.AppImage`
2. Make it executable: `chmod +x brobot-runner-1.0.0.AppImage`
3. Run: `./brobot-runner-1.0.0.AppImage`

#### Building from Source

```bash
# Clone the repository
git clone https://github.com/jspinak/brobot.git
cd brobot/runner

# Build with Gradle
./gradlew clean build

# Create native package
./gradlew jpackage

# Run the application
./gradlew run
```

## Post-Installation Setup

### File Associations

The installers automatically configure file associations for `.json` configuration files. To verify:

- **Windows**: Right-click a `.json` file > Open with > Choose "Brobot Runner"
- **macOS**: Right-click a `.json` file > Get Info > Change "Open with" to "Brobot Runner"
- **Linux**: Right-click a `.json` file > Properties > Open With > Select "Brobot Runner"

### Environment Variables

Optionally set these environment variables:

```bash
# Configuration directory
export BROBOT_CONFIG_DIR="$HOME/.brobot/configs"

# Log directory
export BROBOT_LOG_DIR="$HOME/.brobot/logs"

# Memory settings (if not using defaults)
export BROBOT_JAVA_OPTS="-Xmx4G -Xms1G"
```

### Initial Configuration

1. Launch Brobot Runner
2. The application will create default directories:
   - Windows: `%APPDATA%\BrobotRunner`
   - macOS: `~/Library/Application Support/BrobotRunner`
   - Linux: `~/.config/brobot-runner`
3. Configure your preferences in Settings
4. Load or create your first configuration file

## Updating

### Automatic Updates
Brobot Runner checks for updates on startup. When an update is available:
1. Click "Download Update" when prompted
2. The application will download and install the update
3. Restart to apply the update

### Manual Updates
1. Download the latest installer from the releases page
2. Run the installer - it will upgrade the existing installation
3. Your settings and configurations will be preserved

## Uninstallation

### Windows
1. Go to Settings > Apps > Apps & features
2. Find "Brobot Runner" and click "Uninstall"
3. Or use Control Panel > Programs and Features

### macOS
1. Drag "Brobot Runner" from Applications to Trash
2. Empty Trash
3. Optionally remove settings: `rm -rf ~/Library/Application\ Support/BrobotRunner`

### Linux
```bash
# Debian/Ubuntu
sudo apt-get remove brobot-runner

# Fedora/RHEL
sudo dnf remove brobot-runner

# Remove configuration files
rm -rf ~/.config/brobot-runner
```

## Troubleshooting Installation Issues

### Windows Issues

**Problem**: "Windows protected your PC" message
- **Solution**: Click "More info" then "Run anyway". The installer is safe but not yet widely recognized by SmartScreen.

**Problem**: Missing VCRUNTIME140.dll
- **Solution**: Install Visual C++ Redistributable from Microsoft

### macOS Issues

**Problem**: "Brobot Runner is damaged and can't be opened"
- **Solution**: The app may have lost its quarantine attribute. Run:
  ```bash
  xattr -cr /Applications/Brobot\ Runner.app
  ```

**Problem**: Permissions errors
- **Solution**: Grant necessary permissions in System Preferences > Security & Privacy

### Linux Issues

**Problem**: Missing dependencies
- **Solution**: Install required libraries:
  ```bash
  # Debian/Ubuntu
  sudo apt-get install libgtk-3-0 libglib2.0-0
  
  # Fedora
  sudo dnf install gtk3 glib2
  ```

**Problem**: GPU acceleration issues
- **Solution**: Try software rendering:
  ```bash
  export LIBGL_ALWAYS_SOFTWARE=1
  brobot-runner
  ```

### General Issues

**Problem**: Application won't start
- **Check**: Java 21+ is properly installed
- **Try**: Running from terminal to see error messages
- **Check**: Available disk space and memory

**Problem**: Slow performance
- **Increase memory**: Edit launcher settings to increase heap size
- **Check**: Antivirus software may be scanning operations
- **Disable**: Unnecessary visual effects in Settings

## Getting Help

If you encounter issues during installation:

1. Check the [Troubleshooting Guide](TROUBLESHOOTING.md)
2. Search existing [GitHub Issues](https://github.com/jspinak/brobot/issues)
3. Create a new issue with:
   - Your operating system and version
   - Installation method used
   - Complete error messages
   - Steps to reproduce the issue

## Next Steps

After successful installation:
1. Read the [User Manual](USER_MANUAL.md) to learn how to use Brobot Runner
2. Review [Configuration Format](CONFIG_FORMAT.md) documentation
3. Try the [Example Configurations](../examples/README.md)
4. Join our community discussions

---

Thank you for installing Brobot Runner! We hope it helps streamline your automation workflows.