package io.github.jspinak.brobot.app.responses;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AnchorsResponse {

    private List<AnchorResponse> anchorList = new ArrayList<>();

}
