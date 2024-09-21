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

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private ActionOptionsEntity actionOptionsEntity;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private ObjectCollectionEntity objectCollectionEntity;

    public ActionStepEntity() {}

    public ActionStepEntity(ActionOptionsEntity actionOptionsEntity, ObjectCollectionEntity objectCollectionEntity) {
        this.actionOptionsEntity = actionOptionsEntity;
        this.objectCollectionEntity = objectCollectionEntity;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Action: ").append(actionOptionsEntity.getAction()).append(", ");
        sb.append("StateImages: [");
        objectCollectionEntity.getStateImages().forEach(si ->
                sb.append("(id=").append(si.getId())
                        .append(", name=").append(si.getName())
                        .append(", patterns=").append(si.getPatterns().size())
                        .append("), ")
        );
        if (!objectCollectionEntity.getStateImages().isEmpty()) {
            sb.setLength(sb.length() - 2); // Remove last ", "
        }
        sb.append("]");
        return sb.toString();
    }
}
