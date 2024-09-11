package io.github.jspinak.brobot.app.web.requests;

import lombok.Data;

/**
 * States are not sent with a request. The id is only sent went the project exists.
 */
@Data
public class ProjectRequest {
    private Long id;
    private String name;
}
