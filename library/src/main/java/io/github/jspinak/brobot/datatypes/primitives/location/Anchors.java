package io.github.jspinak.brobot.datatypes.primitives.location;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Anchors {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "anchors_anchorList", joinColumns = @JoinColumn(name = "anchors_id"))
    private List<Anchor> anchorList = new ArrayList<>();

    public void add(Anchor anchor) {
        anchorList.add(anchor);
    }

    public int size() {
        return anchorList.size();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Anchors:");
        anchorList.forEach(anchor -> stringBuilder.append(" ").append(anchor));
        return stringBuilder.toString();
    }

    public boolean equals(Anchors a) {
        if (anchorList.size() != a.getAnchorList().size()) return false;
        for (int i = 0; i< anchorList.size(); i++)
            if (!anchorList.get(i).equals(a.getAnchorList().get(i))) return false;
        return true;
    }
}
