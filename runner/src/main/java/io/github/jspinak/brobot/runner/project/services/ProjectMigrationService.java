package io.github.jspinak.brobot.runner.project.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for migrating projects between different versions. Handles schema changes and
 * data transformations.
 */
@Slf4j
@Service
public class ProjectMigrationService implements DiagnosticCapable {

    private static final String PROJECT_FILE_NAME = "project.json";
    private static final String CURRENT_VERSION = "2.0.0";
    private static final String VERSION_FIELD = "schemaVersion";

    private final EventBus eventBus;
    private final ObjectMapper objectMapper;

    private final Map<String, MigrationStrategy> migrationStrategies = new HashMap<>();
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    private final AtomicLong migrationCount = new AtomicLong(0);

    public ProjectMigrationService(EventBus eventBus) {
        this.eventBus = eventBus;
        this.objectMapper = new ObjectMapper();

        // Register migration strategies
        registerMigrationStrategies();
    }

    /** Checks if a project needs migration. */
    public boolean isMigrationNeeded(Path projectPath) {
        try {
            Path projectFile = projectPath.resolve(PROJECT_FILE_NAME);
            if (!Files.exists(projectFile)) {
                return false;
            }

            JsonNode root = objectMapper.readTree(projectFile.toFile());
            String version = getProjectVersion(root);

            return !CURRENT_VERSION.equals(version);

        } catch (IOException e) {
            log.error("Failed to check migration status for: {}", projectPath, e);
            return false;
        }
    }

    /** Migrates a project to the current version. */
    public void migrateProject(Path projectPath) {
        migrationCount.incrementAndGet();
        log.info("Starting migration for project at: {}", projectPath);

        try {
            Path projectFile = projectPath.resolve(PROJECT_FILE_NAME);

            // Create backup before migration
            createBackup(projectFile);

            // Load project data
            JsonNode root = objectMapper.readTree(projectFile.toFile());
            String fromVersion = getProjectVersion(root);

            // Apply migrations sequentially
            JsonNode migrated = applyMigrations(root, fromVersion, CURRENT_VERSION);

            // Save migrated project
            objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(projectFile.toFile(), migrated);

            log.info("Migration completed: {} -> {}", fromVersion, CURRENT_VERSION);
            eventBus.publish(
                    LogEvent.info(
                            this,
                            String.format(
                                    "Project migrated from %s to %s", fromVersion, CURRENT_VERSION),
                            "Migration"));

        } catch (Exception e) {
            log.error("Migration failed for project at: {}", projectPath, e);
            eventBus.publish(
                    LogEvent.error(this, "Migration failed: " + e.getMessage(), "Migration", e));

            // Attempt to restore from backup
            restoreFromBackup(projectPath);
            throw new RuntimeException("Project migration failed", e);
        }
    }

    /** Gets the version of a project. */
    private String getProjectVersion(JsonNode root) {
        if (root.has(VERSION_FIELD)) {
            return root.get(VERSION_FIELD).asText();
        }

        // Legacy detection - if no version field, it's pre-1.0.0
        if (!root.has("id") && root.has("projectName")) {
            return "0.9.0";
        }

        return "1.0.0";
    }

    /** Applies migrations sequentially from one version to another. */
    private JsonNode applyMigrations(JsonNode root, String fromVersion, String toVersion) {
        List<String> migrationPath = getMigrationPath(fromVersion, toVersion);

        JsonNode current = root.deepCopy();
        String currentVersion = fromVersion;

        for (String nextVersion : migrationPath) {
            String key = currentVersion + "->" + nextVersion;
            MigrationStrategy strategy = migrationStrategies.get(key);

            if (strategy != null) {
                log.debug("Applying migration: {}", key);
                current = strategy.migrate(current);
                currentVersion = nextVersion;
            } else {
                log.warn("No migration strategy found for: {}", key);
            }
        }

        // Set the final version
        ((ObjectNode) current).put(VERSION_FIELD, toVersion);

        return current;
    }

    /** Determines the migration path between versions. */
    private List<String> getMigrationPath(String fromVersion, String toVersion) {
        // Simple linear progression for now
        List<String> versions = Arrays.asList("0.9.0", "1.0.0", "1.1.0", "2.0.0");

        int fromIndex = versions.indexOf(fromVersion);
        int toIndex = versions.indexOf(toVersion);

        if (fromIndex == -1 || toIndex == -1 || fromIndex >= toIndex) {
            return Collections.emptyList();
        }

        return versions.subList(fromIndex + 1, toIndex + 1);
    }

