package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.AnchorsEntity;
import io.github.jspinak.brobot.app.web.requests.AnchorsRequest;
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

    public AnchorsEntity fromRequest(AnchorsRequest request) {
        if (request == null) {
            return null;
        }
        AnchorsEntity entity = new AnchorsEntity();
        entity.setId(request.getId());
        entity.setAnchorList(request.getAnchorList());
        return entity;
    }

    public AnchorsRequest toRequest(AnchorsEntity entity) {
        if (entity == null) {
            return null;
        }
        AnchorsRequest request = new AnchorsRequest();
        request.setId(entity.getId());
        request.setAnchorList(entity.getAnchorList()); // keep the Embeddable Anchor class for simplicity
        return request;
    }
}
