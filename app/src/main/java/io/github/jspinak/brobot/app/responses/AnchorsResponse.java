package io.github.jspinak.brobot.app.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AnchorsResponse {

    private List<AnchorResponse> anchorList = new ArrayList<>();

}
