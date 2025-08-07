package io.github.jspinak.brobot.persistence.model;

import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.persistence.PersistenceProvider.SessionMetadata;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal data structure for managing session data in file-based persistence.
 */
@Data
@NoArgsConstructor
public class SessionData {
    private SessionMetadata metadata;
    private Path path;
    private List<ActionRecord> records = new ArrayList<>();
    private long lastFlushTime = System.currentTimeMillis();
}