package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.manageStates.JavaStateTransition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    // sourceStateId, targetStateId, and stateImageId are not mapped to StateTransition

    // useful for the web app
    @Column(name = "source_state_id")
    private Long sourceStateId;

    // this field makes it easier to create simple click transitions that activate one state
    @Column(name = "target_state_id")
    private Long targetStateId;

    // useful for the web app
    @Column(name = "state_image_id")
    private Long stateImageId;

    @OneToOne(cascade = CascadeType.ALL)
    private ActionDefinitionEntity actionDefinition;

    @Enumerated(EnumType.STRING)
    @Column(name = "stays_visible_after_transition")
    private JavaStateTransition.StaysVisible staysVisibleAfterTransition;

    @ElementCollection
    @CollectionTable(name = "transition_activate_states", joinColumns = @JoinColumn(name = "transition_id"))
    @Column(name = "state_name")
    private Set<Long> activate;

    @ElementCollection
    @CollectionTable(name = "transition_exit_states", joinColumns = @JoinColumn(name = "transition_id"))
    @Column(name = "state_name")
    private Set<Long> exit;

    @Column(name = "score")
    private int score;

    @Column(name = "times_successful")
    private int timesSuccessful;

}
