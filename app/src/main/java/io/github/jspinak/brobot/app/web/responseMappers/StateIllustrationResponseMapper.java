package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.StateIllustrationEntity;
import io.github.jspinak.brobot.app.web.responses.StateIllustrationResponse;
import org.springframework.stereotype.Component;

@Component
public class StateIllustrationResponseMapper {

    private final ImageResponseMapper imageResponseMapper;

    public StateIllustrationResponseMapper(ImageResponseMapper imageResponseMapper) {
        this.imageResponseMapper = imageResponseMapper;
    }

    public StateIllustrationResponse map(StateIllustrationEntity stateIllustrationEntity) {
        if (stateIllustrationEntity == null) {
            return null;
        }
        StateIllustrationResponse response = new StateIllustrationResponse();
        response.setScreenshot(imageResponseMapper.map(stateIllustrationEntity.getScreenshot()));
        response.setIllustratedScreenshot(imageResponseMapper.map(stateIllustrationEntity.getIllustratedScreenshot()));
        return response;
    }

    public StateIllustrationEntity map(StateIllustrationResponse response) {
        if (response == null) {
            return null;
        }
        StateIllustrationEntity stateIllustrationEntity = new StateIllustrationEntity();
        stateIllustrationEntity.setScreenshot(imageResponseMapper.map(response.getScreenshot()));
        stateIllustrationEntity.setIllustratedScreenshot(imageResponseMapper.map(response.getIllustratedScreenshot()));
        return stateIllustrationEntity;
    }
}
