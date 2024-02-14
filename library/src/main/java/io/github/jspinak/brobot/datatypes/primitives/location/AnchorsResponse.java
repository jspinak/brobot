package io.github.jspinak.brobot.datatypes.primitives.location;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AnchorsResponse {

    private Long id = 0L;
    private List<Anchor> anchorList = new ArrayList<>();

    public AnchorsResponse(Anchors anchors) {
        if (anchors == null) return;
        id = anchors.getId();
        anchorList.addAll(anchors.getAnchorList());
    }
}
