package com.brobot.app.responses;

import com.brobot.app.database.entities.MatchHistoryEntity;
import com.brobot.app.database.mappers.AnchorMapper;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", uses = AnchorMapper.class)
@Component
public interface MatchHistoryResponseMapper {
    MatchHistoryResponseMapper INSTANCE = Mappers.getMapper(MatchHistoryResponseMapper.class);

    @Mapping(source = "snapshots", target = "snapshots", qualifiedByName = "mapToMatchSnapshotResponse")
    MatchHistoryResponse mapToResponse(MatchHistory matchHistory);

    @Mapping(source = "snapshots", target = "snapshots", qualifiedByName = "mapFromMatchSnapshotResponse")
    MatchHistory mapFromResponse(MatchHistoryResponse matchHistoryResponse);
}