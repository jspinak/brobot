package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.embeddable.AnchorEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AnchorEmbeddableMapper {

    private final PositionEmbeddableMapper positionEmbeddableMapper;

    public AnchorEmbeddableMapper(PositionEmbeddableMapper positionEmbeddableMapper) {
        this.positionEmbeddableMapper = positionEmbeddableMapper;
    }

    public AnchorEmbeddable map(Anchor anchor) {
        AnchorEmbeddable anchorEmbeddable = new AnchorEmbeddable();
        anchorEmbeddable.setAnchorInNewDefinedRegion(anchor.getAnchorInNewDefinedRegion());
        anchorEmbeddable.setPositionInMatch(positionEmbeddableMapper.map(anchor.getPositionInMatch()));
        return anchorEmbeddable;
    }

    public Anchor map(AnchorEmbeddable anchorEmbeddable) {
        Anchor anchor = new Anchor();
        anchor.setAnchorInNewDefinedRegion(anchorEmbeddable.getAnchorInNewDefinedRegion());
        anchor.setPositionInMatch(positionEmbeddableMapper.map(anchorEmbeddable.getPositionInMatch()));
        return anchor;
    }

    public List<AnchorEmbeddable> mapAnchorList(List<Anchor> anchorList) {
        List<AnchorEmbeddable> anchorEmbeddableList = new ArrayList<>();
        anchorList.forEach(anchor -> anchorEmbeddableList.add(map(anchor)));
        return anchorEmbeddableList;
    }

    public List<Anchor> mapAnchorEmbeddableList(List<AnchorEmbeddable> anchorEmbeddableList) {
        List<Anchor> anchorList = new ArrayList<>();
        anchorEmbeddableList.forEach(anchorEmbeddable -> anchorList.add(map(anchorEmbeddable)));
        return anchorList;
    }
}
