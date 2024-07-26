package io.github.jspinak.brobot.app.database.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class StateIllustrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @OneToOne(cascade = CascadeType.ALL)
    private ImageEntity illustratedScreenshot;

}
