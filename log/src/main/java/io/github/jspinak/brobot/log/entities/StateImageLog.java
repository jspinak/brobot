package io.github.jspinak.brobot.log.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class StateImageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String stateOwnerName;
    private boolean found;

    @Override
    public String toString() {
        return "StateImageLog{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", stateOwnerName='" + stateOwnerName + '\'' +
                ", found=" + found +
                '}';
    }

}
