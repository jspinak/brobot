package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.MatchHistoryEntity;
import io.github.jspinak.brobot.app.web.requests.MatchHistoryRequest;
import io.github.jspinak.brobot.app.web.responses.MatchHistoryResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class MatchHistoryResponseMapper {

    private final MatchSnapshotResponseMapper matchSnapshotResponseMapper;

    public MatchHistoryResponseMapper(MatchSnapshotResponseMapper matchSnapshotResponseMapper) {
        this.matchSnapshotResponseMapper = matchSnapshotResponseMapper;
    }

    public MatchHistoryResponse map(MatchHistoryEntity matchHistoryEntity) {
        if (matchHistoryEntity == null) {
            return null;
        }
        MatchHistoryResponse matchHistoryResponse = new MatchHistoryResponse();
        matchHistoryResponse.setId(matchHistoryEntity.getId());
        matchHistoryResponse.setTimesSearched(matchHistoryEntity.getTimesSearched());
        matchHistoryResponse.setTimesFound(matchHistoryEntity.getTimesFound());
        matchHistoryResponse.setSnapshots(matchHistoryEntity.getSnapshots().stream()
                .map(matchSnapshotResponseMapper::map)
                .collect(Collectors.toList()));
        return matchHistoryResponse;
    }

    public MatchHistoryEntity map(MatchHistoryResponse matchHistoryResponse) {
        if (matchHistoryResponse == null) {
            return null;
        }
        MatchHistoryEntity matchHistoryEntity = new MatchHistoryEntity();
        matchHistoryEntity.setId(matchHistoryResponse.getId());
        matchHistoryEntity.setTimesSearched(matchHistoryResponse.getTimesSearched());
        matchHistoryEntity.setTimesFound(matchHistoryResponse.getTimesFound());
        matchHistoryEntity.setSnapshots(matchHistoryResponse.getSnapshots().stream()
                .map(matchSnapshotResponseMapper::map)
                .collect(Collectors.toList()));
        return matchHistoryEntity;
    }

    public MatchHistoryEntity fromRequest(MatchHistoryRequest request) {
        if (request == null) {
            return null;
        }
        MatchHistoryEntity entity = new MatchHistoryEntity();
        entity.setId(request.getId());
        entity.setTimesSearched(request.getTimesSearched());
        entity.setTimesFound(request.getTimesFound());
        entity.setSnapshots(request.getSnapshots().stream()
                .map(matchSnapshotResponseMapper::fromRequest)
                .collect(Collectors.toList()));
        return entity;
    }

    public MatchHistoryRequest toRequest(MatchHistoryEntity entity) {
        if (entity == null) {
            return null;
        }
        MatchHistoryRequest request = new MatchHistoryRequest();
        request.setId(entity.getId());
        request.setTimesSearched(entity.getTimesSearched());
        request.setTimesFound(entity.getTimesFound());
        request.setSnapshots(entity.getSnapshots().stream()
                .map(matchSnapshotResponseMapper::toRequest)
                .collect(Collectors.toList()));
        return request;
    }
}
