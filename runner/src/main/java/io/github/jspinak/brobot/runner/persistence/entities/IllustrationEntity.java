package io.github.jspinak.brobot.runner.persistence.entities;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity for storing illustration metadata in the database.
 *
 * <p>This entity tracks all generated illustrations with their metadata, enabling gallery
 * functionality, search, and analytics.
 *
 * @see io.github.jspinak.brobot.runner.ui.illustration.gallery.IllustrationGalleryService
 */
@Entity
@Table(
        name = "illustrations",
        indexes = {
            @Index(name = "idx_session_id", columnList = "sessionId"),
            @Index(name = "idx_timestamp", columnList = "timestamp"),
            @Index(name = "idx_action_type", columnList = "actionType")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IllustrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Session ID this illustration belongs to. */
    @Column(nullable = false)
    private String sessionId;

    /** Filename of the illustration image. */
    @Column(nullable = false)
    private String filename;

    /** Full file path to the illustration image. */
    @Column(nullable = false, length = 500)
    private String filePath;

    /** Type of action illustrated (FIND, CLICK, etc.). */
    @Column(nullable = false)
    private String actionType;

    /** Name of the state where the action occurred. */
    private String stateName;

    /** Whether the action was successful. */
    @Column(nullable = false)
    private boolean success;

    /** When the illustration was created. */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /** Tags for categorization and search. */
    @ElementCollection
    @CollectionTable(
            name = "illustration_tags",
            joinColumns = @JoinColumn(name = "illustration_id"))
    @Column(name = "tag")
    private Set<String> tags;

    /** Additional metadata stored as JSON. */
    @ElementCollection
    @CollectionTable(
            name = "illustration_metadata",
            joinColumns = @JoinColumn(name = "illustration_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value", length = 1000)
    private Map<String, Object> metadata;

    /** File size in bytes. */
    private Long fileSize;

    /** Image dimensions. */
    private Integer width;

    private Integer height;

    /** Creation timestamp. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Last update timestamp. */
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
