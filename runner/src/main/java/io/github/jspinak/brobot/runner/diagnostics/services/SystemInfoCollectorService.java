package io.github.jspinak.brobot.runner.diagnostics.services;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.diagnostics.SystemInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for collecting system and JVM information.
 *
 * <p>This service gathers information about the operating system, JVM, runtime environment, and
 * system properties. It provides a comprehensive view of the system configuration and state.
 *
 * <p>Thread Safety: This class is thread-safe.
 *
 * @since 1.0.0
 */
@Slf4j
@Service
public class SystemInfoCollectorService implements DiagnosticCapable {

    private final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    // Cached system info (relatively static)
    private volatile SystemInfo cachedSystemInfo;
    private volatile long lastSystemInfoUpdate = 0;
    private static final long CACHE_DURATION_MS = 60000; // 1 minute

    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);

    @PostConstruct
    public void initialize() {
        log.info("SystemInfoCollectorService initialized");
        // Pre-cache system info
        cachedSystemInfo = buildSystemInfo();
        lastSystemInfoUpdate = System.currentTimeMillis();
    }

    /**
     * Collect comprehensive system information.
     *
     * @return system information
     */
    public SystemInfo collectSystemInfo() {
        // Use cached info if still fresh
        if (System.currentTimeMillis() - lastSystemInfoUpdate < CACHE_DURATION_MS) {
            return cachedSystemInfo;
        }

        // Refresh cache
        cachedSystemInfo = buildSystemInfo();
        lastSystemInfoUpdate = System.currentTimeMillis();

        if (diagnosticMode.get()) {
            log.info(
                    "[DIAGNOSTIC] System info refreshed - OS: {}, Java: {}, Processors: {}",
                    cachedSystemInfo.osName(),
                    cachedSystemInfo.javaVersion(),
                    cachedSystemInfo.availableProcessors());
        }

        return cachedSystemInfo;
    }

    /**
     * Collect environment variables.
     *
     * @return map of environment variables
     */
    public Map<String, String> collectEnvironmentVariables() {
        Map<String, String> env = new HashMap<>(System.getenv());

        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Collected {} environment variables", env.size());
        }

        return env;
    }

    /**
     * Collect system properties.
     *
     * @return map of system properties
     */
    public Map<String, String> collectSystemProperties() {
        Properties props = System.getProperties();
        Map<String, String> propsMap = new HashMap<>();

        props.forEach((key, value) -> propsMap.put(key.toString(), value.toString()));

        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Collected {} system properties", propsMap.size());
        }

        return propsMap;
    }

    /**
     * Get current CPU usage percentage.
     *
     * @return CPU usage (0-100) or -1 if not available
     */
    public double getCpuUsage() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            double cpuLoad =
                    ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad();
            return cpuLoad * 100;
        }
        return -1;
    }

    /**
     * Get system load average.
     *
     * @return system load average or -1 if not available
     */
    public double getSystemLoadAverage() {
        return osBean.getSystemLoadAverage();
    }

    /**
     * Get JVM uptime in milliseconds.
     *
     * @return JVM uptime
     */
    public long getUptime() {
        return runtimeBean.getUptime();
    }

    /**
     * Get JVM start time.
     *
     * @return start time as Date
     */
    public Date getStartTime() {
        return new Date(runtimeBean.getStartTime());
    }

    /**
     * Get JVM input arguments.
     *
     * @return list of JVM arguments
     */
    public List<String> getJvmArguments() {
        return runtimeBean.getInputArguments();
    }

    /**
     * Get class path.
     *
     * @return class path
     */
    public String getClassPath() {
        return runtimeBean.getClassPath();
    }

    /**
     * Check if running in a containerized environment.
     *
     * @return true if likely running in a container
     */
    public boolean isContainerized() {
        // Check for common container indicators
        Map<String, String> env = System.getenv();
        return env.containsKey("KUBERNETES_SERVICE_HOST")
                || env.containsKey("DOCKER_CONTAINER")
                || new java.io.File("/.dockerenv").exists()
                || new java.io.File("/run/.containerenv").exists();
    }

    /**
     * Get available disk space for the current working directory.
     *
     * @return available disk space in bytes
     */
    public long getAvailableDiskSpace() {
        java.io.File workingDir = new java.io.File(".");
        return workingDir.getFreeSpace();
    }

    /**
     * Get total disk space for the current working directory.
     *
     * @return total disk space in bytes
     */
    public long getTotalDiskSpace() {
        java.io.File workingDir = new java.io.File(".");
        return workingDir.getTotalSpace();
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        SystemInfo sysInfo = collectSystemInfo();

        Map<String, Object> states = new HashMap<>();
        states.put("os.name", sysInfo.osName());
        states.put("os.version", sysInfo.osVersion());
        states.put("os.arch", sysInfo.osArch());
        states.put("java.version", sysInfo.javaVersion());
        states.put("java.vendor", sysInfo.javaVendor());
        states.put("jvm.name", sysInfo.jvmName());
        states.put("jvm.version", sysInfo.jvmVersion());
        states.put("processors", sysInfo.availableProcessors());
        states.put("uptime.ms", sysInfo.uptime());
        states.put("cpu.usage", getCpuUsage());
        states.put("load.average", sysInfo.systemLoadAverage());
        states.put("containerized", isContainerized());
        states.put("disk.free.bytes", getAvailableDiskSpace());
        states.put("disk.total.bytes", getTotalDiskSpace());

        return DiagnosticInfo.builder()
                .component("SystemInfoCollectorService")
                .states(states)
                .build();
    }

    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }

    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);
        log.info(
                "Diagnostic mode {} for SystemInfoCollectorService",
                enabled ? "enabled" : "disabled");
    }

    private SystemInfo buildSystemInfo() {
        return SystemInfo.builder()
                .osName(System.getProperty("os.name"))
                .osVersion(System.getProperty("os.version"))
                .osArch(System.getProperty("os.arch"))
                .javaVersion(System.getProperty("java.version"))
                .javaVendor(System.getProperty("java.vendor"))
                .jvmName(runtimeBean.getVmName())
                .jvmVersion(runtimeBean.getVmVersion())
                .availableProcessors(osBean.getAvailableProcessors())
                .systemLoadAverage(osBean.getSystemLoadAverage())
                .uptime(runtimeBean.getUptime())
                .startTime(new Date(runtimeBean.getStartTime()))
                .build();
    }
}
