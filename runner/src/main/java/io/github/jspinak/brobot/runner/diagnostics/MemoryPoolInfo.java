package io.github.jspinak.brobot.runner.diagnostics;

import lombok.Builder;

/**
 * Memory pool information data.
 */
@Builder
public record MemoryPoolInfo(
    String name,
    String type,
    long used,
    long max,
    long committed
) {}