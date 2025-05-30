package io.github.jspinak.brobot.datatypes.primitives.location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Anchors {

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

}
