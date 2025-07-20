package io.github.jspinak.brobot.runner.diagnostics;

import lombok.Builder;

/**
 * Garbage collection information data.
 */
@Builder
public record GCInfo(
    String name,
    long collectionCount,
    long collectionTime
) {}