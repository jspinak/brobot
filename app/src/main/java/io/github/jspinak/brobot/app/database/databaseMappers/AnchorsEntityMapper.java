package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.AnchorsEntity;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import org.springframework.stereotype.Component;

@Component
public class AnchorsEntityMapper {

    private final AnchorEmbeddableMapper anchorEmbeddableMapper;

    public AnchorsEntityMapper(AnchorEmbeddableMapper anchorEmbeddableMapper) {
        this.anchorEmbeddableMapper = anchorEmbeddableMapper;
    }

    public AnchorsEntity map(Anchors anchors) {
        AnchorsEntity anchorsEntity = new AnchorsEntity();
        anchorsEntity.setAnchorList(anchorEmbeddableMapper.mapAnchorList(anchors.getAnchorList()));
        return anchorsEntity;
    }

    public Anchors map(AnchorsEntity anchorsEntity) {
        Anchors anchors = new Anchors();
        anchors.setAnchorList(anchorEmbeddableMapper.mapAnchorEmbeddableList(anchorsEntity.getAnchorList()));
        return anchors;
    }

}