    /** Registers all migration strategies. */
    private void registerMigrationStrategies() {
        // 0.9.0 -> 1.0.0: Add ID field, restructure project
        migrationStrategies.put(
                "0.9.0->1.0.0",
                new MigrationStrategy() {
                    @Override
                    public JsonNode migrate(JsonNode root) {
                        ObjectNode node = (ObjectNode) root;

                        // Add ID if missing
                        if (!node.has("id")) {
                            node.put("id", UUID.randomUUID().toString());
                        }

                        // Rename projectName to name
                        if (node.has("projectName") && !node.has("name")) {
                            node.put("name", node.get("projectName").asText());
                            node.remove("projectName");
                        }

                        // Add timestamps if missing
                        if (!node.has("createdAt")) {
                            node.put("createdAt", LocalDateTime.now().toString());
                        }
                        if (!node.has("lastModified")) {
                            node.put("lastModified", LocalDateTime.now().toString());
                        }

                        // Add state if missing
                        if (!node.has("state")) {
                            node.put("state", "CONFIGURED");
                        }

                        return node;
                    }
                });

        // 1.0.0 -> 1.1.0: Add automation definition structure
        migrationStrategies.put(
                "1.0.0->1.1.0",
                new MigrationStrategy() {
                    @Override
                    public JsonNode migrate(JsonNode root) {
                        ObjectNode node = (ObjectNode) root;

                        // Create automation structure if missing
                        if (!node.has("automation")) {
                            ObjectNode automation = objectMapper.createObjectNode();

                            // Move existing buttons if any
                            if (node.has("buttons")) {
                                automation.set("buttons", node.get("buttons"));
                                node.remove("buttons");
                            } else {
                                automation.putArray("buttons");
                            }

                            // Add default values
                            automation.put("mainClass", "");
                            automation.putObject("settings");
                            automation.putArray("dependencies");

                            node.set("automation", automation);
                        }

                        return node;
                    }
                });

        // 1.1.0 -> 2.0.0: Add project paths structure
        migrationStrategies.put(
                "1.1.0->2.0.0",
                new MigrationStrategy() {
                    @Override
                    public JsonNode migrate(JsonNode root) {
                        ObjectNode node = (ObjectNode) root;

                        // Ensure path fields exist
                        if (!node.has("configPath")) {
                            node.put("configPath", "./config");
                        }
                        if (!node.has("imagePath")) {
                            node.put("imagePath", "./images");
                        }
                        if (!node.has("dataPath")) {
                            node.put("dataPath", "./data");
                        }

                        // Add history array
                        if (!node.has("history")) {
                            node.putArray("history");
                        }

                        // Add runtime data object
                        if (!node.has("runtimeData")) {
                            node.putObject("runtimeData");
                        }

                        return node;
                    }
                });
    }

    /** Creates a backup of the project file. */
    private void createBackup(Path projectFile) throws IOException {
        if (!Files.exists(projectFile)) {
            return;
        }

        String timestamp =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path backupFile = projectFile.resolveSibling(PROJECT_FILE_NAME + ".backup." + timestamp);

        Files.copy(projectFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
        log.info("Created migration backup: {}", backupFile);
    }

    /** Restores from the most recent backup. */
    private void restoreFromBackup(Path projectPath) {
        try {
            Path projectFile = projectPath.resolve(PROJECT_FILE_NAME);

            // Find most recent backup
            Optional<Path> backup =
                    Files.list(projectPath)
                            .filter(
                                    p ->
                                            p.getFileName()
                                                    .toString()
                                                    .startsWith(PROJECT_FILE_NAME + ".backup."))
                            .max(Comparator.comparing(p -> p.getFileName().toString()));

            if (backup.isPresent()) {
                Files.copy(backup.get(), projectFile, StandardCopyOption.REPLACE_EXISTING);
                log.info("Restored project from backup: {}", backup.get());
            } else {
                log.error("No backup found to restore");
            }

        } catch (IOException e) {
            log.error("Failed to restore from backup", e);
        }
    }

    /** Validates a migrated project structure. */
    public boolean validateMigratedProject(Path projectPath) {
        try {
            Path projectFile = projectPath.resolve(PROJECT_FILE_NAME);
            JsonNode root = objectMapper.readTree(projectFile.toFile());

            // Check required fields
            return root.has("id")
                    && root.has("name")
                    && root.has("state")
                    && root.has("automation")
                    && root.has(VERSION_FIELD)
                    && CURRENT_VERSION.equals(root.get(VERSION_FIELD).asText());

        } catch (IOException e) {
            log.error("Failed to validate migrated project", e);
            return false;
        }
    }

    /** Gets migration history for diagnostics. */
    public List<String> getMigrationHistory() {
        return new ArrayList<>(migrationStrategies.keySet());
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> diagnosticStates = new HashMap<>();
        diagnosticStates.put("status", "Active");
        diagnosticStates.put("currentSchemaVersion", CURRENT_VERSION);
        diagnosticStates.put("registeredMigrations", migrationStrategies.size());
        diagnosticStates.put("totalMigrations", migrationCount.get());

        return DiagnosticInfo.builder()
                .component("ProjectMigrationService")
                .states(diagnosticStates)
                .build();
    }

    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }

    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);
        log.debug("Diagnostic mode {}", enabled ? "enabled" : "disabled");
    }

    /** Migration strategy interface. */
    private interface MigrationStrategy {
        JsonNode migrate(JsonNode root);
    }
}
