package io.github.jspinak.brobot.runner.ui.illustration.gallery;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.persistence.IllustrationRepository;
import io.github.jspinak.brobot.runner.persistence.entities.IllustrationEntity;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing the illustration gallery with web export capabilities.
 *
 * <p>This service handles:
 *
 * <ul>
 *   <li>Storing illustrations with metadata in the database
 *   <li>Organizing illustrations by sessions and tags
 *   <li>Generating static web galleries for sharing
 *   <li>Managing illustration lifecycle and cleanup
 * </ul>
 *
 * @see IllustrationRepository
 * @see GalleryWebExporter
 */
@Service
@Slf4j
public class IllustrationGalleryService {

    private final IllustrationRepository repository;

    @Value("${brobot.illustration.gallery.path:illustrations/gallery}")
    private String galleryPath;

    @Value("${brobot.illustration.gallery.max-size:1000}")
    private int maxGallerySize;

    @Autowired
    public IllustrationGalleryService(IllustrationRepository repository) {
        this.repository = repository;
        ensureGalleryDirectoryExists();
    }

    /**
     * Saves an illustration to the gallery.
     *
     * @param image the JavaFX image
     * @param metadata illustration metadata
     * @param sessionId current session ID
     * @return the saved illustration entity
     */
    public IllustrationEntity saveIllustration(
            Image image, IllustrationMetadata metadata, String sessionId) {
        try {
            // Generate filename
            String filename = generateFilename(metadata);
            Path filePath = Paths.get(galleryPath, sessionId, filename);

            // Ensure directory exists
            Files.createDirectories(filePath.getParent());

            // Save image to disk
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ImageIO.write(bufferedImage, "png", filePath.toFile());

            // Create entity
            IllustrationEntity entity =
                    IllustrationEntity.builder()
                            .sessionId(sessionId)
                            .filename(filename)
                            .filePath(filePath.toString())
                            .actionType(metadata.getActionType())
                            .stateName(metadata.getStateName())
                            .success(metadata.isSuccess())
                            .timestamp(metadata.getTimestamp())
                            .tags(new HashSet<>(metadata.getTags()))
                            .metadata(convertMetadataToMap(metadata))
                            .build();

            // Save to database
            return repository.save(entity);

        } catch (IOException e) {
            log.error("Failed to save illustration", e);
            throw new RuntimeException("Failed to save illustration", e);
        }
    }

    /**
     * Gets all illustrations for a session.
     *
     * @param sessionId the session ID
     * @return list of illustration entities
     */
    public List<IllustrationEntity> getSessionIllustrations(String sessionId) {
        return repository.findBySessionIdOrderByTimestampDesc(sessionId);
    }

    /**
     * Gets illustrations by tag.
     *
     * @param tag the tag to search for
     * @return list of illustration entities
     */
    public List<IllustrationEntity> getIllustrationsByTag(String tag) {
        return repository.findByTagsContaining(tag);
    }

    /**
     * Gets recent illustrations.
     *
     * @param limit maximum number to return
     * @return list of recent illustrations
     */
    public List<IllustrationEntity> getRecentIllustrations(int limit) {
        return repository.findTopNByOrderByTimestampDesc(limit);
    }

    /**
     * Searches illustrations by criteria.
     *
     * @param criteria search criteria
     * @return list of matching illustrations
     */
    public List<IllustrationEntity> searchIllustrations(SearchCriteria criteria) {
        // This would use a more sophisticated query builder
        // For now, simple implementation
        List<IllustrationEntity> results = repository.findAll();

        return results.stream()
                .filter(ill -> matchesCriteria(ill, criteria))
                .sorted(Comparator.comparing(IllustrationEntity::getTimestamp).reversed())
                .limit(criteria.getMaxResults())
                .collect(Collectors.toList());
    }

    /**
     * Exports a session gallery as a static website.
     *
     * @param sessionId the session to export
     * @param exportPath target directory for the web gallery
     * @return path to the generated index.html
     */
    public Path exportWebGallery(String sessionId, String exportPath) {
        List<IllustrationEntity> illustrations = getSessionIllustrations(sessionId);

        GalleryWebExporter exporter = new GalleryWebExporter();
        return exporter.exportGallery(illustrations, exportPath, sessionId);
    }

    /**
     * Adds tags to an illustration.
     *
     * @param illustrationId the illustration ID
     * @param tags tags to add
     */
    public void addTags(Long illustrationId, Set<String> tags) {
        repository
                .findById(illustrationId)
                .ifPresent(
                        entity -> {
                            entity.getTags().addAll(tags);
                            repository.save(entity);
                        });
    }

    /**
     * Deletes an illustration.
     *
     * @param illustrationId the illustration ID
     */
    public void deleteIllustration(Long illustrationId) {
        repository
                .findById(illustrationId)
                .ifPresent(
                        entity -> {
                            // Delete file
                            try {
                                Files.deleteIfExists(Paths.get(entity.getFilePath()));
                            } catch (IOException e) {
                                log.warn("Failed to delete illustration file", e);
                            }

                            // Delete from database
                            repository.delete(entity);
                        });
    }

    /** Cleans up old illustrations beyond the maximum gallery size. */
    public void cleanupOldIllustrations() {
        long totalCount = repository.count();

        if (totalCount > maxGallerySize) {
            long toDelete = totalCount - maxGallerySize;

            // Get oldest illustrations
            List<IllustrationEntity> oldestIllustrations =
                    repository.findTopNByOrderByTimestampAsc((int) toDelete);

            // Delete them
            oldestIllustrations.forEach(entity -> deleteIllustration(entity.getId()));

            log.info("Cleaned up {} old illustrations", toDelete);
        }
    }

