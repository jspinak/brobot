package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.manageStates.JavaStateTransition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Transitions are comprised of methods. To save them in a database, these methods need to be
 * represented by objects. This class is the object representation of transition methods.
 */
@Entity
@Table(name = "transitions")
@Getter
@Setter
public class TransitionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @Column(name = "source_state_id")
    private Long sourceStateId;

    @Column(name = "state_image_id")
    private Long stateImageId;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private ActionDefinitionEntity actionDefinition;

    @Enumerated(EnumType.STRING)
    @Column(name = "stays_visible_after_transition")
    private JavaStateTransition.StaysVisible staysVisibleAfterTransition;

    @ElementCollection
    @CollectionTable(name = "transition_enter_states", joinColumns = @JoinColumn(name = "transition_id"))
    @Column(name = "state_id")
    private Set<Long> statesToEnter = new HashSet<>(); // Renamed from 'activate'

    @ElementCollection
    @CollectionTable(name = "transition_exit_states", joinColumns = @JoinColumn(name = "transition_id"))
    @Column(name = "state_id")
    private Set<Long> statesToExit = new HashSet<>(); // Renamed from 'exit'

    @Column(name = "score")
    private int score;

    @Column(name = "times_successful")
    private int timesSuccessful;
}