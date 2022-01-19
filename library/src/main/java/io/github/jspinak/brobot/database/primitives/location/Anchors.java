package io.github.jspinak.brobot.database.primitives.location;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Anchors {

    private List<Anchor> anchors = new ArrayList<>();

    public void add(Anchor anchor) {
        anchors.add(anchor);
    }
}
