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
public class TransitionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromState;
    private String toState;
    private boolean success;

    @Override
    public String toString() {
        return "TransitionLog{" +
                "id=" + id +
                ", fromState='" + fromState + '\'' +
                ", toState='" + toState + '\'' +
                ", success=" + success +
                '}';
    }
}
