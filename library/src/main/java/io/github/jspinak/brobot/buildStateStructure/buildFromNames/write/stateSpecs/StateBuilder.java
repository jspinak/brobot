package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.stateSpecs;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Writes Java code for the declaration of a new State (everything after 'State state =').
 * Includes .withImages(...) and .withRegions(...)
 */
@Component
public class StateBuilder {

    public String getCode(List<String> imageNames, List<String> regionNames) {
        StringBuilder str = new StringBuilder();
        str.append("new $T($L)");
        if (!imageNames.isEmpty()) {
            str.append("\n\t.withImages(");
            for (String name : imageNames) {
                str.append(name);
                if (imageNames.indexOf(name) < imageNames.size() - 1) str.append(", ");
            }
        }
        if (!regionNames.isEmpty()) {
            str.append("\n\t.withRegions(");
            for (String name : regionNames) {
                str.append(name);
                if (regionNames.indexOf(name) < imageNames.size() - 1) str.append(", ");
            }
        }
        str.append(")\n\t.build()");
        return str.toString();
    }

}
