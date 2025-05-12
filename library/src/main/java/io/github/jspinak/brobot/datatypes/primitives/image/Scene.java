package io.github.jspinak.brobot.datatypes.primitives.image;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Scene {

    private Long id = -1L;
    private Pattern pattern;

    public Scene(Pattern pattern) {
        this.pattern = pattern;
    }

    public Scene(String filename) {
        this.pattern = new Pattern(filename);
    }

    @Override
    public String toString() {
        return "Scene{" +
                "id=" + id +
                ", pattern=" + (pattern != null ? pattern.getName() : "null") +
                '}';
    }
}
