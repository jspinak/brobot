package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.jspinak.brobot.app.database.entities.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ObjectCollectionResponse {

    private Long id;
    private List<StateLocationEntity> stateLocations = new ArrayList<>();
    private List<StateImageEntity> stateImages = new ArrayList<>();
    private List<StateRegionEntity> stateRegions = new ArrayList<>();
    private List<StateStringEntity> stateStrings = new ArrayList<>();
    private List<MatchesEntity> matches = new ArrayList<>();
    private List<SceneEntity> scenes = new ArrayList<>();

}
