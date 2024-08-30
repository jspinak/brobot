package io.github.jspinak.brobot.app.database.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "action_steps")
@Getter
@Setter
public class ActionStepEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private ActionOptionsEntity actionOptions;

    @OneToOne(cascade = CascadeType.ALL)
    private ObjectCollectionEntity objectCollection;

}
