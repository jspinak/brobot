package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL) // Include non-null properties only
@Getter
@Setter
public class RegionResponse {

    private Long id;
    private int x;
    private int y;
    private int w;
    private int h;

}