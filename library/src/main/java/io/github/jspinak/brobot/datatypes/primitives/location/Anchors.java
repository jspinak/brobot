package io.github.jspinak.brobot.datatypes.primitives.location;

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

    public int size() {
        return anchors.size();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Anchors:");
        anchors.forEach(anchor -> stringBuilder.append(" ").append(anchor));
        return stringBuilder.toString();
    }
}
