package io.github.jspinak.brobot.runner.diagnostics;

import lombok.Builder;

import java.util.Date;

/**
 * System information data.
 */
@Builder
public record SystemInfo(
    String osName,
    String osVersion,
    String osArch,
    String javaVersion,
    String javaVendor,
    String jvmName,
    String jvmVersion,
    int availableProcessors,
    double systemLoadAverage,
    long uptime,
    Date startTime
) {}