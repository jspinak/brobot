package io.github.jspinak.brobot.runner.diagnostics;

import lombok.Builder;

import java.util.Map;

/**
 * Thread diagnostic information data.
 */
@Builder  
public record ThreadDiagnosticInfo(
    int threadCount,
    int peakThreadCount,
    int daemonThreadCount,
    long totalStartedThreadCount,
    int deadlockedThreads,
    Map<Thread.State, Integer> threadStates
) {}