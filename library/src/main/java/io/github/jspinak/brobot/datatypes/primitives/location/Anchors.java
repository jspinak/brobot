package io.github.jspinak.brobot.datatypes.primitives.location;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Embeddable
@Getter
@Setter
public class Anchors {

    @ElementCollection
    @CollectionTable(name = "anchors_anchors", joinColumns = @JoinColumn(name = "anchors_id"))
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

    public boolean equals(Anchors a) {
        if (anchors.size() != a.getAnchors().size()) return false;
        for (int i=0; i<anchors.size(); i++)
            if (!anchors.get(i).equals(a.getAnchors().get(i))) return false;
        return true;
    }
}
