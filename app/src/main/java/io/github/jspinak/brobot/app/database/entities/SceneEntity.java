package io.github.jspinak.brobot.app.database.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SceneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(cascade = CascadeType.ALL)
    private PatternEntity pattern;

}
