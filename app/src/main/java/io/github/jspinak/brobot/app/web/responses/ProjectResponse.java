package io.github.jspinak.brobot.app.web.responses;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * A ProjectRequest to set up a new project has only the name field.
 * A ProjectRequest embedded in a StateRequest has all fields.
 */
@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private Set<StateResponse> states = new HashSet<>();
}