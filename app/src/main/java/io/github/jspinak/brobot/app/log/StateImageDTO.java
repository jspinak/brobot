package io.github.jspinak.brobot.app.log;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class StateImageDTO {
    private Long id;
    private Long projectId;
    private String name;
    private String stateOwnerName;
    private List<String> imagesBase64 = new ArrayList<>();
}