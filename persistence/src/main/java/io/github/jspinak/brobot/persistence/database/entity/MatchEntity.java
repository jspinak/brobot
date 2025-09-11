package io.github.jspinak.brobot.persistence.database.entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** JPA entity for match results. */
@Entity
@Table(name = "match_results")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"actionRecord"})
@ToString(exclude = {"actionRecord"})
public class MatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_record_id", nullable = false)
    private ActionRecordEntity actionRecord;

    private int x;
    private int y;
    private int width;
    private int height;

    @Column(name = "sim_score")
    private double simScore;

    @Column(name = "match_index")
    private int matchIndex;

    private String name;
}
