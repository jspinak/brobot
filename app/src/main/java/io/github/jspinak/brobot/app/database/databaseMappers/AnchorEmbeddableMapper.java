package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.embeddable.AnchorEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;

import java.util.ArrayList;
import java.util.List;

public class AnchorEmbeddableMapper {

    public static AnchorEmbeddable map(Anchor anchor) {
        AnchorEmbeddable anchorEmbeddable = new AnchorEmbeddable();
        anchorEmbeddable.setAnchorInNewDefinedRegion(anchor.getAnchorInNewDefinedRegion());
        anchorEmbeddable.setPositionInMatch(PositionEmbeddableMapper.map(anchor.getPositionInMatch()));
        return anchorEmbeddable;
    }

    public static Anchor map(AnchorEmbeddable anchorEmbeddable) {
        Anchor anchor = new Anchor();
        anchor.setAnchorInNewDefinedRegion(anchorEmbeddable.getAnchorInNewDefinedRegion());
        anchor.setPositionInMatch(PositionEmbeddableMapper.map(anchorEmbeddable.getPositionInMatch()));
        return anchor;
    }

    public static List<AnchorEmbeddable> mapAnchorList(List<Anchor> anchorList) {
        List<AnchorEmbeddable> anchorEmbeddableList = new ArrayList<>();
        anchorList.forEach(anchor -> anchorEmbeddableList.add(AnchorEmbeddableMapper.map(anchor)));
        return anchorEmbeddableList;
    }

    public static List<Anchor> mapAnchorEmbeddableList(List<AnchorEmbeddable> anchorEmbeddableList) {
        List<Anchor> anchorList = new ArrayList<>();
        anchorEmbeddableList.forEach(anchorEmbeddable -> anchorList.add(AnchorEmbeddableMapper.map(anchorEmbeddable)));
        return anchorList;
    }
}
