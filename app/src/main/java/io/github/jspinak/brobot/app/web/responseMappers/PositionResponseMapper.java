package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.embeddable.PositionEmbeddable;
import io.github.jspinak.brobot.app.web.responses.PositionResponse;
import org.springframework.stereotype.Component;

@Component
public class PositionResponseMapper {

    public PositionResponse map(PositionEmbeddable positionEmbeddable) {
        if (positionEmbeddable == null) {
            return null;
        }
        PositionResponse positionResponse = new PositionResponse();
        positionResponse.setPercentW(positionEmbeddable.getPercentW());
        positionResponse.setPercentH(positionEmbeddable.getPercentH());
        return positionResponse;
    }

    public PositionEmbeddable map(PositionResponse positionResponse) {
        if (positionResponse == null) {
            return null;
        }
        PositionEmbeddable positionEmbeddable = new PositionEmbeddable();
        positionEmbeddable.setPercentW(positionResponse.getPercentW());
        positionEmbeddable.setPercentH(positionResponse.getPercentH());
        return positionEmbeddable;
    }
}
