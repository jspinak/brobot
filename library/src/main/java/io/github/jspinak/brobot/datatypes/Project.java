package io.github.jspinak.brobot.datatypes;

import lombok.Getter;

@Getter
public class Project {
    private static Project instance;
    private Long id = 0L;
    private String name;

    private Project() {}

    public static synchronized Project getInstance() {
        if (instance == null) {
            instance = new Project();
        }
        return instance;
    }

    public void setProject(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void reset() {
        id = null;
        name = null;
    }
}
