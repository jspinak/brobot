package io.github.jspinak.brobot.app.stateStructureBuilders;

import io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.GetFiles;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter
public class GetPatternsFromBundlePath {

    private final GetFiles getFiles;

    private List<Pattern> patterns = new ArrayList<>();

    public GetPatternsFromBundlePath(GetFiles getFiles) {
        this.getFiles = getFiles;
    }

    public List<Pattern> savePatternsToList() {
        getFiles.getImageNames().forEach(name -> patterns.add(new Pattern(name)));
        return patterns;
    }
}
