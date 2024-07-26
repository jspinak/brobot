package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.embeddable.PositionEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import org.springframework.stereotype.Component;

@Component
public class PositionEmbeddableMapper {

    public PositionEmbeddable map(Position position) {
        PositionEmbeddable positionEmbeddable = new PositionEmbeddable();
        positionEmbeddable.setPercentW(position.getPercentW());
        positionEmbeddable.setPercentH(position.getPercentH());
        return positionEmbeddable;
    }

    public Position map(PositionEmbeddable positionEmbeddable) {
        Position position = new Position();
        position.setPercentW(positionEmbeddable.getPercentW());
        positionEmbeddable.setPercentH(positionEmbeddable.getPercentH());
        return position;
    }
}
