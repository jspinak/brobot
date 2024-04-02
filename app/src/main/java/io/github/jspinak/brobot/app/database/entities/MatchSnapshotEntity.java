package io.github.jspinak.brobot.app.database.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class MatchSnapshotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @OneToOne(cascade = CascadeType.ALL)
    private ActionOptionsEntity actionOptions = new ActionOptionsEntity();
    @ElementCollection
    @CollectionTable(name = "matchList", joinColumns = @JoinColumn(name = "matchSnapshot_id"))
    private List<MatchEntity> matchList = new ArrayList<>();
    private String text = "";
    private double duration = 0.0;
    private LocalDateTime timeStamp;
    private boolean actionSuccess = false;
    private boolean resultSuccess = false;
    private String state = "null";

}
