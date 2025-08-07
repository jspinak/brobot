package io.github.jspinak.brobot.runner.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Entity representing a match found during action execution.
 * Stores the location, size, and similarity score of pattern matches.
 */
@Entity
@Table(name = "matches", indexes = {
    @Index(name = "idx_match_action_record", columnList = "action_record_id"),
    @Index(name = "idx_match_similarity", columnList = "similarity_score")
})
@Data
@EqualsAndHashCode(exclude = "actionRecord")
@ToString(exclude = "actionRecord")
public class MatchEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * X coordinate of the match region
     */
    @Column(name = "x", nullable = false)
    private int x;
    
    /**
     * Y coordinate of the match region
     */
    @Column(name = "y", nullable = false)
    private int y;
    
    /**
     * Width of the match region
     */
    @Column(name = "width", nullable = false)
    private int width;
    
    /**
     * Height of the match region
     */
    @Column(name = "height", nullable = false)
    private int height;
    
    /**
     * Similarity score of the match (0.0 to 1.0)
     */
    @Column(name = "similarity_score")
    private double simScore;
    
    /**
     * Anchor point X coordinate (center of match)
     */
    @Column(name = "anchor_x")
    private int anchorX;
    
    /**
     * Anchor point Y coordinate (center of match)
     */
    @Column(name = "anchor_y")
    private int anchorY;
    
    /**
     * Path to screenshot of the match region
     */
    @Column(name = "screenshot_path", length = 500)
    private String screenshotPath;
    
    /**
     * Name of the pattern that was matched
     */
    @Column(name = "pattern_name")
    private String patternName;
    
    /**
     * The action record this match belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_record_id", nullable = false)
    private ActionRecordEntity actionRecord;
    
    /**
     * Constructor for creating a match from coordinates
     */
    public MatchEntity(int x, int y, int width, int height, double simScore) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.simScore = simScore;
        this.anchorX = x + width / 2;
        this.anchorY = y + height / 2;
    }
    
    /**
     * Default constructor
     */
    public MatchEntity() {
    }
    
    /**
     * Calculate and update anchor points
     */
    @PrePersist
    @PreUpdate
    protected void updateAnchors() {
        if (anchorX == 0 && anchorY == 0) {
            anchorX = x + width / 2;
            anchorY = y + height / 2;
        }
    }
    
    /**
     * Get the center point as a coordinate pair
     */
    public int[] getCenter() {
        return new int[]{anchorX, anchorY};
    }
    
    /**
     * Check if a point is within this match region
     */
    public boolean contains(int px, int py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }
}