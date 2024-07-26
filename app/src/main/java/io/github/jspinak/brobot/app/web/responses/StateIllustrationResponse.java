package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class StateIllustrationResponse {

    private Mat screenshot;
    private ImageResponse illustratedScreenshot;
}
