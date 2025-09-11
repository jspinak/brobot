package io.github.jspinak.brobot.runner.project.services;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.*;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.project.ProjectDefinition;
import io.github.jspinak.brobot.runner.project.ProjectInfo;
import io.github.jspinak.brobot.runner.project.RecentProject;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for discovering and indexing automation projects. Scans configured
 * directories for valid projects and maintains an index.
 */
@Slf4j
@Service
public class ProjectDiscoveryService implements DiagnosticCapable {

    private static final String PROJECT_FILE_NAME = "project.json";
    private static final int MAX_RECENT_PROJECTS = 10;

    private final EventBus eventBus;
    private final ProjectPersistenceService persistenceService;

    @Value("${brobot.runner.projects.scan-paths:./projects,~/brobot-projects}")
    private List<String> scanPaths;

    @Value("${brobot.runner.projects.auto-scan:true}")
    private boolean autoScan;

    private final Map<String, ProjectInfo> discoveredProjects = new ConcurrentHashMap<>();
    private final Map<String, RecentProject> recentProjects = new ConcurrentHashMap<>();

    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    private final AtomicLong scanCount = new AtomicLong(0);
    private LocalDateTime lastScanTime;

    public ProjectDiscoveryService(
            EventBus eventBus, ProjectPersistenceService persistenceService) {
        this.eventBus = eventBus;
        this.persistenceService = persistenceService;
    }

    /** Scans configured directories for automation projects. */
    public void scanForProjects() {
        scanCount.incrementAndGet();
        log.info("Starting project discovery scan");

        discoveredProjects.clear();
        int foundCount = 0;

        for (String scanPath : scanPaths) {
            Path path = resolvePath(scanPath);
            if (path != null && Files.exists(path)) {
                foundCount += scanDirectory(path);
            }
        }

        lastScanTime = LocalDateTime.now();

        log.info("Project discovery completed. Found {} projects", foundCount);
        eventBus.publish(
                LogEvent.info(
                        this, String.format("Discovered %d projects", foundCount), "Discovery"));
    }

    /** Scans a directory for projects. */
    private int scanDirectory(Path directory) {
        int count = 0;

        try {
            Files.walkFileTree(
                    directory,
                    EnumSet.noneOf(FileVisitOption.class),
                    3,
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (PROJECT_FILE_NAME.equals(file.getFileName().toString())) {
                                processProjectFile(file);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) {
                            log.debug("Failed to visit: {}", file, exc);
                            return FileVisitResult.CONTINUE;
                        }
                    });

        } catch (IOException e) {
            log.error("Failed to scan directory: {}", directory, e);
        }

