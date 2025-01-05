package io.github.jspinak.brobot.app.database.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "action_definitions")
@Getter
@Setter
public class ActionDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;

    @Column(name = "action_type")
    private String actionType;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "action_definition_id")
    private List<ActionStepEntity> steps = new ArrayList<>();

    public void addStepEntity(ActionOptionsEntity actionOptionsEntity,
                              ObjectCollectionEntity objectCollectionEntity) {
        steps.add(new ActionStepEntity(actionOptionsEntity, objectCollectionEntity));
    }

}
