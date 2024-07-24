package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.AnchorsEntity;
import io.github.jspinak.brobot.app.web.responses.AnchorsResponse;
import org.springframework.stereotype.Component;

@Component
public class AnchorsResponseMapper {

    public AnchorsResponse map(AnchorsEntity anchorsEntity) {
        AnchorsResponse anchorsResponse = new AnchorsResponse();
        anchorsResponse.setId(anchorsEntity.getId());
        anchorsResponse.setAnchorList(anchorsEntity.getAnchorList());
        return anchorsResponse;
    }

    public AnchorsEntity map(AnchorsResponse anchorsResponse) {
        AnchorsEntity anchorsEntity = new AnchorsEntity();
        anchorsEntity.setId(anchorsResponse.getId());
        anchorsEntity.setAnchorList(anchorsResponse.getAnchorList());
        return anchorsEntity;
    }
}
