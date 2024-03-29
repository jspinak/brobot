package com.brobot.app.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL) // Include non-null properties only
@Getter
public class ImageResponse {

    private String name = "";
    private String imageBase64 = "";

}
