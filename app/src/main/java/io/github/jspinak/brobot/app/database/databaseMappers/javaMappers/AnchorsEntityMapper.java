package io.github.jspinak.brobot.app.database.databaseMappers.javaMappers;

import io.github.jspinak.brobot.app.database.embeddable.AnchorEmbeddable;
import io.github.jspinak.brobot.app.database.entities.AnchorsEntity;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;

import java.util.ArrayList;
import java.util.List;

public class AnchorsEntityMapper {

    public static AnchorsEntity map(Anchors anchors) {
        AnchorsEntity anchorsEntity = new AnchorsEntity();
        anchorsEntity.setAnchorList(AnchorEmbeddableMapper.mapAnchorList(anchors.getAnchorList()));
        return anchorsEntity;
    }

    public static Anchors map(AnchorsEntity anchorsEntity) {
        Anchors anchors = new Anchors();
        anchors.setAnchorList(AnchorEmbeddableMapper.mapAnchorEmbeddableList(anchorsEntity.getAnchorList()));
        return anchors;
    }

}
