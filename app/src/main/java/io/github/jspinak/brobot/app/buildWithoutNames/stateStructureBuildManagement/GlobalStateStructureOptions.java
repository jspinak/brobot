package io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class GlobalStateStructureOptions {

    private StateStructureTemplate stateStructureTemplate;
}
