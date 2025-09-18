package io.github.jspinak.brobot.monitor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.core.BrobotProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Manages multi-monitor support for Brobot automation framework. Provides methods to detect,
 * select, and work with multiple monitors.
 */
@Slf4j
@Component
public class MonitorManager {

    private final BrobotProperties properties;
    private final Map<Integer, MonitorInfo> monitorCache = new ConcurrentHashMap<>();
    private final Map<String, Integer> operationMonitorMap = new ConcurrentHashMap<>();
    private boolean headlessMode = false;
    private int primaryMonitorIndex = 0;

    public MonitorManager(BrobotProperties properties) {
        this.properties = properties;
        initializeMonitors();
        if (properties.getMonitor().getOperationMonitorMap() != null) {
            operationMonitorMap.putAll(properties.getMonitor().getOperationMonitorMap());
        }
    }

    /** Detect the primary monitor based on position and Windows settings */
    private int detectPrimaryMonitor(GraphicsDevice[] devices, GraphicsEnvironment ge) {
        // Method 1: Check if GraphicsEnvironment default device matches
        GraphicsDevice defaultDevice = ge.getDefaultScreenDevice();
        for (int i = 0; i < devices.length; i++) {
            if (devices[i].equals(defaultDevice)) {
                return i; // Primary monitor is default screen device
            }
        }

        // Method 2: Find monitor closest to (0,0) - typically the primary
        int closestIndex = 0;
        double closestDistance = Double.MAX_VALUE;

        for (int i = 0; i < devices.length; i++) {
            Rectangle bounds = devices[i].getDefaultConfiguration().getBounds();
            // Calculate distance from (0,0)
            double distance = Math.sqrt(bounds.x * bounds.x + bounds.y * bounds.y);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestIndex = i;
            }
        }

