package io.github.jspinak.brobot.persistence.database.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity for action records.
 */
@Entity
@Table(name = "action_records", indexes = {
    @Index(name = "idx_session_id", columnList = "session_id"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_action_type", columnList = "action_config_type")
})
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"session", "matches"})
@ToString(exclude = {"session", "matches"})
public class ActionRecordEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private RecordingSessionEntity session;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "action_config_type")
    private String actionConfigType;
    
    @Lob
    @Column(name = "action_config_json", columnDefinition = "TEXT")
    private String actionConfigJson;
    
    @Column(name = "action_success", nullable = false)
    private boolean actionSuccess;
    
    private long duration;
    
    @Column(name = "state_name")
    private String stateName;
    
    @Column(name = "object_name")
    private String objectName;
    
    @Column(length = 500)
    private String text;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String screenshot;
    
    @OneToMany(mappedBy = "actionRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<MatchEntity> matches = new HashSet<>();
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}