#!/bin/bash

# Base paths
CONFIG_BASE="/home/jspinak/brobot_parent/brobot/library/src/main/java/io/github/jspinak/brobot/config"
STARTUP_BASE="/home/jspinak/brobot_parent/brobot/library/src/main/java/io/github/jspinak/brobot/startup"

echo "Reorganizing Brobot config and startup folders..."

# Move config files to subfolders
# Core configuration
mv "$CONFIG_BASE/BrobotConfig.java" "$CONFIG_BASE/core/" 2>/dev/null
mv "$CONFIG_BASE/BrobotConfiguration.java" "$CONFIG_BASE/core/" 2>/dev/null
mv "$CONFIG_BASE/BrobotProperties.java" "$CONFIG_BASE/core/" 2>/dev/null
mv "$CONFIG_BASE/BrobotPropertiesInitializer.java" "$CONFIG_BASE/core/" 2>/dev/null
mv "$CONFIG_BASE/BrobotPropertyVerifier.java" "$CONFIG_BASE/core/" 2>/dev/null
mv "$CONFIG_BASE/BrobotDefaultsConfiguration.java" "$CONFIG_BASE/core/" 2>/dev/null
mv "$CONFIG_BASE/FrameworkSettings.java" "$CONFIG_BASE/core/" 2>/dev/null
mv "$CONFIG_BASE/FrameworkSettingsConfig.java" "$CONFIG_BASE/core/" 2>/dev/null
mv "$CONFIG_BASE/FrameworkInitializer.java" "$CONFIG_BASE/core/" 2>/dev/null
mv "$CONFIG_BASE/SpringConfiguration.java" "$CONFIG_BASE/core/" 2>/dev/null
mv "$CONFIG_BASE/BrobotAutoConfiguration.java" "$CONFIG_BASE/core/" 2>/dev/null
mv "$CONFIG_BASE/ImagePathManager.java" "$CONFIG_BASE/core/" 2>/dev/null
mv "$CONFIG_BASE/EarlyImagePathInitializer.java" "$CONFIG_BASE/core/" 2>/dev/null
mv "$CONFIG_BASE/SmartImageLoader.java" "$CONFIG_BASE/core/" 2>/dev/null

# DPI configuration
mv "$CONFIG_BASE/BrobotDPIAutoDetector.java" "$CONFIG_BASE/dpi/" 2>/dev/null
mv "$CONFIG_BASE/BrobotDPIConfiguration.java" "$CONFIG_BASE/dpi/" 2>/dev/null
mv "$CONFIG_BASE/AutoScalingConfiguration.java" "$CONFIG_BASE/dpi/" 2>/dev/null

# Logging configuration
mv "$CONFIG_BASE/ActionLoggingConfig.java" "$CONFIG_BASE/logging/" 2>/dev/null
mv "$CONFIG_BASE/LoggingConfiguration.java" "$CONFIG_BASE/logging/" 2>/dev/null
mv "$CONFIG_BASE/LoggingVerbosityConfig.java" "$CONFIG_BASE/logging/" 2>/dev/null
mv "$CONFIG_BASE/SikuliXLoggingConfig.java" "$CONFIG_BASE/logging/" 2>/dev/null
mv "$CONFIG_BASE/VisualFeedbackConfiguration.java" "$CONFIG_BASE/logging/" 2>/dev/null

# Mock configuration
mv "$CONFIG_BASE/MockConfiguration.java" "$CONFIG_BASE/mock/" 2>/dev/null
mv "$CONFIG_BASE/MockModeManager.java" "$CONFIG_BASE/mock/" 2>/dev/null

# Environment configuration
mv "$CONFIG_BASE/ExecutionEnvironment.java" "$CONFIG_BASE/environment/" 2>/dev/null
mv "$CONFIG_BASE/ExecutionEnvironmentConfig.java" "$CONFIG_BASE/environment/" 2>/dev/null
mv "$CONFIG_BASE/ExecutionMode.java" "$CONFIG_BASE/environment/" 2>/dev/null
mv "$CONFIG_BASE/HeadlessDiagnostics.java" "$CONFIG_BASE/environment/" 2>/dev/null
mv "$CONFIG_BASE/NotHeadlessCondition.java" "$CONFIG_BASE/environment/" 2>/dev/null
mv "$CONFIG_BASE/BrobotProfileAutoConfiguration.java" "$CONFIG_BASE/environment/" 2>/dev/null
mv "$CONFIG_BASE/ConfigurationDiagnostics.java" "$CONFIG_BASE/environment/" 2>/dev/null
mv "$CONFIG_BASE/DiagnosticsConfiguration.java" "$CONFIG_BASE/environment/" 2>/dev/null

# Files that should move to startup
mv "$CONFIG_BASE/BrobotLifecycleManager.java" "$STARTUP_BASE/orchestration/" 2>/dev/null
mv "$CONFIG_BASE/FrameworkLifecycleManager.java" "$STARTUP_BASE/orchestration/" 2>/dev/null
mv "$CONFIG_BASE/EventListenerConfiguration.java" "$STARTUP_BASE/orchestration/" 2>/dev/null

# Move startup files to subfolders
# Orchestration
mv "$STARTUP_BASE/BrobotInitializationOrchestrator.java" "$STARTUP_BASE/orchestration/" 2>/dev/null
mv "$STARTUP_BASE/BrobotStartupRunner.java" "$STARTUP_BASE/orchestration/" 2>/dev/null
mv "$STARTUP_BASE/BrobotApplicationContextInitializer.java" "$STARTUP_BASE/orchestration/" 2>/dev/null
mv "$STARTUP_BASE/BrobotStartupConfiguration.java" "$STARTUP_BASE/orchestration/" 2>/dev/null

# Verification
mv "$STARTUP_BASE/ApplicationStartupVerifier.java" "$STARTUP_BASE/verification/" 2>/dev/null
mv "$STARTUP_BASE/AutoStartupVerifier.java" "$STARTUP_BASE/verification/" 2>/dev/null
mv "$STARTUP_BASE/InitialStateVerifier.java" "$STARTUP_BASE/verification/" 2>/dev/null
mv "$STARTUP_BASE/ListenerRegistrationVerifier.java" "$STARTUP_BASE/verification/" 2>/dev/null

# State management
mv "$STARTUP_BASE/InitialStateAutoConfiguration.java" "$STARTUP_BASE/state/" 2>/dev/null

echo "Reorganization complete!"
echo ""
echo "New structure:"
echo "config/"
echo "  ├── core/          - Core framework settings"
echo "  ├── dpi/           - DPI and display scaling"
echo "  ├── logging/       - Logging configuration"
echo "  ├── mock/          - Mock mode configuration"
echo "  └── environment/   - Environment detection & setup"
echo ""
echo "startup/"
echo "  ├── orchestration/ - Startup orchestration & lifecycle"
echo "  ├── verification/  - Startup verification & diagnostics"
echo "  └── state/         - Initial state management"