        return closestIndex; // Primary monitor is closest to (0,0)
    }

    /** Initialize monitor information and cache available monitors */
    private void initializeMonitors() {
        // Check if we should preserve the headless setting
        String preserveHeadless = System.getProperty("brobot.preserve.headless.setting");
        if (!"true".equals(preserveHeadless)) {
            // For GUI automation, ensure headless is false before checking
            String currentHeadless = System.getProperty("java.awt.headless");
            if ("true".equals(currentHeadless)) {
                System.setProperty("java.awt.headless", "false");
                log.info(
                        "Reset java.awt.headless from 'true' to 'false' for GUI automation in"
                                + " MonitorManager");
            }
        }

        // Now check if headless mode is still forced
        String headlessProperty = System.getProperty("java.awt.headless");
        boolean forcedHeadless = "true".equalsIgnoreCase(headlessProperty);

        if (forcedHeadless) {
            log.info("Headless mode forced via java.awt.headless=true property");
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        // Check if running in headless mode
        if (ge.isHeadlessInstance() || GraphicsEnvironment.isHeadless()) {
            log.warn("Running in headless mode. Monitor detection disabled.");
            headlessMode = true;
            // Create a default monitor for headless mode
            Rectangle bounds = new Rectangle(0, 0, 1920, 1080); // Default resolution
            MonitorInfo info = new MonitorInfo(0, bounds, "headless-default");
            monitorCache.put(0, info);
            return;
        }

        try {
            GraphicsDevice[] devices = ge.getScreenDevices();

            if (devices == null || devices.length == 0) {
                log.warn("No screen devices found. Creating default monitor.");
                Rectangle bounds = new Rectangle(0, 0, 1920, 1080);
                MonitorInfo info = new MonitorInfo(0, bounds, "default-monitor");
                monitorCache.put(0, info);
                return;
            }

            // Collect all monitor info
            StringBuilder monitorSummary = new StringBuilder();
            for (int i = 0; i < devices.length; i++) {
                GraphicsDevice device = devices[i];
                Rectangle bounds = device.getDefaultConfiguration().getBounds();
                MonitorInfo info = new MonitorInfo(i, bounds, device.getIDstring());
                monitorCache.put(i, info);

                if (monitorSummary.length() > 0) monitorSummary.append(", ");
                monitorSummary.append(
                        String.format("Monitor %d: %dx%d", i, bounds.width, bounds.height));
            }
            log.info(monitorSummary.toString());

            // Second pass: determine the primary monitor
            primaryMonitorIndex = detectPrimaryMonitor(devices, ge);

            if (devices.length > 1 && !properties.getMonitor().isMultiMonitorEnabled()) {
                log.warn(
                        "Multiple monitors detected but multi-monitor support is disabled. Enable"
                            + " it in configuration: brobot.monitor.multi-monitor-enabled=true");
            }
        } catch (HeadlessException e) {
            log.warn("HeadlessException caught. Creating default monitor for headless mode.", e);
            // Create a default monitor for headless mode
            Rectangle bounds = new Rectangle(0, 0, 1920, 1080); // Default resolution
            MonitorInfo info = new MonitorInfo(0, bounds, "headless-default");
            monitorCache.put(0, info);
        } catch (Exception e) {
            log.error(
                    "Unexpected error during monitor initialization. Creating default monitor.", e);
            Rectangle bounds = new Rectangle(0, 0, 1920, 1080);
            MonitorInfo info = new MonitorInfo(0, bounds, "error-default");
            monitorCache.put(0, info);
        }
    }

    /**
     * Get Screen object for specified monitor index
     *
     * @param monitorIndex The monitor index (0-based)
     * @return Screen object for the specified monitor
     */
    public Screen getScreen(int monitorIndex) {
        if (headlessMode) {
            log.debug("Running in headless mode - returning null Screen");
            return null;
        }

        if (!isValidMonitorIndex(monitorIndex)) {
            log.warn("Invalid monitor index: {}. Using primary monitor.", monitorIndex);
            try {
                return new Screen();
            } catch (Exception e) {
                log.error("Failed to create Screen: " + e.getMessage());
                headlessMode = true;
                return null;
            }
        }

        if (properties.getMonitor().isLogMonitorInfo()) {
            MonitorInfo info = monitorCache.get(monitorIndex);
            log.debug("Using monitor {}: {} for operation", monitorIndex, info.getDeviceId());
        }

        try {
            return new Screen(monitorIndex);
        } catch (Exception e) {
            log.error("Failed to create Screen for monitor {}: {}", monitorIndex, e.getMessage());
            headlessMode = true;
            return null;
        }
    }

    /**
     * Get Screen object based on configuration and operation context
     *
     * @param operationName Optional operation name for specific monitor assignment
     * @return Appropriate Screen object
     */
    public Screen getScreen(String operationName) {
        // Check if operation has specific monitor assignment
        if (operationName != null && operationMonitorMap.containsKey(operationName)) {
            int monitorIndex = operationMonitorMap.get(operationName);
            if (properties.getMonitor().isLogMonitorInfo()) {
                log.debug("Operation '{}' assigned to monitor {}", operationName, monitorIndex);
            }
            return getScreen(monitorIndex);
        }

        // Use default monitor from configuration
        int defaultIndex = properties.getMonitor().getDefaultScreenIndex();
        if (defaultIndex >= 0) {
            return getScreen(defaultIndex);
        }

        // Use detected primary monitor when defaultIndex is -1
        if (defaultIndex == -1) {
            log.debug("Using detected primary monitor: Monitor {}", primaryMonitorIndex);
            return getScreen(primaryMonitorIndex);
        }

        // Fall back to primary monitor
        try {
            return new Screen();
        } catch (Exception e) {
            log.error("Failed to create default Screen: " + e.getMessage());
            headlessMode = true;
            return null;
        }
    }

    /**
     * Get all available screens for multi-monitor search
     *
     * @return List of all Screen objects
     */
    public List<Screen> getAllScreens() {
        List<Screen> screens = new ArrayList<>();
        if (headlessMode) {
            log.debug("Running in headless mode - returning empty screen list");
            return screens;
        }

        for (int i = 0; i < getMonitorCount(); i++) {
            try {
                screens.add(new Screen(i));
            } catch (Exception e) {
                log.error("Failed to create Screen for monitor {}: {}", i, e.getMessage());
                headlessMode = true;
                return screens;
            }
        }
        return screens;
    }

    /** Check if monitor index is valid */
    public boolean isValidMonitorIndex(int index) {
        return index >= 0 && index < getMonitorCount();
    }

    /** Get total number of monitors */
    public int getMonitorCount() {
        return monitorCache.size();
    }

    /** Get the index of the primary monitor */
    public int getPrimaryMonitorIndex() {
        return primaryMonitorIndex;
    }

    /** Get monitor information */
    public MonitorInfo getMonitorInfo(int index) {
        return monitorCache.get(index);
    }

    /** Get all monitor information */
    public List<MonitorInfo> getAllMonitorInfo() {
        return new ArrayList<>(monitorCache.values());
    }

    /** Set monitor for specific operation */
    public void setOperationMonitor(String operationName, int monitorIndex) {
        if (isValidMonitorIndex(monitorIndex)) {
            operationMonitorMap.put(operationName, monitorIndex);
            log.info("Assigned operation '{}' to monitor {}", operationName, monitorIndex);
        } else {
            log.error(
                    "Cannot assign operation '{}' to invalid monitor index: {}",
                    operationName,
                    monitorIndex);
        }
    }

    /** Get the monitor containing a specific point */
    public int getMonitorAtPoint(Point point) {
        for (MonitorInfo info : monitorCache.values()) {
            if (info.getBounds().contains(point)) {
                return info.getIndex();
            }
        }
        return 0; // Default to primary if not found
    }

    /** Convert global coordinates to monitor-relative coordinates */
    public Point toMonitorCoordinates(Point globalPoint, int monitorIndex) {
        MonitorInfo info = monitorCache.get(monitorIndex);
        if (info == null) {
            return globalPoint;
        }

        Rectangle bounds = info.getBounds();
        return new Point(globalPoint.x - bounds.x, globalPoint.y - bounds.y);
    }

    /** Convert monitor-relative coordinates to global coordinates */
    public Point toGlobalCoordinates(Point monitorPoint, int monitorIndex) {
        MonitorInfo info = monitorCache.get(monitorIndex);
        if (info == null) {
            return monitorPoint;
        }

        Rectangle bounds = info.getBounds();
        return new Point(monitorPoint.x + bounds.x, monitorPoint.y + bounds.y);
    }

    /** Information about a monitor */
    public static class MonitorInfo {
        private final int index;
        private final Rectangle bounds;
        private final String deviceId;

        public MonitorInfo(int index, Rectangle bounds, String deviceId) {
            this.index = index;
            this.bounds = bounds;
            this.deviceId = deviceId;
        }

        public int getIndex() {
            return index;
        }

        public Rectangle getBounds() {
            return bounds;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public int getWidth() {
            return bounds.width;
        }

        public int getHeight() {
            return bounds.height;
        }

        public int getX() {
            return bounds.x;
        }

        public int getY() {
            return bounds.y;
        }
    }
}
