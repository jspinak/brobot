package io.github.jspinak.brobot.runner.diagnostics;

import java.util.Map;

import lombok.Builder;

/** Memory information data. */
@Builder
public record MemoryInfo(
        long heapUsed,
        long heapMax,
        long heapCommitted,
        long nonHeapUsed,
        long nonHeapMax,
        long nonHeapCommitted,
        long freeMemory,
        long totalMemory,
        long maxMemory,
        Map<String, MemoryPoolInfo> memoryPools) {}
