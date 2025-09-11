package io.github.jspinak.brobot.runner.diagnostics;

import java.util.Date;

import lombok.Builder;

/** System information data. */
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
        Date startTime) {}