    /**
     * Gets an illustration by ID.
     *
     * @param illustrationId the illustration ID
     * @return the illustration or empty if not found
     */
    public Optional<IllustrationEntity> getIllustrationById(Long illustrationId) {
        return repository.findById(illustrationId);
    }

    /**
     * Gets gallery statistics.
     *
     * @return gallery statistics
     */
    public GalleryStatistics getStatistics() {
        long totalCount = repository.count();
        long successCount = repository.countBySuccess(true);

        Map<String, Long> countByAction =
                repository.findAll().stream()
                        .collect(
                                Collectors.groupingBy(
                                        IllustrationEntity::getActionType, Collectors.counting()));

        Map<String, Long> countBySession =
                repository.findAll().stream()
                        .collect(
                                Collectors.groupingBy(
                                        IllustrationEntity::getSessionId, Collectors.counting()));

        return GalleryStatistics.builder()
                .totalIllustrations(totalCount)
                .successfulIllustrations(successCount)
                .illustrationsByAction(countByAction)
                .illustrationsBySession(countBySession)
                .storageUsedMB(calculateStorageUsed())
                .build();
    }

    /** Ensures the gallery directory exists. */
    private void ensureGalleryDirectoryExists() {
        try {
            Files.createDirectories(Paths.get(galleryPath));
        } catch (IOException e) {
            log.error("Failed to create gallery directory", e);
        }
    }

    /** Generates a filename for an illustration. */
    private String generateFilename(IllustrationMetadata metadata) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = metadata.getTimestamp().format(formatter);
        String action = metadata.getActionType().toLowerCase().replace(" ", "_");

        return String.format(
                "%s_%s_%s.png", timestamp, action, UUID.randomUUID().toString().substring(0, 8));
    }

    /** Converts metadata to a map for storage. */
    private Map<String, Object> convertMetadataToMap(IllustrationMetadata metadata) {
        Map<String, Object> map = new HashMap<>();

        map.put("actionType", metadata.getActionType());
        map.put("stateName", metadata.getStateName());
        map.put("success", metadata.isSuccess());

        if (metadata.getErrorMessage() != null) {
            map.put("errorMessage", metadata.getErrorMessage());
        }

        if (metadata.getPerformanceData() != null) {
            map.put("executionTimeMs", metadata.getPerformanceData().getExecutionTimeMs());
            map.put("matchesFound", metadata.getPerformanceData().getMatchesFound());
        }

        return map;
    }

    /** Checks if an illustration matches search criteria. */
    private boolean matchesCriteria(IllustrationEntity illustration, SearchCriteria criteria) {
        // Check action type
        if (criteria.getActionTypes() != null && !criteria.getActionTypes().isEmpty()) {
            if (!criteria.getActionTypes().contains(illustration.getActionType())) {
                return false;
            }
        }

        // Check success status
        if (criteria.getSuccessOnly() != null) {
            if (criteria.getSuccessOnly() && !illustration.isSuccess()) {
                return false;
            }
        }

        // Check date range
        if (criteria.getStartDate() != null) {
            if (illustration.getTimestamp().isBefore(criteria.getStartDate())) {
                return false;
            }
        }

        if (criteria.getEndDate() != null) {
            if (illustration.getTimestamp().isAfter(criteria.getEndDate())) {
                return false;
            }
        }

        // Check tags
        if (criteria.getTags() != null && !criteria.getTags().isEmpty()) {
            if (illustration.getTags() == null
                    || !illustration.getTags().containsAll(criteria.getTags())) {
                return false;
            }
        }

        return true;
    }

    /** Calculates total storage used by the gallery. */
    private long calculateStorageUsed() {
        try {
            return Files.walk(Paths.get(galleryPath))
                            .filter(Files::isRegularFile)
                            .mapToLong(
                                    path -> {
                                        try {
                                            return Files.size(path);
                                        } catch (IOException e) {
                                            return 0;
                                        }
                                    })
                            .sum()
                    / (1024 * 1024); // Convert to MB
        } catch (IOException e) {
            log.error("Failed to calculate storage used", e);
            return 0;
        }
    }

    /** Search criteria for finding illustrations. */
    @lombok.Data
    @lombok.Builder
    public static class SearchCriteria {
        private Set<String> actionTypes;
        private Boolean successOnly;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Set<String> tags;
        private String sessionId;
        @lombok.Builder.Default private int maxResults = 100;
    }

    /** Gallery statistics. */
    @lombok.Data
    @lombok.Builder
    public static class GalleryStatistics {
        private long totalIllustrations;
        private long successfulIllustrations;
        private Map<String, Long> illustrationsByAction;
        private Map<String, Long> illustrationsBySession;
        private long storageUsedMB;
    }

    /** Simplified metadata for gallery service. */
    @lombok.Data
    @lombok.Builder
    public static class IllustrationMetadata {
        private String actionType;
        private String stateName;
        private boolean success;
        private LocalDateTime timestamp;
        private String errorMessage;
        private List<String> tags;
        private PerformanceData performanceData;

        @lombok.Data
        @lombok.Builder
        public static class PerformanceData {
            private long executionTimeMs;
            private int matchesFound;
        }
    }
}
