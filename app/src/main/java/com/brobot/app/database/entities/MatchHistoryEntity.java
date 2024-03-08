package com.brobot.app.database.entities;

import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class MatchHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int timesSearched = 0;
    private int timesFound = 0;
    @ElementCollection
    @CollectionTable(name = "snapshots", joinColumns = @JoinColumn(name = "matchHistory_id"))
    private List<MatchSnapshotEntity> snapshots = new ArrayList<>();
}
