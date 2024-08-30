package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.embeddable.SearchRegionsEmbeddable;
import io.github.jspinak.brobot.app.web.requests.SearchRegionsRequest;
import io.github.jspinak.brobot.app.web.responses.SearchRegionsResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SearchRegionsResponseMapper {

    private final RegionResponseMapper regionResponseMapper;

    public SearchRegionsResponseMapper(RegionResponseMapper regionResponseMapper) {
        this.regionResponseMapper = regionResponseMapper;
    }

    public SearchRegionsResponse map(SearchRegionsEmbeddable searchRegionsEmbeddable) {
        if (searchRegionsEmbeddable == null) {
            return null;
        }
        SearchRegionsResponse searchRegionResponse = new SearchRegionsResponse();
        searchRegionResponse.setRegions(searchRegionsEmbeddable.getRegions().stream()
                .map(regionResponseMapper::map)
                .collect(Collectors.toList()));
        searchRegionResponse.setFixedRegion(regionResponseMapper.map(searchRegionsEmbeddable.getFixedRegion()));
        return searchRegionResponse;
    }

    public SearchRegionsEmbeddable map(SearchRegionsResponse searchRegionResponse) {
        if (searchRegionResponse == null) {
            return null;
        }
        SearchRegionsEmbeddable searchRegionsEmbeddable = new SearchRegionsEmbeddable();
        searchRegionsEmbeddable.setRegions(searchRegionResponse.getRegions().stream()
                .map(regionResponseMapper::map)
                .collect(Collectors.toList()));
        searchRegionsEmbeddable.setFixedRegion(regionResponseMapper.map(searchRegionResponse.getFixedRegion()));
        return searchRegionsEmbeddable;
    }

    public SearchRegionsEmbeddable fromRequest(SearchRegionsRequest request) {
        if (request == null) return null;
        SearchRegionsEmbeddable entity = new SearchRegionsEmbeddable();
        entity.setRegions(request.getRegions().stream()
                .map(regionResponseMapper::fromRequest)
                .collect(Collectors.toList()));
        entity.setFixedRegion(regionResponseMapper.fromRequest(request.getFixedRegion()));
        return entity;
    }

    public SearchRegionsRequest toRequest(SearchRegionsEmbeddable embeddable) {
        if (embeddable == null) {
            return null;
        }
        SearchRegionsRequest request = new SearchRegionsRequest();
        request.setRegions(embeddable.getRegions().stream()
                .map(regionResponseMapper::toRequest)
                .collect(Collectors.toList()));
        request.setFixedRegion(regionResponseMapper.toRequest(embeddable.getFixedRegion()));
        return request;
    }
}

