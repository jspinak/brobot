// File: io/github/jspinak/brobot/dsl/model/Parameter.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Parameter {
    private String name;
    private String type; // Consider Enum: boolean, string, int, double, etc.
}