package com.brobot.app.database.entities;

import com.brobot.app.database.embeddable.AnchorEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class AnchorsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "anchors_anchorList", joinColumns = @JoinColumn(name = "anchors_id"))
    private List<AnchorEmbeddable> anchorList = new ArrayList<>();

}