        return count;
    }

    /** Processes a found project file. */
    private void processProjectFile(Path projectFile) {
        try {
            Path projectPath = projectFile.getParent();
            Optional<ProjectDefinition> projectOpt = persistenceService.loadProject(projectPath);

            if (projectOpt.isPresent()) {
                ProjectDefinition project = projectOpt.get();
                ProjectInfo info = createProjectInfo(project, projectPath);
                discoveredProjects.put(info.getId(), info);

                log.debug("Discovered project: {} at {}", info.getName(), projectPath);
            }

        } catch (Exception e) {
            log.warn("Failed to process project file: {}", projectFile, e);
        }
    }

    /** Creates project info from a project. */
    private ProjectInfo createProjectInfo(ProjectDefinition project, Path projectPath) {
        long size = calculateProjectSize(projectPath);

        return ProjectInfo.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .version(project.getVersion())
                .projectPath(projectPath)
                .lastModified(project.getLastModified())
                .lastAccessed(project.getLastExecuted())
                .sizeInBytes(size)
                .valid(true)
                .build();
    }

    /** Calculates the total size of a project directory. */
    private long calculateProjectSize(Path projectPath) {
        try {
            return Files.walk(projectPath)
                    .filter(Files::isRegularFile)
                    .mapToLong(
                            path -> {
                                try {
                                    return Files.size(path);
                                } catch (IOException e) {
                                    return 0;
                                }
                            })
                    .sum();
        } catch (IOException e) {
            log.debug("Failed to calculate project size: {}", projectPath, e);
            return 0;
        }
    }

    /** Gets all discovered projects. */
    public List<ProjectInfo> getDiscoveredProjects() {
        return new ArrayList<>(discoveredProjects.values());
    }

    /** Gets discovered projects sorted by last modified date. */
    public List<ProjectInfo> getDiscoveredProjectsSorted() {
        return discoveredProjects.values().stream()
                .sorted(Comparator.comparing(ProjectInfo::getLastModified).reversed())
                .collect(Collectors.toList());
    }

    /** Finds a project by ID. */
    public Optional<ProjectInfo> findProjectById(String projectId) {
        return Optional.ofNullable(discoveredProjects.get(projectId));
    }

    /** Finds projects by name pattern. */
    public List<ProjectInfo> findProjectsByName(String namePattern) {
        String pattern = namePattern.toLowerCase();
        return discoveredProjects.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(pattern))
                .collect(Collectors.toList());
    }

    /** Records project access for recent projects tracking. */
    public void recordProjectAccess(String projectId, String projectName, Path projectPath) {
        RecentProject recent = recentProjects.get(projectId);

        if (recent == null) {
            recent =
                    RecentProject.builder()
                            .id(projectId)
                            .name(projectName)
                            .projectPath(projectPath)
                            .lastAccessed(LocalDateTime.now())
                            .accessCount(1)
                            .pinned(false)
                            .exists(true)
                            .build();
        } else {
            recent.setLastAccessed(LocalDateTime.now());
            recent.setAccessCount(recent.getAccessCount() + 1);
        }

        recentProjects.put(projectId, recent);

        // Trim recent projects list
        trimRecentProjects();
    }

    /** Gets recent projects. */
    public List<RecentProject> getRecentProjects() {
        return recentProjects.values().stream()
                .sorted(Comparator.comparing(RecentProject::getLastAccessed).reversed())
                .limit(MAX_RECENT_PROJECTS)
                .collect(Collectors.toList());
    }

    /** Pins a project to keep it in recent list. */
    public void pinProject(String projectId) {
        RecentProject recent = recentProjects.get(projectId);
        if (recent != null) {
            recent.setPinned(true);
        }
    }

    /** Unpins a project. */
    public void unpinProject(String projectId) {
        RecentProject recent = recentProjects.get(projectId);
        if (recent != null) {
            recent.setPinned(false);
        }
    }

    /** Clears recent projects history. */
    public void clearRecentProjects() {
        recentProjects.entrySet().removeIf(e -> !e.getValue().isPinned());
    }

    /** Validates and updates recent projects. */
    public void validateRecentProjects() {
        for (RecentProject recent : recentProjects.values()) {
            recent.setExists(Files.exists(recent.getProjectPath()));
        }
    }

    /** Trims recent projects to maximum count. */
    private void trimRecentProjects() {
        if (recentProjects.size() <= MAX_RECENT_PROJECTS) {
            return;
        }

        List<Map.Entry<String, RecentProject>> sorted =
                recentProjects.entrySet().stream()
                        .filter(e -> !e.getValue().isPinned())
                        .sorted(
                                Map.Entry.comparingByValue(
                                        Comparator.comparing(RecentProject::getLastAccessed)))
                        .collect(Collectors.toList());

        int toRemove = recentProjects.size() - MAX_RECENT_PROJECTS;
        for (int i = 0; i < toRemove && i < sorted.size(); i++) {
            recentProjects.remove(sorted.get(i).getKey());
        }
    }

    /** Resolves a path string to an actual path. */
    private Path resolvePath(String pathStr) {
        try {
            if (pathStr.startsWith("~")) {
                String home = System.getProperty("user.home");
                pathStr = home + pathStr.substring(1);
            }
            return Paths.get(pathStr).toAbsolutePath().normalize();
        } catch (InvalidPathException e) {
            log.warn("Invalid path: {}", pathStr, e);
            return null;
        }
    }

    /** Adds a custom scan path. */
    public void addScanPath(String path) {
        if (!scanPaths.contains(path)) {
            scanPaths.add(path);
            log.info("Added scan path: {}", path);
        }
    }

    /** Removes a scan path. */
    public void removeScanPath(String path) {
        scanPaths.remove(path);
        log.info("Removed scan path: {}", path);
    }

    /** Gets configured scan paths. */
    public List<String> getScanPaths() {
        return new ArrayList<>(scanPaths);
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> diagnosticStates = new HashMap<>();
        diagnosticStates.put("status", "Active");
        diagnosticStates.put("discoveredProjects", discoveredProjects.size());
        diagnosticStates.put("recentProjects", recentProjects.size());
        diagnosticStates.put("scanPaths", scanPaths.size());
        diagnosticStates.put("totalScans", scanCount.get());
        diagnosticStates.put(
                "lastScanTime", lastScanTime != null ? lastScanTime.toString() : "Never");
        diagnosticStates.put("autoScan", autoScan);

        return DiagnosticInfo.builder()
                .component("ProjectDiscoveryService")
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
}
