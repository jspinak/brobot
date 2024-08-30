package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.manageStates.IStateTransition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "state_transitions")
@Getter
@Setter
public class StateTransitionsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "state_id", unique = true)
    private Long stateId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "state_transitions_id")
    private List<TransitionEntity> transitions = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "finish_transition_id")
    private TransitionEntity finishTransition;

    @Enumerated(EnumType.STRING)
    @Column(name = "stays_visible_after_transition")
    private IStateTransition.StaysVisible staysVisibleAfterTransition;
}