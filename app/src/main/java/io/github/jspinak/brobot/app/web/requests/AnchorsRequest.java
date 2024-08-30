package io.github.jspinak.brobot.app.web.requests;

import io.github.jspinak.brobot.app.database.embeddable.AnchorEmbeddable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AnchorsRequest {
    private Long id;
    private List<AnchorEmbeddable> anchorList = new ArrayList<>();
}
