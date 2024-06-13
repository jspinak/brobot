package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.AnchorsEntity;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;

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
