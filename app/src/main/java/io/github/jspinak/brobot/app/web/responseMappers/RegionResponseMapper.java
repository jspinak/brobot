package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.embeddable.RegionEmbeddable;
import io.github.jspinak.brobot.app.web.requests.RegionRequest;
import io.github.jspinak.brobot.app.web.responses.RegionResponse;
import org.springframework.stereotype.Component;

@Component
public class RegionResponseMapper {

    public RegionResponse map(RegionEmbeddable regionEmbeddable) {
        if (regionEmbeddable == null) {
            return null;
        }
        RegionResponse regionResponse = new RegionResponse();
        regionResponse.setX(regionEmbeddable.getX());
        regionResponse.setY(regionEmbeddable.getY());
        regionResponse.setW(regionEmbeddable.getW());
        regionResponse.setH(regionEmbeddable.getH());
        return regionResponse;
    }

    public RegionEmbeddable map(RegionResponse regionResponse) {
        if (regionResponse == null) {
            return null;
        }
        RegionEmbeddable regionEmbeddable = new RegionEmbeddable();
        regionEmbeddable.setX(regionResponse.getX());
        regionEmbeddable.setY(regionResponse.getY());
        regionEmbeddable.setW(regionResponse.getW());
        regionEmbeddable.setH(regionResponse.getH());
        return regionEmbeddable;
    }

    public RegionEmbeddable fromRequest(RegionRequest request) {
        if (request == null) return null;
        RegionEmbeddable entity = new RegionEmbeddable();
        entity.setX(request.getX());
        entity.setY(request.getY());
        entity.setW(request.getW());
        entity.setH(request.getH());
        return entity;
    }

    public RegionRequest toRequest(RegionEmbeddable embeddable) {
        if (embeddable == null) {
            return null;
        }
        RegionRequest request = new RegionRequest();
        request.setX(embeddable.getX());
        request.setY(embeddable.getY());
        request.setW(embeddable.getW());
        request.setH(embeddable.getH());
        return request;
    }
}

