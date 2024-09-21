package io.github.jspinak.brobot.datatypes.primitives.image;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
