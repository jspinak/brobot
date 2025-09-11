package io.github.jspinak.brobot.runner.diagnostics;

import java.util.Map;

import lombok.Builder;

/** Thread diagnostic information data. */
@Builder
public record ThreadDiagnosticInfo(
        int threadCount,
        int peakThreadCount,
        int daemonThreadCount,
        long totalStartedThreadCount,
        int deadlockedThreads,
        Map<Thread.State, Integer> threadStates) {}
