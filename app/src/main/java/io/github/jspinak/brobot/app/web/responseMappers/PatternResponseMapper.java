package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.app.web.responses.PatternResponse;
import io.github.jspinak.brobot.app.web.responses.StateImageResponse;
import io.github.jspinak.brobot.app.web.responses.StateResponse;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import io.github.jspinak.brobot.stringUtils.Base64Converter;
import org.springframework.stereotype.Component;

@Component
public class PatternResponseMapper {

    private final ImageResponseMapper imageResponseMapper;

    public PatternResponseMapper(ImageResponseMapper imageResponseMapper) {
        this.imageResponseMapper = imageResponseMapper;
    }

    public PatternResponse map(PatternEntity patternEntity) {
        PatternResponse patternResponse = new PatternResponse();
        patternResponse.setId(patternEntity.getId());
        patternResponse.setName(patternEntity.getName());
        patternResponse.setImage(imageResponseMapper.map(patternEntity.getImage()));
        return patternResponse;
    }
}
